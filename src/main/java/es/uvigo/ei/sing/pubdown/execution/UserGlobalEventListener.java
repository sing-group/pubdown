package es.uvigo.ei.sing.pubdown.execution;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

public class UserGlobalEventListener implements EventListener<Event> {
	public static final String KEY_TASK = "task";
	public static final String KEY_DATA = "data";
	public static final String KEY_ACTION = "action";
	public static final String KEY_EVENT = "event";

	private final String userId;

	public UserGlobalEventListener(final String userId) {
		super();
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}

	@Override
	public void onEvent(final Event event) throws Exception {
		final String eventName = event.getName();
		final Map<String, Object> params = new HashMap<>();

		if (event.getData() instanceof EventRepositoryQuery) {
			final EventRepositoryQuery etData = (EventRepositoryQuery) event.getData();

			if (etData.getData() != null) {
				params.put(UserGlobalEventListener.KEY_DATA, etData.getData());
			}
			params.put(UserGlobalEventListener.KEY_TASK, etData.getTask());

			if (eventName.contains("#")) {
				final String[] eventNameSplit = eventName.split("#");
				params.put(UserGlobalEventListener.KEY_EVENT, eventNameSplit[0]);
				params.put(UserGlobalEventListener.KEY_ACTION, eventNameSplit[1]);
			} else {
				params.put(UserGlobalEventListener.KEY_EVENT, eventName);
			}
		}

		for (final String globalCommand : GlobalEvents.getEventGlobalCommands(eventName)) {
			BindUtils.postGlobalCommand(EventQueueUtils.QUEUE_NAME, null, globalCommand, params);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof UserGlobalEventListener)) {
			return false;
		}
		final UserGlobalEventListener other = (UserGlobalEventListener) obj;
		if (userId == null) {
			if (other.userId != null) {
				return false;
			}
		} else if (!userId.equals(other.userId)) {
			return false;
		}
		return true;
	}
}
