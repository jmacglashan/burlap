package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import java.util.List;

import burlap.behavior.singleagent.learning.modellearning.Model;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class OOMDPModel extends Model {

	



	@Override
	public RewardFunction getModelRF() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TerminalFunction getModelTF() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean transitionIsModeled(State s, GroundedAction ga) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stateTransitionsAreModeled(State s) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<AbstractGroundedAction> getUnmodeledActionsForState(State s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public State sampleModelHelper(State s, GroundedAction ga) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TransitionProbability> getTransitionProbabilities(State s,
			GroundedAction ga) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateModel(State s, GroundedAction ga, State sprime, double r,
			boolean sprimeIsTerminal) {


	}

	@Override
	public void resetModel() {
		// TODO Auto-generated method stub

	}

}
