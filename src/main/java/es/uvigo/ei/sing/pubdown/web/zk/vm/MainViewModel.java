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
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.pubdown.execution.ExecutionEngine;
import es.uvigo.ei.sing.pubdown.execution.GlobalEvents;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.pubmed.PubMedDownloader;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.scopus.ScopusDownloader;
import es.uvigo.ei.sing.pubdown.web.entities.GlobalConfiguration;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.entities.RobotExecution;
import es.uvigo.ei.sing.pubdown.web.entities.User;
import es.uvigo.ei.sing.pubdown.web.zk.tree.RepositoryQueryTreeNode;
import es.uvigo.ei.sing.pubdown.web.zk.tree.RepositoryTreeModel;
import es.uvigo.ei.sing.pubdown.web.zk.util.DesktopTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.ViewModelFunctions;
import es.uvigo.ei.sing.pubdown.web.zk.vm.robot.RobotExecutionTask;

/**
 * ViewModel to manage the user panel
 */
public class MainViewModel extends ViewModelFunctions {
	private final TransactionManager tm = new DesktopTransactionManager();

	private static final String GC_UPDATE_EXECUTIONS = "updateExecutions";

	static {
		GlobalEvents.fullActionRegisterGlobalCommand(GlobalEvents.ACTION_SCHEDULED, MainViewModel.GC_UPDATE_EXECUTIONS);
		GlobalEvents.fullActionRegisterGlobalCommand(GlobalEvents.ACTION_STARTED, MainViewModel.GC_UPDATE_EXECUTIONS);
		GlobalEvents.fullActionRegisterGlobalCommand(GlobalEvents.ACTION_FINISHED, MainViewModel.GC_UPDATE_EXECUTIONS);
		GlobalEvents.fullActionRegisterGlobalCommand(GlobalEvents.ACTION_ABORTED, MainViewModel.GC_UPDATE_EXECUTIONS);
	}

	// private String repositoryPath = "/home/lab33/pubdownTest";

	private final static String[] NAVIGATION_PROPERTIES = new String[] { "robotExecutionList" };

	private RepositoryQuery repositoryQuery;
	private RepositoryQuery uneditedRepositoryQuery;
	private RepositoryTreeModel repositoryModel;
	private List<RobotExecution> robotExecutionList;
	private RobotExecution robotExecution;
	private String repositoryPath;

	/**
	 * Initializes variables
	 */
	@Init
	public void init() {
		this.repositoryModel = new RepositoryTreeModel(getCurrentUser(tm).getRepositoriesQueryByRepository());
		this.setRepositoryQuery(new RepositoryQuery());
		this.robotExecutionList = tm.get(
				em -> em.createQuery("SELECT u FROM RobotExecution u WHERE u.userLogin = :user", RobotExecution.class)
						.setParameter("user", getCurrentUser(tm).getLogin()).getResultList());
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

		this.uneditedRepositoryQuery = this.repositoryQuery.clone();

		this.repositoryModel.setSelectedRepositoryQuery(repositoryQuery);
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
//		return tm.get(em -> 
//		em.createQuery("SELECT g FROM GlobalConfiguration g WHERE g.configurationKey = :path",
//						GlobalConfiguration.class)
//				.setParameter("path", "repositoryPath").getSingleResult().getConfigurationValue());
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

	public List<RobotExecution> getRobotExecutionList() {
		return this.robotExecutionList;
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

	/**
	 * Discards all the changes done in the {@link RepositoryQuery}
	 */
	private void discardChanges() {
		if (!isNewRepositoryQuery()) {
			tm.runInTransaction(em -> {
				em.refresh(this.repositoryQuery);

				this.uneditedRepositoryQuery = this.repositoryQuery.clone();
			});
		} else {
			this.setRepositoryQuery(new RepositoryQuery());
		}
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

	/**
	 * Checks if some variables are valid. Method linked with
	 * {@link MainViewModel#isValidRepositoryQuery()}
	 */
	@Command
	@NotifyChange("validRepositoryQuery")
	public void checkData() {
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
								selectedRepositoryQuery.setUser(null);
							});
							this.repositoryModel.removeRepositoryQuery(selectedRepositoryQuery);
							this.repositoryModel.setSelectedRepositoryQuery(null);
							this.setRepositoryQuery(new RepositoryQuery());
							postNotifyChange(this, "repositoryQuery", "repositoryModel");

							break;
						case Messagebox.ON_CANCEL:
							setRepositoryQuery(selectedRepositoryQuery);
							break;
						default:
							this.repositoryModel.setSelectedRepositoryQuery(this.repositoryQuery);
						}
					}, singletonMap("width", "500"));
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
	public void moveRepositoryQueryTo(@BindingParam("repositoryQuery") final RepositoryQuery repositoryQuery,
			@BindingParam("repository") final String repository) {
		this.repositoryModel.moveRepositoryQueryToRepository(repositoryQuery, repository);
		tm.runInTransaction(em -> {
			em.persist(repositoryQuery);
		});
		this.repositoryModel.setSelectedRepositoryQuery(this.repositoryQuery);
		this.uneditedRepositoryQuery = this.repositoryQuery.clone();
	}

	// @Command
	// @NotifyChange({ "repositoryModel" })
	// public void newRepository() {
	// Executions.createComponents("repositoryForm.zul", null, null);
	// }
	//
	@GlobalCommand
	public void addRepository(@BindingParam("repository") final String repositoryName) {
		this.repositoryModel.addRepository(repositoryName);
//		final File newDirectory = new File(
//				getRepositoryPath() + File.separator + getCurrentUser(tm).getLogin() + File.separator + repositoryName);
		final File newDirectory = new File(getCurrentUser(tm).getLogin() + File.separator + repositoryName);
		if (!newDirectory.exists()) {
			newDirectory.mkdirs();
		}
		postNotifyChange(this, "repositoryModel");
	}

	/**
	 * If the {@link RepositoryQuery} clicked by an user is a
	 * {@link RepositoryQueryTreeNode}, sets the {@link RepositoryQuery} as
	 * selected
	 */
	@Command
	@NotifyChange("repositoryQuery")
	public void repositoryQuerySelected() {
		final RepositoryQuery selectedRepositoryQuery = this.repositoryModel.getSelectedRepositoryQuery();
		if (selectedRepositoryQuery != null) {
			if (!this.repositoryQuery.equals(selectedRepositoryQuery) && isRepositoryQueryModified()) {
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

								postNotifyChange(this, "repositoryQuery");
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
	 * Closes the current {@link User} session
	 */
	@Command
	public void closeSession() {
		closeUserSession();
	}

	/**
	 * Creates or discard the changes of a new {@link RepositoryQuery}
	 */
	@Command
	@NotifyChange("repositoryQuery")
	public void newRepositoryQuery() {
		if (this.isRepositoryQueryModified()) {
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
	@NotifyChange({ "repositoryQuery", "repositories", "repositoryModel" })
	public void persistRepositoryQuery() {
		final RepositoryQuery repositoryQuery = this.getRepositoryQuery();

//		final String downloadDirectory = getRepositoryPath() + File.separator + getCurrentUser(tm).getLogin()
//				+ File.separator + repositoryQuery.getRepository() + File.separator;
		
		final String downloadDirectory = getCurrentUser(tm).getLogin() + File.separator
				+ repositoryQuery.getRepository() + File.separator;

		final boolean isNew = isNewRepositoryQuery();

		if (isNew) {
			repositoryQuery.setUser(getCurrentUser(tm));
		}

		repositoryQuery.setDirectory(downloadDirectory);

		tm.runInTransaction(em -> {
			em.persist(repositoryQuery);
		});

		if (isNew) {
			this.repositoryModel.addRepositoryQuery(repositoryQuery);
		} else {
			this.repositoryModel.moveRepositoryQueryToRepository(getRepositoryQuery(),
					getRepositoryQuery().getRepository());
		}

		final File newDirectory = new File(downloadDirectory);
		if (!newDirectory.exists()) {
			newDirectory.mkdirs();
		}

		this.repositoryModel.setSelectedRepositoryQuery(repositoryQuery);

		this.uneditedRepositoryQuery = repositoryQuery.clone();
	}

	/*
	 * 
	 * 
	 * 
	 * EXECUTION BLOCK
	 * 
	 * 
	 * 
	 */
	@Command
	public void launchRepositoryQuery() {
		final RepositoryQuery repositoryQuery = this.getRepositoryQuery();
		final String query = repositoryQuery.getQuery().replace(" ", "+");
		
		final String scopusApiKey = getCurrentUser(tm).getApiKey();
		final String directoryPath = getRepositoryPath() + File.separator;
		
		final ScopusDownloader scopusDownloader = new ScopusDownloader(query, scopusApiKey,
				directoryPath + repositoryQuery.getDirectory());
		
		final PubMedDownloader pubmedDownloader = new PubMedDownloader(query,
				directoryPath + repositoryQuery.getDirectory());
		
		System.out.println("Directory Path: "+directoryPath);
		System.out.println("Pubmed dir: "+pubmedDownloader.getDirectory());
		System.out.println("Scopus dir: "+scopusDownloader.getDirectory());
		
		final int downloadFrom = 0;

		int scopusDownloadTo = 0;
		int pubmedDownloadTo = 0;

		if (repositoryQuery.getScopusDownloadTo() == Integer.MAX_VALUE) {
			int scopusResult = scopusDownloader.getResultSize();
			if (scopusResult != 0) {
				System.out.println("SCOPUS DOWNLOAD TO : " + scopusResult);

				if (scopusResult > 6000) {
					scopusDownloadTo = 6000;
				}

				scopusDownloadTo = 3;
				repositoryQuery.setScopusDownloadTo(scopusDownloadTo);
				tm.runInTransaction(em -> {
					em.persist(repositoryQuery);
				});
			}
		}

		if (repositoryQuery.getPubmedDownloadTo() == Integer.MAX_VALUE) {
			int pubmedResult = pubmedDownloader.getResultSize();
			if (pubmedResult != 0) {
				System.out.println("PUBMED DOWNLOAD TO : " + pubmedResult);

				pubmedDownloadTo = 3;
				repositoryQuery.setPubmedDownloadTo(pubmedDownloadTo);
				tm.runInTransaction(em -> {
					em.persist(repositoryQuery);
				});
			}
		}
		if (repositoryQuery.isFulltextPaper()) {
			final boolean directoryType = Boolean.valueOf(repositoryQuery.getGroupBy());
			if (repositoryQuery.isScopus() && repositoryQuery.getScopusDownloadTo() != Integer.MAX_VALUE) {
				System.out.println("directory type: " + directoryType);
				scopusDownloader.downloadPapers(true, repositoryQuery.isPdfToText(), repositoryQuery.isKeepPdf(),
						directoryType, downloadFrom, repositoryQuery.getScopusDownloadTo());
			}

			if (repositoryQuery.isPubmed() && repositoryQuery.getPubmedDownloadTo() != Integer.MAX_VALUE) {
				System.out.println("directory type: " + directoryType);
				pubmedDownloader.downloadPapers(true, repositoryQuery.isPdfToText(), repositoryQuery.isKeepPdf(),
						directoryType, downloadFrom, repositoryQuery.getPubmedDownloadTo());
			}
		}

		if (repositoryQuery.isAbstractPaper()) {
			final boolean directoryType = Boolean.valueOf(repositoryQuery.getGroupBy());
			if (repositoryQuery.isScopus() && repositoryQuery.getScopusDownloadTo() != Integer.MAX_VALUE) {
				System.out.println("directory type: " + directoryType);
				scopusDownloader.downloadPapers(false, repositoryQuery.isPdfToText(), repositoryQuery.isKeepPdf(),
						directoryType, downloadFrom, repositoryQuery.getScopusDownloadTo());
			}

			if (repositoryQuery.isPubmed() && repositoryQuery.getPubmedDownloadTo() != Integer.MAX_VALUE) {
				System.out.println("directory type: " + directoryType);
				pubmedDownloader.downloadPapers(false, repositoryQuery.isPdfToText(), repositoryQuery.isKeepPdf(),
						directoryType, downloadFrom, repositoryQuery.getPubmedDownloadTo());
			}
		}

	}

	@GlobalCommand
	public void executeRobot(@BindingParam("input") final String input) {
		final String[] inputs = (input == null || input.trim().isEmpty()) ? new String[0] : input.split("\n");
		final RobotExecution robotExecution = new RobotExecution(getCurrentUser(tm).getLogin(),
				repositoryQuery.getName(), "");

		ExecutionEngine.getSingleton()
				.execute(new RobotExecutionTask(getCurrentUser(tm), repositoryQuery, robotExecution, inputs));
	}

	@GlobalCommand(MainViewModel.GC_UPDATE_EXECUTIONS)
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
								this.setRobotExecution(new RobotExecution());
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
		final String userId = getCurrentUser(tm).getLogin();

		Messagebox.show("Are you sure you want to abort the current execution?", "Abort execution",
				Messagebox.OK | Messagebox.NO, Messagebox.EXCLAMATION, event -> {
					if (event.getName().equals(Messagebox.ON_OK)) {
						ExecutionEngine.getSingleton().cancelUserTask(userId, robotExecution);
					}
				});
	}

	@Command
	public void abortAllExecutions() {
		final String userId = getCurrentUser(tm).getLogin();

		Messagebox.show("Are you sure you want to abort all executions?", "Abort executions",
				Messagebox.OK | Messagebox.NO, Messagebox.EXCLAMATION, event -> {
					if (event.getName().equals(Messagebox.ON_OK)) {
						ExecutionEngine.getSingleton().cancelUserTasks(userId);
					}
				});
	}
}
