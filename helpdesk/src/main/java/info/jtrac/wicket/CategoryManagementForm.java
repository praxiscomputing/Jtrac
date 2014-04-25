package info.jtrac.wicket;

import info.jtrac.domain.Category;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;

public class CategoryManagementForm extends BasePage {

	public CategoryManagementForm() {

		List<Category> categories = new ArrayList<Category>();
		categories = getJtrac().findAllCategories();

		add(new CategoryForm("form", categories));
	}

	@SuppressWarnings("serial")
	private class CategoryForm extends Form {

		private TextField newCategory;
		
		private CheckBox alert_escalate_yn;
		
		private String newInputCategory;
		
		private CheckBox new_alert_escalate_yn;
		
		private boolean newAlertEscalateYn;
		
		private String editCategoryText;
		
		private String editAlertEscalate;
		
		public String getEditAlertEscalate() {
			return editAlertEscalate;
		}

		public void setEditAlertEscalate(String editAlertEscalate) {
			this.editAlertEscalate = editAlertEscalate;
		}

		public String getEditCategoryText() {
			return editCategoryText;
		}

		public void setEditCategoryText(String editCategoryText) {
			this.editCategoryText = editCategoryText;
		}

		public boolean isNewAlertEscalateYn() {
			return newAlertEscalateYn;
		}

		public void setNewAlertEscalateYn(boolean newAlertEscalateYn) {
			this.newAlertEscalateYn = newAlertEscalateYn;
		}

		public String getNewInputCategory() {
			return newInputCategory;
		}

		public void setNewInputCategory(String newInputCategory) {
			this.newInputCategory = newInputCategory;
		}

		@SuppressWarnings("serial")
		public CategoryForm(String id, final List<Category> categories) {
			super(id);

			List<String> columnHeadings = new ArrayList<String>();
			columnHeadings.add("Category");
			columnHeadings.add("Alert and Escalate?");
			columnHeadings.add("Edit");
			columnHeadings.add("Delete");

			ListView headings = new ListView("headings", columnHeadings) {
	            protected void populateItem(ListItem listItem) {
	                listItem.add(new Label("heading", listItem.getModelObjectAsString()));
	            }
	        };
	        
	        add(headings);
	        
			add(new ListView("listview", categories) {
			    protected void populateItem(ListItem listItem) {
			        final Category category = (Category) listItem.getModelObject();
			        
			        final TextField txtCategory = new TextField("category", new PropertyModel(category, "description"));
			        
			        txtCategory.add(new AjaxFormComponentUpdatingBehavior("onchange") {
						
						@Override
						protected void onUpdate(AjaxRequestTarget target) {
							
							//category.setDescription(txtCategory.getValue());
							setEditCategoryText(txtCategory.getValue());
						}
					});
			        
			        listItem.add(txtCategory);
			        
			        alert_escalate_yn = new CheckBox("alert_escalate_yn", new PropertyModel(category, "alert_escalate_yn"));
			        
			        alert_escalate_yn.add(new AjaxFormComponentUpdatingBehavior("onchange") {
						
						@Override
						protected void onUpdate(AjaxRequestTarget target) {

							//category.setAlert_escalate_yn(Boolean.valueOf(alert_escalate_yn.getValue()));
							setEditAlertEscalate(alert_escalate_yn.getValue());
						}
					});
			        
			        
			        listItem.add(alert_escalate_yn);
			        
			        listItem.add(new Link("delete") {
						
						@Override
						public void onClick() {
							
							String heading = localize("category_delete.confirm");
		                    String warning = localize("category_delete.line3");
		                    String line1 = localize("category_delete.line1");
		                    String line2 = localize("category_delete.line2");
		                    ConfirmPage confirm = new ConfirmPage(CategoryManagementForm.this, heading, warning, new String[] {line1, line2}) {
		                        public void onConfirm() {
		                        	getJtrac().deleteCategory(category);
		                            setResponsePage(new CategoryManagementForm());
		                        }                        
		                    };
		                    setResponsePage(confirm);
						}
					});
			        
			        listItem.add(new Link("edit") {

						@Override
						protected String getOnClickScript(CharSequence url) {
							return "alert('Category Updated')";
						}

						@Override
						public void onClick() {
							
							if(getEditCategoryText() != null) {
								category.setDescription(getEditCategoryText());
							}
							/*if(getEditAlertEscalate() != null) {
								category.setAlert_escalate_yn(Boolean.valueOf(getEditAlertEscalate()));
							}*/
							getJtrac().saveCategory(category);
						}
			        	
			        });
			        
			       
			        
			    }
			}.setReuseItems(true));
			
			newCategory = new TextField("newCategory", new PropertyModel(this, "newInputCategory"));
			newCategory.setRequired(true);
			add(newCategory);
			
			new_alert_escalate_yn = new CheckBox("new_alert_escalate_yn", new PropertyModel(this, "newAlertEscalateYn"));
			add(new_alert_escalate_yn);
		}
		
		@Override
		protected void validate() {
			super.validate();
		}

		@Override
		protected void onSubmit() {
			Category category = new Category();
			category.setDescription(newInputCategory);
			category.setAlert_escalate_yn(newAlertEscalateYn);
			getJtrac().saveCategory(category);
			setResponsePage(new CategoryManagementForm());
		}
		
	}

}
