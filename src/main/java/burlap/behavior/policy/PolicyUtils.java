package burlap.behavior.policy;

import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.policy.support.PolicyUndefinedException;
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
 * @author James MacGlashan.
 */
public class PolicyUtils {

	private PolicyUtils(){

	}

	/**
	 * Indicates whether rollout methods will decompose {@link burlap.behavior.singleagent.options.Option}
	 * selections into the primitive {@link Action} objects they execute and annotate them with the name
	 * of the calling {@link burlap.behavior.singleagent.options.Option} in the returned
	 * {@link Episode}. Default value is true.
	 */
	public static boolean rolloutsDecomposeOptions = true;


	/**
	 * Returns the probability of the policy taking action a in state s by searching for the action
	 * in the returned policy distribution from the provided {@link EnumerablePolicy}.
	 * @param p the {@link EnumerablePolicy}
	 * @param s the state in which the action would be taken
	 * @param a the action being queried
	 * @return the probability of this policy taking action ga in state s
	 */
	public static double actionProbFromEnum(EnumerablePolicy p, State s, Action a){
		List <ActionProb> probs = p.policyDistribution(s);
		if(probs == null || probs.isEmpty()){
			throw new PolicyUndefinedException();
		}
		for(ActionProb ap : probs){
			if(ap.ga.equals(a)){
				return ap.pSelection;
			}
		}
		return 0.;
	}


	/**
	 * Searches the input distribution for the occurrence of the input action and returns its probability.
	 * @param a the {@link Action} for which its probability in specified distribution should be returned.
	 * @param distribution the probability distribution over actions.
	 * @return the probability of selecting action ga according to the probability specified in distribution.
	 */
	public static double actionProbGivenDistribution(Action a, List<ActionProb> distribution){
		if(distribution == null || distribution.isEmpty()){
			throw new RuntimeException("Distribution is null or empty, cannot return probability for given action.");
		}
		for(ActionProb ap : distribution){
			if(ap.ga.equals(a)){
				return ap.pSelection;
			}
		}
		return 0.;
	}

	/**
	 * A helper method for defining deterministic policies. This method relies on the {@link Policy#action(State)} method being
	 * implemented and will return a list of {@link ActionProb} objects with a single instance: the result of
	 * the {@link Policy#action(State)} method with assigned probability 1.
	 * @param p the {@link Policy}
	 * @param s the state for which the action distribution should be returned.
	 * @return a deterministic action distribution for the action returned by the getAction method.
	 */
	public static List <ActionProb> deterministicPolicyDistribution(Policy p, State s){
		Action a = p.action(s);
		if(a == null){
			throw new PolicyUndefinedException();
		}
		ActionProb ap = new ActionProb(a, 1.);
		List <ActionProb> aps = new ArrayList<ActionProb>();
		aps.add(ap);
		return aps;
	}


	/**
	 * This is a helper method for stochastic policies. If the policy is stochastic, then rather than
	 * having the policy define both the {@link Policy#action(State)} method and
	 * {@link EnumerablePolicy#policyDistribution(State)} method,
	 * the objects needs to only define the {@link EnumerablePolicy#policyDistribution(State)} method and
	 * the {@link Policy#action(State)} method can simply
	 * return the result of this method to sample an action.
	 * @param p the {@link EnumerablePolicy}
	 * @param s the input state from which an action should be selected.
	 * @return an {@link Action} to take
	 */
	public static Action sampleFromActionDistribution(EnumerablePolicy p, State s){
		Random rand = RandomFactory.getMapped(0);
		double roll = rand.nextDouble();
		List <ActionProb> probs = p.policyDistribution(s);
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
	 * This method will return the an episode that results from following the given policy from state s. The episode will terminate
	 * when the policy reaches a terminal state.
	 * @param p the {@link Policy} to roll out
	 * @param s the state from which to roll out the policy
	 * @param model the model from which to sample
	 * @return an EpisodeAnalysis object that records the events from following the policy.
	 */
	public static Episode rollout(Policy p, State s, SampleModel model){
		return rollout(p, new SimulatedEnvironment(model, s));
	}



	/**
	 * This method will return the an episode that results from following the given policy from state s. The episode will terminate
	 * when the policy reaches a terminal state or when the number of steps surpasses maxSteps.
	 * @param p the {@link Policy} to roll out
	 * @param s the state from which to roll out the policy
	 * @param model the model from which to same state transitions
	 * @param maxSteps the maximum number of steps to take before terminating the policy rollout.
	 * @return an EpisodeAnalysis object that records the events from following the policy.
	 */
	public static Episode rollout(Policy p, State s, SampleModel model, int maxSteps){
		return rollout(p, new SimulatedEnvironment(model, s), maxSteps);
	}



	/**
	 * Follows the policy in the given {@link burlap.mdp.singleagent.environment.Environment}. The policy will stop being followed once a terminal state
	 * in the environment is reached.
	 * @param p the {@link Policy}
	 * @param env The {@link burlap.mdp.singleagent.environment.Environment} in which this policy is to be evaluated.
	 * @return An {@link Episode} object specifying the interaction with the environment.
	 */
	public static Episode rollout(Policy p, Environment env){

		Episode ea = new Episode(env.currentObservation());

		do{
			followAndRecordPolicy(p, env, ea);
		}while(!env.isInTerminalState());

		return ea;
	}

	/**
	 * Follows the policy in the given {@link burlap.mdp.singleagent.environment.Environment}. The policy will stop being followed once a terminal state
	 * in the environment is reached or when the provided number of steps has been taken.
	 * @param p the {@link Policy}
	 * @param env The {@link burlap.mdp.singleagent.environment.Environment} in which this policy is to be evaluated.
	 * @param numSteps the maximum number of steps to take in the environment.
	 * @return An {@link Episode} object specifying the interaction with the environment.
	 */
	public static Episode rollout(Policy p, Environment env, int numSteps){

		Episode ea = new Episode(env.currentObservation());

		int nSteps;
		do{
			followAndRecordPolicy(p, env, ea);
			nSteps = ea.numTimeSteps();
		}while(!env.isInTerminalState() && nSteps < numSteps);

		return ea;
	}


	/**
	 * Follows this policy for one time step in the provided {@link burlap.mdp.singleagent.environment.Environment} and
	 * records the interaction in the provided {@link Episode} object. If the policy
	 * selects an {@link burlap.behavior.singleagent.options.Option}, then how the option's interaction in the environment
	 * is recorded depends on the {@link #rolloutsDecomposeOptions} flag.
	 * If {@link #rolloutsDecomposeOptions} is false, then the option is recorded as a single action. If it is true, then
	 * the individual primitive actions selected by the environment are recorded.
	 * @param p the {@link Policy}
	 * @param env The {@link burlap.mdp.singleagent.environment.Environment} in which this policy should be followed.
	 * @param ea The {@link Episode} object to which the action selection will be recorded.
	 */
	protected static void followAndRecordPolicy(Policy p, Environment env, Episode ea){


		//follow policy
		Action a = p.action(env.currentObservation());
		if(a == null){
			throw new PolicyUndefinedException();
		}


		EnvironmentOutcome eo = env.executeAction(a);


		if(a instanceof Option && rolloutsDecomposeOptions){
			ea.appendAndMergeEpisodeAnalysis(((EnvironmentOptionOutcome)eo).episode);
		}
		else{
			ea.transition(a, eo.op, eo.r);
		}

	}

}
