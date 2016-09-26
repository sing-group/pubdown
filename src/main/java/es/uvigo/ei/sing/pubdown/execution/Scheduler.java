package es.uvigo.ei.sing.pubdown.execution;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.entities.Task;

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

	// scheduled execution
//	synchronized public void scheduleTask(final String directoryPath, final Task task) {
//		final Integer taskId = task.getId();
//
//		if (taskReadyToBeScheduled(task) && !scheduledTasks.containsKey(taskId)) {
//
//			scheduledTasks.put(taskId, new LinkedList<ScheduledFuture<?>>());
//
//			if (Boolean.valueOf(task.getRepositoryQuery().getDaily())) {
//				final Runnable toRun = schedulerExecutor.getRunnableQuery(directoryPath, task);
//				final long initialDelay = schedulerExecutor.getDailyInitialDelay(task);
//				final ScheduledFuture<?> scheduledFuture = executor.scheduleAtFixedRate(toRun, initialDelay,
//						DAILY_PERIOD_MINUTES, TimeUnit.MINUTES);
//				final List<ScheduledFuture<?>> scheduledFutures = scheduledTasks.get(taskId);
//				scheduledFutures.add(scheduledFuture);
//				this.scheduledTasks.put(taskId, scheduledFutures);
//				taskNumber++;
//			} else {
//				if (taskReadyToBeScheduled(task)) {
//					final List<Long> initialDelays = schedulerExecutor.getWeeklyInitialDelay(task);
//					initialDelays.forEach((initialDelay) -> {
//						final Runnable toRun = schedulerExecutor.getRunnableQuery(directoryPath, task);
//						final ScheduledFuture<?> scheduledFuture = executor.scheduleAtFixedRate(toRun, initialDelay,
//								WEEKLY_PERIOD_IN_MINUTES, TimeUnit.MINUTES);
//
//						final List<ScheduledFuture<?>> scheduledFutures = scheduledTasks.get(taskId);
//						scheduledFutures.add(scheduledFuture);
//						this.scheduledTasks.put(taskId, scheduledFutures);
//						taskNumber++;
//					});
//				}
//			}
//			System.out.println("Task scheduled");
//		} else {
//			System.out.println("task not scheduled");
//		}
//
//		System.out.println("SCHEDULE TASK");
//		System.out.println("Scheduled tasks: " + taskNumber);
//	}
	
	synchronized public void scheduleTask(final Runnable runnable, final Task task) {
		final Integer taskId = task.getId();

		if (taskReadyToBeScheduled(task) && !scheduledTasks.containsKey(taskId)) {

			scheduledTasks.put(taskId, new LinkedList<ScheduledFuture<?>>());

			if (Boolean.valueOf(task.getRepositoryQuery().getDaily())) {
				final long initialDelay = schedulerExecutor.getDailyInitialDelay(task);
				System.out.println(initialDelay);
				final ScheduledFuture<?> scheduledFuture = executor.scheduleAtFixedRate(runnable, initialDelay,
						DAILY_PERIOD_MINUTES, TimeUnit.MINUTES);
				final List<ScheduledFuture<?>> scheduledFutures = scheduledTasks.get(taskId);
				scheduledFutures.add(scheduledFuture);
				this.scheduledTasks.put(taskId, scheduledFutures);
				taskNumber++;
			} else {
				if (taskReadyToBeScheduled(task)) {
					final List<Long> initialDelays = schedulerExecutor.getWeeklyInitialDelay(task);
					initialDelays.forEach((initialDelay) -> {
						final ScheduledFuture<?> scheduledFuture = executor.scheduleAtFixedRate(runnable, initialDelay,
								WEEKLY_PERIOD_IN_MINUTES, TimeUnit.MINUTES);

						final List<ScheduledFuture<?>> scheduledFutures = scheduledTasks.get(taskId);
						scheduledFutures.add(scheduledFuture);
						this.scheduledTasks.put(taskId, scheduledFutures);
						taskNumber++;
					});
				}
			}
			System.out.println("Task scheduled");
		} else {
			System.out.println("task not scheduled");
		}

		System.out.println("SCHEDULE TASK");
		System.out.println("Scheduled tasks: " + taskNumber);
	}

	synchronized public void removeTask(final Task task) {
		final List<ScheduledFuture<?>> scheduledFutures = this.scheduledTasks.remove(task.getId());
		scheduledFutures.forEach((scheduledFuture) -> {
			scheduledFuture.cancel(true);
			taskNumber--;
		});

		System.out.println("REMOVE TASK");
		System.out.println("Scheduled tasks: " + taskNumber);
	}

	

	// synchronized public void executeTask(final String directoryPath, final
	// Task task, final boolean isQueryResult) {
	// final Integer taskId = task.getId();
	// if (!scheduledTasks.containsKey(taskId)) {
	// scheduledTasks.put(taskId, new LinkedList<ScheduledFuture<?>>());
	// taskNumber++;
	//
	// } else {
	// removeTask(task);
	// scheduledTasks.remove(taskId);
	// scheduledTasks.put(taskId, new LinkedList<ScheduledFuture<?>>());
	// }
	//
	// final Runnable toRun = isQueryResult ?
	// schedulerExecutor.getRepositoryQueryResult(directoryPath, task)
	// : schedulerExecutor.getRunnableQuery(directoryPath, task);
	//
	// final ScheduledFuture<?> scheduledFuture = executor.schedule(toRun, 0,
	// TimeUnit.SECONDS);
	//
	// final List<ScheduledFuture<?>> scheduledFutures =
	// scheduledTasks.get(taskId);
	// scheduledFutures.add(scheduledFuture);
	// this.scheduledTasks.put(taskId, scheduledFutures);
	//
	// System.out.println("EXECUTE TASK");
	// System.out.println("Scheduled tasks: " + taskNumber);
	// }

	synchronized public void executeTask(final Runnable runnable, final Task task) {
		executor.schedule(runnable, 0, TimeUnit.SECONDS);
		
		// final Integer taskId = task.getId();
		// if (!scheduledTasks.containsKey(taskId)) {
		// scheduledTasks.put(taskId, new LinkedList<ScheduledFuture<?>>());
		//
		// } else {
		// removeTask(task);
		// scheduledTasks.remove(taskId);
		// scheduledTasks.put(taskId, new LinkedList<ScheduledFuture<?>>());
		// }
		// taskNumber++;

		// final Runnable toRun = isQueryResult ?
		// schedulerExecutor.getRepositoryQueryResult(directoryPath, task)
		// : schedulerExecutor.getRunnableQuery(directoryPath, task);

		// final ScheduledFuture<?> scheduledFuture =
		// executor.schedule(runnable, 0, TimeUnit.SECONDS);

		// final List<ScheduledFuture<?>> scheduledFutures =
		// scheduledTasks.get(taskId);
		// scheduledFutures.add(scheduledFuture);
		// this.scheduledTasks.put(taskId, scheduledFutures);
		//
		// System.out.println("EXECUTE TASK");
		// System.out.println("Scheduled tasks: " + taskNumber);
	}

	// // execute right now
	// synchronized public void executeTask(final String directoryPath, final
	// Task task) {
	// final Integer taskId = task.getId();
	// if (taskReadyToBeScheduled(task) && !scheduledTasks.containsKey(taskId))
	// {
	//
	// scheduledTasks.put(taskId, new LinkedList<ScheduledFuture<?>>());
	//
	// final Runnable toRun = schedulerExecutor.getRunnableQuery(directoryPath,
	// task);
	// final ScheduledFuture<?> scheduledFuture = executor.schedule(toRun, 0,
	// TimeUnit.SECONDS);
	//
	// final List<ScheduledFuture<?>> scheduledFutures =
	// scheduledTasks.get(taskId);
	// scheduledFutures.add(scheduledFuture);
	// this.scheduledTasks.put(taskId, scheduledFutures);
	// }
	// }
	//
	// //execute right now the
	// synchronized public void executeQueryResultSize(final String
	// directoryPath, final Task task) {
	// this.executor.submit(schedulerExecutor.getRepositoryQueryResult(directoryPath,
	// task));
	// }

	synchronized public boolean isScheduled(final Task task) {
		return scheduledTasks.containsKey(task.getId());
	}

	public Map<Integer, List<ScheduledFuture<?>>> getScheduledTasks() {
		return scheduledTasks;
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

	private boolean taskReadyToBeScheduled(final Task task) {
		final RepositoryQuery repositoryQuery = task.getRepositoryQuery();
		return (repositoryQuery.getPubmedDownloadTo() != 0
				&& repositoryQuery.getPubmedDownloadTo() != Integer.MAX_VALUE)
				|| (repositoryQuery.getScopusDownloadTo() != 0
						&& repositoryQuery.getScopusDownloadTo() != Integer.MAX_VALUE);
	}

}
