package burlap.behavior.affordances;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.ArrowActionGlyph;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.LandmarkColorBlendInterpolation;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.PolicyGlyphPainter2D;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.PolicyGlyphPainter2D.PolicyGlyphRenderStyle;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.StateValuePainter2D;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.AffordanceGreedyQPolicy;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.stochastic.rtdp.AffordanceRTDP;
import burlap.behavior.singleagent.planning.stochastic.rtdp.RTDP;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.logicalexpressions.LogicalExpression;
import burlap.oomdp.logicalexpressions.PFAtom;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;

public class AffordancesController {

	protected List<AffordanceDelegate> affordances;
	protected LogicalExpression currentGoal;
	
	public AffordancesController(List<AffordanceDelegate> affs) {
		this.affordances = affs;
	}
	
	/**
	 * Update the current goal, and change the state of each affordance accordingly.
	 * @param currentGoal
	 */
	public void setCurrentGoal(LogicalExpression currentGoal){
		this.currentGoal = currentGoal;
		for(AffordanceDelegate aff : this.affordances){
			aff.setCurrentGoal(currentGoal);
		}
	}
	
	/**
	 * Resets all of the action sets for each affordance (resamples from each Dirichlet)
	 */
	public void resampleActionSets(){
		for(AffordanceDelegate aff : this.affordances){
			aff.resampleActionSet();
		}
	}
	
	/**
	 * Retrieves the list of relevant actions for a particular state, as pruned by affordances.
	 * @param actions: The set of actions to consider
	 * @param s: The current world state
	 * @return: A list of AbstractGroundedActions, the pruned action set.
	 */
	public List<AbstractGroundedAction> filterIrrelevantActionsInState(List<AbstractGroundedAction> actions, State s){
		
		List<AffordanceDelegate> activeAffordances = new ArrayList<AffordanceDelegate>(this.affordances.size());
		for(AffordanceDelegate aff : this.affordances){
			if(aff.primeAndCheckIfActiveInState(s)){
				activeAffordances.add(aff);
			}
		}
		
		List<AbstractGroundedAction> filteredList = new ArrayList<AbstractGroundedAction>(actions.size());
		for(AbstractGroundedAction a : actions){
			for(AffordanceDelegate aff : activeAffordances){
				if(aff.actionIsRelevant(a)){
					filteredList.add(a);
					break;
				}
			}
		}
		
		return filteredList;
	}
	
	
}
