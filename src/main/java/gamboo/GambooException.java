package gamboo;

public class GambooException extends Exception {
	public GambooException() {
		super();
	}

	public GambooException(String message) {
		super(message);
	}
	
	public GambooException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public GambooException(Throwable cause) {
		super(cause);
	}
}
