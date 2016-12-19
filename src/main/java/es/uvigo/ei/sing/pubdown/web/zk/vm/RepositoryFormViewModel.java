package es.uvigo.ei.sing.pubdown.web.zk.vm;

import static es.uvigo.ei.sing.pubdown.util.Checks.isEmpty;
import static java.util.Collections.singletonMap;

import java.util.UUID;

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
import es.uvigo.ei.sing.pubdown.web.zk.util.CleanEntityManagerTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.ViewModelUtils;

/**
 * ViewModel to manage the form to create/edit {@link RepositoryQuery}
 */
public class RepositoryFormViewModel extends ViewModelUtils {
	private final TransactionManager tm = new CleanEntityManagerTransactionManager();

	private Repository repository;
	private Repository uneditedRepository;

	@Init
	public void init(@ExecutionArgParam("repository") final Repository repository) {
		this.repository = repository;
		this.uneditedRepository = repository.clone();
	}

	public Repository getRepository() {
		return repository;
	}

	public boolean isNewRepository() {
		return this.repository.getId() == null;
	}

	/**
	 * Checks if any of the {@link RepositoryQuery} fields are modified
	 * 
	 * @return <code>true</code> if any field has been modified,
	 *         <code>false</code> otherwise
	 */
	private boolean isRepositoryModified() {
		return this.repository.compareTo(this.uneditedRepository) != 0;
	}

	public boolean isValidRepository() {
		return !isEmpty(this.repository.getName());
	}

	public boolean isRepositoryReady() {
		return isValidRepository() && (this.repository.isFulltextPaper() || this.repository.isAbstractPaper());
	}

	@Command
	@NotifyChange({ "validRepository", "repositoryReady" })
	public void checkRepository() {
	}

	public Validator getNameValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String name = (String) ctx.getProperty().getValue();

				if (isEmpty(name) || name.length() == 0) {
					addInvalidMessage(ctx, "Name can't be empty");
				}
			}
		};
	}

	public Validator getDownloadLimitValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String downloadLimit = (String) ctx.getProperty().getValue();

				if (isEmpty(downloadLimit)) {
					addInvalidMessage(ctx, "Limit can't be empty");
				}
				try {
					final int limit = Integer.parseInt(downloadLimit);
					if (limit <= 0 || limit > Integer.MAX_VALUE) {
						addInvalidMessage(ctx, "Limit must be an integer between 0 and " + Integer.MAX_VALUE);
					}
				} catch (NumberFormatException e) {
					addInvalidMessage(ctx, "Limit must be an integer between 0 and " + Integer.MAX_VALUE);
				}
			}
		};
	}

	@Command
	@NotifyChange({ "repository" })
	public void confirm() {

		Repository repository = this.getRepository();
		
		final boolean isNew = isNewRepository();

		if (isNew) {
			repository.setPath(UUID.randomUUID().toString());
			repository.setUser(getCurrentUser(tm));
			tm.runInTransaction(em -> em.persist(repository));

		} else {
			tm.runInTransaction(em -> em.merge(repository));
		}

		final String command = isNew ? "addRepository" : "updateRepository";

		this.uneditedRepository = repository.clone();

		BindUtils.postGlobalCommand(EventQueueUtils.QUEUE_NAME, null, command, singletonMap("repository", repository));
	}

	@Command
	public void refresh() {
		if (isRepositoryModified()) {
			if (!isNewRepository()) {
				tm.runInTransaction(em -> {
					if (!em.contains(this.repository)) {
						this.repository = em.find(Repository.class, this.repository.getId());
					}

					em.refresh(this.repository);

					this.uneditedRepository = this.repository.clone();

					BindUtils.postGlobalCommand(EventQueueUtils.QUEUE_NAME, null, "updateRepository",
							singletonMap("repository", this.repository));

				});
			} else {
				this.repository = new Repository();
			}
			postNotifyChange(this, "repository");
		}
	}

}