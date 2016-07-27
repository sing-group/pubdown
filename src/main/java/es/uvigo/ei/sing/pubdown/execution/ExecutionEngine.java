package es.uvigo.ei.sing.pubdown.execution;

import static java.util.Collections.unmodifiableList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;

import es.uvigo.ei.sing.pubdown.execution.Task.TaskStatus;
import es.uvigo.ei.sing.pubdown.util.Mailer;
import es.uvigo.ei.sing.pubdown.web.entities.RobotExecution;
import es.uvigo.ei.sing.pubdown.web.entities.Role;
import es.uvigo.ei.sing.pubdown.web.entities.User;
import es.uvigo.ei.sing.pubdown.web.zk.util.CleanEntityManagerTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.vm.robot.RobotExecutionTask;

public class ExecutionEngine {
	private static TransactionManager tm = new CleanEntityManagerTransactionManager();

	private final static ExecutionEngine SINGLETON = new ExecutionEngine();

	public static ExecutionEngine getSingleton() {
		return SINGLETON;
	}

	public static final int EXPERIMENTS_POOL_SIZE = 10;
	public static final int EXPERIMENTS_MAX_POOL_SIZE = 10;

	private final ConcurrentMap<String, List<Task<?>>> userTasks;
	private final Map<Task<?>, Set<SubtaskExecutor<?>>> taskExecutors;
	private final Map<SubtaskExecutor<?>, RunnableFuture<?>> subtaskFutures;
	private final ThreadPoolExecutor executor;
	private final CustomBlockingQueue workQueue;

	private final Mailer mailer = new Mailer();

	private ExecutionEngine() {
		this.userTasks = new ConcurrentHashMap<>();
		this.taskExecutors = new HashMap<>();
		this.subtaskFutures = new Hashtable<>();

		this.workQueue = new CustomBlockingQueue();
		this.executor = new ThreadPoolExecutor(EXPERIMENTS_POOL_SIZE, EXPERIMENTS_MAX_POOL_SIZE, Long.MAX_VALUE,
				TimeUnit.DAYS, this.workQueue);
	}

	public <T> void execute(final Task<T> task) throws IllegalStateException {
		if (this.executor.isShutdown())
			throw new IllegalStateException("Executor is shutted down");

		final String userId = task.getUserId();

		synchronized (this.userTasks) {
			if (!this.userTasks.containsKey(userId))
				this.userTasks.putIfAbsent(userId, new LinkedList<>());
			this.userTasks.get(userId).add(task);

			final Set<SubtaskExecutor<?>> subtasks = new HashSet<>();
			this.taskExecutors.put(task, subtasks);

			this.workQueue.beginBatchAdd(userId);
			try {
				synchronized (task) {
					for (final Subtask<T> subtask : task.getSubtasks()) {
						final SubtaskExecutor<T> subtaskExecutor = new SubtaskExecutor<>(subtask);

						final RunnableFuture<?> runnableFuture = (RunnableFuture<?>) this.executor
								.submit(subtaskExecutor);
						this.subtaskFutures.put(subtaskExecutor, runnableFuture);
						subtasks.add(subtaskExecutor);
					}

					task.schedule();
					ExecutionEngine.publishEvent(task, GlobalEvents.SUFFIX_SCHEDULED, task);
				}
			} finally {
				this.workQueue.endBatchAdd();
			}
		}
	}

	private final static void publishEvent(final Task<?> task, final String suffix, final Object value) {
		final EventQueue<Event> queue = EventQueueUtils.getUserQueue(task.getUserId());

		if (suffix == null) {
			queue.publish(new Event(task.getTaskId(), null, new EventTaskData(task, value)));
		} else {
			queue.publish(new Event(task.getTaskId() + suffix, null, new EventTaskData(task, value)));
		}

		for (final User user : getUserAdmins()) {
			final EventQueue<Event> adminQueue = EventQueueUtils.getUserQueue(user.getLogin());
			if (adminQueue != null) {
				if (suffix == null) {
					adminQueue.publish(new Event(task.getTaskId(), null, new EventTaskData(task, value)));
				} else {
					adminQueue.publish(new Event(task.getTaskId() + suffix, null, new EventTaskData(task, value)));
				}
			}
		}
	}

	private static List<User> getUserAdmins() {
		return tm.get(em -> em.createQuery("SELECT u FROM User u WHERE u.role =:role", User.class)
				.setParameter("role", Role.ADMIN).getResultList());
	}

	public void shutdown() {
		synchronized (this.userTasks) {
			this.userTasks.values().stream().flatMap(List::stream).forEach(Task::abort);

			this.executor.shutdownNow();
			try {
				this.executor.awaitTermination(10l, TimeUnit.SECONDS);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			} finally {
				this.userTasks.clear();
				this.taskExecutors.clear();
				this.subtaskFutures.clear();
			}
		}
	}

	public List<Task<?>> getUserTask(final String login) {
		return unmodifiableList(this.userTasks.get(login));
	}

	public boolean hasTaskRunning(final String login) {
		return this.userTasks.containsKey(login);
	}

	public void cancelUserTask(final String login, final RobotExecution robotExecution) {
		if (doesUserHaveTasks(login)) {
			synchronized (this.userTasks) {
				if (doesUserHaveTasks(login)) {
					for (final Task<?> userTask : this.userTasks.get(login)) {
						if (userTask instanceof RobotExecutionTask) {
							final RobotExecutionTask robotExecutionTask = (RobotExecutionTask) userTask;
							if (robotExecutionTask.getRobotExecution().equals(robotExecution)) {
								final int indexOf = this.userTasks.get(login).indexOf(robotExecutionTask);
								this.userTasks.get(login).remove(indexOf);

								synchronized (robotExecutionTask) {
									if (!robotExecutionTask.isFinished()) {
										robotExecutionTask.abort();

										final List<Subtask<?>> unfinished = new ArrayList<>();

										this.workQueue.beginBatchRemove();

										try {
											for (final SubtaskExecutor<?> subtaskExecutor : this.taskExecutors
													.remove(robotExecutionTask)) {
												if (this.executor.remove(this.subtaskFutures.remove(subtaskExecutor))) {
													unfinished.add(subtaskExecutor.getSubtask());
												}
											}
										} finally {
											this.workQueue.endBatchRemove();
										}

										robotExecutionTask.postFinish();
										ExecutionEngine.publishEvent(robotExecutionTask, GlobalEvents.SUFFIX_ABORTED,
												unfinished);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public void cancelUserTasks(final String login) {
		if (doesUserHaveTasks(login)) {
			synchronized (this.userTasks) {
				if (doesUserHaveTasks(login)) {
					for (final Task<?> userTask : this.userTasks.get(login)) {
						if (userTask instanceof RobotExecutionTask) {
							final RobotExecutionTask robotExecutionTask = (RobotExecutionTask) userTask;

							synchronized (robotExecutionTask) {
								if (!robotExecutionTask.isFinished()) {
									robotExecutionTask.abort();

									final List<Subtask<?>> unfinished = new ArrayList<>();

									this.workQueue.beginBatchRemove();

									try {
										for (final SubtaskExecutor<?> subtaskExecutor : this.taskExecutors
												.remove(robotExecutionTask)) {
											if (this.executor.remove(this.subtaskFutures.remove(subtaskExecutor))) {
												unfinished.add(subtaskExecutor.getSubtask());
											}
										}
									} finally {
										this.workQueue.endBatchRemove();
									}

									robotExecutionTask.postFinish();
									ExecutionEngine.publishEvent(robotExecutionTask, GlobalEvents.SUFFIX_ABORTED,
											unfinished);
								}
							}
						}
					}
					this.userTasks.get(login).clear();
				}
			}
		}
	}

	public void cancelAllTasks() {
		synchronized (this.userTasks) {
			this.userTasks.forEach((login, userTask) -> {
				this.cancelUserTasks(login);
			});
		}
	}

	private boolean doesUserHaveTasks(final String login) {
		return this.userTasks.containsKey(login) && !this.userTasks.get(login).isEmpty();
	}

	private final class SubtaskExecutor<T> implements Runnable {
		private final Subtask<T> subtask;

		public SubtaskExecutor(final Subtask<T> subtask) {
			this.subtask = subtask;
		}

		public Subtask<T> getSubtask() {
			return subtask;
		}

		private Task<T> getTask() {
			return this.getSubtask().getTask();
		}

		private void checkAborted() throws AbortException {
			if (this.getTask().isAborted())
				throw new AbortException();
		}

		@Override
		public void run() {
			try {
				final Task<T> task = this.getTask();

				if (task.getStatus() == TaskStatus.UNSCHEDULED) {
					// Forces subtask to wait until the task is completely
					// scheduled
					synchronized (task) {
					}
				}

				this.checkAborted();

				if (task.getStatus() == TaskStatus.SCHEDULED) {
					synchronized (task) {
						if (task.getStatus() == TaskStatus.SCHEDULED) {
							task.preStart();
							task.start();
							ExecutionEngine.publishEvent(task, GlobalEvents.SUFFIX_STARTED, task);
						}
					}
				}

				this.checkAborted();

				task.preSubtaskStart(this.subtask);
				task.subtaskStarted(this.subtask);
				ExecutionEngine.publishEvent(task, GlobalEvents.SUFFIX_SUBTASK_STARTED, this.subtask);

				try {
					this.checkAborted();

					final T result = this.subtask.call();

					this.checkAborted();
					task.subtaskFinished(this.subtask);
					task.postSubtaskFinish(this.subtask, result);
					ExecutionEngine.publishEvent(task, GlobalEvents.SUFFIX_SUBTASK_FINISHED, result);
				} catch (final AbortException e) {
					e.printStackTrace();
					this.checkAborted();
					task.subtaskAborted(this.subtask, e);
					task.postSubtaskAbort(this.subtask, e);
					ExecutionEngine.publishEvent(task, GlobalEvents.SUFFIX_SUBTASK_ABORTED, this.subtask);
				} catch (final Exception e) {
					e.printStackTrace();
					this.checkAborted();
					task.subtaskError(this.subtask, e);
					task.postSubtaskError(this.subtask, e);
					ExecutionEngine.publishEvent(task, GlobalEvents.SUFFIX_SUBTASK_ERROR, this.subtask);
				}

				synchronized (ExecutionEngine.this.userTasks) {
					synchronized (task) {
						if (task.isFinished()) {
							if (ExecutionEngine.this.userTasks.get(task.getUserId()).isEmpty()) {
								ExecutionEngine.this.userTasks.remove(task.getUserId());
							}
							ExecutionEngine.this.taskExecutors.get(task).remove(this);
							ExecutionEngine.this.subtaskFutures.remove(this);

							task.postFinish();

							ExecutionEngine.publishEvent(task, GlobalEvents.SUFFIX_FINISHED, task);

							try {

								final User user = tm.get(em -> em.find(User.class, task.getUserId()));

								mailer.sendEmail("no_reply@jarvestweb.com", user.getEmail(), "Task Finished",
										generatetaskFinishedMessage(task));
							} catch (final Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			} catch (final AbortException ae) {
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String generatetaskFinishedMessage(final Task<?> task) {
		return new StringBuilder().append("<html>").append("<head><title>Task Finished</title></head>")
				.append("<body><br/><br/>").append("The task " + task.getDescription()).append(" has been finished at ")
				.append(new SimpleDateFormat().format(new Date()) + " ").append("</body>").append("</html>").toString();
	}
}
