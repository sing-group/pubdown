package es.uvigo.ei.sing.pubdown.web.entities;

import static es.uvigo.ei.sing.pubdown.web.entities.Role.USER;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Entity to sign up an {@link User}
 */
@Entity(name = "Registration")
public class Registration {
	@Id
	@Column
	private String login;

	@Column
	private String password;

	@Column
	private String apiKey;

	@Column(nullable = false)
	private String email;

	@Column
	private String uuid;

	/**
	 * Empty constructor
	 */
	public Registration() {

	}

	/**
	 * Constructs a {@link Registration}
	 * 
	 * @param login
	 *            the {@link User} login
	 * @param password
	 *            the {@link User} password
	 * @param apiKey
	 *            the {@link User} apikey
	 * @param email
	 *            the {@link User} email
	 * @param uuid
	 *            a random {@link UUID}
	 */
	public Registration(final String login, final String password, final String apiKey, final String email,
			final String uuid) {
		super();
		this.login = login;
		this.password = password;
		this.apiKey = apiKey;
		this.email = email;
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
	 * Getter method of the password global variable
	 * 
	 * @return the value of the password global variable
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Setter method of the password global variable
	 * 
	 * @param password
	 *            the value of the password global variable
	 */
	public void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * Getter method of the apiKey global variable
	 * 
	 * @return the value of the apiKey global variable
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * Setter method of the apiKey global variable
	 * 
	 * @param apiKey
	 *            the value of the apiKey global variable
	 */
	public void setApiKey(final String apiKey) {
		this.apiKey = apiKey;
	}

	/**
	 * Getter method of the email global variable
	 * 
	 * @return the value of the email global variable
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Setter method of the email global variable
	 * 
	 * @param email
	 *            the value of the email global variable
	 */
	public void setEmail(final String email) {
		this.email = email;
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
	 * Creates an {@link User}
	 * 
	 * @return an {@link User}
	 */
	public User getUser() {
		return new User(this.login, this.password, this.apiKey, this.email, USER);
	}
}
