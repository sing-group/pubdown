package es.uvigo.ei.sing.pubdown.web.zk.vm;

import static es.uvigo.ei.sing.pubdown.util.Checks.isEmpty;

import javax.persistence.NoResultException;

import org.mindrot.jbcrypt.BCrypt;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.pubdown.web.entities.PasswordRecovery;
import es.uvigo.ei.sing.pubdown.web.entities.User;
import es.uvigo.ei.sing.pubdown.web.zk.util.DesktopTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;

/**
 * ViewModel to manage the {@link User} password change
 */
public class PasswordRecoveryViewModel {
	private final TransactionManager tm = new DesktopTransactionManager();

	public User user;
	public String retypedPassword;

	/**
	 * Finds an {@link User} by its uuid. The uuid is passed as parameter in the
	 * url.
	 */
	@Init
	public void init() {
		final String uuid = Executions.getCurrent().getParameter("uuid");
		final PasswordRecovery passwordRecovery = getPasswordRecoveryByApiKey(uuid);
		this.user = tm.get(em -> em.find(User.class, passwordRecovery.getLogin()));
		this.user.setPassword("");
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
	 *            the value of the retypedPassword global variable
	 */
	public void setRetypedPassword(final String retypedPassword) {
		this.retypedPassword = retypedPassword;
	}

	/**
	 * Method linked with {@link PasswordRecoveryViewModel#checkData()}. Checs
	 * if the {@link User} password or retypedPassword global variables are
	 * empty
	 * 
	 * @return <code>true</code> if the the {@link User} password or
	 *         retypedPassword are not empty, <code>false</code> otherwise.
	 */
	public boolean isValid() {
		return !isEmpty(this.user.getPassword()) && !isEmpty(this.retypedPassword);
	}

	/**
	 * Checks if some variables are valid. Method linked with
	 * {@link PasswordRecoveryViewModel#isValid()}
	 */
	@Command
	@NotifyChange("valid")
	public void checkData() {
	}

	/**
	 * Gets a {@link PasswordRecovery}
	 * 
	 * @param uuid
	 *            the {@link PasswordRecovery} uuid
	 * @return a {@link PasswordRecovery} if its founded, <code>null</code>
	 *         otherwise
	 */
	private PasswordRecovery getPasswordRecoveryByApiKey(final String uuid) {
		try {
			return tm.get(em -> em
					.createQuery("SELECT u FROM PasswordRecovery u WHERE u.uuid = :uuid", PasswordRecovery.class)
					.setParameter("uuid", uuid).getSingleResult());
		} catch (final NoResultException e) {
			return null;
		}
	}

	/**
	 * Validates the {@link User} password and retype global variables
	 * 
	 * @return a error message if the {@link User} password/retypedPassword are
	 *         empty or if they do not match, nothing otherwise (if the error
	 *         message is active, it will clean it)
	 */
	public Validator getPasswordValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String password = user.getPassword();
				final String retype = (String) ctx.getValidatorArg("retypedPassword");

				if (isEmpty(password) || isEmpty(retype)) {
					addInvalidMessage(ctx, "password", "Password can't be empty");
				} else if (!password.equals(retype)) {
					addInvalidMessage(ctx, "password", "Passwords must be equals");
				}
			}
		};
	}

	/**
	 * Changes an {@link User} password. It will persist the change in the DB
	 * {@link User} table and it will remove the corresponding
	 * {@link PasswordRecovery}. If the {@link User} is locked, it will unlock
	 * it before persist the changes.
	 */
	@Command
	public void changePassword() {
		final PasswordRecovery passwordRecovery = tm.get(em -> em.find(PasswordRecovery.class, this.user.getLogin()));
		tm.runInTransaction(em -> {
			this.user.setPassword(BCrypt.hashpw(this.retypedPassword, BCrypt.gensalt()));
			if (this.user.isLocked()) {
				this.user.setLocked(false);
			}
			em.persist(this.user);
			em.remove(passwordRecovery);
		});

		Messagebox.show("Your password has been changed.", null, Messagebox.OK, Messagebox.INFORMATION, event -> {
			if (event.getName().equals("onOK")) {
				Executions.sendRedirect("/index.zul");
			}
		});
	}
}
