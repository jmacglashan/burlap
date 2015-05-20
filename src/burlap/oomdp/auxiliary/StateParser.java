package burlap.oomdp.auxiliary;

import burlap.oomdp.core.State;

/**
 * This interface is used to converting states to parsable string representations and parsing those string representations back into states.
 * Although there is a domain-universal string parser implementation of this interface, it is very verbose and file size and readability
 * may be improved by creating a domain-specific state parser.
 * @author James MacGlashan
 *
 */
public interface StateParser {

	/**
	 * Converts state s into a parsable string representation.
	 * @param s the state to convert
	 * @return a parsable string representation of state s.
	 */
	public String stateToString(State s);
	
	/**
	 * Converts a string into a State object assuming the string representation was produced using this state parser.
	 * @param str a string representation of a state
	 * @return the state object that corresponds to the string representation.
	 */
	public State stringToState(String str);
	
	
}
