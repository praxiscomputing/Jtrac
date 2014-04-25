package info.jtrac.wicket;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;

public class Reporting extends BasePage {

	public Reporting() {

		add(new ReportingForm("form"));

		/*
		 * add(new Link("edit") {
		 * 
		 * @Override public void onClick() {
		 * 
		 * } });
		 */
	}

	private class ReportingForm extends Form {

		public ReportingForm(String id) {
			super(id);

			
			WebMarkupContainer reportData = new WebMarkupContainer("reportData");
			add(reportData);
			
			add(new Link("pdf") {

				@Override
				public void onClick() {

				}
			});

			add(new Link("xls") {

				@Override
				public void onClick() {

				}
			});

			add(new Link("doc") {

				@Override
				public void onClick() {

				}
			});

		}

	}

}
