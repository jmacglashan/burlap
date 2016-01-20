package burlap.oomdp.core.values;

/**
 * A class for indicating that a OO-MDP object instance value is unset.
 * @author James MacGlashan
 *
 */
public class UnsetValueException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnsetValueException(){
		super("OO-MDP Object Instance Value is Unset");
	}
}
