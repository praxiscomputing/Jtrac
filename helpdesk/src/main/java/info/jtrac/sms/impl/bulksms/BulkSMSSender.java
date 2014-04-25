package info.jtrac.sms.impl.bulksms;

import static  java.net.URLEncoder.encode;
import static  java.lang.String.format;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.jtrac.sms.SMS;
import info.jtrac.sms.SMSSender;

public class BulkSMSSender implements SMSSender {
	private String bulksmsUrl	=	"http://bulksms.2way.co.za:5567/eapi/submission/send_sms/2/2.0";
	private final String DATA	=	"username=%s&password=%s&message=%s&want_report=1&msisdn=%s";
	private final String ISO	=	"ISO-8859-1";
	private String username;
	private String password;
	private static final Logger logger = LoggerFactory.getLogger(BulkSMSSender.class);
	public BulkSMSSender(){
		
	}
	public void send(final SMS sms){
		new Thread(new Runnable(){
			public void run() {
				doSend(sms);
			}
		}).start();
	}
	private void doSend(SMS sms) {
		try {
            URL url = new URL(bulksmsUrl);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(format(DATA,encode(username, ISO), encode(password, ISO), encode(sms.getMessage(), ISO), encode(sms.getMSISDN(), ISO)));
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
            //e.printStackTrace();
            throw new RuntimeException(e);
        }
	}
	public String getBulksmsUrl() {
		return bulksmsUrl;
	}
	public void setBulksmsUrl(String bulksmsUrl) {
		this.bulksmsUrl = bulksmsUrl;
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
