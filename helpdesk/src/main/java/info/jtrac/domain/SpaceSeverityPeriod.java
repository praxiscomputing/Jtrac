package info.jtrac.domain;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SpaceSeverityPeriod implements Serializable {

	private long id;
	private Space space;
	private Severity severity;
	private int period;
	
	public SpaceSeverityPeriod(){
		
	}
	
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
	public Severity getSeverity() {
		return severity;
	}
	public void setSeverity(Severity severity) {
		this.severity = severity;
	}
	public int getPeriod() {
		return period;
	}
	public void setPeriod(int period) {
		this.period = period;
	}
}