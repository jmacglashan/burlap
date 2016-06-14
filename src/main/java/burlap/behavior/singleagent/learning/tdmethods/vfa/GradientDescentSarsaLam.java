package burlap.behavior.singleagent.learning.tdmethods.vfa;

import burlap.behavior.functionapproximation.DifferentiableStateActionValue;
import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.options.EnvironmentOptionOutcome;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.RewardFunction;

import java.util.*;


/**
 * Gradient Descent SARSA(\lambda) implementation [1]. This implementation will work correctly with options [2]. This implementation will work
 * with both linear and non-linear value function approximations by using the gradient value provided to it through the 
 * {@link DifferentiableStateActionValue} implementation provided. <p>
 * The implementation can either be used for learning or planning,
 * the latter of which is performed by running many learning episodes in succession in a {@link burlap.mdp.singleagent.environment.SimulatedEnvironment}.
 * If you are going to use this algorithm for planning, call the {@link #initializeForPlanning(int)}
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
public class GradientDescentSarsaLam extends MDPSolver implements QProvider, LearningAgent, Planner {
	
	
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
	 * Whether to use accumulating or replacing eligibility traces.
	 */
	protected boolean												useReplacingTraces = false;
	
	/**
	 * Whether options should be decomposed into actions in the returned {@link Episode} objects.
	 */
	protected boolean												shouldDecomposeOptions = true;

	
	
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
	public GradientDescentSarsaLam(SADomain domain, double gamma, DifferentiableStateActionValue vfa,
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
	public GradientDescentSarsaLam(SADomain domain, double gamma, DifferentiableStateActionValue vfa,
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
	public GradientDescentSarsaLam(SADomain domain, double gamma, DifferentiableStateActionValue vfa,
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
	protected void GDSLInit(SADomain domain, double gamma, DifferentiableStateActionValue vfa,
							double learningRate, Policy learningPolicy, int maxEpisodeSize, double lambda){
		
		this.solverInit(domain, gamma, null);
		this.vfa = vfa;
		this.learningRate = new ConstantLR(learningRate);
		this.learningPolicy = learningPolicy;
		this.maxEpisodeSize = maxEpisodeSize;
		this.lambda = lambda;

		
		numEpisodesForPlanning = 1;
		maxWeightChangeForPlanningTermination = 0.;

		
	}

	/**
	 * Sets the {@link RewardFunction}, {@link burlap.mdp.core.TerminalFunction},
	 * and the number of simulated episodes to use for planning when
	 * the {@link #planFromState(State)} method is called. If the
	 * {@link RewardFunction} and {@link burlap.mdp.core.TerminalFunction}
	 * are not set, the {@link #planFromState(State)} method will throw a runtime exception.
	 * @param numEpisodesForPlanning the number of simulated episodes to run for planning.
	 */
	public void initializeForPlanning(int numEpisodesForPlanning){
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
	 * @param toggle true to use replacing traces, false to use accumulating traces
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
	}


	@Override
	public Episode runLearningEpisode(Environment env) {
		return this.runLearningEpisode(env, -1);
	}

	@Override
	public Episode runLearningEpisode(Environment env, int maxSteps) {

		State initialState = env.currentObservation();

		Episode ea = new Episode(initialState);
		maxWeightChangeInLastEpisode = 0.;

		State curState = initialState;
		eStepCounter = 0;
		Map <Integer, EligibilityTraceVector> traces = new HashMap<Integer, EligibilityTraceVector>();

		Action action = this.learningPolicy.action(curState);
		while(!env.isInTerminalState() && (eStepCounter < maxSteps || maxSteps == -1)){

			//get Q-value and gradient
			double curQ = this.vfa.evaluate(curState, action);
			FunctionGradient gradient = this.vfa.gradient(curState, action);

			EnvironmentOutcome eo;
			if(!(action instanceof Option)){
				eo = env.executeAction(action);
			}
			else{
				eo = ((Option)action).control(env, this.gamma);
			}
			State nextState = eo.op;

			//determine next Q-value for outcome state
			Action nextAction = this.learningPolicy.action(nextState);
			double nextQV = 0.;
			if(!eo.terminated){
				nextQV = this.vfa.evaluate(nextState, nextAction);
			}

			//manage option specifics
			double r = eo.r;
			double discount = eo instanceof EnvironmentOptionOutcome ? ((EnvironmentOptionOutcome)eo).discount : this.gamma;
			int stepInc = eo instanceof EnvironmentOptionOutcome ? ((EnvironmentOptionOutcome)eo).numSteps() : 1;
			eStepCounter += stepInc;

			if(!(action instanceof Option) || !this.shouldDecomposeOptions){
				ea.transition(action, nextState, r);
			}
			else{
				ea.appendAndMergeEpisodeAnalysis(((EnvironmentOptionOutcome)eo).episode);
			}

			//compute function delta
			double delta = r + (discount*nextQV) - curQ;


			//manage replacing traces by zeroing out features for actions
			//also zero out selected action, since it will be put back in later code
			if(this.useReplacingTraces){
				List<Action> allActions = this.applicableActions(curState);
				for(Action oa : allActions){

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


		return ea;
	}


	@Override
	public List<QValue> qValues(State s) {
		List<Action> gas = this.applicableActions(s);
		List <QValue> qs = new ArrayList<QValue>(gas.size());

		for(Action ga : gas){
			qs.add(new QValue(s, ga, this.vfa.evaluate(s, ga)));
		}
		
		return qs;
	}

	@Override
	public double qValue(State s, Action a) {
		return this.vfa.evaluate(s, a);
	}

	@Override
	public double value(State s) {
		return Helper.maxQ(this, s);
	}
	



	/**
	 * Plans from the input state and then returns a {@link burlap.behavior.policy.GreedyQPolicy} that greedily
	 * selects the action with the highest Q-value and breaks ties uniformly randomly.
	 * @param initialState the initial state of the planning problem
	 * @return a {@link burlap.behavior.policy.GreedyQPolicy}.
	 */
	@Override
	public GreedyQPolicy planFromState(State initialState) {

		if(this.model == null){
			throw new RuntimeException("Planning requires a model, but none is provided.");
		}

		SimulatedEnvironment env = new SimulatedEnvironment(domain, initialState);

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
		 * @param weightValue the initial weight value
		 * @param eligibilityValue the eligibility to assign to it.
		 */
		public EligibilityTraceVector(int weight, double weightValue, double eligibilityValue){
			this.weight = weight;
			this.eligibilityValue = eligibilityValue;
			this.initialWeightValue = weightValue;
		}
		
	}

}
