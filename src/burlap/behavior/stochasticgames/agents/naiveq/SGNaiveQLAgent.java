package burlap.behavior.stochasticgames.agents.naiveq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.policy.Policy;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.auxiliary.common.NullAbstractionNoCopy;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.SGAgent;
import burlap.oomdp.stochasticgames.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SGAgentAction;

/**
 * A Tabular Q-learning [1] algorithm for stochastic games formalisms. This algorithm ignores the actions of other agents and treats the outcomes
 * from their decisions as if they're part of the environment transition dynamics, hence the "naive" qualifier.
 * 
 * <p/>
 * 1. Watkins, Christopher JCH, and Peter Dayan. "Q-learning." Machine learning 8.3-4 (1992): 279-292. <br/>
 * @author James MacGlashan
 *
 */
public class SGNaiveQLAgent extends SGAgent implements QFunction {

	/**
	 * The tabular map from (hashed) states to the list of Q-values for each action in those states
	 */	
	protected Map<HashableState, List<QValue>>							qMap;
	
	/**
	 * A map from hashed states to the internal state representation for the states stored in the q-table. 
	 * This is useful since two identical states may have different object instance name identifiers
	 * that can affect the parameters in GroundedActions.
	 * 
	 */
	protected Map <HashableState, State>								stateRepresentations;
	
	/**
	 * A state abstraction to use.
	 */
	protected StateAbstraction											storedMapAbstraction;
	
	/**
	 * The discount factor
	 */
	protected double													discount;
	
	/**
	 * the learning rate
	 */
	protected LearningRate												learningRate;
	
	/**
	 * Defines how q-values are initialized
	 */
	protected ValueFunctionInitialization								qInit;
	

	/**
	 * The policy this agent follows
	 */
	protected Policy													policy;
	
	/**
	 * The state hashing factory to use.
	 */
	protected HashableStateFactory hashFactory;
	
	
	/**
	 * The total number of learning steps performed by this agent.
	 */
	protected int													totalNumberOfSteps = 0;
	
	
	/**
	 * Initializes with a default Q-value of 0 and a 0.1 epsilon greedy policy/strategy
	 * @param d the domain in which the agent will act
	 * @param discount the discount factor
	 * @param learningRate the learning rate
	 * @param hashFactory the state hashing factory
	 */
	public SGNaiveQLAgent(SGDomain d, double discount, double learningRate, HashableStateFactory hashFactory) {
		this.init(d);
		this.discount = discount;
		this.learningRate = new ConstantLR(learningRate);
		this.hashFactory = hashFactory;
		this.qInit = new ValueFunctionInitialization.ConstantValueFunctionInitialization(0.);
		
		this.qMap = new HashMap<HashableState, List<QValue>>();
		stateRepresentations = new HashMap<HashableState, State>();
		this.policy = new EpsilonGreedy(this, 0.1);
		
		this.storedMapAbstraction = new NullAbstractionNoCopy();
	}
	
	
	/**
	 * Initializes with a default 0.1 epsilon greedy policy/strategy
	 * @param d the domain in which the agent will act
	 * @param discount the discount factor
	 * @param learningRate the learning rate
	 * @param defaultQ the default to which all Q-values will be initialized
	 * @param hashFactory the state hashing factory
	 */
	public SGNaiveQLAgent(SGDomain d, double discount, double learningRate, double defaultQ, HashableStateFactory hashFactory) {
		this.init(d);
		this.discount = discount;
		this.learningRate = new ConstantLR(learningRate);
		this.hashFactory = hashFactory;
		this.qInit = new ValueFunctionInitialization.ConstantValueFunctionInitialization(defaultQ);
		
		this.qMap = new HashMap<HashableState, List<QValue>>();
		stateRepresentations = new HashMap<HashableState, State>();
		this.policy = new EpsilonGreedy(this, 0.1);
		
		this.storedMapAbstraction = new NullAbstractionNoCopy();
	}
	
	/**
	 * Initializes with a default 0.1 epsilon greedy policy/strategy
	 * @param d the domain in which the agent will act
	 * @param discount the discount factor
	 * @param learningRate the learning rate
	 * @param qInitizalizer the Q-value initialization method
	 * @param hashFactory the state hashing factory
	 */
	public SGNaiveQLAgent(SGDomain d, double discount, double learningRate, ValueFunctionInitialization qInitizalizer, HashableStateFactory hashFactory) {
		this.init(d);
		this.discount = discount;
		this.learningRate = new ConstantLR(learningRate);
		this.hashFactory = hashFactory;
		this.qInit = qInitizalizer;
		
		this.qMap = new HashMap<HashableState, List<QValue>>();
		stateRepresentations = new HashMap<HashableState, State>();
		this.policy = new EpsilonGreedy(this, 0.1);
		
		this.storedMapAbstraction = new NullAbstractionNoCopy();
	}
	
	/**
	 * Sets the state abstraction that this agent will use
	 * @param abstraction the state abstraction that this agent will use
	 */
	public void setStoredMapAbstraction(StateAbstraction abstraction){
		this.storedMapAbstraction = abstraction;
	}
	
	/**
	 * Sets the Q-learning policy that this agent will use (e.g., epsilon greedy)
	 * @param policy the Q-learning policy that this agent will use
	 */
	public void setStrategy(Policy policy){
		this.policy = policy;
	}
	
	public void setQValueInitializer(ValueFunctionInitialization qInit){
		this.qInit = qInit;
	}
	
	public void setLearningRate(LearningRate lr){
		this.learningRate = lr;
	}
	

	@Override
	public void gameStarting() {
		//nothing to do

	}

	@Override
	public GroundedSGAgentAction getAction(State s) {
		return (GroundedSGAgentAction)this.policy.getAction(s);
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction, Map<String, Double> jointReward, State sprime, boolean isTerminal) {
		
		if(internalRewardFunction != null){
			jointReward = internalRewardFunction.reward(s, jointAction, sprime);
		}
		
		GroundedSGAgentAction myAction = jointAction.action(worldAgentName);

		double r = jointReward.get(worldAgentName);
		QValue qv = this.getQ(s, myAction);
		
		double maxQ = 0.;
		if(!isTerminal){
			maxQ = this.getMaxQValue(sprime);
		}
		

		qv.q = qv.q + this.learningRate.pollLearningRate(this.totalNumberOfSteps, s, myAction) * (r + (this.discount * maxQ) - qv.q);
		
		this.totalNumberOfSteps++;

	}

	@Override
	public void gameTerminated() {
		//nothing to do

	}
	
	
	
	
	/**
	 * Returns maximum numeric Q-value for a given state
	 * @param s the state for which the max Q-value should be returned
	 * @return maximum numeric Q-value for a given state
	 */
	protected double getMaxQValue(State s){
		List<QValue> qs = this.getQs(s);
		double maxQ = Double.NEGATIVE_INFINITY;
		for(QValue q : qs){
			maxQ = Math.max(maxQ, q.q);
		}
		return maxQ;
	}
	
	
	/**
	 * First abstracts state s, and then returns the {@link burlap.oomdp.statehashing.HashableState} object for the abstracted state.
	 * @param s the state for which the state hash should be returned.
	 * @return the hashed state.
	 */
	protected HashableState stateHash(State s){
		State abstracted = this.storedMapAbstraction.abstraction(s);
		return hashFactory.hashState(abstracted);
	}
	
	
	/**
	 * Takes an input action and mapping objects in the source state for the action to objects in another state
	 * and returns a action with its object parameters mapped to the matched objects.
	 * @param a the input action
	 * @param matching the matching between objects from the source state in which the action was generated to objects in another state.
	 * @return an action with its object parameters mapped according to the state object matching.
	 */
	protected GroundedSGAgentAction translateAction(GroundedSGAgentAction a, Map <String,String> matching){
		String [] newParams = new String[a.params.length];
		for(int i = 0; i < a.params.length; i++){
			newParams[i] = matching.get(a.params[i]);
		}
		return new GroundedSGAgentAction(worldAgentName, a.action, newParams);
	}


	@Override
	public List<QValue> getQs(State s) {
		
		List<GroundedSGAgentAction> gsas = SGAgentAction.getAllPossibleGroundedSingleActions(s, worldAgentName, agentType.actions);
		
		HashableState shq = this.stateHash(s);
		
		State storedRep = stateRepresentations.get(shq);
		if(storedRep == null){
			//no existing entry so we can create it
			stateRepresentations.put(shq, shq.s);
			List <QValue> entries = new ArrayList<QValue>();
			for(GroundedSGAgentAction gsa : gsas){
				QValue q = new QValue(shq.s, gsa, this.qInit.qValue(shq.s, gsa));
				entries.add(q);
			}
			qMap.put(shq, entries);
			return entries;
			
		}
		
		//otherwise an entry exists and we need to do the matching
		
		List <QValue> entries = qMap.get(shq);
		List <QValue> returnedEntries = new ArrayList<QValue>(gsas.size());
		Map <String, String> matching = null;
		for(GroundedSGAgentAction gsa :gsas){
			GroundedSGAgentAction transgsa = gsa;
			if(gsa.isParameterized() && !this.domain.isObjectIdentifierDependent() && gsa.parametersAreObjects()){
				if(matching == null){
					matching = shq.s.getObjectMatchingTo(storedRep, false);
				}
				transgsa = this.translateAction(gsa, matching);
			}
			
			//find matching action in entry list
			boolean foundMatch = false;
			for(QValue qe : entries){
				if(qe.a.equals(transgsa)){
					returnedEntries.add(qe);
					foundMatch = true;
					break;
				}
			}
			
			if(!foundMatch){
				QValue qe = new QValue(shq.s, transgsa, this.qInit.qValue(shq.s, transgsa));
				entries.add(qe);
				returnedEntries.add(qe);
			}
			
			
		}
		
		if(returnedEntries.size() == 0){
			throw new RuntimeException();
		}
		
		return returnedEntries;
		
	}


	@Override
	public double value(State s) {
		return QFunction.QFunctionHelper.getOptimalValue(this, s);
	}


	@Override
	public QValue getQ(State s, AbstractGroundedAction a) {
		
		GroundedSGAgentAction gsa = (GroundedSGAgentAction)a;
		
		HashableState shq = this.stateHash(s);
		
		State storedRep = stateRepresentations.get(shq);
		if(storedRep == null){
			//no existing entry so we can create it
			stateRepresentations.put(shq, shq.s);
			QValue q = new QValue(storedRep, gsa, this.qInit.qValue(shq.s, gsa));
			List <QValue> entries = new ArrayList<QValue>();
			entries.add(q);
			qMap.put(shq, entries);
			return q;
		}
		
		if(gsa.isParameterized() && !this.domain.isObjectIdentifierDependent() && a.parametersAreObjects()){
			//then we'll need to translate this action to match the internal state representation
			Map <String, String> matching = shq.s.getObjectMatchingTo(storedRep, false);
			gsa = this.translateAction(gsa, matching);
		}
		
		List <QValue> entries = qMap.get(shq);
		for(QValue qe : entries){
			if(qe.a.equals(gsa)){
				return qe;
			}
		}
		
		//if we got here then there are no entries for this action
		QValue qe = new QValue(shq.s, gsa, this.qInit.qValue(shq.s, gsa));
		entries.add(qe);
		
		return qe;
		
	}
	
	

}
