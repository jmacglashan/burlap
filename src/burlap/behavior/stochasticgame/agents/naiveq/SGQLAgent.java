package burlap.behavior.stochasticgame.agents.naiveq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.behavior.stochasticgame.Strategy;
import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.auxiliary.common.NullAbstractionNoCopy;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.AgentType;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SingleAction;


public class SGQLAgent extends Agent {

	protected Map <StateHashTuple, List<SGQValue>>						qMap;
	protected Map <StateHashTuple, State>								stateRepresentations;
	protected StateAbstraction											storedMapAbstraction;
	protected double													discount;
	protected double													learningRate;
	protected double													defaultQ;
	protected Strategy													strategy;
	protected StateHashFactory											hashFactory;
	
	
	public SGQLAgent(SGDomain d, double discount, double learningRate, StateHashFactory hashFactory) {
		this.init(d);
		this.discount = discount;
		this.learningRate = learningRate;
		this.hashFactory = hashFactory;
		this.defaultQ = 0.;
		
		this.qMap = new HashMap<StateHashTuple, List<SGQValue>>();
		stateRepresentations = new HashMap<StateHashTuple, State>();
		this.strategy = new SGEQGreedy(this, 0.1);
		
		this.storedMapAbstraction = new NullAbstractionNoCopy();
	}
	
	public SGQLAgent(SGDomain d, double discount, double learningRate, double defaultQ, StateHashFactory hashFactory) {
		this.init(d);
		this.discount = discount;
		this.learningRate = learningRate;
		this.hashFactory = hashFactory;
		this.defaultQ = defaultQ;
		
		this.qMap = new HashMap<StateHashTuple, List<SGQValue>>();
		stateRepresentations = new HashMap<StateHashTuple, State>();
		this.strategy = new SGEQGreedy(this, 0.1);
		
		this.storedMapAbstraction = new NullAbstractionNoCopy();
	}
	
	public void setStoredMapAbstraction(StateAbstraction abstraction){
		this.storedMapAbstraction = abstraction;
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
		

		qe.q = qe.q + this.learningRate * (r + (this.discount * maxQ) - qe.q);

	}

	@Override
	public void gameTerminated() {
		//nothing to do

	}
	
	
	public List <SGQValue> getAllQsFor(State s){
		
		
		AgentType at = this.getAgentType();
		String aname = this.getAgentName();
		
		List<GroundedSingleAction> gsas = SingleAction.getAllPossibleGroundedSingleActions(s, aname, at.actions);
		
		
		return this.getAllQsFor(s, gsas);
	}
	
	
	public List <SGQValue> getAllQsFor(State s, List <GroundedSingleAction> gsas){
		
		StateHashTuple shq = this.stateHash(s);
		
		State storedRep = stateRepresentations.get(shq);
		if(storedRep == null){
			//no existing entry so we can create it
			stateRepresentations.put(shq, shq.s);
			List <SGQValue> entries = new ArrayList<SGQValue>();
			for(GroundedSingleAction gsa : gsas){
				SGQValue q = new SGQValue(gsa, defaultQ);
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
			if(gsa.isPamaeterized()){
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
				SGQValue qe = new SGQValue(transgsa, defaultQ);
				entries.add(qe);
				returnedEntries.add(qe);
			}
			
			
		}
		
		if(returnedEntries.size() == 0){
			throw new RuntimeException();
		}
		
		return returnedEntries;
		
	}
	
	public SGQValue getSGQValue(State s, GroundedSingleAction gsa){
		StateHashTuple shq = this.stateHash(s);
		
		State storedRep = stateRepresentations.get(shq);
		if(storedRep == null){
			//no existing entry so we can create it
			stateRepresentations.put(shq, shq.s);
			SGQValue q = new SGQValue(gsa, defaultQ);
			List <SGQValue> entries = new ArrayList<SGQValue>();
			entries.add(q);
			qMap.put(shq, entries);
			return q;
		}
		
		if(gsa.isPamaeterized()){
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
		SGQValue qe = new SGQValue(gsa, defaultQ);
		entries.add(qe);
		
		return qe;
	}
	
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
	
	
	protected StateHashTuple stateHash(State s){
		State abstracted = this.storedMapAbstraction.abstraction(s);
		return hashFactory.hashState(abstracted);
	}
	
	protected GroundedSingleAction translateAction(GroundedSingleAction a, Map <String,String> matching){
		String [] newParams = new String[a.params.length];
		for(int i = 0; i < a.params.length; i++){
			newParams[i] = matching.get(a.params[i]);
		}
		return new GroundedSingleAction(worldAgentName, a.action, newParams);
	}
	
	

}
