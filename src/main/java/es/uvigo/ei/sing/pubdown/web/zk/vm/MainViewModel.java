package es.uvigo.ei.sing.pubdown.web.zk.vm;

import static es.uvigo.ei.sing.pubdown.util.Checks.isEmpty;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.pubdown.execution.Scheduler;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.RepositoryManager;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.pubmed.PubMedDownloader;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.scopus.ScopusDownloader;
import es.uvigo.ei.sing.pubdown.web.entities.GlobalConfiguration;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.entities.RobotExecution;
import es.uvigo.ei.sing.pubdown.web.entities.Task;
import es.uvigo.ei.sing.pubdown.web.entities.User;
import es.uvigo.ei.sing.pubdown.web.zk.tree.RepositoryQueryTreeNode;
import es.uvigo.ei.sing.pubdown.web.zk.tree.RepositoryTreeModel;
import es.uvigo.ei.sing.pubdown.web.zk.util.CleanEntityManagerTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.DesktopTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.ViewModelFunctions;

/**
 * ViewModel to manage the user panel
 */
public class MainViewModel extends ViewModelFunctions {
	private final TransactionManager tm = new DesktopTransactionManager();

	// private static final String GC_UPDATE_EXECUTIONS = "updateExecutions";
	//
	// static {
	// GlobalEvents.fullActionRegisterGlobalCommand(GlobalEvents.ACTION_SCHEDULED,
	// MainViewModel.GC_UPDATE_EXECUTIONS);
	// GlobalEvents.fullActionRegisterGlobalCommand(GlobalEvents.ACTION_STARTED,
	// MainViewModel.GC_UPDATE_EXECUTIONS);
	// GlobalEvents.fullActionRegisterGlobalCommand(GlobalEvents.ACTION_FINISHED,
	// MainViewModel.GC_UPDATE_EXECUTIONS);
	// GlobalEvents.fullActionRegisterGlobalCommand(GlobalEvents.ACTION_ABORTED,
	// MainViewModel.GC_UPDATE_EXECUTIONS);
	// }
	//
	// private final static String[] NAVIGATION_PROPERTIES = new String[] {
	// "robotExecutionList" };

	private RepositoryQuery repositoryQuery;
	private RepositoryQuery uneditedRepositoryQuery;
	private Task repositoryQueryTask;
	private Task uneditedRepositoryQueryTask;
	private RepositoryTreeModel repositoryModel;
	private List<RepositoryQuery> runningRepositoryQuery;
	private RobotExecution robotExecution;
	private String repositoryPath;
	private User currentUser;

	/**
	 * Initializes variables
	 */
	@Init
	public void init() {
		this.currentUser = getCurrentUser(tm);
		this.repositoryModel = new RepositoryTreeModel(getCurrentUser(tm).getRepositoriesQueryByRepository());
		this.setRepositoryQuery(new RepositoryQuery());
		this.runningRepositoryQuery = tm.get(em -> em
				.createQuery("SELECT r FROM RepositoryQuery r WHERE r.user = :user AND (r.running = 1 or r.checked= 1)",
						RepositoryQuery.class)
				.setParameter("user", getCurrentUser(tm)).getResultList());

		this.robotExecution = null;
	}

	/**
	 * Getter of the repositoryQuery global variable
	 * 
	 * @return the value of the repositoryQuery global variable
	 */
	public RepositoryQuery getRepositoryQuery() {
		return repositoryQuery;
	}

	/**
	 * Setter of the repositoryQuery global variable
	 * 
	 * @param repositoryQuery
	 *            the value of the repositoryQuery global variable
	 */
	public void setRepositoryQuery(RepositoryQuery repositoryQuery) {

		this.repositoryQuery = repositoryQuery;

		this.repositoryQueryTask = repositoryQuery.getTask();

		this.uneditedRepositoryQuery = this.repositoryQuery.clone();

		this.uneditedRepositoryQueryTask = this.repositoryQuery.getTask();

		this.repositoryModel.setSelectedRepositoryQuery(this.repositoryQuery);
	}

	public User getCurrentUser() {
		return this.currentUser;
	}

	public String getRepositoryPath() {
		tm.runInTransaction(em -> {
			em.clear();
			this.repositoryPath = em
					.createQuery("SELECT g FROM GlobalConfiguration g WHERE g.configurationKey = :path",
							GlobalConfiguration.class)
					.setParameter("path", "repositoryPath").getSingleResult().getConfigurationValue();
		});
		return this.repositoryPath;
	}

	/**
	 * Getter of the uneditedRepositoryQuery global variable
	 * 
	 * @return the value of the uneditedRepositoryQuery global variable
	 */
	public RepositoryQuery getUneditedRepositoryQuery() {
		return uneditedRepositoryQuery;
	}

	/**
	 * Setter of the uneditedRepositoryQuery global variable
	 * 
	 * @param uneditedRepositoryQuery
	 *            the value of the uneditedRepositoryQuery global variable
	 */
	public void setUneditedRepositoryQuery(final RepositoryQuery uneditedRepositoryQuery) {
		this.uneditedRepositoryQuery = uneditedRepositoryQuery;
	}

	public Task getRepositoryQueryTask() {
		return repositoryQueryTask;
	}

	public Task getUneditedRepositoryQueryTask() {
		return uneditedRepositoryQueryTask;
	}

	public List<RepositoryQuery> getRunningRepositoryQuery() {
		return runningRepositoryQuery;
	}

	/**
	 * Getter of the repositoryModel global variable
	 * 
	 * @return the value of the repositoryModel global variable
	 */
	public RepositoryTreeModel getRepositoryModel() {
		return this.repositoryModel;
	}

	/**
	 * Gets the {@link RepositoryQuery} repositories of the current
	 * {@link User}.
	 * 
	 * @return a sortedSet with all the {@link RepositoryQuery} repositories of
	 *         the current {@link User}
	 */
	public SortedSet<String> getRepositories() {
		return new TreeSet<>(getCurrentUser(tm).getRepositoriesQueryByRepository().keySet().stream()
				.filter(category -> !category.isEmpty()).collect(toSet()));
	}

	public RobotExecution getRobotExecution() {
		return this.robotExecution;
	}

	public void setRobotExecution(final RobotExecution robotExecution) {
		this.robotExecution = robotExecution;
	}

	/**
	 * Checks if a {@link RepositoryQuery} is new
	 * 
	 * @return <code>true</code> if the {@link RepositoryQuery} is new,
	 *         <code>false</code> otherwise
	 */
	private boolean isNewRepositoryQuery() {
		return this.repositoryQuery.getId() == null;
	}

	/**
	 * Checks if any of the {@link RepositoryQuery} fields are modified
	 * 
	 * @return <code>true</code> if any field has been modified,
	 *         <code>false</code> otherwise
	 */
	public boolean isRepositoryQueryModified() {
		return this.repositoryQuery.compareTo(this.uneditedRepositoryQuery) != 0;
	}

	public boolean isRepositoryQueryTaskModified() {
		return this.repositoryQueryTask.compareTo(this.uneditedRepositoryQueryTask) != 0;
	}

	/**
	 * Checks if the selected {@link RepositoryQuery} in the repositoryModel is
	 * a {@link RepositoryQueryTreeNode}
	 * 
	 * @return <code>true</code> if the selected {@link RepositoryQuery} is a
	 *         {@link RepositoryQueryTreeNode}, <code>false</code> otherwise
	 */
	@DependsOn("repositoryQuery")
	public boolean isSelected() {
		return this.repositoryModel.getSelectedRepositoryQuery() == null;
	}

	/**
	 * Method linked with {@link MainViewModel#checkData()}. Checks if the
	 * {@link RepositoryQuery} name is an empty
	 * 
	 * @return <code>true</code> if the {@link RepositoryQuery} is not empty,
	 *         <code>false</code> otherwise.
	 */
	public boolean isValidRepositoryQuery() {
		return !isEmpty(this.repositoryQuery.getName()) && !isEmpty(this.repositoryQuery.getQuery())
				&& !isEmpty(this.repositoryQuery.getRepository());
	}

	public boolean isUserApiKeyValid() {
		return !(getCurrentUser(tm).getApiKey() == null || getCurrentUser(tm).getApiKey().isEmpty());
	}

	public boolean isQueryReadyToCheckResult() {
		return isValidRepositoryQuery() && (this.repositoryQuery.isScopus() || this.repositoryQuery.isPubmed())
				&& (this.repositoryQuery.isFulltextPaper() || this.repositoryQuery.isAbstractPaper());
	}

	public boolean isRepositoryQueryChecked() {
		return this.repositoryQuery.isChecked();
	}

	/**
	 * Checks if some variables are valid. Method linked with
	 * {@link MainViewModel#isValidRepositoryQuery()}
	 */
	@Command
	@NotifyChange({ "validRepositoryQuery", "queryReadyToCheckResult" })
	public void checkData() {
	}

	@Command
	@NotifyChange("userApiKeyValid")
	public void checkUserApiKey() {
	}

	/**
	 * Validates the {@link RepositoryQuery} name global variable
	 * 
	 * @return a error message if the {@link RepositoryQuery} name is empty,
	 *         nothing otherwise (if the error message is active, it will clean
	 *         it)
	 */
	public Validator getNameValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String name = (String) ctx.getProperty().getValue();

				if (isEmpty(name)) {
					addInvalidMessage(ctx, "Name can't be empty");
				}
			}
		};
	}

	public Validator getQueryValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String query = (String) ctx.getProperty().getValue();

				if (isEmpty(query)) {
					addInvalidMessage(ctx, "Query can't be empty");
				}
			}
		};
	}

	public Validator getRepositoryValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String repository = (String) ctx.getProperty().getValue();

				if (isEmpty(repository)) {
					addInvalidMessage(ctx, "Repository can't be empty");
				}
			}
		};
	}

	/**
	 * Closes the current {@link User} session
	 */
	@Command
	public void closeSession() {
		closeUserSession();
	}

	/**
	 * Refresh the current {@link RepositoryQuery}
	 */
	@Command
	@NotifyChange({ "repositoryQuery", "repositoryModel" })
	public void refreshRepositoryQuery() {
		discardChanges();
	}

	/**
	 * Deletes the selected {@link RepositoryQuery}
	 */
	@Command
	@NotifyChange({ "repositoryQuery", "repositoryModel" })
	public void deleteRepositoryQuery() {
		final RepositoryQuery selectedRepositoryQuery = this.repositoryModel.getSelectedRepositoryQuery();
		if (selectedRepositoryQuery != null) {
			Messagebox.show("Do you want to delete the query?", "Delete Query",
					new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
					new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
						switch (event.getName()) {
						case Messagebox.ON_OK:
							tm.runInTransaction(em -> {
								em.refresh(getCurrentUser(tm));
								em.remove(selectedRepositoryQuery.getTask());
								selectedRepositoryQuery.setUser(null);
							});
							this.repositoryModel.removeRepositoryQuery(selectedRepositoryQuery);
							this.repositoryModel.setSelectedRepositoryQuery(null);
							this.setRepositoryQuery(new RepositoryQuery());
							postNotifyChange(this, "repositoryQuery", "repositoryModel");

							break;
						case Messagebox.ON_CANCEL:
							this.setRepositoryQuery(selectedRepositoryQuery);
							break;
						default:
							this.repositoryModel.setSelectedRepositoryQuery(this.repositoryQuery);
						}
					}, singletonMap("width", "500"));
		}
	}

	/**
	 * If the {@link RepositoryQuery} clicked by an user is a
	 * {@link RepositoryQueryTreeNode}, sets the {@link RepositoryQuery} as
	 * selected
	 */
	@Command
	@NotifyChange({ "repositoryQuery", "validRepositoryQuery", "queryReadyToCheckResult" })
	public void selectRepositoryQuery() {
		final RepositoryQuery selectedRepositoryQuery = this.repositoryModel.getSelectedRepositoryQuery();
		if (selectedRepositoryQuery != null) {
			if (!this.repositoryQuery.equals(selectedRepositoryQuery)
					&& (isRepositoryQueryModified() || isRepositoryQueryTaskModified())) {
				Messagebox.show("Do you want to save?", "Save Query",
						new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL,
								Messagebox.Button.NO },
						new String[] { "Save & Continue", "Discard Changes", "Continue Editing" }, Messagebox.QUESTION,
						null, event -> {
							switch (event.getName()) {
							case Messagebox.ON_OK:
								persistRepositoryQuery();

								setRepositoryQuery(selectedRepositoryQuery);

								postNotifyChange(this, "repositoryQuery", "repositoryModel");
								break;
							case Messagebox.ON_CANCEL:
								discardChanges();

								setRepositoryQuery(selectedRepositoryQuery);

								postNotifyChange(this, "repositoryQuery", "repositoryModel");
								break;
							case Messagebox.ON_NO:
							default:
								this.repositoryModel.setSelectedRepositoryQuery(this.repositoryQuery);
							}
						}, singletonMap("width", "500"));
			} else {
				setRepositoryQuery(selectedRepositoryQuery);
			}
		}
	}

	/**
	 * Moves a {@link RepositoryQuery} from a repository to another
	 * 
	 * @param repositoryQuery
	 *            the {@link RepositoryQuery}
	 * @param repository
	 *            the {@link RepositoryQuery} new repository
	 */
	@Command
	@NotifyChange({ "repositoryQuery", "repositoryModel" })
	public void moveRepositoryQueryTo(@BindingParam("repositoryQuery") RepositoryQuery repositoryQuery,
			@BindingParam("repository") final String repository) {
		this.repositoryModel.moveRepositoryQueryToRepository(repositoryQuery, repository);
		// final boolean isNew = isNewRepositoryQuery();
		tm.runInTransaction(em -> {
			em.merge(repositoryQuery);
		});

		// if (isNew) {
		// tm.runInTransaction(em -> em.persist(repositoryQuery));
		// this.repositoryModel.addRepositoryQuery(repositoryQuery);
		// } else {
		// tm.runInTransaction(em -> em.merge(repositoryQuery));
		// this.repositoryModel.moveRepositoryQueryToRepository(getRepositoryQuery(),
		// getRepositoryQuery().getRepository());
		// }

		this.repositoryModel.setSelectedRepositoryQuery(this.repositoryQuery);
		this.uneditedRepositoryQuery = this.repositoryQuery.clone();
	}

	/**
	 * Discards all the changes done in the {@link RepositoryQuery}
	 */
	private void discardChanges() {

		if (!isNewRepositoryQuery()) {

			tm.runInTransaction(em -> {
				if (!em.contains(this.repositoryQuery)) {
					this.repositoryQuery = em.find(RepositoryQuery.class, this.repositoryQuery.getId());
				}

				if (!em.contains(this.repositoryQueryTask)) {
					this.repositoryQueryTask = em.find(Task.class, this.repositoryQuery.getTask().getId());
				}
				this.repositoryModel.removeRepositoryQuery(this.repositoryQuery);

				em.refresh(this.repositoryQueryTask);
				em.refresh(this.repositoryQuery);

				this.repositoryModel.addRepositoryQuery(this.repositoryQuery);

				this.uneditedRepositoryQuery = this.repositoryQuery.clone();
				this.uneditedRepositoryQueryTask = this.uneditedRepositoryQuery.getTask();

			});

			// tm.runInTransaction(em -> {
			// em.refresh(this.repositoryQueryTask);
			// em.refresh(this.repositoryQuery);
			//
			//
			// this.uneditedRepositoryQuery = this.repositoryQuery.clone();
			// this.uneditedRepositoryQueryTask =
			// this.uneditedRepositoryQuery.getTask();
			//
			// });
		} else {
			this.setRepositoryQuery(new RepositoryQuery());
		}
	}

	/**
	 * Creates or discard the changes of a new {@link RepositoryQuery}
	 */
	@Command
	@NotifyChange("repositoryQuery")
	public void newRepositoryQuery() {
		if (isRepositoryQueryModified() || isRepositoryQueryTaskModified()) {
			Messagebox.show("Do you want to save?", "Save Query",
					new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL, Messagebox.Button.NO },
					new String[] { "Save & Continue", "Discard Changes", "Continue Editing" }, Messagebox.QUESTION,
					null, event -> {
						switch (event.getName()) {
						case Messagebox.ON_OK:
							persistRepositoryQuery();
							setRepositoryQuery(new RepositoryQuery());

							postNotifyChange(this, "repositoryQuery", "repositoryModel");

							break;
						case Messagebox.ON_CANCEL:
							discardChanges();

							this.setRepositoryQuery(new RepositoryQuery());

							postNotifyChange(this, "repositoryQuery", "repositoryModel");
							break;
						case Messagebox.ON_NO:
						default:
						}
					}, singletonMap("width", "500"));
		} else {
			this.setRepositoryQuery(new RepositoryQuery());
		}
	}

	/**
	 * Persists the {@link RepositoryQuery} creation/changes
	 */
	@Command
	@NotifyChange({ "repositoryQuery", "repositories", "repositoryModel", "repositoryQueryTask" })
	public void persistRepositoryQuery() {

		final RepositoryQuery repositoryQuery = this.getRepositoryQuery();

		final String downloadDirectory = getCurrentUser(tm).getLogin() + File.separator
				+ repositoryQuery.getRepository() + File.separator;

		final boolean isNew = isNewRepositoryQuery();

		repositoryQuery.setDirectory(downloadDirectory);

		if (isNew) {
			repositoryQuery.setUser(getCurrentUser(tm));
			tm.runInTransaction(em -> em.persist(repositoryQuery));
			this.repositoryModel.addRepositoryQuery(repositoryQuery);
		} else {
			tm.runInTransaction(em -> em.merge(repositoryQuery));
			this.repositoryModel.moveRepositoryQueryToRepository(getRepositoryQuery(),
					getRepositoryQuery().getRepository());
		}

		final File newDirectory = new File(downloadDirectory);
		if (!newDirectory.exists()) {
			newDirectory.mkdirs();
		}

		this.repositoryModel.setSelectedRepositoryQuery(repositoryQuery);

		this.uneditedRepositoryQuery = repositoryQuery.clone();
		this.uneditedRepositoryQueryTask = this.uneditedRepositoryQuery.getTask();
	}

	@Command
	public void persistUserChanges() {
		final User user = this.getCurrentUser();
		tm.runInTransaction(em -> em.merge(user));
	}

	@Command
	public void checkRepositoryQueryResult() {
		final Task task = this.repositoryQueryTask;
		final String directoryPath = RepositoryManager.getRepositoryPath() + File.separator;
		// Scheduler.getSingleton().executeTask(directoryPath, task, true);
		Scheduler.getSingleton().executeTask(getRepositoryQueryResult(directoryPath), task);

		if (!this.runningRepositoryQuery.contains(this.repositoryQuery)) {
			this.runningRepositoryQuery.add(this.repositoryQuery);
		}
		postNotifyChange(this, "repositoryQuery", "runningRepositoryQuery");
	}

	//
	// @Command
	// @NotifyChange("runningRepositoryQuery")
	// public void launchRepositoryQuery() {
	// final Task task = this.repositoryQuery.getTask();
	// final String directoryPath = RepositoryManager.getRepositoryPath() +
	// File.separator;
	// Scheduler.getSingleton().scheduleTask(getRunnableQuery(directoryPath),
	// task);
	// this.repositoryQuery.setRunning(true);
	// tm.runInTransaction(em -> em.merge(this.repositoryQuery));
	//
	// if (!this.runningRepositoryQuery.contains(this.repositoryQuery)) {
	// this.runningRepositoryQuery.add(this.repositoryQuery);
	// }
	// }

	@Command
	public void launchExecution(@BindingParam("current") final RepositoryQuery repositoryQuery) {
		final Task task = repositoryQuery.getTask();
		final String directoryPath = RepositoryManager.getRepositoryPath() + File.separator;
		Scheduler.getSingleton().scheduleTask(getRunnableQuery(directoryPath), task);
		repositoryQuery.setRunning(true);
		tm.runInTransaction(em -> em.merge(repositoryQuery));
		postNotifyChange(this, "repositoryQuery", "runningRepositoryQuery");
	}

	@Command
	public void abortOrRemoveExecution(@BindingParam("current") final RepositoryQuery repositoryQuery) {
		final Task task = repositoryQuery.getTask();
		if (repositoryQuery.isRunning()) {
			Messagebox.show("Do you want to abort the current execution?", "Abort execution",
					Messagebox.OK | Messagebox.NO, Messagebox.EXCLAMATION, event -> {
						if (event.getName().equals(Messagebox.ON_OK)) {
							abortRepositoryQueryExecution(repositoryQuery, task);

							postNotifyChange(this, "runningRepositoryQuery");
						}
					});
		} else {
			Messagebox.show("Do you want to remove the query?", "Abort execution", Messagebox.OK | Messagebox.NO,
					Messagebox.EXCLAMATION, event -> {
						if (event.getName().equals(Messagebox.ON_OK)) {
							removeRepositoryQueryExecution(repositoryQuery);
							postNotifyChange(this, "runningRepositoryQuery");
						}
					});
		}
	}

	@Command
	public void abortAllExecutions() {
		Messagebox.show("Are you sure you want to abort all executions?", "Abort executions",
				Messagebox.OK | Messagebox.NO, Messagebox.EXCLAMATION, event -> {
					if (event.getName().equals(Messagebox.ON_OK)) {
						for (RepositoryQuery repositoryQuery : this.runningRepositoryQuery) {
							final Task task = repositoryQuery.getTask();
							if (repositoryQuery.isRunning()) {
								abortRepositoryQueryExecution(repositoryQuery, task);
							}
						}
						postNotifyChange(this, "runningRepositoryQuery");
					}
				});
	}

	private void abortRepositoryQueryExecution(final RepositoryQuery repositoryQuery, final Task task) {
		Scheduler.getSingleton().removeTask(task);

		repositoryQuery.setRunning(false);
		tm.runInTransaction(em -> em.merge(repositoryQuery));
	}

	private void removeRepositoryQueryExecution(final RepositoryQuery repositoryQuery) {
		repositoryQuery.setChecked(false);
		tm.runInTransaction(em -> em.merge(repositoryQuery));
		if (this.runningRepositoryQuery.contains(repositoryQuery)) {
			this.runningRepositoryQuery.remove(repositoryQuery);
		}
	}

	public Runnable getRunnableQuery(final String directoryPath) {
		final int startsDownloadFrom = 0;
		return () -> {
			ScopusDownloader scopusDownloader = new ScopusDownloader();
			PubMedDownloader pubmedDownloader = new PubMedDownloader();
			boolean scopusReady = false;
			boolean pubmedReady = false;

			final String query = repositoryQuery.getQuery().replace(" ", "+");

			final String scopusApiKey = repositoryQuery.getUser().getApiKey();

			if (repositoryQuery.isScopus() && isUserApiKeyValid() && repositoryQuery.getScopusDownloadTo() != 0
					&& repositoryQuery.getScopusDownloadTo() != Integer.MAX_VALUE) {
				scopusDownloader = new ScopusDownloader(query, scopusApiKey,
						directoryPath + repositoryQuery.getDirectory());
				scopusReady = true;
			}

			if (repositoryQuery.isPubmed() && repositoryQuery.getPubmedDownloadTo() != 0
					&& repositoryQuery.getPubmedDownloadTo() != Integer.MAX_VALUE) {
				pubmedDownloader = new PubMedDownloader(query, directoryPath + repositoryQuery.getDirectory());
				pubmedReady = true;
			}

			if (repositoryQuery.isFulltextPaper()) {
				final boolean directoryType = Boolean.valueOf(repositoryQuery.getGroupBy());
				if (repositoryQuery.isScopus() && scopusReady) {
					scopusDownloader.downloadPapers(true, repositoryQuery.isPdfToText(), repositoryQuery.isKeepPdf(),
							directoryType, startsDownloadFrom, repositoryQuery.getScopusDownloadTo());
				}

				if (repositoryQuery.isPubmed() && pubmedReady) {
					pubmedDownloader.downloadPapers(true, repositoryQuery.isPdfToText(), repositoryQuery.isKeepPdf(),
							directoryType, startsDownloadFrom, repositoryQuery.getPubmedDownloadTo());
				}
			}

			if (repositoryQuery.isAbstractPaper()) {
				final boolean directoryType = Boolean.valueOf(repositoryQuery.getGroupBy());
				if (repositoryQuery.isScopus() && scopusReady) {
					scopusDownloader.downloadPapers(false, repositoryQuery.isPdfToText(), repositoryQuery.isKeepPdf(),
							directoryType, startsDownloadFrom, repositoryQuery.getScopusDownloadTo());
				}
				if (repositoryQuery.isPubmed() && pubmedReady) {
					pubmedDownloader.downloadPapers(false, repositoryQuery.isPdfToText(), repositoryQuery.isKeepPdf(),
							directoryType, startsDownloadFrom, repositoryQuery.getPubmedDownloadTo());
				}

			}
		};
	}

	public Runnable getRepositoryQueryResult(final String directoryPath) {
		return () -> {
			final TransactionManager tm = new CleanEntityManagerTransactionManager();
			ScopusDownloader scopusDownloader = new ScopusDownloader();
			PubMedDownloader pubmedDownloader = new PubMedDownloader();

			int scopusResult = 0;
			int pubmedResult = 0;
			int scopusDownloadTo = 0;
			int pubmedDownloadTo = 0;

			final String query = repositoryQuery.getQuery().replace(" ", "+");

			final String scopusApiKey = repositoryQuery.getUser().getApiKey();

			if (repositoryQuery.isScopus()) {
				scopusDownloader = new ScopusDownloader(query, scopusApiKey,
						directoryPath + repositoryQuery.getDirectory());

				if (repositoryQuery.getScopusDownloadTo() == Integer.MAX_VALUE
						|| repositoryQuery.getScopusDownloadTo() == 0) {
					scopusResult = scopusDownloader.getResultSize();
					System.out.println("Scopus real result = " + scopusResult);
					if (scopusResult != 0) {
						if (scopusResult > 6000) {
							scopusDownloadTo = 6000;
						}

						// limit to 3 to test
						scopusDownloadTo = 3;
						repositoryQuery.setScopusDownloadTo(scopusDownloadTo);
					}
				}
			}

			if (repositoryQuery.isPubmed()) {
				pubmedDownloader = new PubMedDownloader(query, directoryPath + repositoryQuery.getDirectory());
				if (repositoryQuery.getPubmedDownloadTo() == Integer.MAX_VALUE
						|| repositoryQuery.getPubmedDownloadTo() == 0) {
					pubmedResult = pubmedDownloader.getResultSize();
					System.out.println("PubMed real result = " + pubmedResult);
					if (pubmedResult != 0) {
						// limit to 3 to test
						pubmedDownloadTo = 3;
						repositoryQuery.setPubmedDownloadTo(pubmedDownloadTo);
					}
				}
			}
			repositoryQuery.setChecked(true);
			tm.runInTransaction(em -> {
				em.merge(repositoryQuery);
			});
			setRepositoryQuery(repositoryQuery);
		};
	}

	// @GlobalCommand
	// public void executeRobot(@BindingParam("input") final String input) {
	// final String[] inputs = (input == null || input.trim().isEmpty()) ? new
	// String[0] : input.split("\n");
	// final RobotExecution robotExecution = new
	// RobotExecution(getCurrentUser(tm).getLogin(),
	// repositoryQuery.getName(), "");
	//
	// ExecutionEngine.getSingleton()
	// .execute(new RobotExecutionTask(getCurrentUser(tm), repositoryQuery,
	// robotExecution, inputs));
	// }
	//
	// @GlobalCommand(MainViewModel.GC_UPDATE_EXECUTIONS)
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
	//
	// @Command
	// public void deleteResult(@BindingParam("currentResult") final
	// RobotExecution robotExecution) {
	// Messagebox.show("Do you want to delete the result?", "Delete Result",
	// new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL
	// },
	// new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event ->
	// {
	// switch (event.getName()) {
	// case Messagebox.ON_OK:
	// tm.runInTransaction(em -> {
	// final RobotExecution robotExecutionDelete = em.find(RobotExecution.class,
	// robotExecution.getId());
	// if (this.robotExecutionList.contains(robotExecutionDelete)) {
	// em.remove(robotExecutionDelete);
	//
	// this.robotExecutionList.remove(robotExecutionDelete);
	// this.setRobotExecution(new RobotExecution());
	// }
	// });
	// postNotifyChange(this, "robotExecutionList", "robotExecution");
	//
	// break;
	// case Messagebox.ON_CANCEL:
	// break;
	// default:
	// }
	// }, singletonMap("width", "500"));
	// }

}
