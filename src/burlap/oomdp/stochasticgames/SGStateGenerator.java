package burlap.oomdp.stochasticgames;

import java.util.List;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;


/**
 * An abstract class defining the interface and common mechanism for generating State objects specifically for stochastic games domains. 
 * Unlike the similar {@link burlap.oomdp.auxiliary.StateGenerator} class, this class requires a list of agents that will be in the world
 * and will create an ObjecInstance for each agent that belongs the OO-MDP object class specified by each agent's {@link AgentType}.
 * @author James MacGlashan
 *
 */
public abstract class SGStateGenerator {

	/**
	 * Generates a new state with the given agents in it.
	 * @param agents the agents that should be in the state.
	 * @return a new state instance.
	 */
	public abstract State generateState(List <Agent> agents);
	
	/**
	 * Creates an object instance belonging to the object class specified in the agent's {@link AgentType} data member.
	 * The returned object instance will have the name of the agent.
	 * @param a the agent for which to create an OO-MDP state object instance
	 * @return an object instance for this agent.
	 */
	protected ObjectInstance getAgentObjectInstance(Agent a){
		return new ObjectInstance(a.agentType.oclass, a.worldAgentName);
	}

}
