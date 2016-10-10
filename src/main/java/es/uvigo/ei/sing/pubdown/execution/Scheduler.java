package es.uvigo.ei.sing.pubdown.execution;

import static es.uvigo.ei.sing.pubdown.execution.GlobalEvents.EVENT_REPOSITORY_QUERY;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;

import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQueryTask;
import es.uvigo.ei.sing.pubdown.web.entities.User;

public class Scheduler {

	private static final int SCHEDULED_THREAD_POOL_SIZE = 30;

	private static final int WEEK_DAYS = 7;
	private static final int WEEK_HOURS = WEEK_DAYS * 24;
	private static final int WEEKLY_PERIOD_IN_MINUTES = WEEK_HOURS * 60;
	private static final int DAY_HOURS = 24;
	private static final int DAILY_PERIOD_MINUTES = DAY_HOURS * 60;

	private final static Scheduler SINGLETON = new Scheduler();

	public static Scheduler getSingleton() {
		return SINGLETON;
	}

	private final Map<Integer, List<ScheduledFuture<?>>> scheduledTasks = new HashMap<>();
	private final SchedulerExecutor schedulerExecutor = new SchedulerExecutor();
	private final ScheduledExecutorService executor;
	private int taskNumber = 0;

	private Scheduler() {
		this.executor = Executors.newScheduledThreadPool(SCHEDULED_THREAD_POOL_SIZE);
	}

	// synchronized public void scheduleTask(final Runnable runnable, final
	// RepositoryQueryTask task) {
	// final Integer taskId = task.getId();
	//
	// if (taskReadyToBeScheduled(task) && !scheduledTasks.containsKey(taskId))
	// {
	//
	// scheduledTasks.put(taskId, new LinkedList<ScheduledFuture<?>>());
	//
	// if (task.isDaily()) {
	// final long initialDelay = schedulerExecutor.getDailyInitialDelay(task);
	// System.out.println("MINUTES TO START: " + initialDelay);
	// final ScheduledFuture<?> scheduledFuture =
	// executor.scheduleAtFixedRate(runnable, initialDelay,
	// DAILY_PERIOD_MINUTES, TimeUnit.MINUTES);
	// final List<ScheduledFuture<?>> scheduledFutures =
	// scheduledTasks.get(taskId);
	// scheduledFutures.add(scheduledFuture);
	// this.scheduledTasks.put(taskId, scheduledFutures);
	// taskNumber++;
	// } else {
	// if (taskReadyToBeScheduled(task)) {
	// final List<Long> initialDelays =
	// schedulerExecutor.getWeeklyInitialDelay(task);
	// initialDelays.forEach((initialDelay) -> {
	// final ScheduledFuture<?> scheduledFuture =
	// executor.scheduleAtFixedRate(runnable, initialDelay,
	// WEEKLY_PERIOD_IN_MINUTES, TimeUnit.MINUTES);
	//
	// final List<ScheduledFuture<?>> scheduledFutures =
	// scheduledTasks.get(taskId);
	// scheduledFutures.add(scheduledFuture);
	// this.scheduledTasks.put(taskId, scheduledFutures);
	// taskNumber++;
	// });
	// }
	// }
	// System.out.println("TASK SCHEDULED");
	// } else {
	// System.out.println("TASK NOT SCHEDULED");
	// }
	//
	// System.out.println("Scheduled tasks: " + taskNumber);
	// }

	synchronized public void scheduleTask(final RepositoryQueryScheduled repositoryQueryScheduled) {
		final RepositoryQuery repositoryQuery = repositoryQueryScheduled.getRepositoryQuery();
		final RepositoryQueryTask task = repositoryQuery.getTask();
		final Integer taskId = task.getId();
		final TaskExecutor taskExecutor = new TaskExecutor(repositoryQueryScheduled);

		if (taskReadyToBeScheduled(repositoryQuery) && !scheduledTasks.containsKey(taskId)) {

			scheduledTasks.put(taskId, new LinkedList<ScheduledFuture<?>>());

			if (task.isDaily()) {
				final long initialDelay = schedulerExecutor.getDailyInitialDelay(task);
				System.out.println("Daily - minutes to start: " + initialDelay);
				final ScheduledFuture<?> scheduledFuture = executor.scheduleAtFixedRate(taskExecutor, initialDelay,
						DAILY_PERIOD_MINUTES, TimeUnit.MINUTES);
				final List<ScheduledFuture<?>> scheduledFutures = scheduledTasks.get(taskId);
				scheduledFutures.add(scheduledFuture);
				this.scheduledTasks.put(taskId, scheduledFutures);
				taskNumber++;
			} else {
				if (taskReadyToBeScheduled(repositoryQuery)) {
					final List<Long> initialDelays = schedulerExecutor.getWeeklyInitialDelay(task);
					initialDelays.forEach((initialDelay) -> {
						System.out.println("Weekly - minutes to start: " + initialDelay);
						final ScheduledFuture<?> scheduledFuture = executor.scheduleAtFixedRate(taskExecutor,
								initialDelay, WEEKLY_PERIOD_IN_MINUTES, TimeUnit.MINUTES);

						final List<ScheduledFuture<?>> scheduledFutures = scheduledTasks.get(taskId);
						scheduledFutures.add(scheduledFuture);
						this.scheduledTasks.put(taskId, scheduledFutures);
						taskNumber++;
					});
				}
			}
			System.out.println("TASK SCHEDULED");
		} else {
			System.out.println("TASK NOT SCHEDULED");
		}

		System.out.println("Scheduled tasks: " + taskNumber);
	}

	synchronized public void removeTask(final RepositoryQueryScheduled repositoryQueryScheduled) {
		final RepositoryQueryTask task = repositoryQueryScheduled.getRepositoryQuery().getTask();
		if (scheduledTasks.containsKey(task.getId())) {
			final List<ScheduledFuture<?>> scheduledFutures = this.scheduledTasks.remove(task.getId());
			scheduledFutures.forEach((scheduledFuture) -> {
				scheduledFuture.cancel(true);
				taskNumber--;
			});

			System.out.println("TASK REMOVED");
			System.out.println("Scheduled tasks: " + taskNumber);
		}
	}

	// synchronized public void executeTask(final Runnable runnable, final
	// RepositoryQueryTask task) {
	// executor.submit(runnable);
	// }

	synchronized public void executeTask(final RepositoryQueryScheduled repositoryQueryScheduled) {
		final TaskExecutor taskExecutor = new TaskExecutor(repositoryQueryScheduled);
		executor.submit(taskExecutor);
	}

	synchronized public boolean isScheduled(final RepositoryQueryScheduled repositoryQueryScheduled) {
		final RepositoryQueryTask task = repositoryQueryScheduled.getRepositoryQuery().getTask();
		return scheduledTasks.containsKey(task.getId());
	}

	public Map<Integer, List<ScheduledFuture<?>>> getScheduledTasks() {
		return scheduledTasks;
	}
	
	public int getTaskNumber() {
		return taskNumber;
	}

	public void shutdown() {
		this.executor.shutdownNow();
		try {
			this.executor.awaitTermination(10l, TimeUnit.SECONDS);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} finally {
			this.scheduledTasks.clear();
		}
	}

	private boolean taskReadyToBeScheduled(final RepositoryQuery repositoryQuery) {
		return (repositoryQuery.getPubmedDownloadTo() != 0
				&& repositoryQuery.getPubmedDownloadTo() != Integer.MAX_VALUE)
				|| (repositoryQuery.getScopusDownloadTo() != 0
						&& repositoryQuery.getScopusDownloadTo() != Integer.MAX_VALUE);
	}

	private final static void publishEvent(final RepositoryQueryScheduled repositoryQueryScheduled, final String suffix,
			final Object value) {
		final User user = repositoryQueryScheduled.getRepositoryQuery().getRepository().getUser();
		final EventQueue<Event> queue = EventQueueUtils.getUserQueue(user);

		if (suffix == null) {
			queue.publish(
					new Event(EVENT_REPOSITORY_QUERY, null, new EventRepositoryQuery(repositoryQueryScheduled, value)));
		} else {
			queue.publish(new Event(EVENT_REPOSITORY_QUERY + suffix, null,
					new EventRepositoryQuery(repositoryQueryScheduled, value)));
		}
	}

	private final class TaskExecutor implements Runnable {

		private RepositoryQueryScheduled repositoryQueryScheduled;

		public TaskExecutor(RepositoryQueryScheduled repositoryQueryScheduled) {
			this.repositoryQueryScheduled = repositoryQueryScheduled;
		}

		@Override
		public void run() {
			final boolean toCheck = repositoryQueryScheduled.isToCheck();
			publishEvent(repositoryQueryScheduled, GlobalEvents.SUFFIX_STARTED, toCheck);
			if (toCheck) {
				System.out.println("EXECUTING RESULT SIZE");
				repositoryQueryScheduled.getResultSize();
			} else {
				System.out.println("EXECUTING GET PAPERS");
				repositoryQueryScheduled.getPapers();
			}
			publishEvent(repositoryQueryScheduled, GlobalEvents.SUFFIX_FINISHED, toCheck);
		}

	}

}
