package burlap.behavior.stochasticgame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.Policy;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.AgentType;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.World;


/**
 * An abstract Policy object for defining a stochastic games joint policy; that is, a policy over joint actions taken by all agents in the world.
 * The primary extension of this object is a set of agent definitions which define the set of possible joint actions in any state.
 * An agent definition consists of an agent name and their {@link AgentType}, the latter of which specifies the set of individual actions
 * that they can take and which object class represents their state.
 * <p/>
 * It is not uncommon for some joint policies to be defined from a privledged agent's position. This class also contains an abstract method
 * for setting that target privledge agent: {@link #setTargetAgent(String)}. If the joint policy is agent agnostic, then this method
 * does not need to do anything.
 * @author James MacGlashan
 *
 */
public abstract class JointPolicy extends Policy {

	/**
	 * The agent definitions that define the set of possible joint actions in each state.
	 */
	protected Map<String, AgentType>		agentsInJointPolicy;
	
	
	/**
	 * Sets the agent definitions that define the set of possible joint actions in each state.
	 * @param agentsInJointPolicy the agent definitions that define the set of possible joint actions in each state.
	 */
	public void setAgentsInJointPolicy(Map<String, AgentType> agentsInJointPolicy){
		this.agentsInJointPolicy = agentsInJointPolicy;
	}
	
	
	/**
	 * Sets the agent definitions by querying the agent names and {@link AgentType} objects from a list of agents.
	 * @param agents the set of agents that will be involved in a joint aciton.
	 */
	public void setAgentsInJointPolicy(List<Agent> agents){
		this.agentsInJointPolicy = new HashMap<String, AgentType>(agents.size());
		for(Agent agent : agents){
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
