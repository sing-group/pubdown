package es.uvigo.ei.sing.pubdown.web.entities;

import static java.util.Objects.requireNonNull;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
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

	private static final String FREQUENCY_DAILY = "daily";

	private static final String FREQUENCY_WEEKLY = "weekly";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Basic
	private String name;

	@Basic
	private String query;

	@Basic
	private String repository;

	@Basic
	private String directory;

	@Basic
	private boolean scopus = false;

	@Basic
	private boolean pubmed = false;

	@Basic
	private int scopusDownloadTo = Integer.MAX_VALUE;

	@Basic
	private int pubmedDownloadTo = Integer.MAX_VALUE;

	@Basic
	private boolean abstractPaper = false;

	@Basic
	private boolean fulltextPaper = false;

	@Basic
	private boolean pdfToText = false;

	@Basic
	private boolean keepPdf = false;

	@Basic
	private boolean groupBy = false;

	@Basic
	private boolean daily = true;

	@Basic
	private boolean checked = false;

	@Basic
	private boolean running = false;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "userId")
	private User user;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "taskId")
	private Task task;

	/**
	 * Empty Constructor
	 */
	public RepositoryQuery() {
		this.task = new Task();
	}

	public RepositoryQuery(final User user, final String name, final String query, final String repository,
			final String directory, final boolean scopus, final boolean pubmed, final int scopusDownloadTo,
			final int pubmedDownloadTo, final boolean abstractPaper, final boolean fulltextPaper,
			final boolean pdfToText, final boolean keepPdf, final boolean groupBy, final boolean daily,
			final boolean checked, final boolean running, final Task task) {
		this.user = user;
		this.name = name;
		this.query = query;
		this.repository = repository;
		this.directory = directory;
		this.scopus = scopus;
		this.pubmed = pubmed;
		this.scopusDownloadTo = scopusDownloadTo;
		this.pubmedDownloadTo = pubmedDownloadTo;
		this.abstractPaper = abstractPaper;
		this.fulltextPaper = fulltextPaper;
		this.pdfToText = pdfToText;
		this.keepPdf = keepPdf;
		this.groupBy = groupBy;
		this.daily = daily;
		this.checked = checked;
		this.running = running;
		this.task = new Task();
	}

	private RepositoryQuery(final Integer id, final String name, final String query, final String repository,
			final String directory, final boolean scopus, final boolean pubmed, final int scopusDownloadTo,
			final int pubmedDownloadTo, final boolean abstractPaper, final boolean fulltextPaper,
			final boolean pdfToText, final boolean keepPdf, final boolean groupBy, final boolean daily,
			final boolean checked, final boolean running, final Task task) {
		this.id = id;
		this.name = name;
		this.query = query;
		this.repository = repository;
		this.directory = directory;
		this.scopus = scopus;
		this.pubmed = pubmed;
		this.scopusDownloadTo = scopusDownloadTo;
		this.pubmedDownloadTo = pubmedDownloadTo;
		this.abstractPaper = abstractPaper;
		this.fulltextPaper = fulltextPaper;
		this.pdfToText = pdfToText;
		this.keepPdf = keepPdf;
		this.groupBy = groupBy;
		this.daily = daily;
		this.checked = checked;
		this.running = running;
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
	public String getRepository() {
		return repository;
	}

	/**
	 * Setter method of the repository global variable
	 * 
	 * @param repository
	 *            the value of the repository global variable
	 */
	public void setRepository(final String repository) {
		requireNonNull(repository, "repository can't be null");

		this.repository = repository.trim();
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(final String directory) {
		this.directory = directory;
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

	public boolean isAbstractPaper() {
		return abstractPaper;
	}

	public void setAbstractPaper(final boolean abstractPaper) {
		this.abstractPaper = abstractPaper;
	}

	public boolean isFulltextPaper() {
		return fulltextPaper;
	}

	public void setFulltextPaper(final boolean fulltextPaper) {
		this.fulltextPaper = fulltextPaper;
	}

	public boolean isPdfToText() {
		return pdfToText;
	}

	public void setPdfToText(final boolean pdfToText) {
		this.pdfToText = pdfToText;
	}

	public boolean isKeepPdf() {
		return keepPdf;
	}

	public void setKeepPdf(final boolean keepPdf) {
		this.keepPdf = keepPdf;
	}

	public String getGroupBy() {
		return String.valueOf(groupBy);
	}

	public void setGroupBy(final String groupBy) {
		this.groupBy = Boolean.parseBoolean(groupBy);
	}
	
	public void setExecutionFrequency(String frequency) {
		switch (frequency) {
		case FREQUENCY_DAILY:
			this.setDaily(true);
			break;
		case FREQUENCY_WEEKLY:
			this.setDaily(false);
			break;
		default:
			throw new IllegalArgumentException("Valid frequencies are 'daily' and 'weekly'");
		}
	}
	
	public String getExecutionFrequency() {
		return this.isDaily() ? FREQUENCY_DAILY : FREQUENCY_WEEKLY;
	}

	public boolean isDaily() {
		return daily;
	}

	public void setDaily(final boolean daily) {
		this.daily = daily;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	/**
	 * Getter method of the user global variable
	 * 
	 * @return the value of the user global variable
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Setter method of the user global variable
	 * 
	 * @param user
	 *            the value of the user global variable
	 */
	public void setUser(final User user) {
		if (this.user != null) {
			this.user.removeRepositoryQuery(this);
		}
		if (user != null) {
			user.addRepositoryQuery(this);
		}
	}

	/**
	 * Assigns the {@link RepositoryQuery} owner
	 * 
	 * @param user
	 *            indicates the {@link User} who owns the
	 *            {@link RepositoryQuery}
	 */
	void packageSetUser(final User user) {
		this.user = user;
	}

	/**
	 * Gets the {@link RepositoryQuery} name
	 * 
	 * @return the {@link RepositoryQuery} name
	 */
	@Override
	public String toString() {
		return this.getName();
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
		return new RepositoryQuery(this.id, this.name, this.query, this.repository, this.directory, this.scopus,
				this.pubmed, this.scopusDownloadTo, this.pubmedDownloadTo, this.abstractPaper, this.fulltextPaper,
				this.pdfToText, this.keepPdf, this.groupBy, this.daily, this.checked, this.running, this.task.clone());
	}

	/**
	 * Compares a {@link RepositoryQuery} with another {@link RepositoryQuery}
	 *
	 * @return <code>true</code> if the {@link RepositoryQuery} are equal,
	 *         <code>false</code> otherwise
	 */
	@Override
	public int compareTo(final RepositoryQuery obj) {
		return Compare.objects(this, obj)
			.by(RepositoryQuery::getId)
			.thenBy(RepositoryQuery::getName)
			.thenBy(RepositoryQuery::getQuery)
			.thenBy(RepositoryQuery::getRepository)
			.thenBy(RepositoryQuery::getDirectory)
			.thenBy(RepositoryQuery::isScopus)
			.thenBy(RepositoryQuery::isPubmed)
			.thenBy(RepositoryQuery::isAbstractPaper)
			.thenBy(RepositoryQuery::isFulltextPaper)
			.thenBy(RepositoryQuery::isPdfToText)
			.thenBy(RepositoryQuery::isKeepPdf)
			.thenBy(RepositoryQuery::getGroupBy)
			.thenBy(RepositoryQuery::isDaily)
			.thenBy(RepositoryQuery::getTask)
		.andGet();
	}

}
