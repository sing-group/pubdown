package es.uvigo.ei.sing.pubdown.web.zk.vm;

import static es.uvigo.ei.sing.pubdown.execution.GlobalEvents.EVENT_REFRESH_DATA;
import static es.uvigo.ei.sing.pubdown.execution.GlobalEvents.EVENT_REPOSITORY_QUERY;
import static es.uvigo.ei.sing.pubdown.util.Checks.isEmpty;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zul.Messagebox;

import es.uvigo.ei.sing.pubdown.execution.EventQueueUtils;
import es.uvigo.ei.sing.pubdown.execution.EventRepositoryQuery;
import es.uvigo.ei.sing.pubdown.execution.ExecutionEngine;
import es.uvigo.ei.sing.pubdown.execution.GlobalEvents;
import es.uvigo.ei.sing.pubdown.execution.RepositoryQueryScheduled;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.RepositoryManager;
import es.uvigo.ei.sing.pubdown.web.entities.GlobalConfiguration;
import es.uvigo.ei.sing.pubdown.web.entities.Repository;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQueryTask;
import es.uvigo.ei.sing.pubdown.web.entities.User;
import es.uvigo.ei.sing.pubdown.web.zk.util.DesktopTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.ViewModelUtils;

/**
 * ViewModel to manage the user panel
 */
public class MainViewModel extends ViewModelUtils {
	private final TransactionManager tm = new DesktopTransactionManager();

	private static final String DOWNLOAD_FILE_EXTENSION = ".zip";
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	private static final String GC_UPDATE_EXECUTIONS = "updateExecutions";
	private static final String GC_REFRESH_DATA = "resfreshData";

	static {
		GlobalEvents.fullStatesRegisterGlobalCommand(EVENT_REPOSITORY_QUERY, GC_UPDATE_EXECUTIONS);
		GlobalEvents.fullStatesRegisterGlobalCommand(EVENT_REFRESH_DATA, GC_REFRESH_DATA);
	}

	private User currentUser;
	private Repository repository;
	private Repository uneditedRepository;
	private RepositoryQuery repositoryQuery;
	private RepositoryQuery uneditedRepositoryQuery;
	private RepositoryQueryTask repositoryQueryTask;
	private RepositoryQueryTask uneditedRepositoryQueryTask;

	private List<Repository> repositories;
	private List<RepositoryQuery> queries;
	private List<RepositoryQuery> repositoryQueries;

	private String repositoryPath;
	private String repositoryQueryFilterByRepositoryName;
	private String repositoryQueryFilterByName;

	/**
	 * Initializes variables
	 */
	@Init
	public void init() {
		this.currentUser = getCurrentUser(tm);

		this.currentUser.setLogged(true);
		tm.runInTransaction(em -> em.merge(this.currentUser));

		this.repositories = getRepositories();

		this.queries = getAllQueries();

		sortQueries(this.queries);

		this.repositoryQueries = new LinkedList<>();

		this.setRepository(new Repository());

	}

	public List<Repository> getRepositories() {
		return tm.getInTransaction(em -> em
				.createQuery("SELECT r FROM Repository r WHERE r.user = :user ORDER BY r.name ASC", Repository.class)
				.setParameter("user", getCurrentUser(tm)).getResultList());
	}

	public void setRepositories(final List<Repository> repositories) {
		this.repositories = repositories;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(final Repository repository) {
		this.repository = repository;

		this.uneditedRepository = this.repository.clone();
	}

	public Repository getUneditedRepository() {
		return uneditedRepository;
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
	public void setRepositoryQuery(final RepositoryQuery repositoryQuery) {

		this.repositoryQuery = repositoryQuery;

		this.repositoryQueryTask = repositoryQuery.getTask();

		this.uneditedRepositoryQuery = this.repositoryQuery.clone();

		this.uneditedRepositoryQueryTask = this.repositoryQuery.getTask();
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

	public List<RepositoryQuery> getQueries() {
		sortQueries(queries);
		return queries;
	}

	public List<RepositoryQuery> getAllQueries() {
		final List<Repository> repositories = getRepositories();
		final List<RepositoryQuery> queries = new LinkedList<>();

		for (final Repository repository : repositories) {
			final List<RepositoryQuery> repoQueries = tm.getInTransaction(
					em -> em.createQuery("SELECT rq FROM RepositoryQuery rq WHERE rq.repository = :repository",
							RepositoryQuery.class).setParameter("repository", repository).getResultList());
			queries.addAll(repoQueries);
		}
		return queries;
	}

	@DependsOn("repository")
	public List<RepositoryQuery> getRepositoryQueries() {
		this.repositoryQueries = this.queries.stream()
				.filter(repositoryQuery -> repositoryQuery.getRepository().getName().equals(this.repository.getName()))
				.collect(toList());
		return repositoryQueries;
	}

	public User getCurrentUser() {
		return currentUser;
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

	public RepositoryQueryTask getRepositoryQueryTask() {
		return repositoryQueryTask;
	}

	public RepositoryQueryTask getUneditedRepositoryQueryTask() {
		return uneditedRepositoryQueryTask;
	}

	public String getRepositoryQueryFilterByRepositoryName() {
		return repositoryQueryFilterByRepositoryName;
	}

	public void setRepositoryQueryFilterByRepositoryName(String repositoryQueryFilterByRepositoryName) {
		this.repositoryQueryFilterByRepositoryName = repositoryQueryFilterByRepositoryName;
	}

	public String getRepositoryQueryFilterByName() {
		return repositoryQueryFilterByName;
	}

	public void setRepositoryQueryFilterByName(String repositoryQueryFilterByName) {
		this.repositoryQueryFilterByName = repositoryQueryFilterByName;
	}

	@DependsOn("repository")
	public List<String> getRepositoryPapers() {
		return readRepositoryPapers();
	}

	@DependsOn("repository")
	public boolean isRepositoryReadyToDownload() {
		return this.repository.getNumberOfPapers() > 0;
	}

	private boolean isNewRepository() {
		return this.repository.getId() == null;
	}

	public boolean isRepositoryModified() {
		return this.repository.compareTo(this.uneditedRepository) != 0;
	}

	public boolean isRepositoryQueryModified() {
		return this.repositoryQuery.compareTo(this.uneditedRepositoryQuery) != 0;
	}

	public boolean isUserApiKeyValid(final User user) {
		return !(user.getApiKey() == null || user.getApiKey().isEmpty());
	}

	@Command
	@NotifyChange("userApiKeyValid")
	public void checkUserApiKey() {
	}

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

	public Validator getPathValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String path = (String) ctx.getProperty().getValue();

				if (isEmpty(path)) {
					addInvalidMessage(ctx, "Path can't be empty");
				}
			}
		};
	}

	public boolean isValidRepository() {
		return !isEmpty(this.repository.getName()) && !isEmpty(this.repository.getPath());
	}

	@Command
	@NotifyChange("validRepository")
	public void checkRepository() {
	}

	/**
	 * Closes the current {@link User} session
	 */
	@Command
	public void closeSession() {
		this.currentUser.setLogged(false);
		tm.runInTransaction(em -> em.merge(this.currentUser));
		closeUserSession();
	}

	@Command
	public void persistUserChanges() {
		final User user = this.getCurrentUser();
		tm.runInTransaction(em -> em.merge(user));

		publishRefreshData("users");
	}

	@Command
	public void downloadPapers() {
		final String suggestedDownloadName = this.repository.getName() + DOWNLOAD_FILE_EXTENSION;

		final String basePath = RepositoryManager.getRepositoryPath() + File.separator;
		final String userLogin = this.repository.getUser().getLogin() + File.separator;
		final String repositoryPath = this.repository.getPath() + File.separator;

		final String finalPath = basePath + userLogin + repositoryPath;

		try {
			RepositoryManager.zipDirectory(suggestedDownloadName, finalPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void discardRepositoryChanges() {
		if (!isNewRepository()) {
			tm.runInTransaction(em -> {
				if (!em.contains(this.repository)) {
					this.repository = em.find(Repository.class, this.repository.getId());
				}

				em.refresh(this.repository);
			});
			setRepository(this.repository);

			this.queries = getAllQueries();

			this.repositoryQueries = getRepositoryQueries();

			postNotifyChange(this, "repository", "repositories", "queries", "repositoryQueries");

		} else {
			this.setRepository(new Repository());
		}
	}

	@Command
	public void selectRepository(@BindingParam("current") Repository repository) {
		final Repository selectedRepository = repository;
		if (selectedRepository != null) {
			if (!this.repository.equals(selectedRepository) && isRepositoryModified()) {
				Messagebox.show("Do you want to save?", "Save Repository",
						new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL,
								Messagebox.Button.NO },
						new String[] { "Save & Continue", "Discard Changes", "Continue Editing" }, Messagebox.QUESTION,
						null, event -> {
							switch (event.getName()) {
							case Messagebox.ON_OK:
								persistRepository();

								setRepository(selectedRepository);

								break;
							case Messagebox.ON_CANCEL:
								discardRepositoryChanges();

								setRepository(selectedRepository);

								break;
							case Messagebox.ON_NO:
								setRepository(this.repository);

								postNotifyChange(this, "repository");
							default:
							}
						}, singletonMap("width", "500"));
			} else {
				setRepository(selectedRepository);
				postNotifyChange(this, "repository");
			}
		}
	}

	@Command
	public void newRepository() {
		if (isRepositoryModified()) {
			Messagebox.show("Do you want to save?", "Save Repository",
					new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL, Messagebox.Button.NO },
					new String[] { "Save & Continue", "Discard Changes", "Continue Editing" }, Messagebox.QUESTION,
					null, event -> {
						switch (event.getName()) {
						case Messagebox.ON_OK:
							persistRepository();

							setRepository(this.repository);
							postNotifyChange(this, "repository", "repositories", "queries", "repositoryQueries");

							break;
						case Messagebox.ON_CANCEL:
							discardRepositoryChanges();

							setRepository(new Repository());

							postNotifyChange(this, "repository", "repositories", "queries", "repositoryQueries");
							break;
						case Messagebox.ON_NO:
						default:
						}
					}, singletonMap("width", "500"));
		} else {
			this.setRepository(new Repository());
			this.repositoryQueries = new LinkedList<>();
			postNotifyChange(this, "repository", "repositories", "queries", "repositoryQueries");
		}
	}

	@Command
	public void removeRepository() {
		findRepository(this.repository);
		final Repository selectedRepository = this.repository;
		if (selectedRepository != null) {
			Messagebox.show("Do you want to delete the repository?", "Delete Repository",
					new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
					new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
						switch (event.getName()) {
						case Messagebox.ON_OK:
							for (RepositoryQuery repositoryQuery : selectedRepository.getRepositoryQueries()) {
								if (repositoryQuery.isRunning()) {
									final RepositoryQueryScheduled repositoryQueryScheduled = new RepositoryQueryScheduled(
											repositoryQuery);
									stopRepositoryQueryExecution(repositoryQueryScheduled);
								}
							}
							tm.runInTransaction(em -> {
								em.refresh(getCurrentUser(tm));

								selectedRepository.setUser(null);
							});

							this.setRepository(new Repository());

							this.repositories.remove(selectedRepository);

							this.repositories = getRepositories();
							this.queries = getAllQueries();

							postNotifyChange(this, "repository", "repositories", "queries", "repositoryQueries");

							publishRefreshData("repositories");

							break;
						case Messagebox.ON_CANCEL:
						default:
							this.setRepository(selectedRepository);
							break;
						}
					}, singletonMap("width", "500"));
		}
	}

	@GlobalCommand
	public void updateRepository(@BindingParam("repository") final Repository repository) {
		final int indexOf = this.repositories.indexOf(repository);
		this.repositories.remove(indexOf);
		this.repositories.add(indexOf, repository);
		postNotifyChange(this, "repository", "repositories", "queries", "repositoryQueries");

		publishRefreshData("repositories");
	}

	@Command
	public void persistRepository() {
		final Repository repository = this.repository;

		if (repository.getPath().charAt(0) == File.separatorChar) {
			repository.setPath(repository.getPath().substring(1));
		}

		if (repository.getPath().charAt(repository.getPath().length() - 1) == File.separatorChar) {
			repository.setPath(repository.getPath().substring(0, repository.getPath().length() - 1));
		}

		final boolean isNew = isNewRepository();

		if (isNew) {
			repository.setUser(getCurrentUser(tm));
			tm.runInTransaction(em -> em.persist(repository));
			this.repositories.add(repository);

		} else {
			tm.runInTransaction(em -> em.merge(repository));
		}

		this.setRepository(repository);

		this.repositories = getRepositories();
		this.queries = getAllQueries();

		postNotifyChange(this, "repository", "repositories", "queries", "repositoryQueries");

		publishRefreshData("repositories");
	}

	@Command
	public void newRepositoryQuery() {
		Executions.createComponents("repositoryQueryForm.zul", null,
				singletonMap("repositoryQuery", new RepositoryQuery()));
	}

	@Command
	public void editRepositoryQuery(@BindingParam("current") RepositoryQuery repositoryQuery) {
		findRepositoryQuery(repositoryQuery);

		Executions.createComponents("repositoryQueryForm.zul", null,
				singletonMap("repositoryQuery", this.repositoryQuery));
	}

	@GlobalCommand
	public void addRepositoryQuery(@BindingParam("repositoryQuery") final RepositoryQuery repositoryQuery) {

		this.queries.add(repositoryQuery);

		sortQueries(this.queries);

		postNotifyChange(this, "repositoryQuery", "queries", "repositoryQueries");

		checkRepositoryQueryResult(repositoryQuery);
	}

	@GlobalCommand
	public void updateRepositoryQuery(@BindingParam("repositoryQuery") final RepositoryQuery repositoryQuery) {
		findRepositoryQuery(repositoryQuery);
		final int indexOf = this.queries.indexOf(this.repositoryQuery);
		this.queries.remove(indexOf);
		this.setRepositoryQuery(this.repositoryQuery);
		this.queries.add(indexOf, this.repositoryQuery);

		sortQueries(this.queries);

		postNotifyChange(this, "queries", "repositoryQueries");
		checkRepositoryQueryResult(this.repositoryQuery);
	}

	@Command
	public void removeRepositoryQuery(@BindingParam("current") final RepositoryQuery repositoryQuery) {
		findRepositoryQuery(repositoryQuery);
		if (this.repositoryQuery != null) {
			Messagebox.show("Do you want to delete the query?", "Delete Query",
					new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
					new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
						switch (event.getName()) {
						case Messagebox.ON_OK:

							tm.runInTransaction(em -> {
								this.repositoryQuery.setRepository(null);
							});
							this.queries.remove(this.repositoryQuery);

							this.queries = getAllQueries();

							postNotifyChange(this, "queries", "repositoryQueries");

							publishRefreshData("queries");

							break;
						case Messagebox.ON_CANCEL:
						default:
							break;
						}
					}, singletonMap("width", "500"));
		}
	}

	/**
	 * Refresh the current {@link Repository}
	 */
	@Command
	public void refreshRepository() {
		discardRepositoryChanges();
	}

	private void checkRepositoryQueryResult(final RepositoryQuery repositoryQuery) {
		final String basePath = RepositoryManager.getRepositoryPath() + File.separator;
		final String userLogin = repositoryQuery.getRepository().getUser().getLogin();
		final String repositoryPath = repositoryQuery.getRepository().getPath() + File.separator;
		final String directoryPath = basePath + userLogin + File.separator + repositoryPath;

		final RepositoryQueryScheduled repositoryQueryScheduled = new RepositoryQueryScheduled(repositoryQuery,
				directoryPath, true);

		ExecutionEngine.getSingleton().executeTask(repositoryQueryScheduled);
	}
	
	private boolean RepositoryQueryReadyToBeScheduled(final RepositoryQuery repositoryQuery) {
		return (repositoryQuery.getPubmedDownloadTo() != 0
				&& repositoryQuery.getPubmedDownloadTo() != Integer.MAX_VALUE)
				|| (repositoryQuery.getScopusDownloadTo() != 0
						&& repositoryQuery.getScopusDownloadTo() != Integer.MAX_VALUE);
	}
	
	@Command
	public void launchExecution(@BindingParam("current") final RepositoryQuery repositoryQuery) {
		findRepositoryQuery(repositoryQuery);

		final String basePath = RepositoryManager.getRepositoryPath() + File.separator;
		final String userLogin = repositoryQuery.getRepository().getUser().getLogin();
		final String repositoryPath = this.repositoryQuery.getRepository().getPath() + File.separator;
		final String directoryPath = basePath + userLogin + File.separator + repositoryPath;

		if(RepositoryQueryReadyToBeScheduled(this.repositoryQuery)){
			final RepositoryQueryScheduled repositoryQueryScheduled = new RepositoryQueryScheduled(this.repositoryQuery,
					directoryPath, false);
			ExecutionEngine.getSingleton().scheduleTask(repositoryQueryScheduled);
		} else {
			Messagebox.show("The query has no results.\n Edit it and try again.");
		}

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
	public void abortExecution(@BindingParam("current") RepositoryQuery repositoryQuery) {
		findRepositoryQuery(repositoryQuery);

		final RepositoryQueryScheduled repositoryQueryScheduled = new RepositoryQueryScheduled(this.repositoryQuery);
		if (this.repositoryQuery.isRunning()) {
			Messagebox.show("Do you want to stop the execution?", "Stop Execution",
					new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
					new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
						if (event.getName().equals(Messagebox.ON_OK)) {
							stopRepositoryQueryExecution(repositoryQueryScheduled);
							postNotifyChange(this, "repositoryQuery", "queries", "repositoryQueries");
						}
					});
		}
	}

	private void stopRepositoryQueryExecution(final RepositoryQueryScheduled repositoryQueryScheduled) {
		ExecutionEngine.getSingleton().removeTask(repositoryQueryScheduled);
	}

	@Command
	public void abortAllExecutions() {
		Messagebox.show("Do you want to stop all the executions?", "Stop Executions",
				new Messagebox.Button[] { Messagebox.Button.OK, Messagebox.Button.CANCEL },
				new String[] { "Confirm", "Cancel" }, Messagebox.QUESTION, null, event -> {
					if (event.getName().equals(Messagebox.ON_OK)) {
						for (RepositoryQuery repositoryQuery : this.queries) {
							findRepositoryQuery(repositoryQuery);
							final RepositoryQueryScheduled repositoryQueryScheduled = new RepositoryQueryScheduled(
									this.repositoryQuery);
							stopRepositoryQueryExecution(repositoryQueryScheduled);
						}
					}
				});
	}

	@GlobalCommand(MainViewModel.GC_UPDATE_EXECUTIONS)
	public void updateExecutions(@BindingParam("task") final RepositoryQueryScheduled repositoryQueryScheduled,
			@BindingParam("action") final String action, @BindingParam("data") boolean toCheck) {
		this.repositoryQuery = repositoryQueryScheduled.getRepositoryQuery();

		Repository repository = this.repositoryQuery.getRepository();

		switch (action) {
		case GlobalEvents.ACTION_FINISHED:
			if (toCheck) {

				synchronized (this.repositoryQuery) {
					this.repositoryQuery.setChecked(true);
					tm.runInTransaction(em -> {
						em.merge(this.repositoryQuery);
					});
				}

				synchronized (this.queries) {
					if (this.queries.contains(this.repositoryQuery)) {
						final int indexOf = this.queries.indexOf(this.repositoryQuery);
						this.queries.set(indexOf, this.repositoryQuery);
					} else {
						this.queries.add(this.repositoryQuery);
					}
					postNotifyChange(this, "repositoryQuery", "queries", "repositoryQueries");

					publishRefreshData("queries");
				}
			} else {

				final String directoryPath = repositoryQueryScheduled.getDirectoryPath();
				final long filesInDirectory = numberOfFilesInDirectory(directoryPath);

				synchronized (repository) {
					final int numberOfPapers = (int) (repository.getNumberOfPapers() + filesInDirectory);

					repository.setNumberOfPapers((int) (numberOfPapers));
					repository.setLastUpdate(SIMPLE_DATE_FORMAT.format(new Date()));

					tm.runInTransaction(em -> em.merge(repository));
				}

				setRepository(repository);
				this.repositories = getRepositories();

				postNotifyChange(this, "repository", "repositories", "repositoryReadyToCompress");
			}
			break;
		case GlobalEvents.ACTION_SCHEDULED:
			synchronized (this.repositoryQuery) {
				this.repositoryQuery.setRunning(true);
				tm.runInTransaction(em -> em.merge(this.repositoryQuery));
			}
			synchronized (this.queries) {
				final int indexOf = this.queries.indexOf(this.repositoryQuery);
				this.queries.remove(indexOf);
				this.setRepositoryQuery(this.repositoryQuery);
				this.queries.add(indexOf, this.repositoryQuery);
			}
			postNotifyChange(this, "repositoryQuery", "queries", "repositoryQueries");
			break;
		case GlobalEvents.ACTION_ABORTED:
			synchronized (this.repositoryQuery) {
				this.repositoryQuery.setRunning(false);
				tm.runInTransaction(em -> em.merge(this.repositoryQuery));

			}
			synchronized (this.queries) {
				this.queries.set(this.queries.indexOf(this.repositoryQuery), this.repositoryQuery);
			}
			postNotifyChange(this, "repositoryQuery", "queries");
			break;
		case GlobalEvents.ACTION_STARTED:
			findRepositoryQuery(this.repositoryQuery);

			if (toCheck) {
				synchronized (this.repositoryQuery) {
					this.repositoryQuery.setChecked(false);
					tm.runInTransaction(em -> {
						em.merge(this.repositoryQuery);
					});
				}

				final int indexOf = this.queries.indexOf(this.repositoryQuery);
				this.queries.remove(indexOf);
				this.queries.add(indexOf, this.repositoryQuery);
				postNotifyChange(this, "repositoryQuery", "queries", "repositoryQueries");
			}
			break;
		default:
		}
	}

	private long numberOfFilesInDirectory(final String directoryPath) {
		try {
			final int metadataAndLogFiles = 2;
			return Files
					.find(Paths.get(directoryPath), Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile())
					.count() - metadataAndLogFiles;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
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

	private List<String> readRepositoryPapers() {
		if (isRepositoryReadyToDownload()) {
			final String userLogin = this.repository.getUser().getLogin() + File.separator;
			final String basePath = RepositoryManager.getRepositoryPath() + File.separator + userLogin;
			final String repositoryPath = this.repository.getPath() + File.separator;
			final String finalPath = basePath + repositoryPath;

			return RepositoryManager.readPaperTitleFromLog(finalPath);
		}
		return new LinkedList<>();
	}

	@GlobalCommand(MainViewModel.GC_REFRESH_DATA)
	public void refreshData(@BindingParam("data") String toRefresh) {
		if (toRefresh.equals("queries")) {
			this.queries = getAllQueries();
			this.repositoryQueries = getRepositoryQueries();
			postNotifyChange(this, "queries", "repositoryQueries");
		} else {
			this.repositories = getRepositories();
			this.queries = getAllQueries();
			this.repositoryQueries = getRepositoryQueries();
			postNotifyChange(this, "repositories", "queries", "repositoryQueries");
		}
	}

	private void publishRefreshData(final String... properties) {
		for (String property : properties) {
			final String suffix;
			final String event;
			if (property.equals("queries")) {
				event = GlobalEvents.SUFFIX_QUERIES;
				suffix = "queries";
			} else if (property.equals("repositories")) {
				event = GlobalEvents.SUFFIX_REPOSITORIES;
				suffix = "repositories";
			} else {
				event = GlobalEvents.SUFFIX_USERS;
				suffix = "users";
			}

			final EventQueue<Event> adminQueue = EventQueueUtils.getAdminQueue();
			if (adminQueue != null) {
				adminQueue.publish(new Event(EVENT_REFRESH_DATA + event, null, new EventRepositoryQuery(null, suffix)));
			}
		}
	}
}
