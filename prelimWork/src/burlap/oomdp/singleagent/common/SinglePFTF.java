package burlap.oomdp.singleagent.common;

import java.util.List;

import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;


/**
 * This class defines a terminal function that terminates in states where there exists a grounded version of a specified
 * propositional function that is true in the state or alternatively, when there is a grounded version that is false in the state.
 * @author James MacGlashan
 *
 */
public class SinglePFTF implements TerminalFunction {

	PropositionalFunction			pf;
	boolean							terminateOnTrue;
	
	
	/**
	 * Initializes the propositional function that will cause the state to be terminal when any Grounded version of
	 * pf is true.
	 * @param pf the propositional function that must have a true grounded version for the state to be terminal.
	 */
	public SinglePFTF(PropositionalFunction pf){
		this.pf = pf;
		terminateOnTrue = true;
	}
	
	
	/**
	 * Initializes the propositional function that will cause the state to be terminal when any Grounded version of
	 * pf is true or alternatively false.
	 * @param pf the propositional function that must have a true grounded version for the state to be terminal.
	 * @param terminateOnTrue when true requires a grounded version of pf to be true for the state to be terminal. When false requires a grounded version to be false to be terminal.
	 */
	public SinglePFTF(PropositionalFunction pf, boolean terminateOnTrue){
		this.pf = pf;
		this.terminateOnTrue = terminateOnTrue;
	}
	
	
	/**
	 * Sets whether to be terminal state it is required for there to be a true grounded version of this classes propositional function
	 * or whether it is required for there to be a false grounded version.
	 * @param terminateOnTrue if true then there must be a true grounded prop; if false then there must be a false grounded prop.
	 */
	public void setTerminateOnTrue(boolean terminateOnTrue){
		this.terminateOnTrue = terminateOnTrue;
	}
	
	@Override
	public boolean isTerminal(State s) {
		List<GroundedProp> gps = s.getAllGroundedPropsFor(pf);
		if(terminateOnTrue){
			for(GroundedProp gp : gps){
				if(gp.isTrue(s)){
					return true;
				}
			}
		}
		else{
			for(GroundedProp gp : gps){
				if(!gp.isTrue(s)){
					return true;
				}
			}
		}
		
		return false;
	}

}
