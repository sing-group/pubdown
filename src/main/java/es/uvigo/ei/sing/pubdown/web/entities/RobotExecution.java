package es.uvigo.ei.sing.pubdown.web.entities;

import static es.uvigo.ei.sing.pubdown.web.entities.Status.ABORTED;
import static es.uvigo.ei.sing.pubdown.web.entities.Status.FINISHED;
import static es.uvigo.ei.sing.pubdown.web.entities.Status.SCHEDULED;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity(name = "RobotExecution")
public class RobotExecution {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String userLogin;

	@Column(nullable = false)
	private String robotName;

	@Column
	@Lob
	private String result;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Status status;

	public RobotExecution() {
	}

	public RobotExecution(final String userLogin, final String robotName, final String result) {
		super();
		this.userLogin = userLogin;
		this.robotName = robotName;
		this.result = result;
		this.status = SCHEDULED;
	}

	public Integer getId() {
		return id;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public String getRobotName() {
		return robotName;
	}

	public String getResult() {
		return result;
	}

	public void setResult(final String result) {
		this.result = result;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(final Status status) {
		this.status = status;
	}

	public boolean isFinished() {
		return FINISHED == this.getStatus();
	}

	public boolean isAborted() {
		return ABORTED == this.getStatus();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final RobotExecution other = (RobotExecution) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
