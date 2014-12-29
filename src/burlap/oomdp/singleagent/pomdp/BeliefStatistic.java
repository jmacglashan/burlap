package burlap.oomdp.singleagent.pomdp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.oomdp.core.State;

public abstract class BeliefStatistic extends State{
	
//	protected StateEnumerator stateEnumerator;
	protected PODomain domain;
	
	public BeliefStatistic(PODomain domain){
		this.domain = domain;
//		this.stateEnumerator = domain.getStateEnumerator();
	}
	
//	public int numStates(){
//		return this.stateEnumerator.numStatesEnumerated();
//	}
	
//	public State stateForId(int id){
//		return this.stateEnumerator.getStateForEnumertionId(id);
//	}
	
//	public List<State> getStateSpace(){
//		LinkedList<State> states = new LinkedList<State>();
//		for(int i = 0; i < this.numStates(); i++){
//			states.add(this.stateForId(i));
//		}
//		return states;
//	}
	
	public PODomain getDomain(){
		return this.domain;
	}
	
	public abstract List<State> getStatesWithNonZeroProbability();
	
	public abstract double belief(State s);
	
	public abstract State sampleStateFromBelief();
	
	public abstract void clearBeliefCollection();
	
	

}
