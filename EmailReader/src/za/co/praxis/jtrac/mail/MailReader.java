package za.co.praxis.jtrac.mail;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.util.HtmlUtils;

import za.co.praxis.jtrac.domain.Attachments;
import za.co.praxis.jtrac.domain.Config;
import za.co.praxis.jtrac.domain.Items;
import za.co.praxis.jtrac.domain.SpaceSequence;
import za.co.praxis.jtrac.domain.Spaces;
import za.co.praxis.jtrac.domain.State;
import za.co.praxis.jtrac.domain.Users;
import za.co.praxis.jtrac.domain.ZCategories;
import za.co.praxis.jtrac.domain.ZSpaceEmail;
import za.co.praxis.jtrac.domain.ZSpaceSeverityPeriod;
import za.co.praxis.jtrac.util.EncryptDecryptHelper;
import za.co.praxis.jtrac.util.Helper;

import com.sun.mail.util.BASE64DecoderStream;

public class MailReader {
	
	private static Logger logger = Logger.getLogger(MailReader.class.getName());

	public MailReader() {
		try {
			this.helper = new Helper();
		} catch (InstantiationException e) {
			System.out.println(e.getMessage());
		} catch (IllegalAccessException e) {
			System.out.println(e.getMessage());
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}

	private Helper helper = null;
	private String saveDirectory;
	private Attachments attachment = null;
	private List<Object> nonBase64Parts = new ArrayList<Object>();
	private String emailDetail = "";
	private List<String> base64Content = new ArrayList<String>();
	String attachFiles = "";
	String fileName = "";
	private String emailContent = "";
	private MimeBodyPart attachmentPart = null;
	private String configURL = "";
	/**
	 * Sets the directory where attached files will be stored.
	 * 
	 * @param dir
	 *            absolute path of the directory
	 */
	public void setSaveDirectory(String dir) {
		this.saveDirectory = dir;
	}
	
	/**
	 * Downloads new messages and saves attachments to disk if any.
	 * 
	 * @param host
	 * @param port
	 * @param userName
	 * @param password
	 * @throws Exception 
	 */
	public void processEmails(String host, String port, String userName, String password, Spaces space, Users user, String email) throws Exception {
		
		try {
			// connects to the message store
			Properties properties = System.getProperties();
		    properties.setProperty("mail.store.protocol", "imap"); 
		    Session session = Session.getDefaultInstance(properties);
		    Store store = session.getStore("imap");
		    store.connect(host, Integer.valueOf(port), userName, password);

			// opens the inbox folder
			Folder folderInbox = store.getFolder("INBOX");
			folderInbox.open(Folder.READ_WRITE);
			FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

			Message[] arrayMessages = folderInbox.search(ft);
			
			 
			for (int i = 0; i < arrayMessages.length; i++) {
				
				MimeMessageParser message = new MimeMessageParser((MimeMessage) arrayMessages[i]);
				message.parse();
				
				String from = null;
				String subject = null;
				
				try {
					from = message.getFrom();
					subject = message.getSubject();
				} catch (Exception e1) {
					logger.info(e1.getMessage());
				}
				message.getAttachmentList();

				
				// String manipulation to get separate sender's email address from sender's name
				String originator = from;
				try
				{
					originator = from.substring(0, from.indexOf('<') - 1);
					from = from.replace(originator, "");
					from = from.replace("<", "");
					from = from.replace(">", "");
					from = from.replace(" ", "");
				}
				catch (StringIndexOutOfBoundsException e)
				{
					logger.info(e.getMessage());
					originator = from;
				}
				
				String originatorContact = from;
				
				List<Users> users = new ArrayList<Users>();
				users = helper.getUsersByEmail(email.toLowerCase());
				List<SpaceSequence> spaceSequences = new ArrayList<SpaceSequence>();
				spaceSequences = helper.getNextSequenceNumberForSpace(space);
				List<ZSpaceSeverityPeriod> spaceSeverityPeriods = new ArrayList<ZSpaceSeverityPeriod>();
				spaceSeverityPeriods = helper.getMediumSpaceSeverityPeriodBySpace(space);
				List<ZCategories> categories = new ArrayList<ZCategories>();
				categories = helper.getSupportCategory();
				
				if(!users.isEmpty() && !spaceSequences.isEmpty() && !spaceSeverityPeriods.isEmpty()) {
				
					logger.info("User obtained by email: " + users.get(0).getName());
					logger.info("Space sequence obtained: " + spaceSequences.get(0).getNextSeqNum());
					logger.info("Originator: " + originator);
					logger.info("Originator contact: " + originatorContact);
					
					Items item = new Items();
					item.setSpaces(space);
					item.setSequenceNum(spaceSequences.get(0).getNextSeqNum());
					item.setTimeStamp(new Date());
					
					categories = helper.getAllCategories();
					item.setZCategories(categories.get(0));
					
					Calendar cal = Calendar.getInstance();
					cal.setTime(message.getMimeMessage().getSentDate());
					cal.add(Calendar.HOUR, spaceSeverityPeriods.get(0).getPeriod());
					item.setDueDate(cal.getTime());
					item.setDateAdded(message.getMimeMessage().getSentDate());
					item.setUsersByLoggedBy(users.get(0));
					item.setUsersByAssignedTo(user);
					item.setSummary(subject);
					item.setDetail(message.getHtmlContent());
					item.setOriginator(originator);
					item.setOriginatorContact(originatorContact);
					item.setZSpaceSeverityPeriod(spaceSeverityPeriods.get(0));
					item.setStatus(State.OPEN);
					item.setVersion(0);
					
					helper.insertItem(item, message.getAttachmentList());
					
					List<Config> configs = new ArrayList<Config>();
					configs = helper.getAllConfigurations();
					String configEmail = "";
					String configHost = "";
					String configUsername = "";
					String configPassword = "";
					String configPort = "";		
					String configTls = "";
					
							
					for(Config config : configs) {
						
						if(config.getParam().equals("jtrac.url.base"))
							configURL = config.getValue();
						if(config.getParam().equals("mail.from"))
							configEmail = config.getValue();
						if(config.getParam().equals("mail.server.host"))
							configHost = config.getValue();
						if(config.getParam().equals("mail.server.password"))
							configPassword = config.getValue();
						if(config.getParam().equals("mail.server.username"))
							configUsername = config.getValue();
						if(config.getParam().equals("mail.server.port"))
							configPort = config.getValue();
						if(config.getParam().equals("mail.server.starttls.enable"))
							configTls = config.getValue();
					}
					
					sendEmailToAssignedPerson(item, configHost, configPort, configTls, configUsername, configPassword, configEmail, user.getEmail(), space, item.getSequenceNum());
				}
				
				try {
				    Thread.sleep(1000);
				} catch(InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}

			}

			// disconnect
			folderInbox.close(false);
			store.close();
		} catch (NoSuchProviderException ex) {
			logger.info(ex.getMessage());
			System.out.println("No provider for imap.");
			ex.printStackTrace();
		} catch (MessagingException ex) {
			logger.info(ex.getMessage());
			System.out.println("Could not connect to the message store");
			ex.printStackTrace();
		} catch (IOException ex) {
			logger.info(ex.getMessage());
			ex.printStackTrace();
		}
	}

	private String processNonMultiPartMessage(Message message) throws IOException, MessagingException {
		
		String messageContent = "";
		
		Object content = message.getContent();
		
		if (content != null) {
			messageContent = content.toString();
		}
		
		return messageContent;
	}

	private void processMultiPartMessage( Message message) throws IOException, MessagingException,
			UnsupportedEncodingException {
		Object messageContent;
		// content may contain attachments
		Multipart multiPart = (Multipart) message.getContent();
		
		int numberOfParts = multiPart.getCount();
		
		for (int partCount = 0; partCount < numberOfParts; partCount++) {
			
			MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
			
			if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
				
				//if(AllAttachFile.exists()){
					String saveDirectory = helper.getConfigurationByParam("attachments_zip").get(0).getValue();
					
					// this part is attachment
					String fileName = UUID.randomUUID().toString();
					String attachmentFileName = fileName + "#" + part.getFileName();



					part.saveFile(saveDirectory + File.separator + attachmentFileName);
					
				
					if (attachment == null)
					{
					  attachment = new Attachments();
					}
					
					logger.info("Item has attachment(s)");
				
				
			} else {
				// this part may be the message content							
				messageContent = part.getContent();
				
				if (messageContent instanceof BASE64DecoderStream) {
					//addBase64Content(messageContent);
					addBase64Content(messageContent);
				} else if(messageContent instanceof MimeMultipart) {
					processMultiPartComponent(messageContent);								
				} else {
					addNonBase64Content(messageContent);								
				}
			}
			
		}

		if (attachFiles.length() > 1) {
			attachFiles = attachFiles.substring(0,
					attachFiles.length() - 2);
		}
	}

	private void processMultiPartComponent(Object messageContent) throws MessagingException, IOException,
			UnsupportedEncodingException {
		MimeMultipart msgPart = (MimeMultipart)messageContent;
		
		for(int k=0; k<msgPart.getCount(); k++) {
			
			BodyPart bp = msgPart.getBodyPart(k);
			
			if (bp.getContent() instanceof BASE64DecoderStream) {
				
				Object content = bp.getContent();
				
			    addBase64Content(content);
			    
			} else {
				addNonBase64Content(messageContent);
			}
		}
	}

	private void addBase64Content(Object content) throws IOException,
			UnsupportedEncodingException {
		BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) content;
		byte[] byteArray = IOUtils.toByteArray(base64DecoderStream);
		byte[] encodedBase64 = Base64.encodeBase64(byteArray);
		base64Content.add(new String(encodedBase64, "UTF-8"));		
	}

	private void addNonBase64Content(Object messageContent) {
		
		if(nonBase64Parts.size() == 0){
			nonBase64Parts.add(messageContent);
		}
		else{
			if(messageContent.toString().contains("<html"))
			{   nonBase64Parts.clear();
				nonBase64Parts.add(messageContent);
			}
		}
	}

	private String getBody(Object msgContent) throws MessagingException,
			IOException {

		String content = "";

		/* Check if content is pure text/html or in parts */
		if (msgContent instanceof MimeMultipart) {

			//Multipart multipart = (Multipart) msgContent;
			MimeMultipart multipart = (MimeMultipart) msgContent;

			System.out.println("BodyPart " + "MultiPartCount: "
					+ multipart.getCount());

			for (int j = 0; j < multipart.getCount(); j++) {

				BodyPart bodyPart = multipart.getBodyPart(j);

				String disposition = bodyPart.getDisposition();

				if (disposition != null
						&& (disposition.equalsIgnoreCase("ATTACHMENT"))) {
					System.out.println("Mail has some attachment");

					DataHandler handler = bodyPart.getDataHandler();
					System.out.println("file name : " + handler.getName());
				} else {
					content += getText(bodyPart); // the changed code
				}
			}
		} 
	   else
		content += msgContent.toString();
		
		return  content;

	}

	/**
	 * Return the primary text content of the message.
	 */
	private String getText(Part p) throws MessagingException, IOException {

		boolean textIsHtml = false;

		if (p.isMimeType("text/*")) {
			String s = (String) p.getContent();
			textIsHtml = p.isMimeType("text/html");
			return s;
		}

		if (p.isMimeType("multipart/alternative")) {
			// prefer html text over plain text
			Multipart mp = (Multipart) p.getContent();
			String text = null;
			for (int i = 0; i < mp.getCount(); i++) {
				Part bp = mp.getBodyPart(i);
				if (bp.isMimeType("text/plain")) {
					if (text == null)
						text = getText(bp);
					continue;
				} else if (bp.isMimeType("text/html")) {
					String s = getText(bp);
					if (s != null)
						return s;
				} else {
					return getText(bp);
				}
			}
			return text;
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				String s = getText(mp.getBodyPart(i));
				if (s != null)
					return s;
			}
		}

		return null;
	}
	
	public String addHeaderAndFooter(StringBuffer html) {
		StringBuffer sb = new StringBuffer();
		// additional cosmetic tweaking of e-mail layout
		// style just after the body tag does not work for a minority of clients
		// like gmail, thunderbird etc.
		// ItemUtils adds the main inline CSS when generating the email content,
		// so we gracefully degrade
		sb
				.append("<html><body><style type='text/css'>table.jtrac th, table.jtrac td { padding-left: 0.2em; padding-right: 0.2em; }</style>");
		sb.append(html);
		sb.append("</html>");
		return sb.toString();
	}

	public void sendEmailToAssignedPerson(Items item, String host, String port, String tls, final String userName, final String password, String fromAddress, String toAddress, Spaces space, BigDecimal sequencNum) {
		
		try{
			
			// Get system properties
		      Properties properties = System.getProperties();

		      // Setup mail server
		      properties.setProperty("mail.smtp.host", host);
		      properties.put("mail.smtp.port", port);
		      properties.put("mail.smtp.starttls.enable", Boolean.parseBoolean(tls));
		      properties.put("mail.smtp.auth", "false");

		      // Get the default Session object.
		      Session session = Session.getDefaultInstance(properties);
		      session=Session.getInstance(properties, new Authenticator()
		      {
		          @Override
		          protected PasswordAuthentication getPasswordAuthentication() {
		              return new PasswordAuthentication(userName, password);
		          }
		      });
		      session.setDebug(true);

		         // Create a default MimeMessage object.
		         MimeMessage message = new MimeMessage(session);
		         MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

		         // Set From: header field of the header.
		         message.setFrom(new InternetAddress(fromAddress));

		         // Message To: User on level above current assigned user
		         message.addRecipient(Message.RecipientType.TO,
		                                  new InternetAddress(toAddress));
		         message.addRecipient(Message.RecipientType.BCC, new InternetAddress(fromAddress));
		         System.out.println("Email to: " + toAddress);

		         // Subject
		         //message.setSubject(Helper.EMAIL_NOTIFICATION_SUBJECT);
		         message.setSubject(space.getPrefixCode() + "-" + item.getSequenceNum() + " - " + item.getSummary());
		         StringBuffer sb = new StringBuffer();
		         String tdCommonStyle = "border: 1px solid black";
		         String tableStyle = " class='jtrac' style='border-collapse: collapse; font-family: Arial; font-size: 75%'";
		         String tdStyle = "";
		         String thStyle = "";
		         String altStyle = "";
		         String labelStyle = "";
		         tdStyle = " style='" + tdCommonStyle + "'";
		         thStyle = " style='" + tdCommonStyle + "; background: #CCCCCC'";
		         altStyle = " style='background: #e1ecfe'";
		         labelStyle = " style='" + tdCommonStyle + "; background: #CCCCCC; font-weight: bold; text-align: right'";
		            
		         sb.append("<p>");
		         sb.append("<p style='font-family: Arial; font-size: 75%'>A ticket for " + space.getName() + " has been logged and assigned to you for your attention.</p>");
		         sb.append("</p>");
		         sb.append("<br/><br/>");
		         
		         sb.append("<table width='100%'" + tableStyle + ">");
		         sb.append("<tr" + altStyle + ">");
		         sb.append("  <td" + labelStyle + ">" + "ID" + "</td>");
		         sb.append("  <td" + tdStyle + ">" + space.getPrefixCode() + "-" + item.getSequenceNum() + "</td>");
		         sb.append("</tr>");
		         
		         sb.append("<tr>");
		         sb.append("  <td width='15%'" + labelStyle + ">" + "Status" + "</td>");
		         
		         String status = "";
		         
		         switch(item.getStatus()) {
			         case 0: 
			        	 status = "New";
			        	 break;
			         case 1:
			        	 status = "Open";
			        	 break;
			         case 88:
			        	 status = "On Hold";
			         case 99:
			        	 status = "Closed";
		        	 default:
		        		 status = "Open";
		         }
		         
		         sb.append("  <td" + tdStyle + ">" + status + "</td>");
		         sb.append("  <td" + labelStyle + ">" + "Logged By" + "</td>");
		         sb.append("  <td" + tdStyle + ">" + item.getUsersByLoggedBy().getName() + "</td>");
		         sb.append("  <td" + labelStyle + ">" + "Assigned To" + "</td>");
		         sb.append("  <td width='15%'" + tdStyle + ">" + (item.getUsersByAssignedTo() == null ? "" : item.getUsersByAssignedTo().getName()) + "</td>");
		         sb.append("</tr>");
		         
		         sb.append("<tr" + altStyle + ">");
		         sb.append("  <td" + labelStyle + ">" + "Summary" + "</td>");
		         sb.append("  <td colspan='5'" + tdStyle + ">" + HtmlUtils.htmlEscape(item.getSummary()) + "</td>");
		         sb.append("</tr>");
		         
		         sb.append("<tr" + altStyle + ">");
		         sb.append("  <td" + labelStyle + ">" + "Date Logged" + "</td>");
		         sb.append("  <td colspan='5'" + tdStyle + ">" + item.getDateAdded() + "</td>");
		         sb.append("</tr>");
		         
		         sb.append("<tr" + altStyle + ">");
		         sb.append("  <td" + labelStyle + ">" + "Due Date" + "</td>");
		         sb.append("  <td colspan='5'" + tdStyle + ">" + item.getDueDate() + "</td>");
		         sb.append("</tr>");
		         
		         sb.append("<tr" + altStyle + ">");
		         sb.append("  <td" + labelStyle + ">" + "Originator" + "</td>");
		         sb.append("  <td colspan='5'" + tdStyle + ">" + item.getOriginator() + "</td>");
		         sb.append("</tr>");
		         
		         sb.append("<tr" + altStyle + ">");
		         sb.append("  <td" + labelStyle + ">" + "Originator Contact" + "</td>");
		         sb.append("  <td colspan='5'" + tdStyle + ">" + item.getOriginatorContact() + "</td>");
		         sb.append("</tr>");
		         
		         sb.append("<tr" + altStyle + ">");
		         sb.append("  <td" + labelStyle + ">" + "Priority" + "</td>");
		         sb.append("  <td colspan='5'" + tdStyle + ">" + item.getZSpaceSeverityPeriod().getZSeverities().getDescription() + "</td>");
		         sb.append("</tr>");
		         
		         sb.append("</table>");
		         
		         sb.append("<br/><br/>");
		         sb.append("<p>");
		         
		         String URL = configURL +"/app/item/" + space.getPrefixCode() + "-" + sequencNum;
		         sb.append("<p style='font-family: Arial; font-size: 75%'>Please go to <a href=" + URL + ">" + URL + "</a> for more details.</p>");
		         sb.append("</p>");
		         
		         sb.append("</body>");
		         
		      // Message content
		         message.setContent(addHeaderAndFooter(sb), "text/html");

		         
		         // Send message
		         Transport.send(message);
		         for(Address address : message.getRecipients(RecipientType.TO)) {
		        	 logger.info("Notification sent to: " + address.toString());
		         }			
		         
		       
			System.out.println("Sent message successfully....");
		}
		catch (MessagingException mex)
		{
			logger.info(mex.getMessage());
			mex.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		List<ZSpaceEmail> spaceEmails = new ArrayList<ZSpaceEmail>();
		Helper helper = new Helper();
		//Helper.establishConnection();
		
		FileHandler fh = null;
		String logFile = "mailreader_log_" + new SimpleDateFormat("dd-MM-yyyy").format(new Date());
		File currentFile = new File(MailReader.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		File dir = currentFile.getAbsoluteFile().getParentFile();
		String path = dir.toString() + "\\Jtrac_logs\\" + logFile + ".log";
		System.out.println(path);
		File file  = new File(path);
		
		
		if(!file.exists()) {
			try {
				file.createNewFile();
				
			} catch (IOException e2) {
				logger.info(e2.getMessage());
			}
		}
		
		try {
			fh = new FileHandler(path);
		} catch (SecurityException e1) {
			System.out.println(e1.getMessage());
			
		} catch (IOException e1) {
			System.out.println(e1.getMessage());
		}   
		//logger.addHandler(fh);
		
		String sourcePath = MailReader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = null;
		try {
			decodedPath = URLDecoder.decode(sourcePath, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			System.out.println(e1.getMessage());
		}
		
		System.out.println(decodedPath);
		
		logger.info("Obtaining Data from table Space Emails...");
		try {
			spaceEmails = helper.getAllSpaceEmails();
			logger.info("Space Emails Obtained.");
		} catch (Exception exception) {
			System.out.println(exception.getMessage());
			logger.info(exception.getMessage());
		}

		for(ZSpaceEmail spaceEmail : spaceEmails) {
			
			String host = spaceEmail.getHost();
			String port = spaceEmail.getPort();
			String userName = spaceEmail.getUsername();
			String password = spaceEmail.getPassword();
			String email = spaceEmail.getEmail();
			Users user = spaceEmail.getUsers();
			
			logger.info("=============== SPACE EMAIL DATA =====================");
			logger.info("Email: " + email);
			logger.info("Host: " + host);
			logger.info("Port: " + port);
			logger.info("Space name: " + spaceEmail.getSpaces().getName());
			logger.info("Assign to user: " + user.getName());
			logger.info("=============== END OF SPACE EMAIL DATA ==============");
			
			
			MailReader receiver = new MailReader();
			receiver.setSaveDirectory(helper.getConfigurationByParam("attachments").get(0).getValue());
			List<Spaces> spaces = new ArrayList<Spaces>();
			spaces = helper.getSpace(spaceEmail.getSpaces());
			
			if(!spaces.isEmpty() && spaces != null) {		
				System.out.println(spaces.get(0).getName());
				System.out.println(spaces.get(0).getId());
				System.out.println(spaces.get(0).getPrefixCode());
				System.out.println(user.getName());
				
				try {
					password = EncryptDecryptHelper.decrypt(password);
					
					receiver.processEmails(host, port, userName, password, spaces.get(0), user, email);
				} catch(Exception e) {
					logger.info(e.getMessage());
				}
				System.out.println("Done!!!");
			}
			
		}
		
		for(Handler h : logger.getHandlers()) {
			h.close();
		}
		
	}
	
}
