package burlap.behavior.singleagent.learning.actorcritic.critics;

import burlap.behavior.singleagent.options.EnvironmentOptionOutcome;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An implementation of TDLambda that can be used as a critic for {@link burlap.behavior.singleagent.learning.actorcritic.ActorCritic} algorithms [1], except
 * that this class treats states at different depths as unique states. In general the typical {@link TDLambda} method is recommend unless a special 
 * {@link burlap.behavior.singleagent.learning.actorcritic.Actor} object that exploits the time information is to be used as well.
 * 
 * <p>
 * 1. Barto, Andrew G., Steven J. Bradtke, and Satinder P. Singh. "Learning to act using real-time dynamic programming." Artificial Intelligence 72.1 (1995): 81-138.
 * @author James MacGlashan
 *
 */
public class TimeIndexedTDLambda extends TDLambda {

	/**
	 * The time/depth indexed value function
	 */
	protected List<Map<HashableState, VValue>>			vTIndex;
	
	/**
	 * The current time index / depth of the current episode
	 */
	protected int										curTime;
	
	/**
	 * The maximum number of steps possible in an episode.
	 */
	protected int										maxEpisodeSize = Integer.MAX_VALUE;
	
	
	/**
	 * Initializes the algorithm.
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory to use for hashing states and performing equality checks. 
	 * @param learningRate the learning rate that affects how quickly the estimated value function is adjusted.
	 * @param vinit a constant value function initialization value to use.
	 * @param lambda indicates the strength of eligibility traces. Use 1 for Monte-carlo-like traces and 0 for single step backups
	 */
	public TimeIndexedTDLambda(double gamma, HashableStateFactory hashingFactory, double learningRate, double vinit, double lambda) {
		super(gamma, hashingFactory, learningRate, vinit, lambda);
		
		this.vTIndex = new ArrayList<Map<HashableState,VValue>>();
		
	}
	
	
	/**
	 * Initializes the algorithm.
	 * @param rf the reward function
	 * @param tf the terminal state function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory to use for hashing states and performing equality checks. 
	 * @param learningRate the learning rate that affects how quickly the estimated value function is adjusted.
	 * @param vinit a constant value function initialization value to use.
	 * @param lambda indicates the strength of eligibility traces. Use 1 for Monte-carlo-like traces and 0 for single step backups
	 * @param maxEpisodeSize the maximum number of steps possible in an episode
	 */
	public TimeIndexedTDLambda(RewardFunction rf, TerminalFunction tf, double gamma, HashableStateFactory hashingFactory, double learningRate, double vinit, double lambda, int maxEpisodeSize) {
		super(gamma, hashingFactory, learningRate, vinit, lambda);
		
		this.maxEpisodeSize = maxEpisodeSize;
		this.vTIndex = new ArrayList<Map<HashableState,VValue>>();
		
	}
	
	
	/**
	 * Initializes the algorithm.
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory to use for hashing states and performing equality checks. 
	 * @param learningRate the learning rate that affects how quickly the estimated value function is adjusted.
	 * @param vinit a method of initializing the value function for previously unvisited states.
	 * @param lambda indicates the strength of eligibility traces. Use 1 for Monte-carlo-like traces and 0 for single step backups
	 * @param maxEpisodeSize the maximum number of steps possible in an episode
	 */
	public TimeIndexedTDLambda(double gamma, HashableStateFactory hashingFactory, double learningRate, ValueFunction vinit, double lambda, int maxEpisodeSize) {
		super(gamma, hashingFactory, learningRate, vinit, lambda);
		
		this.maxEpisodeSize = maxEpisodeSize;
		this.vTIndex = new ArrayList<Map<HashableState,VValue>>();
		
	}

	
	/**
	 * Returns the current time/depth of the current episodes
	 * @return the current time/depth of the current episodes
	 */
	public int getCurTime(){
		return curTime;
	}
	
	/**
	 * Sets the time/depth of the current episode.
	 * @param t the time/depth of the current episode.
	 */
	public void setCurTime(int t){
		this.curTime = t;
	}
	
	@Override
	public void startEpisode(State s) {
		super.startEpisode(s);
		this.curTime = 0;
	}

	@Override
	public void endEpisode() {
		super.endEpisode();
	}
	
	
	@Override
	public double critique(EnvironmentOutcome eo) {
		
		HashableState sh = hashingFactory.hashState(eo.o);
		HashableState shprime = hashingFactory.hashState(eo.op);
		
		double r = eo.r;
		double discount = gamma;
		int n = 1;
		if(eo.a instanceof Option){
			n = ((EnvironmentOptionOutcome)eo).numSteps();
			discount = Math.pow(gamma, n);
		}
		
		VValue vs = this.getV(sh, curTime);
		double nextV = 0.;
		if(!eo.terminated && this.curTime < this.maxEpisodeSize-1){
			nextV = this.getV(shprime, curTime+n).v;
		}
		
		double delta = r + discount*nextV - vs.v;
		
		//update all traces
		for(StateEligibilityTrace t : traces){
			double learningRate = this.learningRate.pollLearningRate(this.totalNumberOfSteps, t.sh.s(), null);
			t.v.v = t.v.v + learningRate * delta * t.eligibility;
			t.eligibility = t.eligibility * lambda * discount;
		}
		
		//always need to add the current state since it's a different time stamp for each state
		double learningRate = this.learningRate.pollLearningRate(this.totalNumberOfSteps, sh.s(), null);
		vs.v = vs.v + learningRate * delta;
		StateEligibilityTrace t = new StateTimeElibilityTrace(sh, curTime, discount*this.lambda, vs);
		traces.add(t);

		//update time stamp for next visit
		curTime += n;

		
		this.totalNumberOfSteps++;
		
		return delta;
	}
	
	
	/**
	 * Returns the {@link TDLambda.VValue} object (storing the value) for a given hashed stated at the specified time/depth.
	 * @param sh the hashed state for which the value should be returned.
	 * @param t the time/depth at which the state is visited
	 * @return the {@link TDLambda.VValue} object (storing the value) for a given hashed stated at the specified time/depth
	 */
	protected VValue getV(HashableState sh, int t){
		
		while(vTIndex.size() <= t){
			vTIndex.add(new HashMap<HashableState, TDLambda.VValue>());
		}
		
		Map <HashableState, VValue> timeMap = vTIndex.get(t);
		
		VValue v = timeMap.get(sh);
		if(v == null){
			v = new VValue(this.vInitFunction.value(sh.s()));
			timeMap.put(sh, v);
		}
		return v;
	}
	
	
	@Override
	public void reset(){
		super.reset();
		this.vTIndex.clear();
	}
	
	
	/**
	 * Extends the standard {@link TDLambda.StateEligibilityTrace} to include time/depth information.
	 * @author James MacGlashan
	 *
	 */
	public static class StateTimeElibilityTrace extends StateEligibilityTrace{

		/**
		 * The time/depth of the state this eligibility represents.
		 */
		public int timeIndex;
		
		
		/**
		 * Initializes with hashed state, eligibility value, time/depth of the state, and the value function value associated with the state.
		 * @param sh the hashed input state for this eligibility
		 * @param time the time/depth of the state at which it was first visited.
		 * @param eligibility the eligibility of the state
		 * @param v the value function value for the state.
		 */
		public StateTimeElibilityTrace(HashableState sh, int time, double eligibility, VValue v) {
			super(sh, eligibility, v);
			this.timeIndex = time;
		}
		
		
		
		
		
	}
	
	
}
