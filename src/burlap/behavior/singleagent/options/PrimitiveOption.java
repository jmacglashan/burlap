package burlap.behavior.singleagent.options;

import java.util.List;

import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;


/*
 * This class is just an option wrapper for a primitive action
 * This may be useful if a caller wants to universally support the option interface
 * and wants to include primitive options
 */


public class PrimitiveOption extends Option {

	protected Action srcAction;
	
	
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
