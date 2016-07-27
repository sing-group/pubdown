package es.uvigo.ei.sing.pubdown.execution;

public class AbortException extends Exception {
	private static final long serialVersionUID = 1L;

	public AbortException() {
		super();
	}

	public AbortException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public AbortException(final String message) {
		super(message);
	}

	public AbortException(final Throwable cause) {
		super(cause);
	}
}
