package info.jtrac.domain;

public class SubSpace {

	private long id;
	private Space space;
	private String prefix_code;
	private String name;
	private String description;
	
	public SubSpace() {
		this.space = new Space();
	}
	
	public long getId() {
		return id;
	}
	public String getPrefix_code() {
		return prefix_code;
	}

	public void setPrefix_code(String prefix_code) {
		this.prefix_code = prefix_code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
