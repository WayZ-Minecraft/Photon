package com.photon.network;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.photon.util.NetworkOnly;

@NetworkOnly
public class EmailManager { //TODO EMAILs SENDER
	
	public final static String style = "#body { background: blue; }";
	public final static String startHmtl = "<html><head><style>"+style+"</style></head>";
	public final static String endHmtl = "</html>";

	public static Session getSession() {
		Properties props = new Properties();
        final String pass = "";
        final String from = "";
        props.setProperty("mail.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.username", from);
        props.put("mail.password", pass);
        props.put("mail.smtp.auth", "true");   
        props.put("mail.smtp.starttls.enable", "true");
        
        return Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication(from, pass); }
        });
	}
	
	public static void sendEmail(String from, String[] to, String subject, String[] content) {
		try {
			MimeMessage message = createMessage(getSession(), from, subject, content);
			for(String recivers : to) { message.addRecipient(Message.RecipientType.TO, new InternetAddress(recivers)); }
			Transport.send(message);
		} catch (MessagingException e) {}
	}
	
	public static void sendEmail(String from,String[] to, String subject, MimeBodyPart[] content) {
		try {
	        final MimeMessage message = createMessage(getSession(), from, subject, content);
	        for(String recivers : to) { message.addRecipient(Message.RecipientType.TO, new InternetAddress(recivers)); }
	        Transport.send(message);
		} catch (MessagingException e) {}
    }
	
	private static MimeMessage createMessage(Session session, String from, String subject, MimeBodyPart[] content) throws MessagingException {
		final MimeMessage msg = createBaseMessage(session, from, subject);
		final MimeMultipart msgParts = new MimeMultipart();
		if(content !=null && content.length >= 0) {
			for(MimeBodyPart part : content) { msgParts.addBodyPart(part); }
		}
		msg.setContent(msgParts);
		return msg;
	}
	
	private static MimeMessage createMessage(Session session, String from, String subject, String[] content) throws MessagingException {
		final MimeMessage msg = createBaseMessage(session, from, subject);
		msg.setContent(content[0], content.length > 1 && !content[1].isEmpty() ? content[1] : "text/html; charset=utf-8");
		return msg;
	}
	
	private static MimeMessage createBaseMessage(Session session, String from, String subject) throws AddressException, MessagingException {
		final MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(from));
		msg.setSubject(subject);
		return msg;
	}
}
