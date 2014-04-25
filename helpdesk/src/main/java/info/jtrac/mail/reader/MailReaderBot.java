package info.jtrac.mail.reader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

@SuppressWarnings("serial")
public class MailReaderBot implements Serializable {

	// URL pointing to the feed
	private final String CONNECTION_URL = "https://mail.google.com/mail/feed/atom/";
	// Username and password Variables
	private String userName;
	private String password;
	private List<MailReaderModel> mailsArrayList;
	private transient HttpURLConnection connection;

	public MailReaderBot(String username, String password) {
		this.userName = username;
		this.password = password;
		mailsArrayList = new ArrayList<MailReaderModel>();
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public List<MailReaderModel> getMailsArrayList() {
        return mailsArrayList;
    }

	private void connect() throws Exception {

		URL url = new URL(CONNECTION_URL);

		connection = (HttpURLConnection) url.openConnection();
		connection
				.setRequestProperty(
						"Authorization",
						"Basic"
								+ Base64.encode((userName + ":" + password)
										.getBytes()));

		connection.connect();
	}

	public void readMails() {

		try {
			connect();
		

			// Buffered Reader from input stream
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
	
			// temporary variable
			String line;
	
			// Contents of XML file
			String contents = "";
	
			// Loop to read the XML into contents string
			while ((line = reader.readLine()) != null) {
				contents += line;
			}
	
			// Define entry item pattern
			Pattern entryPat = Pattern.compile("<entry>(.*?)</entry>");
			Matcher entryMatcher = entryPat.matcher(contents);
	
			// Current mail Entry (temp variable)
			MailReaderModel currentMailEntry;
	
			while (entryMatcher.find()) {
	
				// Create new temporary object
				currentMailEntry = new MailReaderModel();
	
				// Extract the entry
				String matchedEntry = entryMatcher.group(1);
	
				// Extract title from entry
				Pattern titlePattern = Pattern.compile("<title>(.*)</title>");
				Matcher titleMatcher = titlePattern.matcher(matchedEntry);
	
				// Put it into temporary object
				while (titleMatcher.find()) {
					currentMailEntry.setSubject(titleMatcher.group(1));
				}
	
				// Extract summary from entry
				Pattern summaryPattern = Pattern.compile("<summary>(.*)</summary>");
				Matcher summaryMatcher = summaryPattern.matcher(matchedEntry);
	
				// Put it into temporary object
				while (summaryMatcher.find()) {
					currentMailEntry.setSummary(summaryMatcher.group(1));
				}
	
				// Extract link from entry
				Pattern linkPattern = Pattern.compile("<link.*href=(.*).*/>");
				Matcher linkMatcher = linkPattern.matcher(matchedEntry);
	
				// Put it into temporary object
				while (linkMatcher.find()) {
					currentMailEntry.setLink(linkMatcher.group(1));
				}
	
				// Extract name from entry
				Pattern fromNamePattern = Pattern.compile("<name>(.*)</name>");
				Matcher fromNameMatcher = fromNamePattern.matcher(matchedEntry);
	
				// Put it into temporary object
				while (fromNameMatcher.find()) {
					currentMailEntry.setFromName(fromNameMatcher.group(1));
				}
	
				// Extract email from entry
				Pattern fromEmailPattern = Pattern.compile("<email>(.*)</email>");
				Matcher fromEmailMatcher = fromEmailPattern.matcher(matchedEntry);
	
				// Put it into temporary object
				while (fromEmailMatcher.find()) {
					currentMailEntry.setFromEmail(fromEmailMatcher.group(1));
				}
	
				// Add to list
				mailsArrayList.add(currentMailEntry);
				
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
