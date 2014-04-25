package info.jtrac.domain;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class AlertLog implements Serializable {

	private long id;
	private User user;
	private Item item;
	private Date due_date;
	private Date time_stamp;
	
	public AlertLog() {	}
	
	public AlertLog(long id, User user, Item item, Date due_date,
			Date time_stamp) {
		super();
		this.id = id;
		this.user = user;
		this.item = item;
		this.due_date = due_date;
		this.time_stamp = time_stamp;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the item
	 */
	public Item getItem() {
		return item;
	}

	/**
	 * @param item the item to set
	 */
	public void setItem(Item item) {
		this.item = item;
	}

	/**
	 * @return the due_date
	 */
	public Date getDue_date() {
		return due_date;
	}

	/**
	 * @param due_date the due_date to set
	 */
	public void setDue_date(Date due_date) {
		this.due_date = due_date;
	}

	/**
	 * @return the time_stamp
	 */
	public Date getTime_stamp() {
		return time_stamp;
	}

	/**
	 * @param time_stamp the time_stamp to set
	 */
	public void setTime_stamp(Date time_stamp) {
		this.time_stamp = time_stamp;
	}
	
}
