package es.uvigo.ei.sing.pubdown.web.zk.vm.robot;

import java.util.Arrays;
import java.util.List;

import es.uvigo.ei.sing.jarvest.core.Transformer;
import es.uvigo.ei.sing.pubdown.execution.EventQueueUtils;
import es.uvigo.ei.sing.pubdown.execution.GlobalEvents;
import es.uvigo.ei.sing.pubdown.execution.Task;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.entities.RobotExecution;
import es.uvigo.ei.sing.pubdown.web.entities.Status;
import es.uvigo.ei.sing.pubdown.web.entities.User;
import es.uvigo.ei.sing.pubdown.web.zk.util.CleanEntityManagerTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;

public class RobotExecutionTask extends Task<RobotExecution> {
	public static final String TASK_DESCRIPTION = "Jarvest Robot Execution";

	public RobotExecutionTask(final User user, final RepositoryQuery robot, final RobotExecution robotExecution, final String... inputs) {
		super(GlobalEvents.EVENT_ROBOT_EXECUTION, EventQueueUtils.getUserQueueName(user), TASK_DESCRIPTION,
				Arrays.asList(new RobotExecutionSubTask(robot, robotExecution, inputs,
						new CleanEntityManagerTransactionManager())));
	}

	@Override
	public void postSchedule() {
		updateRobotExecution();
	}

	@Override
	public void preStart() {
		this.getRobotExecution().setStatus(Status.RUNNING);
		updateRobotExecution();
	}

	@Override
	public void postFinish() {
		if (this.isAborted()) {
			this.getJarvestTransformer().stop();
			this.getRobotExecution().setStatus(Status.ABORTED);
			updateRobotExecution();
		} else {
			this.getRobotExecution().setStatus(Status.FINISHED);
			updateRobotExecution();
		}
		this.getSubtasks().forEach(RobotExecutionSubTask::close);
	}

	private void updateRobotExecution() {
		final RobotExecutionSubTask subtask = this.getSubtask();

		final TransactionManager tm = subtask.getTransactionManager();

		tm.runInTransaction(em -> em.persist(subtask.getRobotExecution()));
	}

	public RobotExecutionSubTask getSubtask() {
		return this.getSubtasks().get(0);
	}

	public RobotExecution getRobotExecution() {
		return this.getSubtask().getRobotExecution();
	}

	public Transformer getJarvestTransformer() {
		return this.getSubtask().getJarvestTransformer();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RobotExecutionSubTask> getSubtasks() {
		return (List<RobotExecutionSubTask>) super.getSubtasks();
	}

}
