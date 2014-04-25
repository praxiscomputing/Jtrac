package info.jtrac.wicket;
import info.jtrac.domain.Level;
import info.jtrac.domain.User;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

public class LevelPage extends BasePage {

	public LevelPage() {
		add(new LevelsForm("form"));		
	}

	@SuppressWarnings("serial")
	private class LevelsForm extends Form {

		private TextField level_number;
		
		
		public LevelsForm(String id) {
			
			super(id);
			
			final User principal = getPrincipal();

			// since this admin screen can be seen by space-admins,
			// only allow super users to create new space
			add(new Link("create") {
				public void onClick() {
					SpaceFormPage page = new SpaceFormPage();
					page.setPrevious(LevelPage.this);
					setResponsePage(page);
				}
			}.setVisible(principal.isSuperUser()));
			List<Level> levels = new ArrayList<Level>();
			levels = getJtrac().findAllLevels();
			Level level = new Level();
			
			if(!levels.isEmpty())
				level = levels.get(0);
			
			level_number = new TextField("level", new Model(levels.size()));
			level_number.setRequired(true);
			level_number.setOutputMarkupId(true);
			add(level_number);
		}
		
		@Override
        protected void onSubmit() {
			
			validate();
			
			List<Level> levels = new ArrayList<Level>();
			levels = getJtrac().findAllLevels();
			
			for(Level l : levels) {
				getJtrac().removeLevel(l);
			}
			
			List<Integer> levelNums = generateLevels(Integer.parseInt(level_number.getModelObjectAsString()));
			
			for(int levelNum : levelNums) {
				Level l = new Level();
				l.setLevel(levelNum);
				getJtrac().storeLevel(l);
			}
			
			
				setResponsePage(DashboardPage.class);
			
		}
		
		private List<Integer> generateLevels(int levelCount) {
			
			List<Integer> levels = new ArrayList<Integer>();
			
			for(int i=0; i<levelCount; i++) {
				levels.add(i+1);
			}
			
			return levels;
		}
		
	}
	
	
		
	
}