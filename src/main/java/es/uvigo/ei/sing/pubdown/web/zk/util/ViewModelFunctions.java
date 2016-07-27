package es.uvigo.ei.sing.pubdown.web.zk.util;

import javax.persistence.EntityManager;

import org.zkoss.bind.BindUtils;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.EventQueues;

import es.uvigo.ei.sing.pubdown.execution.EventQueueUtils;
import es.uvigo.ei.sing.pubdown.web.entities.User;

/**
 * Implements common functions for the ViewModels
 * 
 */
public class ViewModelFunctions {
	public static final String USER_SESSION_KEY = "user";

	/**
	 * Returns the application's current {@link User}
	 * 
	 * @param tm
	 *            {@link TransactionManager} to execute an {@link EntityManager}
	 *            function
	 * @return the current {@link User} or <code>null</code> if there is not a
	 *         current {@link User}
	 */
	public User getCurrentUser(final TransactionManager tm) {
		final Session current = Sessions.getCurrent(false);
		if (current != null && current.hasAttribute(USER_SESSION_KEY)) {
			final User user = (User) current.getAttribute(USER_SESSION_KEY);
			return tm.get(em -> em.find(User.class, user.getLogin()));
		}
		return null;
	}

	/**
	 * Notifies a bean to update its properties
	 * 
	 * @param object
	 *            Bean instance
	 * @param properties
	 *            Name of the bean properties to notify
	 */
	public void postNotifyChange(final Object object, final String... properties) {
		for (final String property : properties) {
			BindUtils.postNotifyChange(EventQueueUtils.QUEUE_NAME, EventQueues.DESKTOP, object, property);
		}
	}

	/**
	 * Closes the current {@link User} session
	 */
	public void closeUserSession() {
		final Session current = Sessions.getCurrent(false);

		if (current != null && current.hasAttribute(USER_SESSION_KEY)) {
			current.removeAttribute(USER_SESSION_KEY);

			Executions.sendRedirect("index.zul");
		}
	}
}
