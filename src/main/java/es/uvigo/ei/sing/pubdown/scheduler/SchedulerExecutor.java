package es.uvigo.ei.sing.pubdown.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import es.uvigo.ei.sing.pubdown.paperdown.downloader.pubmed.PubMedDownloader;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.scopus.ScopusDownloader;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.entities.Task;
import es.uvigo.ei.sing.pubdown.web.zk.util.CleanEntityManagerTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;

public class SchedulerExecutor {
	private final TransactionManager tm = new CleanEntityManagerTransactionManager();

	private static final int WEEK_DAYS = 7;
	private static final int WEEK_HOURS = WEEK_DAYS * 24;
	private static final int WEEKLY_PERIOD_IN_MINUTES = WEEK_HOURS * 60;
	private static final int DAY_HOURS = 24;
	private static final int DAILY_PERIOD_MINUTES = DAY_HOURS * 60;
	private static final int SECONDS = 0;

	private static final int DOWNLOAD_FROM = 0;

	public SchedulerExecutor() {
	}

	public Runnable getRunnableQuery(final String directoryPath, final Task task) {
		return () -> {
			final RepositoryQuery repositoryQuery = task.getRepositoryQuery();

			ScopusDownloader scopusDownloader = new ScopusDownloader();
			PubMedDownloader pubmedDownloader = new PubMedDownloader();
			boolean scopusReady = false;
			boolean pubmedReady = false;

			final String query = repositoryQuery.getQuery().replace(" ", "+");

			final String scopusApiKey = repositoryQuery.getUser().getApiKey();

			if (repositoryQuery.isScopus() && repositoryQuery.getScopusDownloadTo() != 0
					&& repositoryQuery.getScopusDownloadTo() != Integer.MAX_VALUE) {
				scopusDownloader = new ScopusDownloader(query, scopusApiKey,
						directoryPath + repositoryQuery.getDirectory());
				scopusReady = true;
			}

			if (repositoryQuery.isPubmed() && repositoryQuery.getPubmedDownloadTo() != 0
					&& repositoryQuery.getPubmedDownloadTo() != Integer.MAX_VALUE) {
				pubmedDownloader = new PubMedDownloader(query, directoryPath + repositoryQuery.getDirectory());
				pubmedReady = true;
			}

			if (repositoryQuery.isFulltextPaper()) {
				final boolean directoryType = Boolean.valueOf(repositoryQuery.getGroupBy());
				if (repositoryQuery.isScopus() && scopusReady) {
					scopusDownloader.downloadPapers(true, repositoryQuery.isPdfToText(), repositoryQuery.isKeepPdf(),
							directoryType, DOWNLOAD_FROM, repositoryQuery.getScopusDownloadTo());
				}

				if (repositoryQuery.isPubmed() && pubmedReady) {
					pubmedDownloader.downloadPapers(true, repositoryQuery.isPdfToText(), repositoryQuery.isKeepPdf(),
							directoryType, DOWNLOAD_FROM, repositoryQuery.getPubmedDownloadTo());
				}
			}

			if (repositoryQuery.isAbstractPaper()) {
				final boolean directoryType = Boolean.valueOf(repositoryQuery.getGroupBy());
				if (repositoryQuery.isScopus() && scopusReady) {
					scopusDownloader.downloadPapers(false, repositoryQuery.isPdfToText(), repositoryQuery.isKeepPdf(),
							directoryType, DOWNLOAD_FROM, repositoryQuery.getScopusDownloadTo());
				}
				if (repositoryQuery.isPubmed() && pubmedReady) {
					pubmedDownloader.downloadPapers(false, repositoryQuery.isPdfToText(), repositoryQuery.isKeepPdf(),
							directoryType, DOWNLOAD_FROM, repositoryQuery.getPubmedDownloadTo());
				}

			}
		};
	}

	public Runnable getRepositoryQueryResult(final String directoryPath, final RepositoryQuery repositoryQuery) {
		return () -> {
			ScopusDownloader scopusDownloader = new ScopusDownloader();
			PubMedDownloader pubmedDownloader = new PubMedDownloader();

			int scopusResult = 0;
			int pubmedResult = 0;
			int scopusDownloadTo = 0;
			int pubmedDownloadTo = 0;

			final String query = repositoryQuery.getQuery().replace(" ", "+");

			final String scopusApiKey = repositoryQuery.getUser().getApiKey();

			if (repositoryQuery.isScopus()) {
				scopusDownloader = new ScopusDownloader(query, scopusApiKey,
						directoryPath + repositoryQuery.getDirectory());

				if (repositoryQuery.getScopusDownloadTo() == Integer.MAX_VALUE) {
					scopusResult = scopusDownloader.getResultSize();
					System.out.println("Scopus real result = " + scopusResult);
					if (scopusResult != 0) {
						if (scopusResult > 6000) {
							scopusDownloadTo = 6000;
						}

						// limit to 100 to improve performance
						scopusDownloadTo = 100;
						repositoryQuery.setScopusDownloadTo(scopusDownloadTo);

						tm.runInTransaction(em -> {
							em.merge(repositoryQuery);
						});
					}
				}
			}

			if (repositoryQuery.isPubmed()) {
				pubmedDownloader = new PubMedDownloader(query, directoryPath + repositoryQuery.getDirectory());
				if (repositoryQuery.getPubmedDownloadTo() == Integer.MAX_VALUE) {
					pubmedResult = pubmedDownloader.getResultSize();
					System.out.println("PubMed real result = " + pubmedResult);
					if (pubmedResult != 0) {
						// limit to 100 to improve performance
						pubmedDownloadTo = 100;
						repositoryQuery.setPubmedDownloadTo(pubmedDownloadTo);

						tm.runInTransaction(em -> {
							em.merge(repositoryQuery);
						});
					}
				}
			}
			repositoryQuery.setChecked(true);
		};
	}

	public long getDailyInitialDelay(final Task task) {

		final int hourOfDay = task.getHour();
		final int minutes = task.getMinutes();

		final Calendar actualCalendar = Calendar.getInstance();
		actualCalendar.setTime(new Date());
		actualCalendar.set(Calendar.SECOND, 0);

		final Calendar userCalendar = createCalendar(hourOfDay, minutes);

		final long initialDelayInMinutes = getInitialDelayInMinutes(true, actualCalendar, userCalendar);

		if (initialDelayInMinutes == DAILY_PERIOD_MINUTES) {
			userCalendar.set(Calendar.MINUTE, userCalendar.get(Calendar.MINUTE) + (int) initialDelayInMinutes);
		}

		return initialDelayInMinutes;
	}

	public List<Long> getWeeklyInitialDelay(final Task task) {
		final int hourOfDay = task.getHour();
		final int minutes = task.getMinutes();

		final Map<String, Boolean> executionDays = new HashMap<>();
		executionDays.put("MONDAY", task.isMonday());
		executionDays.put("TUESDAY", task.isTuesday());
		executionDays.put("WEDNESDAY", task.isWednesday());
		executionDays.put("THURSDAY", task.isThursday());
		executionDays.put("FRIDAY", task.isFriday());
		executionDays.put("SATURDAY", task.isSaturday());
		executionDays.put("SUNDAY", task.isSunday());

		List<Long> initialDelays = new LinkedList<>();

		final Calendar actualCalendar = Calendar.getInstance();
		actualCalendar.setTime(new Date());
		actualCalendar.set(Calendar.SECOND, 0);

		final Calendar userCalendar = createCalendar(hourOfDay, minutes);

		executionDays.forEach((day, toExecute) -> {
			if (toExecute) {
				final Calendar calendarClone = (Calendar) userCalendar.clone();
				calendarClone.set(Calendar.DAY_OF_WEEK, getCalendarDay(day));

				final long initialDelayInMinutes = getInitialDelayInMinutes(false, actualCalendar, calendarClone);

				if (initialDelayInMinutes == WEEKLY_PERIOD_IN_MINUTES) {
					calendarClone.set(Calendar.MINUTE,
							calendarClone.get(Calendar.MINUTE) + (int) initialDelayInMinutes);
				}

				initialDelays.add(initialDelayInMinutes);
			}
		});

		return initialDelays;

	}

	private long getInitialDelayInMinutes(final boolean daily, final Calendar actualCalendar,
			final Calendar userCalendar) {
		final Date actualDate = actualCalendar.getTime();
		final Date userDate = userCalendar.getTime();

		long delayInMinutes = TimeUnit.MILLISECONDS.toMinutes((userDate.getTime() - actualDate.getTime()));

		if (daily) {

			if (delayInMinutes == DAILY_PERIOD_MINUTES) {
				delayInMinutes = 0;
			}

			if (delayInMinutes < 0) {
				delayInMinutes = DAILY_PERIOD_MINUTES;
			}

		} else {

			if (delayInMinutes == WEEKLY_PERIOD_IN_MINUTES) {
				delayInMinutes = 0;
			}

			if (delayInMinutes < 0) {
				delayInMinutes = WEEKLY_PERIOD_IN_MINUTES;
			}
		}

		return delayInMinutes;
	}

	private Calendar createCalendar(final int hourOfDay, final int minutes) {
		final Calendar userCalendar = Calendar.getInstance();
		userCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		userCalendar.set(Calendar.MINUTE, minutes);
		userCalendar.set(Calendar.SECOND, SECONDS);
		return userCalendar;
	}

	private int getCalendarDay(final String day) {
		int calendarDay = 0;
		switch (day.toUpperCase()) {
		case "MONDAY":
			calendarDay = Calendar.MONDAY;
			break;
		case "TUESDAY":
			calendarDay = Calendar.TUESDAY;
			break;
		case "WEDNESDAY":
			calendarDay = Calendar.WEDNESDAY;
			break;
		case "THURSDAY":
			calendarDay = Calendar.THURSDAY;
			break;
		case "FRIDAY":
			calendarDay = Calendar.FRIDAY;
			break;
		case "SATURDAY":
			calendarDay = Calendar.SATURDAY;
			break;
		case "SUNDAY":
			calendarDay = Calendar.SUNDAY;
			break;
		}
		return calendarDay;
	}
}
