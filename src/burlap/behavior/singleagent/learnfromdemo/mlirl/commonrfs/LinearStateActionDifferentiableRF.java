package burlap.behavior.singleagent.learnfromdemo.mlirl.commonrfs;

import burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableRF;
import burlap.behavior.singleagent.vfa.FunctionGradient;
import burlap.behavior.singleagent.vfa.ParametricFunction;
import burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.GroundedAction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A class for defining a state-action linear {@link burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableRF}.
 * The class takes as input a {@link burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator} and the set of possible
 * grounded actions that can be applied in the world. The dimensionality of this reward function is equal to |A|*|f|,
 * where A is the set of possible grounded actions, and |f| is the state feature vector dimensionality.
 * <p>
 * The reward function is defined as R(s, a, s') = w(a) * f(s), where w(a) is the set of weights (the parameters) of this
 * reward functions associated with action a, * is the dot product operator, and f(s) is the feature vector for state s.
 * <p>
 * Note that when the gradient is a vector of size |A||f|, since the feature vector is replicated for each action, and the gradient
 * for all entries associated with an action other than the one taken in the (s, a, s') query will have a gradient value of zero.
 * <p>
 * The set of possible grounded actions must be defined either in the {@link #LinearStateActionDifferentiableRF(burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator, int, burlap.mdp.singleagent.GroundedAction...)}
 * constructor, or added iteratively with the {@link #addAction(burlap.mdp.singleagent.GroundedAction)} method.
 * @author James MacGlashan.
 */
public class LinearStateActionDifferentiableRF implements DifferentiableRF {

	/**
	 * An ordering of grounded actions
	 */
	protected Map<GroundedAction, Integer> 		actionMap;

	/**
	 * The parameters of this reward function
	 */
	protected double [] 						parameters;


	/**
	 * The dimension of this reward function
	 */
	protected int								dim;

	/**
	 * The state feature vector generator to use
	 */
	protected StateToFeatureVectorGenerator 	fvGen;

	/**
	 * The number of state features
	 */
	protected int 								numStateFeatures;

	/**
	 * The number of possible grounded Actions
	 */
	int 										numActions = 0;


	/**
	 * Initializes. If not all possible grounded actions are provided, they can be defined/added later with the
	 * {@link #addAction(burlap.mdp.singleagent.GroundedAction)} method.
	 * @param stateFeatures the state feature vector generator
	 * @param numStateFeatures the dimensionality of the state feature vector
	 * @param allPossibleActions the set of possible grounded actions.
	 */
	public LinearStateActionDifferentiableRF(StateToFeatureVectorGenerator stateFeatures, int numStateFeatures, GroundedAction...allPossibleActions){
		this.fvGen = stateFeatures;
		this.numStateFeatures = numStateFeatures;
		this.actionMap = new HashMap<GroundedAction, Integer>(allPossibleActions.length);
		for(int i = 0; i < allPossibleActions.length; i++){
			this.actionMap.put(allPossibleActions[i], i);
		}
		this.numActions = allPossibleActions.length;
		this.parameters = new double[numActions*this.numStateFeatures];
		this.dim = this.numActions*this.numStateFeatures;
	}


	/**
	 * Adds a possible grounded action. This addition increases the dimensionality of this reward function
	 * by |f| where |f| is the dimensionality of the state feature vector.
	 * @param ga the possible grounded action to add to this reward function's definition.
	 */
	public void addAction(GroundedAction ga){
		this.actionMap.put(ga, this.numActions);
		this.numActions++;
		this.parameters = new double[numActions*this.numStateFeatures];
		this.dim = this.numActions*this.numStateFeatures;
	}


	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		double [] sFeatures = this.fvGen.generateFeatureVectorFrom(s);
		int sIndex = this.actionMap.get(a) * this.numStateFeatures;
		double sum = 0.;
		for(int i = sIndex; i < sIndex + this.numStateFeatures; i++){
			sum += this.parameters[i]*sFeatures[i-sIndex];
		}
		return sum;
	}


	@Override
	public FunctionGradient gradient(State s, GroundedAction a, State sprime) {
		double [] sFeatures = this.fvGen.generateFeatureVectorFrom(s);
		int sIndex = this.actionMap.get(a) * this.numStateFeatures;

		FunctionGradient gradient = new FunctionGradient.SparseGradient(sFeatures.length);
		int soff = this.numStateFeatures*this.numActions;
		for(int i = 0; i < sFeatures.length; i++){
			int f = i + soff;
			gradient.put(f, sFeatures[i]);
		}

		return gradient;
	}

	@Override
	public int numParameters() {
		return this.dim;
	}

	@Override
	public double getParameter(int i) {
		return this.parameters[i];
	}

	@Override
	public void setParameter(int i, double p) {
		this.parameters[i] = p;
	}

	@Override
	public void resetParameters() {
		for(int i = 0; i < this.parameters.length; i++){
			this.parameters[i] = 0.;
		}
	}

	@Override
	public ParametricFunction copy() {
		LinearStateActionDifferentiableRF rf = new LinearStateActionDifferentiableRF(this.fvGen, this.numStateFeatures);
		for(Map.Entry<GroundedAction, Integer> e : this.actionMap.entrySet()){
			rf.actionMap.put(e.getKey(), e.getValue());
		}
		rf.parameters = this.parameters.clone();
		return rf;
	}

	@Override
	public String toString() {
		return Arrays.toString(this.parameters);
	}
}
