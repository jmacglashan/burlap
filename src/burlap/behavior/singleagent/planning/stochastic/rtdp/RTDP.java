package burlap.behavior.singleagent.planning.stochastic.rtdp;

import java.util.LinkedList;
import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.policy.Policy;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.behavior.singleagent.planning.ValueFunctionPlanner;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * Implementation of Real-time dynamic programming [1]. The planning algorithm uses a Q-value derived policy to sample rollouts in the domain. During
 * each step of the rollout, the current state has its value updated using the Bellman operator and the action for the current state
 * is selected using a greedy Q policy in which ties are randomly broken. Alternatively, this algorithm may be set to batch mode. In batch mode,
 * all Bellman updates are stalled until rollout
 * is complete, after which the Bellman update is performed on each state that was visited
 * in reverse.
 * <p/>
 * To ensure optimality, an optimistic value function initialization should be used. However, RTDP excels when a good value function initialization
 * (e.g., an admissible heuristic) can be provided.
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
	 * RTDP will be delcared "converged" if there are this many consecutive policy rollouts in which the value function change is smaller than the maxDelta value.
	 * The default value is 10.
	 */
	protected int						minNumRolloutsWithSmallValueChange = 10;
	
	
	/**
	 * If set to use batch mode; Bellman updates will be stalled until a rollout is complete and then run in reverse.
	 */
	protected boolean					useBatch = false;
	
	
	/**
	 * Stores the number of Bellman updates made across all planning.
	 */
	protected int						numberOfBellmanUpdates = 0;
	
	
	
	/**
	 * Initializes the planner. The value function will be initialized to vInit by default everywhere and will use a greedy policy with random tie breaks
	 * for performing rollouts. Use the {@link #setValueFunctionInitialization(ValueFunctionInitialization)} method
	 * to change the value function initialization and the {@link #setRollOutPolicy(Policy)} method to change the rollout policy to something else. vInit
	 * should be set to something optimistic like VMax to ensure convergence.
	 * @param domain the domain in which to plan
	 * @param rf the reward function
	 * @param tf the terminal state function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factor to use
	 * @param vInit the value to the the value function for all states will be initialized
	 * @param numRollouts the number of rollouts to perform when planning is started.
	 * @param maxDelta when the maximum change in the value function from a rollout is smaller than this value, planning will terminate.
	 * @param maxDepth the maximum depth/length of a rollout before it is terminated and Bellman updates are performed.
	 */
	public RTDP(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, double vInit, int numRollouts, double maxDelta, int maxDepth){
		
		this.VFPInit(domain, rf, tf, gamma, hashingFactory);
		
		this.numRollouts = numRollouts;
		this.maxDelta = maxDelta;
		this.maxDepth = maxDepth;
		this.rollOutPolicy = new GreedyQPolicy(this);
		
		this.valueInitializer = new ValueFunctionInitialization.ConstantValueFunctionInitialization(vInit);
		
	}
	
	
	
	
	/**
	 * Initializes the planner. The value function will be initialized to vInit by default everywhere and will use a greedy policy with random tie breaks
	 * for performing rollouts. Use the {@link #setValueFunctionInitialization(ValueFunctionInitialization)} method
	 * to change the value function initialization and the {@link #setRollOutPolicy(Policy)} method to change the rollout policy to something else. vInit
	 * should be set to something optimistic like VMax to ensure convergence.
	 * @param domain the domain in which to plan
	 * @param rf the reward function
	 * @param tf the terminal state function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factor to use
	 * @param vInit the object which defines how the value function will be initialized for each individual state.
	 * @param numRollouts the number of rollouts to perform when planning is started.
	 * @param maxDelta when the maximum change in the value function from a rollout is smaller than this value, planning will terminate.
	 * @param maxDepth the maximum depth/length of a rollout before it is terminated and Bellman updates are performed.
	 */
	public RTDP(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, ValueFunctionInitialization vInit, int numRollouts, double maxDelta, int maxDepth){
		
		this.VFPInit(domain, rf, tf, gamma, hashingFactory);
		
		this.numRollouts = numRollouts;
		this.maxDelta = maxDelta;
		this.maxDepth = maxDepth;
		this.rollOutPolicy = new GreedyQPolicy(this);
		
		this.valueInitializer = vInit;
		
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
	 * @param p the rollout policy to use
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
	
	
	/**
	 * Sets the minimum number of consecutive rollsouts with a value function change less than the maxDelta value that will cause RTDP
	 * to stop.
	 * @param nRollsouts the minimum number of consecutive rollouts required.
	 */
	public void setMinNumRolloutsWithSmallValueChange(int nRollsouts){
		this.minNumRolloutsWithSmallValueChange = nRollsouts;
	}
	
	
	/**
	 * When batch mode is set, Bellman updates will be stalled until a roll out is complete and then run in reverse.
	 * @param useBatch whether to use batchmode RTDP or not.
	 */
	public void toggleBatchMode(boolean useBatch){
		this.useBatch = useBatch;
	}
	
	/**
	 * Returns the total number of Bellman updates across all planning
	 * @return the total number of Bellman updates across all planning
	 */
	public int getNumberOfBellmanUpdates(){
		return this.numberOfBellmanUpdates;
	}
	
	@Override
	public void planFromState(State initialState) {
		
		if(!useBatch){
			this.normalRTDP(initialState);
		}
		else{
			this.batchRTDP(initialState);
		}

	}
	


	
	/**
	 * Runs normal RTDP in which bellman updates are performed after each action selection.
	 * @param initialState the initial state from which to plan
	 */
	protected void normalRTDP(State initialState){
		
		int totalStates = 0;
		int consecutiveSmallDeltas = 0;
		for(int i = 0; i < numRollouts; i++){
			
			State curState = initialState;
			int nSteps = 0;
			double delta = 0;
			while(!this.tf.isTerminal(curState) && nSteps < this.maxDepth){
				
				StateHashTuple sh = this.hashingFactory.hashState(curState);
				
				//select an action
				GroundedAction ga = (GroundedAction)this.rollOutPolicy.getAction(curState);
				
				//update this state's value
				double curV = this.value(sh);
				double nV = this.performBellmanUpdateOn(sh);
				delta = Math.max(Math.abs(nV - curV), delta); 
				this.numberOfBellmanUpdates++;
				
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
	
	
	/**
	 * Performs Bellman updates only after a rollout is complete and in reverse order
	 * @param initialState the initial state from which to plan
	 */
	protected void batchRTDP(State initialState){
		
		int totalStates = 0;
		
		int consecutiveSmallDeltas = 0;
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
	
	
	/**
	 * Performs ordered Bellman updates on the list of (hashed) states provided to it.
	 * @param states the ordered list of states on which to perform Bellamn updates.
	 * @return the maximum change in the value function for the given states
	 */
	protected double performOrderedBellmanUpdates(List <StateHashTuple> states){
		
		double delta = 0.;
		for(StateHashTuple sh : states){
			
			double v = this.value(sh);
			
			double maxQ = this.performBellmanUpdateOn(sh);
			delta = Math.max(Math.abs(maxQ - v), delta);
			this.numberOfBellmanUpdates++;
			
		}
		
		return delta;
		
	}
	

}
