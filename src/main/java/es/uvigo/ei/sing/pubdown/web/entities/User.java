package es.uvigo.ei.sing.pubdown.web.entities;

import static es.uvigo.ei.sing.pubdown.web.entities.Role.ADMIN;
import static es.uvigo.ei.sing.pubdown.web.entities.Role.USER;
import static java.util.stream.Collectors.toList;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import es.uvigo.ei.sing.pubdown.util.Compare;

/**
 * Entity that represents an {@link User} of the application
 */
@Entity(name = "User")
public class User implements Cloneable, Comparable<User> {
	@Id
	@Column
	private String login;

	@Column
	private String password;

	@Column
	private String apiKey;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Role role;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user")
	private List<Repository> repositories;

	@Column(nullable = false)
	private boolean locked;

	@Column(nullable = false)
	private boolean logged;

	/**
	 * Constructs an {@link User} with an empty set of robots, the
	 * {@link Role#USER} {@link Role} and the locked value as false
	 */
	public User() {
		this.role = USER;
		this.repositories = new LinkedList<>();
		this.locked = false;
		this.logged = false;
	}

	/**
	 * Constructs an {@link User} with the apikey value as the login, locked
	 * value as false, an empty set of robots and the {@link Role#USER}
	 * {@link Role}
	 * 
	 * @param login
	 *            the {@link User} login
	 * @param password
	 *            the {@link User} password
	 * @param email
	 *            the {@link User} email
	 */
	public User(final String login, final String password, final String email) {
		super();
		this.login = login;
		this.password = password;
		this.apiKey = login;
		this.email = email;
		this.role = USER;
		this.repositories = new LinkedList<>();
		this.locked = false;
		this.logged = false;
	}

	/**
	 * Constructs an {@link User} with the locked value as false and an empty
	 * set of robots
	 * 
	 * @param login
	 *            the {@link User} login
	 * @param password
	 *            the {@link User} password
	 * @param apiKey
	 *            the {@link User} apikey
	 * @param email
	 *            the {@link User} email
	 * @param role
	 *            the {@link User} {@link Role}
	 */
	public User(final String login, final String password, final String apiKey, final String email, final Role role) {
		super();
		this.login = login;
		this.password = password;
		this.apiKey = apiKey;
		this.email = email;
		this.role = role;
		this.repositories = new LinkedList<>();
		this.locked = false;
		this.logged = false;
	}

	/**
	 * Constructs an {@link User} with the locked value as false
	 * 
	 * @param login
	 *            the {@link User} login
	 * @param password
	 *            the {@link User} password
	 * @param apiKey
	 *            the {@link User} apikey
	 * @param email
	 *            the {@link User} email
	 * @param role
	 *            the {@link User} {@link Role}
	 * @param repositoryQueries
	 *            the {@link User} {@link Repository}
	 */
	public User(final String login, final String password, final String apiKey, final String email, final Role role,
			final List<Repository> repositoryQueries) {
		super();
		this.login = login;
		this.password = password;
		this.apiKey = apiKey;
		this.email = email;
		this.role = role;
		this.repositories = repositoryQueries;
		this.locked = false;
		this.logged = false;
	}

	/**
	 * Constructs an {@link User}
	 * 
	 * @param login
	 *            the {@link User} login
	 * @param password
	 *            the {@link User} password
	 * @param apiKey
	 *            the {@link User} apikey
	 * @param email
	 *            the {@link User} email
	 * @param role
	 *            the {@link User} {@link Role}
	 * @param repositoryQueries
	 *            the {@link User} {@link Repository}
	 * @param locked
	 *            indicates if the {@link User} is locked
	 */
	public User(final String login, final String password, final String apiKey, final String email, final Role role,
			final List<Repository> repositoryQueries, final boolean locked, final boolean logged) {
		super();
		this.login = login;
		this.password = password;
		this.apiKey = apiKey;
		this.email = email;
		this.role = role;
		this.repositories = repositoryQueries;
		this.locked = locked;
		this.logged = logged;
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
	 * Getter method of the role global variable
	 * 
	 * @return the value of the role global variable
	 */
	public Role getRole() {
		return role;
	}

	/**
	 * Setter method of the role global variable
	 * 
	 * @param role
	 *            the value of the role global variable
	 */
	public void setRole(final Role role) {
		this.role = role;
	}

	/**
	 * Checks if the {@link User} {@link Role}
	 * 
	 * @return <code>true</code> if the {@link User} {@link Role} is
	 *         {@link Role#ADMIN}, <code>false</code> otherwise
	 */
	public boolean isAdmin() {
		return ADMIN == this.getRole();
	}

	/**
	 * Sets the {@link User} {@link Role}
	 * 
	 * @param isAdmin
	 *            <code>true</code> indicates if the {@link User} {@link Role}
	 *            is {@link Role#ADMIN}, <code>false</code> indicates if the
	 *            {@link User} {@link Role} is {@link Role#USER}
	 */
	public void setAdmin(final boolean isAdmin) {
		if (isAdmin) {
			this.setRole(ADMIN);
		} else {
			this.setRole(USER);
		}
	}

	/**
	 * Getter method of the locked global variable
	 * 
	 * @return the value of the locked global variable
	 */
	public boolean isLocked() {
		return locked;
	}

	/**
	 * Setter method of the locked global variable
	 * 
	 * @param locked
	 *            the value of the locked global variable
	 */
	public void setLocked(final boolean locked) {
		this.locked = locked;
	}

	public boolean isLogged() {
		return logged;
	}

	public void setLogged(boolean logged) {
		this.logged = logged;
	}

	/**
	 * Gets the number of robots. Including the total number and the
	 * private/public number
	 * 
	 * @return the number of robots
	 */
	public String getRepositoryNumber() {
		return String.valueOf(this.getRepositories().size());
	}

	/**
	 * Gets all the {@link User} {@link RepositoryQuery}
	 * 
	 * @return the {@link User} {@link RepositoryQuery}
	 */
	public List<Repository> getRepositories() {
		return repositories.stream()
				.sorted((repository1, repository2) -> repository1.getName().compareTo(repository2.getName()))
				.collect(toList());
	}

	/**
	 * Adds a {@link Repository} from his {@link User}
	 * 
	 * @param Repository
	 *            the {@link Repository} to remove
	 * @return <code>true</code> if the {@link Repository} has been removed,
	 *         <code>false</code> otherwise
	 */
	public boolean addRepository(final Repository repository) {
		if (this.repositories.add(repository)) {
			repository.packageSetUser(this);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Removes a {@link Repository} from his {@link User}
	 * 
	 * @param repositoryQuery
	 *            the {@link Repository} to remove
	 * @return <code>true</code> if the {@link Repository} has been removed,
	 *         <code>false</code> otherwise
	 */
	public boolean removeRepository(final Repository repository) {
		if (this.repositories.remove(repository)) {
			repository.packageSetUser(null);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if a {@link RepositoryQuery} belongs to the {@link User}
	 * 
	 * @param repositoryQuery
	 *            the {@link RepositoryQuery}
	 * @return <code>true</code> if the {@link RepositoryQuery} belongs to the
	 *         {@link User}, <code>false</code> otherwise
	 */
	public boolean containsRepository(final Repository repository) {
		return this.getRepositories().contains(repository);
	}

	/**
	 * Clones a {@link User}
	 * 
	 * @return a new {@link User}
	 */
	@Override
	public User clone() {
		return new User(this.login, this.password, this.apiKey, this.email, this.role, this.repositories, this.locked,
				this.logged);
	}

	/**
	 * Compares a {@link User} with another {@link User}
	 * 
	 * @return <code>true</code> if the {@link User} are equal,
	 *         <code>false</code> otherwise
	 */
	@Override
	public int compareTo(final User obj) {
		return Compare.objects(this, obj).by(User::getLogin).thenBy(User::getEmail).thenBy(User::getApiKey)
				.thenBy(User::getPassword).thenBy(User::getRole).andGet();
	}
}
