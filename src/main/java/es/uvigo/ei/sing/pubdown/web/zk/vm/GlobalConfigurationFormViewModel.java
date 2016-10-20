package es.uvigo.ei.sing.pubdown.web.zk.vm;

import static es.uvigo.ei.sing.pubdown.util.Checks.isEmpty;
import static java.util.Collections.singletonMap;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;

import es.uvigo.ei.sing.pubdown.execution.EventQueueUtils;
import es.uvigo.ei.sing.pubdown.web.entities.GlobalConfiguration;
import es.uvigo.ei.sing.pubdown.web.zk.util.CleanEntityManagerTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.ViewModelUtils;

/***
 * ViewModel to manage the form to create/edit{
 * 
 * @link User}
 */
public class GlobalConfigurationFormViewModel extends ViewModelUtils {
	private final TransactionManager tm = new CleanEntityManagerTransactionManager();

	private GlobalConfiguration globalConfiguration;
	private GlobalConfiguration uneditedGlobalConfiguration;
	private boolean isNewGlobalConfiguration;

	@Init
	public void init(@ExecutionArgParam("globalConfiguration") final GlobalConfiguration globalConfiguration) {
		this.globalConfiguration = globalConfiguration;
		this.uneditedGlobalConfiguration = globalConfiguration.clone();
		this.isNewGlobalConfiguration = checkIfNewGlobalConfiguration();
	}

	public GlobalConfiguration getGlobalConfiguration() {
		return globalConfiguration;
	}

	public boolean isNewGlobalConfiguration() {
		return isNewGlobalConfiguration;
	}

	private boolean isGlobalConfigurationModified() {
		return this.globalConfiguration.compareTo(this.uneditedGlobalConfiguration) != 0;
	}

	private boolean checkIfNewGlobalConfiguration() {
		return this.globalConfiguration.getConfigurationKey() == null;
	}

	public boolean isValid() {
		return !isEmpty(this.globalConfiguration.getConfigurationKey())
				&& !isEmpty(this.globalConfiguration.getConfigurationValue());
	}

	@Command
	@NotifyChange("valid")
	public void checkData() {
	}

	public Validator getParameterValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String parameter = (String) ctx.getProperty().getValue();

				if (isEmpty(parameter)) {
					addInvalidMessage(ctx, "Parameter can't be empty");
				}
			}
		};
	}

	public Validator getValueValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String value = (String) ctx.getProperty().getValue();
				if (isEmpty(value)) {
					addInvalidMessage(ctx, "Value can't be empty");
				}
			}
		};
	}

	@Command
	public void confirm() {
		if (isGlobalConfigurationModified()) {
			final String command = isNewGlobalConfiguration ? "addGlobalConfiguration" : "updateGlobalConfiguration";
			tm.runInTransaction(em -> {
				em.persist(this.globalConfiguration);

				BindUtils.postGlobalCommand(EventQueueUtils.ADMIN_QUEUE_NAME, null, command,
						singletonMap("globalConfiguration", this.globalConfiguration));
			});
		}
	}

	@Command
	@NotifyChange("globalConfiguration")
	public void refresh() {
		if (isGlobalConfigurationModified()) {
			tm.runInTransaction(em -> {
				em.refresh(this.globalConfiguration);
				this.uneditedGlobalConfiguration = this.globalConfiguration.clone();
				BindUtils.postGlobalCommand(EventQueueUtils.ADMIN_QUEUE_NAME, null, "updateGlobalConfiguration",
						singletonMap("globalConfiguration", this.globalConfiguration));
			});
		}
	}

}
