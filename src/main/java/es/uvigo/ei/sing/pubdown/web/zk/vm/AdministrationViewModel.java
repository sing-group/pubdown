package es.uvigo.ei.sing.pubdown.web.zk.vm;

import static es.uvigo.ei.sing.pubdown.execution.GlobalEvents.EVENT_REFRESH_DATA;
import static es.uvigo.ei.sing.pubdown.execution.GlobalEvents.EVENT_REPOSITORY_QUERY;
import static es.uvigo.ei.sing.pubdown.util.Checks.isEmpty;
import static java.util.Collections.singletonMap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.pubdown.execution.EventQueueUtils;
import es.uvigo.ei.sing.pubdown.execution.EventRepositoryQuery;
import es.uvigo.ei.sing.pubdown.execution.ExecutionEngine;
import es.uvigo.ei.sing.pubdown.execution.GlobalEvents;
import es.uvigo.ei.sing.pubdown.execution.RepositoryQueryScheduled;
import es.uvigo.ei.sing.pubdown.util.Mailer;
import es.uvigo.ei.sing.pubdown.web.entities.GlobalConfiguration;
import es.uvigo.ei.sing.pubdown.web.entities.PasswordRecovery;
import es.uvigo.ei.sing.pubdown.web.entities.Repository;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.entities.User;
import es.uvigo.ei.sing.pubdown.web.zk.util.DesktopTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.ViewModelUtils;

/*** ViewModel to manage the administration panel */

public class AdministrationViewModel extends ViewModelUtils {

	private static final String PARAMETER_REPOSITORY_PATH = "repositoryPath";
	private final static Set<String> NON_REMOVABLE_GLOBAL_CONFIGURATION = new HashSet<>();

	static {
		NON_REMOVABLE_GLOBAL_CONFIGURATION.add(PARAMETER_REPOSITORY_PATH);
	}

	private final TransactionManager tm = new DesktopTransactionManager();

	private static final String GC_UPDATE_EXECUTIONS = "updateExecutions";
	private static final String GC_REFRESH_DATA = "resfreshData";

	static {
		GlobalEvents.fullStatesRegisterGlobalCommand(EVENT_REPOSITORY_QUERY, GC_UPDATE_EXECUTIONS);
		GlobalEvents.fullStatesRegisterGlobalCommand(EVENT_REFRESH_DATA, GC_REFRESH_DATA);
	}

	private static final String PASSWORD_RECOVERY_URL = "/recovery?uuid=";

	private Repository repository;
	private RepositoryQuery repositoryQuery;
	
	private List<User> users;
	private List<Repository> repositories;
	private List<RepositoryQuery> queries;

	private String userFilter;
	private String repositoryQueryFilterByRepositoryName;
	private String repositoryQueryFilterByName;

	private List<GlobalConfiguration> globalConfigurations;

	private final Mailer mailer = new Mailer();

	@Init
	public void init() {
		this.users = getAllUsers();

		this.repositories = getRepositories();

		this.queries = getAllQueries();
		sortQueries(this.queries);

		this.globalConfigurations = getGlobalConfigurations();

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

	private boolean isCurrentUser(final User user) {
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

	public List<Repository> getRepositories() {
		tm.runInTransaction(em -> {
			em.clear();
			this.repositories = em.createQuery("SELECT r FROM Repository r ORDER BY r.name ASC", Repository.class)
					.getResultList();
		});
		return this.repositories;
	}

	/**
	 * Gets all the {@link User} in the DB
	 *
	 * @return a list with all the {@link User} in the DB sorted by login
	 */
	private List<User> getAllUsers() {
		tm.runInTransaction(em -> {
			em.clear();
			this.users = em.createQuery("SELECT u FROM User u ORDER BY u.login ASC", User.class).getResultList();
		});
		return this.users;
	}

	/**
	 * Gets all the {@link RepositoryQuery} in the DB
	 *
	 * @return a list with all the {@link RepositoryQuery} in the DB sorted by
	 *         {@link User}login and {@link RepositoryQuery} name
	 */
	private List<RepositoryQuery> getAllQueries() {
		tm.runInTransaction(em -> {
			em.clear();
			this.queries = em.createQuery("SELECT rq FROM RepositoryQuery rq ORDER BY rq.repository ASC, rq.name ASC",
					RepositoryQuery.class).getResultList();
		});
		return this.queries;
	}

	private void sortQueries(List<RepositoryQuery> queries) {
		queries.sort((rq1, rq2) -> {
			if (rq1.getRepository().getName().compareTo(rq2.getRepository().getName()) == 0) {
				return rq1.getName().compareTo(rq2.getName());
			} else {
				return rq1.getRepository().getName().compareTo(rq2.getRepository().getName());
			}
		});
	}

	public List<GlobalConfiguration> getGlobalConfigurations() {
		return tm.getInTransaction(em -> em
				.createQuery("SELECT gc FROM GlobalConfiguration gc", GlobalConfiguration.class).getResultList());
	}

	private boolean isNonRemovableGlobalConfiguration(GlobalConfiguration globalConfiguration) {
		return NON_REMOVABLE_GLOBAL_CONFIGURATION.contains(globalConfiguration.getConfigurationKey());
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
	public void editUser(@BindingParam("current") final User user) {
		Executions.createComponents("userForm.zul", null, singletonMap("user", user));
	}

	/**
	 * Deletes an {@link User}. It can not delete the current {@link User}
	 *
	 * @param user
	 *            the {@link User} to delete
	 */
	@Command
	public void removeUser(@BindingParam("current") final User user) {
		if (!isCurrentUser(user)) {
			Messagebox.show("Do you want to delete the user?", "Delete User",
					new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
					new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
						switch (event.getName()) {
						case Messagebox.ON_OK:
							tm.runInTransaction(em -> {
								em.remove(user);
							});

							this.users.remove(user);
							this.users = getAllUsers();
							this.queries = getAllQueries();
							for (Repository repository : user.getRepositories()) {
								for (RepositoryQuery repositoryQuery : repository.getRepositoryQueries()) {
									if (repositoryQuery.isRunning()) {
										final RepositoryQueryScheduled repositoryQueryScheduled = new RepositoryQueryScheduled(
												repositoryQuery);
										stopRepositoryQueryExecution(repositoryQueryScheduled);
									}
								}
							}

							postNotifyChangeAdmins(this, "users", "queries");

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
	public void resetUserPassword(@BindingParam("current") final User user) {
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
								postNotifyChangeAdmins(this, "queries", "users");
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
	@NotifyChange("users")
	public void unlockUser(@BindingParam("current") final User user) {
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
							postNotifyChangeAdmins(this, "users");
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
	 * Updates the user list
	 *
	 * @param user
	 *            the {@link User} to add in the user list
	 */
	@GlobalCommand
	public void addUsers(@BindingParam("user") final User user) {
		users.add(user);
		users = getAllUsers();
		postNotifyChangeAdmins(this, "users");
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
		postNotifyChangeAdmins(this, "users");
	}
	
	private void findRepository(Repository repository) {
		tm.runInTransaction(em -> {
			if (!em.contains(repository)) {
				this.repository = em.find(Repository.class, repository.getId());
			} else {
				this.repository = repository;
			}
		});
	}

	private void findRepositoryQuery(RepositoryQuery repositoryQuery) {
		tm.runInTransaction(em -> {
			if (!em.contains(repositoryQuery)) {
				this.repositoryQuery = em.find(RepositoryQuery.class, repositoryQuery.getId());
			} else {
				this.repositoryQuery = repositoryQuery;
			}
		});
	}

	@Command
	public void removeRepository(@BindingParam("current") final Repository repository) {
		findRepository(repository);
		if (this.repository != null) {
			Messagebox.show("Do you want to delete the repository?", "Delete Repository",
					new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
					new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
						switch (event.getName()) {
						case Messagebox.ON_OK:
							for (RepositoryQuery repositoryQuery : this.repository.getRepositoryQueries()) {
								if (repositoryQuery.isRunning()) {
									final RepositoryQueryScheduled repositoryQueryScheduled = new RepositoryQueryScheduled(
											repositoryQuery);
									stopRepositoryQueryExecution(repositoryQueryScheduled);
								}
							}
							final User userToNotify = this.repository.getUser();
							tm.runInTransaction(em -> {
								em.refresh(getCurrentUser(tm));

								this.repository.setUser(null);
							});

							this.repositories.remove(repository);

							this.repositories = getRepositories();
							this.queries = getAllQueries();

							postNotifyChangeAdmins(this, "repositories", "queries");

							publishRefreshData(userToNotify, "repositories");

							break;
						case Messagebox.ON_CANCEL:
						default:
							break;
						}
					}, singletonMap("width", "500"));
		}
	}

	// /**
	// * Updates the user and queries list
	// *
	// * @param repositoryQuery
	// * the {@link RepositoryQuery} to add in the queries list
	// */
	// @GlobalCommand
	// public void addRepositoryQuery(@BindingParam("repositoryQuery") final
	// RepositoryQuery repositoryQuery) {
	// queries.add(repositoryQuery);
	// queries = getAllQueries();
	// users = getAllUsers();
	// postNotifyChangeAdmins(this, "queries", "users");
	// }
	//
	// @GlobalCommand
	// public void updateRepositoryQuery(@BindingParam("repositoryQuery") final
	// RepositoryQuery repositoryQuery) {
	// final int indexOf = this.queries.indexOf(repositoryQuery);
	// this.queries.remove(indexOf);
	// this.queries.add(indexOf, repositoryQuery);
	//
	// sortQueries(this.queries);
	//
	// postNotifyChangeAdmins(this, "queries", "users");
	// }

	@Command
	public void removeRepositoryQuery(@BindingParam("current") final RepositoryQuery repositoryQuery) {
		findRepositoryQuery(repositoryQuery);
		if (this.repositoryQuery != null) {
			Messagebox.show("Do you want to delete the Query?", "Delete Query",
					new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
					new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
						switch (event.getName()) {
						case Messagebox.ON_OK:
							final User userToNotify = this.repositoryQuery.getRepository().getUser();

							tm.runInTransaction(em -> {
								this.repositoryQuery.setRepository(null);
							});
							this.queries.remove(this.repositoryQuery);

							this.users = getAllUsers();
							this.queries = getAllQueries();

							postNotifyChangeAdmins(this, "queries", "users");

							publishRefreshData(userToNotify, "queries");
							break;
						case Messagebox.ON_CANCEL:
							break;
						default:
						}
					}, singletonMap("width", "500"));
		}

	}

	@Command
	public void newGlobalConfiguration() {
		Executions.createComponents("globalConfigurationForm.zul", null,
				singletonMap("globalConfiguration", new GlobalConfiguration()));
	}

	@Command
	public void editGlobalConfiguration(@BindingParam("current") GlobalConfiguration globalConfiguration) {
		Executions.createComponents("globalConfigurationForm.zul", null,
				singletonMap("globalConfiguration", globalConfiguration));
	}

	@GlobalCommand
	public void addGlobalConfiguration(
			@BindingParam("globalConfiguration") final GlobalConfiguration globalConfiguration) {
		this.globalConfigurations.add(globalConfiguration);
		postNotifyChangeAdmins(this, "globalConfigurations");
	}

	@GlobalCommand
	public void updateGlobalConfiguration(
			@BindingParam("globalConfiguration") final GlobalConfiguration globalConfiguration) {
		final int indexOf = this.globalConfigurations.indexOf(globalConfiguration);
		this.globalConfigurations.set(indexOf, globalConfiguration);
		postNotifyChangeAdmins(this, "globalConfigurations");
	}

	@Command
	public void removeGlobalConfiguration(@BindingParam("current") final GlobalConfiguration globalConfiguration) {
		if (globalConfiguration != null) {
			if (!isNonRemovableGlobalConfiguration(globalConfiguration)) {
				Messagebox.show("Do you want to delete the parameter?", "Delete Parameter",
						new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
						new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
							switch (event.getName()) {
							case Messagebox.ON_OK:
								tm.runInTransaction(em -> {
									em.remove(globalConfiguration);
								});
								this.globalConfigurations.remove(globalConfiguration);

								postNotifyChangeAdmins(this, "globalConfigurations");

								break;
							case Messagebox.ON_CANCEL:
							default:
								break;
							}
						}, singletonMap("width", "500"));
			} else {
				Messagebox.show(
						"You can't delete this parameter.\nThis would compromise the proper functioning of the system",
						null, new Messagebox.Button[] { Messagebox.Button.OK }, new String[] { "OK" },
						Messagebox.INFORMATION, null, null, singletonMap("width", "450"));
			}

		}
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
							postNotifyChangeAdmins(this, "queries");
						}
					});
		}
	}

	private void stopRepositoryQueryExecution(final RepositoryQueryScheduled repositoryQueryScheduled) {
		ExecutionEngine.getSingleton().removeTask(repositoryQueryScheduled);
	}

	@GlobalCommand(AdministrationViewModel.GC_UPDATE_EXECUTIONS)
	public void updateExecutions(@BindingParam("task") final RepositoryQueryScheduled repositoryQueryScheduled,
			@BindingParam("action") final String action, @BindingParam("data") boolean toCheck) {

		final RepositoryQuery repositoryQuery = repositoryQueryScheduled.getRepositoryQuery();

		switch (action) {
		case GlobalEvents.ACTION_SCHEDULED:
		case GlobalEvents.ACTION_STARTED:
		case GlobalEvents.ACTION_FINISHED:
			synchronized (this.queries) {
				if (this.queries.contains(repositoryQuery)) {
					final int indexOf = this.queries.indexOf(repositoryQuery);
					this.queries.remove(indexOf);
					this.queries.add(indexOf, repositoryQuery);
				} else {
					this.queries.add(repositoryQuery);
				}
				postNotifyChangeAdmins(this, "queries");
			}
			break;
		case GlobalEvents.ACTION_ABORTED:
			synchronized (repositoryQuery) {
				repositoryQuery.setRunning(false);
				tm.runInTransaction(em -> em.merge(repositoryQuery));
			}

			synchronized (this.queries) {
				this.queries.set(this.queries.indexOf(repositoryQuery), repositoryQuery);
			}
			postNotifyChangeAdmins(this, "repositoryQuery", "queries");
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

	@GlobalCommand(AdministrationViewModel.GC_REFRESH_DATA)
	public void refreshData(@BindingParam("data") String toRefresh) {
		if (toRefresh.equals("queries")) {
			this.queries = getAllQueries();
			postNotifyChangeAdmins(this, "queries");
		} else if (toRefresh.equals("repositories")) {
			this.repositories = getRepositories();
			this.queries = getAllQueries();
			postNotifyChangeAdmins(this, "repositories", "queries");
		} else {
			this.users = getAllUsers();
			postNotifyChangeAdmins(this, "users");
		}
	}

	private void publishRefreshData(final User user, final String... properties) {
		for (String property : properties) {
			final String suffix;
			final String event;
			if (property.equals("queries")) {
				event = GlobalEvents.SUFFIX_QUERIES;
				suffix = "queries";
			} else {
				event = GlobalEvents.SUFFIX_REPOSITORIES;
				suffix = "repositories";
			}

			final EventQueue<Event> userQueue = EventQueueUtils.getUserQueue(user);
			if (userQueue != null) {
				userQueue.publish(new Event(EVENT_REFRESH_DATA + event, null, new EventRepositoryQuery(null, suffix)));
			}
			final EventQueue<Event> adminQueue = EventQueueUtils.getAdminQueue();
			if (adminQueue != null) {
				adminQueue.publish(new Event(EVENT_REFRESH_DATA + event, null, new EventRepositoryQuery(null, suffix)));
			}
		}
	}

}
