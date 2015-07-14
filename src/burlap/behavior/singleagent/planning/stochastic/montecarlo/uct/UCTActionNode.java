package burlap.behavior.singleagent.planning.stochastic.montecarlo.uct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * UCT Action node that stores relevant action statics necessary for UCT.
 * @author James MacGlashan
 *
 */
public class UCTActionNode {

	/**
	 * The action this action node wraps
	 */
	public GroundedAction								action;
	
	/**
	 * The sum return observed for this action node
	 */
	public double										sumReturn;
	
	/**
	 * The number of of times this action node has been taken
	 */
	public int											n;
	
	/**
	 * The possible successor states. Stores a list of nodes for the same outcome state
	 * since options may reach the same outcome state after a different number steps causing a further depth in the tree.
	 */
	public Map<StateHashTuple, List<UCTStateNode>>		successorStates;
	
	
	/**
	 * Generates a new action node for a given action. All statistics are initialized to 0.
	 * @param a the action this node wraps.
	 */
	public UCTActionNode(GroundedAction a){
		action = a;
		sumReturn = 0.;
		n = 0;
		successorStates = new HashMap<StateHashTuple, List<UCTStateNode>>();
	}
	
	/**
	 * Returns the average return
	 * @return the average return
	 */
	public double averageReturn(){
		if(this.n == 0){
			return Double.NEGATIVE_INFINITY;
		}
		return sumReturn / n;
	}
	
	
	/**
	 * Updates the node statistics with a sample return
	 * @param sampledReturn the sample return observed
	 */
	public void update(double sampledReturn){
		sumReturn += sampledReturn;
		n++;
	}
	
	/**
	 * Adds a successor node to the list of possible successors
	 * @param node
	 */
	public void addSuccessor(UCTStateNode node){
		
		List <UCTStateNode> succesorsMatchingState = successorStates.get(node.state);
		if(succesorsMatchingState == null){
			succesorsMatchingState = new ArrayList<UCTStateNode>();
			successorStates.put(node.state, succesorsMatchingState);
		}
		
		if(!succesorsMatchingState.contains(node)){
			succesorsMatchingState.add(node);
		}
		
	}
	
	/**
	 * Returns whether this action node has a observed a given successor state node in the past
	 * @param node the node which is checked to be in the current successor states
	 * @return true if this node contains in its observed successors the input state node
	 */
	public boolean referencesSuccessor(UCTStateNode node){
		
		List <UCTStateNode> succesorsMatchingState = successorStates.get(node.state);
		if(succesorsMatchingState == null){
			return false;
		}
		
		else if(succesorsMatchingState.contains(node)){
			return true;
		}
		
		return false;
		
	}
	
	
	/**
	 * Returns a list of all successor nodes observed
	 * @return a list of all successor nodes observed
	 */
	public List <UCTStateNode> getAllSuccessors(){
		List <UCTStateNode> res = new ArrayList<UCTStateNode>();
		for(List <UCTStateNode> nodes : successorStates.values()){
			for(UCTStateNode node : nodes){
				res.add(node);
			}
		}
		
		return res;
	}
	
	
	

	/**
	 * A factory for generating UCTActionNode objects.
	 * @author James MacGlashan
	 *
	 */
	public static class UCTActionConstructor{
		
		
		/**
		 * Returns a UCTActionNode Object that wraps the given action
		 * @param a the action to wrap
		 * @return a UCTActionNode Object that wraps the given action
		 */
		public UCTActionNode generate(GroundedAction a){
			return new UCTActionNode(a);
		}
		
	}
	
}
