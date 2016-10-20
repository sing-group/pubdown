package es.uvigo.ei.sing.pubdown.web.zk.initiators;

import static es.uvigo.ei.sing.pubdown.web.entities.Role.ADMIN;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Initiator;

import es.uvigo.ei.sing.pubdown.execution.EventQueueUtils;
import es.uvigo.ei.sing.pubdown.web.entities.User;
import es.uvigo.ei.sing.pubdown.web.zk.util.ViewModelUtils;

/**
 * Initiator that manages the security of the application
 */
public class SecurityInitiator implements Initiator {
	private static final String INDEX_PAGE = "/index.zul";
	private static final String LOGOUT_PAGE = "/logout.zul";
	private static final String REGISTER_PAGE = "/registration.zul";
	private static final String CONFIRM_PAGE = "/confirmation.zul";
	private static final String CONFIRM_PAGE_ERROR = "/confirmationError.zul";
	private static final String PASSWORD_RECOVERY = "/passwordRecovery.zul";
	private static final String PASSWORD_RECOVERY_EMAIL = "/passwordRecoveryEmail.zul";
	private final static Set<String> IGNORE_PAGES = new HashSet<>();

	static {
		IGNORE_PAGES.add(INDEX_PAGE);
		IGNORE_PAGES.add(REGISTER_PAGE);
		IGNORE_PAGES.add(CONFIRM_PAGE);
		IGNORE_PAGES.add(CONFIRM_PAGE_ERROR);
		IGNORE_PAGES.add(PASSWORD_RECOVERY);
		IGNORE_PAGES.add(PASSWORD_RECOVERY_EMAIL);
	}

	/**
	 * If the {@link User} logs out, it will destroy the {@link User} session.If
	 * there is not a {@link User} session, it will redirect to the index
	 */
	@Override
	public void doInit(final Page page, final Map<String, Object> args) throws Exception {
		final String requestPath = page.getRequestPath();

		if (requestPath.equals(LOGOUT_PAGE)) {
			final Session session = Sessions.getCurrent(false);
			if (session != null) {
				session.invalidate();
			}

			Executions.sendRedirect(INDEX_PAGE);
		} else if (!IGNORE_PAGES.contains(requestPath)) {
			final Session session = Sessions.getCurrent(false);
			if (session == null || !session.hasAttribute(ViewModelUtils.USER_SESSION_KEY)) {
				Executions.sendRedirect(INDEX_PAGE);
			} else {
				final User user = (User) session.getAttribute("user");
				if(user.getRole().equals(ADMIN)){
					EventQueueUtils.registerAdminGlobalListener();
				} else {
					EventQueueUtils.registerUserGlobalListener();
				}
			}
		}
	}
}
