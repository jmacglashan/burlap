package burlap.behavior.singleagent.planning.stochastic.policyiteration;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.planning.stochastic.DynamicProgramming;
import burlap.debugtools.DPrint;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class is used to compute the value function under some specified policy. The value function is computed using tabular
 * Value Iteration with the Bellman operator being fixed to the specified policy. After constructing an instance
 * use the {@link #evaluatePolicy(burlap.behavior.policy.Policy, State)} method to evaluate a
 * policy from some initial seed state. You can reuse this class to evaluate different subsequent policies, but doing so
 * will overwrite the value function. If you want to save the value function that was computed for some policy,
 * use the {@link #getCopyOfValueFunction()} method.
 * <p>
 * Alternatively, you can also evaluate a policy with the {@link #evaluatePolicy(burlap.behavior.policy.Policy)} method,
 * but you should have already seeded the state space by having called the {@link #evaluatePolicy(burlap.behavior.policy.Policy, State)}
 * method or the {@link #performReachabilityFrom(State)} method at least once previously,
 * a runtime exception will be thrown.
 *
 * @author James MacGlashan.
 */
public class PolicyEvaluation extends DynamicProgramming {

	/**
	 * When the maximum change in the value function is smaller than this value, policy evaluation will terminate.
	 */
	protected double maxEvalDelta;


	/**
	 * When the maximum number of evaluation iterations passes this number, policy evaluation will terminate
	 */
	protected double maxEvalIterations;


	/**
	 * Initializes.
	 * @param domain the domain on which to evaluate a policy
	 * @param gamma the discount factor
	 * @param hashingFactory the {@link burlap.statehashing.HashableStateFactory} used to index states and perform state equality
	 * @param maxEvalDelta the minimum change in the value function that will cause policy evaluation to terminate
	 * @param maxEvalIterations the maximum number of evaluation iterations to perform before terminating policy evaluation
	 */
	public PolicyEvaluation(SADomain domain, double gamma, HashableStateFactory hashingFactory, double maxEvalDelta, double maxEvalIterations) {
		this.DPPInit(domain, gamma, hashingFactory);
		this.maxEvalDelta = maxEvalDelta;
		this.maxEvalIterations = maxEvalIterations;
	}


	/**
	 * Computes the value function for the given policy after finding all reachable states from seed state s
	 * @param policy The {@link burlap.behavior.policy.Policy} to evaluate
	 * @param s the seed initiate state from which to find all reachable states
	 */
	public void evaluatePolicy(Policy policy, State s){
		this.performReachabilityFrom(s);
		this.evaluatePolicy(policy);
	}


	/**
	 * Computes the value function for the given policy over the states that have been discovered
	 * @param policy the {@link burlap.behavior.policy.Policy} to evaluate
	 */
	public void evaluatePolicy(Policy policy){

		if(this.valueFunction.size() == 0){
			throw new RuntimeException("Cannot evaluate policy, because no states have been expanded. Use the performStateReachability method" +
					"or call the evaluatePolicy method that takes a seed initial state as input.");
		}


		double maxChangeInPolicyEvaluation = Double.NEGATIVE_INFINITY;

		Set <HashableState> states = valueFunction.keySet();

		int i;
		for(i = 0; i < this.maxEvalIterations; i++){

			double delta = 0.;
			for(HashableState sh : states){

				double v = this.value(sh);
				double maxQ = this.performFixedPolicyBellmanUpdateOn(sh, policy);
				delta = Math.max(Math.abs(maxQ - v), delta);

			}

			maxChangeInPolicyEvaluation = Math.max(delta, maxChangeInPolicyEvaluation);

			if(delta < this.maxEvalDelta){
				i++;
				break; //approximated well enough; stop iterating
			}

		}


	}


	/**
	 * This method will find all reachable states that will be used when computing the value function.
	 * This method will not do anything if all reachable states from the input state have been discovered from previous calls to this method.
	 * @param si the source state from which all reachable states will be found
	 * @return true if a reachability analysis had never been performed from this state; false otherwise.
	 */
	public boolean performReachabilityFrom(State si){

		HashableState sih = this.stateHash(si);
		//if this is not a new state and we are not required to perform a new reachability analysis, then this method does not need to do anything.
		if(valueFunction.containsKey(sih)){
			return false; //no need for additional reachability testing
		}

		DPrint.cl(this.debugCode, "Starting reachability analysis");

		//add to the open list
		LinkedList<HashableState> openList = new LinkedList<HashableState>();
		Set<HashableState> openedSet = new HashSet<HashableState>();
		openList.offer(sih);
		openedSet.add(sih);


		while(!openList.isEmpty()){
			HashableState sh = openList.poll();

			//skip this if it's already been expanded
			if(valueFunction.containsKey(sh)){
				continue;
			}


			//do not need to expand from terminal states
			if(model.terminal(sh.s())){
				continue;
			}

			valueFunction.put(sh, this.valueInitializer.value(sh.s()));



			List<Action> actions = this.applicableActions(sh.s());
			for(Action a : actions){
				List<TransitionProb> tps = ((FullModel)model).transitions(sh.s(), a);
				for(TransitionProb tp : tps){
					HashableState tsh = this.stateHash(tp.eo.op);
					if(!openedSet.contains(tsh) && !valueFunction.containsKey(tsh)){
						openedSet.add(tsh);
						openList.offer(tsh);
					}
				}
			}

		}

		DPrint.cl(this.debugCode, "Finished reachability analysis; # states: " + valueFunction.size());


		return true;

	}


}
