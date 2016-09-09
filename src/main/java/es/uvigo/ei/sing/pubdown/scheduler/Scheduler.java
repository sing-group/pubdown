package es.uvigo.ei.sing.pubdown.scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import es.uvigo.ei.sing.pubdown.web.entities.Task;

public class Scheduler {
	private static final int SCHEDULED_THREAD_POOL_SIZE = 10;

	private final static Scheduler SINGLETON = new Scheduler();

	public static Scheduler getSingleton() {
		return SINGLETON;
	}

	private ScheduledExecutorService executor;

	private Scheduler() {
		this.executor = Executors.newScheduledThreadPool(SCHEDULED_THREAD_POOL_SIZE);
	}

	private Map<Integer, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

	synchronized public void scheduleTask(Task task) {
		int initialDelay = 0;
		int period = 0;

		ScheduledFuture<?> scheduledFuture = executor.scheduleAtFixedRate(task.getRunnableQuery(), initialDelay, period,
				TimeUnit.MINUTES);

		this.scheduledTasks.put(task.getId(), scheduledFuture);

	}

	synchronized public void unScheduleTask(Task task) {
		ScheduledFuture<?> scheduledFuture = this.scheduledTasks.remove(task.getId());
		scheduledFuture.cancel(true);
	}

	public void execute(Task task) {
		this.executor.submit(task.getRunnableQuery());
	}

	public Map<Integer, ScheduledFuture<?>> getScheduledTasks() {
		return this.scheduledTasks;
	}

	public boolean isScheduled(Task task) {
		return scheduledTasks.containsKey(task.getId());
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

}
