package burlap.behavior.singleagent.planning.deterministic;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;



public abstract class DeterministicPlanner extends OOMDPPlanner{

	protected StateConditionTest						gc;
	protected Map <StateHashTuple, GroundedAction>		internalPolicy;
	
	
	
	
	
	
	
	
	
	public void deterministicPlannerInit(Domain domain, RewardFunction rf, TerminalFunction tf, StateConditionTest gc, StateHashFactory hashingFactory){
		
		this.PlannerInit(domain, rf, tf, 1., hashingFactory); //goal condition doubles as termination function for detemrinistic planners 
		this.gc = gc;
		this.internalPolicy = new HashMap<StateHashTuple, GroundedAction>();
	

	}
	

	
	public boolean cachedPlanForState(State s){
		StateHashTuple sh = this.stateHash(s);
		StateHashTuple indexSH = mapToStateIndex.get(sh);
		
		return indexSH != null;
	}
	
	public GroundedAction querySelectedActionForState(State s){
		
		StateHashTuple sh = this.stateHash(s);
		StateHashTuple indexSH = mapToStateIndex.get(sh);
		if(indexSH == null){
			this.planFromState(s);
			return internalPolicy.get(sh); //no need to translate because if the state didn't exist then it got indexed with this state's rep
		}
		
		//otherwise it's already computed
		GroundedAction res = internalPolicy.get(sh);
		
		//do object matching from returned result to this query state and return result
		if(containsParameterizedActions){
			Map<String,String> matching = indexSH.s.getObjectMatchingTo(sh.s, false);
			for(int i = 0; i < res.params.length; i++){
				res.params[i] = matching.get(res.params[i]);
			}
		}
		
				
		return res;
		
		
	}
	
	
	protected void encodePlanIntoPolicy(SearchNode lastVisitedNode){
		
		SearchNode curNode = lastVisitedNode;
		while(curNode.backPointer != null){
			StateHashTuple bpsh = curNode.backPointer.s;
			if(!mapToStateIndex.containsKey(bpsh)){ //makes sure earlier plan duplicate nodes do not replace the correct later visits
				internalPolicy.put(bpsh, curNode.generatingAction);
				mapToStateIndex.put(bpsh, bpsh);
			}
			
			curNode = curNode.backPointer;
		}
	}
	
	
	protected boolean planContainsOption(SearchNode lastVisitedNode){
		SearchNode curNode = lastVisitedNode;
		while(curNode.backPointer != null){
			
			if(!curNode.generatingAction.action.isPrimitive()){
				return true;
			}
			
			curNode = curNode.backPointer;
		}
		return false;
	}
	
	protected boolean planHasDupilicateStates(SearchNode lastVisitedNode){
		
		Set<StateHashTuple> statesInPlan = new HashSet<StateHashTuple>();
		SearchNode curNode = lastVisitedNode;
		while(curNode.backPointer != null){
			if(statesInPlan.contains(curNode.s)){
				return true;
			}
			statesInPlan.add(curNode.s);
			curNode = curNode.backPointer;
		}
		return false;
		
	}
	
	
	
	
	
	
}
