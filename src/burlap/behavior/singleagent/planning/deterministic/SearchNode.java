package burlap.behavior.singleagent.planning.deterministic;

import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.singleagent.GroundedAction;

public class SearchNode {

	public StateHashTuple 		s;
	public GroundedAction		generatingAction;
	public SearchNode			backPointer;
	
	
	public SearchNode(StateHashTuple s){
		this.s = s;
		this.generatingAction = null;
		this.backPointer = null;
	}
	
	public SearchNode(StateHashTuple s, GroundedAction ga){
		this.s = s;
		this.generatingAction = ga;
		this.backPointer = null;
	}
	
	
	public SearchNode(StateHashTuple s, GroundedAction ga, SearchNode bp){
		this.s = s;
		this.generatingAction = ga;
		this.backPointer = bp;
	}
	
	
	@Override
	public boolean equals(Object o){
		SearchNode so = (SearchNode)o;
		return s.equals(so.s);
	}
	
	
	@Override
	public int hashCode(){
		return s.hashCode();
	}
	
}
