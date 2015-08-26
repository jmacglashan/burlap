package burlap.behavior.singleagent.vfa.fourier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.behavior.singleagent.vfa.ActionFeaturesQuery;
import burlap.behavior.singleagent.vfa.FeatureDatabase;
import burlap.behavior.singleagent.vfa.StateFeature;
import burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator;
import burlap.behavior.singleagent.vfa.ValueFunctionApproximation;
import burlap.behavior.singleagent.vfa.common.ConcatenatedObjectFeatureVectorGenerator;
import burlap.behavior.singleagent.vfa.common.LinearVFA;
import burlap.oomdp.core.AbstractObjectParameterizedGroundedAction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * An implementation of Fourier Basis functions [1]. This class expects a normalized state variable/feature vector of input states, if it is not normalized, behavior is not well defined. Therefore consider using the 
 * {@link ConcatenatedObjectFeatureVectorGenerator}
 * generator with the normalization flag set to convert the OO-MDP {@link State} objects into the necessary input vector.
 * The higher order the basis functions, the higher the VFA resolution is. Typically, order n will produce (n+1)^d state basis functions (and a copy for each action), where d is the number of state variables. Since this grows quickly,
 * a way to manage the complexity is to simplify the number of coefficient vectors. That is, each basis function is a function of the dot product of the input state variable vector and a coefficient vector {0...n}^d 
 * and normally all possible coefficient vectors (and their corresponding basis functions) for
 * order n are produced. However, this class can be told to limit the permitted coefficient vectors to those that have no more than k non-zero entries in the coefficient vector. When k = 1, all features are treated as indepdent resulting
 * in n*d basis functions.
 * <p/>
 * When using a learning algorithm like {@link GradientDescentSarsaLam} with Fourier basis functions, it is typically a good idea to use the {@link FourierBasisLearningRateWrapper}, which scales the normal learning rate by the inverse of the norm
 * of a basis function's coefficient vector. 
 * 
 * <p/>
 * 1. G.D. Konidaris, S. Osentoski and P.S. Thomas. Value Function Approximation in Reinforcement Learning using the Fourier Basis. In Proceedings of the Twenty-Fifth Conference on Artificial Intelligence, pages 380-385, August 2011.
 * 
 * @author James MacGlashan
 *
 */
public class FourierBasis implements FeatureDatabase {

	/**
	 * The number of state varibles on which the produced basis functions operate
	 */
	protected int								numStateVariables;
	
	/**
	 * The OO-MDP {@link State} to feature vector/variable generator. Should produced normalized values.
	 */
	protected StateToFeatureVectorGenerator		featureVectorGenerator;
	
	/**
	 * The coefficient vectors used
	 */
	protected List<short[]>						coefficientVectors;
	
	/**
	 * The maximum number of non-zero coefficient entries permitted in a coefficient vector
	 */
	protected int								maxNonZeroCoefficents;
	
	/**
	 * The order of the Fourier basis functions.
	 */
	protected int								order;
	
	/**
	 * A map for returning a multiplier to the number of state features for each action. Effectively
	 * this ensures a unique feature ID for each Fourier basis function for each action.
	 */
	protected Map<GroundedAction, Integer> actionFeatureMultiplier = new HashMap<GroundedAction, Integer>();
	
	
	/**
	 * The next action Fourier basis function size multiplier to use for the next newly seen action.
	 */
	protected int nextActionMultiplier = 0;
	
	
	
	
	/**
	 * Initializes. The coefficient vectors used by this Fourier Basis function will be generated lazily when the first features for an input state/state-action pair are queried.
	 * The maximum number of non-zero coefficient entries in a coefficient vector will be set to the maixmum (the state variable dimensionality).
	 * @param featureVectorGenerator the state feature vector generator that turns OO-MDP {@link State} objects into double arrays.
	 * @param order the Fourier basis order
	 */
	public FourierBasis(StateToFeatureVectorGenerator featureVectorGenerator, int order){
		this.featureVectorGenerator = featureVectorGenerator;
		this.order = order;
		this.maxNonZeroCoefficents = -1;
	}
	
	/**
	 * Initializes. The coefficient vectors used by this Fourier Basis function will be generated lazily when the first features for an input state/state-action pair are queried.
	 * Setting maxNonZeroCoefficents to one results in treating each state variable as indepdent and thereby producing order*d basis functions (for each action) where d is the number
	 * of state variables. Larger values of maxNonZeroCoefficents will result in more variable dependency conbinations.
	 * @param featureVectorGenerator the state feature vector generator that turns OO-MDP {@link State} objects into double arrays
	 * @param order the fourier basis order
	 * @param maxNonZeroCoefficents the maximum number of entries in coeffient vectors that can have non-zero values. 
	 */
	public FourierBasis(StateToFeatureVectorGenerator featureVectorGenerator, int order, int maxNonZeroCoefficents){
		this.featureVectorGenerator = featureVectorGenerator;
		this.order = order;
		this.maxNonZeroCoefficents = maxNonZeroCoefficents;
	}
	
	
	/**
	 * Forces the set of coefficient vectors (and thereby Fourier basis functions) used. Use this method only if you want to fine tune the basis functions used.
	 * @param coefficientVectors the coefficient vectors used to produce the Fourier basis functions.
	 */
	public void setCoefficientVectors(List<short[]> coefficientVectors){
		this.coefficientVectors = coefficientVectors;
	}
	
	
	/**
	 * Returns the basis function value for the given state input for the given basis function index. The basisFunction index may be greater than the number of coefficient vectors because
	 * it may refer to an state-action feature's basis function (and there is a copy of each basis function for each action). The basis function used will be the one associated with the coefficient vector at index basisFunction % m, where m
	 * is the number of this object's coefficient vectors.
	 * @param input the state variables
	 * @param basisFunction the basis function index
	 * @return the value of the basis function for the given input state variables
	 */
	public double basisValue(double [] input, int basisFunction){
		short [] coefficientVector = this.coefficientVectors.get(basisFunction % this.coefficientVectors.size());
		if(coefficientVector.length != input.length){
			throw new RuntimeException("Error in Fourier Basis function evaluation: expected input state variable vector of size " + this.numStateVariables + ", but received one of dimension " + input.length);
		}
		//dot product of input and coefficient vector
		double sum = 0.;
		for(int i = 0; i < this.numStateVariables; i++){
			sum += input[i] * (double)coefficientVector[i];
		}
		
		//get cos function of it
		sum *= Math.PI;
		sum = Math.cos(sum);
		return sum;
	}
	
	@Override
	public List<StateFeature> getStateFeatures(State s) {
		
		double [] input = this.featureVectorGenerator.generateFeatureVectorFrom(s);
		if(this.coefficientVectors == null){
			this.numStateVariables = input.length;
			if(this.maxNonZeroCoefficents == -1){
				this.maxNonZeroCoefficents = this.numStateVariables;
			}
			this.generateCoefficientVectors();
		}
		
		List<StateFeature> res = new ArrayList<StateFeature>(this.coefficientVectors.size());
		
		for(int i = 0; i < this.coefficientVectors.size(); i++){
			double value = this.basisValue(input, i);
			StateFeature sf = new StateFeature(i, value);
			res.add(sf);
		}
		
		
		return res;
	}

	@Override
	public List<ActionFeaturesQuery> getActionFeaturesSets(State s,
			List<GroundedAction> actions) {
		
		List<ActionFeaturesQuery> lstAFQ = new ArrayList<ActionFeaturesQuery>();
		
		List<StateFeature> sfs = this.getStateFeatures(s);
		
		for(GroundedAction ga : actions){
			int actionMult = this.getActionMultiplier(ga);
			int indexOffset = actionMult*this.coefficientVectors.size();
			
			ActionFeaturesQuery afq = new ActionFeaturesQuery(ga);
			for(StateFeature sf : sfs){
				afq.addFeature(new StateFeature(sf.id + indexOffset, sf.value));
			}
			
			lstAFQ.add(afq);
			
		}
		
		return lstAFQ;
		
	}

	@Override
	public void freezeDatabaseState(boolean toggle) {
		//nothing to do
	}

	@Override
	public int numberOfFeatures() {
		if(this.coefficientVectors == null){
			return 0;
		}
		if(this.nextActionMultiplier == 0){
			return this.coefficientVectors.size();
		}
		return this.coefficientVectors.size()*this.nextActionMultiplier;
	}
	
	
	/**
	 * Returns the coefficient vector for the given basis function index. The basisFunction index may be greater than the number of coefficient vectors because
	 * it may refer to an state-action feature's basis function (and there is a copy of each basis function for each action). The coefficient vector returned is at the index basisFunction % m, where m
	 * is the number of this object's coefficient vectors.
	 * @param i the basis function index
	 * @return the coefficient vector for the given basis function
	 */
	public short [] getCoefficientVector(int i){
		return this.coefficientVectors.get(i % this.coefficientVectors.size());
	}
	
	
	/**
	 * Returns the norm of the coefficient vector for the given basis function index. The basisFunction index may be greater than the number of coefficient vectors because
	 * it may refer to an state-action feature's basis function (and there is a copy of each basis function for each action). The coefficient vector returned is at the index basisFunction % m, where m
	 * is the number of this object's coefficient vectors.
	 * @param i the basis function index
	 * @return the norm of the coefficient vector for the given basis function
	 */
	public double coefficientNorm(int i){
		short [] vector = this.coefficientVectors.get(i % this.coefficientVectors.size());
		double sum = 0.;
		for(short c : vector){
			sum += (double)c*(double)c;
		}
		sum = Math.sqrt(sum);
		return sum;
	}
	
	
	/**
	 * Creates and returns a linear VFA object over this Fourier basis feature database.
	 * @param defaultWeightValue the default feature weight value to use for all features
	 * @return a linear VFA object over this Fourier basis feature database.
	 */
	public ValueFunctionApproximation generateVFA(double defaultWeightValue)
	{
		return new LinearVFA(this, defaultWeightValue);
	}
	
	
	/**
	 * Generates all coefficient vectors given the number of state variables and the maximum number of non-zero coefficient element entries.
	 */
	protected void generateCoefficientVectors(){
		this.coefficientVectors = new ArrayList<short[]>();
		short [] tempVector = new short[this.numStateVariables];
		this.generateCoefficientVectorsHelper(0, tempVector, 0);
	}
	
	
	/**
	 * Recursive cofficient generator helper method. Once a permitted coefficient vector is fully generated, it is copied and added to this object's list of coefficient vectors.
	 * @param index the index into the coefficient vector that needs to have its values filled in.
	 * @param vector the coefficient vector generated thus far
	 * @param numNonZeroEntries the number of non-zero coefficient vector entires currently in the vector.
	 */
	protected void generateCoefficientVectorsHelper(int index, short[] vector, int numNonZeroEntries){
		
		//base case is we're at the end of the vector
		if(index == this.numStateVariables){
			this.coefficientVectors.add(vector.clone());
			return;
		}
		
		//otherwise, consider all possible values for this vector provided we don't have too many non-zero entries
		if(numNonZeroEntries >= this.maxNonZeroCoefficents){
			vector[index] = 0;
			this.generateCoefficientVectorsHelper(index+1, vector, numNonZeroEntries);
		}
		else{
			//consider all possible values
			for(short i = 0; i <= this.order; i++){
				vector[index] = i;
				if(i > 0){
					this.generateCoefficientVectorsHelper(index+1, vector, numNonZeroEntries+1);
				}
				else{
					this.generateCoefficientVectorsHelper(index+1, vector, numNonZeroEntries);
				}
			}
		}
		
	}

	
	
	/**
	 * This method returns the action multiplier for the specified grounded action.
	 * If the action is not stored, a new action multiplier will created, stored, and returned.
	 * If the action is parameterized a runtime exception is thrown.
	 * @param ga the grounded action for which the multiplier will be returned
	 * @return the action multiplier to be applied to a state feature id.
	 */
	protected int getActionMultiplier(GroundedAction ga){
		
		if(ga instanceof AbstractObjectParameterizedGroundedAction){
			throw new RuntimeException("Fourier Basis Feature Database does not support AbstractObjectParameterizedGroundedActions");
		}
		
		Integer stored = this.actionFeatureMultiplier.get(ga);
		if(stored == null){
			this.actionFeatureMultiplier.put(ga, this.nextActionMultiplier);
			stored = this.nextActionMultiplier;
			this.nextActionMultiplier++;
		}
		
		return stored;
	}
	
	
	
}
