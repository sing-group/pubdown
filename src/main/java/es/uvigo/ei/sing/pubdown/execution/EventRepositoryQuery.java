package es.uvigo.ei.sing.pubdown.execution;

public class EventRepositoryQuery {
	private final RepositoryQueryScheduled task;
	private final Object value;

	public EventRepositoryQuery(final RepositoryQueryScheduled task, final Object value) {
		this.task = task;
		this.value = value;
	}

	public RepositoryQueryScheduled getTask() {
		return task;
	}

	public Object getData() {
		return value;
	}

}
