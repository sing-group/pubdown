package es.uvigo.ei.sing.pubdown.web.zk.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.zkoss.zk.ui.Desktop;

/**
 * Gets an {@link EntityManager} for non {@link Desktop} transactions
 * 
 */
public class CleanEntityManagerTransactionManager extends AbstractTransactionManager {
	private volatile EntityManager em;

	/**
	 * Returns an {@link EntityManager} for non {@link Desktop} transactions. If
	 * there is not an {@link EntityManager} instance, it creates the
	 * {@link EntityManager} and returns it
	 * 
	 * @return an {@link EntityManager}
	 */
	@Override
	public EntityManager getEntityManager() {
		if (this.em == null) {
			synchronized (this) {
				if (this.em == null) {
					final EntityManagerFactory factory = TransactionManager.createEntityManagerFactory();

					this.em = factory.createEntityManager();
				}
			}
		}

		return this.em;
	}
}
