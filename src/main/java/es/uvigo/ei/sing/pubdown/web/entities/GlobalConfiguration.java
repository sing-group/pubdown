package es.uvigo.ei.sing.pubdown.web.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "GlobalConfiguration")
public class GlobalConfiguration {
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

	public void setConfigurationKey(String configurationKey) {
		this.configurationKey = configurationKey;
	}

	public String getConfigurationValue() {
		return configurationValue;
	}

	public void setConfigurationValue(String configurationValue) {
		this.configurationValue = configurationValue;
	}

}
