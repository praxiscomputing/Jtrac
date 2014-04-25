package info.jtrac.wicket;

import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.domain.ZSpaceEmail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

public class SpaceEmailPage extends BasePage {

	private TextField fullEmail;
	private TextField username;
	private PasswordTextField password;
	private TextField domain;
	private TextField port;
	private RadioChoice spaceUsers;
	
	private List<ZSpaceEmail> spaceEmails;
	private ZSpaceEmail spaceEmail;
	
	private List<User> users = new  ArrayList<User>();
	private Map<Long, String> usersMap = new HashMap<Long, String>();
	private List<String> userNames = new ArrayList<String>();
	
	
	public SpaceEmailPage(Space space) {
		add(new Label("label", space.getName() + " (" + space.getPrefixCode() + ")"));
		add(new SpaceEmailForm("form", space));
	}
	
	@SuppressWarnings("serial")
	private class SpaceEmailForm extends Form {

		private String selectedUser;
		
		public void setSelectedUser(String selectedUser) {
			this.selectedUser = selectedUser;
		}
		
		public String getSelectedUser() {
			return selectedUser;
		}
		
		public SpaceEmailForm(String id, Space space) {
			super(id);
			
			spaceEmails = getJtrac().findSpaceEmail(space);
			
			if(!spaceEmails.isEmpty() && spaceEmails != null) 
				spaceEmail = spaceEmails.get(0);
			else
				spaceEmail = new ZSpaceEmail();
			
			spaceEmail.setSpace(space);
			
			users = getJtrac().findUsersForSpace(space.getId());
			
			for(User user : users) {
				usersMap.put(user.getId(), user.getName());
			}
			
			userNames.addAll(usersMap.values());
			
			
			fullEmail = new  TextField("fullEmail", new PropertyModel(spaceEmail, "email"));
			fullEmail.setRequired(true);
			add(fullEmail);
			
			username = new  TextField("username", new PropertyModel(spaceEmail, "username"));
			username.setRequired(true);
			add(username);
			
			password = new  PasswordTextField("password", new PropertyModel(spaceEmail, "password"));
			password.setRequired(true);
			add(password);
			
			domain = new  TextField("domain", new PropertyModel(spaceEmail, "host"));
			domain.setRequired(true);
			add(domain);
			
			port = new  TextField("port", new PropertyModel(spaceEmail, "port"));
			port.setRequired(true);
			add(port);
			
			spaceUsers = new RadioChoice("users", new PropertyModel(spaceEmail, "user"), users, new IChoiceRenderer() {
				
				public String getIdValue(Object object, int index) {
					return ((User)object).getId() +  "";
				}
				
				public Object getDisplayValue(Object object) {
					return ((User)object).getName();
				}
			});
			spaceUsers.setRequired(true);
			add(spaceUsers);
		}

		@Override
		protected void onSubmit() {
			
			List<User> user = getJtrac().findUsers(Long.valueOf(spaceUsers.getModelValue()));
			if(!user.isEmpty() && user != null)
				spaceEmail.setUser(user.get(0));
				
			getJtrac().storeSpaceEmail(spaceEmail);
		}

		@Override
		protected void validate() {
			
			super.validate();
		}
		
	}
}
