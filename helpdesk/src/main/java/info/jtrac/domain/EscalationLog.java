package info.jtrac.domain;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class EscalationLog implements Serializable {

	private long id;
	private Item item;
	private User assigned_user;
	private User escalated_to;
	private Date due_date;
	private Date time_stamp;
	
	public EscalationLog() { }

	public EscalationLog(long id, Item item, User assigned_user,
			User escalated_to, Date due_date, Date time_stamp) {
		super();
		this.id = id;
		this.item = item;
		this.assigned_user = assigned_user;
		this.escalated_to = escalated_to;
		this.due_date = due_date;
		this.time_stamp = time_stamp;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public User getAssigned_user() {
		return assigned_user;
	}

	public void setAssigned_user(User assigned_user) {
		this.assigned_user = assigned_user;
	}

	public User getEscalated_to() {
		return escalated_to;
	}

	public void setEscalated_to(User escalated_to) {
		this.escalated_to = escalated_to;
	}

	public Date getDue_date() {
		return due_date;
	}

	public void setDue_date(Date due_date) {
		this.due_date = due_date;
	}

	public Date getTime_stamp() {
		return time_stamp;
	}

	public void setTime_stamp(Date time_stamp) {
		this.time_stamp = time_stamp;
	}
	
}
