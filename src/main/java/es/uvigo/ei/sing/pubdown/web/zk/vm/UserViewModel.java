package es.uvigo.ei.sing.pubdown.web.zk.vm;

import static es.uvigo.ei.sing.pubdown.util.Checks.isEmpty;
import static es.uvigo.ei.sing.pubdown.web.entities.Role.ADMIN;
import static java.util.Collections.singletonMap;

import org.mindrot.jbcrypt.BCrypt;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.pubdown.web.entities.Role;
import es.uvigo.ei.sing.pubdown.web.entities.User;
import es.uvigo.ei.sing.pubdown.web.zk.util.DesktopTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.ViewModelFunctions;

/**
 * ViewModel to manage the {@link User} login process
 */
public class UserViewModel extends ViewModelFunctions {
	private final TransactionManager tm = new DesktopTransactionManager();

	private String login = "";
	private String password = "";

	/**
	 * Getter of the login global variable
	 * 
	 * @return the value of the login global variable
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * Setter of the login global variable
	 * 
	 * @param login
	 *            the value of the login global variable
	 */
	public void setLogin(final String login) {
		this.login = login;
	}

	/**
	 * Getter of the password global variable
	 * 
	 * @return the value of the password global variable
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Setter of the password global variable
	 * 
	 * @param password
	 *            the value of the password global variable
	 */
	public void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * Method linked with {@link UserViewModel#checkData()}. Checks if the login
	 * and password global variables are empty
	 * 
	 * @return <code>true</code> if the login and password are not empty,
	 *         <code>false</code> otherwise.
	 */
	public boolean isValid() {
		return !isEmpty(this.login) && !isEmpty(this.password);
	}

	/**
	 * Checks if some variables are valid. Method linked with
	 * {@link UserViewModel#isValid()}
	 */
	@Command
	@NotifyChange("valid")
	public void checkData() {
	}

	/**
	 * Validates for the login global variable
	 * 
	 * @return a error message if the login is empty, nothing otherwise (if the
	 *         error message is active, it will clean it)
	 */
	public Validator getLoginValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String login = (String) ctx.getProperty().getValue();

				if (isEmpty(login)) {
					addInvalidMessage(ctx, "Login can't be empty");
				}
			}
		};
	}

	/**
	 * Validates for the password global variable
	 * 
	 * @return a error message if the password is empty, nothing otherwise (if
	 *         the error message is active, it will clean it)
	 */
	public Validator getPasswordValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String password = (String) ctx.getProperty().getValue();

				if (isEmpty(password)) {
					addInvalidMessage(ctx, "Password can't be empty");
				}
			}
		};
	}

	/**
	 * Checks if the {@link User} login and password matches with the login and
	 * password introduced by an user. If both matches, if the {@link User} is
	 * locked it will show a message, if the {@link User} is not locked it will
	 * redirect to the administration page if the {@link User} is a
	 * {@link Role#ADMIN} or main page if its a {@link Role#USER}. If both
	 * fields do not match, it will show a message
	 */
	@Command
	public void checkLogin() {
		final User user = tm.get(em -> em.find(User.class, this.login));
		if (user != null && BCrypt.checkpw(this.password, user.getPassword())) {
			if (!user.isLocked()) {
				Sessions.getCurrent(true).setAttribute(ViewModelFunctions.USER_SESSION_KEY, user);
				if (user.getRole().equals(ADMIN)) {
					Executions.sendRedirect("administration.zul");
				} else {
					Executions.sendRedirect("main.zul");
				}
			} else {
				Messagebox.show(
						"Your user is locked.\nAn email has been sent to change your password.\nPlease check your inbox.",
						null, new Messagebox.Button[] { Messagebox.Button.OK }, new String[] { "OK" },
						Messagebox.INFORMATION, null, null, singletonMap("width", "400"));
			}
		} else {
			Messagebox.show("Incorrect login or password");
		}
	}
}
