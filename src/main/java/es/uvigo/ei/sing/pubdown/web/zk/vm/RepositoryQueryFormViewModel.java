package es.uvigo.ei.sing.pubdown.web.zk.vm;

import static es.uvigo.ei.sing.pubdown.util.Checks.isEmpty;
import static java.util.Collections.singletonMap;

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
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.zk.util.DesktopTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.ViewModelFunctions;

/**
 * ViewModel to manage the form to create/edit {@link RepositoryQuery}
 */
public class RepositoryQueryFormViewModel extends ViewModelFunctions {
	private final TransactionManager tm = new DesktopTransactionManager();

	private RepositoryQuery repositoryQuery;
	private RepositoryQuery uneditedRespositoryQuery;

	/**
	 * Assigns the robot received as parameter to the {@link RepositoryQueryFormViewModel}
	 * global variables.
	 * 
	 * @param repositoryQuery
	 *            the {@link RepositoryQuery}
	 */
	@Init
	public void init(@ExecutionArgParam("robot") final RepositoryQuery repositoryQuery) {
		this.repositoryQuery = repositoryQuery;
		this.uneditedRespositoryQuery = repositoryQuery.clone();
	}

	/**
	 * Getter of the robot global variable
	 * 
	 * @return the value of the robot global variable
	 */
	public RepositoryQuery getRepositoryQuery() {
		return repositoryQuery;
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
	private boolean isRobotModified() {
		return this.repositoryQuery.compareTo(this.uneditedRespositoryQuery) != 0;
	}

	/**
	 * Gets all the {@link RepositoryQuery} categories
	 * 
	 * @return a list with all the distinct {@link RepositoryQuery} categories
	 *         sorted by category
	 */
	public List<String> getRepositories() {
		return tm.getInTransaction(
				em -> em.createQuery("SELECT DISTINCT r.repository FROM RepositoryQuery r ORDER BY r.repository ASC",
						String.class).getResultList());
	}

	/**
	 * Method linked with {@link RepositoryQueryFormViewModel#checkData()}. Checks if the
	 * {@link RepositoryQuery} name is an empty
	 * 
	 * @return <code>true</code> if the {@link RepositoryQuery} is not empty,
	 *         <code>false</code> otherwise.
	 */
	public boolean isValid() {
		return !isEmpty(this.repositoryQuery.getName());
	}

	/**
	 * Checks if some variables are valid. Method linked with
	 * {@link RepositoryQueryFormViewModel#isValid()}
	 */
	@Command
	@NotifyChange("valid")
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
					addInvalidMessage(ctx, "Query name can't be empty");
				}
			}
		};
	}

	/**
	 * Persists the {@link RepositoryQuery} changes and notifies the changes to
	 * the {@link AdministrationViewModel}
	 */
	@Command
	public void confirm() {
		if (isRobotModified()) {
			tm.runInTransaction(em -> {
				if (isNewRepositoryQuery()) {
					this.repositoryQuery.setUser(getCurrentUser(tm));
				}
				final String command = !isNewRepositoryQuery() ? "updateRobotList" : "addRobotList";

				em.persist(this.repositoryQuery);

				BindUtils.postGlobalCommand(EventQueueUtils.QUEUE_NAME, null, command,
						singletonMap("robot", this.repositoryQuery));
			});
		}
	}

	/**
	 * Refresh the {@link RepositoryQuery} in the form
	 */
	@Command
	@NotifyChange("repositoryQuery")
	public void refresh() {
		if (isRobotModified()) {
			tm.runInTransaction(em -> {
				em.refresh(this.repositoryQuery);

				this.uneditedRespositoryQuery = this.repositoryQuery.clone();
			});
		}
	}

}