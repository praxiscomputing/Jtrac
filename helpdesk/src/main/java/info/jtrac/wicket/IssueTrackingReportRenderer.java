package info.jtrac.wicket;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;

public class IssueTrackingReportRenderer extends BasePage {

	private WebMarkupContainer reportViewer;
	private ExternalLink reportLink;
	
	public IssueTrackingReportRenderer(PageParameters params) {
		addComponents();
		
		
	}
	
	private void addComponents() {
		
		reportViewer = new WebMarkupContainer("reportViewer");
		reportLink = new ExternalLink("issueTracking", "http://localhost:8081/birt/frameset?__report=report\\IssueTracking.rptdesign");
		
		add(reportViewer);
		add(new Link("back") {
            public void onClick() {
            	setResponsePage(new DashboardPage());
            }
        });
		add(reportLink);
		
	}
}
