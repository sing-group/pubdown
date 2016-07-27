package es.uvigo.ei.sing.pubdown.web.entities;

import static java.util.Objects.requireNonNull;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

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
	private String repository;

	@Basic
	private boolean scopus = false;

	@Basic
	private boolean pubmed = false;

	@Basic
	private int scopusDownloadTo = 0;

	@Basic
	private int pubmedDownloadTo = 0;

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

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "userId")
	private User user;

	/**
	 * Empty Constructor
	 */
	public RepositoryQuery() {
	}

	public RepositoryQuery(User user, String name, String query, String repository, boolean scopus, boolean pubmed,
			int scopusDownloadTo, int pubmedDownloadTo, boolean abstractPaper, boolean fulltextPaper, boolean pdfToText,
			boolean keepPdf, boolean groupBy) {
		this.user = user;
		this.name = name;
		this.query = query;
		this.repository = repository;
		this.scopus = scopus;
		this.pubmed = pubmed;
		this.scopusDownloadTo = scopusDownloadTo;
		this.pubmedDownloadTo = pubmedDownloadTo;
		this.abstractPaper = abstractPaper;
		this.fulltextPaper = fulltextPaper;
		this.pdfToText = pdfToText;
		this.keepPdf = keepPdf;
		this.groupBy = groupBy;
	}

	private RepositoryQuery(Integer id, String name, String query, String repository, boolean scopus, boolean pubmed,
			int scopusDownloadTo, int pubmedDownloadTo, boolean abstractPaper, boolean fulltextPaper, boolean pdfToText,
			boolean keepPdf, boolean groupBy) {
		this.id = id;
		this.name = name;
		this.query = query;
		this.repository = repository;
		this.scopus = scopus;
		this.pubmed = pubmed;
		this.scopusDownloadTo = scopusDownloadTo;
		this.pubmedDownloadTo = pubmedDownloadTo;
		this.abstractPaper = abstractPaper;
		this.fulltextPaper = fulltextPaper;
		this.pdfToText = pdfToText;
		this.keepPdf = keepPdf;
		this.groupBy = groupBy;
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

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public boolean isScopus() {
		return scopus;
	}

	public void setScopus(boolean scopus) {
		this.scopus = scopus;
	}

	public boolean isPubmed() {
		return pubmed;
	}

	public void setPubmed(boolean pubmed) {
		this.pubmed = pubmed;
	}

	public int getScopusDownloadTo() {
		return scopusDownloadTo;
	}

	public void setScopusDownloadTo(int scopusDownloadTo) {
		this.scopusDownloadTo = scopusDownloadTo;
	}

	public int getPubmedDownloadTo() {
		return pubmedDownloadTo;
	}

	public void setPubmedDownloadTo(int pubmedDownloadTo) {
		this.pubmedDownloadTo = pubmedDownloadTo;
	}

	public boolean isAbstractPaper() {
		return abstractPaper;
	}

	public void setAbstractPaper(boolean abstractPaper) {
		this.abstractPaper = abstractPaper;
	}

	public boolean isFulltextPaper() {
		return fulltextPaper;
	}

	public void setFulltextPaper(boolean fulltextPaper) {
		this.fulltextPaper = fulltextPaper;
	}

	public boolean isPdfToText() {
		return pdfToText;
	}

	public void setPdfToText(boolean pdfToText) {
		this.pdfToText = pdfToText;
	}

	public boolean isKeepPdf() {
		return keepPdf;
	}

	public void setKeepPdf(boolean keepPdf) {
		this.keepPdf = keepPdf;
	}

	public boolean isGroupBy() {
		return groupBy;
	}

	public void setGroupBy(boolean groupBy) {
		this.groupBy = groupBy;
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final RepositoryQuery other = (RepositoryQuery) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/**
	 * Clones a {@link RepositoryQuery}
	 *
	 * @return a new {@link RepositoryQuery}
	 */
	@Override
	public RepositoryQuery clone() {
		return new RepositoryQuery(this.id, this.name, this.query, this.repository, this.scopus, this.pubmed,
				this.scopusDownloadTo, this.pubmedDownloadTo, this.abstractPaper, this.fulltextPaper, this.pdfToText,
				this.keepPdf, this.groupBy);
	}

	/**
	 * Compares a {@link RepositoryQuery} with another {@link RepositoryQuery}
	 *
	 * @return <code>true</code> if the {@link RepositoryQuery} are equal,
	 *         <code>false</code> otherwise
	 */
	@Override
	public int compareTo(final RepositoryQuery o) {
		return Compare.objects(this, o).by(RepositoryQuery::getId).thenBy(RepositoryQuery::getName)
				.thenBy(RepositoryQuery::getQuery).thenBy(RepositoryQuery::getRepository)
				.thenBy(RepositoryQuery::isScopus).thenBy(RepositoryQuery::isPubmed)
				.thenBy(RepositoryQuery::getScopusDownloadTo).thenBy(RepositoryQuery::getPubmedDownloadTo)
				.thenBy(RepositoryQuery::isAbstractPaper).thenBy(RepositoryQuery::isFulltextPaper)
				.thenBy(RepositoryQuery::isPdfToText).thenBy(RepositoryQuery::isKeepPdf)
				.thenBy(RepositoryQuery::isGroupBy).andGet();
	}
}
