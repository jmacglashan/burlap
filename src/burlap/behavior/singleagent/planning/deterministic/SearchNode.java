package burlap.behavior.singleagent.planning.deterministic;

import burlap.mdp.statehashing.HashableState;
import burlap.mdp.singleagent.GroundedAction;


/**
 * The SearchNode class is used for classic deterministic forward search planners. It represents a current state, a back pointer
 * to the search node from which this node's state was generated, and the action that was taken in the generating node's state to
 * produce this node's state. Once a goal state is found by the forward search valueFunction, the back pointers can be traced to
 * find the plan that got to the goal.
 * @author James MacGlashan
 *
 */
public class SearchNode {

	/**
	 * The (hashed) state of this node
	 */
	public HashableState s;
	
	
	/**
	 * The action that generated this state in the previous state. Null if this node is for the initial state.
	 */
	public GroundedAction		generatingAction;
	
	/**
	 * The search node for the previous state that generated this node's state. Null if this node is for the initial state.
	 */
	public SearchNode			backPointer;
	
	
	
	/**
	 * Constructs a SearchNode for the input state. The generating action and back pointer are set to null, which is valid if this
	 * is the search node for an initial state. Otherwise, these fields should be filled in.
	 * @param s the hashed input state this node will represent.
	 */
	public SearchNode(HashableState s){
		this.s = s;
		this.generatingAction = null;
		this.backPointer = null;
	}
	
	
	/**
	 * Constructs a SearchNode for the input state and sets the generating action and back pointer to the provided elements.
	 * @param s the hashed input state this node will represent.
	 * @param ga the action that was used to generate s
	 * @param bp the search node that contains the previous state from which s was generated.
	 */
	public SearchNode(HashableState s, GroundedAction ga, SearchNode bp){
		this.s = s;
		this.generatingAction = ga;
		this.backPointer = bp;
	}
	
	
	@Override
	public boolean equals(Object o){
	    if (o == null || this.getClass() != o.getClass()) {
	        return false;   
	    }
		SearchNode so = (SearchNode)o;
		return s.equals(so.s);
	}
	
	
	@Override
	public int hashCode(){
		return s.hashCode();
	}
	
}
