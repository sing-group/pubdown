package es.uvigo.ei.sing.pubdown.web.zk.vm;

import static es.uvigo.ei.sing.pubdown.execution.GlobalEvents.EVENT_REPOSITORY_QUERY;
import static es.uvigo.ei.sing.pubdown.util.Checks.isEmpty;
import static java.util.Collections.singletonMap;

import java.util.List;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.pubdown.execution.GlobalEvents;
import es.uvigo.ei.sing.pubdown.execution.RepositoryQueryScheduled;
import es.uvigo.ei.sing.pubdown.execution.Scheduler;
import es.uvigo.ei.sing.pubdown.util.Mailer;
import es.uvigo.ei.sing.pubdown.web.entities.PasswordRecovery;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.entities.User;
import es.uvigo.ei.sing.pubdown.web.zk.util.DesktopTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.ViewModelFunctions;

/*** ViewModel to manage the administration panel */

public class AdministrationViewModel extends ViewModelFunctions {
	private final TransactionManager tm = new DesktopTransactionManager();

	private static final String GC_UPDATE_EXECUTIONS = "updateExecutions";

	static {
		GlobalEvents.fullStatesRegisterGlobalCommand(EVENT_REPOSITORY_QUERY, GC_UPDATE_EXECUTIONS);
	}

	private static final String PASSWORD_RECOVERY_URL = "/recovery?uuid=";

	private List<User> users;
	private List<RepositoryQuery> queries;

	private String userFilter;
	private String repositoryQueryFilterByRepositoryName;
	private String repositoryQueryFilterByName;

	private final Mailer mailer = new Mailer();

	@Init
	public void init() {
		users = getAllUsers();
		queries = getAllQueries();
	}

	/**
	 * Getter of the userList global variable
	 *
	 * @return the value of the userList global variable
	 */
	public List<User> getusers() {
		return users;
	}

	/**
	 * Getter of the queries global variable
	 *
	 * @return the value of the queries global variable
	 */
	public List<RepositoryQuery> getQueries() {
		return queries;
	}

	/**
	 * Getter of the userFilter global variable
	 *
	 * @return the value of the userFilter global variable
	 */
	public String getUserFilter() {
		return userFilter;
	}

	/**
	 * Setter of the userFilter global variable
	 *
	 * @param userFilter
	 *            the value of the userFilter global variable
	 */
	public void setUserFilter(final String userFilter) {
		this.userFilter = userFilter;
	}

	/**
	 * Checks if the user passed as parameter is the current {@link User}
	 *
	 * @param user
	 *            the {@link User} to check
	 * @return <code>true</code> if the user is the current {@link User},
	 *         <code>false</code> otherwise
	 */
	public boolean isCurrentUser(@BindingParam("currentUser") final User user) {
		return user.equals(getCurrentUser(tm));
	}

	public String getRepositoryQueryFilterByName() {
		return repositoryQueryFilterByName;
	}

	public void setRepositoryQueryFilterByName(String repositoryQueryFilterByName) {
		this.repositoryQueryFilterByName = repositoryQueryFilterByName;
	}

	public String getRepositoryQueryFilterByRepositoryName() {
		return repositoryQueryFilterByRepositoryName;
	}

	public void setRepositoryQueryFilterByRepositoryName(String repositoryQueryFilterByRepositoryName) {
		this.repositoryQueryFilterByRepositoryName = repositoryQueryFilterByRepositoryName;
	}

	/**
	 * Gets all the {@link User} in the DB
	 *
	 * @return a list with all the {@link User} in the DB sorted by login
	 */
	private List<User> getAllUsers() {
		return tm.getInTransaction(
				em -> em.createQuery("SELECT u FROM User u ORDER BY u.login ASC", User.class).getResultList());
	}

	/**
	 * Gets all the {@link RepositoryQuery} in the DB
	 *
	 * @return a list with all the {@link RepositoryQuery} in the DB sorted by
	 *         {@link User}login and {@link RepositoryQuery} name
	 */
	private List<RepositoryQuery> getAllQueries() {
		return tm.getInTransaction(
				em -> em.createQuery("SELECT rq FROM RepositoryQuery rq ORDER BY rq.repository ASC, rq.name ASC",
						RepositoryQuery.class).getResultList());
	}

	/**
	 * Closes the current {@link User} session
	 */
	@Command
	public void closeSession() {
		closeUserSession();
	}

	/**
	 * Opens a modal window with a form to create an {@link User}
	 */
	@Command
	@NotifyChange("users")
	public void newUser() {
		Executions.createComponents("userForm.zul", null, singletonMap("user", new User()));
	}

	/**
	 * Opens a modal window with a form to edit an {@link User}
	 *
	 * @param user
	 *            the {@link User} to edit
	 */
	@Command
	@NotifyChange({ "users" })
	public void editUser(@BindingParam("currentUser") final User user) {
		Executions.createComponents("userForm.zul", null, singletonMap("user", user));
	}

	/**
	 * Deletes an {@link User}. It can not delete the current {@link User}
	 *
	 * @param user
	 *            the {@link User} to delete
	 */
	@Command
	public void deleteUser(@BindingParam("currentUser") final User user) {
		if (!isCurrentUser(user)) {
			Messagebox.show("Do you want to delete the user?", "Delete User",
					new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
					new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
						switch (event.getName()) {
						case Messagebox.ON_OK:
							tm.runInTransaction(em -> {
								em.remove(user);

								users.remove(user);
							});
							queries = getAllQueries();
							postNotifyChange(this, "users", "queries");

							break;
						case Messagebox.ON_CANCEL:
							break;
						default:
						}
					}, singletonMap("width", "500"));
		} else {
			Messagebox.show("You can't delete the actual user", "Information", Messagebox.OK, Messagebox.INFORMATION);
		}
	}

	/**
	 * If the {@link User} is not locked, it locks the {@link User} and sends a
	 * password reset message to that {@link User} (the {@link User} will remain
	 * locked until it changes its password). If the {@link User} is locked, it
	 * will re send the reset password message
	 *
	 * @param user
	 *            the {@link User} to reset its password
	 */
	@Command
	@NotifyChange("users")
	public void resetUserPassword(@BindingParam("currentUser") final User user) {
		final String uuid = UUID.randomUUID().toString();
		final String resetPasswordMessage = generateResetPasswordMessage(uuid);
		final PasswordRecovery passwordRecovery = new PasswordRecovery(user.getLogin(), uuid);

		if (!isCurrentUser(user)) {
			if (!user.isLocked()) {
				Messagebox.show("Do you want to reset the user's password?", "Reset Password",
						new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
						new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
							switch (event.getName()) {
							case Messagebox.ON_OK:

								tm.runInTransaction(em -> {
									user.setLocked(true);
									em.persist(passwordRecovery);
									em.persist(user);
									try {
										mailer.sendEmail("no_reply@pubdown.com", user.getEmail(), "Reset  Password",
												resetPasswordMessage);
									} catch (final MessagingException e) {
										e.printStackTrace();
									}
								});
								postNotifyChange(this, "queries", "users");
								break;
							case Messagebox.ON_CANCEL:
								break;
							default:
							}
						}, singletonMap("width", "500"));
			} else {
				Messagebox.show("The user is locked, do you want to resend the email?", "Reset Password",
						new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
						new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
							switch (event.getName()) {
							case Messagebox.ON_OK:
								try {
									mailer.sendEmail("no_reply@pubdown.com", user.getEmail(), "Reset  Password",
											resetPasswordMessage);
								} catch (final MessagingException e) {
									e.printStackTrace();
								}
								break;
							case Messagebox.ON_CANCEL:
								break;
							default:
							}
						}, singletonMap("width", "500"));
			}
		} else {
			Messagebox.show("You can't reset the password of the actual user", "Information", Messagebox.OK,
					Messagebox.INFORMATION);
		}
	}

	/**
	 * If the {@link User} is locked, unlocks the {@link User} and sends a
	 * unlock user message to that {@link User}
	 *
	 * @param user
	 *            the {@link User} to unlock
	 */
	@Command
	@NotifyChange({ "users" })
	public void unlockUser(@BindingParam("currentUser") final User user) {
		if (!isCurrentUser(user)) {
			Messagebox.show("Do you want to unlock the user?", "Unlock User",
					new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
					new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
						switch (event.getName()) {
						case Messagebox.ON_OK:
							final PasswordRecovery passwordRecovery = tm
									.get(em -> em.find(PasswordRecovery.class, user.getLogin()));
							tm.runInTransaction(em -> {
								user.setLocked(false);
								em.persist(user);
								em.remove(passwordRecovery);
								try {
									mailer.sendEmail("no_reply@pubdown.com", user.getEmail(), "Unlock User",
											generateUnlockMessage());
								} catch (final MessagingException e) {
									e.printStackTrace();
								}
							});
							postNotifyChange(this, "users");
							break;
						case Messagebox.ON_CANCEL:
							break;
						default:
						}
					}, singletonMap("width", "500"));
		}
	}

	/**
	 * Generates a reset password message
	 *
	 * @param uuid
	 *            a random uuid
	 * @return the reset password message
	 */
	private String generateResetPasswordMessage(final String uuid) {
		final HttpServletRequest req = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
		final String url = req.getRequestURL().toString().replace("/zkau", "");
		return new StringBuilder().append("<html>").append("<head><title>Reset Password</title></head>")
				.append("<body><br/><br/>").append(url + PASSWORD_RECOVERY_URL).append(uuid).append("</body>")
				.append("</html>").toString();
	}

	/**
	 * Generates a unlock user message
	 *
	 * @return the unlock user message
	 */
	private String generateUnlockMessage() {
		return new StringBuilder().append("<html>").append("<head><title>Unlock User</title></head>")
				.append("<body><br/><br/>").append("<p>Your user has been unlocked</p>").append("</body>")
				.append("</html>").toString();
	}

	/**
	 * Opens a modal window with a form to create a {@link RepositoryQuery}
	 */
	@Command
	@NotifyChange("queries")
	public void newRepositoryQuery() {
		Executions.createComponents("repositoryQueryForm.zul", null,
				singletonMap("repositoryQuery", new RepositoryQuery()));
	}

	/**
	 * Opens a modal window with a form to edit a {@link RepositoryQuery}
	 *
	 * @param repositoryQuery
	 *            the {@link RepositoryQuery} to edit
	 */
	@Command
	@NotifyChange("queries")
	public void editRepositoryQuery(@BindingParam("current") final RepositoryQuery repositoryQuery) {
		Executions.createComponents("repositoryQueryForm.zul", null, singletonMap("repositoryQuery", repositoryQuery));
	}

	/**
	 * Deletes a {@link RepositoryQuery}
	 *
	 * @param repositoryQuery
	 *            the {@link RepositoryQuery} to delete
	 */
	@Command
	public void removeRepositoryQuery(@BindingParam("current") final RepositoryQuery repositoryQuery) {
		Messagebox.show("Do you want to delete the Query?", "Delete Query",
				new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
				new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
					switch (event.getName()) {
					case Messagebox.ON_OK:
						tm.runInTransaction(em -> {
							repositoryQuery.setRepository(null);
							queries.remove(repositoryQuery);
						});
						users = getAllUsers();
						postNotifyChange(this, "queries", "users");

						break;
					case Messagebox.ON_CANCEL:
						break;
					default:
					}
				}, singletonMap("width", "500"));

	}

	/**
	 * Updates the user list
	 *
	 * @param user
	 *            the {@link User} to add in the user list
	 */
	@GlobalCommand
	public void addUsers(@BindingParam("user") final User user) {
		users.add(user);
		users = getAllUsers();
		postNotifyChange(this, "users");
	}

	/**
	 * Updates the user list
	 *
	 * @param user
	 *            the {@link User} to update in the user list
	 */
	@GlobalCommand
	public void updateUsers(@BindingParam("user") final User user) {
		users.set(users.indexOf(user), user);
		postNotifyChange(this, "users");
	}

	/**
	 * Updates the user and queries list
	 *
	 * @param repositoryQuery
	 *            the {@link RepositoryQuery} to add in the queries list
	 */
	@GlobalCommand
	public void addRepositoryQuery(@BindingParam("repositoryQuery") final RepositoryQuery repositoryQuery) {
		queries.add(repositoryQuery);
		queries = getAllQueries();
		users = getAllUsers();
		postNotifyChange(this, "queries", "users");
	}

	/**
	 * Updates the queries list
	 *
	 * @param repositoryQuery
	 *            the {@link RepositoryQuery} to update in the queries list
	 */
	@GlobalCommand
	public void updateRepositoryQuery(@BindingParam("repositoryQuery") final RepositoryQuery repositoryQuery) {
		queries.set(queries.indexOf(repositoryQuery), repositoryQuery);
		postNotifyChange(this, "queries", "users");
	}

	/**
	 * Updates the user and queries list
	 *
	 * @param repositoryQuery
	 *            the {@link RepositoryQuery} to delete from an {@link User}
	 */
	@GlobalCommand
	public void deleteRepositoryQueryFromUser(@BindingParam("repositoryQuery") final RepositoryQuery repositoryQuery) {
		repositoryQuery.setRepository(null);
		queries.remove(repositoryQuery);
		queries = getAllQueries();
		users = getAllUsers();
		postNotifyChange(this, "queries", "users");
	}

	@Command
	public void abortAllExecutions() {
		Messagebox.show("Do you want to stop all the executions?", "Stop Executions",
				new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
				new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
					if (event.getName().equals(Messagebox.ON_OK)) {
						for (RepositoryQuery repositoryQuery : this.queries) {
							final RepositoryQueryScheduled repositoryQueryScheduled = new RepositoryQueryScheduled(
									repositoryQuery);
							stopRepositoryQueryExecution(repositoryQueryScheduled);
						}
					}
				});
	}

	@Command
	public void abortExecution(@BindingParam("current") RepositoryQuery repositoryQuery) {

		final RepositoryQueryScheduled repositoryQueryScheduled = new RepositoryQueryScheduled(repositoryQuery);
		if (repositoryQuery.isRunning()) {
			Messagebox.show("Do you want to stop the execution?", "Stop Execution",
					new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
					new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
						if (event.getName().equals(Messagebox.ON_OK)) {
							stopRepositoryQueryExecution(repositoryQueryScheduled);
							postNotifyChange(this, "queries");
						}
					});
		}
	}

	private void stopRepositoryQueryExecution(final RepositoryQueryScheduled repositoryQueryScheduled) {
		Scheduler.getSingleton().removeTask(repositoryQueryScheduled);
		final RepositoryQuery repositoryQuery = repositoryQueryScheduled.getRepositoryQuery();
		repositoryQuery.setRunning(false);
		tm.runInTransaction(em -> em.merge(repositoryQuery));
		queries.set(this.queries.indexOf(repositoryQuery), repositoryQuery);
	}

	// @GlobalCommand(AdministrationViewModel.GC_UPDATE_EXECUTIONS)
	// public void updateExecutions(@BindingParam("task") final
	// RobotExecutionTask task,
	// @BindingParam("action") final String action) {
	// final RobotExecution robotExecution = task.getRobotExecution();
	//
	// switch (action) {
	// case GlobalEvents.ACTION_SCHEDULED:
	// synchronized (this.robotExecutionList) {
	// this.robotExecutionList.add(robotExecution);
	// }
	// break;
	// default:
	// synchronized (this.robotExecutionList) {
	// final int indexOf = this.robotExecutionList.indexOf(robotExecution);
	//
	// this.robotExecutionList.remove(indexOf);
	// this.robotExecutionList.add(indexOf, robotExecution);
	// }
	// }
	//
	// for (final String property : NAVIGATION_PROPERTIES) {
	// postNotifyChange(this, property);
	// }
	// }

	@GlobalCommand(AdministrationViewModel.GC_UPDATE_EXECUTIONS)
	public void updateExecutions(@BindingParam("task") final RepositoryQueryScheduled repositoryQueryScheduled,
			@BindingParam("action") final String action, @BindingParam("data") boolean toCheck) {

		System.out.println("ENTRO EN UPDATE ADMIN : " + action);

		final RepositoryQuery repositoryQuery = repositoryQueryScheduled.getRepositoryQuery();

		switch (action) {
		case GlobalEvents.ACTION_SCHEDULED:
		case GlobalEvents.ACTION_STARTED:
		case GlobalEvents.ACTION_FINISHED:
		case GlobalEvents.ACTION_ABORTED:
			synchronized (this.queries) {
				if (this.queries.contains(repositoryQuery)) {
					final int indexOf = this.queries.indexOf(repositoryQuery);
					this.queries.remove(indexOf);
					this.queries.add(indexOf, repositoryQuery);
				} else {
					this.queries.add(repositoryQuery);
				}
				postNotifyChange(this, "queries");
			}
			break;
		default:
		}
	}

	/**
	 * Filters the {@link User} list by its login or email
	 */
	@Command
	@NotifyChange("users")
	public void searchUser() {
		this.users.clear();
		final List<User> users = getAllUsers();

		if (isEmpty(this.userFilter)) {
			this.users.addAll(users);
		} else {
			final String filterLC = this.userFilter.toLowerCase();
			for (final User user : users) {
				if (user.getLogin().toLowerCase().contains(filterLC)
						|| user.getEmail().toLowerCase().contains(filterLC)) {
					this.users.add(user);
				}
			}
		}
	}

	@Command
	@NotifyChange("queries")
	public void searchRepositoryQueryByRepositoryName() {
		this.queries.clear();
		final List<RepositoryQuery> allQueries = getAllQueries();

		if (isEmpty(this.repositoryQueryFilterByRepositoryName)) {
			this.queries.addAll(allQueries);
		} else {
			final String filterLC = this.repositoryQueryFilterByRepositoryName.toLowerCase();
			for (final RepositoryQuery repositoryQuery : allQueries) {
				if (repositoryQuery.getRepository().getName().toLowerCase().contains(filterLC)) {
					this.queries.add(repositoryQuery);
				}
			}
		}
	}

	@Command
	@NotifyChange("queries")
	public void searchRepositoryQueryByName() {
		this.queries.clear();
		final List<RepositoryQuery> allQueries = getAllQueries();

		if (isEmpty(this.repositoryQueryFilterByName)) {
			this.queries.addAll(allQueries);
		} else {
			final String filterLC = this.repositoryQueryFilterByName.toLowerCase();
			for (final RepositoryQuery repositoryQuery : allQueries) {
				if (repositoryQuery.getName().toLowerCase().contains(filterLC)) {
					this.queries.add(repositoryQuery);
				}
			}
		}
	}

}
