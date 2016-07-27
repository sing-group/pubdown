package es.uvigo.ei.sing.pubdown.web.zk.util;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.persistence.EntityManager;

/**
 * Implements the {@link TransactionManager} methods
 * 
 */
public abstract class AbstractTransactionManager implements TransactionManager {

	@Override
	public abstract EntityManager getEntityManager();

	/**
	 * Runs all the {@link EntityManager} actions in one transaction
	 */
	@Override
	public void runInTransaction(final Consumer<EntityManager> action) {
		final EntityManager em = getEntityManager();

		try {
			if (!em.getTransaction().isActive()) {
				em.getTransaction().begin();
			}

			action.accept(em);

			em.getTransaction().commit();
		} catch (final RuntimeException e) {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}

			throw e;
		}
	}

	/**
	 * Runs all the {@link EntityManager} actions in one transaction and returns
	 * a value
	 */
	@Override
	public <T> T getInTransaction(final Function<EntityManager, T> action) {
		final EntityManager em = getEntityManager();

		try {
			if (!em.getTransaction().isActive()) {
				em.getTransaction().begin();
			}

			final T value = action.apply(em);

			em.getTransaction().commit();

			return value;
		} catch (final RuntimeException e) {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}

			throw e;
		}
	}

	/**
	 * Runs an {@link EntityManager} transaction
	 */
	@Override
	public void run(final Consumer<EntityManager> action) {
		action.accept(getEntityManager());
	}

	/**
	 * Runs an {@link EntityManager} transaction and returns a value
	 */
	@Override
	public <T> T get(final Function<EntityManager, T> action) {
		return action.apply(getEntityManager());
	}

}