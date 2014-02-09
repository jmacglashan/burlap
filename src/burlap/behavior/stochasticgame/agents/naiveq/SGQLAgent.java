package burlap.behavior.stochasticgame.agents.naiveq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.behavior.stochasticgame.Strategy;
import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.auxiliary.common.NullAbstractionNoCopy;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.AgentType;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SingleAction;

/**
 * A Tabular Q-learning [1] algorithm for stochastic games formalisms. This algorithm ignores the actions of other agents and treats the outcomes
 * from their decisions as if they're part of the environment transition dynamics.
 * 
 * <p/>
 * 1. Watkins, Christopher JCH, and Peter Dayan. "Q-learning." Machine learning 8.3-4 (1992): 279-292. <br/>
 * @author James MacGlashan
 *
 */
public class SGQLAgent extends Agent {

	/**
	 * The tabular map from (hashed) states to the list of Q-values for each action in those states
	 */
	protected Map <StateHashTuple, List<SGQValue>>						qMap;
	
	/**
	 * A map from hashed states to the internal state representation for the states stored in the q-table. 
	 * This is useful since two identical states may have different object instance name identifiers
	 * that can affect the parameters in GroundedActions.
	 * 
	 */
	protected Map <StateHashTuple, State>								stateRepresentations;
	
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
	protected SGNQValueInitialization									qInit;
	
	/**
	 * The learning strategy to follow (e.g., epsilon greedy).
	 */
	protected Strategy													strategy;
	
	/**
	 * The state hashing factory to use.
	 */
	protected StateHashFactory											hashFactory;
	
	
	/**
	 * Initializes with a default Q-value of 0 and a 0.1 epsilon greedy policy/strategy
	 * @param d the domain in which the agent will act
	 * @param discount the discount factor
	 * @param learningRate the learning rate
	 * @param hashFactory the state hashing factory
	 */
	public SGQLAgent(SGDomain d, double discount, double learningRate, StateHashFactory hashFactory) {
		this.init(d);
		this.discount = discount;
		this.learningRate = new ConstantLR(learningRate);
		this.hashFactory = hashFactory;
		this.qInit = new SGNQValueInitialization.ConstantValueQInit(0.);
		
		this.qMap = new HashMap<StateHashTuple, List<SGQValue>>();
		stateRepresentations = new HashMap<StateHashTuple, State>();
		this.strategy = new SGEQGreedy(this, 0.1);
		
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
	public SGQLAgent(SGDomain d, double discount, double learningRate, double defaultQ, StateHashFactory hashFactory) {
		this.init(d);
		this.discount = discount;
		this.learningRate = new ConstantLR(learningRate);
		this.hashFactory = hashFactory;
		this.qInit = new SGNQValueInitialization.ConstantValueQInit(defaultQ);
		
		this.qMap = new HashMap<StateHashTuple, List<SGQValue>>();
		stateRepresentations = new HashMap<StateHashTuple, State>();
		this.strategy = new SGEQGreedy(this, 0.1);
		
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
	public SGQLAgent(SGDomain d, double discount, double learningRate, SGNQValueInitialization qInitizalizer, StateHashFactory hashFactory) {
		this.init(d);
		this.discount = discount;
		this.learningRate = new ConstantLR(learningRate);
		this.hashFactory = hashFactory;
		this.qInit = qInitizalizer;
		
		this.qMap = new HashMap<StateHashTuple, List<SGQValue>>();
		stateRepresentations = new HashMap<StateHashTuple, State>();
		this.strategy = new SGEQGreedy(this, 0.1);
		
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
	 * Sets the Q-learning strategy that this agent will use (e.g., epsilon greedy)
	 * @param strategy the Q-learning strategy that this agent will use
	 */
	public void setStrategy(Strategy strategy){
		this.strategy = strategy;
	}
	
	public void setQValueInitializer(SGNQValueInitialization qInit){
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
	public GroundedSingleAction getAction(State s) {
		return strategy.getAction(s);
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction, Map<String, Double> jointReward, State sprime, boolean isTerminal) {
		
		if(internalRewardFunction != null){
			jointReward = internalRewardFunction.reward(s, jointAction, sprime);
		}
		
		GroundedSingleAction myAction = jointAction.action(worldAgentName);

		double r = jointReward.get(worldAgentName);
		SGQValue qe = this.getSGQValue(s, myAction);
		double maxQ = 0.;
		if(!isTerminal){
			maxQ = this.getMaxQValue(sprime);
		}
		

		qe.q = qe.q + this.learningRate.pollLearningRate(s, myAction) * (r + (this.discount * maxQ) - qe.q);

	}

	@Override
	public void gameTerminated() {
		//nothing to do

	}
	
	
	/**
	 * Returns all Q-values for the input state
	 * @param s the state for which all Q-values should be returned
	 * @return all Q-values for the input state
	 */
	public List <SGQValue> getAllQsFor(State s){
		
		
		AgentType at = this.getAgentType();
		String aname = this.getAgentName();
		
		List<GroundedSingleAction> gsas = SingleAction.getAllPossibleGroundedSingleActions(s, aname, at.actions);
		
		
		return this.getAllQsFor(s, gsas);
	}
	
	
	/**
	 * Returns all the Q-values for the given state and actions
	 * @param s the state for which Q-values should be returned
	 * @param gsas the actions for which Q-values should be returned
	 * @return all the Q-values for the given state and actions
	 */
	public List <SGQValue> getAllQsFor(State s, List <GroundedSingleAction> gsas){
		
		StateHashTuple shq = this.stateHash(s);
		
		State storedRep = stateRepresentations.get(shq);
		if(storedRep == null){
			//no existing entry so we can create it
			stateRepresentations.put(shq, shq.s);
			List <SGQValue> entries = new ArrayList<SGQValue>();
			for(GroundedSingleAction gsa : gsas){
				SGQValue q = new SGQValue(gsa, this.qInit.qInit(shq.s, gsa));
				entries.add(q);
			}
			qMap.put(shq, entries);
			return entries;
			
		}
		
		//otherwise an entry exists and we need to do the matching
		
		List <SGQValue> entries = qMap.get(shq);
		List <SGQValue> returnedEntries = new ArrayList<SGQValue>(gsas.size());
		Map <String, String> matching = null;
		for(GroundedSingleAction gsa :gsas){
			GroundedSingleAction transgsa = gsa;
			if(gsa.isParameterized()){
				if(matching == null){
					matching = shq.s.getObjectMatchingTo(storedRep, false);
				}
				transgsa = this.translateAction(gsa, matching);
			}
			
			//find matching action in entry list
			boolean foundMatch = false;
			for(SGQValue qe : entries){
				if(qe.gsa.equals(transgsa)){
					returnedEntries.add(qe);
					foundMatch = true;
					break;
				}
			}
			
			if(!foundMatch){
				SGQValue qe = new SGQValue(transgsa, this.qInit.qInit(shq.s, transgsa));
				entries.add(qe);
				returnedEntries.add(qe);
			}
			
			
		}
		
		if(returnedEntries.size() == 0){
			throw new RuntimeException();
		}
		
		return returnedEntries;
		
	}
	
	
	/**
	 * Returns the Q-value for a given state-action pair
	 * @param s the state for which the Q-value should be returned
	 * @param gsa the action for which the Q-value should be returned.
	 * @return the Q-value for the given state-action pair.
	 */
	public SGQValue getSGQValue(State s, GroundedSingleAction gsa){
		StateHashTuple shq = this.stateHash(s);
		
		State storedRep = stateRepresentations.get(shq);
		if(storedRep == null){
			//no existing entry so we can create it
			stateRepresentations.put(shq, shq.s);
			SGQValue q = new SGQValue(gsa, this.qInit.qInit(shq.s, gsa));
			List <SGQValue> entries = new ArrayList<SGQValue>();
			entries.add(q);
			qMap.put(shq, entries);
			return q;
		}
		
		if(gsa.isParameterized()){
			//then we'll need to translate this action to match the internal state representation
			Map <String, String> matching = shq.s.getObjectMatchingTo(storedRep, false);
			gsa = this.translateAction(gsa, matching);
		}
		
		List <SGQValue> entries = qMap.get(shq);
		for(SGQValue qe : entries){
			if(qe.gsa.equals(gsa)){
				return qe;
			}
		}
		
		//if we got here then there are no entries for this action
		SGQValue qe = new SGQValue(gsa, this.qInit.qInit(shq.s, gsa));
		entries.add(qe);
		
		return qe;
	}
	
	
	/**
	 * Returns maximum numeric Q-value for a given state
	 * @param s the state for which the max Q-value should be returned
	 * @return maximum numeric Q-value for a given state
	 */
	protected double getMaxQValue(State s){

		List<GroundedSingleAction> gas = SingleAction.getAllPossibleGroundedSingleActions(s, worldAgentName, agentType.actions);
		List <SGQValue> entries = this.getAllQsFor(s, gas);
		
		
		double maxQ = Double.NEGATIVE_INFINITY;
		for(SGQValue qe : entries){
			if(qe.q > maxQ){
				maxQ = qe.q;
			}
		}
		
		return maxQ;
	}
	
	
	/**
	 * First abstracts state s, and then returns the {@link burlap.behavior.statehashing.StateHashTuple} object for the abstracted state.
	 * @param s the state for which the state hash should be returned.
	 * @return the hashed state.
	 */
	protected StateHashTuple stateHash(State s){
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
	protected GroundedSingleAction translateAction(GroundedSingleAction a, Map <String,String> matching){
		String [] newParams = new String[a.params.length];
		for(int i = 0; i < a.params.length; i++){
			newParams[i] = matching.get(a.params[i]);
		}
		return new GroundedSingleAction(worldAgentName, a.action, newParams);
	}
	
	

}
