package burlap.oomdp.singleagent.pomdp.beliefstate.tabular;

import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.MutableState;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.pomdp.ObservationFunction;
import burlap.oomdp.singleagent.pomdp.PODomain;
import burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState;
import burlap.oomdp.singleagent.pomdp.beliefstate.DenseBeliefVector;
import burlap.oomdp.singleagent.pomdp.beliefstate.EnumerableBeliefState;

import java.util.*;

/**
 * A class for storing a sparse tabular representation of the belief state. That is, each MDP state is assigned a unique
 * identifier using a {@link burlap.behavior.singleagent.auxiliary.StateEnumerator} and this class uses a {@link java.util.Map}
 * to associated the probability mass with each state. MDP states that have zero mass are not stored in the map.
 * <p>
 * If using a BeliefMDP solver with a {@link burlap.oomdp.singleagent.pomdp.beliefstate.tabular.TabularBeliefState},
 * it is recommended that you use {@link burlap.oomdp.singleagent.pomdp.beliefstate.tabular.HashableTabularBeliefStateFactory}
 * which will compute hash codes and perform state equality checks with the sparse Map representation
 * (rather than the dense OO-MDP representation)
 * @author James MacGlashan.
 */
public class TabularBeliefState implements BeliefState, EnumerableBeliefState, DenseBeliefVector, MutableState{


	/**
	 * A state enumerator for determining the index of MDP states in the belief vector.
	 */
	protected StateEnumerator stateEnumerator;

	/**
	 * The belief vector, stored sparsely with a {@link java.util.Map}.
	 */
	protected Map<Integer, Double> beliefValues = new HashMap<Integer, Double>();


	/**
	 * The POMDP domain with which this belief state is associated. Contains the {@link burlap.oomdp.singleagent.pomdp.ObservationFunction} necessary to perform
	 * belief state updates.
	 */
	protected PODomain domain;

	public TabularBeliefState() {
	}

	/**
	 * Constructs a new {@link burlap.oomdp.singleagent.pomdp.beliefstate.tabular.TabularBeliefState} from a source
	 * {@link burlap.oomdp.singleagent.pomdp.beliefstate.tabular.TabularBeliefState}. Changes to the new state or source
	 * state will not affect the other.
	 * @param srcBeliefState the source {@link burlap.oomdp.singleagent.pomdp.beliefstate.tabular.TabularBeliefState} to copy.
	 */
	public TabularBeliefState(TabularBeliefState srcBeliefState){
		this(srcBeliefState.domain, srcBeliefState.stateEnumerator);
		this.beliefValues = new HashMap<Integer, Double>(srcBeliefState.beliefValues.size());
		for(Map.Entry<Integer, Double> e : srcBeliefState.beliefValues.entrySet()){
			this.beliefValues.put(e.getKey(), e.getValue());
		}
	}



	public TabularBeliefState(PODomain domain){
		if(!domain.providesStateEnumerator()){
			throw new RuntimeException("TabularBeliefState(PODomain domain) constructor requires that" +
					"the PODomain provides a StateEnumerator, but it does not. Alternatively consider using the" +
					"TabularBeliefState(PODomain domain, StateEnumerator stateEnumerator), where you can" +
					"specify your own StateEnumerator for lazy state indexing.");
		}
		this.domain = domain;
		this.stateEnumerator = domain.getStateEnumerator();
	}

	/**
	 * Constructs a new {@link burlap.oomdp.singleagent.pomdp.beliefstate.tabular.TabularBeliefState}.
	 * @param domain the {@link burlap.oomdp.singleagent.pomdp.PODomain} domain to which the belief state is associated.
	 * @param stateEnumerator a {@link burlap.behavior.singleagent.auxiliary.StateEnumerator} to index the states in the belief vector.
	 */
	public TabularBeliefState(PODomain domain, StateEnumerator stateEnumerator){
		this.domain = domain;
		this.stateEnumerator = stateEnumerator;
	}

	public StateEnumerator getStateEnumerator() {
		return stateEnumerator;
	}

	public void setStateEnumerator(StateEnumerator stateEnumerator) {
		this.stateEnumerator = stateEnumerator;
	}

	public Map<Integer, Double> getBeliefValues() {
		return beliefValues;
	}

	public void setBeliefValues(Map<Integer, Double> beliefValues) {
		this.beliefValues = beliefValues;
	}

	public PODomain getDomain() {
		return domain;
	}

	public void setDomain(PODomain domain) {
		this.domain = domain;
	}

	@Override
	public double belief(State s) {
		int sid = this.stateEnumerator.getEnumeratedID(s);
		return this.belief(sid);
	}

	/**
	 * Returns the value of the belief vector for the provided index.
	 * @param stateId the index (state identification number) of the belief vector to return.
	 * @return the value of the belief vector for the provided index.
	 */
	public double belief(int stateId){

		Double b = this.beliefValues.get(stateId);
		if(b == null){
			return 0.;
		}
		return b;
	}

	@Override
	public State sampleStateFromBelief() {
		double sumProb = 0.;
		double r = RandomFactory.getMapped(0).nextDouble();
		for(Map.Entry<Integer, Double> e : this.beliefValues.entrySet()){
			sumProb += e.getValue();
			if(r < sumProb){
				return this.stateEnumerator.getStateForEnumerationId(e.getKey());
			}
		}

		throw new RuntimeException("Error; could not sample from belief state because the beliefs did not sum to 1; they summed to: " + sumProb);
	}

	@Override
	public BeliefState getUpdatedBeliefState(State observation, GroundedAction ga) {
		ObservationFunction of = this.domain.getObservationFunction();
		double [] newBeliefStateVector = new double[this.numStates()];
		double sum = 0.;
		for(int i = 0; i < newBeliefStateVector.length; i++){
			State ns = this.stateForId(i);
			double op = of.getObservationProbability(observation, ns, ga);
			double transitionSum = 0.;
			for(Map.Entry<Integer, Double> srcStateEntry : this.beliefValues.entrySet()){
				double srcB = srcStateEntry.getValue();
				State srcState = this.stateEnumerator.getStateForEnumerationId(srcStateEntry.getKey());
				double tp = this.getTransitionProb(srcState, ga, ns);
				transitionSum += srcB * tp;
			}
			double numerator = op * transitionSum;
			sum += numerator;
			newBeliefStateVector[i] = numerator;

		}

		if(sum == 0. || Double.isNaN(sum)){
			throw new RuntimeException("getUpdatedBeliefState for TaubularBeliefState failed because the probability normalization is " + sum + "." +
					"\nFailed for action: " + ga.toString() + "\nAnd observation:\n" + observation.toString());
		}

		TabularBeliefState newBeliefState = new TabularBeliefState(this.domain, this.stateEnumerator);
		for(int i = 0; i < newBeliefStateVector.length; i++){
			double nb = newBeliefStateVector[i] / sum;
			newBeliefState.setBelief(i, nb);
		}


		return newBeliefState;
	}

	@Override
	public List<StateBelief> getStatesAndBeliefsWithNonZeroProbability(){
		List<StateBelief> result = new LinkedList<StateBelief>();
		for(Map.Entry<Integer, Double> e : this.beliefValues.entrySet()){
			StateBelief sb = new StateBelief(this.stateForId(e.getKey()), e.getValue());
			result.add(sb);
		}
		return result;
	}

	/**
	 * Returns the size of the observed underlying MDP state space.
	 * @return the size of the observed underlying MDP state space.
	 */
	public int numStates(){
		return this.stateEnumerator.numStatesEnumerated();
	}


	/**
	 * Returns the corresponding MDP state for the provided unique identifier.
	 * @param id the MDP state identifier
	 * @return the corresponding MDP state, defined by a {@link State}, for the provided unique identifier.
	 */
	public State stateForId(int id){
		return this.stateEnumerator.getStateForEnumerationId(id);
	}


	/**
	 * Sets the probability mass (belief) associated with the underlying MDP state. If this object has not yet assigned
	 * a unique identifier to the provided MDP state, then it will first create one. Note that using this method
	 * will not ensure that the total probability mass across this belief state sums to 1, so other changes will have
	 * to be specified manually.
	 * @param s the underlying MDP state defined as a {@link State}
	 * @param b the probability mass to assigned to the underlying MDP state.
	 */
	public void setBelief(State s, double b){
		int sid = this.stateEnumerator.getEnumeratedID(s);
		this.setBelief(sid, b);
	}


	/**
	 * Sets the probability mass (belief) associated with the underlying MDP state. Note that using this method
	 * will not ensure that the total probability mass across this belief state sums to 1, so other changes will have
	 * to be specified manually.
	 * @param stateId the unique numeric identifier of the underlying MDP state defined.
	 * @param b the probability mass to assigned to the underlying MDP state.
	 */
	public void setBelief(int stateId, double b){
		if(stateId < 0 || stateId > this.numStates()){
			throw new RuntimeException("Error; cannot set belief value for state id " + stateId + "; belief vector is of dimension " + this.numStates());
		}

		if(b != 0){
			this.beliefValues.put(stateId, b);
		}
		else{
			this.beliefValues.remove(stateId);
		}
	}


	/**
	 * Returns the probability that the underlying MDP will transition from state s to sp when taking action a in state s.
	 * @param s the previous MDP state defined by a {@link State}
	 * @param ga the taken action defined by a {@link burlap.oomdp.singleagent.GroundedAction}
	 * @param sp The next MDP state observed defined by a {@link State}.
	 * @return the probability that the underlying MDP will transition from state s to sp when taking action a in state s.
	 */
	protected double getTransitionProb(State s, GroundedAction ga, State sp){
		List<TransitionProbability> tps = ga.getTransitions(s);
		for(TransitionProbability tp : tps){
			if(tp.s.equals(sp)){
				return tp.p;
			}
		}
		return 0.;

	}

	/**
	 * Sets this belief state to the provided. Dense belief vector. If the belief vector dimensionality does not match
	 * this objects dimensionality then a runtime exception will be thrown.
	 * @param b the belief vector to set this belief state to.
	 */
	@Override
	public void setBeliefVector(double [] b){
		if(b.length != this.numStates()){
			throw new RuntimeException("Error; cannot set belief state with provided vector because dimensionality does not match." +
					"Provided vector of dimension " + b.length + " need dimension " + this.numStates());
		}

		for(int i = 0; i < b.length; i++){
			this.setBelief(i, b[i]);
		}
	}

	/**
	 * Returns this belief state as a dense (non-sparse) belief vector.
	 * @return a double array specifying this belief state as a dense (non-sparse) belief vector.
	 */
	@Override
	public double [] getBeliefVector(){
		double [] b = new double[this.numStates()];
		for(int i = 0; i < b.length; i++){
			b[i] = this.belief(i);
		}
		return b;
	}

	/**
	 * Returns the set of underlying MDP states this belief vector spans.
	 * @return the set of underlying MDP states this belief vector spans.
	 */
	public List<State> getStateSpace(){
		LinkedList<State> states = new LinkedList<State>();
		for(int i = 0; i < this.numStates(); i++){
			states.add(this.stateForId(i));
		}
		return states;
	}

	/**
	 * Sets this belief state to have zero probability mass for all underlying MDP states.
	 */
	public void zeroOutBeliefVector(){
		this.beliefValues.clear();
	}


	/**
	 * Initializes this belief state to a uniform distribution
	 */
	public void initializeBeliefsUniformly(){
		double b = 1. / (double)this.numStates();
		this.initializeAllBeliefValuesTo(b);
	}


	/**
	 * Initializes the probability mass of all underlying MDP states to the specified value. Note that this will not
	 * enforce summing to one, so you will have to manually modify the value of other states if initialValue is not
	 * 1/n where n is the number of underlying MDP states.
	 * @param initialValue the probability mass to assign to each state.
	 */
	public void initializeAllBeliefValuesTo(double initialValue){

		if(initialValue == 0){
			this.zeroOutBeliefVector();
		}
		else{
			for(int i = 0; i < this.numStates(); i++){
				this.setBelief(i, initialValue);
			}
		}
	}


	@Override
	public MutableState set(Object variableKey, Object value) {

		if(!(value instanceof Double)){
			throw new RuntimeException("Cannot set belief state value, because the value is a " + value.getClass().getName() + " not a Double");
		}

		Double val = (Double)value;

		if(variableKey instanceof Integer){
			this.setBelief((Integer)variableKey, val);
		}
		else if(variableKey instanceof State){
			this.setBelief((State)variableKey, val);
		}
		else if(variableKey instanceof String){
			try{
				int key = Integer.parseInt((String)variableKey);
				this.setBelief(key, val);
			}catch(Exception e){
				throw new RuntimeException("Could not set belief for TabularBeliefState because the key is a String, but does not parse into an int; it is " + variableKey);
			}
		}
		else{
			throw new RuntimeException("Cannot set belief for TabularBeliefState because they is a " + variableKey.getClass().getName() + " rather than a, Integer, State, or String representation of an int");
		}


		return this;
	}

	@Override
	public List<Object> variableKeys() {
		int max = this.stateEnumerator.numStatesEnumerated();
		List<Object> keys = new ArrayList<Object>(max);
		for(int i = 0; i < max; i++){
			keys.add(i);
		}
		return keys;
	}

	@Override
	public Object get(Object variableKey) {

		if(variableKey instanceof Integer){
			return this.belief((Integer)variableKey);
		}
		else if(variableKey instanceof State){
			return this.belief((State)variableKey);
		}
		else if(variableKey instanceof String){
			try{
				int key = Integer.parseInt((String)variableKey);
				return this.belief(key);
			}catch(Exception e){
				throw new RuntimeException("Could not return belief for TabularBeliefState because the key is a String, but does not parse into an int; it is " + variableKey);
			}
		}

		throw new RuntimeException("Cound not return belief value for key, because it is a " + variableKey.getClass().getName() + " rather than an Integer, State, or String representation of an integer");
	}

	@Override
	public State copy() {
		return new TabularBeliefState(this);
	}



	@Override
	public boolean equals(Object obj) {

		if(!(obj instanceof TabularBeliefState)){
			return false;
		}

		TabularBeliefState otb = (TabularBeliefState)obj;
		if(this.beliefValues.size() == otb.beliefValues.size()) {
			boolean match = true;
			for(Map.Entry<Integer, Double> e : this.beliefValues.entrySet()) {
				Double otherVal = otb.beliefValues.get(e.getKey());
				if(otherVal == null){
					return false;
				}
				if(Math.abs(otherVal - e.getValue()) > 1e-10){
					return false;
				}
			}
			return true;
		}

		return false;

	}
	
	public <T> State setObjectsValue(String objectName, String attName, T value) {
		throw new UnsupportedOperationException("TabularBeliefState cannot have OO-MDP objects added to it.");
	}
}
