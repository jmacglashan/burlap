package burlap.behavior.stochasticgames;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.policy.support.ActionProb;
import burlap.datastructures.HashedAggregator;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * This class defines a single agent's policy that is derived from a joint policy. It takes as input a source joint policy
 * and the agent name whose actions in the returned joint actions are being followed. The action distribution for the agent
 * is determined by marginalizing over other agent's actions in the joint policy as if they were selcted indepently. Note that
 * this assumption may not always be correct, depending on the joint policy.
 * <p>
 * When the agent name for this policy is
 * set, it automatically calls the {@link JointPolicy#setTargetAgent(int)} method of the source joint policy with the same
 * agent name.
 * <p>
 * Action selection from the underlying joint policy may also be synchronized with multiple agents who are following the same
 * underlying joint policy object. This has the effect of the joint policy choosing a joint action for each time step and causing
 * each agent to follow that selected joint action. See the {@link JointPolicy} class documentation for more information on how
 * this works. By default, synchronization will not be used.
 * @author James MacGlashan
 *
 */
public class PolicyFromJointPolicy implements EnumerablePolicy {

	/**
	 * The underlying joint policy from which actions are selected.
	 */
	protected JointPolicy					jointPolicy;
	

	protected int actingAgent;
	
	
	protected boolean						synchronizeJointActionSelectionAmongAgents = false;
	
	
	/**
	 * Initializes with the underlying joint polciy
	 * @param jointPolicy the underlying joint polciy
	 */
	public PolicyFromJointPolicy(JointPolicy jointPolicy){
		this.jointPolicy = jointPolicy;
	}
	
	/**
	 * Initializes with the underlying joint polciy and whether actions should be synchronized with other agents following the same underlying joint policy.
	 * @param jointPolicy the underlying joint polciy
	 * @param synchronizeJointActionSelectionAmongAgents whether actions should be synchronized with other agents following the same underlying joint policy.
	 */
	public PolicyFromJointPolicy(JointPolicy jointPolicy, boolean synchronizeJointActionSelectionAmongAgents){
		this.jointPolicy = jointPolicy;
		this.synchronizeJointActionSelectionAmongAgents = synchronizeJointActionSelectionAmongAgents;
	}
	
	
	/**
	 * Initializes with the acting agent name whose actions from the underlying joint policy will be returned.
	 * @param actingAgent the acting agent name
	 * @param jointPolicy the underlying joint polciy
	 */
	public PolicyFromJointPolicy(int actingAgent, JointPolicy jointPolicy){
		this.jointPolicy = jointPolicy;
		this.setActingAgent(actingAgent);
	}
	
	/**
	 * Initializes with the acting agent name whose actions from the underlying joint policy will be returned and 
	 * whether actions should be synchronized with other agents following the same underlying joint policy.
	 * @param actingAgent the acting agent name
	 * @param jointPolicy the underlying joint polciy
	 * @param synchronizeJointActionSelectionAmongAgents whether actions should be synchronized with other agents following the same underlying joint policy.
	 */
	public PolicyFromJointPolicy(int actingAgent, JointPolicy jointPolicy, boolean synchronizeJointActionSelectionAmongAgents){
		this.jointPolicy = jointPolicy;
		this.setActingAgent(actingAgent);
		this.synchronizeJointActionSelectionAmongAgents = synchronizeJointActionSelectionAmongAgents;
	}
	
	
	/**
	 * Sets the underlying joint policy
	 * @param jointPolicy the underlying joint polciy
	 */
	public void setJointPolicy(JointPolicy jointPolicy){
		this.jointPolicy = jointPolicy;
	}
	
	/**
	 * Returns the underlying joint policy
	 * @return the underlying joint policy
	 */
	public JointPolicy getJointPolicy(){
		return this.jointPolicy;
	}
	
	/**
	 * Sets the acting agents name
	 * @param agentNum the acting agent
	 */
	public void setActingAgent(int agentNum){
		this.actingAgent = agentNum;
		this.jointPolicy.setTargetAgent(agentNum);
	}
	
	
	/**
	 * Sets whether actions selection of this agent's policy should be synchronized with the action selection of other agents
	 * following the same underlying joint policy. 
	 * @param synchronizeJointActionSelectionAmongAgents whether agent actions should be synchronized or not.
	 */
	public void setSynchronizeJointActionSelectionAmongAgents(boolean synchronizeJointActionSelectionAmongAgents){
		this.synchronizeJointActionSelectionAmongAgents = synchronizeJointActionSelectionAmongAgents;
	}
	
	/**
	 * Returns the acting agent
	 * @return the acting agent
	 */
	public int getActingAgent(){
		return this.actingAgent;
	}
	
	@Override
	public Action action(State s) {
		if(!this.synchronizeJointActionSelectionAmongAgents){
			return ((JointAction)this.jointPolicy.action(s)).action(this.actingAgent);
		}
		else{
			return this.jointPolicy.getAgentSynchronizedActionSelection(this.actingAgent, s);
		}
	}

	@Override
	public double actionProb(State s, Action a) {
		return PolicyUtils.actionProbFromEnum(this, s, a);
	}

	@Override
	public List<ActionProb> policyDistribution(State s) {

		if(!(jointPolicy instanceof EnumerablePolicy)){
			throw new RuntimeException("Joint policy does not implement EnumerablePolicy, cannot return policy distribution.");
		}

		List<ActionProb> jaProbs = ((EnumerablePolicy)this.jointPolicy).policyDistribution(s);
		HashedAggregator<Action> marginalized = new HashedAggregator<Action>();
		for(ActionProb ap : jaProbs){
			JointAction ja = (JointAction)ap.ga;
			Action thisAgentsAction = ja.action(this.actingAgent);
			marginalized.add(thisAgentsAction, ap.pSelection);
		}
		
		List<ActionProb> finalProbs = new ArrayList<ActionProb>(marginalized.size());
		for(Map.Entry<Action, Double> e : marginalized.entrySet()){
			ActionProb ap = new ActionProb(e.getKey(), e.getValue());
			finalProbs.add(ap);
		}
		
		
		return finalProbs;
	}


	@Override
	public boolean definedFor(State s) {
		return this.jointPolicy.definedFor(s);
	}
	
	
	/**
	 * Returns a copy of this policy, which entails of first making a copy of the joint policy.
	 * @return a copy of this policy.
	 */
	public PolicyFromJointPolicy copy(){
		PolicyFromJointPolicy np = new PolicyFromJointPolicy(this.jointPolicy.copy());
		np.setActingAgent(this.actingAgent);
		return np;
	}
	

}
