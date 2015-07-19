package burlap.behavior.singleagent.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.planning.StateMapping;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.NullAction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;


/**
 * This is an abstract class to provide support to learning and planning with options [1], which are
 * temporally extended actions. Options may be Markov or non-Markov. An example of a non-Markov
 * option is a macro action whose termination depends on its action history. Because options
 * are subclasses of the {@link burlap.oomdp.singleagent.Action} class, they may be trivally
 * added to any planning or learning algorithm. Some planning and learning algorithms should
 * handle options specially; for instance Q-learning needs to treat the return from options
 * specially. However, the current planning and learning algorithms all handle options in the
 * appropriately special ways so that Options may be used confidently with existing algorithms.
 * <p/>
 * In order for correct value function returns from option executions to be determined,
 * options need to keep track of the cumulative reward and number of steps they've taken
 * since they began execution. This abstract class has data structures and code in place to automatically
 * handle that information so that any subclass of this Option class should "just work." When
 * an option is added to an {@link burlap.behavior.singleagent.planning.OOMDPPlanner} object
 * through the {@link burlap.behavior.singleagent.planning.OOMDPPlanner#addNonDomainReferencedAction(Action)}
 * method, it will automatically tell the Option which reward function and discount factor it should be using
 * to keep track of the cumulative reward.
 * <p/>
 * Note that value function planning algorithms that use the Bellman update (such as value iteration)
 * require the option to return not only the possible terminal states, but the expected number of
 * steps to those terminal states and the expected cumulative reward. By default, this
 * abstract Option class will compute those transition dynamics through a branching
 * exploration of the possible outcomes at each step of execution and save the results
 * so that they do not need to be computed again. If an option is stochastic or if
 * the underlining domain is stochastic, there may be an infinite number of possible outcomes.
 * As a result, the transition dynamics computation will stop searching for states at given
 * horizons that are less than some small probability of occurring (by default set to
 * 0.001). This threshold hold may be modified. However, if these transition dynamics can be specified
 * a priori, it is recommended that the {@link #getTransitions(State, String [])} method is overridden
 * and specified by hand rather than requiring this class to have to enumerate the results. Finally,
 * note that the {@link #getTransitions(State, String [])} returns {@link burlap.oomdp.core.TransitionProbability} 
 * elements, where each {@link burlap.oomdp.core.TransitionProbability} holds the probability of transitioning to a state discounted
 * by the the expected length of time. That is, the probability value in each {@link burlap.oomdp.core.TransitionProbability} is
 * <br/> 
 * \sum_k \gamma^k * p(s, s', k) <br/>
 * where p(s, s', k) is the
 * probability that the option will terminate in s' after being initiated in state s and taking k steps, gamma is the discount
 * factor and s' is the state associated with the probability value in the {@link burlap.oomdp.core.TransitionProbability} object.
 * <p/>
 * 1. Sutton, Richard S., Doina Precup, and Satinder Singh. "Between MDPs and semi-MDPs: A framework for temporal abstraction 
 * in reinforcement learning." Artificial intelligence 112.1 (1999): 181-211.
 * @author James MacGlashan
 *
 */
public abstract class Option extends Action {

	/*
	 * Note for further development: will the reward tracker work for 3-level hierarchical options with discounting?
	 * I think not; they will only work if the one step hierarchical action selection
	 * only returns primitives, in which case I will have to implement
	 * that for any hierarchical subclasses 
	 * 
	 */
	
	
	/**
	 * Random object for following stochastic option policies
	 */
	protected Random 												rand;
	
	
	/**
	 * Stores the last execution results of an option from the initiation state to the state in which it terminated
	 */
	protected EpisodeAnalysis										lastOptionExecutionResults;
	
	/**
	 * Boolean indicating whether the last option execution result should be saved
	 */
	protected boolean												shouldRecordResults;
	
	/**
	 * Boolean indicating whether the last option execution recording annotates the selected actions with this option's name
	 */
	protected boolean												shouldAnnotateExecution;
	
	//////////////////////////////////////////////////////////////
	//variables for keeping track of reward from execution
	//////////////////////////////////////////////////////////////
	
	
	/**
	 * reward function for keeping track of the cumulative reward during an execution
	 */
	protected RewardFunction 										rf;
	
	/**
	 * boolean indicating whether the cumulative reward during execution should be recorded
	 */
	protected boolean 												keepTrackOfReward;
	
	/**
	 * discount factor of the MDP in which this option will be applied
	 */
	protected double 												discountFactor;
	
	/**
	 * the cumulative reward received during the last execution of this option
	 */
	protected double 												lastCumulativeReward;
	
	/**
	 * How much to discount the reward in the next option step
	 */
	protected double												cumulativeDiscount;
	
	/**
	 * How many steps were taken in the options last execution
	 */
	protected int													lastNumSteps;
	
	/**
	 * the terminal function of the MDP in which this option is to be executed. Will cause an option to prematurely end
	 * if ever reached.
	 */
	protected TerminalFunction										externalTerminalFunction;
	
	
	//////////////////////////////////////////////////////////////
	//variables required for planners that use full bellman updates
	//////////////////////////////////////////////////////////////
	
	
	
	/**
	 * State hash factory used to cache the transition probabilities so that they only need to be computed once for each state
	 */
	protected StateHashFactory										expectationStateHashingFactory;
	
	/**
	 * The cached transition probabilities from each initiation state
	 */
	protected Map<StateHashTuple, List <TransitionProbability>> 	cachedExpectations;
	
	/**
	 * The cached expected reward from each initiation state
	 */
	protected Map<StateHashTuple, Double>							cachedExpectedRewards;
	
	
	/**
	 * The minimum probability a possible terminal state being reached to be included in the computed transition dynamics
	 */
	protected double												expectationSearchCutoffProb = 0.001;
	
	
	/**
	 * An option state mapping to use to map from a source MDP state representation to a representation that this option will use
	 * for action selection.
	 */
	protected StateMapping											stateMapping;
	
	
	/**
	 * An optional mapping from initiation states to terminal states so that the execution of an option does not need to be simulated.
	 * This can only be used in special circumstances. See the {@link DirectOptionTerminateMapper} class documentation for more
	 * information.
	 */
	protected DirectOptionTerminateMapper							terminateMapper;
	
	
	/**
	 * Returns whether this option is Markov or not; that is, whether action selection and termination only depends on the current state.
	 * @return True if this option is Markov ; false otherwise.
	 */
	public abstract boolean isMarkov();
	
	/**
	 * Returns whether this option's termination conditions are deterministic or stochastic
	 * @return true if this option's termination conditions are deterministic; false if stochastic.
	 */
	public abstract boolean usesDeterministicTermination();
	
	/**
	 * Returns whether this option's policy is deterministic or stochastic
	 * @return true if this option's policy is deterministic; false if stochastic
	 */
	public abstract boolean usesDeterministicPolicy();
	
	/**
	 * Returns the probability that this option (executed with the given parameters) will terminate in the given state
	 * @param s the state to test for termination
	 * @param params any parameters that were applied to this option when it was initiated
	 * @return the probability that this option (executed with the given parameters) will terminate in the given state
	 */
	public abstract double probabilityOfTermination(State s, String [] params);
	
	/**
	 * This method is always called when an option is initated and begins execution. Specifically, it is called from the {@link #performActionHelper(burlap.oomdp.core.State, String[])}
	 * For Markov options, this method probably does not need to do anything, but for non-Markov options, like Macro actions, it may need
	 * to initialize some structures for determining termination and action selection.
	 * @param s the state in which the option was initiated
	 * @param params the parameters that were passed to the option for execution
	 */
	public abstract void initiateInStateHelper(State s, String [] params);
	
	
	/**
	 * This method causes the option to select a single step in the given state, when the option was initiated with the provided parameters.
	 * This method will be called by the {@link #performActionHelper(burlap.oomdp.core.State, String[])}  method until it is determined that the option terminates.
	 * @param s the state in which an action should be selected.
	 * @param params the parameters that were passed to the option when it was initiated
	 * @return the action the option has selected to take in State <code>s</code>
	 */
	public abstract GroundedAction oneStepActionSelection(State s, String [] params);
	
	
	/**
	 * Returns the option's policy distribution for a given state. This method is primarily used by the methods for computing transition dynamics.
	 * Note that if this is a non-Markov option, the returned distribution should be with respect to the state in which the option was
	 * executed and any previous actions it took that influence behavior.
	 * @param s the state for which this option's policy distribution should be returned
	 * @param params the parameters that were passed to the option when it was initiated
	 * @return this options policy distribution for the given state.
	 */
	public abstract List<ActionProb> getActionDistributionForState(State s, String [] params);
	
	
	/**
	 * Initializes an option without a name and parameters. These values should be filled in by other means
	 * otherwise the option may not work.
	 */
	public Option(){
		this.init();
	}
	
	
	/**
	 * Initializes.
	 * @param name the name of the option (should be unique from other options and actions a planning/learning algorithm can use)
	 * @param domain a domain with which this option is associated; note that this option will *not* be added to domain's list of actions like a normal action.
	 * @param parameterClasses the object classes of required parameters for the option in a comma delimited form.
	 */
	public Option(String name, Domain domain, String parameterClasses) {
		super(name, domain, parameterClasses);
		this.init();
	}


	/**
	 * Initializes.
	 * @param name the name of the option (should be unique from other options and actions a planning/learning algorithm can use)
	 * @param domain a domain with which this option is associated; note that this option will *not* be added to domain's list of actions like a normal action.
	 * @param parameterClasses the object classes of required parameters for the option
	 */
	public Option(String name, Domain domain, String [] parameterClasses){
		super(name, domain, parameterClasses);
		this.init();
	}
	
	
	/**
	 * Initializes.
	 * @param name the name of the option (should be unique from other options and actions a planning/learning algorithm can use)
	 * @param domain a domain with which this option is associated; note that this option will *not* be added to domain's list of actions like a normal action.
	 * @param parameterClasses the object classes of required parameters for the option
	 * @param parameterOrderGroups the parameter order group assignments.
	 */
	public Option(String name, Domain domain, String [] parameterClasses, String [] parameterOrderGroups){
		super(name, domain, parameterClasses, parameterOrderGroups);
		this.init();
	}
	
	private void init(){
		rand = new Random();
		rf = null;
		keepTrackOfReward = false;
		discountFactor = 1.;
		lastCumulativeReward = 0.;
		cumulativeDiscount = 1.;
		lastNumSteps = 0;
		stateMapping = null;
		terminateMapper = null;
		externalTerminalFunction = new NullTermination();
		shouldRecordResults = true;
		shouldAnnotateExecution = true;
	}
	
	
	/**
	 * Sets the option to use the provided hashing factory for caching transition probability results.
	 * @param hashingFactory the state hashing factory to use.
	 */
	public void setExpectationHashingFactory(StateHashFactory hashingFactory){
		this.expectationStateHashingFactory = hashingFactory;
		this.cachedExpectations = new HashMap<StateHashTuple, List<TransitionProbability>>();
		this.cachedExpectedRewards = new HashMap<StateHashTuple, Double>();
	}
	
	
	/**
	 * Sets the minimum probability of reaching a terminal state for it to be included in the options computed transition dynamics distribution.
	 * @param cutoff the minimum probability of reaching a terminal state for it to be included in the options computed transition dynamics distribution.
	 */
	public void setExpectationCalculationProbabilityCutoff(double cutoff){
		this.expectationSearchCutoffProb = cutoff;
	}
	
	
	/**
	 * Change whether the options last execution will be recorded or not.
	 * @param toggle true if the last option execution should be saved; false otherwise.
	 */
	public void toggleShouldRecordResults(boolean toggle){
		this.shouldRecordResults = toggle;
	}
	
	
	/**
	 * Toggle whether the last recorded option execution will annotate the actions taken with this option's name
	 * @param toggle true if the last recorded option execution will annotate the actions taken with this option's name; false otherwise
	 */
	public void toggleShouldAnnotateResults(boolean toggle){
		this.shouldAnnotateExecution = toggle;
	}
	
	/**
	 * Returns whether this option is recording its executions 
	 * @return true if this option is recording its executions; false otherwise.
	 */
	public boolean isRecordingExecutionResults(){
		return shouldRecordResults;
	}
	
	
	/**
	 * Returns whether this option is annotating recorded action executions with this option's name.
	 * @return true if this  option is annotating recorded action executions with this option's name; false otherwise.
	 */
	public boolean isAnnotatingExecutionResults(){
		return shouldAnnotateExecution;
	}
	
	
	/**
	 * Returns the events from this option's last execution
	 * @return the events from this option's last execution
	 */
	public EpisodeAnalysis getLastExecutionResults(){
		return lastOptionExecutionResults;
	}
	
	
	/**
	 * Sets this option to use a state mapping that maps from the source MDP states to another state representation that will be used by this option for making
	 * action selections.
	 * @param m the state mapping to use.
	 */
	public void setStateMapping(StateMapping m){
		this.stateMapping = m;
	}
	
	
	/**
	 * Sets this option to determine its execution results using a direct terminal state mapping rather than actually executing each action selcted
	 * by the option step by step. A method like this should only be used under specific circumstances. See the {@link DirectOptionTerminateMapper}
	 * class documentation for more information.
	 * @param tm the direct state to terminal state mapping to use.
	 */
	public void setTerminateMapper(DirectOptionTerminateMapper tm){
		this.terminateMapper = tm;
	}
	
	
	/**
	 * Sets what the external MDPs terminal function is that will cause this option to terminate if it enters those terminal states.
	 * @param tf the external MDPs terminal function is
	 */
	public void setExernalTermination(TerminalFunction tf){
		if(tf == null){
			this.externalTerminalFunction = new NullTermination();
		}
		else{
			this.externalTerminalFunction = tf;
		}
	}
	
	
	/**
	 * Returns the state that is mapped from the input state. If this option does not using a state mapping, then the input state is returned.
	 * @param s the input state from which a mapped state is to be returned.
	 * @return the state that is mapped from the input state.
	 */
	protected State map(State s){
		if(stateMapping == null){
			return s;
		}
		return stateMapping.mapState(s);
	}
	
	
	/**
	 * Tells this option to keep track the cumulative reward from its execution using the given reward function and the given discount factor.
	 * @param rf the reward function to use
	 * @param discount the discount factor to use
	 */
	public void keepTrackOfRewardWith(RewardFunction rf, double discount){
		this.keepTrackOfReward = true;
		this.rf = rf;
		this.discountFactor = discount;
	}
	
	
	/**
	 * Overrides action superclass init because options will not be added to the associated domains list of actions.
	 */
	@Override
	public void init(String name, Domain domain, String [] parameterClasses, String [] replacedClassNames){
		
		this.name = name;
		this.domain = domain;
		this.parameterClasses = parameterClasses;
		this.parameterOrderGroup = replacedClassNames;
		
	}
	
	
	/**
	 * Returns the cumulative discounted reward received in last execution of this option.
	 * @return the cumulative discounted reward received in last execution of this option.
	 */
	public double getLastCumulativeReward(){
		return this.lastCumulativeReward;
	}
	
	
	/**
	 * Returns the number of steps taken in the last execution of this option.
	 * @return the number of steps taken in the last execution of this option.
	 */
	public int getLastNumSteps(){
		return this.lastNumSteps;
	}
	

	@Override
	public boolean isPrimitive(){
		return false;
	}
	
	
	/**
	 * Tells the option that it is being initiated in the given state with the given parameters. Will set auxiliary data such as the cumulative reward
	 * received in the last execution to 0 since the option is about to be executed again. The {@link #initiateInStateHelper(State, String[])}
	 * method will be called before exiting.
	 * @param s the state in which the option is being initiated.
	 * @param params the parameters passed to the option 
	 */
	public void initiateInState(State s, String [] params){
		lastCumulativeReward = 0.;
		cumulativeDiscount = 1.;
		lastNumSteps = 0;
		lastOptionExecutionResults = new EpisodeAnalysis(s);
		this.initiateInStateHelper(s, params);
	}
	
	
	@Override
	protected State performActionHelper(State st, String[] params){
		
		if(terminateMapper != null){
			State ns = terminateMapper.generateOptionTerminalState(st);
			lastNumSteps = terminateMapper.getNumSteps(st, ns);
			lastCumulativeReward = terminateMapper.getCumulativeReward(st, ns, rf, discountFactor);
			return ns;
		}
		
		State curState = st;
		
		this.initiateInState(curState, params);
		
		do{
			curState = this.oneStep(curState, params);
		}while(this.continueFromState(curState, params) && !externalTerminalFunction.isTerminal(curState));
		
		
		
		return curState;
	}


	@Override
	public EnvironmentOutcome performInEnvironment(Environment env, String[] params) {

		State initialState = env.getCurState();
		this.initiateInState(initialState, params);
		do{
			this.oneStep(env, params);
		}while(this.continueFromState(env.getCurState(), params) && !env.curStateIsTerminal());

		EnvironmentOptionOutcome eoo = new EnvironmentOptionOutcome(initialState,
																	new GroundedAction(this, params),
																	env.getCurState(),
																	this.lastCumulativeReward,
																	env.curStateIsTerminal(),
																	this.discountFactor,
																	this.lastNumSteps);

		return eoo;
	}

	/**
	 * Performs one step of execution of the option. This method assumes that the {@link #initiateInState(burlap.oomdp.core.State, String[])}
	 * method was called previously for the state in which this option was initiated.
	 * @param s the state in which a single step of the option is to be taken.
	 * @param params the parameters that were passed to the option at initiation
	 * @return the resulting state from a single step of the option being performed.
	 */
	public State oneStep(State s, String [] params){
		GroundedAction ga = this.oneStepActionSelection(s, params);
		State sprime = ga.executeIn(s);
		lastNumSteps++;
		double r = 0.;
		if(keepTrackOfReward){
			r = rf.reward(s, ga, sprime);
			lastCumulativeReward += cumulativeDiscount*r;
			cumulativeDiscount *= discountFactor;
		}
		
		if(shouldRecordResults){
			GroundedAction recordAction = ga;
			if(shouldAnnotateExecution){
				NullAction annotatedPrimitive = new NullAction(this.name + "(" + (lastNumSteps-1) + ")-" + ga.action.getName());
				recordAction = new GroundedAction(annotatedPrimitive, ga.params);
			}
			lastOptionExecutionResults.recordTransitionTo(recordAction, sprime, r);
		}
		
		
		
		return sprime;
	}


	/**
	 * Performs one step of execution of the option in the provided {@link burlap.oomdp.singleagent.environment.Environment}.
	 * This method assuems that the {@link #initiateInState(burlap.oomdp.core.State, String[])} method
	 * was called previously for the state in which this option was initiated.
	 * @param env The {@link burlap.oomdp.singleagent.environment.Environment} in which this option is to be applied
	 * @param params the parameters that were passed to the option at initiation
	 * @return the {@link burlap.oomdp.singleagent.environment.EnvironmentOutcome} of the one step of interaction.
	 */
	public EnvironmentOutcome oneStep(Environment env, String [] params){

		GroundedAction ga = this.oneStepActionSelection(env.getCurState(), params);
		EnvironmentOutcome eo = ga.executeIn(env);
		if(eo instanceof EnvironmentOptionOutcome){
			EnvironmentOptionOutcome eoo = (EnvironmentOptionOutcome)eo;
			lastNumSteps += eoo.numSteps;
			lastCumulativeReward += cumulativeDiscount*eoo.r;
			cumulativeDiscount *= eoo.discount;
		}
		else{
			lastNumSteps++;
			lastCumulativeReward += cumulativeDiscount*eo.r;
			cumulativeDiscount *= discountFactor;
		}

		if(shouldRecordResults){
			GroundedAction recordAction = ga;
			if(shouldAnnotateExecution){
				NullAction annotatedPrimitive = new NullAction(this.name + "(" + (lastNumSteps-1) + ")-" + ga.action.getName());
				recordAction = new GroundedAction(annotatedPrimitive, ga.params);
			}
			lastOptionExecutionResults.recordTransitionTo(recordAction, eo.sp, eo.r);
		}

		return eo;

	}
	
	
	
	/**
	 * This method will use this option's termination probability, roll the dice, and
	 * return whether the option should continue or terminate.
	 * @param s the state to check against
	 * @param params the parameters that were passed to the option at initiation
	 * @return true if this option should continuing executing in s; false if it should terminate.
	 */
	public boolean continueFromState(State s, String [] params){
		double pt = this.probabilityOfTermination(s, params);
		
		//deterministic case needs no random roll
		if(pt == 1.){
			return false;
		}
		else if(pt == 0.){
			return true;
		}
		
		//otherwise need to do a random roll to determine if we terminated here or not
		double roll = rand.nextDouble();
		if(roll < pt){
			return false; //terminate
		}
		
		return true;
		
	}
	
	
	
	/**
	 * Returns the expected reward to be received from initiating this option from state s.
	 * @param s the state in which the option is initiated
	 * @param params the parameters that were passed to the option at initiation
	 * @return the expected reward to be received from initiating this option from state s.
	 */
	public double getExpectedRewards(State s, String [] params){
		StateHashTuple sh = this.expectationStateHashingFactory.hashState(s);
		Double result = this.cachedExpectedRewards.get(sh);
		if(result != null){
			return result;
		}
		this.getTransitions(s, params);
		return this.cachedExpectedRewards.get(sh);
	}
	
	
	@Override
	public List<TransitionProbability> getTransitions(State st, String [] params){
		
		StateHashTuple sh = this.expectationStateHashingFactory.hashState(st);
		
		List <TransitionProbability> result = this.cachedExpectations.get(sh);
		if(result != null){
			return result;
		}
		
		this.initiateInState(st, params);
		
		ExpectationSearchNode esn = new ExpectationSearchNode(st, params);
		Map <StateHashTuple, Double> possibleTerminations = new HashMap<StateHashTuple, Double>();
		double [] expectedReturn = new double[]{0.};
		this.iterateExpectationScan(esn, 1., possibleTerminations, expectedReturn);
		
		this.cachedExpectedRewards.put(sh, expectedReturn[0]);
		
		List <TransitionProbability> transition = new ArrayList<TransitionProbability>();
		for(Map.Entry<StateHashTuple, Double> e : possibleTerminations.entrySet()){
			TransitionProbability tp = new TransitionProbability(e.getKey().s, e.getValue());
			transition.add(tp);
		}
		
		this.cachedExpectations.put(sh, transition);
		
		//State res = this.performAction(st, params);
		//transition.add(new TransitionProbability(res, 1.0));
		
		return transition;
	}
	
	

	/**
	 * This method will recursively determine all possible paths that could occur from execution of the option as well
	 * as the expected return. This method will stop expanding the possible paths when the probability of a state
	 * being reached is less than {@link #expectationSearchCutoffProb}
	 * @param src the source node from which to expand possible paths
	 * @param stackedDiscount the discount amount up to this point
	 * @param possibleTerminations a map of possible termination states and their probability
	 * @param expectedReturn the expected discounted cumulative reward up to node src (this is an array of length 1 that is used to be a mutable double)
	 */
	protected void iterateExpectationScan(ExpectationSearchNode src, double stackedDiscount, 
			Map <StateHashTuple, Double> possibleTerminations, double [] expectedReturn){
		
		
		double probTerm = 0.0; //can never terminate in initiation state
		if(src.nSteps > 0){
			probTerm = this.probabilityOfTermination(src.s, src.optionParams);
		}
		
		double probContinue = 1.-probTerm;
		
		
		//handle possible termination
		if(probTerm > 0.){
			double probOfDiscountedTrajectory = src.probability*stackedDiscount;
			this.accumulateDiscountedProb(possibleTerminations, src.s, probOfDiscountedTrajectory);
			expectedReturn[0] += src.cumulativeDiscountedReward;
		}
		
		//handle continuation
		if(probContinue > 0.){
			
			//handle option policy selection
			List <ActionProb> actionSelction = this.getActionDistributionForState(src.s, src.optionParams);
			for(ActionProb ap : actionSelction){
				
				//now get possible outcomes of each action
				List <TransitionProbability> transitions = ((GroundedAction)ap.ga).action.getTransitions(src.s, src.optionParams);
				for(TransitionProbability tp : transitions){
					double totalTransP = ap.pSelection * tp.p;
					double r = stackedDiscount * this.rf.reward(src.s, (GroundedAction)ap.ga, tp.s);
					ExpectationSearchNode next = new ExpectationSearchNode(src, tp.s, totalTransP, r);
					if(next.probability > this.expectationSearchCutoffProb){
						this.iterateExpectationScan(next, stackedDiscount*discountFactor, possibleTerminations, expectedReturn);
					}
				}
				
			}
			
		}
		
	}
	
	
	/**
	 * Adds to the expected discounted probability of reaching state given a value p, where p = \gamma^k * p(s, s', k), where
	 * s' is a possible terminal state and k is a unique number of steps not yet added to sum over all possible step sizes
	 * to s'.
	 * @param possibleTerminations the map from of all possible termination states to the expected discounted probability of reaching them
	 * @param s a possible termination state
	 * @param p the discounted probability of reaching s for some specific number of steps not already summed into the respective possibleTerminations map. 
	 */
	protected void accumulateDiscountedProb(Map <StateHashTuple, Double> possibleTerminations, State s, double p){
		StateHashTuple sh = expectationStateHashingFactory.hashState(s);
		Double stored = possibleTerminations.get(sh);
		double newP = p;
		if(stored != null){
			newP = stored + p;
		}
		possibleTerminations.put(sh, newP);
	}
	
	
	/**
	 * This method creates a deterministic action selection probability distribution where the deterministic action
	 * to be selected with probability 1 is the one returned by the method {@link #getDeterministicPolicy(State, String[])}.
	 * This method is helpful for quickly defining the action selection distribution for deterministic option policies.
	 * @param s the state for which the action selection distribution should be returned.
	 * @param params the parameters that were passed to the option at initiation
	 * @return a deterministic action selection distribution.
	 */
	protected List <ActionProb> getDeterministicPolicy(State s, String [] params){
		GroundedAction ga = this.oneStepActionSelection(s, params);
		ActionProb ap = new ActionProb(ga, 1.);
		List <ActionProb> aps = new ArrayList<Policy.ActionProb>();
		aps.add(ap);
		return aps;
	}
	
	
	
	
	
	
	
	/**
	 * A search node class used for finding all possible paths of execution an option could take in the world from each initiation state.
	 * @author James MacGlashan
	 *
	 */
	class ExpectationSearchNode{
		
		/**
		 * the state this search node wraps
		 */
		public State s;
		
		/**
		 * The option parameters that were passed to the option upon initiation.
		 */
		public String [] optionParams;
		
		
		/**
		 * the *un*-discounted probability of reaching this search node
		 */
		public double	probability;
		
		/**
		 * The cumulative discounted reward received reaching this node.
		 */
		public double	cumulativeDiscountedReward;
		
		/**
		 * The number of steps taken to reach this node.
		 */
		public int		nSteps;
		
		
		
		/**
		 * Initializes with the probability set to 1, the number of steps 0, and the cumulative discounted reward 0. This constructor
		 * is useful for the initial root node of the option path expansion
		 * @param s the state this search node wraps
		 * @param optionParams The option parameters that were passed to the option upon initiation.
		 */
		public ExpectationSearchNode(State s, String [] optionParams){
			this.s = s;
			this.optionParams = optionParams;
			this.probability = 1.;
			this.cumulativeDiscountedReward = 0.;
			this.nSteps = 0;
		}
		
		
		
		/**
		 * Initializes.
		 * @param src a source parent node from which this node was generated
		 * @param s the state this search node wraps
		 * @param transProb the transition probability of reaching this node from the source node
		 * @param discountedR the discounted reward received from reaching this node from the source node.
		 */
		public ExpectationSearchNode(ExpectationSearchNode src, State s, double transProb, double discountedR){
		
			this.s = s;
			this.optionParams = src.optionParams;
			this.probability = src.probability*transProb;
			this.cumulativeDiscountedReward = src.cumulativeDiscountedReward + discountedR;
			this.nSteps = src.nSteps+1;
			
			
		}
		
	}
	

}
