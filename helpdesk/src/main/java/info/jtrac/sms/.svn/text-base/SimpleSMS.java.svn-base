package info.jtrac.sms;

public class SimpleSMS implements SMS {
	private String message;
	private String MSISDN;
	private final String PREFIX	=	"+27";
	public SimpleSMS(){
		
	}
	public SimpleSMS(String MSISDN, String message){
		this.setMSISDN(MSISDN);
		this.setMessage(message);
	}
	public String getMSISDN() {
		if(MSISDN!=null){
			if(MSISDN.length()==10){
				return(PREFIX+MSISDN.substring(1, 10));
			}
		}
		return null;
	}

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public void setMSISDN(String mSISDN) {
		MSISDN = mSISDN;
	}
}