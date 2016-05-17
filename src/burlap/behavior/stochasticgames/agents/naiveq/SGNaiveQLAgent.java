package burlap.behavior.stochasticgames.agents.naiveq;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.auxiliary.common.ShallowIdentityStateMapping;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import burlap.mdp.stochasticgames.action.JointAction;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.action.SGActionUtils;
import burlap.mdp.stochasticgames.action.SGAgentAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Tabular Q-learning [1] algorithm for stochastic games formalisms. This algorithm ignores the actions of other agents and treats the outcomes
 * from their decisions as if they're part of the environment transition dynamics, hence the "naive" qualifier.
 * 
 * <p>
 * 1. Watkins, Christopher JCH, and Peter Dayan. "Q-learning." Machine learning 8.3-4 (1992): 279-292. <p>
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
	protected StateMapping storedMapAbstraction;
	
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
		
		this.storedMapAbstraction = new ShallowIdentityStateMapping();
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
		
		this.storedMapAbstraction = new ShallowIdentityStateMapping();
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
		
		this.storedMapAbstraction = new ShallowIdentityStateMapping();
	}
	
	/**
	 * Sets the state abstraction that this agent will use
	 * @param abstraction the state abstraction that this agent will use
	 */
	public void setStoredMapAbstraction(StateMapping abstraction){
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
	public SGAgentAction getAction(State s) {
		return (SGAgentAction)this.policy.getAction(s);
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction, Map<String, Double> jointReward, State sprime, boolean isTerminal) {
		
		if(internalRewardFunction != null){
			jointReward = internalRewardFunction.reward(s, jointAction, sprime);
		}
		
		SGAgentAction myAction = jointAction.action(worldAgentName);

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
	 * First abstracts state s, and then returns the {@link burlap.statehashing.HashableState} object for the abstracted state.
	 * @param s the state for which the state hash should be returned.
	 * @return the hashed state.
	 */
	protected HashableState stateHash(State s){
		State abstracted = this.storedMapAbstraction.mapState(s);
		return hashFactory.hashState(abstracted);
	}



	@Override
	public List<QValue> getQs(State s) {

		List<SGAgentAction> gsas = SGActionUtils.allApplicableActionsForTypes(agentType.actions, worldAgentName, s);
		
		HashableState shq = this.stateHash(s);
		
		State storedRep = stateRepresentations.get(shq);
		if(storedRep == null){
			//no existing entry so we can create it
			stateRepresentations.put(shq, shq.s);
			List <QValue> entries = new ArrayList<QValue>();
			for(SGAgentAction gsa : gsas){
				QValue q = new QValue(shq.s, gsa, this.qInit.qValue(shq.s, gsa));
				entries.add(q);
			}
			qMap.put(shq, entries);
			return entries;
			
		}
		
		//otherwise an entry exists and we need to do the matching
		
		List <QValue> entries = qMap.get(shq);
		List <QValue> returnedEntries = new ArrayList<QValue>(gsas.size());
		for(SGAgentAction gsa :gsas){
		;
			//find matching action in entry list
			boolean foundMatch = false;
			for(QValue qe : entries){
				if(qe.a.equals(gsa)){
					returnedEntries.add(qe);
					foundMatch = true;
					break;
				}
			}
			
			if(!foundMatch){
				QValue qe = new QValue(shq.s, gsa, this.qInit.qValue(shq.s, gsa));
				entries.add(qe);
				returnedEntries.add(qe);
			}
			
			
		}
		
		if(returnedEntries.isEmpty()){
			throw new RuntimeException();
		}
		
		return returnedEntries;
		
	}


	@Override
	public double value(State s) {
		return QFunction.QFunctionHelper.getOptimalValue(this, s);
	}


	@Override
	public QValue getQ(State s, Action a) {
		
		SGAgentAction gsa = (SGAgentAction)a;
		
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
