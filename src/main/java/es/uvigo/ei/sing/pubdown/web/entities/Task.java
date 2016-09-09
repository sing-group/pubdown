package es.uvigo.ei.sing.pubdown.web.entities;

import java.io.File;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import es.uvigo.ei.sing.pubdown.paperdown.downloader.RepositoryManager;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.pubmed.PubMedDownloader;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.scopus.ScopusDownloader;
import es.uvigo.ei.sing.pubdown.util.Compare;

@Entity
public class Task implements Cloneable, Comparable<Task> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Basic
	private int hour;

	@Basic
	private int minutes;

	@Basic
	private boolean monday = false;

	@Basic
	private boolean tuesday = false;

	@Basic
	private boolean wednesday = false;

	@Basic
	private boolean thursday = false;

	@Basic
	private boolean friday = false;

	@Basic
	private boolean saturday = false;

	@Basic
	private boolean sunday = false;

	@OneToOne(fetch = FetchType.EAGER, mappedBy = "task")
	private RepositoryQuery repositoryQuery;

	public Task() {
	}

	public Task(int hour, int minutes, boolean monday, boolean tuesday, boolean wednesday, boolean thursday,
			boolean friday, boolean saturday, boolean sunday, RepositoryQuery repositoryQuery) {
		super();
		this.hour = hour;
		this.minutes = minutes;
		this.monday = monday;
		this.tuesday = tuesday;
		this.wednesday = wednesday;
		this.thursday = thursday;
		this.friday = friday;
		this.saturday = saturday;
		this.sunday = sunday;
		this.repositoryQuery = repositoryQuery;
	}

	private Task(Integer id, int hour, int minutes, boolean monday, boolean tuesday, boolean wednesday,
			boolean thursday, boolean friday, boolean saturday, boolean sunday, RepositoryQuery repositoryQuery) {
		super();
		this.id = id;
		this.hour = hour;
		this.minutes = minutes;
		this.monday = monday;
		this.tuesday = tuesday;
		this.wednesday = wednesday;
		this.thursday = thursday;
		this.friday = friday;
		this.saturday = saturday;
		this.sunday = sunday;
		this.repositoryQuery = repositoryQuery;
	}

	public Integer getId() {
		return id;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(final int hour) {
		this.hour = hour;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(final int minutes) {
		this.minutes = minutes;
	}

	public boolean isMonday() {
		return monday;
	}

	public void setMonday(boolean monday) {
		this.monday = monday;
	}

	public boolean isTuesday() {
		return tuesday;
	}

	public void setTuesday(boolean tuesday) {
		this.tuesday = tuesday;
	}

	public boolean isWednesday() {
		return wednesday;
	}

	public void setWednesday(boolean wednesday) {
		this.wednesday = wednesday;
	}

	public boolean isThursday() {
		return thursday;
	}

	public void setThursday(boolean thursday) {
		this.thursday = thursday;
	}

	public boolean isFriday() {
		return friday;
	}

	public void setFriday(boolean friday) {
		this.friday = friday;
	}

	public boolean isSaturday() {
		return saturday;
	}

	public void setSaturday(boolean saturday) {
		this.saturday = saturday;
	}

	public boolean isSunday() {
		return sunday;
	}

	public void setSunday(boolean sunday) {
		this.sunday = sunday;
	}

	public RepositoryQuery getRepositoryQuery() {
		return repositoryQuery;
	}

	public void setRepositoryQuery(final RepositoryQuery repositoryQuery) {
		this.repositoryQuery = repositoryQuery;
	}

	public Runnable getRunnableQuery() {
		return () -> {
			try {
				// final RepositoryQuery repositoryQuery =
				// this.getRepositoryQuery();
				final String query = this.repositoryQuery.getQuery().replace(" ", "+");

				final String scopusApiKey = this.getRepositoryQuery().getUser().getApiKey();
				final String directoryPath = RepositoryManager.getRepositoryPath() + File.separator;

				final ScopusDownloader scopusDownloader = new ScopusDownloader(query, scopusApiKey,
						directoryPath + this.repositoryQuery.getDirectory());

				final PubMedDownloader pubmedDownloader = new PubMedDownloader(query,
						directoryPath + this.repositoryQuery.getDirectory());

				final int downloadFrom = 0;

				int scopusDownloadTo = 0;
				int pubmedDownloadTo = 0;

				if (this.repositoryQuery.getScopusDownloadTo() == Integer.MAX_VALUE
						&& this.repositoryQuery.isScopus()) {
					final int scopusResult = scopusDownloader.getResultSize();
					if (scopusResult != 0) {
						if (scopusResult > 6000) {
							scopusDownloadTo = 6000;
						}

						// 3 to test
						scopusDownloadTo = 3;
						this.repositoryQuery.setScopusDownloadTo(scopusDownloadTo);

						RepositoryManager.updateRepositoryQuery(this.repositoryQuery);
					}
				}

				if (this.repositoryQuery.getPubmedDownloadTo() == Integer.MAX_VALUE
						&& this.repositoryQuery.isPubmed()) {
					final int pubmedResult = pubmedDownloader.getResultSize();
					if (pubmedResult != 0) {
						// 3 to test
						pubmedDownloadTo = 3;
						this.repositoryQuery.setPubmedDownloadTo(pubmedDownloadTo);

						RepositoryManager.updateRepositoryQuery(this.repositoryQuery);
					}
				}
				if (this.repositoryQuery.isFulltextPaper()) {
					final boolean directoryType = Boolean.valueOf(this.repositoryQuery.getGroupBy());
					if (this.repositoryQuery.isScopus()
							&& this.repositoryQuery.getScopusDownloadTo() != Integer.MAX_VALUE) {
						scopusDownloader.downloadPapers(true, this.repositoryQuery.isPdfToText(),
								this.repositoryQuery.isKeepPdf(), directoryType, downloadFrom,
								this.repositoryQuery.getScopusDownloadTo());
					}

					if (this.repositoryQuery.isPubmed()
							&& this.repositoryQuery.getPubmedDownloadTo() != Integer.MAX_VALUE) {
						pubmedDownloader.downloadPapers(true, this.repositoryQuery.isPdfToText(),
								this.repositoryQuery.isKeepPdf(), directoryType, downloadFrom,
								this.repositoryQuery.getPubmedDownloadTo());
					}
				}

				if (this.repositoryQuery.isAbstractPaper()) {
					final boolean directoryType = Boolean.valueOf(this.repositoryQuery.getGroupBy());
					if (this.repositoryQuery.isScopus()
							&& this.repositoryQuery.getScopusDownloadTo() != Integer.MAX_VALUE) {
						scopusDownloader.downloadPapers(false, this.repositoryQuery.isPdfToText(),
								this.repositoryQuery.isKeepPdf(), directoryType, downloadFrom,
								this.repositoryQuery.getScopusDownloadTo());
					}

					if (this.repositoryQuery.isPubmed()
							&& this.repositoryQuery.getPubmedDownloadTo() != Integer.MAX_VALUE) {
						pubmedDownloader.downloadPapers(false, this.repositoryQuery.isPdfToText(),
								this.repositoryQuery.isKeepPdf(), directoryType, downloadFrom,
								this.repositoryQuery.getPubmedDownloadTo());
					}
				}
			} catch (final Exception e) {
			}
		};
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
		final Task other = (Task) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public Task clone() {
		return new Task(this.id, this.hour, this.minutes, this.monday, this.tuesday, this.wednesday, this.thursday,
				this.friday, this.saturday, this.sunday, this.repositoryQuery);
	}

	@Override
	public int compareTo(final Task obj) {
		return Compare.objects(this, obj).by(Task::getId).thenBy(Task::getHour).thenBy(Task::getMinutes)
				.thenBy(Task::isMonday).thenBy(Task::isTuesday).thenBy(Task::isWednesday).thenBy(Task::isThursday)
				.thenBy(Task::isFriday).thenBy(Task::isSaturday).thenBy(Task::isSunday).andGet();
	}

	@Override
	public String toString() {
		return "Task [id=" + id + ", hour=" + hour + ", minutes=" + minutes + ", monday=" + monday + ", tuesday="
				+ tuesday + ", wednesday=" + wednesday + ", thursday=" + thursday + ", friday=" + friday + ", saturday="
				+ saturday + ", sunday=" + sunday + "]";
	}
	
	
}
