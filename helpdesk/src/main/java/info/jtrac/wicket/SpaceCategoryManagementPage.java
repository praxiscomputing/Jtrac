package info.jtrac.wicket;

import java.util.ArrayList;
import java.util.List;

import info.jtrac.domain.Category;
import info.jtrac.domain.Space;
import info.jtrac.domain.SpaceCategory;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

public class SpaceCategoryManagementPage extends BasePage {

	public SpaceCategoryManagementPage(Space space) {
		
		new SpaceCategoryManagementForm("form", space);
	}

	private class SpaceCategoryManagementForm extends Form{

		private CheckBox assign_category_yn;
		private boolean category_is_assigned;
		private List<Category> categories = new ArrayList<Category>();
		
		public boolean isCategory_is_assigned() {
			return category_is_assigned;
		}

		public void setCategory_is_assigned(boolean category_is_assigned) {
			this.category_is_assigned = category_is_assigned;
		}

		public SpaceCategoryManagementForm(String id, Space space) {
			super(id);
			
			List<SpaceCategory> spaceCategories = new ArrayList<SpaceCategory>();
			spaceCategories = getJtrac().findSpaceCategoriesBySpace(space);
			
			categories = getJtrac().findAllCategories();
			
			List<String> columnHeadings = new ArrayList<String>();
			columnHeadings.add("Category");
			columnHeadings.add("Category Applicable to Space?");
			columnHeadings.add("Edit");
			columnHeadings.add("Delete");

			ListView headings = new ListView("headings", columnHeadings) {
	            protected void populateItem(ListItem listItem) {
	                listItem.add(new Label("heading", listItem.getModelObjectAsString()));
	            }
	        };
	        
	        add(headings);
	        
	        add(new ListView("listview", categories) {
			    protected void populateItem(ListItem item) {
			        final Category category = (Category) item.getModelObject();
			        
			        item.add(new Label("category", new PropertyModel(category, "description")));
			        
			        assign_category_yn = new CheckBox("assign_category_yn", new PropertyModel(this, "category_is_assigned"));
			        item.add(assign_category_yn);
			    }
			});
	        
	        
	        
		}

		@Override
		protected void onSubmit() {

			
		}
		
	}
}
