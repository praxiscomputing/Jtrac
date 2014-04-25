package info.jtrac.domain;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Status implements Serializable {
	private long id;
	private String description;
	public long getId() {
		return id;
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
