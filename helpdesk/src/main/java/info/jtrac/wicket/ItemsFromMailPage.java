package info.jtrac.wicket;

import info.jtrac.domain.Item;
import info.jtrac.domain.Severity;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.mail.reader.Constants;
import info.jtrac.mail.reader.MailReaderBot;
import info.jtrac.mail.reader.MailReaderModel;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;

public class ItemsFromMailPage extends BasePage {

	private List<MailReaderModel> mailReaderModels;
	private MailReaderBot mailReaderBot;
	private Label status;
	private String statusValue = "Item successfully created";
	
	public String getStatusValue() {
		return statusValue;
	}

	public void setStatusValue(String statusValue) {
		this.statusValue = statusValue;
	}

	@SuppressWarnings("serial")
	public ItemsFromMailPage() {

		mailReaderModels = new ArrayList<MailReaderModel>();
		mailReaderBot = new MailReaderBot(getJtrac().loadConfig("mail.server.username"), getJtrac().loadConfig("mail.server.password"));
		mailReaderBot.readMails();
		mailReaderModels = mailReaderBot.getMailsArrayList();
		
		WebMarkupContainer table = new WebMarkupContainer("table");

		add(table);
		table.add(new ListView("emails", mailReaderModels) {
			
			@Override
			protected void populateItem(ListItem listItem) {
				
				final MailReaderModel mailReaderModel = (MailReaderModel)listItem.getModelObject();
				
				listItem.add(new Label("name", new PropertyModel(mailReaderModel, "fromName")));
				listItem.add(new Label("from", new PropertyModel(mailReaderModel, "fromEmail")));
				listItem.add(new Label("summary", new PropertyModel(mailReaderModel, "summary")));
				listItem.add(new Label("title", new PropertyModel(mailReaderModel, "subject")));
				status = new Label("status", new PropertyModel(this, "statusValue"));
				listItem.add(status);
				status.setVisible(false);
				
				Link createItem = new Link("createItem") {
					public void onClick() {
						//TODO: Create Ticket
						
						Item item = new Item();
						Space space = new Space();
						String emialContent = mailReaderModel.getSummary();
						String itemSummary = mailReaderModel.getSubject();
						
						boolean priorityIsSet = emialContent.toLowerCase().contains(Constants.PRIORITY_IDENTIFIER);
					
						/*if(priorityIsSet) {
							List<Severity> severity = new ArrayList<Severity>();
							int priorityIndex = emialContent.indexOf(Constants.PRIORITY_IDENTIFIER);
							int whiteSpaceIndex = emialContent.substring(priorityIndex).indexOf(" ");
							
							int indexOfDelimiter = emialContent.indexOf(";");
							
							String priority = emialContent.substring(whiteSpaceIndex+1, indexOfDelimiter);
							priority = priority.replace(" ", "");
							priority = priority.replace(";", "");
							
							severity = getJtrac().findSeverityByName(priority);
							
							if(!severity.isEmpty()) {
								
							}
							
							item.setDetail(emialContent.substring(indexOfDelimiter+1));
							
						} else {
							item.setDetail(emialContent);
						}*/
						
						item.setDetail(emialContent);
						item.setSummary(itemSummary);
						List<Long> ids = new ArrayList<Long>();
						ids.add((long) 2);
						User user = getJtrac().findUsersWhereIdIn(ids).get(0);
						
						item.setLoggedBy(user);
						item.setAssignedTo(user);
						space = getJtrac().findDefaultSpace().get(0);
						item.setSpace(space);
						item.setStatus(1);
						
						getJtrac().storeItem(item, null);
						status.setVisible(true);
					}
				};
				
				listItem.add(createItem);
			}
		});
		
	}

}
