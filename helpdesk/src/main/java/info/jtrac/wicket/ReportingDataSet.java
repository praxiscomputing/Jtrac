package info.jtrac.wicket;

import info.jtrac.domain.Item;

import java.util.List;

public class ReportingDataSet extends BasePage {

	public ReportingDataSet() {
		//super();
	}
	
	public List<Item> getItems() {
		return getJtrac().findAllItems();
	}

}
