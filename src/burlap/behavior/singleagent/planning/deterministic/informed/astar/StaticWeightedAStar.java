package burlap.behavior.singleagent.planning.deterministic.informed.astar;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.PrioritizedSearchNode;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Domain;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * Statically weighted A* [1] implementation. Epsilon is a parameter > 1. The larger the value the more greedy the search. The returned solution
 * is guaranteed to be at most \epsilon times the optimal solution cost.
 * 
 * 
 * 1. Pohl, Ira (1970). "First results on the effect of error in heuristic search". Machine Intelligence 5: 219Ð236.
 * 
 * @author James MacGlashan
 *
 */
public class StaticWeightedAStar extends AStar {

	/**
	 * The > 1 epsilon parameter. The larger the value the more greedy. 
	 */
	protected double			epsilonP1;
	
	
	/**
	 * Initializes the planner. Returned solution will be at most \epsilon times the optimal solution cost.
	 * @param domain the domain in which to plan
	 * @param rf the reward function that represents costs as negative reward
	 * @param gc should evaluate to true for goal states; false otherwise
	 * @param hashingFactory the state hashing factory to use
	 * @param heuristic the planning heuristic. Should return non-positive values.
	 * @param epsilon parameter > 1. The larger the value the more greedy. 
	 */
	public StaticWeightedAStar(Domain domain, RewardFunction rf, StateConditionTest gc, StateHashFactory hashingFactory, Heuristic heuristic, double epsilon) {
		super(domain, rf, gc, hashingFactory, heuristic);
		this.epsilonP1 = 1. + epsilon;
	}
	
	@Override
	public double computeF(PrioritizedSearchNode parentNode, GroundedAction generatingAction, StateHashTuple successorState) {
		double cumR = 0.;
		double r = 0.;
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
