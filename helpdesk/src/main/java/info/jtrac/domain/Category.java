package info.jtrac.domain;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Category implements Serializable{
	private long id;
	private String description;
	private boolean alert_escalate_yn;
	
	public long getId() {
		return id;
	}
	public boolean isAlert_escalate_yn() {
		return alert_escalate_yn;
	}
	public void setAlert_escalate_yn(boolean alert_escalate_yn) {
		this.alert_escalate_yn = alert_escalate_yn;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

}
