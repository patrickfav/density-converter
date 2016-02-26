package at.favre.tools.converter;

/**
 * Created by PatrickF on 26.02.2016.
 */
public class InvalidArgumentException extends Exception {
	public InvalidArgumentException(String message) {
		super(message);
	}

	public InvalidArgumentException(String message, Throwable cause) {
		super(message, cause);
	}
}
