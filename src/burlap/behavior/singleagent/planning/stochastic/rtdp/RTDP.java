package burlap.behavior.singleagent.planning.stochastic.rtdp;

import java.util.LinkedList;
import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.ValueFunctionPlanner;
import burlap.behavior.singleagent.planning.commonpolicies.BoltzmannQPolicy;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * Implementation of Real-time dynamic programming [1]. The planning algorithm uses a Q-value derived policy to sample rollouts in the domain. After a rollout
 * is complete, either from reaching a terminal state or taking longer the some set number of steps, the bellman update is performed on each state that was visited
 * in reverse. By default, a Boltzmann policy is used with temperature 0.1. You can change the rollout policy.
 * 
 * 
 * 
 * 1. Barto, Andrew G., Steven J. Bradtke, and Satinder P. Singh. "Learning to act using real-time dynamic programming." Artificial Intelligence 72.1 (1995): 81-138.
 * 
 * 
 * @author James MacGlashan
 *
 */
public class RTDP extends ValueFunctionPlanner {

	
	/**
	 * The policy to use for episode rollouts
	 */
	protected Policy					rollOutPolicy;
	
	/**
	 * the number of rollouts to perform when planning is started unless the value function delta is small enough.
	 */
	protected int						numRollouts;
	
	/**
	 * When the maximum change in the value function from a rollout is smaller than this value, VI will terminate.
	 */
	protected double					maxDelta;
	
	/**
	 * The maximum depth/length of a rollout before it is terminated and Bellman updates are performed.
	 */
	protected int						maxDepth;
	
	
	
	/**
	 * Initializes the planner.
	 * @param domain the domain in which to plan
	 * @param rf the reward function
	 * @param tf the terminal state function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factor to use
	 * @param numRollouts the number of rollouts to perform when planning is started.
	 * @param maxDelta when the maximum change in the value function from a rollout is smaller than this value, VI will terminate.
	 * @param maxDepth the maximum depth/length of a rollout before it is terminated and Bellman updates are performed.
	 */
	public RTDP(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, int numRollouts, double maxDelta, int maxDepth){
		
		this.VFPInit(domain, rf, tf, gamma, hashingFactory);
		
		this.numRollouts = numRollouts;
		this.maxDelta = maxDelta;
		this.maxDepth = maxDepth;
		this.rollOutPolicy = new BoltzmannQPolicy(this, 0.1);
		
	}
	
	
	/**
	 * Sets the number of rollouts to perform when planning is started (unless the value function delta is small enough).
	 * @param p the number of passes
	 */
	public void setNumPasses(int p){
		this.numRollouts = p;
	}
	
	/**
	 * Sets the maximum delta state value update in a rollout that will cause planning to terminate
	 * @param delta the max delta
	 */
	public void setMaxDelta(double delta){
		this.maxDelta = delta;
	}
	
	
	/**
	 * Sets the rollout policy to use.
	 * @param p
	 */
	public void setRollOutPolicy(Policy p){
		this.rollOutPolicy = p;
	}
	
	/**
	 * Sets the maximum depth of a rollout to use until it is prematurely temrinated to update the value function.
	 * @param d the maximum depth of a rollout.
	 */
	public void setMaxDynamicDepth(int d){
		this.maxDepth = d;
	}
	
	@Override
	public void planFromState(State initialState) {
		
		int totalStates = 0;
		
		for(int i = 0; i < numRollouts; i++){
			
			EpisodeAnalysis ea = this.rollOutPolicy.evaluateBehavior(initialState, rf, tf, maxDepth);
			LinkedList <StateHashTuple> orderedStates = new LinkedList<StateHashTuple>();
			for(State s : ea.stateSequence){
				orderedStates.addFirst(this.stateHash(s));
			}
			
			double delta = this.performOrderedBellmanUpdates(orderedStates);
			totalStates += orderedStates.size();
			DPrint.cl(debugCode, "Pass: " + i + "; Num states: " + orderedStates.size() + " (total: " + totalStates + ")");
			
			if(delta < this.maxDelta){
				break;
			}
		}

	}
	
	
	
	/**
	 * Performs ordered Bellman updates on the list of (hashed) states provided to it.
	 * @param states the ordered list of states on which to perform Bellamn updates.
	 * @return the maximum change in the value function for the given states
	 */
	protected double performOrderedBellmanUpdates(List <StateHashTuple> states){
		
		double delta = 0.;
		for(StateHashTuple sh : states){
			
			if(mapToStateIndex.get(sh) == null){
				//not stored yet
				mapToStateIndex.put(sh, sh);
			}
			
			if(tf.isTerminal(sh.s)){
				//no need to compute this state; always zero because it is terminal and agent cannot behave here
				valueFunction.put(sh, 0.);
				continue;
			}
			
			
			double v = this.value(sh);
			
			double maxQ = this.performBellmanUpdateOn(sh);
			delta = Math.max(Math.abs(maxQ - v), delta);
			
		}
		
		return delta;
		
	}

}
