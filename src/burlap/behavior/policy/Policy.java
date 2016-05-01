package burlap.behavior.policy;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.options.Option;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.state.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * This abstract class is used to store a policy for a domain that can be queried and perform common operations with the policy.
 * This class provides a number of important methods for working with and defining policies. To implement this class
 * you must implement the methods:
 * {@link #getAction(State)},
 * {@link #getActionDistributionForState(State)},
 * {@link #isStochastic()}, and
 * {@link #isDefinedFor(State)}.
 * <p>
 * The {@link #getAction(State)} should return the action (specified by an
 * {@link AbstractGroundedAction}; e.g., a {@link burlap.oomdp.singleagent.GroundedAction} for
 * single agent domains) this policy defines for the
 * input {@link State}. If this {@link Policy} is a stochastic policy,
 * then the {@link #getAction(State)} method should sample an action from its probability distribution
 * and return it.
 * <p>
 * The {@link #getActionDistributionForState(State)} should return this {@link Policy}'s
 * action selection probability distribution for the input {@link State}. The probability distribution is
 * specified by returning a {@link java.util.List} of {@link Policy.ActionProb} instances.
 * An {@link Policy.ActionProb} is a pair consisting of an {@link AbstractGroundedAction}
 * specifying the action and a double specifying the probability that this {@link Policy} would
 * select that action.
 * <p>
 * The {@link #isStochastic()} method should return true if this {@link Policy} is
 * stochastic and false if it is deterministic.
 * <p>
 * The {@link #isDefinedFor(State)} method should return true if this {@link Policy}
 * is defined for the input {@link State} and false if it is not.
 * <p>
 * This abstract class also has some pre-implemented methods that can be used to help define these required methods. For example,
 * if the {@link #getActionDistributionForState(State)} is implemented and stochastic, then the
 * {@link #getAction(State)} can be trivially implemented by having it return the result of the
 * superclass method {@link #sampleFromActionDistribution(State)}, which will get the probability
 * distribution from the {@link #getActionDistributionForState(State)}, roll a random number
 * and return an action based on the fully define action distribution. Inversely, if the policy is deterministic and
 * the {@link #getAction(State)} is implemented, then the
 * {@link #getActionDistributionForState(State)} method can be trivially implemented by having it
 * return the result of {@link #getDeterministicPolicy(State)}, which will call {@link #getAction(State)}
 * and wrap the result in an {@link Policy.ActionProb} object with assigned probability of 1.0.
 * <p><p>
 * <b>Superclass method</b><p>
 * This class also has many superclass methods for interacting with policy. These include
 * {@link #getProbOfAction(State, AbstractGroundedAction)},
 * {@link #evaluateBehavior(State, burlap.oomdp.singleagent.RewardFunction, burlap.oomdp.core.TerminalFunction)}
 * (and other variants of the method signature), and {@link #evaluateBehavior(burlap.oomdp.singleagent.environment.Environment)} (and
 * other variants of the method signature).
 * <p>
 * The {@link #getProbOfAction(State, AbstractGroundedAction)} method
 * takes as input a {@link State} and {@link AbstractGroundedAction} and returns
 * the probability of this {@link Policy} selecting that action. It uses the result of the
 * {@link #getActionDistributionForState(State)} method to determine the full distribution, finds
 * the matching {@link AbstractGroundedAction} in the returned list, and then returns its assigned probability.
 * It may be possible to return this value in a more efficient way than enumerating the full probability distribution,
 * in which case you may want to consider overriding the method.
 * <p>
 * The {@link #evaluateBehavior(State, burlap.oomdp.singleagent.RewardFunction, burlap.oomdp.core.TerminalFunction)},
 * {@link #evaluateBehavior(State, burlap.oomdp.singleagent.RewardFunction, int)}, and
 * {@link #evaluateBehavior(State, burlap.oomdp.singleagent.RewardFunction, burlap.oomdp.core.TerminalFunction, int)}
 * methods will all evaluate this policy by rolling it out from the input {@link State} or until
 * it reaches a terminal state or executes for the maximum number of steps (depending on which version of the method you use).
 * The resulting behavior will be saved in an {@link burlap.behavior.singleagent.EpisodeAnalysis} object that is returned.
 * Note that this method requires that the returned {@link AbstractGroundedAction} instances are
 * able to be executed using the action's defined transition dynamics. For single agent domains in which the actions
 * are {@link burlap.oomdp.singleagent.GroundedAction} instances, this will work as long as the corresponding
 * {@link burlap.oomdp.singleagent.Action#performAction(State, burlap.oomdp.singleagent.GroundedAction)} method is implemented. If this
 * policy defines the policy for an agent in a stochastic game, returning {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction} instances
 * for the action, then the policy cannot be rolled out since the outcome state would depend on the action selection of
 * other agents.
 * <p>
 * The {@link #evaluateBehavior(burlap.oomdp.singleagent.environment.Environment)} and
 * {@link #evaluateBehavior(burlap.oomdp.singleagent.environment.Environment, int)}
 * methods will execute this policy in some input {@link burlap.oomdp.singleagent.environment.Environment} until either
 * the {@link burlap.oomdp.singleagent.environment.Environment} reaches a terminal state or the maximum number of
 * steps are taken (depending on which method signature is used). This method is useful if a policy was computed
 * with a planning algorithm using some model of the world and then needs to be executed in an environment which may
 * have slightly different transitions; for example, planning a policy for a robot using a model of the world and then
 * executing it on the actual robot by following the policy in an {@link burlap.oomdp.singleagent.environment.Environment}.
 * <p>
 * All of the evaluateBehavior methods also know how to work with {@link burlap.behavior.singleagent.options.Option}s.
 * In particular, they also are able to record
 * the option execution in the returned {@link burlap.behavior.singleagent.EpisodeAnalysis} object in verbose ways
 * for better debugging. By default, when an option is selected in an evaluateBehavior method, each primitive step
 * will be recorded in the {@link burlap.behavior.singleagent.EpisodeAnalysis} object, rather than only recording that
 * the option was taken. Additionally, in the returned {@link burlap.behavior.singleagent.EpisodeAnalysis}, each primitive
 * step by default will be annotated with the option the executed and which step in the option execution that it was.
 * If you would like to disable option decomposition and/or the option annotation, you can do so with the
 * {@link #evaluateMethodsShouldDecomposeOption(boolean)} and {@link #evaluateMethodsShouldAnnotateOptionDecomposition(boolean)}
 * methods.
 *
 *
 * @author James MacGlashan
 *
 */
public abstract class Policy {

	protected boolean evaluateDecomposesOptions = true;
	protected boolean annotateOptionDecomposition = true;
	
	/**
	 * This method will return an action sampled by the policy for the given state. If the defined policy is
	 * stochastic, then multiple calls to this method for the same state may return different actions. The sampling
	 * should be with respect to defined action distribution that is returned by getActionDistributionForState
	 * @param s the state for which an action should be returned
	 * @return a sample action from the action distribution; null if the policy is undefined for s
	 */
	public abstract AbstractGroundedAction getAction(State s);
	
	/**
	 * This method will return action probability distribution defined by the policy. The action distribution is represented
	 * by a list of ActionProb objects, each which specifies a grounded action and a probability of that grounded action being
	 * taken. The returned list does not have to include actions with probability 0.
	 * @param s the state for which an action distribution should be returned
	 * @return a list of possible actions taken by the policy and their probability. 
	 */
	public abstract List<ActionProb> getActionDistributionForState(State s); //returns null when policy is undefined for s
	
	/**
	 * Indicates whether the policy is stochastic or deterministic.
	 * @return true when the policy is stochastic; false when it is deterministic.
	 */
	public abstract boolean isStochastic();


	/**
	 * Specifies whether this policy is defined for the input state.
	 * @param s the input state to test for whether this policy is defined
	 * @return true if this policy is defined for {@link State} s, false otherwise.
	 */
	public abstract boolean isDefinedFor(State s);
	
	/**
	 * Will return the probability of this policy taking action ga in state s
	 * @param s the state in which the action would be taken
	 * @param ga the action being queried
	 * @return the probability of this policy taking action ga in state s
	 */
	public double getProbOfAction(State s, AbstractGroundedAction ga){
		List <ActionProb> probs = this.getActionDistributionForState(s);
		if(probs == null || probs.isEmpty()){
			throw new PolicyUndefinedException();
		}
		for(ActionProb ap : probs){
			if(ap.ga.equals(ga)){
				return ap.pSelection;
			}
		}
		return 0.;
	}
	


	/**
	 * Don't use this, the input state is not necessary; instead use {@link #getProbOfActionGivenDistribution(AbstractGroundedAction, java.util.List)}.
	 */
	@Deprecated
	public static double getProbOfActionGivenDistribution(State s, AbstractGroundedAction ga, List<ActionProb> distribution){
		if(distribution == null || distribution.isEmpty()){
			throw new RuntimeException("Distribution is null or empty, cannot return probability for given action.");
		}
		for(ActionProb ap : distribution){
			if(ap.ga.equals(ga)){
				return ap.pSelection;
			}
		}
		return 0.;
	}

	/**
	 * Searches the input distribution for the occurrence of the input action and returns its probability.
	 * @param ga the {@link AbstractGroundedAction} for which its probability in specified distribution should be returned.
	 * @param distribution the probability distribution over actions.
	 * @return the probability of selecting action ga according to the probability specified in distribution.
	 */
	public static double getProbOfActionGivenDistribution(AbstractGroundedAction ga, List<ActionProb> distribution){
		if(distribution == null || distribution.isEmpty()){
			throw new RuntimeException("Distribution is null or empty, cannot return probability for given action.");
		}
		for(ActionProb ap : distribution){
			if(ap.ga.equals(ga)){
				return ap.pSelection;
			}
		}
		return 0.;
	}
	
	/**
	 * A helper method for defining deterministic policies. This method relies on the getAction method being
	 * implemented and will return a list of ActionProb objects with a single instance: the result of
	 * the getAction method with assigned probability 1. This method simplifies the definition of
	 * deterministic policies because the getActionDistributionForState method can just retunr this method.
	 * @param s the state for which the action distribution should be returned.
	 * @return a deterministic action distribution for the action returned by the getAction method.
	 */
	protected List <ActionProb> getDeterministicPolicy(State s){
		AbstractGroundedAction ga = this.getAction(s);
		if(ga == null){
			throw new PolicyUndefinedException();
		}
		ActionProb ap = new ActionProb(ga, 1.);
		List <ActionProb> aps = new ArrayList<Policy.ActionProb>();
		aps.add(ap);
		return aps;
	}
	
	
	
	/**
	 * This is a helper method for stochastic policies. If the policy is stochastic, then rather than
	 * having the subclass policy define both the {@link #getAction(State)} method and
	 * {@link #getActionDistributionForState(State)} method,
	 * the subclass needs to only define the {@link #getActionDistributionForState(State)} method and
	 * the {@link #getAction(State)} method can simply
	 * call this method to return an action.
	 * @param s the input state from which an action should be selected.
	 * @return an {@link AbstractGroundedAction} to take
	 */
	protected AbstractGroundedAction sampleFromActionDistribution(State s){
		Random rand = RandomFactory.getMapped(0);
		double roll = rand.nextDouble();
		List <ActionProb> probs = this.getActionDistributionForState(s);
		if(probs == null || probs.isEmpty()){
			throw new PolicyUndefinedException();
		}
		double sump = 0.;
		for(ActionProb ap : probs){
			sump += ap.pSelection;
			if(roll < sump){
				return ap.ga;
			}
		}
		
		throw new RuntimeException("Tried to sample policy action distribution, but it did not sum to 1.");
		
	}
	
	
	/**
	 * Sets whether the primitive actions taken during an options will be included as steps in produced EpisodeAnalysis objects.
	 * The default value is true. If this is set to false, then EpisodeAnalysis objects returned from evaluating a policy will record options
	 * as a single "action" and the steps taken by the option will be hidden. 
	 * @param toggle whether to decompose options into the primitive actions taken by them or not.
	 */
	public void evaluateMethodsShouldDecomposeOption(boolean toggle){
		this.evaluateDecomposesOptions = toggle;
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
	public void evaluateMethodsShouldAnnotateOptionDecomposition(boolean toggle){
		this.annotateOptionDecomposition = toggle;
	}
	
	
	/**
	 * This method will return the an episode that results from following this policy from state s. The episode will terminate
	 * when the policy reaches a terminal state defined by tf.
	 * @param s the state from which to roll out the policy
	 * @param rf the reward function used to track rewards accumulated during the episode
	 * @param tf the terminal function defining when the policy should stop being followed.
	 * @return an EpisodeAnalysis object that records the events from following the policy.
	 */
	public EpisodeAnalysis evaluateBehavior(State s, RewardFunction rf, TerminalFunction tf){
		EpisodeAnalysis res = new EpisodeAnalysis();
		res.addState(s); //add initial state
		
		State cur = s;
		while(!tf.isTerminal(cur)){
			cur = this.followAndRecordPolicy(res, cur, rf);
		}
		
		return res;
	}
	
	
	
	/**
	 * This method will return the an episode that results from following this policy from state s. The episode will terminate
	 * when the policy reaches a terminal state defined by tf or when the number of steps surpasses maxSteps.
	 * @param s the state from which to roll out the policy
	 * @param rf the reward function used to track rewards accumulated during the episode
	 * @param tf the terminal function defining when the policy should stop being followed.
	 * @param maxSteps the maximum number of steps to take before terminating the policy rollout.
	 * @return an EpisodeAnalysis object that records the events from following the policy.
	 */
	public EpisodeAnalysis evaluateBehavior(State s, RewardFunction rf, TerminalFunction tf, int maxSteps){
		EpisodeAnalysis res = new EpisodeAnalysis();
		res.addState(s); //add initial state
		
		State cur = s;
		int nSteps = 0;
		while(!tf.isTerminal(cur) && nSteps < maxSteps){
			
			cur = this.followAndRecordPolicy(res, cur, rf);
			
			nSteps = res.numTimeSteps();
			
		}
		
		return res;
	}
	
	/**
	 * This method will return the an episode that results from following this policy from state s. The episode will terminate
	 * when the number of steps taken is &gt;= numSteps.
	 * @param s the state from which to roll out the policy
	 * @param rf the reward function used to track rewards accumulated during the episode
	 * @param numSteps the number of steps to take before terminating the policy rollout
	 * @return an EpisodeAnalysis object that records the events from following the policy.
	 */
	public EpisodeAnalysis evaluateBehavior(State s, RewardFunction rf, int numSteps){
		EpisodeAnalysis res = new EpisodeAnalysis();
		res.addState(s);
		
		State cur = s;
		int nSteps = 0;
		while(nSteps < numSteps){
			
			cur = this.followAndRecordPolicy(res, cur, rf);
			
			nSteps = res.numTimeSteps();
			
		}
		
		return res;
	}


	/**
	 * Evaluates this policy in the provided {@link burlap.oomdp.singleagent.environment.Environment}. The policy will stop being evaluated once a terminal state
	 * in the environment is reached.
	 * @param env The {@link burlap.oomdp.singleagent.environment.Environment} in which this policy is to be evaluated.
	 * @return An {@link burlap.behavior.singleagent.EpisodeAnalysis} object specifying the interaction with the environment.
	 */
	public EpisodeAnalysis evaluateBehavior(Environment env){

		EpisodeAnalysis ea = new EpisodeAnalysis(env.getCurrentObservation());

		do{
			this.followAndRecordPolicy(env, ea);
		}while(!env.isInTerminalState());

		return ea;
	}

	/**
	 * Evaluates this policy in the provided {@link burlap.oomdp.singleagent.environment.Environment}. The policy will stop being evaluated once a terminal state
	 * in the environment is reached or when the provided number of steps has been taken.
	 * @param env The {@link burlap.oomdp.singleagent.environment.Environment} in which this policy is to be evaluated.
	 * @param numSteps the maximum number of steps to take in the environment.
	 * @return An {@link burlap.behavior.singleagent.EpisodeAnalysis} object specifying the interaction with the environment.
	 */
	public EpisodeAnalysis evaluateBehavior(Environment env, int numSteps){

		EpisodeAnalysis ea = new EpisodeAnalysis(env.getCurrentObservation());

		int nSteps;
		do{
			this.followAndRecordPolicy(env, ea);
			nSteps = ea.numTimeSteps();
		}while(!env.isInTerminalState() && nSteps < numSteps);

		return ea;
	}


	/**
	 * Follows this policy for one time step in the provided {@link burlap.oomdp.singleagent.environment.Environment} and
	 * records the interaction in the provided {@link burlap.behavior.singleagent.EpisodeAnalysis} object. If the policy
	 * selects an {@link burlap.behavior.singleagent.options.Option}, then how the option's interaction in the environment
	 * is recorded depends on this object's {@link #evaluateDecomposesOptions} and {@link #annotateOptionDecomposition} flags.
	 * If {@link #evaluateDecomposesOptions} is false, then the option is recorded as a single action. If it is true, then
	 * the individual primitive actions selected by the environment are recorded. If {@link #annotateOptionDecomposition} is
	 * also true, then each primitive action selected but the option is also given a unique name specifying the option
	 * which controlled it and its step in the option's execution.
	 * @param env The {@link burlap.oomdp.singleagent.environment.Environment} in which this policy should be followed.
	 * @param ea The {@link burlap.behavior.singleagent.EpisodeAnalysis} object to which the action selection will be recorded.
	 */
	protected void followAndRecordPolicy(Environment env, EpisodeAnalysis ea){


		//follow policy
		AbstractGroundedAction aga = this.getAction(env.getCurrentObservation());
		if(aga == null){
			throw new PolicyUndefinedException();
		}
		if(!(aga instanceof GroundedAction)){
			throw new RuntimeException("cannot folow policy for non-single agent actions");
		}
		GroundedAction ga = (GroundedAction)aga;

		if(ga.action.isPrimitive()|| !this.evaluateDecomposesOptions){
			EnvironmentOutcome eo = ga.executeIn(env);
			ea.recordTransitionTo(ga, eo.op, eo.r);
		}
		else{
			//then we need to decompose the option
			State cur = env.getCurrentObservation();
			Option o = (Option)ga.action;
			o.initiateInState(cur, ga);
			int ns = 0;
			do{
				//do step of option
				GroundedAction cga = o.oneStepActionSelection(cur, ga);
				EnvironmentOutcome eo = cga.executeIn(env);
				State next = eo.op;
				double r = eo.r;

				if(annotateOptionDecomposition){
					//setup a null action to record the option and primitive action taken
					GroundedAction annotatedPrimitiveGA = new GroundedAnnotatedAction(ga.toString() + "(" + ns + ")", cga);

					//record it
					ea.recordTransitionTo(annotatedPrimitiveGA, next, r);
				}
				else{
					//otherwise just record the primitive that was taken
					ea.recordTransitionTo(cga, next, r);
				}

				cur = env.getCurrentObservation();
				ns++;
			}while(o.continueFromState(cur, ga));
		}

	}


	/**
	 * Follows this policy for one time step from the provided {@link State} and
	 * records the interaction in the provided {@link burlap.behavior.singleagent.EpisodeAnalysis} object. If the policy
	 * selects an {@link burlap.behavior.singleagent.options.Option}, then how the option's interaction in the environment
	 * is recorded depends on this object's {@link #evaluateDecomposesOptions} and {@link #annotateOptionDecomposition} flags.
	 * If {@link #evaluateDecomposesOptions} is false, then the option is recorded as a single action. If it is true, then
	 * the individual primitive actions selected by the environment are recorded. If {@link #annotateOptionDecomposition} is
	 * also true, then each primitive action selected but the option is also given a unique name specifying the option
	 * which controlled it and its step in the option's execution.
	 * @param ea The {@link burlap.behavior.singleagent.EpisodeAnalysis} object to which the action selection will be recorded.
	 * @param cur The {@link State} from which the policy will be followed
	 * @param rf The {@link burlap.oomdp.singleagent.RewardFunction} to keep track of reward
	 * @return the next {@link State} that is a consequence of following this policy for one action selection.
	 */
	protected State followAndRecordPolicy(EpisodeAnalysis ea, State cur, RewardFunction rf){
		
		State next;
		
		//follow policy
		AbstractGroundedAction aga = this.getAction(cur);
		if(aga == null){
			throw new PolicyUndefinedException();
		}
		if(!(aga instanceof GroundedAction)){
			throw new RuntimeException("cannot folow policy for non-single agent actions");
		}
		GroundedAction ga = (GroundedAction)aga;
		
		if(ga.action.isPrimitive() || !this.evaluateDecomposesOptions){
			next = ga.executeIn(cur);
			double r = rf.reward(cur, ga, next);
			
			//record result
			ea.recordTransitionTo(ga, next, r);
		}
		else{
			//then we need to decompose the option
			Option o = (Option)ga.action;
			o.initiateInState(cur, ga);
			int ns = 0;
			do{
				//do step of option
				GroundedAction cga = o.oneStepActionSelection(cur, ga);
				next = cga.executeIn(cur);
				double r = rf.reward(cur, cga, next);
				
				if(annotateOptionDecomposition){
					//setup a null action to record the option and primitive action taken
					GroundedAction annotatedPrimitiveGA = new GroundedAnnotatedAction(ga.toString() + "(" + ns + ")", cga);
					
					//record it
					ea.recordTransitionTo(annotatedPrimitiveGA, next, r);
				}
				else{
					//otherwise just record the primitive that was taken
					ea.recordTransitionTo(cga, next, r);
				}
				
				cur = next;
				ns++;
				
				
			}while(o.continueFromState(cur, ga));
			
		}
		
		//return outcome state
		return next;
	}
	
	
	
	/**
	 * Class for storing an action and probability tuple. The probability represents the probability that the action will be selected.
	 * @author James MacGlashan
	 *
	 */
	public static class ActionProb{
		
		/**
		 * The action to be considered.
		 */
		public AbstractGroundedAction ga;
		
		/**
		 * The probability of the action being selected.
		 */
		public double pSelection;
		
		
		/**
		 * Initializes the action, probability tuple.
		 * @param ga the action to be considered
		 * @param p the probability of the action being selected.
		 */
		public ActionProb(AbstractGroundedAction ga, double p){
			this.ga = ga;
			this.pSelection = p;
		}

		@Override
		public String toString() {
			return this.pSelection + ": " + ga.toString();
		}
	}


	/**
	 * A class for annotating an action selection, specified with a {@link burlap.oomdp.singleagent.GroundedAction}, with a string.
	 * The resulting {@link #toString()} method will produce a string of the following form:<p>
	 * "*annotation--action.toString()" where annotation is the user input annotation and action.toString()
	 * is the result from the input {@link burlap.oomdp.singleagent.GroundedAction} that is being annotated. The
	 * leading * character indicates to {@link burlap.oomdp.singleagent.GroundedAction} serializers (such as
	 * the {@link burlap.behavior.singleagent.EpisodeAnalysis} serialization) that this {@link burlap.oomdp.singleagent.GroundedAction}
	 * is an {@link burlap.behavior.policy.Policy.GroundedAnnotatedAction}.
	 * <p>
	 * All other {@link burlap.oomdp.singleagent.GroundedAction} methods are delegated to the inputted
	 * {@link burlap.oomdp.singleagent.GroundedAction}.
	 */
	public static class GroundedAnnotatedAction extends GroundedAction{

		/**
		 * The string annotation to return in the {@link #toString()} method.
		 */
		public String annotation;

		/**
		 * The {@link burlap.oomdp.singleagent.GroundedAction} delegate to be annotated that handles all
		 * methods except the {@link #toString()} method.
		 */
		public GroundedAction delegate;


		/**
		 * Initializes.
		 * @param annotation the String annotation to be returned by the {@link #toString()} method.
		 * @param delegate the {@link burlap.oomdp.singleagent.GroundedAction} delegate to be annotated.
		 */
		public GroundedAnnotatedAction(String annotation, GroundedAction delegate) {
			super(delegate.action);
			this.annotation = annotation;
			this.delegate = delegate;
		}

		@Override
		public String actionName() {
			return delegate.actionName();
		}

		@Override
		public boolean isParameterized() {
			return delegate.isParameterized();
		}

		@Override
		public void initParamsWithStringRep(String[] params) {
			delegate.initParamsWithStringRep(params);
		}

		@Override
		public String[] getParametersAsString() {
			return delegate.getParametersAsString();
		}

		@Override
		public String toString() {
			return "*" + this.annotation + "--" + this.delegate.toString();
		}

		@Override
		public boolean applicableInState(State s) {
			return delegate.applicableInState(s);
		}

		@Override
		public GroundedAction copy() {
			GroundedAction selCopy = (GroundedAction)this.delegate.copy();
			return new GroundedAnnotatedAction(this.annotation, selCopy);
		}

		@Override
		public EnvironmentOutcome executeIn(Environment env) {
			return delegate.executeIn(env);
		}

		@Override
		public State executeIn(State s) {
			return delegate.executeIn(s);
		}

		@Override
		public List<TransitionProbability> getTransitions(State s) {
			return delegate.getTransitions(s);
		}

		@Override
		public GroundedAction translateParameters(State source, State target) {
			GroundedAction transSel = delegate.translateParameters(source, target);
			return new GroundedAnnotatedAction(this.annotation, transSel);
		}

		@Override
		public int hashCode() {
			return delegate.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			return delegate.equals(other);
		}
	}

	
	/**
	 * RuntimeException to be thrown when a Policy is queried for a state in which the policy is undefined.
	 * @author James MacGlashan
	 *
	 */
	public static class PolicyUndefinedException extends RuntimeException{

		private static final long serialVersionUID = 1L;
		
		public PolicyUndefinedException(){
			super("Policy is undefined for provided state");
		}
		
	}


}
