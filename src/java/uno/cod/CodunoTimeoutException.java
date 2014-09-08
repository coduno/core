package uno.cod;

public class CodunoTimeoutException extends CodunoException {
	public CodunoTimeoutException() {
		super();
	}

	public CodunoTimeoutException(String message) {
		super(message);
	}
	
	public CodunoTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public CodunoTimeoutException(Throwable cause) {
		super(cause);
	}
}
