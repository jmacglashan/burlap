package burlap.behavior.singleagent.planning.deterministic.informed.astar;

import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.PrioritizedSearchNode;
import burlap.mdp.statehashing.HashableStateFactory;
import burlap.mdp.statehashing.HashableState;
import burlap.mdp.core.Domain;
import burlap.mdp.singleagent.GroundedAction;
import burlap.mdp.singleagent.RewardFunction;

/**
 * Statically weighted A* [1] implementation. Epsilon is a parameter &gt; 1. The larger the value the more greedy the search. The returned solution
 * is guaranteed to be at most \epsilon times the optimal solution cost.
 * 
 * <p>
 * If a terminal function is provided via the setter method defined for OO-MDPs, then the BestFirst search algorithm will not expand any nodes
 * that are terminal states, as if there were no actions that could be executed from that state. Note that terminal states
 * are not necessarily the same as goal states, since there could be a fail condition from which the agent cannot act, but
 * that is not explicitly represented in the transition dynamics.
 * 
 * 1. Pohl, Ira (1970). "First results on the effect of error in heuristic search". Machine Intelligence 5: 219-236.
 * 
 * @author James MacGlashan
 *
 */
public class StaticWeightedAStar extends AStar {

	/**
	 * The &gt; 1 epsilon parameter. The larger the value the more greedy.
	 */
	protected double			epsilonP1;
	
	
	/**
	 * Initializes the valueFunction. Returned solution will be at most \epsilon times the optimal solution cost.
	 * @param domain the domain in which to plan
	 * @param rf the reward function that represents costs as negative reward
	 * @param gc should evaluate to true for goal states; false otherwise
	 * @param hashingFactory the state hashing factory to use
	 * @param heuristic the planning heuristic. Should return non-positive values.
	 * @param epsilon parameter &gt; 1. The larger the value the more greedy.
	 */
	public StaticWeightedAStar(Domain domain, RewardFunction rf, StateConditionTest gc, HashableStateFactory hashingFactory, Heuristic heuristic, double epsilon) {
		super(domain, rf, gc, hashingFactory, heuristic);
		this.epsilonP1 = 1. + epsilon;
	}
	
	@Override
	public double computeF(PrioritizedSearchNode parentNode, GroundedAction generatingAction, HashableState successorState) {
		double cumR = 0.;
		double r;
		if(parentNode != null){
			double pCumR = cumulatedRewardMap.get(parentNode.s);
			r = rf.reward(parentNode.s.s, generatingAction, successorState.s);
			cumR = pCumR + r;
		}
		
		double H  = heuristic.h(successorState.s);
		lastComputedCumR = cumR;
		double F = cumR + (this.epsilonP1*H);
		
		return F;
	}

}
