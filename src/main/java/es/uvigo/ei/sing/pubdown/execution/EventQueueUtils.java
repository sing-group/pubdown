package es.uvigo.ei.sing.pubdown.execution;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;

import es.uvigo.ei.sing.pubdown.web.entities.User;

public class EventQueueUtils {
	public static final String QUEUE_NAME = "pubdown";

	private final static Map<String, EventQueue<Event>> USER_QUEUES = new HashMap<>();

	public static String getUserQueueName(final User user) {
		return user.getLogin();
	}

	public static EventQueue<Event> getUserQueue(final User user) {
		return getUserQueue(getUserQueueName(user));
	}

	public static EventQueue<Event> getUserQueue(final String userId) {
		try {
			return EventQueues.lookup(userId, EventQueues.APPLICATION, true);
		} catch (final Exception e) {
			return EventQueueUtils.USER_QUEUES.get(userId);
		}
	}

	public static void destroyUserQueue() {
		final Session session = Sessions.getCurrent(false);

		if (session != null && session.hasAttribute("user")) {
			EventQueues.remove(getUserQueueName(((User) session.getAttribute("user"))));
		}
	}

	public static EventQueue<Event> getUserQueue() {
		final Session session = Sessions.getCurrent(false);

		if (session == null || !session.hasAttribute("user")) {
			return null;
		} else {
			final User user = (User) session.getAttribute("user");

			final String userQueueName = getUserQueueName(user);

			final EventQueue<Event> queue = EventQueues.lookup(userQueueName, EventQueues.APPLICATION, true);

			EventQueueUtils.USER_QUEUES.put(userQueueName, queue);

			return queue;
		}
	}

	public static UserGlobalEventListener registerUserGlobalListener() throws IllegalStateException {
		final Session session = Sessions.getCurrent(false);

		if (session == null || !session.hasAttribute("user")) {
			return null;
		} else if (session.hasAttribute("userGlobalListener")) {
			final UserGlobalEventListener listener = (UserGlobalEventListener) session
					.getAttribute("userGlobalListener");

			EventQueueUtils.addListener(listener);

			return listener;
		} else {
			final User user = (User) session.getAttribute("user");

			final String userQueueName = getUserQueueName(user);

			final UserGlobalEventListener listener = new UserGlobalEventListener(userQueueName);
			session.setAttribute("userGlobalListener", listener);

			EventQueueUtils.addListener(listener);

			return listener;
		}

	}

	public static UserGlobalEventListener unregisterUserGlobalListener() throws IllegalStateException {
		final Session session = Sessions.getCurrent(false);

		if (session == null || !session.hasAttribute("userGlobalListener")) {
			return null;
		} else {
			final UserGlobalEventListener listener = (UserGlobalEventListener) session
					.removeAttribute("userGlobalListener");

			if (listener != null && session.hasAttribute("user")) {
				EventQueueUtils.removeListener(listener);
			}

			return listener;
		}
	}

	private static void addListener(final EventListener<Event> listener) {
		if (!EventQueueUtils.getUserQueue().isSubscribed(listener)) {
			EventQueueUtils.getUserQueue().subscribe(listener);
		}
	}

	private static void removeListener(final EventListener<Event> listener) {
		EventQueueUtils.getUserQueue().unsubscribe(listener);
	}
}
