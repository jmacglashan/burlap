package burlap.behavior.singleagent.planning.stochastic.valueiteration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import burlap.behavior.singleagent.planning.ActionTransitions;
import burlap.behavior.singleagent.planning.HashedTransitionProbability;
import burlap.behavior.singleagent.planning.ValueFunctionPlanner;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.domain.singleagent.minecraft.Affordance;
import burlap.domain.singleagent.minecraft.MinecraftDomain;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


public class ValueIteration extends ValueFunctionPlanner{

	
	protected double												minDelta;
	protected int													maxPasses;
	protected int[][][]												startMap;
	
	
	
	public ValueIteration(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, double minDelta, int maxPasses){
		
		this.VFPInit(domain, rf, tf, gamma, hashingFactory);
		
		this.minDelta = minDelta;
		this.maxPasses = maxPasses;

	}
	
	
	@Override
	public void planFromState(State initialState){
		this.initializeOptionsForExpectationComputations();
		if(this.performReachabilityFrom(initialState)){
			this.runVI();
		}
			
	}

	@Override
	public int planFromState(State initialState, MinecraftDomain mcDomain){
		boolean oldDetFlag = mcDomain.deterministicMode;
		mcDomain.deterministicMode = true;
		this.initializeOptionsForExpectationComputations();
		if(this.performReachabilityFrom(initialState)){
			mcDomain.deterministicMode = oldDetFlag;
			return this.runVI();
		}
		return -1;
	}
	
	@Override
	public double planFromStateAndTime(State initialState, boolean timeReachability){
		long start = 0;
		this.initializeOptionsForExpectationComputations();
		if (timeReachability) {
			start = System.nanoTime();
		}
		if(this.performReachabilityFrom(initialState)){
			if (!timeReachability) {
				start = System.nanoTime();
			}
			this.runVI();
		}
		return (double)(System.nanoTime() - start) / 1000000000;
			
	}
	
	@Override
	public int planFromStateAffordance(State initialState, ArrayList<Affordance> kb){
		this.initializeOptionsForExpectationComputations();
		if(this.performAffordanceReachabilityFrom(initialState, kb)){
			return this.runVI();
		}
		return -1;
	}
	

//	public double value(State s) {
//		StateHashTuple sh = this.hashingFactory.hashState(s);
//		return valueFunction.get(sh);
//	}
	
	
	public int runVI(){
		
		Set <StateHashTuple> states = mapToStateIndex.keySet();
		
		int i = 0;
		int nStates = 0;
		for(i = 0; i < this.maxPasses; i++){
//			System.out.println(i);
			double delta = 0.;
			nStates = states.size();
			for(StateHashTuple sh : states){

				if(tf.isTerminal(sh.s)){
					//no need to compute this state; always zero because it is terminal and agent cannot behave here
					valueFunction.put(sh, 0.);
					nStates--;
					continue;
					
				}
				
				Double V = valueFunction.get(sh);
				double v = 0.;
				if(V != null){
					v = V;
				}
//				System.out.println(v);
				List<ActionTransitions> transitions = transitionDynamics.get(sh);
				double maxQ = Double.NEGATIVE_INFINITY;
				for(ActionTransitions at : transitions){
					double q = this.computeQ(sh.s, at);
					if(q > maxQ){
						maxQ = q;
					}
				}
				
				//set V to maxQ
				valueFunction.put(sh, maxQ);
				delta = Math.max(Math.abs(maxQ - v), delta);
				
			}
			if(delta < this.minDelta){
				break; //approximated well enough; stop iterating
			}
			
		}

		
		DPrint.cl(10, "Passes: " + i);
		return i * states.size();
	}
	
	
	public boolean performReachabilityFrom(State si){
		
		StateHashTuple sih = this.stateHash(si);
		//first check if this is an new state, otherwise we do not need to do any new reachability analysis
		if(transitionDynamics.containsKey(sih)){
			return false; //no need for additional reachability testing
		}
		
		DPrint.cl(11, "Starting reachability analysis");
		
		//add to the open list
		LinkedList <StateHashTuple> openList = new LinkedList<StateHashTuple>();
		Set <StateHashTuple> openedSet = new HashSet<StateHashTuple>();
		openList.offer(sih);
		openedSet.add(sih);
		
		
		while(openList.size() > 0){
			StateHashTuple sh = openList.poll();
			
			//skip this if it's already been expanded
			if(transitionDynamics.containsKey(sh)){
				continue;
			}
			
			mapToStateIndex.put(sh, sh);
			
			//do not need to expand from terminal states
			if(this.tf.isTerminal(sh.s)){
				System.out.println("reached terminal");
				continue;
			}
			
			//otherwise do expansion
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
			
			
		}
		
		DPrint.cl(11, "Finished reachability analysis; # states: " + mapToStateIndex.size());
		
		
		return true;
		
	}
		
	public boolean performAffordanceReachabilityFrom(State si, ArrayList<Affordance> kb){
		
		StateHashTuple sih = this.stateHash(si);
		//first check if this is an new state, otherwise we do not need to do any new reachability analysis
		if(transitionDynamics.containsKey(sih)){
			return false; //no need for additional reachability testing
		}
		
		DPrint.cl(11, "Starting reachability analysis");
		
		//add to the open list
		LinkedList <StateHashTuple> openList = new LinkedList<StateHashTuple>();
		Set <StateHashTuple> openedSet = new HashSet<StateHashTuple>();
		openList.offer(sih);
		openedSet.add(sih);
		
		
		while(openList.size() > 0){
			StateHashTuple sh = openList.poll();
			
			//skip this if it's already been expanded
			if(transitionDynamics.containsKey(sh)){
				continue;
			}
			
			mapToStateIndex.put(sh, sh);
			
			//do not need to expand from terminal states
			if(this.tf.isTerminal(sh.s)){
				System.out.println("reached terminal");
				continue;
			}
			
			// Select action set with affordance KB
//			List <GroundedAction> gas = new ArrayList<GroundedAction>();
//			for(Action a : actions){
//				gas.addAll(sh.s.getAllGroundedAffordanceActionsFor(a, kb, this.domain));
//			}
			
			List <GroundedAction> gas = this.getAffordanceGroundedActions(sh.s, kb);
			// Fall back on regular VI (Affordance pruned ALL actions, so fall back on using all)
			// In other words, we don't have a relevant affordance to dictate actions here
			if (gas.size() == 0) {
				System.out.println("REACHABILITY: BACKING OFF TO VI ACTIONS");
				for(Action a : actions){
					gas.addAll(sh.s.getAllGroundedActionsFor(a));
				}
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
			
			
		}
		
		DPrint.cl(11, "Finished reachability analysis; # states: " + mapToStateIndex.size());
		
		
		return true;
		
	}
		
	
	
}
