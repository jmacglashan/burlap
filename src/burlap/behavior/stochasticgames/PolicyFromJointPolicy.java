package burlap.behavior.stochasticgames;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import burlap.behavior.policy.Policy;
import burlap.datastructures.HashedAggregator;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.JointAction;


/**
 * This class defines a single agent's policy that is derived from a joint policy. It takes as input a source joint policy
 * and the agent name whose actions in the returned joint actions are being followed. The action distribution for the agent
 * is determined by marginalizing over other agent's actions in the joint policy as if they were selcted indepently. Note that
 * this assumption may not always be correct, depending on the joint policy.
 * <p>
 * When the agent name for this policy is
 * set, it automatically calls the {@link JointPolicy#setTargetAgent(String)} method of the source joint policy with the same
 * agent name.
 * <p>
 * Action selection from the underlying joint policy may also be synchronized with multiple agents who are following the same
 * underlying joint policy object. This has the effect of the joint policy choosing a joint action for each time step and causing
 * each agent to follow that selected joint action. See the {@link JointPolicy} class documentation for more information on how
 * this works. By default, synchronization will not be used.
 * @author James MacGlashan
 *
 */
public class PolicyFromJointPolicy extends Policy {

	/**
	 * The underlying joint policy from which actions are selected.
	 */
	protected JointPolicy					jointPolicy;
	
	/**
	 * The acting agent's name whose actions from the joint policy will be returned.
	 */
	protected String						actingAgentName;
	
	
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
	 * @param actingAgentName the acting agent name
	 * @param jointPolicy the underlying joint polciy
	 */
	public PolicyFromJointPolicy(String actingAgentName, JointPolicy jointPolicy){
		this.jointPolicy = jointPolicy;
		this.setActingAgentName(actingAgentName);
	}
	
	/**
	 * Initializes with the acting agent name whose actions from the underlying joint policy will be returned and 
	 * whether actions should be synchronized with other agents following the same underlying joint policy.
	 * @param actingAgentName the acting agent name
	 * @param jointPolicy the underlying joint polciy
	 * @param synchronizeJointActionSelectionAmongAgents whether actions should be synchronized with other agents following the same underlying joint policy.
	 */
	public PolicyFromJointPolicy(String actingAgentName, JointPolicy jointPolicy, boolean synchronizeJointActionSelectionAmongAgents){
		this.setActingAgentName(actingAgentName);
		this.jointPolicy = jointPolicy;
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
	 * @param agentName the acting agent's name
	 */
	public void setActingAgentName(String agentName){
		this.actingAgentName = agentName;
		this.jointPolicy.setTargetAgent(agentName);
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
	 * Returns the acting agent's name
	 * @return the acting agent's name
	 */
	public String getActingAgentName(){
		return this.actingAgentName;
	}
	
	@Override
	public AbstractGroundedAction getAction(State s) {
		if(!this.synchronizeJointActionSelectionAmongAgents){
			return ((JointAction)this.jointPolicy.getAction(s)).action(this.actingAgentName);
		}
		else{
			return this.jointPolicy.getAgentSynchronizedActionSelection(this.actingAgentName, s);
		}
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		
		List<ActionProb> jaProbs = this.jointPolicy.getActionDistributionForState(s);
		HashedAggregator<GroundedSGAgentAction> marginalized = new HashedAggregator<GroundedSGAgentAction>();
		for(ActionProb ap : jaProbs){
			JointAction ja = (JointAction)ap.ga;
			GroundedSGAgentAction thisAgentsAction = ja.action(this.actingAgentName);
			marginalized.add(thisAgentsAction, ap.pSelection);
		}
		
		List<ActionProb> finalProbs = new ArrayList<Policy.ActionProb>(marginalized.size());
		for(Map.Entry<GroundedSGAgentAction, Double> e : marginalized.entrySet()){
			ActionProb ap = new ActionProb(e.getKey(), e.getValue());
			finalProbs.add(ap);
		}
		
		
		return finalProbs;
	}

	@Override
	public boolean isStochastic() {
		return this.jointPolicy.isStochastic();
	}

	@Override
	public boolean isDefinedFor(State s) {
		return this.jointPolicy.isDefinedFor(s);
	}
	
	
	/**
	 * Returns a copy of this policy, which entails of first making a copy of the joint policy.
	 * @return a copy of this policy.
	 */
	public PolicyFromJointPolicy copy(){
		PolicyFromJointPolicy np = new PolicyFromJointPolicy(this.jointPolicy.copy());
		np.setActingAgentName(this.actingAgentName);
		return np;
	}
	

}
