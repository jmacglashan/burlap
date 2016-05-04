package burlap.oomdp.core.oo.state;

/**
 * An exception for when an OOState is queried for an unknown object.
 * @author James MacGlashan.
 */
public class UnknownObjectException extends RuntimeException {

	public UnknownObjectException(String objectName) {
		super("Unknown object " + objectName);
	}
}
