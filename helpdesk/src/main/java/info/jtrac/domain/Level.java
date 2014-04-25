package info.jtrac.domain;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Level implements Serializable {

	private long id;
	private int level;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
}
