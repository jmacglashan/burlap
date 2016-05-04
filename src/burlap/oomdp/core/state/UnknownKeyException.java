package burlap.oomdp.core.state;

/**
 * A runtime exception for when a State variable key is unknown.
 * @author James MacGlashan.
 */
public class UnknownKeyException extends RuntimeException {
	public UnknownKeyException(Object variableKey) {
		super("Unknown state variable key " + variableKey);
	}
}
