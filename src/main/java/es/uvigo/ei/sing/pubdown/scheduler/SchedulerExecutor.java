package es.uvigo.ei.sing.pubdown.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SchedulerExecutor {
	private static final int WEEK_DAYS = 7;
	private static final int WEEK_HOURS = WEEK_DAYS * 24;
	private static final int WEEKLY_PERIOD_IN_MINUTES = WEEK_HOURS * 60;
	private static final int DAY_HOURS = 24;
	private static final int DAILY_PERIOD_MINUTES = DAY_HOURS * 60;
	private static final int SECONDS = 0;

	public SchedulerExecutor() {
	}

	public void run(boolean daily, Map<String, Boolean> executionDays, int hourOfDay, int minutes) {
		// final SimpleDateFormat simpleDateFormat = new
		// SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		final Calendar actualCalendar = Calendar.getInstance();
		actualCalendar.setTime(new Date());
		actualCalendar.set(Calendar.SECOND, 0);

		final Calendar userCalendar = createCalendar(hourOfDay, minutes);

		if (daily) {
			long initialDelayInMinutes = getInitialDelayInMinutes(daily, actualCalendar, userCalendar);

			if (initialDelayInMinutes == DAILY_PERIOD_MINUTES) {
				userCalendar.set(Calendar.MINUTE, userCalendar.get(Calendar.MINUTE) + (int) initialDelayInMinutes);
			}
			// scheduleTask(scheduler, initialDelayInMinutes,
			// DAILY_PERIOD_MINUTES);

		} else {
			executionDays.forEach((day, toExecute) -> {
				if (toExecute) {
					Calendar calendarClone = (Calendar) userCalendar.clone();
					calendarClone.set(Calendar.DAY_OF_WEEK, getCalendarDay(day));

					long initialDelayInMinutes = getInitialDelayInMinutes(daily, actualCalendar, calendarClone);

					if (initialDelayInMinutes == WEEKLY_PERIOD_IN_MINUTES) {
						calendarClone.set(Calendar.MINUTE,
								calendarClone.get(Calendar.MINUTE) + (int) initialDelayInMinutes);
					}

					// scheduleTask(scheduler, initialDelayInMinutes,
					// WEEKLY_PERIOD_IN_MINUTES);
				}
			});

		}

	}

	// private void scheduleTask(final ScheduledExecutorService scheduler, long
	// initialDelayInMinutes, long finalDelay) {
	// scheduler.scheduleAtFixedRate(new Runnable() {
	// @Override
	// public void run() {
	// try {
	// System.out.println("Execution at fixed rate");
	// } catch (final Exception e) {
	// }
	// }
	// }, initialDelayInMinutes, finalDelay, TimeUnit.MINUTES);
	// }

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

	private int getCalendarDay(String day) {
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
