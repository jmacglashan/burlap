package burlap.behavior.singleagent.learning.tdmethods;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.ValueFunctionInitialization;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.EpsilonGreedy;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * Tabular Q-learning algorithm [1]. This implementation will work correctly with Options [2]. The implementation can either be used for learning or planning,
 * the latter of which is performed by running many learning episodes in succession. The number of episodes used for planning can be determined
 * by a threshold maximum number of episodes, or by a maximum change in the Q-function threshold.
 * 
 * <p/>
 * 1. Watkins, Christopher JCH, and Peter Dayan. "Q-learning." Machine learning 8.3-4 (1992): 279-292. <br/>
 * 2. Sutton, Richard S., Doina Precup, and Satinder Singh. "Between MDPs and semi-MDPs: A framework for temporal abstraction in reinforcement learning." Artificial intelligence 112.1 (1999): 181-211.
 * 
 * @author James MacGlashan
 *
 */
public class QLearning extends OOMDPPlanner implements QComputablePlanner, LearningAgent{

	
	/**
	 * The tabular mapping from states to Q-values
	 */
	protected Map<StateHashTuple, QLearningStateNode>				qIndex;
	
	/**
	 * The object that defines how Q-values are initialized.
	 */
	protected ValueFunctionInitialization							qInitFunction;
	
	/**
	 * The learning rate function used.
	 */
	protected LearningRate											learningRate;
	
	/**
	 * The learning policy to use. Typically these will be policies that link back to this object so that they change as the Q-value estimate change.
	 */
	protected Policy												learningPolicy;
	
	
	/**
	 * The maximum number of steps that will be taken in an episode before the agent terminates a learning episode
	 */
	protected int													maxEpisodeSize;
	
	/**
	 * A counter for counting the number of steps in an episode that have been taken thus far
	 */
	protected int													eStepCounter;
	
	/**
	 * The maximum number of episodes to use for planning
	 */
	protected int													numEpisodesForPlanning;
	
	/**
	 * The maximum allowable change in the Q-function during an episode before the planning method terminates.
	 */
	protected double												maxQChangeForPlanningTermination;
	
	/**
	 * The maximum Q-value change that occurred in the last learning episode.
	 */
	protected double												maxQChangeInLastEpisode = Double.POSITIVE_INFINITY;
	
	
	/**
	 * the saved previous learning episodes
	 */
	protected LinkedList<EpisodeAnalysis>							episodeHistory;
	
	/**
	 * The number of the most recent learning episodes to store.
	 */
	protected int													numEpisodesToStore;
	
	
	/**
	 * Whether options should be decomposed into actions in the returned {@link burlap.behavior.singleagent.EpisodeAnalysis} objects.
	 */
	protected boolean												shouldDecomposeOptions = true;
	
	/**
	 * Whether decomposed options should have their primitive actions annotated with the options name in the returned {@link burlap.behavior.singleagent.EpisodeAnalysis} objects.
	 */
	protected boolean												shouldAnnotateOptions = true;
	
	
	
	/**
	 * Initializes Q-learning with 0.1 epsilon greedy policy, the same Q-value initialization everywhere, and places no limit on the number of steps the 
	 * agent can take in an episode. By default the agent will only save the last learning episode and a call to the {@link #planFromState(State)} method
	 * will cause the planner to use only one episode for planning; this should probably be changed to a much larger value if you plan on using this
	 * algorithm as a planning algorithm.
	 * @param domain the domain in which to learn
	 * @param rf the reward function
	 * @param tf the terminal function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory to use for Q-lookups
	 * @param qInit the initial Q-value to user everywhere
	 * @param learningRate the learning rate
	 */
	public QLearning(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, 
			double qInit, double learningRate) {
		this.QLInit(domain, rf, tf, gamma, hashingFactory, new ValueFunctionInitialization.ConstantValueFunctionInitialization(qInit), learningRate, new EpsilonGreedy(this, 0.1), Integer.MAX_VALUE);
	}
	
	
	/**
	 * Initializes Q-learning with 0.1 epsilon greedy policy, the same Q-value initialization everywhere. By default the agent will only save the last learning episode and a call to the {@link #planFromState(State)} method
	 * will cause the planner to use only one episode for planning; this should probably be changed to a much larger value if you plan on using this
	 * algorithm as a planning algorithm.
	 * @param domain the domain in which to learn
	 * @param rf the reward function
	 * @param tf the terminal function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory to use for Q-lookups
	 * @param qInit the initial Q-value to user everywhere
	 * @param learningRate the learning rate
	 * @param maxEpisodeSize the maximum number of steps the agent will take in a learning episode for the agent stops trying.
	 */
	public QLearning(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, 
			double qInit, double learningRate, int maxEpisodeSize) {
		this.QLInit(domain, rf, tf, gamma, hashingFactory, new ValueFunctionInitialization.ConstantValueFunctionInitialization(qInit), learningRate, new EpsilonGreedy(this, 0.1), maxEpisodeSize);
	}
	
	
	/**
	 * Initializes the same Q-value initialization everywhere. Note that if the provided policy is derived from the Q-value of this learning agent (as it should be),
	 * you may need to set the policy to point to this object after call this constructor; the constructor will not do this automatically in case it was by design
	 * to use the policy that was learned in some other domain. By default the agent will only save the last learning episode and a call to the {@link #planFromState(State)} method
	 * will cause the planner to use only one episode for planning; this should probably be changed to a much larger value if you plan on using this
	 * algorithm as a planning algorithm.
	 * @param domain the domain in which to learn
	 * @param rf the reward function
	 * @param tf the terminal function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory to use for Q-lookups
	 * @param qInit the initial Q-value to user everywhere
	 * @param learningRate the learning rate
	 * @param learningPolicy the learning policy to follow during a learning episode.
	 * @param maxEpisodeSize the maximum number of steps the agent will take in a learning episode for the agent stops trying.
	 */
	public QLearning(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, 
			double qInit, double learningRate, Policy learningPolicy, int maxEpisodeSize) {
		this.QLInit(domain, rf, tf, gamma, hashingFactory, new ValueFunctionInitialization.ConstantValueFunctionInitialization(qInit), learningRate, learningPolicy, maxEpisodeSize);
	}
	
	
	/**
	 * Initializes the algorithm. Note that if the provided policy is derived from the Q-value of this learning agent (as it should be),
	 * you may need to set the policy to point to this object after call this constructor; the constructor will not do this automatically in case it was by design
	 * to use the policy that was learned in some other domain. By default the agent will only save the last learning episode and a call to the {@link #planFromState(State)} method
	 * will cause the planner to use only one episode for planning; this should probably be changed to a much larger value if you plan on using this
	 * algorithm as a planning algorithm.
	 * @param domain the domain in which to learn
	 * @param rf the reward function
	 * @param tf the terminal function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory to use for Q-lookups
	 * @param qInit a {@link burlap.behavior.singleagent.ValueFunctionInitialization} object that can be used to initialize the Q-values.
	 * @param learningRate the learning rate
	 * @param learningPolicy the learning policy to follow during a learning episode.
	 * @param maxEpisodeSize the maximum number of steps the agent will take in a learning episode for the agent stops trying.
	 */
	public QLearning(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, 
			ValueFunctionInitialization qInit, double learningRate, Policy learningPolicy, int maxEpisodeSize) {
		this.QLInit(domain, rf, tf, gamma, hashingFactory, qInit, learningRate, learningPolicy, maxEpisodeSize);
	}
	
	
	
	/**
	 * Initializes the algorithm. By default the agent will only save the last learning episode and a call to the {@link #planFromState(State)} method
	 * will cause the planner to use only one episode for planning; this should probably be changed to a much larger value if you plan on using this
	 * algorithm as a planning algorithm.
	 * @param domain the domain in which to learn
	 * @param rf the reward function
	 * @param tf the terminal function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factory to use for Q-lookups
	 * @param vInit a {@link burlap.behavior.singleagent.ValueFunctionInitialization} object that can be used to initialize the Q-values.
	 * @param learningRate the learning rate
	 * @param learningPolicy the learning policy to follow during a learning episode.
	 * @param maxEpisodeSize the maximum number of steps the agent will take in a learning episode for the agent stops trying.
	 */
	protected void QLInit(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, 
			ValueFunctionInitialization qInitFunction, double learningRate, Policy learningPolicy, int maxEpisodeSize){
		
		this.plannerInit(domain, rf, tf, gamma, hashingFactory);
		this.qIndex = new HashMap<StateHashTuple, QLearningStateNode>();
		this.learningRate = new ConstantLR(learningRate);
		this.learningPolicy = learningPolicy;
		this.maxEpisodeSize = maxEpisodeSize;
		this.qInitFunction = qInitFunction;
		
		numEpisodesToStore = 1;
		episodeHistory = new LinkedList<EpisodeAnalysis>();
		
		numEpisodesForPlanning = 1;
		maxQChangeForPlanningTermination = 0.;

		
	}
	
	
	/**
	 * Sets the learning rate function to use
	 * @param lr the learning rate function to use
	 */
	public void setLearningRateFunction(LearningRate lr){
		this.learningRate = lr;
	}
	
	/**
	 * Sets how to initialize Q-values for previously unexperienced state-action pairs.
	 * @param qInit a {@link burlap.behavior.singleagent.ValueFunctionInitialization} object that can be used to initialize the Q-values.
	 */
	public void setQInitFunction(ValueFunctionInitialization qInit){
		this.qInitFunction = qInit;
	}
	
	
	/**
	 * Sets which policy this agent should use for learning.
	 * @param p the policy to use for learning.
	 */
	public void setLearningPolicy(Policy p){
		this.learningPolicy = p;
	}
	
	/**
	 * Sets the maximum number of episodes that will be performed when the {@link #planFromState(State)} method is called.
	 * @param n the maximum number of episodes that will be performed when the {@link #planFromState(State)} method is called.
	 */
	public void setMaximumEpisodesForPlanning(int n){
		if(n > 0){
			this.numEpisodesForPlanning = n;
		}
		else{
			this.numEpisodesForPlanning = 1;
		}
	}
	
	/**
	 * Sets a max change in the Q-function threshold that will cause the {@link #planFromState(State)} to stop planning
	 * when it is achieved.
	 * @param m the maximum allowable change in the Q-function before planning stops
	 */
	public void setMaxQChangeForPlanningTerminaiton(double m){
		if(m > 0.){
			this.maxQChangeForPlanningTermination = m;
		}
		else{
			this.maxQChangeForPlanningTermination = 0.;
		}
	}
	
	/**
	 * Returns the number of steps taken in the last episode;
	 * @return the number of steps taken in the last episode;
	 */
	public int getLastNumSteps(){
		return eStepCounter;
	}
	
	
	/**
	 * Sets whether the primitive actions taken during an options will be included as steps in produced EpisodeAnalysis objects.
	 * The default value is true. If this is set to false, then EpisodeAnalysis objects returned from a learning episode will record options
	 * as a single "action" and the steps taken by the option will be hidden. 
	 * @param toggle whether to decompose options into the primitive actions taken by them or not.
	 */
	public void toggleShouldDecomposeOption(boolean toggle){
		
		this.shouldDecomposeOptions = toggle;
		for(Action a : actions){
			if(a instanceof Option){
				((Option)a).toggleShouldRecordResults(toggle);
			}
		}
	}
	
	/**
	 * Sets whether options that are decomposed into primitives will have the option that produced them and listed.
	 * The default value is true. If option decomposition is not enabled, changing this value will do nothing. When it
	 * is enabled and this is set to true, primitive actions taken by an option in EpisodeAnalysis objects will be
	 * recorded with a special action name that indicates which option was called to produce the primitive action
	 * as well as which step of the option the primitive action is. When set to false, recorded names of primitives
	 * will be only the primitive aciton's name it will be unclear which option was taken to generate it.
	 * @param toggle whether to annotate the primitive actions of options with the calling option's name.
	 */
	public void toggleShouldAnnotateOptionDecomposition(boolean toggle){
		shouldAnnotateOptions = toggle;
		for(Action a : actions){
			if(a instanceof Option){
				((Option)a).toggleShouldAnnotateResults(toggle);
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	

	@Override
	public List<QValue> getQs(State s) {
		return this.getQs(this.stateHash(s));
	}

	@Override
	public QValue getQ(State s, AbstractGroundedAction a) {
		return this.getQ(this.stateHash(s), (GroundedAction)a);
	}
	
	
	/**
	 * Returns the possible Q-values for a given hashed stated.
	 * @param s the hashed state for which to get the Q-values.
	 * @return the possible Q-values for a given hashed stated.
	 */
	protected List<QValue> getQs(StateHashTuple s) {
		QLearningStateNode node = this.getStateNode(s);
		return node.qEntry;
	}


	/**
	 * Returns the Q-value for a given hashed state and action.
	 * @param s the hashed state
	 * @param a the action
	 * @return the Q-value for a given hashed state and action; null is returned if there is not Q-value currently stored.
	 */
	protected QValue getQ(StateHashTuple s, GroundedAction a) {
		QLearningStateNode node = this.getStateNode(s);
		
		if(a.params.length > 0 && !this.domain.isObjectIdentifierDependent()){
			Map<String, String> matching = s.s.getObjectMatchingTo(node.s.s, false);
			a = this.translateAction(a, matching);
		}
		
		for(QValue qv : node.qEntry){
			if(qv.a.equals(a)){
				return qv;
			}
		}
		
		return null; //no action for this state indexed
	}
	
	
	/**
	 * Returns the {@link QLearningStateNode} object stored for the given hashed state. If no {@link QLearningStateNode} object.
	 * is stored, then it is created and has its Q-value initialize using this objects {@link burlap.behavior.singleagent.ValueFunctionInitialization} data member.
	 * @param s the hashed state for which to get the {@link QLearningStateNode} object
	 * @return the {@link QLearningStateNode} object stored for the given hashed state. If no {@link QLearningStateNode} object.
	 */
	protected QLearningStateNode getStateNode(StateHashTuple s){
		
		QLearningStateNode node = qIndex.get(s);
		
		if(node == null){
			node = new QLearningStateNode(s);
			List<GroundedAction> gas = this.getAllGroundedActions(s.s);
			if(gas.size() == 0){
				gas = this.getAllGroundedActions(s.s);
				throw new RuntimeErrorException(new Error("No possible actions in this state, cannot continue Q-learning"));
			}
			for(GroundedAction ga : gas){
				node.addQValue(ga, qInitFunction.qValue(s.s, ga));
			}
			
			qIndex.put(s, node);
		}
		
		return node;
		
	}
	
	/**
	 * Returns the maximum Q-value in the hashed stated.
	 * @param s the state for which to get he maximum Q-value;
	 * @return the maximum Q-value in the hashed stated.
	 */
	protected double getMaxQ(StateHashTuple s){
		List <QValue> qs = this.getQs(s);
		double max = Double.NEGATIVE_INFINITY;
		for(QValue q : qs){
			if(q.q > max){
				max = q.q;
			}
		}
		return max;
	}

	@Override
	public void planFromState(State initialState) {
		
		int eCount = 0;
		do{
			this.runLearningEpisodeFrom(initialState);
			eCount++;
		}while(eCount < numEpisodesForPlanning && maxQChangeInLastEpisode > maxQChangeForPlanningTermination);
		

	}

	
	@Override
	public EpisodeAnalysis runLearningEpisodeFrom(State initialState){
		return this.runLearningEpisodeFrom(initialState, maxEpisodeSize);
	}

	@Override
	public EpisodeAnalysis runLearningEpisodeFrom(State initialState, int maxSteps) {
		
		this.toggleShouldAnnotateOptionDecomposition(shouldAnnotateOptions);
		
		EpisodeAnalysis ea = new EpisodeAnalysis(initialState);
		
		StateHashTuple curState = this.stateHash(initialState);
		eStepCounter = 0;
		
		maxQChangeInLastEpisode = 0.;
		
		while(!tf.isTerminal(curState.s) && eStepCounter < maxSteps){
			
			GroundedAction action = (GroundedAction)learningPolicy.getAction(curState.s);
			QValue curQ = this.getQ(curState, action);
			
			StateHashTuple nextState = this.stateHash(action.executeIn(curState.s));
			double maxQ = 0.;
			
			if(!tf.isTerminal(nextState.s)){
				maxQ = this.getMaxQ(nextState);
			}
			
			//manage option specifics
			double r = 0.;
			double discount = this.gamma;
			if(action.action.isPrimitive()){
				r = rf.reward(curState.s, action, nextState.s);
				eStepCounter++;
				ea.recordTransitionTo(action, nextState.s, r);
			}
			else{
				Option o = (Option)action.action;
				r = o.getLastCumulativeReward();
				int n = o.getLastNumSteps();
				discount = Math.pow(this.gamma, n);
				eStepCounter += n;
				if(this.shouldDecomposeOptions){
					ea.appendAndMergeEpisodeAnalysis(o.getLastExecutionResults());
				}
				else{
					ea.recordTransitionTo(action, nextState.s, r);
				}
			}
			
			
			
			double oldQ = curQ.q;
			
			//update Q-value
			curQ.q = curQ.q + this.learningRate.pollLearningRate(curState.s, action) * (r + (discount * maxQ) - curQ.q);
			
			double deltaQ = Math.abs(oldQ - curQ.q);
			if(deltaQ > maxQChangeInLastEpisode){
				maxQChangeInLastEpisode = deltaQ;
			}
			
			//move on
			curState = nextState;
			
			
		}
		
		if(episodeHistory.size() >= numEpisodesToStore){
			episodeHistory.poll();
		}
		episodeHistory.offer(ea);
		
		return ea;
	}


	@Override
	public EpisodeAnalysis getLastLearningEpisode() {
		return episodeHistory.getLast();
	}


	@Override
	public void setNumEpisodesToStore(int numEps) {
		if(numEps > 0){
			numEpisodesToStore = numEps;
		}
		else{
			numEpisodesToStore = 1;
		}
	}


	@Override
	public List<EpisodeAnalysis> getAllStoredLearningEpisodes() {
		return episodeHistory;
	}
	
	
	@Override
	public void resetPlannerResults(){
		this.mapToStateIndex.clear();
		this.qIndex.clear();
		this.episodeHistory.clear();
		this.eStepCounter = 0;
		this.maxQChangeInLastEpisode = Double.POSITIVE_INFINITY;
	}

}
