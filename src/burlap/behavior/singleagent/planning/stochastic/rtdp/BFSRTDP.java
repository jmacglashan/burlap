package burlap.behavior.singleagent.planning.stochastic.rtdp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.ActionTransitions;
import burlap.behavior.singleagent.planning.HashedTransitionProbability;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


public class BFSRTDP extends RTDP {
	
	protected Policy												rollOutPolicy;
	

	protected int													dynamicPasses;
	protected boolean												performedInitialPlan;
	protected StateConditionTest									goalCondition;
	
	
	
	public BFSRTDP(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, int numPasses, int maxDepth){
		
		super(domain, rf, tf, gamma, hashingFactory, numPasses, maxDepth);

		this.dynamicPasses = 1;
		this.performedInitialPlan = false;
		this.goalCondition = null;

	}
	
	
	public BFSRTDP(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, int numPasses, int maxDepth, StateConditionTest goalCondition){
		
		super(domain, rf, tf, gamma, hashingFactory, numPasses, maxDepth);

		this.dynamicPasses = 1;
		this.performedInitialPlan = false;
		this.goalCondition = goalCondition;

	}
	

	
	public void setGoalCondition(StateConditionTest gc){
		this.goalCondition = gc;
	}
	
	
	@Override
	public void planFromState(State initialState) {
		StateHashTuple sh = this.stateHash(initialState);
		if(!mapToStateIndex.containsKey(sh)){
			this.performInitialPassFromState(initialState);
		}
		else{
			this.performRolloutPassFromState(initialState);
		}

	}
	
	
	protected void performInitialPassFromState(State initialState){
		
		List <StateHashTuple> orderedStates = this.performRecahabilityAnalysisFrom(initialState);
		for(int i = 0; i < numPasses; i++){
			this.performOrderedVIPass(orderedStates);
		}
		
		performedInitialPlan = true;
		
	}
	
	protected void performRolloutPassFromState(State initialState){
		
		for(int i = 0; i < dynamicPasses; i++){
			
			EpisodeAnalysis ea = this.rollOutPolicy.evaluateBehavior(initialState, rf, tf, maxDepth);
			LinkedList <StateHashTuple> orderedStates = new LinkedList<StateHashTuple>();
			for(State s : ea.stateSequence){
				orderedStates.addFirst(this.stateHash(s));
			}
			
			this.performOrderedVIPass(orderedStates);
		}
		
		
	}
	
	
	
	
	//this will return a reverse ordered closed list
	protected List <StateHashTuple> performRecahabilityAnalysisFrom(State si){
		
		DPrint.cl(debugCode, "Starting reachability analysis");
		
		StateHashTuple sih = this.stateHash(si);
		//first check if this is an new state, otherwise we do not need to do any new reachability analysis
		if(transitionDynamics.containsKey(sih)){
			return new ArrayList<StateHashTuple>(); //no need for additional reachability testing so return empty closed list
		}
		
		//add to the open list
		LinkedList <StateHashTuple> closedList = new LinkedList<StateHashTuple>();
		LinkedList <StateHashTuple> openList = new LinkedList<StateHashTuple>();
		Set <StateHashTuple> openedSet = new HashSet<StateHashTuple>();
		openList.offer(sih);
		openedSet.add(sih);
		
		List <Action> actions = domain.getActions();
		
		
		while(openList.size() > 0){
			StateHashTuple sh = openList.poll();
			
			//skip this if it's already been expanded
			if(transitionDynamics.containsKey(sh)){
				continue;
			}
			
			//otherwise do expansion
			//if we reached a goal state or a previously explored state, then then bfs completes and we do not need to add terminal's children
			if(this.satisfiesGoal(sh.s) || mapToStateIndex.containsKey(sh)){
				break;
			}
			
			if(this.tf.isTerminal(sh.s)){
				continue; //cannot act from here
			}
			
			
			//first get all grounded actions for this state
			List <GroundedAction> gas = new ArrayList<GroundedAction>();
			for(Action a : actions){
				gas.addAll(sh.s.getAllGroundedActionsFor(a));
			}
			
			//then get the transition dynamics for each action and queue up new states
			List <ActionTransitions> transitions = new ArrayList<ActionTransitions>();
			for(GroundedAction ga : gas){
				ActionTransitions at = new ActionTransitions(sh.s, ga, hashingFactory);
				transitions.add(at);
				for(HashedTransitionProbability tp : at.transitions){
					StateHashTuple tsh = tp.sh;
					if(!openedSet.contains(tsh) && !transitionDynamics.containsKey(tsh)){
						openedSet.add(tsh);
						openList.offer(tsh);
					}
				}
			}
			
			//now make entry for this in the transition dynamics
			transitionDynamics.put(sh, transitions);

			//close it
			closedList.addFirst(sh);
			
		}
		
		
		for(StateHashTuple sh : closedList){
			//add as visited states
			mapToStateIndex.put(sh, sh);
		}
		
		DPrint.cl(debugCode, "Finished reachability analysis; # states: " + transitionDynamics.size());
		
		
		return closedList;
	}
	
	protected boolean satisfiesGoal(State s){
		if(goalCondition == null){
			return false;
		}
		return goalCondition.satisfies(s);
	}
	

}
