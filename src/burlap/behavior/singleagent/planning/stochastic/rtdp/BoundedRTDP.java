package burlap.behavior.singleagent.planning.stochastic.rtdp;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.stochastic.DynamicProgramming;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.State;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.HashableStateFactory;

import java.util.*;


/**
 * An implementation of Bounded RTDP [1]. Bounded RTPD is very similar to standard {@link RTDP} [2] with the main difference
 * being that both an upper bound and lower bound value function is computed. Like RTDP, the upper bound is used for planning rollout
 * exploration, but when planning rollouts are complete, the lower bound value function is used to direct behavior. Using the lower
 * bound provides significantly better any-time planning performance that does not require convergence to get resaonable results.
 * <p>
 * Another differences in Bounded RTDP is updating the value of each state in a rollout in reverse after its completion,
 * which has the effect of propagating back goal state values to the beginning (though this can be disbaled in this implementation using
 * the {@link #setRunRolloutsInRevere(boolean)} method).
 * <p>
 * Finally, after action selection, the next outcome state from which the rollout continues may also be selected in a number of ways.
 * The way presnted in the original paper is to select the next state randomly according to the transition dynamics probability weighted
 * by the margin between the upper bound and lower bound of the states, which promotes exploration toward states that are uncertain.
 * However, in practice, we found that the standard unweighted sampling apporach of RTDP can work better and is more efficient; therefore,
 * the default in this implementation is to use the unweighted transition dynamics, but it can be changed to way presented in the paper using
 * the method {@link #setStateSelectionMode(StateSelectionMode)}. Another optional state selection mode is to always choose the next state
 * with the highest uncertainty, but this tends to be even slower due to being overly conservative so it is not reccommended in genral.
 * See the {@link StateSelectionMode} documentation for more information.
 * 
 * 
 * 
 * <p>
 * 1.McMahan, H. Brendan, Maxim Likhachev, and Geoffrey J. Gordon. "Bounded real-time dynamic programming: RTDP with monotone upper bounds and performance guarantees." 
 * Proceedings of the 22nd international conference on Machine learning. ACM, 2005.
 * <p>
 * 2. Barto, Andrew G., Steven J. Bradtke, and Satinder P. Singh. "Learning to act using real-time dynamic programming." Artificial Intelligence 72.1 (1995): 81-138.
 * 
 * 
 * @author James MacGlashan
 *
 */
public class BoundedRTDP extends DynamicProgramming implements Planner {

	
	/**
	 * The different ways that states can be selected for expansion. That is, after an action is selected in the rollout,
	 * the next state from the possible outcome states may be selected in these different ways. The default, and reccomended mode
	 * is MODELBASED.<p>
	 * MODELBASED is a standard RTDP-like sampling from the transition dynamics. This is the default mode and in practice,
	 * we found it gives faster performance than the reccomeneded approach in the Bounded RTDP paper. Additionally, if the domain
	 * has an efficient performAction state sampler, this mode will provide some computational gains over the other approaches.
	 * <p>
	 * WEIGHTEDMARGIN is the state selection method suggested in the original Bounded RTDP paper, which we found to be somewhat slower than
	 * MODELBASED. In this approach, each possible outcome state is selected randomly from a distribution that is the transition dynamics probability
	 * weighted by the margin in the lower bound and upper bound of the next state's value function. This promotes exploration towards states that
	 * are more uncertain.
	 * <p>
	 * MAXMARGIN selects the state with the maximum margin between the lower bound and upperbound of the value function. Ties are broken randomly.
	 * This method is much more exploratory, but results in significantly longer initial rollouts.
	 * @author James MacGlashan
	 *
	 */
	public static enum StateSelectionMode{
		MODELBASED, WEIGHTEDMARGIN, MAXMARGIN
	}
	
	
	/**
	 * The lower bound value function
	 */
	protected Map<HashableState, Double>		lowerBoundV = new HashMap<HashableState, Double>();
	
	/**
	 * The upperbound value function
	 */
	protected Map<HashableState, Double>		upperBoundV = new HashMap<HashableState, Double>();
	
	
	/**
	 * The lowerbound value function initialization
	 */
	protected ValueFunctionInitialization		lowerVInit;
	
	/**
	 * The upperbound value function initialization
	 */
	protected ValueFunctionInitialization		upperVInit;
	
	/**
	 * the max number of rollouts to perform when planning is started unless the value function margin is small enough. If
	 * set to -1, then there is no limit.
	 */
	protected int								maxRollouts = -1;
	
	/**
	 * The max permitted difference between the lower bound and upperbound for planning termination.
	 */
	protected double							maxDiff;
	
	
	/**
	 * The maximum depth/length of a rollout before it is terminated and Bellman updates are performed. If set to -1
	 * then there is no limit; the default is -1.
	 */
	protected int								maxDepth = -1;
	
	
	/**
	 * Whether the current {@link burlap.behavior.singleagent.planning.stochastic.DynamicProgramming} valueFunction reference points to the lower bound value function or the upper bound value function.
	 * If true, then it points to the lower bound; if false then to the upper bound.
	 */
	protected boolean							currentValueFunctionIsLower = false;
	
	
	/**
	 * Sets what the {@link burlap.behavior.singleagent.planning.stochastic.DynamicProgramming} valueFunction reference points to (the lower bound or upperbound) once a planning rollout is complete.
	 * If true, then it points to the lower bound; if false then the upper bound. Pointing to the lower bound is default and provides any-time planning
	 * performance.
	 */
	protected boolean							defaultToLowerValueAfterPlanning = true;
	
	/**
	 * Which state selection mode is used. Refer to the {@link StateSelectionMode} documentation for more information on the modes. The default is MODELBASED.
	 */
	protected StateSelectionMode				selectionMode = StateSelectionMode.MODELBASED;
	
	/**
	 * Keeps track of the number of Bellman updates that have been performed across all planning.
	 */
	protected int								numBellmanUpdates = 0;
	
	
	/**
	 * Keeps track of the number of rollout steps that have been performed across all planning rollouts.
	 */
	protected int								numSteps = 0;
	
	
	/**
	 * Whether each rollout should be run in reverse after completion. This is useful in goal-directed MDPs because it backups the goal reward to the initial state.
	 * The default is true.
	 */
	protected boolean							runRolloutsInReverse = true;
	
	
	
	/**
	 * Initializes.
	 * @param domain the domain in which to plan
	 * @param rf the reward function
	 * @param tf the terminal state function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factor to use
	 * @param lowerVInit the value function lower bound initialization
	 * @param upperVInit the value function upper bound initialization
	 * @param maxDiff the max permitted difference in value function margin to permit planning termination. This value is also used to prematurely stop a rollout if the next state's margin is under this value.
	 * @param maxRollouts the maximum number of rollouts permitted before planning is forced to terminate. If set to -1 then there is no limit.
	 */
	public BoundedRTDP(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, HashableStateFactory hashingFactory,
			ValueFunctionInitialization lowerVInit, ValueFunctionInitialization upperVInit, double maxDiff, int maxRollouts){
		this.DPPInit(domain, rf, tf, gamma, hashingFactory);
		this.lowerVInit = lowerVInit;
		this.upperVInit = upperVInit;
		this.maxDiff = maxDiff;
		this.maxRollouts = maxRollouts;
		
		this.useCachedTransitions = false;
	}
	

	/**
	 * Sets the maximum number of rollouts permitted before planning is forced to terminate. If set to -1 then there is no limit.
	 * @param numRollouts the maximum number of rollouts permitted before planning is forced to terminate. If set to -1 then there is no limit.
	 */
	public void setMaxNumberOfRollouts(int numRollouts){
		this.maxRollouts = numRollouts;
	}
	
	/**
	 * Sets the maximum rollout depth of any rollout. If set to -1, then there is no limit on rollout depth.
	 * @param maxDepth the maximum rollout depth of any rollout. If set to -1, then there is no limit on rollout depth.
	 */
	public void setMaxRolloutDepth(int maxDepth){
		this.maxDepth = maxDepth;
	}
	
	/**
	 * Sets the max permitted difference in value function margin to permit planning termination. 
	 * This value is also used to prematurely stop a rollout if the next state's margin is under this value.
	 * @param maxDiff the max permitted difference in value function margin to permit planning termination.
	 */
	public void setMaxDifference(double maxDiff){
		this.maxDiff = maxDiff;
	}
	
	/**
	 * Sets the state selection mode used when choosing next states to expand. See the {@link StateSelectionMode} documentation for more information on the modes.
	 * @param selectionMode the state selection mode to use.
	 */
	public void setStateSelectionMode(StateSelectionMode selectionMode){
		this.selectionMode = selectionMode;
	}
	
	/**
	 * Use this method to set which value function--the lower bound or upper bound--to use after a planning rollout is complete. Setting this
	 * value affects which values the {@link #value(State)}, {@link #getQs(State)}, and {@link #getQ(State, AbstractGroundedAction)} methods returns.
	 * Using the lower bound results in anytime performance.
	 * @param useLowerBound if true, then the value function is set to use the lower bound after planning. If false, then the upper bound is used.
	 */
	public void setDefaultValueFunctionAfterARollout(boolean useLowerBound){
		this.defaultToLowerValueAfterPlanning = useLowerBound;
	}
	
	/**
	 * Sets whether each rollout should be run in reverse after completion. This is useful in goal-directed MDPs because it backups the goal reward to the initial state.
	 * @param runRolloutsInRevers if true, then rollouts will be run in reverse. If false, then they will not be run in reverse.
	 */
	public void setRunRolloutsInRevere(boolean runRolloutsInRevers){
		this.runRolloutsInReverse = runRolloutsInRevers;
	}



	/**
	 * Plans from the input state and then returns a {@link burlap.behavior.policy.GreedyQPolicy} that greedily
	 * selects the action with the highest Q-value and breaks ties uniformly randomly.
	 * @param initialState the initial state of the planning problem
	 * @return a {@link burlap.behavior.policy.GreedyQPolicy}.
	 */
	@Override
	public GreedyQPolicy planFromState(State initialState) {
	
		DPrint.cl(this.debugCode, "Beginning Planning.");
		int nr = 0;
		while(this.runRollout(initialState) > this.maxDiff && (nr < this.maxRollouts || this.maxRollouts == -1)){
			nr++;
		}
		
		
		DPrint.cl(this.debugCode, "Finished planning with a total of " + this.numBellmanUpdates + " backups.");

		return new GreedyQPolicy(this);

	}
	
	/**
	 * Sets the value function to use to be the upper bound.
	 */
	public void setValueFunctionToUpperBound(){
		this.valueFunction = this.upperBoundV;
		this.valueInitializer = this.upperVInit;
		this.currentValueFunctionIsLower = false;
	}
	
	
	/**
	 * Sets the value function to use to be the lower bound.
	 */
	public void setValueFunctionToLowerBound(){
		this.valueFunction = this.lowerBoundV;
		this.valueInitializer = this.lowerVInit;
		this.currentValueFunctionIsLower = true;
	}
	
	/**
	 * Returns the total number of Bellman updates across all planning
	 * @return the total number of Bellman updates across all planning
	 */
	public int getNumberOfBellmanUpdates(){
		return this.numBellmanUpdates;
	}
	
	/**
	 * Returns the total number of planning steps that have been performed.
	 * @return the total number of planning steps that have been performed.
	 */
	public int getNumberOfSteps(){
		return this.numSteps;
	}
	
	
	/**
	 * Runs a planning rollout from the provided state.
	 * @param s the initial state from which a planning rollout should be performed.
	 * @return the margin between the lower bound and upper bound value function for the initial state.
	 */
	public double runRollout(State s){
		LinkedList<HashableState> trajectory = new LinkedList<HashableState>();
		
		HashableState csh = this.hashingFactory.hashState(s);
		
		while(!this.tf.isTerminal(csh.s) && (trajectory.size() < this.maxDepth+1 || this.maxDepth == -1)){
			
			if(this.runRolloutsInReverse){
				trajectory.offerFirst(csh);
			}
			
			this.setValueFunctionToLowerBound();
			QValue mxL = this.maxQ(csh.s);
			this.lowerBoundV.put(csh, mxL.q);
			
			this.setValueFunctionToUpperBound();
			QValue mxU = this.maxQ(csh.s);
			this.upperBoundV.put(csh, mxU.q);
			
			numBellmanUpdates += 2;
			this.numSteps++;
			
			StateSelectionAndExpectedGap select = this.getNextState(csh.s, (GroundedAction)mxU.a);
			csh = select.sh;
			
			if(select.expectedGap < this.maxDiff){
				break;
			}
			
			
		}
		
		if(this.tf.isTerminal(csh.s)){
			this.lowerBoundV.put(csh, 0.);
			this.upperBoundV.put(csh, 0.);
		}
		
		
		double lastGap = 0.;
		
		//run in reverse
		if(this.runRolloutsInReverse){
			while(!trajectory.isEmpty()){
				HashableState sh = trajectory.pop();
				this.setValueFunctionToLowerBound();
				QValue mxL = this.maxQ(sh.s);
				this.lowerBoundV.put(sh, mxL.q);
				
				this.setValueFunctionToUpperBound();
				QValue mxU = this.maxQ(sh.s);
				this.upperBoundV.put(sh, mxU.q);
				
				numBellmanUpdates += 2;
				lastGap = mxU.q - mxL.q;
				
			}
		}
		else{
			lastGap = this.getGap(this.hashingFactory.hashState(s));
		}
		
		
		if(this.defaultToLowerValueAfterPlanning){
			this.setValueFunctionToLowerBound();
		}
		else{
			this.setValueFunctionToUpperBound();
		}
		
		return lastGap;
		
	}
	
	/**
	 * Selects a next state for expansion when action a is applied in state s.
	 * @param s the source state of the transition
	 * @param a the action applied in the source state
	 * @return a {@link StateSelectionAndExpectedGap} object holding the next state to be expanded and the expected margin size of this transition.
	 */
	protected StateSelectionAndExpectedGap getNextState(State s, GroundedAction a){
		
		if(this.selectionMode == StateSelectionMode.MODELBASED){
			HashableState nsh =  this.hashingFactory.hashState(a.executeIn(s));
			double gap = this.getGap(nsh);
			return new StateSelectionAndExpectedGap(nsh, gap);
		}
		else if(this.selectionMode == StateSelectionMode.WEIGHTEDMARGIN){
			return this.getNextStateBySampling(s, a);
		}
		else if(this.selectionMode == StateSelectionMode.MAXMARGIN){
			return this.getNextStateByMaxMargin(s, a);
		}
		throw new RuntimeException("Unknown state selection mode.");
	}
	
	
	/**
	 * Selects a next state for expansion when action a is applied in state s according to the next possible state that has the largest lower and upper bound margin.
	 * Ties are broken randomly.
	 * @param s the source state of the transition
	 * @param a the action applied in the source state
	 * @return a {@link StateSelectionAndExpectedGap} object holding the next state to be expanded and the expected margin size of this transition.
	 */
	protected StateSelectionAndExpectedGap getNextStateByMaxMargin(State s, GroundedAction a){
		
		List<TransitionProbability> tps = a.getTransitions(s);
		double sum = 0.;
		double maxGap = Double.NEGATIVE_INFINITY;
		List<HashableState> maxStates = new ArrayList<HashableState>(tps.size());
		for(TransitionProbability tp : tps){
			HashableState nsh = this.hashingFactory.hashState(tp.s);
			double gap = this.getGap(nsh);
			sum += tp.p*gap;
			if(gap == maxGap){
				maxStates.add(nsh);
			}
			else if(gap > maxGap){
				maxStates.clear();
				maxStates.add(nsh);
				maxGap = gap;
			}
		}
		
		int rint = RandomFactory.getMapped(0).nextInt(maxStates.size());
		StateSelectionAndExpectedGap select = new StateSelectionAndExpectedGap(maxStates.get(rint), sum);
		
		return select;
	}
	
	
	/**
	 * Selects a next state for expansion when action a is applied in state s by randomly sampling from the transition dynamics weighted by the margin of the lower and
	 * upper bound value functions.
	 * @param s the source state of the transition
	 * @param a the action applied in the source state
	 * @return a {@link StateSelectionAndExpectedGap} object holding the next state to be expanded and the expected margin size of this transition.
	 */
	protected StateSelectionAndExpectedGap getNextStateBySampling(State s, GroundedAction a){
		
		List<TransitionProbability> tps = a.getTransitions(s);
		double sum = 0.;
		double [] weightedGap = new double[tps.size()];
		HashableState[] hashedStates = new HashableState[tps.size()];
		for(int i = 0; i < tps.size(); i++){
			TransitionProbability tp = tps.get(i);
			HashableState nsh = this.hashingFactory.hashState(tp.s);
			hashedStates[i] = nsh;
			double gap = this.getGap(nsh);
			weightedGap[i] = tp.p*gap;
			sum += weightedGap[i];
		}
		
		double roll = RandomFactory.getMapped(0).nextDouble();
		double cumSum = 0.;
		for(int i = 0; i < weightedGap.length; i++){
			cumSum += weightedGap[i]/sum;
			if(roll < cumSum){
				StateSelectionAndExpectedGap select = new StateSelectionAndExpectedGap(hashedStates[i], sum);
				return select;
			}
		}
		
		throw new RuntimeException("Error: probabilities in state selection did not sum to 1.");
		
	}
	
	
	/**
	 * Returns the lower bound and upper bound value function margin/gap for the given state
	 * @param sh the state whose margin should be returned.
	 * @return the lower bound and upper bound value function margin/gap for the given state
	 */
	protected double getGap(HashableState sh){
		this.setValueFunctionToLowerBound();
		double l = this.value(sh);
		this.setValueFunctionToUpperBound();
		double u = this.value(sh);
		double gap = u-l;
		return gap;
	}
	
	
	/**
	 * Returns the maximum Q-value entry for the given state with ties broken randomly. 
	 * @param s the query state for the Q-value
	 * @return the maximum Q-value entry for the given state with ties broken randomly. 
	 */
	protected QValue maxQ(State s){
		
		List<QValue> qs = this.getQs(s);
		double max = Double.NEGATIVE_INFINITY;
		List<QValue> maxQs = new ArrayList<QValue>(qs.size());
		
		for(QValue q : qs){
			if(q.q == max){
				maxQs.add(q);
			}
			else if(q.q > max){
				max = q.q;
				maxQs.clear();
				maxQs.add(q);
			}
		}
		
		//return random max
		int rint = RandomFactory.getMapped(0).nextInt(maxQs.size());
		
		return maxQs.get(rint);
	}
	
	
	
	/**
	 * A tuple class for a hashed state and the expected value function margin/gap of a the source transition.
	 * @author James MacGlashan
	 *
	 */
	protected static class StateSelectionAndExpectedGap{
		
		/**
		 * The selected state
		 */
		public HashableState sh;
		
		/**
		 * The expected margin/gap of the value function from the source transition
		 */
		public double expectedGap;
		
		/**
		 * Initializes.
		 * @param sh The selected state
		 * @param expectedGap The expected margin/gap of the value function from the source transition
		 */
		public StateSelectionAndExpectedGap(HashableState sh, double expectedGap){
			this.sh = sh;
			this.expectedGap = expectedGap;
		}
		
	}
	
	

}
