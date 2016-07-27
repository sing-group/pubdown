package es.uvigo.ei.sing.pubdown.web.zk.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.zkoss.bind.annotation.Init;
import org.zkoss.util.Cleanups.Cleanup;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.util.DesktopCleanup;
import org.zkoss.zk.ui.util.WebAppCleanup;
import org.zkoss.zk.ui.util.WebAppInit;

/**
 * Manages the {@link EntityManager} life cycle for the {@link Desktop}
 * transactions
 * 
 */
public class DesktopEntityManagerManager implements WebAppInit, WebAppCleanup {
	private static final String ENTITY_MANAGER_NAME = "pubdownPU";
	private static EntityManagerFactory emf = null;

	/**
	 * Creates an {@link EntityManager} for the {@link Desktop} transactions
	 * 
	 * @return an {@link EntityManager}
	 */
	public static EntityManager getDesktopEntityManager() {
		final Desktop currentDesktop = Executions.getCurrent().getDesktop();

		if (currentDesktop != null) {
			if (currentDesktop.hasAttribute(ENTITY_MANAGER_NAME)) {
				return (EntityManager) currentDesktop.getAttribute(ENTITY_MANAGER_NAME);
			} else {
				final EntityManager newEm = createNewEntityMamanger();
				currentDesktop.setAttribute(ENTITY_MANAGER_NAME, newEm);

				final DesktopCleanup cleanupListener = desktop -> newEm.close();
				currentDesktop.addListener(cleanupListener);
				return newEm;
			}

		} else {
			throw new IllegalArgumentException("Desktop not found in this execution");
		}
	}

	/**
	 * Creates and returns an {@link EntityManager}
	 * 
	 * @return an {@link EntityManager}
	 */
	public static EntityManager createNewEntityMamanger() {
		return emf.createEntityManager();
	}

	/**
	 * Creates an {@link EntityManagerFactory} in the {@link WebApp}
	 * {@link Init}
	 */
	@Override
	public void init(final WebApp wapp) throws Exception {
		emf = TransactionManager.createEntityManagerFactory();
	}

	/**
	 * Closes an {@link EntityManagerFactory} in the {@link WebApp}
	 * {@link Cleanup}
	 */
	@Override
	public void cleanup(final WebApp wapp) throws Exception {
		try {
			emf.close();
		} finally {
			emf = null;
		}
	}
}
