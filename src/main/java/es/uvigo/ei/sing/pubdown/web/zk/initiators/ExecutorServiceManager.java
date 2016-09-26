package es.uvigo.ei.sing.pubdown.web.zk.initiators;

import java.util.concurrent.ExecutorService;

import org.zkoss.util.Cleanups.Cleanup;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.util.WebAppCleanup;

import es.uvigo.ei.sing.pubdown.execution.ExecutionEngine;
import es.uvigo.ei.sing.pubdown.execution.Scheduler;

/**
 * Manages the {@link ExecutorService}
 */
public class ExecutorServiceManager implements WebAppCleanup {
	/**
	 * Shuts down the {@link ExecutorService} in the {@link WebApp}
	 * {@link Cleanup}
	 */
	@Override
	public void cleanup(final WebApp wapp) throws Exception {
		ExecutionEngine.getSingleton().shutdown();
		Scheduler.getSingleton().shutdown();
	}
}
