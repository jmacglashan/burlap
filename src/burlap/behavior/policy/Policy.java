package burlap.behavior.policy;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.options.EnvironmentOptionOutcome;
import burlap.behavior.singleagent.options.Option;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.SampleModel;

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
 * {@link Action} if this policy is defined for the
 * input {@link State}. If this {@link Policy} is a stochastic policy,
 * then the {@link #getAction(State)} method should sample an action from its probability distribution
 * and return it.
 * <p>
 * The {@link #getActionDistributionForState(State)} should return this {@link Policy}'s
 * action selection probability distribution for the input {@link State}. The probability distribution is
 * specified by returning a {@link java.util.List} of {@link Policy.ActionProb} instances.
 * An {@link Policy.ActionProb} is a pair consisting of an {@link Action}
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
 * <p>
 * <b>Superclass methods</b><p>
 * This class also has many superclass methods for interacting with policy. These include
 * {@link #getProbOfAction(State, Action)} and
 * {@link #evaluateBehavior(Environment)}
 * (and other variants of evaluateBehavior, such as ones that use a model).
 * <p>
 * The {@link #getProbOfAction(State, Action)} method
 * takes as input a {@link State} and {@link Action} and returns
 * the probability of this {@link Policy} selecting that action. It uses the result of the
 * {@link #getActionDistributionForState(State)} method to determine the full distribution, finds
 * the matching {@link Action} in the returned list, and then returns its assigned probability.
 * It may be possible to return this value in a more efficient way than enumerating the full probability distribution,
 * in which case you may want to consider overriding the method.
 * <p>
 * The {@link #evaluateBehavior(Environment)} and its variants
 * will all evaluate this policy by rolling it out from the input {@link State} or until
 * it reaches a terminal state or executes for the maximum number of steps (depending on which version of the method you use).
 * The resulting behavior will be saved in an {@link Episode} object that is returned.
 * <p>
 * All of the evaluateBehavior methods also know how to work with {@link burlap.behavior.singleagent.options.Option}s.
 * In particular, they also are able to record
 * the option execution in the returned {@link Episode} object in verbose ways
 * for better debugging. By default, when an option is selected in an evaluateBehavior method, each primitive step
 * will be recorded in the {@link Episode} object, rather than only recording that
 * the option was taken. Additionally, in the returned {@link Episode}, each primitive
 * will be annotated with the option that executed it and which step in the option execution that it was.
 * If you would like to disable option decomposition you can do so with the
 * {@link #evaluateMethodsShouldDecomposeOption(boolean)}
 * methods.
 *
 *
 * @author James MacGlashan
 *
 */
public abstract class Policy {

	protected boolean evaluateDecomposesOptions = true;
	
	/**
	 * This method will return an action sampled by the policy for the given state. If the defined policy is
	 * stochastic, then multiple calls to this method for the same state may return different actions. The sampling
	 * should be with respect to defined action distribution that is returned by getActionDistributionForState
	 * @param s the state for which an action should be returned
	 * @return a sample action from the action distribution; null if the policy is undefined for s
	 */
	public abstract Action getAction(State s);
	
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
	public double getProbOfAction(State s, Action ga){
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
	 * Searches the input distribution for the occurrence of the input action and returns its probability.
	 * @param ga the {@link Action} for which its probability in specified distribution should be returned.
	 * @param distribution the probability distribution over actions.
	 * @return the probability of selecting action ga according to the probability specified in distribution.
	 */
	public static double getProbOfActionGivenDistribution(Action ga, List<ActionProb> distribution){
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
		Action ga = this.getAction(s);
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
	 * @return an {@link Action} to take
	 */
	protected Action sampleFromActionDistribution(State s){
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
	 * This method will return the an episode that results from following this policy from state s. The episode will terminate
	 * when the policy reaches a terminal state
	 * @param s the state from which to roll out the policy
	 * @return an EpisodeAnalysis object that records the events from following the policy.
	 */
	public Episode evaluateBehavior(State s, SampleModel model){
		return this.evaluateBehavior(new SimulatedEnvironment(model, s));
	}
	
	
	
	/**
	 * This method will return the an episode that results from following this policy from state s. The episode will terminate
	 * when the policy reaches a terminal state or when the number of steps surpasses maxSteps.
	 * @param s the state from which to roll out the policy
	 * @param model the model from which to same state transitions
	 * @param maxSteps the maximum number of steps to take before terminating the policy rollout.
	 * @return an EpisodeAnalysis object that records the events from following the policy.
	 */
	public Episode evaluateBehavior(State s, SampleModel model, int maxSteps){
		return this.evaluateBehavior(new SimulatedEnvironment(model, s), maxSteps);
	}



	/**
	 * Evaluates this policy in the provided {@link burlap.mdp.singleagent.environment.Environment}. The policy will stop being evaluated once a terminal state
	 * in the environment is reached.
	 * @param env The {@link burlap.mdp.singleagent.environment.Environment} in which this policy is to be evaluated.
	 * @return An {@link Episode} object specifying the interaction with the environment.
	 */
	public Episode evaluateBehavior(Environment env){

		Episode ea = new Episode(env.currentObservation());

		do{
			this.followAndRecordPolicy(env, ea);
		}while(!env.isInTerminalState());

		return ea;
	}

	/**
	 * Evaluates this policy in the provided {@link burlap.mdp.singleagent.environment.Environment}. The policy will stop being evaluated once a terminal state
	 * in the environment is reached or when the provided number of steps has been taken.
	 * @param env The {@link burlap.mdp.singleagent.environment.Environment} in which this policy is to be evaluated.
	 * @param numSteps the maximum number of steps to take in the environment.
	 * @return An {@link Episode} object specifying the interaction with the environment.
	 */
	public Episode evaluateBehavior(Environment env, int numSteps){

		Episode ea = new Episode(env.currentObservation());

		int nSteps;
		do{
			this.followAndRecordPolicy(env, ea);
			nSteps = ea.numTimeSteps();
		}while(!env.isInTerminalState() && nSteps < numSteps);

		return ea;
	}


	/**
	 * Follows this policy for one time step in the provided {@link burlap.mdp.singleagent.environment.Environment} and
	 * records the interaction in the provided {@link Episode} object. If the policy
	 * selects an {@link burlap.behavior.singleagent.options.Option}, then how the option's interaction in the environment
	 * is recorded depends on this object's {@link #evaluateDecomposesOptions} flag.
	 * If {@link #evaluateDecomposesOptions} is false, then the option is recorded as a single action. If it is true, then
	 * the individual primitive actions selected by the environment are recorded.
	 * @param env The {@link burlap.mdp.singleagent.environment.Environment} in which this policy should be followed.
	 * @param ea The {@link Episode} object to which the action selection will be recorded.
	 */
	protected void followAndRecordPolicy(Environment env, Episode ea){


		//follow policy
		Action a = this.getAction(env.currentObservation());
		if(a == null){
			throw new PolicyUndefinedException();
		}


		EnvironmentOutcome eo = env.executeAction(a);


		if(a instanceof Option && evaluateDecomposesOptions){
			ea.appendAndMergeEpisodeAnalysis(((EnvironmentOptionOutcome)eo).episode);
		}
		else{
			ea.recordTransitionTo(a, eo.op, eo.r);
		}

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
		public Action ga;
		
		/**
		 * The probability of the action being selected.
		 */
		public double pSelection;

		public ActionProb() {
		}

		/**
		 * Initializes the action, probability tuple.
		 * @param ga the action to be considered
		 * @param p the probability of the action being selected.
		 */
		public ActionProb(Action ga, double p){
			this.ga = ga;
			this.pSelection = p;
		}

		@Override
		public String toString() {
			return this.pSelection + ": " + ga.toString();
		}
	}


	public static class AnnotatedAction implements Action{
		public Action srcAction;
		public String annotation;


		public AnnotatedAction() {
		}

		public AnnotatedAction(Action srcAction, String annotation) {
			this.srcAction = srcAction;
			this.annotation = annotation;
		}

		@Override
		public String actionName() {
			return srcAction.actionName();
		}

		@Override
		public Action copy() {
			return new AnnotatedAction(srcAction, annotation);
		}


		@Override
		public int hashCode() {
			return srcAction.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(o == null || getClass() != o.getClass()) return false;

			AnnotatedAction that = (AnnotatedAction) o;

			if(srcAction != null ? !srcAction.equals(that.srcAction) : that.srcAction != null) return false;
			return annotation != null ? annotation.equals(that.annotation) : that.annotation == null;

		}

		@Override
		public String toString() {
			return "*" + this.annotation + "--" + this.srcAction.toString();
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
