package es.uvigo.ei.sing.pubdown.web.entities;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import es.uvigo.ei.sing.pubdown.util.Compare;

@Entity(name = "Repository")
public class Repository implements Cloneable, Comparable<Repository> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column
	private String name;

	@Column
	private String path;

	@Column
	private int numberOfPapers = 0;

	@Column
	private String lastUpdate = "";

	@Column
	private String nextUpdate = "";

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "repository")
	private List<RepositoryQuery> repositoryQueries;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "userId")
	private User user;

	public Repository() {
		this.repositoryQueries = new LinkedList<>();
	}

	public Repository(final String name, final String path, final int numberOfPapers, final String lastUpdate,
			final String nextUpdate, final List<RepositoryQuery> repositoryQueries) {
		super();
		this.name = name;
		this.path = path;
		this.numberOfPapers = numberOfPapers;
		this.lastUpdate = lastUpdate;
		this.nextUpdate = nextUpdate;
		this.repositoryQueries = repositoryQueries;
	}

	private Repository(final Integer id, final String name, final String path, final int numberOfPapers,
			final String lastUpdate, final String nextUpdate, final List<RepositoryQuery> repositoryQueries,
			final User user) {
		super();
		this.id = id;
		this.name = name;
		this.path = path;
		this.numberOfPapers = numberOfPapers;
		this.lastUpdate = lastUpdate;
		this.nextUpdate = nextUpdate;
		this.repositoryQueries = repositoryQueries;
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public Integer getId() {
		return id;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public int getNumberOfPapers() {
		return numberOfPapers;
	}

	public void setNumberOfPapers(final int numberOfPapers) {
		this.numberOfPapers = numberOfPapers;
	}

	public String getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(final String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getNextUpdate() {
		return nextUpdate;
	}

	public void setNextUpdate(final String nextUpdate) {
		this.nextUpdate = nextUpdate;
	}

	public List<RepositoryQuery> getRepositoryQueries() {
		return repositoryQueries;
	}

	public void setRepositoryQueries(final List<RepositoryQuery> repositoryQueries) {
		this.repositoryQueries = repositoryQueries;
	}

	/**
	 * Getter method of the user global variable
	 * 
	 * @return the value of the user global variable
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Setter method of the user global variable
	 * 
	 * @param user
	 *            the value of the user global variable
	 */
	public void setUser(final User user) {
		if (this.user != null) {
			this.user.removeRepository(this);
		}
		if (user != null) {
			user.addRepository(this);
		}
	}

	void packageSetUser(final User user) {
		this.user = user;
	}

	public boolean addRepository(final RepositoryQuery repositoryQuery) {
		if (this.repositoryQueries.add(repositoryQuery)) {
			repositoryQuery.packageSetRepository(this);
			return true;
		} else {
			return false;
		}
	}

	public boolean removeRepositoryQuery(final RepositoryQuery repositoryQuery) {
		if (this.repositoryQueries.remove(repositoryQuery)) {
			repositoryQuery.packageSetRepository(null);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Repository other = (Repository) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public Repository clone() {
		return new Repository(this.id, this.name, this.path, this.numberOfPapers, this.lastUpdate, this.nextUpdate,
				this.repositoryQueries, this.user);
	}

	@Override
	public int compareTo(final Repository obj) {
		return Compare.objects(this, obj).by(Repository::getId).thenBy(Repository::getName).thenBy(Repository::getPath)
				.andGet();
	}

	@Override
	public String toString() {
		return this.name;
	}

}
