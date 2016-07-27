package es.uvigo.ei.sing.pubdown.web.zk.vm;

import static es.uvigo.ei.sing.pubdown.util.Checks.isEmail;

import java.util.UUID;

import javax.mail.MessagingException;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;

import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.pubdown.util.Mailer;
import es.uvigo.ei.sing.pubdown.web.entities.PasswordRecovery;
import es.uvigo.ei.sing.pubdown.web.entities.User;
import es.uvigo.ei.sing.pubdown.web.zk.util.DesktopTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;

/**
 * ViewModel to manage the {@link User} password recovery
 */
public class PasswordRecoveryEmailViewModel {
	private final TransactionManager tm = new DesktopTransactionManager();

	private static final String PASSWORD_RECOVERY_URL = "/recovery?uuid=";

	private String email;
	private final Mailer mailer = new Mailer();

	/**
	 * Getter of the email global variable
	 * 
	 * @return the value of the email global variable
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Setter of the email global variable
	 * 
	 * @param email
	 *            the value of the email global variable
	 */
	public void setEmail(final String email) {
		this.email = email;
	}

	/**
	 * Method linked with {@link PasswordRecoveryEmailViewModel#checkData()}.
	 * Checks if the email global variable is an email
	 * 
	 * @return <code>true</code> if the email is a correct email,
	 *         <code>false</code> otherwise.
	 */
	public boolean isValid() {
		return isEmail(this.email);
	}

	/**
	 * Checks if some variables are valid. Method linked with
	 * {@link PasswordRecoveryEmailViewModel#isValid()}
	 */
	@Command
	@NotifyChange("valid")
	public void checkData() {
	}

	/**
	 * Validates the email global variable
	 * 
	 * @return a error message if the email is empty or it does not exists in
	 *         the DB, nothing otherwise (if the error message is active, it
	 *         will clean it)
	 */
	public Validator getEmailValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String email = (String) ctx.getProperty().getValue();
				if (!isEmail(email)) {
					addInvalidMessage(ctx, "Invalid email");
				} else if (getUserByEmail(email) == null) {
					addInvalidMessage(ctx, "The email doens't exists");
				}
			}
		};
	}

	/**
	 * Gets an {@link User} by his email
	 * 
	 * @param email
	 *            the {@link User} email
	 * @return an {@link User} if its founded, <code>null</code> otherwise
	 */
	private User getUserByEmail(final String email) {
		try {
			return tm.get(em -> em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
					.setParameter("email", email).getSingleResult());
		} catch (final NoResultException e) {
			return null;
		}
	}

	/**
	 * Checks if a login exists in the DB {@link PasswordRecovery} table
	 * 
	 * @param login
	 *            the {@link PasswordRecovery} login
	 * @return <code>true</code> if its founded, <code>false</code> otherwise
	 */
	private boolean checkIfPasswordRecoveryExists(final String login) {
		return tm.get(em -> em.find(PasswordRecovery.class, login)) != null;
	}

	/**
	 * Sends a password recovery message. If the password recovery request does
	 * not exists in the DB {@link PasswordRecovery} table, it will persist it.
	 */
	@Command
	public void sendEmail() {
		final User user = getUserByEmail(this.email);
		final String uuid = UUID.randomUUID().toString();
		tm.runInTransaction(em -> {
			final boolean checkIfExists = checkIfPasswordRecoveryExists(user.getLogin());

			final PasswordRecovery passwordRecovery = checkIfExists ? em.find(PasswordRecovery.class, user.getLogin())
					: new PasswordRecovery(user.getLogin(), uuid);

			if (!checkIfExists) {
				em.persist(passwordRecovery);
			}

			final String passwordRecoveryMessage = checkIfExists
					? generatePasswordRecoveryMessage(passwordRecovery.getUuid())
					: generatePasswordRecoveryMessage(uuid);

			try {
				mailer.sendEmail("no_reply@pubdown.com", this.email, "Password Recovery", passwordRecoveryMessage);
			} catch (final MessagingException e) {
				e.printStackTrace();
			}
		});

		Messagebox.show("An email has been sent to retrieve your password.\nPlease check your inbox.", null,
				Messagebox.OK, Messagebox.INFORMATION, event -> {
					if (event.getName().equals("onOK")) {
						Executions.sendRedirect("/index.zul");
					}
				});
	}

	/**
	 * Generates a password recovery message
	 * 
	 * @param uuid
	 *            the {@link PasswordRecovery} uuid or a random uuid
	 * @return the reset password message
	 */
	private String generatePasswordRecoveryMessage(final String uuid) {
		final HttpServletRequest req = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
		final String url = req.getRequestURL().toString().replace("/zkau", "");
		return new StringBuilder().append("<html>").append("<head><title>Password Recovery</title></head>")
				.append("<body><br/><br/>").append(url + PASSWORD_RECOVERY_URL).append(uuid).append("</body>")
				.append("</html>").toString();
	}
}
