package burlap.behavior.singleagent.learning.tdmethods.vfa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.EpsilonGreedy;
import burlap.behavior.singleagent.vfa.ActionApproximationResult;
import burlap.behavior.singleagent.vfa.FunctionWeight;
import burlap.behavior.singleagent.vfa.ValueFunctionApproximation;
import burlap.behavior.singleagent.vfa.WeightGradient;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;



/**
 * Gradient Descent SARSA(\lambda) implementation [1]. This implementation will work correctly with options [2]. This implementation will work
 * with both linear and non-linear value function approximations by using the gradient value provided to it through the 
 * {@link burlap.behavior.singleagent.vfa.ValueFunctionApproximation} interface provided. <p/>The implementation can either be used for learning or planning,
 * the latter of which is performed by running many learning episodes in succession. The number of episodes used for planning can be determined
 * by a threshold maximum number of episodes, or by a maximum change in the VFA weight threshold.
 * @author James MacGlashan
 * 
 * <p/>
 * 1. Rummery, Gavin A., and Mahesan Niranjan. On-line Q-learning using connectionist systems. University of Cambridge, Department of Engineering, 1994. <br/>
 * 2. 2. Sutton, Richard S., Doina Precup, and Satinder Singh. "Between MDPs and semi-MDPs: A framework for temporal abstraction in reinforcement learning." Artificial intelligence 112.1 (1999): 181-211.
 *
 */
public class GradientDescentSarsaLam extends OOMDPPlanner implements QComputablePlanner, LearningAgent {
	
	
	/**
	 * The object that performs value function approximation
	 */
	protected ValueFunctionApproximation							vfa;
	
	/**
	 * A learning rate function to use
	 */
	protected LearningRate											learningRate;
	
	/**
	 * The learning policy to use. Typically these will be policies that link back to this object so that they change as the Q-value estimate change.
	 */
	protected Policy												learningPolicy;
	
	/**
	 * the strength of eligibility traces (0 for one step, 1 for full propagation)
	 */
	protected double												lambda;
	
	
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
	 * The maximum allowable change in the VFA weights during an episode before the planning method terminates.
	 */
	protected double												maxWeightChangeForPlanningTermination;
	
	/**
	 * The maximum VFA weight change that occurred in the last learning episode.
	 */
	protected double												maxWeightChangeInLastEpisode;
	
	
	/**
	 * The minimum eligibility value of a trace that will cause it to be updated
	 */
	protected double												minEligibityForUpdate = 0.01;
	
	
	/**
	 * the saved previous learning episodes
	 */
	protected LinkedList<EpisodeAnalysis>							episodeHistory;
	
	/**
	 * The number of the most recent learning episodes to store.
	 */
	protected int													numEpisodesToStore;
	
	
	/**
	 * Whether to use accumulating or replacing eligibility traces.
	 */
	protected boolean												useReplacingTraces = false;
	
	/**
	 * Whether options should be decomposed into actions in the returned {@link burlap.behavior.singleagent.EpisodeAnalysis} objects.
	 */
	protected boolean												shouldDecomposeOptions = true;
	
	
	/**
	 * Whether decomposed options should have their primitive actions annotated with the options name in the returned {@link burlap.behavior.singleagent.EpisodeAnalysis} objects.
	 */
	protected boolean												shouldAnnotateOptions = true;
	
	
	
	
	
	/**
	 * Initializes SARSA(\lambda) with 0.1 epsilon greedy policy and places no limit on the number of steps the 
	 * agent can take in an episode. By default the agent will only save the last learning episode and a call to the {@link planFromState(State)} method
	 * will cause the planner to use only one episode for planning; this should probably be changed to a much larger value if you plan on using this
	 * algorithm as a planning algorithm.
	 * @param domain the domain in which to learn
	 * @param rf the reward function
	 * @param tf the terminal function
	 * @param gamma the discount factor
	 * @param ValueFunctionApproximation the value function approximation method to use for estimate Q-values
	 * @param learningRate the learning rate
	 * @param lambda specifies the strength of eligibility traces (0 for one step, 1 for full propagation)
	 */
	public GradientDescentSarsaLam(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, ValueFunctionApproximation vfa, 
			double learningRate, double lamda) {
		
		this.GDSLInit(domain, rf, tf, gamma, vfa, learningRate, new EpsilonGreedy(this, 0.1), Integer.MAX_VALUE, lamda);
		
	}
	
	
	/**
	 * Initializes SARSA(\lambda) with 0.1 epsilon greedy policy. By default the agent will only save the last learning episode and a call to the {@link planFromState(State)} method
	 * will cause the planner to use only one episode for planning; this should probably be changed to a much larger value if you plan on using this
	 * algorithm as a planning algorithm.
	 * @param domain the domain in which to learn
	 * @param rf the reward function
	 * @param tf the terminal function
	 * @param gamma the discount factor
	 * @param ValueFunctionApproximation the value function approximation method to use for estimate Q-values
	 * @param learningRate the learning rate
	 * @param maxEpisodeSize the maximum number of steps the agent will take in an episode before terminating
	 * @param lambda specifies the strength of eligibility traces (0 for one step, 1 for full propagation)
	 */
	public GradientDescentSarsaLam(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, ValueFunctionApproximation vfa, 
			double learningRate, int maxEpisodeSize, double lamda) {
		
		this.GDSLInit(domain, rf, tf, gamma, vfa, learningRate, new EpsilonGreedy(this, 0.1), maxEpisodeSize, lamda);
		
	}
	
	
	/**
	 * Initializes SARSA(\lambda) By default the agent will only save the last learning episode and a call to the {@link planFromState(State)} method
	 * will cause the planner to use only one episode for planning; this should probably be changed to a much larger value if you plan on using this
	 * algorithm as a planning algorithm.
	 * @param domain the domain in which to learn
	 * @param rf the reward function
	 * @param tf the terminal function
	 * @param gamma the discount factor
	 * @param ValueFunctionApproximation the value function approximation method to use for estimate Q-values
	 * @param learningRate the learning rate
	 * @param learningPolicy the learning policy to follow during a learning episode.
	 * @param maxEpisodeSize the maximum number of steps the agent will take in an episode before terminating
	 * @param lambda specifies the strength of eligibility traces (0 for one step, 1 for full propagation)
	 */
	public GradientDescentSarsaLam(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, ValueFunctionApproximation vfa, 
			double learningRate, Policy learningPolicy, int maxEpisodeSize, double lamda) {
	
		this.GDSLInit(domain, rf, tf, gamma, vfa, learningRate, learningPolicy, maxEpisodeSize, lamda);
	}

	
	/**
	 * Initializes SARSA(\lambda) By default the agent will only save the last learning episode and a call to the {@link planFromState(State)} method
	 * will cause the planner to use only one episode for planning; this should probably be changed to a much larger value if you plan on using this
	 * algorithm as a planning algorithm.
	 * @param domain the domain in which to learn
	 * @param rf the reward function
	 * @param tf the terminal function
	 * @param gamma the discount factor
	 * @param ValueFunctionApproximation the value function approximation method to use for estimate Q-values
	 * @param learningRate the learning rate
	 * @param learningPolicy the learning policy to follow during a learning episode.
	 * @param maxEpisodeSize the maximum number of steps the agent will take in an episode before terminating
	 * @param lambda specifies the strength of eligibility traces (0 for one step, 1 for full propagation)
	 */
	protected void GDSLInit(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, ValueFunctionApproximation vfa, 
			double learningRate, Policy learningPolicy, int maxEpisodeSize, double lamda){
		
		this.plannerInit(domain, rf, tf, gamma, null);
		this.vfa = vfa;
		this.learningRate = new ConstantLR(learningRate);
		this.learningPolicy = learningPolicy;
		this.maxEpisodeSize = maxEpisodeSize;
		this.lambda = lamda;
		
		numEpisodesToStore = 1;
		episodeHistory = new LinkedList<EpisodeAnalysis>();
		
		numEpisodesForPlanning = 1;
		maxWeightChangeForPlanningTermination = 0.;

		
	}
	
	
	/**
	 * Sets the learning rate function to use.
	 * @param lr the learning rate function to use.
	 */
	public void setLearningRate(LearningRate lr){
		this.learningRate = lr;
	}
	
	/**
	 * Sets which policy this agent should use for learning.
	 * @param p the policy to use for learning.
	 */
	public void setLearningPolicy(Policy p){
		this.learningPolicy = p;
	}
	
	
	/**
	 * Sets the maximum number of episodes that will be performed when the {@link planFromState(State)} method is called.
	 * @param n the maximum number of episodes that will be performed when the {@link planFromState(State)} method is called.
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
	 * Sets a max change in the VFA weight threshold that will cause the {@link planFromState(State)} to stop planning
	 * when it is achieved.
	 * @param m the maximum allowable change in the VFA weights before planning stops
	 */
	public void setMaxVFAWeightChangeForPlanningTerminaiton(double m){
		if(m > 0.){
			this.maxWeightChangeForPlanningTermination = m;
		}
		else{
			this.maxWeightChangeForPlanningTermination = 0.;
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
	 * Sets whether to use replacing eligibility traces rather than accumulating traces.
	 * @param toggle
	 */
	public void setUseReplaceTraces(boolean toggle){
		this.useReplacingTraces = toggle;
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
	public EpisodeAnalysis runLearningEpisodeFrom(State initialState) {
		return this.runLearningEpisodeFrom(initialState, maxEpisodeSize);
	}
	
	@Override
	public EpisodeAnalysis runLearningEpisodeFrom(State initialState, int maxSteps) {
		
		EpisodeAnalysis ea = new EpisodeAnalysis(initialState);
		maxWeightChangeInLastEpisode = 0.;
		
		State curState = initialState;
		eStepCounter = 0;
		Map <Integer, EligibilityTraceVector> traces = new HashMap<Integer, GradientDescentSarsaLam.EligibilityTraceVector>();
		
		GroundedAction action = (GroundedAction)this.learningPolicy.getAction(curState);
		List<ActionApproximationResult> allCurApproxResults = this.getAllActionApproximations(curState);
		ActionApproximationResult curApprox = ActionApproximationResult.extractApproximationForAction(allCurApproxResults, action);
		
		
		while(!tf.isTerminal(curState) && eStepCounter < maxSteps){
			
			
			WeightGradient gradient = this.vfa.getWeightGradient(curApprox.approximationResult);
			
			State nextState = action.executeIn(curState);
			GroundedAction nextAction = (GroundedAction)this.learningPolicy.getAction(nextState);
			List<ActionApproximationResult> allNextApproxResults = this.getAllActionApproximations(nextState);
			ActionApproximationResult nextApprox = ActionApproximationResult.extractApproximationForAction(allNextApproxResults, nextAction);
			double nextQV = nextApprox.approximationResult.predictedValue;
			if(tf.isTerminal(nextState)){
				nextQV = 0.;
			}
			
			
			//manage option specifics
			double r = 0.;
			double discount = this.gamma;
			if(action.action.isPrimitive()){
				r = rf.reward(curState, action, nextState);
				eStepCounter++;
				ea.recordTransitionTo(nextState, action, r);
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
					ea.recordTransitionTo(nextState, action, r);
				}
			}
			
			//delta
			double delta = r + (discount * nextQV) - curApprox.approximationResult.predictedValue;
			
			
			if(useReplacingTraces){
				//then first clear traces of unselected action and reset the trace for the selected one
				for(ActionApproximationResult aar : allCurApproxResults){
					if(!aar.ga.equals(action)){ //clear unselected action trace
						for(FunctionWeight fw : aar.approximationResult.functionWeights){
							traces.remove(fw.weightId());
						}
					}
					else{ //reset trace of selected action
						for(FunctionWeight fw : aar.approximationResult.functionWeights){
							EligibilityTraceVector storedTrace = traces.get(fw.weightId());
							if(storedTrace != null){
								storedTrace.eligibilityValue = 0.;
							}
						}
					}
				}
			}
			
			
			double learningRate = this.learningRate.pollLearningRate(curState, action);
			
			//update all traces
			Set <Integer> deletedSet = new HashSet<Integer>();
			for(EligibilityTraceVector et : traces.values()){
				
				int weightId = et.weight.weightId();
				
				
				
				et.eligibilityValue += gradient.getPartialDerivative(weightId);
				double newWeight = et.weight.weightValue() + learningRate*delta*et.eligibilityValue;
				et.weight.setWeight(newWeight);
				
				double deltaW = Math.abs(et.initialWeightValue - newWeight);
				if(deltaW > maxWeightChangeInLastEpisode){
					maxWeightChangeInLastEpisode = deltaW;
				}
				
				et.eligibilityValue *= this.lambda*discount;
				if(et.eligibilityValue < this.minEligibityForUpdate){
					deletedSet.add(weightId);
				}
				
			}
			
			//add new traces if need be
			for(FunctionWeight fw : curApprox.approximationResult.functionWeights){
				
				int weightId = fw.weightId();
				if(!traces.containsKey(fw)){
					
					//then it's new and we need to add it
					EligibilityTraceVector et = new EligibilityTraceVector(fw, gradient.getPartialDerivative(weightId));
					double newWeight = fw.weightValue() + learningRate*delta*et.eligibilityValue;
					fw.setWeight(newWeight);
					
					double deltaW = Math.abs(et.initialWeightValue - newWeight);
					if(deltaW > maxWeightChangeInLastEpisode){
						maxWeightChangeInLastEpisode = deltaW;
					}
					
					et.eligibilityValue *= this.lambda*discount;
					if(et.eligibilityValue >= this.minEligibityForUpdate){
						traces.put(weightId, et);
					}
					
				}
				
			}
			
			//delete any traces
			for(Integer t : deletedSet){
				traces.remove(t);
			}
			
			
			//move on
			curState = nextState;
			action = nextAction;
			curApprox = nextApprox;
			allCurApproxResults = allNextApproxResults;
			
		}
		
		if(episodeHistory.size() >= numEpisodesToStore){
			episodeHistory.poll();
			episodeHistory.offer(ea);
		}
		
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
	public List<QValue> getQs(State s) {
		List<GroundedAction> gas = this.getAllGroundedActions(s);
		List <QValue> qs = new ArrayList<QValue>(gas.size());
		
		
		List<ActionApproximationResult> results = vfa.getStateActionValues(s, gas);
		for(GroundedAction ga : gas){
			qs.add(this.getQFromFeaturesFor(results, s, ga));
		}
		
		return qs;
	}

	@Override
	public QValue getQ(State s, GroundedAction a) {
		
		List <GroundedAction> gaList = new ArrayList<GroundedAction>(1);
		gaList.add(a);
		
		List<ActionApproximationResult> results = vfa.getStateActionValues(s, gaList);
		
		return this.getQFromFeaturesFor(results, s, a);
	}
	
	
	/**
	 * Creates a Q-value object in which the Q-value is determined from VFA.
	 * @param results the VFA prediction results for each action.
	 * @param s the state of the Q-value
	 * @param ga the action taken
	 * @return a Q-value object in which the Q-value is determined from VFA.
	 */
	protected QValue getQFromFeaturesFor(List<ActionApproximationResult> results, State s, GroundedAction ga){
		
		ActionApproximationResult result = ActionApproximationResult.extractApproximationForAction(results, ga);
		QValue q = new QValue(s, ga, result.approximationResult.predictedValue);
		
		return q;
	}
	
	
	/**
	 * Gets all Q-value VFA results for each action for a given state
	 * @param s the state for which the Q-Value VFA results should be returned.
	 * @return all Q-value VFA results for each action for a given state
	 */
	protected List <ActionApproximationResult> getAllActionApproximations(State s){
		List<GroundedAction> gas = this.getAllGroundedActions(s);
		return this.vfa.getStateActionValues(s, gas);
	}
	
	
	/**
	 * Returns the VFA Q-value approximation for the given state and action.
	 * @param s the state for which the VFA result should be returned
	 * @param ga the action for which the VFA result should be returned
	 * @return the VFA Q-value approximation for the given state and action.
	 */
	protected ActionApproximationResult getActionApproximation(State s, GroundedAction ga){
		List <GroundedAction> gaList = new ArrayList<GroundedAction>(1);
		gaList.add(ga);
		
		List<ActionApproximationResult> results = vfa.getStateActionValues(s, gaList);
		
		return ActionApproximationResult.extractApproximationForAction(results, ga);
	}

	@Override
	public void planFromState(State initialState) {
		
		int eCount = 0;
		do{
			this.runLearningEpisodeFrom(initialState);
			eCount++;
		}while(eCount < numEpisodesForPlanning && maxWeightChangeInLastEpisode > maxWeightChangeForPlanningTermination);

	}
	
	
	
	
	/**
	 * An object for keeping track of the eligibility traces within an episode for each VFA weight
	 * @author James MacGlashan
	 *
	 */
	public static class EligibilityTraceVector{
		
		/**
		 * The VFA weight being traced
		 */
		public FunctionWeight		weight;
		
		/**
		 * The eligibility value
		 */
		public double				eligibilityValue;
		
		/**
		 * The value of the weight when the trace started
		 */
		public double				initialWeightValue;
		
		
		/**
		 * Creates a trace for the given weight with the given eligibility value
		 * @param weight the VFA weight
		 * @param eligibilityValue the eligibility to assign to it.
		 */
		public EligibilityTraceVector(FunctionWeight weight, double eligibilityValue){
			this.weight = weight;
			this.eligibilityValue = eligibilityValue;
			this.initialWeightValue = weight.weightValue();
		}
		
	}

}
