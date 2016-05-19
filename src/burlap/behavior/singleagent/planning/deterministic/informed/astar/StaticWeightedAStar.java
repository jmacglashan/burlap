package burlap.behavior.singleagent.planning.deterministic.informed.astar;

import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.PrioritizedSearchNode;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.Action;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

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
	 * Initializes. Returned solution will be at most \epsilon times the optimal solution cost.
	 * @param domain the domain in which to plan
	 * @param gc should evaluate to true for goal states; false otherwise
	 * @param hashingFactory the state hashing factory to use
	 * @param heuristic the planning heuristic. Should return non-positive values.
	 * @param epsilon parameter &gt; 1. The larger the value the more greedy.
	 */
	public StaticWeightedAStar(SADomain domain, StateConditionTest gc, HashableStateFactory hashingFactory, Heuristic heuristic, double epsilon) {
		super(domain, gc, hashingFactory, heuristic);
		this.epsilonP1 = 1. + epsilon;
	}
	
	@Override
	public double computeF(PrioritizedSearchNode parentNode, Action generatingAction, HashableState successorState, double r) {
		double cumR = 0.;
		if(parentNode != null){
			double pCumR = cumulatedRewardMap.get(parentNode.s);
			cumR = pCumR + r;
		}
		
		double H  = heuristic.h(successorState.s());
		lastComputedCumR = cumR;
		double F = cumR + (this.epsilonP1*H);
		
		return F;
	}

}
