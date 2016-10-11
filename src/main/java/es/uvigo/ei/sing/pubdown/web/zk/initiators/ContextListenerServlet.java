package es.uvigo.ei.sing.pubdown.web.zk.initiators;

import java.io.File;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import es.uvigo.ei.sing.pubdown.execution.RepositoryQueryScheduled;
import es.uvigo.ei.sing.pubdown.execution.Scheduler;
import es.uvigo.ei.sing.pubdown.web.entities.GlobalConfiguration;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.zk.util.CleanEntityManagerTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;

public class ContextListenerServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final TransactionManager tm = new CleanEntityManagerTransactionManager();

	@Override
	public void init() throws ServletException {
		final List<RepositoryQuery> repositoryQueries = tm.getInTransaction(
				em -> em.createQuery("SELECT rq FROM RepositoryQuery rq WHERE rq.running = 1", RepositoryQuery.class)
						.getResultList());

		for (final RepositoryQuery repositoryQuery : repositoryQueries) {
			final String globalConfigurationPath = tm.getInTransaction(em -> em
					.createQuery("SELECT g FROM GlobalConfiguration g WHERE	 g.configurationKey = :path",
							GlobalConfiguration.class)
					.setParameter("path", "repositoryPath").getSingleResult().getConfigurationValue());

			final String basePath = globalConfigurationPath + File.separator;
			final String userLogin = repositoryQuery.getRepository().getUser().getLogin();
			final String repositoryPath = repositoryQuery.getRepository().getPath() + File.separator;
			final String directoryPath = basePath + userLogin + File.separator + repositoryPath;

			final RepositoryQueryScheduled repositoryQueryScheduled = new RepositoryQueryScheduled(repositoryQuery,
					directoryPath, false);
			Scheduler.getSingleton().scheduleTask(repositoryQueryScheduled);
		}

	}

}
