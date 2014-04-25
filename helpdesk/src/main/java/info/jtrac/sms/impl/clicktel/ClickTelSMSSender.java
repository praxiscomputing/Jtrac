package info.jtrac.sms.impl.clicktel;

import static java.lang.String.format;
import static java.net.URLEncoder.encode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.jtrac.sms.SMS;
import info.jtrac.sms.SMSSender;

public class ClickTelSMSSender implements SMSSender {

	private String clickTelURL = "http://api.clickatell.com/http/sendmsg";
	private String username;
	private String password;
	private String api_id;
	private String ISO = "ISO-8859-1";
	private String DATA = "api_id=%s&user=%s&password=%s&text=%s&to=%s";
	private static final Logger logger = LoggerFactory.getLogger(ClickTelSMSSender.class);
	
	public ClickTelSMSSender() {
		
	}
	
	public void send(final SMS sms) {
		
		new Thread(new Runnable() {
			
			public void run() {
				doSend(sms);
			}
		}).start();
		
	}
	
	public String getApi_id() {
		return api_id;
	}

	public void setApi_id(String api_id) {
		this.api_id = api_id;
	}

	private void doSend(SMS sms) {
		
		try {
            URL url = new URL(clickTelURL);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(format(DATA, encode(api_id, ISO), encode(username, ISO), encode(password, ISO), encode(sms.getMessage(), ISO), encode(sms.getMSISDN(), ISO)));
            wr.flush();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            String response	=	"";
            while ((line = rd.readLine()) != null) {
            	response+=line;
            }
            wr.close();
            rd.close();
            logger.error(String.format("SMS ======================= %s ===============", response));
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	
}
