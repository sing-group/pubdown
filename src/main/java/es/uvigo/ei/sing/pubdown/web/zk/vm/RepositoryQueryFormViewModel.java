package es.uvigo.ei.sing.pubdown.web.zk.vm;

import static es.uvigo.ei.sing.pubdown.util.Checks.isEmpty;
import static java.util.Collections.singletonMap;

import java.io.File;
import java.util.List;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;

import es.uvigo.ei.sing.pubdown.execution.EventQueueUtils;
import es.uvigo.ei.sing.pubdown.web.entities.Repository;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQueryTask;
import es.uvigo.ei.sing.pubdown.web.zk.util.DesktopTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.ViewModelFunctions;

/**
 * ViewModel to manage the form to create/edit {@link RepositoryQuery}
 */
public class RepositoryQueryFormViewModel extends ViewModelFunctions {
	private final TransactionManager tm = new DesktopTransactionManager();

	private RepositoryQuery repositoryQuery;
	private RepositoryQuery uneditedRepositoryQuery;
	private RepositoryQueryTask repositoryQueryTask;
	private RepositoryQueryTask uneditedRepositoryQueryTask;

	/**
	 * Assigns the robot received as parameter to the
	 * {@link RepositoryQueryFormViewModel} global variables.
	 * 
	 * @param repositoryQuery
	 *            the {@link RepositoryQuery}
	 */
	@Init
	public void init(@ExecutionArgParam("repositoryQuery") final RepositoryQuery repositoryQuery) {
		this.repositoryQuery = repositoryQuery;
		this.uneditedRepositoryQuery = this.repositoryQuery.clone();
		this.repositoryQueryTask = this.repositoryQuery.getTask();
		this.uneditedRepositoryQueryTask = this.repositoryQueryTask.clone();
	}

	/**
	 * Getter of the robot global variable
	 * 
	 * @return the value of the robot global variable
	 */
	public RepositoryQuery getRepositoryQuery() {
		return repositoryQuery;
	}

	public RepositoryQueryTask getRepositoryQueryTask() {
		return repositoryQueryTask;
	}

	public RepositoryQuery getUneditedRepositoryQuery() {
		return uneditedRepositoryQuery;
	}

	public RepositoryQueryTask getUneditedRepositoryQueryTask() {
		return uneditedRepositoryQueryTask;
	}

	/**
	 * Checks if a {@link RepositoryQuery} is new
	 * 
	 * @return <code>true</code> if the {@link RepositoryQuery} is new,
	 *         <code>false</code> otherwise
	 */
	public boolean isNewRepositoryQuery() {
		return this.repositoryQuery.getId() == null;
	}

	/**
	 * Checks if any of the {@link RepositoryQuery} fields are modified
	 * 
	 * @return <code>true</code> if any field has been modified,
	 *         <code>false</code> otherwise
	 */
	private boolean isRepositoryQueryModified() {
		return this.repositoryQuery.compareTo(this.uneditedRepositoryQuery) != 0;
	}

	public boolean isRepositoryQueryTaskModified() {
		return this.repositoryQueryTask.compareTo(this.uneditedRepositoryQueryTask) != 0;
	}

	public List<Repository> getRepositories() {
		return tm.getInTransaction(
				em -> em.createQuery("SELECT DISTINCT r FROM Repository r WHERE r.user = :user ORDER BY r.name ASC",
						Repository.class).setParameter("user", getCurrentUser(tm)).getResultList());
	}

	public boolean isUserApiKeyValid() {
		return !(getCurrentUser(tm).getApiKey() == null || getCurrentUser(tm).getApiKey().isEmpty());
	}

	public boolean isQueryReadyToCheckResult() {
		return isValidRepositoryQuery() && (this.repositoryQuery.isScopus() || this.repositoryQuery.isPubmed())
				&& (this.repositoryQuery.isFulltextPaper() || this.repositoryQuery.isAbstractPaper());
	}

	public boolean isValidRepositoryQuery() {
		return !isEmpty(this.repositoryQuery.getName()) && !isEmpty(this.repositoryQuery.getQuery())
				&& !isEmpty(this.repositoryQuery.getRepository().getName());
	}

	public boolean isDaily() {
		return this.repositoryQuery.getTask().isDaily();
	}

	/**
	 * Checks if some variables are valid. Method linked with
	 * {@link RepositoryQueryFormViewModel#isValid()}
	 */
	@Command
	@NotifyChange({ "daily", "validRepositoryQuery", "queryReadyToCheckResult" })
	public void checkData() {
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

	/**
	 * Persists the {@link RepositoryQuery} creation/changes
	 */
	@Command
	@NotifyChange({ "repositoryQuery", "repositoryQueries", "repositoryQueryTask" })
	public void confirm() {

		final RepositoryQuery repositoryQuery = this.getRepositoryQuery();

		final String downloadDirectory = getCurrentUser(tm).getLogin() + File.separator
				+ repositoryQuery.getRepository() + File.separator;

		final boolean isNew = isNewRepositoryQuery();

		if (isNew) {
			tm.runInTransaction(em -> em.persist(repositoryQuery));
		} else {
			tm.runInTransaction(em -> em.merge(repositoryQuery));
		}

		final File newDirectory = new File(downloadDirectory);
		if (!newDirectory.exists()) {
			newDirectory.mkdirs();
		}

		final String command = isNew ? "addRepositoryQuery" : "updateRepositoryQuery";

		this.uneditedRepositoryQuery = repositoryQuery.clone();
		this.uneditedRepositoryQueryTask = this.uneditedRepositoryQuery.getTask();
		
		BindUtils.postGlobalCommand(EventQueueUtils.QUEUE_NAME, null, command,
				singletonMap("repositoryQuery", repositoryQuery));
	}

	@Command
	public void refresh() {
		if (isRepositoryQueryModified() || isRepositoryQueryTaskModified()) {
			if (!isNewRepositoryQuery()) {
				tm.runInTransaction(em -> {
					if (!em.contains(this.repositoryQuery)) {
						this.repositoryQuery = em.find(RepositoryQuery.class, this.repositoryQuery.getId());
					}

					if (!em.contains(this.repositoryQueryTask)) {
						this.repositoryQueryTask = em.find(RepositoryQueryTask.class,
								this.repositoryQuery.getTask().getId());
					}

					em.refresh(this.repositoryQueryTask);
					em.refresh(this.repositoryQuery);

					this.uneditedRepositoryQuery = this.repositoryQuery.clone();
					this.uneditedRepositoryQueryTask = this.uneditedRepositoryQuery.getTask();

					BindUtils.postGlobalCommand(EventQueueUtils.QUEUE_NAME, null, "updateRepositoryQuery",
							singletonMap("repositoryQuery", this.repositoryQuery));

				});
			} else {
				this.repositoryQuery = new RepositoryQuery();
			}
			postNotifyChange(this, "repositoryQuery");
		}
	}

}