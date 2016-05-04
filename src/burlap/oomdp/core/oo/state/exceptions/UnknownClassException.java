package burlap.oomdp.core.oo.state.exceptions;

/**
 * @author James MacGlashan.
 */
public class UnknownClassException extends RuntimeException {
	public UnknownClassException(String objectClass) {
		super("Unknown object class " + objectClass);
	}
}
