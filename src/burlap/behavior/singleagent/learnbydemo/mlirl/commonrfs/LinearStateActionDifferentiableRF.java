package burlap.behavior.singleagent.learnbydemo.mlirl.commonrfs;

import burlap.behavior.singleagent.learnbydemo.mlirl.support.DifferentiableRF;
import burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.HashMap;
import java.util.Map;

/**
 * A class for defining a state-action linear {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.DifferentiableRF}.
 * The class takes as input a {@link burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator} and the set of possible
 * grounded actions that can be applied in the world. The dimensionality of this reward function is equal to |A|*|f|,
 * where A is the set of possible grounded actions, and |f| is the state feature vector dimensionality.
 * <p/>
 * The reward function is defined as R(s, a, s') = w(a) * f(s), where w(a) is the set of weights (the parameters) of this
 * reward functions associated with action a, * is the dot product operator, and f(s) is the feature vector for state s.
 * <p/>
 * Note that when the gradient is a vector of size |A||f|, since the feature vector is replicated for each action, and the gradient
 * for all entries associated with an action other than the one taken in the (s, a, s') query will have a gradient value of zero.
 * <p/>
 * The set of possible grounded actions must be defined either in the {@link #LinearStateActionDifferentiableRF(burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator, int, burlap.oomdp.singleagent.GroundedAction...)}
 * constructor, or added iteratively with the {@link #addAction(burlap.oomdp.singleagent.GroundedAction)} method.
 * @author James MacGlashan.
 */
public class LinearStateActionDifferentiableRF extends DifferentiableRF {

	/**
	 * An ordering of grounded actions
	 */
	protected Map<GroundedAction, Integer> 		actionMap;

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
	 * {@link #addAction(burlap.oomdp.singleagent.GroundedAction)} method.
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
	protected DifferentiableRF copyHelper() {
		LinearStateActionDifferentiableRF rf = new LinearStateActionDifferentiableRF(this.fvGen, this.numStateFeatures);
		for(Map.Entry<GroundedAction, Integer> e : this.actionMap.entrySet()){
			rf.actionMap.put(e.getKey(), e.getValue());
		}
		return rf;
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
	public double[] getGradient(State s, GroundedAction ga, State sp) {
		double [] sFeatures = this.fvGen.generateFeatureVectorFrom(s);
		int sIndex = this.actionMap.get(ga) * this.numStateFeatures;
		double [] gradient = new double[this.numStateFeatures*this.numActions];
		this.copyInto(sFeatures, gradient, sIndex);

		return gradient;
	}


	/**
	 * The copies the values of source into the target, starting in target index position index. For example,
	 * target[index] = source[0]; target[index+1] = source[1]; etc.
	 * @param source the source values
	 * @param target the target array to receive the source values
	 * @param index the starting index in the target array into which the source values will be copied.
	 */
	protected void copyInto(double [] source, double [] target, int index){
		for(int i = index; i < index + source.length; i++){
			target[i] = source[i-index];
		}
	}

}
