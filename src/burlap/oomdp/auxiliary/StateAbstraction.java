package burlap.oomdp.auxiliary;

import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.Agent;


/**
 * An interface for taking an input state and returning a simpler abstracted state representation.
 * @author James MacGlashan
 *
 */
public interface StateAbstraction {
	/**
	 * Returns an abstracted version of state s. State s is not modified in this process.
	 * @param s the input state to abstract
	 * @return an abstracted version of state s
	 */
	public State abstraction(State s);
	public State abstraction(State s, Agent a);
}
