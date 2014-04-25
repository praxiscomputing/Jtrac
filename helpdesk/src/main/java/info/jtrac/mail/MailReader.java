package info.jtrac.mail;

import info.jtrac.Jtrac;
import info.jtrac.domain.Item;
import info.jtrac.domain.Space;
import info.jtrac.domain.SpaceSeverityPeriod;
import info.jtrac.domain.State;
import info.jtrac.domain.User;
import info.jtrac.wicket.BasePage;
import info.jtrac.wicket.JtracApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.servlet.ServletContext;

import org.apache.wicket.protocol.http.WebApplication;
import org.jsoup.Jsoup;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class MailReader {

public MailReader() { }
	
	private String saveDirectory;
	private Jtrac jtrac;
	private ApplicationContext applicationContext; 
	
    /**
     * Sets the directory where attached files will be stored.
     * @param dir absolute path of the directory
     */
    public void setSaveDirectory(String dir) {
        this.saveDirectory = dir;
    }
 
    /**
     * Downloads new messages and saves attachments to disk if any.
     * @param host
     * @param port
     * @param userName
     * @param password
     */
    @SuppressWarnings("unchecked")
	public void downloadEmailAttachments(String host, String port,
            String userName, String password) {
    	
    	JtracApplication application = new JtracApplication();
    	
        Properties properties = new Properties();
 
        // server setting
        properties.put("mail.pop3.host", host);
        properties.put("mail.pop3.port", port);
 
        // SSL setting
        properties.setProperty("mail.pop3.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.pop3.socketFactory.fallback", "false");
        properties.setProperty("mail.pop3.socketFactory.port",
                String.valueOf(port));
 
        Session session = Session.getDefaultInstance(properties);
 
        try {
            // connects to the message store
            Store store = session.getStore("pop3");
            store.connect(userName, password);
 
            // opens the inbox folder
            Folder folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_ONLY);
 
            // fetches new messages from server
            Message[] arrayMessages = folderInbox.getMessages();
 
            for (int i = 0; i < arrayMessages.length; i++) {
            	
            	 UUID uuid = UUID.randomUUID();
                 String fileName = uuid.toString();
            	
            	
                Message message = arrayMessages[i];
                
                Address[] fromAddress = message.getFrom();
                String from = fromAddress[0].toString();
                String subject = message.getSubject();
                String sentDate = message.getSentDate().toString();
 
                String contentType = message.getContentType();
                Object messageContent = null;
 
                // store attachment file name, separated by comma
                String attachFiles = "";
 
                if (contentType.contains("multipart")) {
                    // content may contain attachments
                    Multipart multiPart = (Multipart) message.getContent();
                    int numberOfParts = multiPart.getCount();
                    for (int partCount = 0; partCount < numberOfParts; partCount++) {
                        MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                            // this part is attachment
                            String attachmentFileName = fileName + "#" + part.getFileName();
                            attachFiles += attachmentFileName + ", ";
                            part.saveFile(saveDirectory + File.separator + attachmentFileName);
                        } else {
                            // this part may be the message content
                            messageContent = part.getContent();
                        }
                    }
 
                    if (attachFiles.length() > 1) {
                        attachFiles = attachFiles.substring(0, attachFiles.length() - 2);
                    }
                } else if (contentType.contains("text/plain")
                        || contentType.contains("text/html")) {
                    Object content = message.getContent();
                    
                    if (content != null) {
                        messageContent = content;
                    }
                }
 
                String emailContent = getBody(messageContent);
                
                Item item = new Item();
                
                List<User> users = new ArrayList<User>();
                List<Space> spaces = new ArrayList<Space>();
                List<SpaceSeverityPeriod> spaceSeverityPeriods = new ArrayList<SpaceSeverityPeriod>();

                
                
                spaces = application.getJtrac().findNISSpace();
                
                
                if(!spaces.isEmpty()) {
                	item.setSpace(spaces.get(0));
                	spaceSeverityPeriods = application.getJtrac().findSpaceSeverityPeriodBySpaceId(spaces.get(0).getId());
                	item.setSpaceSeverityPeriod(spaceSeverityPeriods.get(0));
                }
                
                
                //item.setSequenceNum(sequenceNum);
                item.setDueDate(new Date());
                item.setDateAdded(message.getSentDate());
                
                users = application.getJtrac().findUserByEmailAddress(from);
                if(!users.isEmpty()) item.setLoggedBy(users.get(0)); 
                
                users = application.getJtrac().findUserByEmailAddress("samuelo@praxis.co.za");
                
                if(!users.isEmpty()) item.setAssignedTo(users.get(0));
                
                item.setSummary(subject);
                item.setDetail(Jsoup.parse(emailContent).toString());
                item.setOriginator(from);
                item.setOriginator_contact(from);
                item.setSpaceSeverityPeriod(null);
                item.setStatus(State.NEW);
                
                //FileUpload fileUpload = new FileUpload("");
                
                // print out details of each message
                System.out.println("Message #" + (i + 1) + ":");
                System.out.println("\t From: " + from);
                System.out.println("\t Subject: " + subject);
                System.out.println("\t Sent Date: " + sentDate);
                System.out.println("\t Message: " + emailContent);
                System.out.println("\t Attachments: " + attachFiles);
                
                
                try {
                	 
        			File file = new File("C:/Users/SamuelO/Desktop/Emials/" + fileName + ".txt");
         
        			// if file doesnt exists, then create it
        			if (!file.exists()) {
        				file.createNewFile();
        			}
         
        			FileWriter fw = new FileWriter(file.getAbsoluteFile());
        			BufferedWriter bw = new BufferedWriter(fw);
        			bw.write(emailContent);
        			bw.close();
         
        			System.out.println("Done");
         
        			application.getJtrac().storeItem(item, null);
        			
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
                
                
                
            }
 
            // disconnect
            folderInbox.close(false);
            store.close();
        } catch (NoSuchProviderException ex) {
            System.out.println("No provider for pop3.");
            ex.printStackTrace();
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
 
    private String getBody(Object msgContent) throws MessagingException, IOException {
    	
    	String content = "";  
    	
    	/* Check if content is pure text/html or in parts */                     
        if (msgContent instanceof Multipart) {

            Multipart multipart = (Multipart) msgContent;

           System.out.println("BodyPart " + "MultiPartCount: " + multipart.getCount());

            for (int j = 0; j < multipart.getCount(); j++) {

             BodyPart bodyPart = multipart.getBodyPart(j);

             String disposition = bodyPart.getDisposition();

             if (disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) { 
                 System.out.println("Mail have some attachment");

                 DataHandler handler = bodyPart.getDataHandler();
                 System.out.println("file name : " + handler.getName());                                 
               }
             else { 
                     content = getText(bodyPart);  // the changed code         
               }
           }
        }
        else                
            content= msgContent.toString();
        
        
        return content;
    }
    
    
    /**
     * Return the primary text content of the message.
     */
    private String getText(Part p) throws
                MessagingException, IOException {
    	
    	boolean textIsHtml = false;
    	
    	
        if (p.isMimeType("text/*")) {
            String s = (String)p.getContent();
            textIsHtml = p.isMimeType("text/html");
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart)p.getContent();
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
            Multipart mp = (Multipart)p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }
    
    /**
     * Runs this program with Gmail POP3 server
     */
    public static void main(String[] args) {
        String host = "pop.gmail.com";
        String port = "995";
        String userName = "praxisjtrac@gmail.com";
        String password = "praxis61JTRAC";
 
        String saveDirectory = "C:/Users/SamuelO/Desktop/Emials";
 
        MailReader receiver = new MailReader();
        receiver.setSaveDirectory(saveDirectory);
        receiver.downloadEmailAttachments(host, port, userName, password);
 
    }

}
