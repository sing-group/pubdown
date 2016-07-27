package es.uvigo.ei.sing.pubdown.execution;

public class EventTaskData {
	private final Task<?> task;
	private final Object value;

	public EventTaskData(final Task<?> task, final Object value) {
		this.task = task;
		this.value = value;
	}

	public Task<?> getTask() {
		return task;
	}

	public Object getData() {
		return value;
	}
}
