package burlap.behavior.policy;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.pomdp.BeliefMDPGenerator;

import java.util.List;

/**
 * This policy takes as input a policy for a belief MDP generated with {@link burlap.oomdp.singleagent.pomdp.BeliefMDPGenerator}
 * and when queried for an action, first queries the belief MDP policy and then translates its actions to the corresponding
 * POMDP actions that can be executed in a POMDP domain.
 * <p>
 * Note that this policy is should *not* be used with policies returned by wrapper planning algorithms like {@link burlap.behavior.singleagent.pomdp.wrappedmdpalgs.BeliefSparseSampling},
 * because these algorithms already manage the conversion between belief MDP and POMDP actions. You should only use this policy if you are manually solving a belief MDP
 * with a standard MDP planner.
 *
 * @author James MacGlashan.
 */
public class BeliefPolicyToPOMDPPolicy extends Policy{

	protected Policy beliefPolicy;

	/**
	 * Constructs from the input belief MDP policy that should be converted to returning the POMDP actions.
	 * @param beliefPolicy the source belief MDP action.
	 */
	public BeliefPolicyToPOMDPPolicy(Policy beliefPolicy) {
		this.beliefPolicy = beliefPolicy;
	}

	@Override
	public AbstractGroundedAction getAction(State s) {
		return this.unpackPOMDPAction(this.beliefPolicy.getAction(s));
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		List<ActionProb> aps = this.beliefPolicy.getActionDistributionForState(s);
		for(ActionProb ap : aps){
			ap.ga = this.unpackPOMDPAction(ap.ga);
		}
		return aps;
	}

	@Override
	public boolean isStochastic() {
		return this.beliefPolicy.isStochastic();
	}

	@Override
	public boolean isDefinedFor(State s) {
		return this.beliefPolicy.isDefinedFor(s);
	}

	/**
	 * Unpacks and returns the pomdp action from an input {@link AbstractGroundedAction} that is an instance of
	 * {@link burlap.oomdp.singleagent.pomdp.BeliefMDPGenerator.GroundedBeliefAction}.
	 * @param beliefAction the input {@link burlap.oomdp.singleagent.pomdp.BeliefMDPGenerator.GroundedBeliefAction}.
	 * @return the input belief action's corresponding POMDP action.
	 */
	protected GroundedAction unpackPOMDPAction(AbstractGroundedAction beliefAction){
		return ((BeliefMDPGenerator.GroundedBeliefAction)beliefAction).pomdpAction;
	}
}
