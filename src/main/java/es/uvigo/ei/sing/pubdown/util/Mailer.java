package es.uvigo.ei.sing.pubdown.util;

import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Utility class to send emails
 */
public class Mailer {
	private final Session session;

	/**
	 * Constructs a {@link Mailer}
	 */
	public Mailer() {
		try {
			final Context initCtx = new InitialContext();
			final Context envCtx = (Context) initCtx.lookup("java:comp/env");

			this.session = (Session) envCtx.lookup("pubdown/mail/Session");
		} catch (final NamingException ne) {
			throw new RuntimeException(ne);
		}
	}

	/**
	 * Sends an email
	 * 
	 * @param from
	 *            the email source
	 * @param email
	 *            the email destination
	 * @param subject
	 *            the email subject
	 * @param message
	 *            the email message
	 * @throws MessagingException
	 *             if there is an error while sending an email
	 */
	public void sendEmail(final String from, final String email, final String subject, final String message)
			throws MessagingException {
		final Message msg = new MimeMessage(session);

		msg.setFrom(new InternetAddress(from));
		final InternetAddress[] toAddresses = { new InternetAddress(email) };
		msg.setRecipients(Message.RecipientType.TO, toAddresses);
		msg.setSubject(subject);
		msg.setSentDate(new Date());
		msg.setContent(message, "text/html");

		Transport.send(msg);
	}
}
