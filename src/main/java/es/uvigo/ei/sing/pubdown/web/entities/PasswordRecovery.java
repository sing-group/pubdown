package es.uvigo.ei.sing.pubdown.web.entities;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Entity to recover/change the {@link User} password
 */
@Entity(name = "PasswordRecovery")
public class PasswordRecovery {

	@Id
	@Column
	private String login;

	@Column
	private String uuid;

	/**
	 * Empty constructor
	 */
	public PasswordRecovery() {

	}

	/**
	 * Constructs a {@link PasswordRecovery}
	 * 
	 * @param login
	 *            the {@link User} login
	 * @param uuid
	 *            a random {@link UUID}
	 */
	public PasswordRecovery(final String login, final String uuid) {
		super();
		this.login = login;
		this.uuid = uuid;
	}

	/**
	 * Getter method of the login global variable
	 * 
	 * @return the value of the login global variable
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * Setter method of the login global variable
	 * 
	 * @param login
	 *            the value of the login global variable
	 */
	public void setLogin(final String login) {
		this.login = login;
	}

	/**
	 * Getter method of the uuid global variable
	 * 
	 * @return the value of the uuid global variable
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * Setter method of the uuid global variable
	 * 
	 * @param uuid
	 *            the value of the uuid global variable
	 */
	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	/**
	 * {@link PasswordRecovery} hascode method
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((login == null) ? 0 : login.hashCode());
		return result;
	}

	/**
	 * {@link PasswordRecovery} equals method
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PasswordRecovery other = (PasswordRecovery) obj;
		if (login == null) {
			if (other.login != null)
				return false;
		} else if (!login.equals(other.login))
			return false;
		return true;
	}

}
