package burlap.behavior.stochasticgames;

import burlap.behavior.policy.Policy;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.action.JointAction;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.world.World;

import java.util.*;


/**
 * An abstract Policy object for defining a stochastic games joint policy; that is, a policy over joint actions taken by all agents in the world.
 * The primary extension of this object is a set of agent definitions which define the set of possible joint actions in any state.
 * An agent definition consists of an agent name and their {@link SGAgentType}, the latter of which specifies the set of individual actions
 * that they can take and which object class represents their state.
 * <p>
 * It is not uncommon for some joint policies to be defined from a privledged agent's position. This class also contains an abstract method
 * for setting that target privledge agent: {@link #setTargetAgent(String)}. If the joint policy is agent agnostic, then this method
 * does not need to do anything.
 * <p>
 * This class can also be used to synchonize the action selection of multiple agents according to the same sampled joint action. This is achieved
 * by using the {@link #getAgentSynchronizedActionSelection(String, State)} method, which returns the single action for each agent (of the specified
 * name) from the same sampled joint action until all agents defined in the policy have queried the method for their action selection.
 * @author James MacGlashan
 *
 */
public abstract class JointPolicy implements Policy {

	/**
	 * The agent definitions that define the set of possible joint actions in each state.
	 */
	protected Map<String, SGAgentType>		agentsInJointPolicy;
	
	/**
	 * The last synchronized joint action that was selected
	 */
	protected JointAction					lastSynchronizedJointAction = null;
	
	/**
	 * The agents whose actiosn have been syncrhonized so far
	 */
	protected Set<String>					agentsSyncrhonizedSoFar = new HashSet<String>();
	
	/**
	 * The last state in which synchronized actions were queried.
	 */
	protected State							lastSyncedState = null;
	
	
	
	
	/**
	 * Sets the agent definitions that define the set of possible joint actions in each state.
	 * @param agentsInJointPolicy the agent definitions that define the set of possible joint actions in each state.
	 */
	public void setAgentsInJointPolicy(Map<String, SGAgentType> agentsInJointPolicy){
		this.agentsInJointPolicy = agentsInJointPolicy;
	}
	
	
	/**
	 * Sets the agent definitions by querying the agent names and {@link SGAgentType} objects from a list of agents.
	 * @param agents the set of agents that will be involved in a joint aciton.
	 */
	public void setAgentsInJointPolicy(List<SGAgent> agents){
		this.agentsInJointPolicy = new HashMap<String, SGAgentType>(agents.size());
		for(SGAgent agent : agents){
			this.agentsInJointPolicy.put(agent.getAgentName(), agent.getAgentType());
		}
	}
	
	
	/**
	 * Sets teh agent definitions by querying the agents that exist in a {@link World} object.
	 * @param w the {@link World} object that contains the agents that will define the set of possible joint acitons.
	 */
	public void setAgentsInJointPolicyFromWorld(World w){
		this.setAgentsInJointPolicy(w.getRegisteredAgents());
	}
	
	
	/**
	 * Returns all possible joint actions that can be taken in state s for the set of agents defined to be used in this joint policy.
	 * @param s the state in which all joint actions should be returned.
	 * @return the set of all possible {@link JointAction} objects.
	 */
	public List<JointAction> getAllJointActions(State s){
		return JointAction.getAllJointActions(s, agentsInJointPolicy);
	}
	
	
	/**
	 * Returns a map specifying the agents who contribute actions to this joint policy. The map goes from
	 * agent names to their agent type.
	 * @return a map specifying the agents who contribute actions to this joint policy
	 */
	public Map<String, SGAgentType> getAgentsInJointPolicy(){
		return this.agentsInJointPolicy;
	}
	
	
	/**
	 * This method returns the action for a single agent by a synchonrized sampling of this joint policy,
	 * which enables multiple agents to query this policy object and act according to the same selected joint
	 * actions from it. This is useful when decisions are made from a "referee" who selects the joint action
	 * that dictates the behavior of each agent. The synchonization is implemented by selecting a joint action.
	 * Each time an agent queries for their action, it is drawn from the previously sampled joint action.
	 * A new joint action is only selected after each agent defined in this objects {@link #agentsInJointPolicy} member 
	 * has queried this method for their action or until an action for a different state is queried (that is, *either* condition
	 * will cause the joint action to be resampled).
	 * @param agentName the agent name whose action in this joint policy is being queried
	 * @param s the state in which the action is to be selected.
	 * @return the single agent action to be taken according to the synchonrized joint action that was selected.
	 */
	public Action getAgentSynchronizedActionSelection(String agentName, State s){
		
		if(this.lastSyncedState == null || !this.lastSyncedState.equals(s)){
			//then reset syncrhonization
			this.lastSyncedState = s;
			this.agentsSyncrhonizedSoFar.clear();
			this.lastSynchronizedJointAction = (JointAction)this.action(s);
		}
		
		Action a = this.lastSynchronizedJointAction.action(agentName);
		this.agentsSyncrhonizedSoFar.add(agentName);
		if(this.agentsSyncrhonizedSoFar.containsAll(this.agentsInJointPolicy.keySet())){
			//then we're finished getting the actions for all agents and enable the next query
			this.lastSyncedState = null;
			this.agentsSyncrhonizedSoFar.clear();
		}
		
		return a;
		
	}
	
	
	
	/**
	 * Sets the target privledged agent from which this joint policy is defined.
	 * @param agentName the name of the target agent.
	 */
	public abstract void setTargetAgent(String agentName);
	
	
	
	/**
	 * Creates a copy of this joint policy and returns it. This is useful when generating different agents using the same kind of policy, but have different target
	 * agents evaluating it.
	 * @return a copy of this joint policy.
	 */
	public abstract JointPolicy copy();

}
