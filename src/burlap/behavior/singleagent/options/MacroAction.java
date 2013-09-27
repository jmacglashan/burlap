package burlap.behavior.singleagent.options;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;


public class MacroAction extends Option {

	protected List<GroundedAction>				actionSequence;
	protected int								curIndex;
	
	
	public MacroAction(String name, List<GroundedAction> actionSequence){
		this.name = name;
		this.actionSequence = new ArrayList<GroundedAction>(actionSequence);
	}
	
	@Override
	public boolean isMarkov() {
		return false;
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
		if(curIndex >= actionSequence.size()){
			return 1.;
		}
		return 0.;
	}

	@Override
	public void initiateInStateHelper(State s, String[] params) {
		curIndex = 0;
	}

	@Override
	public GroundedAction oneStepActionSelection(State s, String[] params) {
		
		GroundedAction a = actionSequence.get(curIndex);
		curIndex++;
		
		return a;
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s, String[] params) {
		return this.getDeterministicPolicy(s, params);
	}

	

}
