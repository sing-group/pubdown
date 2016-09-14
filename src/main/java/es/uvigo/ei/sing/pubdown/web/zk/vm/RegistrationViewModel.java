package es.uvigo.ei.sing.pubdown.web.zk.vm;

import static es.uvigo.ei.sing.pubdown.util.Checks.isEmail;
import static es.uvigo.ei.sing.pubdown.util.Checks.isEmpty;

import java.util.UUID;

import javax.mail.MessagingException;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;

import org.mindrot.jbcrypt.BCrypt;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.pubdown.util.Mailer;
import es.uvigo.ei.sing.pubdown.web.entities.Registration;
import es.uvigo.ei.sing.pubdown.web.entities.User;
import es.uvigo.ei.sing.pubdown.web.zk.util.DesktopTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.ViewModelFunctions;

/**
 * ViewModel to manage the the {@link User} registration process
 */
public class RegistrationViewModel extends ViewModelFunctions {
	private final TransactionManager tm = new DesktopTransactionManager();

	private static final String confirmationUrl = "/confirmation?uuid=";

	private final Registration registration = new Registration();
	private String retypedPassword;
	private final Mailer mailer = new Mailer();

	/**
	 * Getter of the registration global variable
	 * 
	 * @return the value of the registration global variable
	 */
	public Registration getRegistration() {
		return registration;
	}

	/**
	 * Getter of the retypedPassword global variable
	 * 
	 * @return the value of the retypedPassword global variable
	 */
	public String getRetypedPassword() {
		return retypedPassword;
	}

	/**
	 * Setter of the retypedPassword global variable
	 * 
	 * @param retypedPassword
	 *            the value of retypedPassword global variable
	 */
	public void setRetypedPassword(final String retypedPassword) {
		this.retypedPassword = retypedPassword;
	}

	/**
	 * Method linked with {@link RegistrationViewModel#checkData()}. Checks if
	 * the {@link Registration} email is an email and if the
	 * {@link Registration} login is empty
	 * 
	 * @return <code>true</code> if the {@link Registration} email is an email
	 *         and if the {@link Registration} login is not empty,
	 *         <code>false</code> otherwise.
	 */
	public boolean isValid() {
		return isEmail(this.registration.getEmail()) && !isEmpty(this.registration.getLogin());
	}

	/**
	 * Checks if some variables are valid. Method linked with
	 * {@link RegistrationViewModel#isValid()}
	 */
	@Command
	@NotifyChange("valid")
	public void checkData() {
	}

	/**
	 * Validates the {@link Registration} email global variable
	 * 
	 * @return a error message if the {@link Registration} email is not an email
	 *         or it does exist in the DB, nothing otherwise (if the error
	 *         message is active, it will clean it)
	 */
	public Validator getEmailValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String email = (String) ctx.getProperty().getValue();

				if (!isEmail(email)) {
					addInvalidMessage(ctx, "Invalid email");
				} else if (checkIfEmailExistsRegistration(email) || checkIfEmailExistsUser(email)) {
					addInvalidMessage(ctx, "The email already exists");
				}
			}
		};
	}

	/**
	 * Validates the {@link Registration} login global variable
	 * 
	 * @return a error message if the {@link Registration} login is empty or it
	 *         does not exists in the DB, nothing otherwise (if the error
	 *         message is active, it will clean it)
	 */
	public Validator getLoginValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String login = (String) ctx.getProperty().getValue();

				if (isEmpty(login)) {
					addInvalidMessage(ctx, "Login can't be empty");
				} else if (checkIfLoginExistsRegistration(login) || checkIfLoginExistsUser(login)) {
					addInvalidMessage(ctx, "The login already exists");
				}
			}
		};
	}

	/**
	 * Validates the {@link Registration} password and retype global variables
	 * 
	 * @return a error message if the {@link Registration}
	 *         password/retypedPassword are empty or if they do not match,
	 *         nothing otherwise (if the error message is active, it will clean
	 *         it)
	 */
	public Validator getPasswordValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String password = registration.getPassword();
				final String retype = (String) ctx.getValidatorArg("retypedPassword");

				if (isEmpty(password) || isEmpty(retype)) {
					addInvalidMessage(ctx, "password", "Password can't be empty");
				} else if (!password.equals(retype)) {
					addInvalidMessage(ctx, "password", "Passwords must be equals");
				}
			}
		};
	}

	// public Validator getApikeyValidator() {
	// return new AbstractValidator() {
	// @Override
	// public void validate(final ValidationContext ctx) {
	// final String apikey = (String) ctx.getProperty().getValue();
	//
	// if (isEmpty(apikey)) {
	// addInvalidMessage(ctx, "Apikey can't be empty");
	// }
	// }
	// };
	// }

	/**
	 * Checks if exists a {@link Registration} in DB {@link Registration} table
	 * 
	 * @param email
	 *            the {@link Registration} email
	 * @return <code>true</code> if exists, <code>false</code> otherwise
	 */
	private boolean checkIfEmailExistsRegistration(final String email) {
		try {
			tm.run(em -> em.createQuery("SELECT u FROM Registration u WHERE u.email = :email", Registration.class)
					.setParameter("email", email).getSingleResult());
			return true;
		} catch (final NoResultException e) {
			return false;
		}
	}

	/**
	 * Checks if exists a {@link User} in DB {@link User} table
	 * 
	 * @param email
	 *            the {@link User} email
	 * @return <code>true</code> if exists, <code>false</code> otherwise
	 */
	private boolean checkIfEmailExistsUser(final String email) {
		try {
			tm.run(em -> em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
					.setParameter("email", email).getSingleResult());
			return true;
		} catch (final NoResultException e) {
			return false;
		}
	}

	/**
	 * Checks if exists a {@link Registration} in DB {@link Registration} table
	 * 
	 * @param login
	 *            the {@link Registration} login
	 * @return <code>true</code> if exists, <code>false</code> otherwise
	 */
	private boolean checkIfLoginExistsRegistration(final String login) {
		return tm.get(em -> em.find(Registration.class, login)) != null;
	}

	/**
	 * Checks if exists a {@link User} in DB {@link User} table
	 * 
	 * @param login
	 *            the {@link User} login
	 * @return <code>true</code> if exists, <code>false</code> otherwise
	 */
	private boolean checkIfLoginExistsUser(final String login) {
		return tm.get(em -> em.find(User.class, login)) != null;
	}

	/**
	 * Generates a random UUID and assigns its value to the {@link Registration}
	 * apikey global variable
	 */
	@Command
	@NotifyChange("registration")
	public void generateRandomUUID() {
		this.registration.setApiKey(UUID.randomUUID().toString());
	}

	/**
	 * Persists a {@link Registration} and sends a registration message to the
	 * {@link Registration} email. The {@link User} will not be activated in the
	 * application until the user visit the confirmation link in the
	 * registration message
	 */
	@Command
	public void registerUser() {
		final String uuid = UUID.randomUUID().toString();
		final Registration registrationUser = new Registration(this.registration.getLogin(),
				BCrypt.hashpw(this.registration.getPassword(), BCrypt.gensalt()), this.registration.getApiKey(),
				this.registration.getEmail(), uuid);
		tm.runInTransaction(em -> {
			em.persist(registrationUser);
			try {
				final String registrationMessage = generateRegistrationMessage(registrationUser.getUuid());
				mailer.sendEmail("no_reply@pubdown.com", registrationUser.getEmail(), "Confirm your registration",
						registrationMessage);
			} catch (final MessagingException e) {
				e.printStackTrace();
			}
		});

		Messagebox.show(
				"User registration complete.\nAn email has been sent to confirm your registration.\nPlease check your inbox.",
				null, Messagebox.OK, Messagebox.INFORMATION, event -> {
					if (event.getName().equals("onOK")) {
						Executions.sendRedirect("/index.zul");
					}
				});
	}

	/**
	 * Generates a registration message
	 * 
	 * @param uuid
	 *            the {@link Registration} uuid
	 * @return a registration message
	 */
	private String generateRegistrationMessage(final String uuid) {
		final HttpServletRequest req = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
		final String url = req.getRequestURL().toString().replace("/zkau", "");
		return new StringBuilder().append("<html>").append("<head><title>Confirm registration</title></head>")
				.append("<body><br/><br/>").append(url + confirmationUrl).append(uuid).append("</body>")
				.append("</html>").toString();
	}

}
