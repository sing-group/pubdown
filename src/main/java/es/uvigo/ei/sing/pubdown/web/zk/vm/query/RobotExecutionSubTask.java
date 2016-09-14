package es.uvigo.ei.sing.pubdown.web.zk.vm.query;

import es.uvigo.ei.sing.jarvest.core.Transformer;
import es.uvigo.ei.sing.jarvest.core.Util;
import es.uvigo.ei.sing.jarvest.dsl.Jarvest;
import es.uvigo.ei.sing.pubdown.execution.AbortException;
import es.uvigo.ei.sing.pubdown.execution.Subtask;
import es.uvigo.ei.sing.pubdown.execution.Task;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.entities.RobotExecution;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;

public class RobotExecutionSubTask implements Subtask<RobotExecution> {
	private final TransactionManager tm;

	private final RepositoryQuery robot;
	private final String[] inputs;
	private RobotExecution robotExecution;
	private Task<RobotExecution> task;
	private boolean aborted;
	private Transformer jarvestTransformer;

	public RobotExecutionSubTask(final RepositoryQuery robot, final RobotExecution robotExecution,
			final String[] inputs, final TransactionManager tm) {
		this.robot = robot;
		this.robotExecution = robotExecution;
		this.inputs = inputs;
		this.tm = tm;
	}

	@Override
	public RobotExecution call() throws Exception {
		try {
			if (this.aborted) {
				throw new AbortException();
			}

//			final Jarvest jarvest = new Jarvest();
//			final StringBuffer output = new StringBuffer();
//			final String robot = this.robot.getRobot();
//			this.jarvestTransformer = jarvest.eval(robot);
//
//			robotExecution = findRobotExecution();
//
//			synchronized (this.jarvestTransformer) {
//				for (final String input : inputs) {
//					final String[] array = { input };
//					for (final String result : Util.runRobot(jarvestTransformer, array)) {
//						output.setLength(0);
//						output.append(result);
//						robotExecution.setResult(robotExecution.getResult().concat(output.toString()));
//					}
//				}
//			}


			if (this.aborted) {
				throw new AbortException();
			}

			return robotExecution;
		} catch (final Exception e) {
			throw e;
		}
	}

	private RobotExecution findRobotExecution() {
		return tm.get(em -> em.find(RobotExecution.class, this.robotExecution.getId()));
	}

	@Override
	public void abort() {
		this.aborted = true;
	}

	@Override
	public Task<RobotExecution> getTask() {
		return this.task;
	}

	@Override
	public void setTask(final Task<RobotExecution> task) {
		this.task = task;
	}

	public RobotExecution getRobotExecution() {
		return robotExecution;
	}

	public void close() {
		this.tm.getEntityManager().close();
	}

	public TransactionManager getTransactionManager() {
		return tm;
	}

	public Transformer getJarvestTransformer() {
		return jarvestTransformer;
	}
}
