package uno.cod;

public class CodunoException extends Exception {
	public CodunoException() {
		super();
	}

	public CodunoException(String message) {
		super(message);
	}
	
	public CodunoException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public CodunoException(Throwable cause) {
		super(cause);
	}
}
