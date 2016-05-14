package burlap.behavior.singleagent.planning.deterministic.informed;

import burlap.behavior.singleagent.planning.deterministic.SearchNode;
import burlap.mdp.core.Action;
import burlap.mdp.statehashing.HashableState;

import java.util.Comparator;


/**
 * An extension of the {@link burlap.behavior.singleagent.planning.deterministic.SearchNode} class that includes
 * a priority value. Priority is used to represent the "f" function, which in A* is defined to be the sum of the
 * cost to the node and the admissible heuristic from it. The priority is use to order nodes for expansion.
 * @author James MacGlashan
 *
 */
public class PrioritizedSearchNode extends SearchNode {

	/**
	 * The priority of the node used to order it for expansion.
	 */
	public double priority;
	
	
	/**
	 * Initializes a PrioritizedSearchNode for a given (hashed) input state and priority value. 
	 * The generating action and back pointer will be set to null which is valid for initial states.
	 * @param s the hashed input state this node represents.
	 * @param p the priority of this node.
	 */
	public PrioritizedSearchNode(HashableState s, double p){
		super(s);
		priority = p;
	}
	
	
	/**
	 * Constructs a SearchNode for the input state and priority and sets the generating action and back pointer to the provided elements.
	 * @param s the hashed input state this node will represent.
	 * @param ga the action that was used to generate s
	 * @param bp the search node that contains the previous state from which s was generated.
	 * @param p the priority of the node.
	 */
	public PrioritizedSearchNode(HashableState s, Action ga, SearchNode bp, double p){
		super(s,ga,bp);
		priority = p;
	}
	
	/**
	 * This method rewires the generating node information and priority to that specified in a different PrioritizedSearchNode. This
	 * method is useful when a better path to this node has been found. 
	 * @param o the other PrioritizedSearchNode whose generating information and priority should be used.
	 */
	public void setAuxInfoTo(PrioritizedSearchNode o){
		this.priority = o.priority;
		this.generatingAction = o.generatingAction;
		this.backPointer = o.backPointer;
	}
	
	@Override
	public boolean equals(Object o){
	    if (o == null || this.getClass() != o.getClass()) {
            return false;   
        }
		PrioritizedSearchNode po = (PrioritizedSearchNode)o;
		return s.equals(po.s);
	}
	
	
	@Override
	public int hashCode(){
		return s.hashCode();
	}
	
	
	
	/**
	 * A class for comparing the priority of two PrioritizedSearchNodes. Nodes
	 * are considered "less than" nodes with higher priority.
	 * @author James MacGlashan
	 *
	 */
	public static class PSNComparator implements Comparator <PrioritizedSearchNode>{

		@Override
		public int compare(PrioritizedSearchNode a, PrioritizedSearchNode b) {
			if(a.priority < b.priority){
				return -1;
			}
			if(a.priority > b.priority){
				return 1;
			}
			return 0;
		}
		
		
		
	}
	
}
