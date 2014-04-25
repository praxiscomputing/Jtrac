package info.jtrac.domain;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Reports implements Serializable {

	private long id;
	private String description;
	private String name;
	
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
