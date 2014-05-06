package info.jtrac.wicket;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.jtrac.domain.Reports;
import info.jtrac.domain.ServerSettings;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.wicket.yui.YuiCalendar;

import org.apache.axis.wsdl.symbolTable.Parameters;
import org.apache.tools.ant.taskdefs.Input;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.jfree.layout.RadialLayout;

import com.ibm.icu.text.SimpleDateFormat;

@SuppressWarnings("unused")
public class ReportPage extends BasePage {
  
	public ReportPage() { 
		add(new ReportForm("form"));
	}
	
	
	@SuppressWarnings("serial")
	private class ReportForm extends Form {
		
		private RadioChoice group;

		private List<Reports> allReports;
		
		public ReportForm(String id) {
			super(id);
			
			allReports = new ArrayList<Reports>();
			
			allReports = getJtrac().getAllReports();
					
			group = new RadioChoice("group", new Model(), allReports, new IChoiceRenderer() {
				
				public String getIdValue(Object object, int index) {
					return ((Reports)object).getDescription() + "";
				}
				
				public Object getDisplayValue(Object object) {
					return ((Reports)object).getName();
				}
			});
			group.setRequired(true);
			group.add(new ErrorHighlighter());
			add(group);
			
		}
		
		@Override
		protected void validate() {
			super.validate();
		}
		
		@Override
		protected void onSubmit() {
						
			String reportName = group.getValue();
			
			setResponsePage(new ReportRendererPage(reportName));
		}
		
	}
}
