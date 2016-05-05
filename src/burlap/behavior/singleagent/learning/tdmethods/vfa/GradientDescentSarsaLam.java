package burlap.behavior.singleagent.learning.tdmethods.vfa;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.support.EnvironmentOptionOutcome;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.vfa.DifferentiableStateActionValue;
import burlap.behavior.singleagent.vfa.FunctionGradient;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.AbstractGroundedAction;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.Action;
import burlap.mdp.singleagent.GroundedAction;
import burlap.mdp.singleagent.RewardFunction;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;

import java.util.*;


/**
 * Gradient Descent SARSA(\lambda) implementation [1]. This implementation will work correctly with options [2]. This implementation will work
 * with both linear and non-linear value function approximations by using the gradient value provided to it through the 
 * {@link burlap.behavior.singleagent.vfa.DifferentiableStateActionValue} implementation provided. <p>
 * The implementation can either be used for learning or planning,
 * the latter of which is performed by running many learning episodes in succession in a {@link burlap.mdp.singleagent.environment.SimulatedEnvironment}.
 * If you are going to use this algorithm for planning, call the {@link #initializeForPlanning(burlap.mdp.singleagent.RewardFunction, burlap.mdp.core.TerminalFunction, int)}
 * method before calling {@link #planFromState(State)}. The number of episodes used for planning can be determined
 * by a threshold maximum number of episodes, or by a maximum change in the VFA weight threshold.
 * <p>
 * By default, this agent will use an epsilon-greedy policy with epsilon=0.1. You can change the learning policy to
 * anything with the {@link #setLearningPolicy(burlap.behavior.policy.Policy)} policy.
 * <p>
 * If you
 * want to use a custom learning rate decay schedule rather than a constant learning rate, use the
 * {@link #setLearningRate(burlap.behavior.learningrate.LearningRate)}.
 * <p>
 * @author James MacGlashan
 * 
 * <p>
 * 1. Rummery, Gavin A., and Mahesan Niranjan. On-line Q-learning using connectionist systems. University of Cambridge, Department of Engineering, 1994. <p>
 * 2. 2. Sutton, Richard S., Doina Precup, and Satinder Singh. "Between MDPs and semi-MDPs: A framework for temporal abstraction in reinforcement learning." Artificial intelligence 112.1 (1999): 181-211.
 *
 */
public class GradientDescentSarsaLam extends MDPSolver implements QFunction, LearningAgent, Planner {
	
	
	/**
	 * The object that performs value function approximation
	 */
	protected DifferentiableStateActionValue 						vfa;
	
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
	protected double												maxWeightChangeInLastEpisode = Double.POSITIVE_INFINITY;
	
	
	/**
	 * Whether the learning rate polls should be based on the VFA state features or OO-MDP state. If true, then based on feature VFA state features; if false then the OO-MDP state.
	 * Default is to use feature ids.
	 */
	protected boolean												useFeatureWiseLearningRate = true;
	
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
	 * The total number of learning steps performed by this agent.
	 */
	protected int													totalNumberOfSteps = 0;
	
	
	/**
	 * Initializes SARSA(\lambda) with 0.1 epsilon greedy policy and places no limit on the number of steps the 
	 * agent can take in an episode. By default the agent will only save the last learning episode and a call to the {@link #planFromState(State)} method
	 * will cause the valueFunction to use only one episode for planning; this should probably be changed to a much larger value if you plan on using this
	 * algorithm as a planning algorithm.
	 * @param domain the domain in which to learn
	 * @param gamma the discount factor
	 * @param vfa the value function approximation method to use for estimate Q-values
	 * @param learningRate the learning rate
	 * @param lambda specifies the strength of eligibility traces (0 for one step, 1 for full propagation)
	 */
	public GradientDescentSarsaLam(Domain domain, double gamma, DifferentiableStateActionValue vfa,
			double learningRate, double lambda) {
		
		this.GDSLInit(domain, gamma, vfa, learningRate, new EpsilonGreedy(this, 0.1), Integer.MAX_VALUE, lambda);
		
	}
	
	
	/**
	 * Initializes SARSA(\lambda) with 0.1 epsilon greedy policy. By default the agent will only save the last learning episode and a call to the {@link #planFromState(State)} method
	 * will cause the valueFunction to use only one episode for planning; this should probably be changed to a much larger value if you plan on using this
	 * algorithm as a planning algorithm.
	 * @param domain the domain in which to learn
	 * @param gamma the discount factor
	 * @param vfa the value function approximation method to use for estimate Q-values
	 * @param learningRate the learning rate
	 * @param maxEpisodeSize the maximum number of steps the agent will take in an episode before terminating
	 * @param lambda specifies the strength of eligibility traces (0 for one step, 1 for full propagation)
	 */
	public GradientDescentSarsaLam(Domain domain, double gamma, DifferentiableStateActionValue vfa,
			double learningRate, int maxEpisodeSize, double lambda) {
		
		this.GDSLInit(domain, gamma, vfa, learningRate, new EpsilonGreedy(this, 0.1), maxEpisodeSize, lambda);
		
	}
	
	
	/**
	 * Initializes SARSA(\lambda) By default the agent will only save the last learning episode and a call to the {@link #planFromState(State)} method
	 * will cause the valueFunction to use only one episode for planning; this should probably be changed to a much larger value if you plan on using this
	 * algorithm as a planning algorithm.
	 * @param domain the domain in which to learn
	 * @param gamma the discount factor
	 * @param vfa the value function approximation method to use for estimate Q-values
	 * @param learningRate the learning rate
	 * @param learningPolicy the learning policy to follow during a learning episode.
	 * @param maxEpisodeSize the maximum number of steps the agent will take in an episode before terminating
	 * @param lambda specifies the strength of eligibility traces (0 for one step, 1 for full propagation)
	 */
	public GradientDescentSarsaLam(Domain domain, double gamma, DifferentiableStateActionValue vfa,
			double learningRate, Policy learningPolicy, int maxEpisodeSize, double lambda) {
	
		this.GDSLInit(domain, gamma, vfa, learningRate, learningPolicy, maxEpisodeSize, lambda);
	}

	
	/**
	 * Initializes SARSA(\lambda) By default the agent will only save the last learning episode and a call to the {@link #planFromState(State)} method
	 * will cause the valueFunction to use only one episode for planning; this should probably be changed to a much larger value if you plan on using this
	 * algorithm as a planning algorithm.
	 * @param domain the domain in which to learn
	 * @param gamma the discount factor
	 * @param vfa the value function approximation method to use for estimate Q-values
	 * @param learningRate the learning rate
	 * @param learningPolicy the learning policy to follow during a learning episode.
	 * @param maxEpisodeSize the maximum number of steps the agent will take in an episode before terminating
	 * @param lambda specifies the strength of eligibility traces (0 for one step, 1 for full propagation)
	 */
	protected void GDSLInit(Domain domain, double gamma, DifferentiableStateActionValue vfa,
			double learningRate, Policy learningPolicy, int maxEpisodeSize, double lambda){
		
		this.solverInit(domain, null, null, gamma, null);
		this.vfa = vfa;
		this.learningRate = new ConstantLR(learningRate);
		this.learningPolicy = learningPolicy;
		this.maxEpisodeSize = maxEpisodeSize;
		this.lambda = lambda;
		
		numEpisodesToStore = 1;
		episodeHistory = new LinkedList<EpisodeAnalysis>();
		
		numEpisodesForPlanning = 1;
		maxWeightChangeForPlanningTermination = 0.;

		
	}

	/**
	 * Sets the {@link burlap.mdp.singleagent.RewardFunction}, {@link burlap.mdp.core.TerminalFunction},
	 * and the number of simulated episodes to use for planning when
	 * the {@link #planFromState(State)} method is called. If the
	 * {@link burlap.mdp.singleagent.RewardFunction} and {@link burlap.mdp.core.TerminalFunction}
	 * are not set, the {@link #planFromState(State)} method will throw a runtime exception.
	 * @param rf the reward function to use for planning
	 * @param tf the terminal function to use for planning
	 * @param numEpisodesForPlanning the number of simulated episodes to run for planning.
	 */
	public void initializeForPlanning(RewardFunction rf, TerminalFunction tf, int numEpisodesForPlanning){
		this.rf = rf;
		this.tf = tf;
		this.numEpisodesForPlanning = numEpisodesForPlanning;
	}
	
	
	/**
	 * Sets the learning rate function to use.
	 * @param lr the learning rate function to use.
	 */
	public void setLearningRate(LearningRate lr){
		this.learningRate = lr;
	}
	
	/**
	 * Sets whether learning rate polls should be based on the VFA state feature ids, or the OO-MDP state. Default is to use feature ids. 
	 * @param useFeatureWiseLearningRate if true then learning rate polls are based on VFA state feature ids; if false then they are based on the OO-MDP state object.
	 */
	public void setUseFeatureWiseLearningRate(boolean useFeatureWiseLearningRate){
		this.useFeatureWiseLearningRate = useFeatureWiseLearningRate;
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
	 * Sets a max change in the VFA weight threshold that will cause the {@link #planFromState(State)} to stop planning
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
	public EpisodeAnalysis runLearningEpisode(Environment env) {
		return this.runLearningEpisode(env, -1);
	}

	@Override
	public EpisodeAnalysis runLearningEpisode(Environment env, int maxSteps) {

		State initialState = env.getCurrentObservation();

		EpisodeAnalysis ea = new EpisodeAnalysis(initialState);
		maxWeightChangeInLastEpisode = 0.;

		State curState = initialState;
		eStepCounter = 0;
		Map <Integer, EligibilityTraceVector> traces = new HashMap<Integer, EligibilityTraceVector>();

		GroundedAction action = (GroundedAction)this.learningPolicy.getAction(curState);
		while(!env.isInTerminalState() && (eStepCounter < maxSteps || maxSteps == -1)){

			//get Q-value and gradient
			double curQ = this.vfa.evaluate(curState, action);
			FunctionGradient gradient = this.vfa.gradient(curState, action);

			//execute our action choice
			EnvironmentOutcome eo = action.executeIn(env);
			State nextState = eo.op;

			//determine next Q-value for outcome state
			GroundedAction nextAction = (GroundedAction)this.learningPolicy.getAction(nextState);
			double nextQV = 0.;
			if(!eo.terminated){
				nextQV = this.vfa.evaluate(nextState, nextAction);
			}

			//manage option specifics
			double r = eo.r;
			double discount = eo instanceof EnvironmentOptionOutcome ? ((EnvironmentOptionOutcome)eo).discount : this.gamma;
			int stepInc = eo instanceof EnvironmentOptionOutcome ? ((EnvironmentOptionOutcome)eo).numSteps : 1;
			eStepCounter += stepInc;
			if(action.action.isPrimitive() || !this.shouldAnnotateOptions){
				ea.recordTransitionTo(action, nextState, r);
			}
			else{
				ea.appendAndMergeEpisodeAnalysis(((Option)action.action).getLastExecutionResults());
			}

			//compute function delta
			double delta = r + (discount*nextQV) - curQ;


			//manage replacing traces by zeroing out features for actions
			//also zero out selected action, since it will be put back in later code
			if(this.useReplacingTraces){
				List<GroundedAction> allActions = this.getAllGroundedActions(curState);
				for(GroundedAction oa : allActions){

					//get non-zero parameters and zero them
					this.vfa.evaluate(curState, oa);
					FunctionGradient ofg = this.vfa.gradient(curState, oa);
					for(FunctionGradient.PartialDerivative pds : ofg.getNonZeroPartialDerivatives()){
						EligibilityTraceVector et = traces.get(pds.parameterId);
						if(et != null){
							et.eligibilityValue = 0.;
						}
						else{
							//no trace for this yet, so add it
							et = new EligibilityTraceVector(pds.parameterId, this.vfa.getParameter(pds.parameterId), 0.);
							traces.put(pds.parameterId, et);
						}
					}

				}
			}
			else{
				//if not using replacing traces, then add any new parameters whose traces need to be set, but set initially
				//at zero since it will be updated in the next loop
				for(FunctionGradient.PartialDerivative pds : gradient.getNonZeroPartialDerivatives()){
					if(!traces.containsKey(pds.parameterId)){
						traces.put(pds.parameterId, new EligibilityTraceVector(pds.parameterId, this.vfa.getParameter(pds.parameterId), 0.));
					}
				}

			}


			//scan through trace elements, update them, and update parameter
			double learningRate = 0.;
			if(!this.useFeatureWiseLearningRate){
				learningRate = this.learningRate.pollLearningRate(this.totalNumberOfSteps, curState, action);
			}

			Set <Integer> deletedSet = new HashSet<Integer>();
			for(EligibilityTraceVector et : traces.values()){
				if(this.useFeatureWiseLearningRate){
					learningRate = this.learningRate.pollLearningRate(this.totalNumberOfSteps, et.weight);
				}

				et.eligibilityValue += gradient.getPartialDerivative(et.weight);
				double newParam = vfa.getParameter(et.weight) + learningRate * delta * et.eligibilityValue;
				this.vfa.setParameter(et.weight, newParam);

				double deltaW = Math.abs(et.initialWeightValue - newParam);
				if(deltaW > maxWeightChangeInLastEpisode){
					maxWeightChangeInLastEpisode = deltaW;
				}

				//now decay and delete from tracking if too small
				et.eligibilityValue *= this.lambda*discount;
				if(et.eligibilityValue < this.minEligibityForUpdate){
					deletedSet.add(et.weight);
				}


			}

			//delete traces marked for deletion
			for(Integer t : deletedSet){
				traces.remove(t);
			}

			//move on
			curState = nextState;
			action = nextAction;

			this.totalNumberOfSteps++;


		}

		if(episodeHistory.size() >= numEpisodesToStore){
			episodeHistory.poll();
			episodeHistory.offer(ea);
		}

		return ea;
	}


	public EpisodeAnalysis getLastLearningEpisode() {
		return episodeHistory.getLast();
	}

	public void setNumEpisodesToStore(int numEps) {
		if(numEps > 0){
			numEpisodesToStore = numEps;
		}
		else{
			numEpisodesToStore = 1;
		}
	}

	public List<EpisodeAnalysis> getAllStoredLearningEpisodes() {
		return episodeHistory;
	}

	@Override
	public List<QValue> getQs(State s) {
		List<GroundedAction> gas = this.getAllGroundedActions(s);
		List <QValue> qs = new ArrayList<QValue>(gas.size());

		for(GroundedAction ga : gas){
			qs.add(new QValue(s, ga, this.vfa.evaluate(s, ga)));
		}
		
		return qs;
	}

	@Override
	public QValue getQ(State s, AbstractGroundedAction a) {
		return new QValue(s, a, this.vfa.evaluate(s, a));
	}

	@Override
	public double value(State s) {
		return QFunction.QFunctionHelper.getOptimalValue(this, s);
	}
	



	/**
	 * Plans from the input state and then returns a {@link burlap.behavior.policy.GreedyQPolicy} that greedily
	 * selects the action with the highest Q-value and breaks ties uniformly randomly.
	 * @param initialState the initial state of the planning problem
	 * @return a {@link burlap.behavior.policy.GreedyQPolicy}.
	 */
	@Override
	public GreedyQPolicy planFromState(State initialState) {

		if(this.rf == null || this.tf == null){
			throw new RuntimeException("QLearning (and its subclasses) cannot execute planFromState because the reward function and terminal function for planning have not been set. Use the initializeForPlanning method to set them.");
		}

		SimulatedEnvironment env = new SimulatedEnvironment(domain, rf, tf, initialState);

		int eCount = 0;
		do{
			this.runLearningEpisode(env);
			eCount++;
		}while(eCount < numEpisodesForPlanning && maxWeightChangeInLastEpisode > maxWeightChangeForPlanningTermination);

		return new GreedyQPolicy(this);

	}
	
	@Override
	public void resetSolver(){
		this.vfa.resetParameters();
		this.eStepCounter = 0;
		this.maxWeightChangeInLastEpisode = Double.POSITIVE_INFINITY;
		this.episodeHistory.clear();
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
		public int					weight;
		
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
		public EligibilityTraceVector(int weight, double weightValue, double eligibilityValue){
			this.weight = weight;
			this.eligibilityValue = eligibilityValue;
			this.initialWeightValue = weightValue;
		}
		
	}

}
