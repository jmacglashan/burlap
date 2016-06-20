package burlap.behavior.singleagent.planning.stochastic.montecarlo.uct;

import burlap.behavior.singleagent.planning.stochastic.montecarlo.uct.UCTActionNode.UCTActionConstructor;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.ActionUtils;
import burlap.statehashing.HashableState;

import java.util.ArrayList;
import java.util.List;

/**
 * UCT State Node that wraps a hashed state object and provided additional state statistics necessary for UCT.
 * 
 * 
 * @author James MacGlashan
 *
 */
public class UCTStateNode {

	/**
	 * The (hashed) state this node wraps
	 */
	public HashableState state;
	
	/**
	 * The depth the UCT tree
	 */
	public int						depth;
	
	/**
	 * The number of times this node has been visited
	 */
	public int						n;
	
	/**
	 * The possible actions (nodes) that can be performed from this state.
	 */
	public List<UCTActionNode>		actionNodes;
	
	
	/**
	 * Initializes the UCT state node.
	 * @param s the state that this node wraps
	 * @param d the depth of the node
	 * @param actionTypes the possible OO-MDP actions that can be taken
	 * @param constructor a {@link UCTActionNode} factory that can be used to create ActionNodes for each of the actions.
	 */
	public UCTStateNode(HashableState s, int d, List <ActionType> actionTypes, UCTActionConstructor constructor){
		
		state = s;
		depth = d;
		
		n = 0;
		
		actionNodes = new ArrayList<UCTActionNode>();

		List<Action> actions = ActionUtils.allApplicableActionsForTypes(actionTypes, s.s());
		for(Action a : actions){
			UCTActionNode an = constructor.generate(a);
			actionNodes.add(an);
		}

	}
	
	
	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((actionNodes == null) ? 0 : actionNodes.hashCode());
        result = prime * result + depth;
        result = prime * result + n;
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        return result;
    }
	
	
	@Override
    public boolean equals(Object o){
        
        if(!(o instanceof UCTStateNode)){
            return false;
        }
        
        UCTStateNode os = (UCTStateNode)o;
        
        return state.equals(os.state) && depth == os.depth;
        
    }
	
	
	
	/**
	 * A factory for generating UCTStateNode objects
	 * @author James MacGlashan
	 *
	 */
	public static class UCTStateConstructor{
		
		/**
		 * Generates an instance of a {@link UCTStateNode}
		 * @param s the state that this node wraps
		 * @param d the depth of the node
		 * @param actionTypes the possible OO-MDP actions that can be taken
		 * @param constructor a {@link UCTActionNode} factory that can be used to create ActionNodes for each of the actions.
		 * @return a {@link UCTStateNode} instance.
		 */
		public UCTStateNode generate(HashableState s, int d, List <ActionType> actionTypes, UCTActionConstructor constructor){
			return new UCTStateNode(s, d, actionTypes, constructor);
		}
		
		
	}
	
}
