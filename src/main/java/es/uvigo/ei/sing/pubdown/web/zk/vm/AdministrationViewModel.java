package es.uvigo.ei.sing.pubdown.web.zk.vm;

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

import es.uvigo.ei.sing.pubdown.execution.ExecutionEngine;
import es.uvigo.ei.sing.pubdown.execution.GlobalEvents;
import es.uvigo.ei.sing.pubdown.util.Mailer;
import es.uvigo.ei.sing.pubdown.web.entities.PasswordRecovery;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.entities.RobotExecution;
import es.uvigo.ei.sing.pubdown.web.entities.User;
import es.uvigo.ei.sing.pubdown.web.zk.util.DesktopTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.ViewModelFunctions;
import es.uvigo.ei.sing.pubdown.web.zk.vm.robot.RobotExecutionTask;

/**
 * ViewModel to manage the administration panel
 */
public class AdministrationViewModel extends ViewModelFunctions {
	private final TransactionManager tm = new DesktopTransactionManager();

	private static final String GC_UPDATE_EXECUTIONS = "updateExecutions";

	static {
		GlobalEvents.fullActionRegisterGlobalCommand(GlobalEvents.ACTION_SCHEDULED,
				AdministrationViewModel.GC_UPDATE_EXECUTIONS);
		GlobalEvents.fullActionRegisterGlobalCommand(GlobalEvents.ACTION_STARTED,
				AdministrationViewModel.GC_UPDATE_EXECUTIONS);
		GlobalEvents.fullActionRegisterGlobalCommand(GlobalEvents.ACTION_FINISHED,
				AdministrationViewModel.GC_UPDATE_EXECUTIONS);
		GlobalEvents.fullActionRegisterGlobalCommand(GlobalEvents.ACTION_ABORTED,
				AdministrationViewModel.GC_UPDATE_EXECUTIONS);
	}

	private final static String[] NAVIGATION_PROPERTIES = new String[] { "robotExecutionList" };

	private static final String PASSWORD_RECOVERY_URL = "/recovery?uuid=";

	private List<User> usersList;
	private List<RepositoryQuery> robotsList;
	private List<RobotExecution> robotExecutionList;
	private String userFilter;
	private String robotFilterByNameDescriptionOwner;
	private String robotFilterByCategoryContentType;
	private final Mailer mailer = new Mailer();

	@Init
	public void init() {
		usersList = getAllUsers();
		robotsList = getAllRobots();
		robotExecutionList = getAllExecutions();
	}

	/**
	 * Getter of the userList global variable
	 * 
	 * @return the value of the userList global variable
	 */
	public List<User> getUsersList() {
		return usersList;
	}

	/**
	 * Getter of the robotsList global variable
	 * 
	 * @return the value of the robotsList global variable
	 */
	public List<RepositoryQuery> getRobotsList() {
		return robotsList;
	}

	public List<RobotExecution> getRobotExecutionList() {
		return robotExecutionList;
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
	 * Getter of the robotFilterByNameDescriptionOwner global variable
	 * 
	 * @return the value of the robotFilterByNameDescriptionOwner global
	 *         variable
	 */
	public String getRobotFilterByNameDescriptionOwner() {
		return robotFilterByNameDescriptionOwner;
	}

	/**
	 * Setter of the robotFilterByNameDescriptionOwner global variable
	 * 
	 * @param robotFilterByNameDescriptionOwner
	 *            the value of the robotFilterByNameDescriptionOwner global
	 *            variable
	 */
	public void setRobotFilterByNameDescriptionOwner(final String robotFilterByNameDescriptionOwner) {
		this.robotFilterByNameDescriptionOwner = robotFilterByNameDescriptionOwner;
	}

	/**
	 * Getter of the robotFilterByCategoryContentType global variable
	 * 
	 * @return the value of the robotFilterByCategoryContentType global variable
	 */
	public String getRobotFilterByCategoryContentType() {
		return robotFilterByCategoryContentType;
	}

	/**
	 * Setter of the robotFilterByCategoryContentType global variable
	 * 
	 * @param robotFilterByCategoryContentType
	 *            the value of the robotFilterByCategoryContentType global
	 *            variable
	 */
	public void setRobotFilterByCategoryContentType(final String robotFilterByCategoryContentType) {
		this.robotFilterByCategoryContentType = robotFilterByCategoryContentType;
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
	private List<RepositoryQuery> getAllRobots() {
		return tm.getInTransaction(
				em -> em.createQuery("SELECT r FROM Robot r ORDER BY r.user ASC, r.name ASC", RepositoryQuery.class)
						.getResultList());
	}

	private List<RobotExecution> getAllExecutions() {
		return tm.getInTransaction(
				em -> em.createQuery("SELECT re FROM RobotExecution re ORDER BY re.userLogin ASC", RobotExecution.class)
						.getResultList());
	}

	/**
	 * Opens a modal window with a form to create an {@link User}
	 */
	@Command
	@NotifyChange({ "usersList" })
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
	@NotifyChange({ "usersList" })
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

								usersList.remove(user);
							});
							robotsList = getAllRobots();
							postNotifyChange(this, "usersList", "robotsList");

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
	@NotifyChange("usersList")
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
										mailer.sendEmail("no_reply@jarvestweb.com", user.getEmail(), "Reset Password",
												resetPasswordMessage);
									} catch (final MessagingException e) {
										e.printStackTrace();
									}
								});
								postNotifyChange(this, "robotsList", "usersList");
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
									mailer.sendEmail("no_reply@jarvestweb.com", user.getEmail(), "Reset Password",
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
	@NotifyChange({ "usersList" })
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
									mailer.sendEmail("no_reply@jarvestweb.com", user.getEmail(), "Unlock User",
											generateUnlockMessage());
								} catch (final MessagingException e) {
									e.printStackTrace();
								}
							});
							postNotifyChange(this, "usersList");
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
	@NotifyChange({ "robotsList" })
	public void newRobot() {
		Executions.createComponents("robotForm.zul", null, singletonMap("robot", new RepositoryQuery()));
	}

	/**
	 * Opens a modal window with a form to edit a {@link RepositoryQuery}
	 * 
	 * @param robot
	 *            the {@link RepositoryQuery} to edit
	 */
	@Command
	@NotifyChange({ "robotsList" })
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
							robot.setUser(null);
							robotsList.remove(robot);
						});
						usersList = getAllUsers();
						postNotifyChange(this, "robotsList", "usersList");

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
	public void addUserList(@BindingParam("user") final User user) {
		usersList.add(user);
		usersList = getAllUsers();
		postNotifyChange(this, "usersList");
	}

	/**
	 * Updates the user list
	 * 
	 * @param user
	 *            the {@link User} to update in the user list
	 */
	@GlobalCommand
	public void updateUserList(@BindingParam("user") final User user) {
		usersList.set(usersList.indexOf(user), user);
		postNotifyChange(this, "usersList");
	}

	/**
	 * Updates the user and robot list
	 * 
	 * @param robot
	 *            the {@link RepositoryQuery} to add in the robot list
	 */
	@GlobalCommand
	public void addRobotList(@BindingParam("robot") final RepositoryQuery robot) {
		robotsList.add(robot);
		robotsList = getAllRobots();
		usersList = getAllUsers();
		postNotifyChange(this, "robotsList", "usersList");
	}

	/**
	 * Updates the robot list
	 * 
	 * @param robot
	 *            the {@link RepositoryQuery} to update in the robot list
	 */
	@GlobalCommand
	public void updateRobotList(@BindingParam("robot") final RepositoryQuery robot) {
		robotsList.set(robotsList.indexOf(robot), robot);
		postNotifyChange(this, "robotsList", "usersList");
	}

	/**
	 * Updates the user and robot list
	 * 
	 * @param robot
	 *            the {@link RepositoryQuery} to delete from an {@link User}
	 */
	@GlobalCommand
	public void deleteRobotFromUser(@BindingParam("robot") final RepositoryQuery robot) {
		robot.setUser(null);
		robotsList.remove(robot);
		robotsList = getAllRobots();
		usersList = getAllUsers();
		postNotifyChange(this, "robotsList", "usersList");
	}

	/**
	 * Filters the {@link User} list by its login or email
	 */
	@Command
	@NotifyChange("usersList")
	public void searchUser() {
		usersList.clear();
		final List<User> users = getAllUsers();

		if (isEmpty(this.userFilter)) {
			usersList.addAll(users);
		} else {
			final String filterLC = this.userFilter.toLowerCase();
			for (final User user : users) {
				if (user.getLogin().toLowerCase().contains(filterLC)
						|| user.getEmail().toLowerCase().contains(filterLC)) {
					usersList.add(user);
				}
			}
		}
	}

	/**
	 * Filters the {@link RepositoryQuery} list by its name, owner or
	 * description
	 */
	@Command
	@NotifyChange("robotsList")
	public void searchRobotByNameDescriptionOwner() {
		robotsList.clear();
		final List<RepositoryQuery> robots = getAllRobots();

		if (isEmpty(this.robotFilterByNameDescriptionOwner)) {
			robotsList.addAll(robots);
		} else {
			final String filterLC = this.robotFilterByNameDescriptionOwner.toLowerCase();
			for (final RepositoryQuery robot : robots) {
				if (robot.getName().toLowerCase().contains(filterLC)
						|| robot.getUser().getLogin().toLowerCase().contains(filterLC)) {
					robotsList.add(robot);
				}
				// if (robot.getDescription() != null) {
				// if (robot.getDescription().toLowerCase().contains(filterLC))
				// robotsList.add(robot);
				// }
			}
		}
	}

	/**
	 * Filters the {@link RepositoryQuery} list by its category or content type
	 */
	@Command
	@NotifyChange("robotsList")
	public void searchRobotByCategoryContentType() {
		robotsList.clear();
		final List<RepositoryQuery> robots = getAllRobots();

		if (isEmpty(this.robotFilterByCategoryContentType)) {
			robotsList.addAll(robots);
		} else {
			final String filterLC = this.robotFilterByCategoryContentType.toLowerCase();
			for (final RepositoryQuery robot : robots) {
				// if (robot.getRepository().toLowerCase().contains(filterLC)
				// || robot.getContentType().toLowerCase().contains(filterLC)) {
				// robotsList.add(robot);
				// }
				// if (robot.getRepository() != null) {
				// if (robot.getRepository().toLowerCase().contains(filterLC))
				// robotsList.add(robot);
				// }
				//
				// if (robot.getContentType() != null) {
				// if (robot.getContentType().toLowerCase().contains(filterLC))
				// robotsList.add(robot);
				// }
			}
		}
	}

	/**
	 * Closes the current {@link User} session
	 */
	@Command
	public void closeSession() {
		closeUserSession();
	}

	@GlobalCommand(AdministrationViewModel.GC_UPDATE_EXECUTIONS)
	public void updateExecutions(@BindingParam("task") final RobotExecutionTask task,
			@BindingParam("action") final String action) {
		final RobotExecution robotExecution = task.getRobotExecution();

		switch (action) {
		case GlobalEvents.ACTION_SCHEDULED:
			synchronized (this.robotExecutionList) {
				this.robotExecutionList.add(robotExecution);
			}
			break;
		default:
			synchronized (this.robotExecutionList) {
				final int indexOf = this.robotExecutionList.indexOf(robotExecution);

				this.robotExecutionList.remove(indexOf);
				this.robotExecutionList.add(indexOf, robotExecution);
			}
		}

		for (final String property : NAVIGATION_PROPERTIES) {
			postNotifyChange(this, property);
		}
	}

	@Command
	public void deleteResult(@BindingParam("currentResult") final RobotExecution robotExecution) {
		Messagebox.show("Do you want to delete the result?", "Delete Result",
				new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
				new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
					switch (event.getName()) {
					case Messagebox.ON_OK:
						tm.runInTransaction(em -> {
							final RobotExecution robotExecutionDelete = em.find(RobotExecution.class,
									robotExecution.getId());
							if (this.robotExecutionList.contains(robotExecutionDelete)) {
								em.remove(robotExecutionDelete);

								this.robotExecutionList.remove(robotExecutionDelete);
							}
						});
						postNotifyChange(this, "robotExecutionList", "robotExecution");

						break;
					case Messagebox.ON_CANCEL:
						break;
					default:
					}
				}, singletonMap("width", "500"));
	}

	@Command
	public void abortExecution(@BindingParam("currentResult") final RobotExecution robotExecution) {
		final String userId = robotExecution.getUserLogin();

		Messagebox.show("Are you sure you want to abort the current execution?", "Abort execution",
				Messagebox.OK | Messagebox.NO, Messagebox.EXCLAMATION, event -> {
					if (event.getName().equals(Messagebox.ON_OK)) {
						ExecutionEngine.getSingleton().cancelUserTask(userId, robotExecution);
					}
				});
	}

	@Command
	public void abortAllExecutions() {
		Messagebox.show("Are you sure you want to abort all executions?", "Abort executions",
				Messagebox.OK | Messagebox.NO, Messagebox.EXCLAMATION, event -> {
					if (event.getName().equals(Messagebox.ON_OK)) {
						ExecutionEngine.getSingleton().cancelAllTasks();
					}
				});
	}

	public List<String> getRobotExecutionUsers() {
		return tm.getInTransaction(em -> em
				.createQuery("SELECT DISTINCT re.userLogin FROM RobotExecution re", String.class).getResultList());
	}

}
