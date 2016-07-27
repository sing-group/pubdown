package es.uvigo.ei.sing.pubdown.web.zk.util;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Interface for the {@link AbstractTransactionManager} methods
 * 
 */
public interface TransactionManager {
	public final static String PERSISTENCE_UNIT_NAME = "pubdownPU";

	/**
	 * Creates an {@link EntityManagerFactory}
	 * 
	 * @return an {@link EntityManagerFactory}
	 */
	public static EntityManagerFactory createEntityManagerFactory() {
		return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
	}

	public void runInTransaction(Consumer<EntityManager> action);

	public <T> T getInTransaction(Function<EntityManager, T> action);

	public void run(Consumer<EntityManager> action);

	public <T> T get(Function<EntityManager, T> action);

	public EntityManager getEntityManager();

}