package info.jtrac.mail.reader;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MailReaderModel implements Serializable {

	private String summary;
	private String details;
	private String originator;
	private String originatorContact;
	
	/*
	 * Gmail defalut fields
	 */
	private String subject;
	private String link;
	private String fromName;
	private String fromEmail;
	
	public MailReaderModel() {}
	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getFromEmail() {
		return fromEmail;
	}

	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}

	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	public String getOriginator() {
		return originator;
	}
	public void setOriginator(String originator) {
		this.originator = originator;
	}
	public String getOriginatorContact() {
		return originatorContact;
	}
	public void setOriginatorContact(String originatorContact) {
		this.originatorContact = originatorContact;
	}
	
}
