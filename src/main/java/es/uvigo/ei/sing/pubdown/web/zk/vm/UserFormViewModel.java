package es.uvigo.ei.sing.pubdown.web.zk.vm;

import static es.uvigo.ei.sing.pubdown.util.Checks.isEmail;
import static es.uvigo.ei.sing.pubdown.util.Checks.isEmpty;
import static java.util.Collections.singletonMap;

import java.util.UUID;

import javax.persistence.NoResultException;

import org.mindrot.jbcrypt.BCrypt;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.pubdown.execution.EventQueueUtils;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.entities.User;
import es.uvigo.ei.sing.pubdown.web.zk.util.DesktopTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.ViewModelFunctions;

/**
 * ViewModel to manage the form to create/edit {@link User}
 */
public class UserFormViewModel extends ViewModelFunctions {
	private final TransactionManager tm = new DesktopTransactionManager();

	private User user;
	private User uneditedUser;

	/**
	 * Assigns the user received as parameter to the {@link UserFormViewModel}
	 * global variables. If the user exists, it will refresh it.
	 * 
	 * @param user
	 *            the {@link User}
	 */
	@Init
	public void init(@ExecutionArgParam("user") final User user) {
		if (user.getLogin() != null) {
			tm.runInTransaction(em -> {
				em.refresh(user);
			});
		}
		this.user = user;
		this.uneditedUser = user.clone();
	}

	/**
	 * Getter of the user global variable
	 * 
	 * @return the value of the user global variable
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Checks if an {@link User} is registered
	 * 
	 * @return <code>true</code> if the {@link User} login global variable is
	 *         not empty (is not a new user), <code>false</code> otherwise
	 */
	public boolean isRegistered() {
		return !isEmpty(this.getUser().getLogin());
	}

	/**
	 * Checks if any of the {@link User} fields are modified
	 * 
	 * @return <code>true</code> if any field has been modified,
	 *         <code>false</code> otherwise
	 */
	private boolean isUserModified() {
		return this.user.compareTo(this.uneditedUser) != 0;
	}

	/**
	 * Method linked with {@link UserFormViewModel#checkData()}. Checks if the
	 * {@link User} email is an email, if the {@link User} login is empty, if
	 * the {@link User} password is empty and if the {@link User} apikey is
	 * empty
	 * 
	 * @return <code>true</code> if the {@link User} email is an email, if the
	 *         {@link User} login is not empty, if the {@link User} password is
	 *         not empty and if the {@link User} apikey is not empty,
	 *         <code>false</code> otherwise.
	 */
	public boolean isValid() {
		return isEmail(this.user.getEmail()) && !isEmpty(this.user.getLogin()) && !isEmpty(this.user.getPassword())
				&& !isEmpty(this.user.getApiKey());
	}

	/**
	 * Checks if some variables are valid. Method linked with
	 * {@link UserFormViewModel#isValid()}
	 */
	@Command
	@NotifyChange("valid")
	public void checkData() {
	}

	/**
	 * Validates the {@link User} email global variable
	 * 
	 * @return a error message if the {@link User} email is not an email or it
	 *         does exist in the DB, nothing otherwise (if the error message is
	 *         active, it will clean it)
	 */
	public Validator getEmailValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String email = (String) ctx.getProperty().getValue();

				if (!isEmail(email)) {
					addInvalidMessage(ctx, "Invalid email");
				} else if (!email.equals(uneditedUser.getEmail()) && checkIfEmailExists(email)) {
					addInvalidMessage(ctx, "The email already exists");
				}
			}
		};
	}

	/**
	 * Validates the {@link User} login global variable
	 * 
	 * @return a error message if the {@link User} login is empty or it does
	 *         exists in the DB, nothing otherwise (if the error message is
	 *         active, it will clean it)
	 */
	public Validator getLoginValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String login = (String) ctx.getProperty().getValue();

				if (isEmpty(login)) {
					addInvalidMessage(ctx, "Login can't be empty");
				} else if (!login.equals(uneditedUser.getLogin()) && checkIfLoginExists(login)) {
					addInvalidMessage(ctx, "The login already exists");
				}
			}
		};
	}

	/**
	 * Validates the {@link User} apikey global variable
	 * 
	 * @return a error message if the {@link User} apikey is empty or it does
	 *         exists in the DB, nothing otherwise (if the error message is
	 *         active, it will clean it)
	 */
	public Validator getApiKeyValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String apiKey = (String) ctx.getProperty().getValue();

				if (isEmpty(apiKey)) {
					addInvalidMessage(ctx, "API key can't be empty");
				} else if (!apiKey.equals(uneditedUser.getApiKey()) && checkIfApiKeyExists(apiKey)) {
					addInvalidMessage(ctx, "The API key already exists");
				}
			}
		};
	}

	/**
	 * Validates the {@link User} password and retype global variables
	 * 
	 * @return a error message if the {@link User} password is empty, nothing
	 *         otherwise (if the error message is active, it will clean it)
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
	 * Generates a random UUID and assigns its value to the {@link User} apikey
	 * global variable
	 */
	@Command
	@NotifyChange("user")
	public void generateRandomUUID() {
		this.user.setApiKey(UUID.randomUUID().toString());
	}

	/**
	 * Persists the {@link User} changes and notifies the changes to the
	 * {@link AdministrationViewModel}
	 */
	@Command
	public void confirm() {
		if (isUserModified()) {
			tm.runInTransaction(em -> {
				final String command = checkIfLoginExists(this.user.getLogin()) ? "updateUserList" : "addUserList";
				final String updatePassword = BCrypt.hashpw(this.user.getPassword(), BCrypt.gensalt());
				this.user.setPassword(updatePassword);
				em.persist(this.user);

				BindUtils.postGlobalCommand(EventQueueUtils.QUEUE_NAME, null, command, singletonMap("user", this.user));
			});
		}
	}

	/**
	 * Refresh the {@link User} in the form
	 */
	@Command
	@NotifyChange("user")
	public void refresh() {
		if (isUserModified()) {
			tm.runInTransaction(em -> {
				em.refresh(this.user);

				this.uneditedUser = this.user.clone();
			});
		}
	}

	/**
	 * Checks if exists a {@link User} in DB {@link User} table
	 * 
	 * @param apiKey
	 *            the {@link User} apiKey
	 * @return <code>true</code> if exists, <code>false</code> otherwise
	 */
	private boolean checkIfApiKeyExists(final String apiKey) {
		try {
			tm.run(em -> em.createQuery("SELECT u FROM User u WHERE u.apiKey = :apiKey", User.class)
					.setParameter("apiKey", apiKey).getSingleResult());
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
	private boolean checkIfEmailExists(final String email) {
		try {
			tm.run(em -> em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
					.setParameter("email", email).getSingleResult());
			return true;
		} catch (final NoResultException e) {
			return false;
		}
	}

	/**
	 * Checks if exists a {@link User} in DB {@link User} table
	 * 
	 * @param login
	 *            the {@link User} login
	 * @return <code>true</code> if exists, <code>false</code> otherwise
	 */
	private boolean checkIfLoginExists(final String login) {
		return tm.get(em -> em.find(User.class, login)) != null;
	}

	/**
	 * Opens a modal window with a form to edit a {@link RepositoryQuery}
	 * 
	 * @param robot
	 *            the {@link RepositoryQuery} to edit
	 */
	@Command
	public void editRobot(@BindingParam("currentRobot") final RepositoryQuery robot) {
		Executions.createComponents("robotForm.zul", null, singletonMap("robot", robot));
	}

	/**
	 * Deletes a {@link RepositoryQuery}
	 * 
	 * @param robot
	 *            the {@link RepositoryQuery} to delete
	 */
	@Command
	public void deleteRobot(@BindingParam("currentRobot") final RepositoryQuery robot) {
		Messagebox.show("Do you want to delete the robot?", "Delete Robot",
				new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
				new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
					switch (event.getName()) {
					case Messagebox.ON_OK:
						tm.runInTransaction(em -> {
							this.user.removeRepositoryQuery(robot);
						});

						postNotifyChange(this, "user");

						BindUtils.postGlobalCommand(EventQueueUtils.QUEUE_NAME, null, "deleteRobotFromUser",
								singletonMap("robot", robot));
						break;
					case Messagebox.ON_CANCEL:
						break;
					default:
					}
				}, singletonMap("width", "500"));
	}

}
