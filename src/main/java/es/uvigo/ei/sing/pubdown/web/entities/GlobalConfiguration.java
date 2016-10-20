package es.uvigo.ei.sing.pubdown.web.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import es.uvigo.ei.sing.pubdown.util.Compare;

@Entity(name = "GlobalConfiguration")
public class GlobalConfiguration implements Cloneable, Comparable<GlobalConfiguration> {
	@Id
	@Column
	private String configurationKey;

	@Column
	private String configurationValue;

	public GlobalConfiguration() {

	}

	public GlobalConfiguration(final String configurationKey, final String configurationValue) {
		super();
		this.configurationKey = configurationKey;
		this.configurationValue = configurationValue;
	}

	public String getConfigurationKey() {
		return configurationKey;
	}

	public void setConfigurationKey(final String configurationKey) {
		this.configurationKey = configurationKey;
	}

	public String getConfigurationValue() {
		return configurationValue;
	}

	public void setConfigurationValue(final String configurationValue) {
		this.configurationValue = configurationValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configurationKey == null) ? 0 : configurationKey.hashCode());
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
		GlobalConfiguration other = (GlobalConfiguration) obj;
		if (configurationKey == null) {
			if (other.configurationKey != null)
				return false;
		} else if (!configurationKey.equals(other.configurationKey))
			return false;
		return true;
	}

	@Override
	public GlobalConfiguration clone() {
		return new GlobalConfiguration(this.configurationKey, this.configurationValue);
	}

	@Override
	public int compareTo(final GlobalConfiguration obj) {
		return Compare.objects(this, obj).by(GlobalConfiguration::getConfigurationKey)
				.thenBy(GlobalConfiguration::getConfigurationValue).andGet();
	}

}
