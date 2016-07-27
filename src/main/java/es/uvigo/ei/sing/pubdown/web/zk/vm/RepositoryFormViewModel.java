package es.uvigo.ei.sing.pubdown.web.zk.vm;

import static es.uvigo.ei.sing.pubdown.util.Checks.isEmpty;
import static java.util.Collections.singletonMap;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;

import es.uvigo.ei.sing.pubdown.execution.EventQueueUtils;
import es.uvigo.ei.sing.pubdown.web.zk.util.DesktopTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.ViewModelFunctions;

public class RepositoryFormViewModel extends ViewModelFunctions {
	private final TransactionManager tm = new DesktopTransactionManager();

	private String name = "";

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public boolean isValid() {
		return !isEmpty(this.getName());
	}

	@Command
	@NotifyChange("valid")
	public void checkData() {
	}

	public Validator getNameValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String name = (String) ctx.getProperty().getValue();

				if (isEmpty(name)) {
					addInvalidMessage(ctx, "Repository name can't be empty");
				}
			}
		};
	}

	@Command
	public void confirm() {
		tm.runInTransaction(em -> {
			// em.persist(this.robot);

			BindUtils.postGlobalCommand(EventQueueUtils.QUEUE_NAME, null, "addRepository",
					singletonMap("repository", this.name));
		});
	}

}
