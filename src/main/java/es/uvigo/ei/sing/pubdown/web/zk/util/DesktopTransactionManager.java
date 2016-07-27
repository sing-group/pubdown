package es.uvigo.ei.sing.pubdown.web.zk.util;

import static es.uvigo.ei.sing.pubdown.web.zk.util.DesktopEntityManagerManager.getDesktopEntityManager;

import javax.persistence.EntityManager;

import org.zkoss.zk.ui.Desktop;

/**
 * Gets an {@link EntityManager} for {@link Desktop} transactions
 * 
 */
public class DesktopTransactionManager extends AbstractTransactionManager {

	/**
	 * Returns an {@link EntityManager} for the {@link Desktop} transactions
	 * 
	 * @return an {@link EntityManager}
	 */
	@Override
	public EntityManager getEntityManager() {
		return getDesktopEntityManager();
	}
}
