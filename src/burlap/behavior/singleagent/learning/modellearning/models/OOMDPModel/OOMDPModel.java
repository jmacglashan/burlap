package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.learning.modellearning.Model;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * A model to represent the model presented in  
 * "An Object-Oriented Representation for Efficient Reinforcement Learning"
 * by Diuk et al.
 * @author Dhershkowitz
 *
 */
public class OOMDPModel extends Model {

	private PredictionsLearner pLearner;

	private Domain d;
	private int k;
	private RewardFunction rf;
	private TerminalFunction tf;
	private List<PropositionalFunction> relevantPropFuns;
	private List<String> effectsToUse;
	private State initialState;
	private String statePerceptionToUse;
	/**
	 * 
	 * @param d domain to use
	 * @param rf reward function to use
	 * @param tf terminal function to use
	 * @param relevantPropFuns propositional functions to plan over
	 * @param effectsToUse list of strings of effects to plan over (documented as static Strings in Effects.EffectHelpers)
	 * @param initialState the initialState to get grounded actions from
	 * @param k
	 * @param statePerceptionToUse string for how to featurize state for condition learner as gotten from StatePerceptions. If null will run classic DOORMAX with PFs.
	 */
	public OOMDPModel(Domain d, RewardFunction rf, TerminalFunction tf, List<PropositionalFunction> relevantPropFuns, List<String> effectsToUse, State initialState, int k, String statePerceptionToUse) {
		this.d = d;
		this.rf = rf;
		this.k = k;
		this.tf = tf;
		this.statePerceptionToUse = statePerceptionToUse;
		this.initialState = initialState;
		this.relevantPropFuns = relevantPropFuns;
		this.pLearner = new PredictionsLearner(d, relevantPropFuns, effectsToUse, d.getActions(), this.initialState, this.k, this.statePerceptionToUse);
		this.effectsToUse = effectsToUse;
	}

	public PredictionsLearner getPredictionsLearner() {
		return this.pLearner;
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
		State predictedState = this.pLearner.predict(s, ga);
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
		State resultingState = this.pLearner.predict(s, ga);
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
		this.pLearner.learn(s, ga, sprime);
	}

	@Override
	public void resetModel() {
		this.pLearner = new PredictionsLearner(d, relevantPropFuns, effectsToUse, d.getActions(), this.initialState, this.k, this.statePerceptionToUse);

	}

}

