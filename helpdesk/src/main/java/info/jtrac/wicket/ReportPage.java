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
	private class ReportForm extends Form{
		
		private List<String> reportTypes = new ArrayList<String>();
		private RadioChoice group;
		//private HashMap<Integer, String> types = new HashMap<Integer, String>();
		private String selected;
		private Reports reports;
		private List<Reports> allReports;
		private int reportIndex = -1;
		private List<ServerSettings> serverSettings;
		private String serverURL = "";
		
		
		private DateTextField startDate;
		private DateTextField endDate;
		private RadioChoice reportFormats;
		
		private String dateFrom;
		private String dateTo;
		
		private List<String> formats = new ArrayList<String>();
		private Map<String, String> formatMap = new HashMap<String, String>();
		
		private String format;
		
		private String selectedSpace;
		
		private ListChoice spaces;
		
		public String getSelectedSpace() {
			return selectedSpace;
		}

		public void setSelectedSpace(String selectedSpace) {
			this.selectedSpace = selectedSpace;
		}

		public String getDateFrom() {
			return dateFrom;
		}

		public void setDateFrom(String dateFrom) {
			this.dateFrom = dateFrom;
		}

		public String getDateTo() {
			return dateTo;
		}

		public void setDateTo(String dateTo) {
			this.dateTo = dateTo;
		}

		public String getFormat() {
			return format;
		}

		public void setFormat(String format) {
			this.format = format;
		}
		
		public ReportForm(String id) {
			super(id);
			
			reports = new Reports();
			allReports = new ArrayList<Reports>();
			serverSettings = new ArrayList<ServerSettings>();
			
			allReports = getJtrac().getAllReports();
			
			/*types.put(1, "Issue Tracking");
			types.put(2, "Issue Tracking Per Client");
			types.put(3, "Project Tracking");*/
			
			for(Reports r : allReports) {
				reportTypes.add(r.getName());
			}
			

			
			//group = new RadioChoice("group", new PropertyModel(reports, "name"), reportTypes);
			
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
			
			
			// Start Date Param
			startDate = new DateTextField("startDate", new PropertyModel(this, "dateFrom"));
            DatePicker startDatePicker = new DatePicker();
            startDate.add(startDatePicker);
            startDate.setRequired(true);
            startDate.add(new ErrorHighlighter());
            add(startDate);
            
            
            //End Date Param
            endDate = new DateTextField("endDate", new PropertyModel(this, "dateTo"));
            DatePicker endDatePicker = new DatePicker();
            endDate.add(endDatePicker);
            endDate.setRequired(true);
            endDate.add(new ErrorHighlighter());
            add(endDate);
            
            
            formats.add("MS Excel");
            formats.add("PDF");
            formats.add("MS Word");
            formats.add("HTML");
            
            formatMap.put("MS Excel", "xls");
            formatMap.put("PDF", "pdf");
            formatMap.put("MS Word", "doc");
            formatMap.put("HTML", "html");
            
            reportFormats = new RadioChoice("reportFormats", new PropertyModel(this, "format"), formats);
            reportFormats.setRequired(true);
			add(reportFormats);
			
			List<Space> userSpaces = new ArrayList<Space>();
			User user = getPrincipal();
			
			userSpaces = user.isSuperUser() ? getJtrac().findAllSpaces() : getJtrac().findSpaces(user);
			
			spaces = new ListChoice("spaces", new Model(), userSpaces,
					new IChoiceRenderer() {
				
						public String getIdValue(Object object, int index) {
							return ((Space)object).getId() + "";
						}
						
						public Object getDisplayValue(Object object) {
							return ((Space)object).getName();
						}
					});
			spaces.setRequired(true);
			spaces.add(new ErrorHighlighter());
			add(spaces);
		}
		
		@Override
		protected void validate() {
			super.validate();
		}
		
		@Override
		protected void onSubmit() {
			
			SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
			SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
			
			try {
				setDateFrom(newFormat.format(format.parse(getDateFrom())));
				setDateTo(newFormat.format(format.parse(getDateTo())));
			} catch (ParseException e) {
				throw new RuntimeException();
			}
			
			String reportFormat = formatMap.get(getFormat());
			
			String reportName = group.getValue();
			
			Space space = getJtrac().loadSpace(Long.valueOf(spaces.getValue()));
			
			
			setResponsePage(new ReportRendererPage(getDateFrom(), getDateTo(), space, reportName, reportFormat));// new PageParameters("startDate=" + getDateFrom() + "&endDate=" + getDateTo() + "&client=" + space.getId()));
		}
		
	}
}
