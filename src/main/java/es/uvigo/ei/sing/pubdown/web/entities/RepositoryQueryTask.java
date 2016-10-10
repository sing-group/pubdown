package es.uvigo.ei.sing.pubdown.web.entities;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import es.uvigo.ei.sing.pubdown.util.Compare;

@Entity(name = "RepositoryQueryTask")
public class RepositoryQueryTask implements Cloneable, Comparable<RepositoryQueryTask> {

	private static final String FREQUENCY_DAILY = "daily";

	private static final String FREQUENCY_WEEKLY = "weekly";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Basic
	private int hour;

	@Basic
	private int minutes;

	@Basic
	private boolean daily = true;

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

	public RepositoryQueryTask() {
	}

	public RepositoryQueryTask(final int hour, final int minutes, final boolean daily, final boolean monday,
			final boolean tuesday, final boolean wednesday, final boolean thursday, final boolean friday,
			final boolean saturday, final boolean sunday, final RepositoryQuery repositoryQuery) {
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

	private RepositoryQueryTask(final Integer id, final int hour, final int minutes, final boolean daily,
			final boolean monday, final boolean tuesday, final boolean wednesday, final boolean thursday,
			final boolean friday, final boolean saturday, final boolean sunday) {
		super();
		this.id = id;
		this.hour = hour;
		this.minutes = minutes;
		this.daily = daily;
		this.monday = monday;
		this.tuesday = tuesday;
		this.wednesday = wednesday;
		this.thursday = thursday;
		this.friday = friday;
		this.saturday = saturday;
		this.sunday = sunday;
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

	public void setExecutionFrequency(final String frequency) {
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

	public boolean isMonday() {
		return monday;
	}

	public void setMonday(final boolean monday) {
		this.monday = monday;
	}

	public boolean isTuesday() {
		return tuesday;
	}

	public void setTuesday(final boolean tuesday) {
		this.tuesday = tuesday;
	}

	public boolean isWednesday() {
		return wednesday;
	}

	public void setWednesday(final boolean wednesday) {
		this.wednesday = wednesday;
	}

	public boolean isThursday() {
		return thursday;
	}

	public void setThursday(final boolean thursday) {
		this.thursday = thursday;
	}

	public boolean isFriday() {
		return friday;
	}

	public void setFriday(final boolean friday) {
		this.friday = friday;
	}

	public boolean isSaturday() {
		return saturday;
	}

	public void setSaturday(final boolean saturday) {
		this.saturday = saturday;
	}

	public boolean isSunday() {
		return sunday;
	}

	public void setSunday(final boolean sunday) {
		this.sunday = sunday;
	}

	public RepositoryQuery getRepositoryQuery() {
		return repositoryQuery;
	}

	public void setRepositoryQuery(final RepositoryQuery repositoryQuery) {
		this.repositoryQuery = repositoryQuery;
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
		final RepositoryQueryTask other = (RepositoryQueryTask) obj;
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
	public RepositoryQueryTask clone() {
		return new RepositoryQueryTask(this.id, this.hour, this.minutes, this.daily, this.monday, this.tuesday,
				this.wednesday, this.thursday, this.friday, this.saturday, this.sunday);
	}

	@Override
	public int compareTo(final RepositoryQueryTask obj) {
		return Compare.objects(this, obj).by(RepositoryQueryTask::getId)
				.thenBy(RepositoryQueryTask::getHour)
				.thenBy(RepositoryQueryTask::getMinutes)
				.thenBy(RepositoryQueryTask::isDaily)
				.thenBy(RepositoryQueryTask::isMonday)
				.thenBy(RepositoryQueryTask::isTuesday)
				.thenBy(RepositoryQueryTask::isWednesday)
				.thenBy(RepositoryQueryTask::isThursday)
				.thenBy(RepositoryQueryTask::isFriday)
				.thenBy(RepositoryQueryTask::isSaturday)
				.thenBy(RepositoryQueryTask::isSunday)
			.andGet();
	}

}
