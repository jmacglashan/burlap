package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.learning.modellearning.Model;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.Effect;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class OOMDPModel extends Model {

	public MultipleConditionEffectsLearner MCELearner;
	
	private Domain d;
	private RewardFunction rf;
	private TerminalFunction tf;
	List<PropositionalFunction> relevantPropFuns;
	
	public OOMDPModel(Domain d, RewardFunction rf, TerminalFunction tf, List<PropositionalFunction> relevantPropFuns) {
		this.d = d;
		this.rf = rf;
		this.tf = tf;
		this.relevantPropFuns = relevantPropFuns;
		this.MCELearner = new MultipleConditionEffectsLearner(d, relevantPropFuns);
	}

	@Override
	public RewardFunction getModelRF() {
		return this.rf;
	}

	@Override
	public TerminalFunction getModelTF() {
		return this.tf;
	}

	@Override
	public boolean transitionIsModeled(State s, GroundedAction ga) {
		State predictedState = this.MCELearner.predict(s, ga.action);
		return (predictedState != null);
	}

	@Override
	public boolean stateTransitionsAreModeled(State s) {
		List<AbstractGroundedAction> unmodeledActions = this.getUnmodeledActionsForState(s);
		return (unmodeledActions.size() == 0);
	}

	@Override
	public List<AbstractGroundedAction> getUnmodeledActionsForState(State s) {
		List<AbstractGroundedAction> toReturn = new ArrayList<AbstractGroundedAction>();
		List<Action> actions = d.getActions();
		for (Action a: actions) {
			List<GroundedAction> gas = a.getAllApplicableGroundedActions(s);
			for (GroundedAction ga: gas) {
				if (!this.transitionIsModeled(s, ga)) {
					toReturn.add(ga);
				}
			}			
		}
		return toReturn;
	}

	@Override
	public State sampleModelHelper(State s, GroundedAction ga) {
		return this.sampleTransitionFromTransitionProbabilities(s, ga);
	}

	@Override
	public List<TransitionProbability> getTransitionProbabilities(State s,GroundedAction ga) {
		List<TransitionProbability> toReturn = new ArrayList<TransitionProbability>();
		State resultingState = this.MCELearner.predict(s, ga.action);
		//Do know
		if (resultingState != null) {
			TransitionProbability TP = new TransitionProbability(resultingState, 1.0);	
			toReturn.add(TP);
		}
		//If don't know just transition to original state
		else {
			TransitionProbability TP = new TransitionProbability(s, 1.0);	
			toReturn.add(TP);
		}
		
		return toReturn;
	}

	@Override
	public void updateModel(State s, GroundedAction ga, State sprime, double r,boolean sprimeIsTerminal) {
		this.MCELearner.learn(s, ga.action, sprime);
	}

	@Override
	public void resetModel() {
		this.MCELearner = new MultipleConditionEffectsLearner(this.d, this.relevantPropFuns);

	}

}

