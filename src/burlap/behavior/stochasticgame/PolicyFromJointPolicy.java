package burlap.behavior.stochasticgame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.Policy;
import burlap.datastructures.HashedAggregator;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;


/**
 * This class defines a single agent's policy that is derived from a joint policy. It takes as input a source joint policy
 * and the agent name whose actions in the returned joint actions are being followed. The action distribution for the agent
 * is determined by marginalizing over other agent's actions in the joint policy.
 * <p/>
 * When the agent name for this policy is
 * set, it automatically calls the {@link JointPolicy#setTargetAgent(String)} method of the source joint policy with the same
 * agent name.
 * @author James MacGlashan
 *
 */
public class PolicyFromJointPolicy extends Policy {

	protected JointPolicy					jointPolicy;
	protected String						actingAgentName;
	
	
	public PolicyFromJointPolicy(JointPolicy jointPolicy){
		this.jointPolicy = jointPolicy;
	}
	
	
	public PolicyFromJointPolicy(String actingAgentName, JointPolicy jointPolicy){
		this.setActingAgentName(actingAgentName);
		this.jointPolicy = jointPolicy;
	}
	
	public void setJointPolicy(JointPolicy jointPolicy){
		this.jointPolicy = jointPolicy;
	}
	
	public JointPolicy getJointPolicy(){
		return this.jointPolicy;
	}
	
	
	public void setActingAgentName(String agentName){
		this.actingAgentName = agentName;
		this.jointPolicy.setTargetAgent(agentName);
	}
	
	public String getActingAgentName(){
		return this.actingAgentName;
	}
	
	@Override
	public AbstractGroundedAction getAction(State s) {
		return ((JointAction)this.jointPolicy.getAction(s)).action(this.actingAgentName);
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		
		List<ActionProb> jaProbs = this.jointPolicy.getActionDistributionForState(s);
		HashedAggregator<GroundedSingleAction> marginalized = new HashedAggregator<GroundedSingleAction>();
		for(ActionProb ap : jaProbs){
			JointAction ja = (JointAction)ap.ga;
			GroundedSingleAction thisAgentsAction = ja.action(this.actingAgentName);
			marginalized.add(thisAgentsAction, ap.pSelection);
		}
		
		List<ActionProb> finalProbs = new ArrayList<Policy.ActionProb>(marginalized.size());
		for(Map.Entry<GroundedSingleAction, Double> e : marginalized.entrySet()){
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
