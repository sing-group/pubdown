package es.uvigo.ei.sing.pubdown.execution;

import java.util.concurrent.Callable;

public interface Subtask<T> extends Callable<T> {
	public void abort();

	public Task<T> getTask();

	void setTask(Task<T> task);
}
