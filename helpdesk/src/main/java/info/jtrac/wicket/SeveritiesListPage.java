package info.jtrac.wicket;

import java.util.ArrayList;
import java.util.List;

import info.jtrac.domain.Severity;
import info.jtrac.domain.User;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

public class SeveritiesListPage extends BasePage {

	public SeveritiesListPage() {
		add(new SeveritiesForm("form"));		
	}

	@SuppressWarnings("serial")
	private class SeveritiesForm extends Form {

		private TextField severityName;
		List<Severity> severities = new ArrayList<Severity>();
		List<Link> deleteLinks =  new ArrayList<Link>();
		
		public SeveritiesForm(String id) {
			
			super(id);
			
			final User principal = getPrincipal();

			// since this admin screen can be seen by space-admins,
			// only allow super users to create new space
			add(new Link("create") {
				public void onClick() {
					SpaceFormPage page = new SpaceFormPage();
					page.setPrevious(SeveritiesListPage.this);
					setResponsePage(page);
				}
			}.setVisible(principal.isSuperUser()));

			severities = getJtrac().findAllSeverities();
			LoadableDetachableModel severitiesListModel = new LoadableDetachableModel() {
				protected Object load() {
					if (principal.isSuperUser()) {
						return severities;
					} else {
						return principal.getSpacesWhereRoleIsAdmin();
					}
				}
			};

			final SimpleAttributeModifier sam = new SimpleAttributeModifier(
					"class", "alt");

			ListView listView = new ListView("severities", severitiesListModel) {
				protected void populateItem(ListItem listItem) {
					final Severity severity = (Severity) listItem.getModelObject();
					
					Link edit = new Link("edit") {
						public void onClick() {

							//TODO:
						
						}
					};
					listItem.add(edit);
					
					Link delete = new Link("delete") {
						public void onClick() {
							getJtrac().removeSeverity(severity);
							setResponsePage(SeveritiesListPage.class);
						}
					};
					deleteLinks.add(delete);
					listItem.add(delete);
					
					listItem.add(new Label("description", new PropertyModel(severity, "description")));
				}
			};

			add(listView);
			
			severityName = new TextField("newSeverity", new Model(""));
			severityName.setRequired(true);
			severityName.add(new ErrorHighlighter());
			severityName.setOutputMarkupId(true);
            add(severityName);
			
		}
		
		@Override
        protected void onSubmit() {
			
			validate();
			
			String sevName = severityName.getModelObjectAsString();
					
			if(sevName.length() > 0) {
				Severity severity = new Severity();
				severity.setDescription(sevName.toUpperCase());
				getJtrac().storeSeverity(severity);
				
				setResponsePage(SeveritiesListPage.class);
			}
		}
	}
}
