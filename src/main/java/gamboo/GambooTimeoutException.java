package gamboo;

public class GambooTimeoutException extends GambooException {
	public GambooTimeoutException() {
		super();
	}

	public GambooTimeoutException(String message) {
		super(message);
	}
	
	public GambooTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public GambooTimeoutException(Throwable cause) {
		super(cause);
	}
}
