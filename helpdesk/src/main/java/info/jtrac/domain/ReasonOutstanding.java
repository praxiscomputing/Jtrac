package info.jtrac.domain;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ReasonOutstanding implements Serializable{
	
	private long id;
	private String description;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getdescription() {
		return description;
	}
	public void setdescription(String description) {
		this.description = description;
	}

}
