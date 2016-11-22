package es.uvigo.ei.sing.pubdown.web.zk.initiators;

import static es.uvigo.ei.sing.pubdown.web.entities.ExecutionState.RUNNING;
import static es.uvigo.ei.sing.pubdown.web.entities.ExecutionState.UNSCHEDULED;

import java.io.File;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import es.uvigo.ei.sing.pubdown.execution.ExecutionEngine;
import es.uvigo.ei.sing.pubdown.execution.RepositoryQueryScheduled;
import es.uvigo.ei.sing.pubdown.web.entities.GlobalConfiguration;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.zk.util.CleanEntityManagerTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;

public class LaunchAtStartUpServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final TransactionManager tm = new CleanEntityManagerTransactionManager();

	@Override
	public void init() throws ServletException {
		List<RepositoryQuery> repositoryQueries = tm.getInTransaction(
				em -> em.createQuery("SELECT rq FROM RepositoryQuery rq", RepositoryQuery.class).getResultList());
		for (final RepositoryQuery repositoryQuery : repositoryQueries) {
			if (repositoryQuery.getExecutionState().equals(RUNNING)) {
				repositoryQuery.setExecutionState(UNSCHEDULED);
				repositoryQuery.setLastExecution("Never");
				tm.runInTransaction(em -> em.merge(repositoryQuery));
			}
		}

		repositoryQueries = tm.getInTransaction(
				em -> em.createQuery("SELECT rq FROM RepositoryQuery rq WHERE rq.scheduled = 1", RepositoryQuery.class)
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
			ExecutionEngine.getSingleton().scheduleTask(repositoryQueryScheduled);
		}

	}

}
