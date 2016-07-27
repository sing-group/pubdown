package es.uvigo.ei.sing.pubdown.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Task<T> {
	public enum TaskStatus {
		UNSCHEDULED, SCHEDULED, STARTED, FINISHED, ABORTED;
	}

	private final String taskId;
	private final String userId;
	private final List<? extends Subtask<T>> subtasks;
	private final Map<Subtask<T>, AbortException> abortCause;
	private final Map<Subtask<T>, Throwable> errors;

	private TaskStatus status;
	private String description;

	private int runningTasks;
	private int finishedTasks;

	@SafeVarargs
	public Task(final String taskId, final String userId, final String description, final Subtask<T>... subtasks) {
		this(taskId, userId, description, Arrays.asList(subtasks));
	}

	public Task(final String taskId, final String userId, final String description, final List<? extends Subtask<T>> subtasks) {
		this.taskId = taskId;
		this.userId = userId;
		this.status = TaskStatus.UNSCHEDULED;
		this.description = description;

		this.abortCause = new HashMap<>();
		this.errors = new HashMap<>();

		this.subtasks = Collections.unmodifiableList(new ArrayList<Subtask<T>>(subtasks));
		for (final Subtask<T> subtask : this.subtasks) {
			subtask.setTask(this);
		}
	}

	public void preSchedule() {
	}

	public void postSchedule() {
	}

	public void preStart() {
	}

	public void preSubtaskStart(final Subtask<T> subtask) {
	}

	public void postSubtaskFinish(final Subtask<T> subtask, final T result) {
	}

	public void postSubtaskAbort(final Subtask<T> subtask, final Throwable cause) {
	}

	public void postSubtaskError(final Subtask<T> subtask, final Throwable cause) {
	}

	public void postFinish() {
	}

	public synchronized void schedule() {
		this.status = TaskStatus.SCHEDULED;
		this.runningTasks = 0;
		this.finishedTasks = 0;
	}

	public synchronized void start() {
		if (this.status != TaskStatus.SCHEDULED)
			throw new IllegalStateException("Task must be scheduled before start");

		this.status = TaskStatus.STARTED;
	}

	/**
	 * 
	 * @param subtask
	 * @return <code>true</code> if <code>subtask</code> is the first subtask
	 *         started. <code>false</code> otherwise.
	 * @throws IllegalStateException
	 */
	public synchronized void subtaskStarted(final Subtask<T> subtask) throws IllegalStateException {
		if (this.status == TaskStatus.ABORTED) {
			return;
		} else if (this.status == TaskStatus.STARTED) {
			this.runningTasks++;
		} else {
			throw new IllegalStateException("Subtask started in state: " + this.status);
		}
	}

	/**
	 * 
	 * @param task
	 * @return <code>true</code> there is no more tasks to execute.
	 *         <code>false</code> otherwise.
	 * @throws IllegalStateException
	 */
	public synchronized void subtaskFinished(final Subtask<T> task) throws IllegalStateException {
		if (this.status == TaskStatus.ABORTED) {
			return;
		} else if (this.status == TaskStatus.STARTED) {
			this.runningTasks--;
			this.finishedTasks++;
		} else {
			throw new IllegalStateException("Subtask finished in state: " + this.status);
		}
	}

	public synchronized void subtaskAborted(final Subtask<T> subtask, final AbortException cause) {
		this.abortCause.put(subtask, cause);

		this.subtaskFinished(subtask);
	}

	public synchronized void subtaskError(final Subtask<T> subtask, final Throwable cause) {
		this.errors.put(subtask, cause);

		this.subtaskFinished(subtask);
	}

	public synchronized void abort() {
		this.status = TaskStatus.ABORTED;

		for (final Subtask<T> subtask : this.subtasks) {
			subtask.abort();
		}
	}

	public boolean isAborted() {
		return this.status == TaskStatus.ABORTED;
	}

	public synchronized boolean isError() {
		if (this.isFinished()) {
			final Set<Subtask<T>> errorTasks = new HashSet<>(this.errors.keySet());
			errorTasks.addAll(this.abortCause.keySet());

			return errorTasks.containsAll(this.getSubtasks());
		} else {
			return false;
		}
	}

	public TaskStatus getStatus() {
		return status;
	}

	public String getTaskId() {
		return taskId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getUserId() {
		return userId;
	}

	public boolean hasErrors() {
		return !this.errors.isEmpty();
	}

	public List<? extends Subtask<T>> getSubtasks() {
		return Collections.unmodifiableList(subtasks);
	}

	public int getNumTasks() {
		return this.subtasks.size();
	}

	public int getRunningTasks() {
		return runningTasks;
	}

	public int getFinishedTasks() {
		return finishedTasks;
	}

	public int getTotalTasks() {
		return this.subtasks.size();
	}

	public boolean isFinished() {
		return !this.isAborted() && this.getFinishedTasks() == this.getTotalTasks();
	}

	public float getCompletedPercentage() {
		return (float) this.finishedTasks / (float) this.subtasks.size();
	}
}
