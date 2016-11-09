package es.uvigo.ei.sing.pubdown.web.entities;

import static es.uvigo.ei.sing.pubdown.web.entities.ExecutionState.UNSCHEDULED;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import es.uvigo.ei.sing.pubdown.util.Compare;

/**
 * Entity that represents an {@link User} {@link RepositoryQuery}
 */
@Entity(name = "RepositoryQuery")
public class RepositoryQuery implements Cloneable, Comparable<RepositoryQuery> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Basic
	private String name;

	@Basic
	private String query;

	@Basic
	private boolean scopus = false;

	@Basic
	private boolean pubmed = false;

	@Basic
	private int downloadLimit = 1;

	@Basic
	private int scopusDownloadTo = Integer.MAX_VALUE;

	@Basic
	private int pubmedDownloadTo = Integer.MAX_VALUE;

	@Basic
	private boolean groupBy = true;

	@Basic
	private boolean checked = false;

	@Basic
	private boolean scheduled = false;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ExecutionState executionState = UNSCHEDULED;

	@Basic
	private String lastExecution = "Never";

	@Basic
	private String nextExecution = "Unscheduled";

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "repositoryId")
	private Repository repository;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "taskId")
	private RepositoryQueryTask task;

	/**
	 * Empty Constructor
	 */
	public RepositoryQuery() {
		this.repository = new Repository();
		this.task = new RepositoryQueryTask();
	}

	public RepositoryQuery(final Repository repository) {
		this.repository = repository;
		this.task = new RepositoryQueryTask();
	}

	public RepositoryQuery(final String name, final String query, final Repository repository, final boolean scopus,
			final boolean pubmed, final int downloadLimit, final int scopusDownloadTo, final int pubmedDownloadTo,
			final boolean groupBy, final boolean checked, final boolean scheduled, final RepositoryQueryTask task) {
		this.name = name;
		this.query = query;
		this.repository = repository;
		this.scopus = scopus;
		this.pubmed = pubmed;
		this.downloadLimit = downloadLimit;
		this.scopusDownloadTo = scopusDownloadTo;
		this.pubmedDownloadTo = pubmedDownloadTo;
		this.groupBy = groupBy;
		this.checked = checked;
		this.scheduled = scheduled;
		this.task = new RepositoryQueryTask();
	}

	private RepositoryQuery(final Integer id, final String name, final String query, final Repository repository,
			final boolean scopus, final boolean pubmed, final int downloadLimit, final int scopusDownloadTo,
			final int pubmedDownloadTo, final boolean groupBy, final boolean checked, final boolean scheduled,
			final RepositoryQueryTask task) {
		this.id = id;
		this.name = name;
		this.query = query;
		this.repository = repository;
		this.scopus = scopus;
		this.pubmed = pubmed;
		this.downloadLimit = downloadLimit;
		this.scopusDownloadTo = scopusDownloadTo;
		this.pubmedDownloadTo = pubmedDownloadTo;
		this.groupBy = groupBy;
		this.checked = checked;
		this.scheduled = scheduled;
		this.task = task;
	}

	/**
	 * Getter method of the id global variable
	 * 
	 * @return the value of the id global variable
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Getter method of the name global variable
	 * 
	 * @return the value of the name global variable
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter method of the name global variable
	 * 
	 * @param name
	 *            the value of the name global variable
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Getter method of the repository global variable
	 * 
	 * @return the value of the repository global variable
	 */
	public Repository getRepository() {
		return repository;
	}

	public void setRepository(final Repository repository) {
		if (this.repository != null) {
			this.repository.removeRepositoryQuery(this);
		}
		if (repository != null) {
			repository.addRepository(this);
		}
	}

	void packageSetRepository(final Repository repository) {
		this.repository = repository;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(final String query) {
		this.query = query;
	}

	public boolean isScopus() {
		return scopus;
	}

	public void setScopus(final boolean scopus) {
		this.scopus = scopus;
	}

	public boolean isPubmed() {
		return pubmed;
	}

	public void setPubmed(final boolean pubmed) {
		this.pubmed = pubmed;
	}

	public int getDownloadLimit() {
		return downloadLimit;
	}

	public void setDownloadLimit(int downloadLimit) {
		this.downloadLimit = downloadLimit;
	}

	public int getScopusDownloadTo() {
		return scopusDownloadTo;
	}

	public void setScopusDownloadTo(final int scopusDownloadTo) {
		this.scopusDownloadTo = scopusDownloadTo;
	}

	public int getPubmedDownloadTo() {
		return pubmedDownloadTo;
	}

	public void setPubmedDownloadTo(final int pubmedDownloadTo) {
		this.pubmedDownloadTo = pubmedDownloadTo;
	}

	public boolean isGroupBy() {
		return groupBy;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(final boolean checked) {
		this.checked = checked;
	}

	public boolean isScheduled() {
		return scheduled;
	}

	public void setScheduled(final boolean scheduled) {
		this.scheduled = scheduled;
	}

	public ExecutionState getExecutionState() {
		return executionState;
	}

	public void setExecutionState(ExecutionState executionState) {
		this.executionState = executionState;
	}

	public String getLastExecution() {
		return lastExecution;
	}

	public void setLastExecution(String lastExecution) {
		this.lastExecution = lastExecution;
	}
	
	public String getNextExecution() {
		return nextExecution;
	}
	
	public void setNextExecution(String nextExecution) {
		this.nextExecution = nextExecution;
	}

	public RepositoryQueryTask getTask() {
		return task;
	}

	public void setTask(final RepositoryQueryTask task) {
		this.task = task;
	}

	/**
	 * {@link RepositoryQuery} hashcode method
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * {@link RepositoryQuery} equals method
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final RepositoryQuery other = (RepositoryQuery) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	/**
	 * Clones a {@link RepositoryQuery}
	 *
	 * @return a new {@link RepositoryQuery}
	 */
	@Override
	public RepositoryQuery clone() {
		return new RepositoryQuery(this.id, this.name, this.query, this.repository.clone(), this.scopus, this.pubmed,
				this.downloadLimit, this.scopusDownloadTo, this.pubmedDownloadTo, this.groupBy, this.checked,
				this.scheduled, this.task.clone());
	}

	/**
	 * Compares a {@link RepositoryQuery} with another {@link RepositoryQuery}
	 *
	 * @return <code>true</code> if the {@link RepositoryQuery} are equal,
	 *         <code>false</code> otherwise
	 */
	@Override
	public int compareTo(final RepositoryQuery obj) {
		return Compare.objects(this, obj).by(RepositoryQuery::getId).thenBy(RepositoryQuery::getName)
				.thenBy(RepositoryQuery::getQuery).thenBy(RepositoryQuery::getRepository)
				.thenBy(RepositoryQuery::isScopus).thenBy(RepositoryQuery::isPubmed)
				.thenBy(RepositoryQuery::getDownloadLimit).thenBy(RepositoryQuery::isGroupBy)
				.thenBy(RepositoryQuery::getTask).andGet();
	}

}
