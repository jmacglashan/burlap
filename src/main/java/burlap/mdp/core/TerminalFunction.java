/* Author: James MacGlashan
 * Description:
 * Abstract class for determining if a state in an OO-MDP domain is a terminal state
 * This kind of information is important for episode and goal-oriented MDPs
 */


package burlap.mdp.core;


import burlap.mdp.core.state.State;

/**
 * An interface for defining terminal states of an MDP.
 * @author James MacGlashan
 *
 */
public interface TerminalFunction {
	
	boolean isTerminal(State s);
	
}
