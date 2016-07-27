package es.uvigo.ei.sing.pubdown.web.zk.vm;

import static java.util.Collections.singletonMap;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

import es.uvigo.ei.sing.pubdown.execution.EventQueueUtils;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.zk.util.ViewModelFunctions;

public class RobotExecutionParametersViewModel extends ViewModelFunctions {

	private RepositoryQuery robot;
	private String input;

	@Init
	public void init(@ExecutionArgParam("robot") final RepositoryQuery robot) {
		this.robot = robot;
		this.input = "";
	}

	/**
	 * Getter of the inputs global variable
	 * 
	 * @return the value of the inputs global variable
	 */
	public String getInput() {
		return input;
	}

	/**
	 * Setter of the inputs global variable
	 * 
	 * @param input
	 *            the value of the inputs global variable
	 */
	public void setInput(final String input) {
		this.input = input;
	}

	/**
	 * Getter of the robot global variable
	 * 
	 * @return the value of the robot global variable
	 */
	public RepositoryQuery getRobot() {
		return robot;
	}

	/**
	 * Setter of the robot global variable
	 * 
	 * @param robot
	 *            the value of the robot global variable
	 */
	public void setRobot(final RepositoryQuery robot) {
		this.robot = robot;
	}

	@Command
	public void executeRobot() {
		BindUtils.postGlobalCommand(EventQueueUtils.QUEUE_NAME, null, "executeRobot",
				singletonMap("input", this.getInput()));
	}
}
