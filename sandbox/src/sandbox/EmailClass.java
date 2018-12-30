package sandbox;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

public class EmailClass {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
				emailSetupAndSend();
		
			}

	private static void emailSetupAndSend() {
		final String fromEmail = "helpline.ybd@gmail.com"; //requires valid gmail id
		final String password = "Spring@2018"; // correct password for gmail id
		final String toEmail = "technical.ybd@gmail.com"; // can be any email id 
		final String ccEmail = "management.ybd@gmail.com";
		
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
		props.put("mail.smtp.port", "587"); //TLS Port
		props.put("mail.smtp.auth", "true"); //enable authentication
		props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS
		
		        //create Authenticator object to pass in Session.getInstance argument
		Authenticator auth = new Authenticator() {
			//override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail, password);
			}
		};
		Session session = Session.getInstance(props, auth);
		
		EmailUtil.sendEmail(session, toEmail,ccEmail,"TLSEmail Testing Subject", "TLSEmail Testing Body");
	}

			
		

}
