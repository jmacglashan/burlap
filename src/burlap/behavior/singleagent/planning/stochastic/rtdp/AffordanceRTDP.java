/**
 * 
 */
package burlap.behavior.singleagent.planning.stochastic.rtdp;

import burlap.behavior.affordances.AffordancesController;
import burlap.behavior.singleagent.ValueFunctionInitialization;
import burlap.behavior.singleagent.planning.commonpolicies.AffordanceGreedyQPolicy;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * Implementation of Affordance-Aware Real-time dynamic programming [1]. The planning algorithm uses a Q-value derived policy to sample rollouts in the domain. During
 * each step of the rollout, the current state has its value updated using the Bellman operator and the action for the current state
 * is selected using a greedy Q policy in which ties are randomly broken. Affordances are used to prune away irrelevant actions from the considered action set in each state.
 * <p/>
 * To ensure optimality, an optimistic value function initialization should be used. However, RTDP excels when a good value function initialization
 * (e.g., an admissible heuristic) can be provided.
 * 
 * 
 * 
 * 1. Barto, Andrew G., Steven J. Bradtke, and Satinder P. Singh. "Learning to act using real-time dynamic programming." Artificial Intelligence 72.1 (1995): 81-138.
 * 
 * 
 * @author James MacGlashan, David Abel
 *
 */
public class AffordanceRTDP extends RTDP {

	private AffordancesController affController;

	public AffordanceRTDP(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, double vInit, int numRollouts, double maxDelta, int maxDepth, AffordancesController affController){
		super(domain, rf, tf, gamma,hashingFactory, vInit, numRollouts, maxDelta, maxDepth);
		this.VFPInit(domain, rf, tf, gamma, hashingFactory);
		this.affController = affController;
		this.numRollouts = numRollouts;
		this.maxDelta = maxDelta;
		this.maxDepth = maxDepth;
		this.rollOutPolicy = new AffordanceGreedyQPolicy(affController, this);
		
		this.valueInitializer = new ValueFunctionInitialization.ConstantValueFunctionInitialization(vInit);
		
	}
	
	public void planFromState(State initialState) {
		
		this.affordanceRTDP(initialState);

	}

	/**
	 * Runs Affordance Aware RTDP
	 * @param initialState
	 */
	private void affordanceRTDP(State initialState) {

		int totalStates = 0;
		int consecutiveSmallDeltas = 0;

		for(int i = 0; i < numRollouts; i++){
			
			State curState = initialState;

			int nSteps = 0;
			double delta = 0;

			while(!this.tf.isTerminal(curState) && nSteps < this.maxDepth){

				this.affController.resampleActionSets();
				StateHashTuple sh = this.hashingFactory.hashState(curState);
				
				//select an action
				GroundedAction ga = (GroundedAction)this.rollOutPolicy.getAction(curState);

				//update this state's value
				double curV = this.value(sh);

				double nV = this.performBellmanUpdateOn(sh);
				
				delta = Math.max(Math.abs(nV - curV), delta); 

				//take the action
				curState = ga.executeIn(curState);
				nSteps++;
			}

			
			totalStates += nSteps;
			
			DPrint.cl(debugCode, "Pass: " + i + "; Num states: " + nSteps + " (total: " + totalStates + ")");
			
			if(delta < this.maxDelta){
				consecutiveSmallDeltas++;
				if(consecutiveSmallDeltas >= this.minNumRolloutsWithSmallValueChange){
					break;
				}
			}
			else{
				consecutiveSmallDeltas = 0;
			}
			
			
		}
		
	}
	
}
