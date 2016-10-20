package es.uvigo.ei.sing.pubdown.execution;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQueryTask;

public class Scheduler {
	private static final int WEEK_DAYS = 7;
	private static final int WEEK_HOURS = WEEK_DAYS * 24;
	private static final int WEEKLY_PERIOD_IN_MINUTES = WEEK_HOURS * 60;
	private static final int DAY_HOURS = 24;
	private static final int DAILY_PERIOD_MINUTES = DAY_HOURS * 60;
	private static final int SECONDS = 0;

	public Scheduler() {
	}

	public long getDailyInitialDelay(final RepositoryQueryTask task) {

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

	public List<Long> getWeeklyInitialDelay(final RepositoryQueryTask task) {
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

		final List<Long> initialDelays = new LinkedList<>();

		final Calendar actualCalendar = Calendar.getInstance();
		actualCalendar.setTime(new Date());
		actualCalendar.set(Calendar.SECOND, 0);

		final Calendar userCalendar = createCalendar(hourOfDay, minutes);

		executionDays.forEach((nameOfDay, isToExecute) -> {
			if (isToExecute) {
				final Calendar calendarClone = (Calendar) userCalendar.clone();
				calendarClone.set(Calendar.DAY_OF_WEEK, getCalendarDay(nameOfDay));

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
				delayInMinutes = DAILY_PERIOD_MINUTES + delayInMinutes;
			}
		} else {
			if (delayInMinutes == WEEKLY_PERIOD_IN_MINUTES) {
				delayInMinutes = 0;
			}

			if (delayInMinutes < 0) {
				delayInMinutes = WEEKLY_PERIOD_IN_MINUTES + delayInMinutes;
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
