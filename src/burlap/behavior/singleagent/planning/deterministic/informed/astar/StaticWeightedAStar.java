package burlap.behavior.singleagent.planning.deterministic.informed.astar;

import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.PrioritizedSearchNode;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


public class StaticWeightedAStar extends AStar {

	protected double			epsilonP1;
	
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
