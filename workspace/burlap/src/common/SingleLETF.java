package common;

import logicalexpressions.LogicalExpression;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;


/**
 * This class defines a terminal function that terminates in states where there exists a grounded version of a specified
 * propositional function that is true in the state or alternatively, when there is a grounded version that is false in the state.
 * @author James MacGlashan
 *
 */
public class SingleLETF implements TerminalFunction {

	LogicalExpression			le;
	boolean						terminateOnTrue;
	
	
	/**
	 * Initializes the logical expression that will cause the state to be terminal when any Grounded version of
	 * le is true.
	 * @param pf the propositional function that must have a true grounded version for the state to be terminal.
	 */
	public SingleLETF(LogicalExpression le){
		this.le = le;
		terminateOnTrue = true;
	}
	
	
	/**
	 * Initializes the propositional function that will cause the state to be terminal when any Grounded version of
	 * pf is true or alternatively false.
	 * @param pf the propositional function that must have a true grounded version for the state to be terminal.
	 * @param terminateOnTrue when true requires a grounded version of pf to be true for the state to be terminal. When false requires a grounded version to be false to be terminal.
	 */
	public SingleLETF(LogicalExpression le, boolean terminateOnTrue){
		this.le = le;
		this.terminateOnTrue = terminateOnTrue;
	}
	
	
	/**
	 * Sets whether to be terminal state it is required for there to be a true grounded version of this class' propositional function
	 * or whether it is required for there to be a false grounded version.
	 * @param terminateOnTrue if true then there must be a true grounded prop; if false then there must be a false grounded prop.
	 */
	public void setTerminateOnTrue(boolean terminateOnTrue){
		this.terminateOnTrue = terminateOnTrue;
	}
	
	@Override
	public boolean isTerminal(State s) {
		
		if(terminateOnTrue) {
			if(this.le.evaluateIn(s)){
				return true;
			}
		}
		else {
			if(!this.le.evaluateIn(s)){
				return true;
			}
		}
		
		return false;
	}

}
