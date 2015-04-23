package burlap.behavior.singleagent.options;

import java.util.List;

import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;



/**
 * This class is just an option wrapper of a standard primitive action. Typically this should be unnecessary
 * because options and actions can coexist in a planner, but if an algorithm wishes to homogenize its interface
 * it may be useful for it to wrap all of the primitives as options.
 * @author James MacGlashan
 *
 */
public class PrimitiveOption extends Option {

	
	/**
	 * The primitive action this option wraps.
	 */
	protected Action srcAction;
	
	
	/**
	 * Creates an option wrapper for a given primitive action. This option will take on the same name as the primitive action.
	 * @param srcAction the primitive action to wrap.
	 */
	public PrimitiveOption(Action srcAction){
		this.srcAction = srcAction;
		this.init(srcAction.getName(), srcAction.getDomain(), srcAction.getParameterClasses(), srcAction.getParameterOrderGroups());
	}

	
	@Override
	public boolean isPrimitive(){
		return true;
	}
	
	@Override
	public boolean isMarkov() {
		return true;
	}

	@Override
	public boolean usesDeterministicTermination() {
		return true;
	}

	@Override
	public boolean usesDeterministicPolicy() {
		return true;
	}

	@Override
	public double probabilityOfTermination(State s, String[] params) {
		return 1.0;
	}

	@Override
	public void initiateInStateHelper(State s, String[] params) {
		//no bookkeeping necessary
	}
	
	@Override
	public boolean applicableInState(State st, String [] params){
		return this.srcAction.applicableInState(st, params);
	}

	@Override
	public GroundedAction oneStepActionSelection(State s, String[] params) {
		return new GroundedAction(this.srcAction, params);
	}


	@Override
	public List<ActionProb> getActionDistributionForState(State s, String[] params) {
		return this.getDeterministicPolicy(s, params);
	}

}
