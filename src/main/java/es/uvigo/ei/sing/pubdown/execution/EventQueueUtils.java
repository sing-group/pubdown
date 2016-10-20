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
	public static final String ADMIN_QUEUE_NAME = "adminpubdown";

	private final static Map<String, EventQueue<Event>> USER_QUEUES = new HashMap<>();

	public static String getUserQueueName(final User user) {
		return user.getLogin();
	}

	public static String getAdminQueueName() {
		return ADMIN_QUEUE_NAME;
	}

	public static EventQueue<Event> getUserQueue(final User user) {
		return getUserQueue(getUserQueueName(user));
	}

	public static EventQueue<Event> getAdminQueue(final User user) {
		return getAdminQueue(getAdminQueueName());
	}

	public static EventQueue<Event> getUserQueue(final String userId) {
		try {
			return EventQueues.lookup(userId, EventQueues.APPLICATION, true);
		} catch (final Exception e) {
			return EventQueueUtils.USER_QUEUES.get(userId);
		}
	}

	public static EventQueue<Event> getAdminQueue(final String userId) {
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

	public static void destroyAdminQueue() {
		final Session session = Sessions.getCurrent(false);

		if (session != null && session.hasAttribute("user")) {
			EventQueues.remove(getAdminQueueName());
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

	public static EventQueue<Event> getAdminQueue() {
		final Session session = Sessions.getCurrent(false);

		if (session == null || !session.hasAttribute("user")) {
			return null;
		} else {

			final String adminQueueName = getAdminQueueName();

			final EventQueue<Event> queue = EventQueues.lookup(adminQueueName, EventQueues.APPLICATION, true);

			EventQueueUtils.USER_QUEUES.put(adminQueueName, queue);

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

	public static AdminGlobalEventListener registerAdminGlobalListener() throws IllegalStateException {
		final Session session = Sessions.getCurrent(false);

		if (session == null || !session.hasAttribute("user")) {
			return null;
		} else if (session.hasAttribute("adminGlobalListener")) {
			final AdminGlobalEventListener listener = (AdminGlobalEventListener) session
					.getAttribute("adminGlobalListener");

			EventQueueUtils.addAdminListener(listener);

			return listener;
		} else {

			final String adminQueueName = getAdminQueueName();

			final AdminGlobalEventListener listener = new AdminGlobalEventListener(adminQueueName);
			session.setAttribute("adminGlobalListener", listener);

			EventQueueUtils.addAdminListener(listener);

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

	public static AdminGlobalEventListener unregisterAdminGlobalListener() throws IllegalStateException {
		final Session session = Sessions.getCurrent(false);

		if (session == null || !session.hasAttribute("adminGlobalListener")) {
			return null;
		} else {
			final AdminGlobalEventListener listener = (AdminGlobalEventListener) session
					.removeAttribute("adminGlobalListener");

			if (listener != null && session.hasAttribute("user")) {
				EventQueueUtils.removeAdminListener(listener);
			}

			return listener;
		}
	}

	private static void addListener(final EventListener<Event> listener) {
		if (!EventQueueUtils.getUserQueue().isSubscribed(listener)) {
			EventQueueUtils.getUserQueue().subscribe(listener);
		}
	}
	
	private static void addAdminListener(final EventListener<Event> listener){
		if (!EventQueueUtils.getAdminQueue().isSubscribed(listener)) {
			EventQueueUtils.getAdminQueue().subscribe(listener);
		}
	}

	private static void removeListener(final EventListener<Event> listener) {
		if (EventQueueUtils.getUserQueue().isSubscribed(listener)) {
			EventQueueUtils.getUserQueue().unsubscribe(listener);
		}
	}
	
	private static void removeAdminListener(final EventListener<Event> listener) {
		if (EventQueueUtils.getAdminQueue().isSubscribed(listener)) {
			EventQueueUtils.getAdminQueue().unsubscribe(listener);
		}
	}
}
