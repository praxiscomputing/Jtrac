package za.co.praxis.helpdesk.escalation;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.hibernate.Hibernate;

import za.co.praxis.helpdesk.escalation.domain.Attachments;
import za.co.praxis.helpdesk.escalation.domain.Config;
import za.co.praxis.helpdesk.escalation.domain.History;
import za.co.praxis.helpdesk.escalation.domain.Items;
import za.co.praxis.helpdesk.escalation.domain.Spaces;
import za.co.praxis.helpdesk.escalation.domain.State;
import za.co.praxis.helpdesk.escalation.domain.Users;
import za.co.praxis.helpdesk.escalation.domain.ZEscalationLogs;
import za.co.praxis.helpdesk.escalation.domain.ZLevels;
import za.co.praxis.helpdesk.escalation.service.AutoEscalationService;

public class AutoEscalation {

	private static AutoEscalationService escalationService = null;
	private static String appUrl = "";
	private static String host = "";
	private static String email = "";
	private static String username = "";
	private static String password = "";
	private static String port = "";
	private static String tls = "";
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static Logger logger = Logger.getLogger(AutoEscalation.class.getName());
	
	public AutoEscalation() {
		
		
	}
	
	private static void sendEmailEscalation(List<Users> escalateTo, Users assignedTo, Items item) {
		
		System.out.println(escalateTo.get(0).getName());
		System.out.println(assignedTo.getName());
		String dir = System.getProperty("user.dir");
		FileHandler fh = null;
		String logFile = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
		String path = "\\escalation_log_" + logFile + ".txt";
		System.out.println(path);
		File file  = new File(path);
		
		
		if(!file.exists()) {
			try {
				file.createNewFile();
				try {
					fh = new FileHandler(path);
				} catch (SecurityException e1) {
					System.out.println(e1.getMessage());
				} catch (IOException e1) {
					System.out.println(e1.getMessage());
				}   
			} catch (IOException e2) {
				System.out.println(e2.getMessage());
			}
		}
		logger.addHandler(fh);
		
		List<Config> configs = new ArrayList<Config>();
		
		
		configs = escalationService.getJTracConfiguration();
		
		for(Config config : configs) {
			
			if(config.getParam().equals("jtrac.url.base"))
				appUrl = config.getValue();
			if(config.getParam().equals("mail.from"))
				email = config.getValue();
			if(config.getParam().equals("mail.server.host"))
				host = config.getValue();
			if(config.getParam().equals("mail.server.password"))
				password = config.getValue();
			if(config.getParam().equals("mail.server.username"))
				username = config.getValue();
			if(config.getParam().equals("mail.server.port"))
				port = config.getValue();
			if(config.getParam().equals("mail.server.starttls.enable"))
				tls = config.getValue();
			
		}
		
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
	              return new PasswordAuthentication(username, password);
	          }
	      });
	      session.setDebug(true);

	      try{
	         // Create a default MimeMessage object.
	         MimeMessage message = new MimeMessage(session);

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(email));

	         // Message To: User on level above current assigned user
	         for(Users user : escalateTo) 
	        	 message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
	         // Add current assigned user to recepient list
	         message.addRecipient(Message.RecipientType.CC, new InternetAddress(assignedTo.getEmail()));
	         message.addRecipient(Message.RecipientType.CC, new InternetAddress(email));

	         // Subject
	         message.setSubject(escalationService.EMAIL_SUBJECT);

	         StringBuffer sb = new StringBuffer();

	         String tdCommonStyle = "border: 1px solid black";
	         String tableStyle = " class='jtrac' style='border-collapse: collapse; font-family: Arial; font-size: 75%'";
	         String tdStyle = "";
	         String thStyle = "";
	         String altStyle = "";
	         String labelStyle = "";
	         tdStyle = " style='" + tdCommonStyle + "'";
	         thStyle = " style='" + tdCommonStyle + "; background: #CCCCCC'";//#8B0000; color: white;
	         altStyle = " style='background: #e1ecfe'";
	         labelStyle = " style='" + tdCommonStyle + "; background: #CCCCCC; font-weight: bold; text-align: right'";
	            
	         sb.append("<p>");
	         sb.append("<p style='font-family: Arial; font-size: 75%'>" + AutoEscalationService.EMAIL_BODY_DESC + "</p>");
	         sb.append("</p>");
	        
	         sb.append("<br/>");
	         
	         sb.append("<table width='100%'" + tableStyle + ">");
	         sb.append("<tr" + altStyle + ">");
	         sb.append("  <td" + labelStyle + ">" + "Ticket ID" + "</td>");
	         sb.append("  <td" + tdStyle + ">" + item.getSpaces().getPrefixCode() + "-" + item.getSequenceNum() + "</td>");
	         sb.append("</tr>");
	         
	         sb.append("<tr>");
	         sb.append("  <td width='15%'" + labelStyle + ">" + "Status" + "</td>");
	         
	         String status = "";
	         
	         switch(item.getStatus()) {
		         case State.NEW: 
		        	 status = "New";
		        	 break;
		         case State.OPEN:
		        	 status = "Open";
		        	 break;
		         case State.ON_HOLD:
		        	 status = "On Hold";
		        	 break;
		         case State.CLOSED:
		        	 status = "Closed";
		        	 break;
	        	 default:
	        		 status = "Open";
	        		 break;
	         }
	         
	         sb.append("  <td" + tdStyle + ">" + status + "</td>");
	         sb.append("  <td" + labelStyle + ">" + "Logged By" + "</td>");
	         sb.append("  <td" + tdStyle + ">" + item.getUsersByLoggedBy().getName() + "</td>");
	         sb.append("  <td" + labelStyle + ">" + "Assigned To" + "</td>");
	         sb.append("  <td width='15%'" + tdStyle + ">" + (item.getUsersByAssignedTo() == null ? "" : item.getUsersByAssignedTo().getName()) + "</td>");
	         sb.append("</tr>");
	         
	         sb.append("<tr" + altStyle + ">");
	         sb.append("  <td" + labelStyle + ">" + "Summary" + "</td>");
	         sb.append("  <td colspan='5'" + tdStyle + ">" + item.getSummary() + "</td>");
	         sb.append("</tr>");
	         
	         sb.append("<tr>");
	         sb.append("  <td" + labelStyle + ">" + "Category" + "</td>");
	         sb.append("  <td colspan='5'" + tdStyle + ">" + item.getZCategories().getDescription() + "</td>");
	         sb.append("</tr>");
	         
	         sb.append("<tr" + altStyle + ">");
	         sb.append("  <td" + labelStyle + ">" + "Date Logged" + "</td>");
	         sb.append("  <td colspan='5'" + tdStyle + ">" + new SimpleDateFormat("dd-MM-yyyy hh:mm:ss.z").format(item.getDateAdded()) + "</td>");
	         sb.append("</tr>");
	         
	         sb.append("<tr>");
	         sb.append("  <td" + labelStyle + ">" + "Due Date" + "</td>");
	         sb.append("  <td colspan='5'" + tdStyle + ">" + new SimpleDateFormat("dd-MM-yyyy hh:mm:ss.z").format(item.getDueDate()) + "</td>");
	         sb.append("</tr>");
	         
	         sb.append("<tr" + altStyle + ">");
	         sb.append("  <td" + labelStyle + ">" + "Originator" + "</td>");
	         sb.append("  <td colspan='5'" + tdStyle + ">" + item.getOriginator() + "</td>");
	         sb.append("</tr>");
	         
	         sb.append("<tr>");
	         sb.append("  <td" + labelStyle + ">" + "Originator Contact" + "</td>");
	         sb.append("  <td colspan='5'" + tdStyle + ">" + item.getOriginatorContact() + "</td>");
	         sb.append("</tr>");
	         
	         sb.append("<tr" + altStyle + ">");
	         sb.append("  <td" + labelStyle + ">" + "Priority" + "</td>");
	         sb.append("  <td colspan='5'" + tdStyle + ">" + item.getZSpaceSeverityPeriod().getZSeverities().getDescription() + "</td>");
	         sb.append("</tr>");
	         
	         sb.append("</table>");
	         
	         sb.append("<br/><br/>");
	         
	         sb.append("<b>History</b>");
	         sb.append("<br/>");
	         
	         if(item.getHistories() != null) {
		         sb.append("<table width='100%'" + tableStyle + ">");
		         sb.append("<tr>");
		         sb.append("  <th" + thStyle + ">" + "Logged By" + "</th><th" + thStyle + ">" + "Status" + "</th>"
		                 + "<th" + thStyle + ">" + "Assigned To" + "</th><th" + thStyle + ">" + "Reason Outstanding" + "</th><th" + thStyle + ">" + "Time Stamp" + "</th>");
		         sb.append("</tr>");
		         
		         int row = 1;
		         String historyStatus = "";
		         
		         for(History history : item.getHistories()) {
		        	 sb.append("<tr valign='top'" + (row % 2 == 0 ? altStyle : "") + ">");
		             sb.append("  <td" + tdStyle + ">" + history.getUsersByLoggedBy().getName() + "</td>");
		             System.out.println("#### " + history.getStatus());
		             
		             if(history.getStatus() != null) {
		             
			             switch(history.getStatus()) {
					         case State.NEW: 
					        	 historyStatus = "New";
					        	 break;
					         case State.OPEN:
					        	 historyStatus = "Open";
					        	 break;
					         case State.ON_HOLD:
					        	 historyStatus = "On Hold";
					        	 break;
					         case State.CLOSED:
					        	 historyStatus = "Closed";
					        	 break;
				        	 default:
				        		 historyStatus = "Open";
				        		 break;
			             }
		             } else {
		            	 historyStatus = "";
		             }
		             
		             sb.append("  <td" + tdStyle + ">" + historyStatus +"</td>");
		             sb.append("  <td" + tdStyle + ">" + (history.getUsersByAssignedTo() == null ? "" : history.getUsersByAssignedTo().getName()) + "</td>");

		             sb.append("  <td" + tdStyle + ">");
		             
		             if(history.getZReasonOutstanding() != null)
		               	sb.append(history.getZReasonOutstanding().getDescription());
		             else
		              	sb.append("");
		             
		             sb.append("  </td>");
		             sb.append("  <td" + tdStyle + ">" + new SimpleDateFormat("dd-MM-yyyy hh:mm:ss.z").format(history.getTimeStamp()) + "</td>");
		             sb.append("</tr>");
		             row++;
		         }
		         sb.append("</table>");
	         
		         sb.append("<br/><br/>");
	         }

	         String URL = appUrl +"/app/item/" + item.getSpaces().getPrefixCode() + "-" + item.getSequenceNum();
	         
	         sb.append("<p style='font-family: Arial; font-size: 75%'>Please go to <a href=" + URL +">" + URL + "</a>");
	         sb.append(" for more details.</p>");
	         
	         sb.append("</body>");
	         
	         message.setContent(addHeaderAndFooter(sb), "text/html");
	         
	         //=======================================================================================

	         // Send message
	         Transport.send(message);
	         System.out.println("Sent message successfully....");
	         for(Users user : escalateTo) {
	        	 
	        	 ZEscalationLogs escalationLog = new ZEscalationLogs();
		         escalationLog.setItems(item);
		         escalationLog.setUsersByAssignedTo(assignedTo);
		         escalationLog.setUsersByEscalatedTo(user);
		         escalationLog.setDueDate(item.getDueDate());
	
		         escalationLog.setTimeStamp(new Date());
		         escalationService.insertEscalationHistory(escalationLog);
	         }
			
	         System.out.println("Escalation logged successfully....");
	         
	      }catch (MessagingException mex) {
	         //throw new RuntimeException(mex);
	    	  System.out.println("Error: " + mex.getMessage());
	    	  logger.info(mex.getMessage());
	      }
	}
	
	public static String addHeaderAndFooter(StringBuffer html) {
		StringBuffer sb = new StringBuffer();
		// additional cosmetic tweaking of e-mail layout
		// style just after the body tag does not work for a minority of clients
		// like gmail, thunderbird etc.
		// We add the main inline CSS when generating the email content,
		// so we gracefully degrade
		sb.append("<html><body><style type='text/css'>table.jtrac th, table.jtrac td { padding-left: 0.2em; padding-right: 0.2em; }</style>");
		sb.append(html);
		sb.append("</html>");
		return sb.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		escalationService = new AutoEscalationService();
		List<Items> overDueItems = new ArrayList<Items>();
		List<Users> escalateToUser = new ArrayList<Users>();
		int count = 1;
		
		
		 
		
		overDueItems = escalationService.getAllOverDueItems();
		
		
		for(Items item : overDueItems) {
			Hibernate.initialize(item);
			Spaces space = new Spaces();
			Hibernate.initialize(space);
			space = item.getSpaces();
			System.out.println(space.getName());
			Users currentAssigned = new Users();
			Hibernate.initialize(currentAssigned);
			currentAssigned = item.getUsersByAssignedTo();
			System.out.println("Current assigned user: " + currentAssigned.getName());
			
			List<ZEscalationLogs> escalationLogs = new ArrayList<ZEscalationLogs>();
			escalationLogs = escalationService.getLastEscalationLogForItem(item);
			
			if(!escalationLogs.isEmpty() && escalationLogs != null && currentAssigned != null) {
				Date itemEscalationTimeStamp = escalationLogs.get(0).getTimeStamp();
				Date currentDate = new Date();
				
				int hoursSinceLastEscalation = escalationService.getHoursBetweenDates(currentDate, itemEscalationTimeStamp);
				int itemEscalationHours = item.getZSpaceSeverityPeriod().getPeriod();
				
				
				if(hoursSinceLastEscalation >= itemEscalationHours) {
				
					List<ZLevels> levels = new ArrayList<ZLevels>();
					Hibernate.initialize(levels);
					levels = escalationService.getLevelBySpaceAndUser(space, currentAssigned);
					
					
					if(!levels.isEmpty() && levels != null) {
						escalateToUser = escalationService.getUserToEscalateToBySpaceAndCurrentLevel(space, levels.get(0));
						
						System.out.println("Escalate to: " + escalateToUser.get(0).getName());
						System.out.println("Assigned to: " + currentAssigned.getName());
						System.out.println("Item ID: " + item.getSpaces().getPrefixCode()+"-"+item.getSequenceNum());
						
						if(!escalateToUser.isEmpty())
							sendEmailEscalation(escalateToUser, currentAssigned, item);
						
						try {
							Thread.sleep(1000L);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					System.out.println(count);
					count++;
				} else {
					
					System.out.println(item.getSpaces().getPrefixCode() + "-" + item.getSequenceNum() + " is not due for escalation.");
					
				}
			} else {
				
				List<ZLevels> levels = new ArrayList<ZLevels>();
				Hibernate.initialize(levels);
				levels = escalationService.getLevelBySpaceAndUser(space, currentAssigned);
				
				if(!levels.isEmpty() && levels != null) {
					escalateToUser = escalationService.getUserToEscalateToBySpaceAndCurrentLevel(space, levels.get(0));
					
					if(!escalateToUser.isEmpty())
						sendEmailEscalation(escalateToUser, currentAssigned, item);
					else
						System.out.println("No user to escalate to.");
					
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						logger.info(e.getMessage());
					}
				}
				
				System.out.println(count);
				count++;
			}
			
		}
		AutoEscalationService.getCurrentSession().getTransaction().commit();
		AutoEscalationService.getCurrentSession().close();
		
	}

}
