package info.jtrac.domain;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SpaceCategory implements Serializable{

	private long id;
	private Space space;
	private Category category;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Space getSpace() {
		return space;
	}

	public void setSpace(Space space) {
		this.space = space;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public SpaceCategory() {
		// TODO Auto-generated constructor stub
	}

}
