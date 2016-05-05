package burlap.mdp.stochasticgames;

import burlap.mdp.core.state.State;

import java.util.List;


/**
 * An abstract class defining the interface and common mechanism for generating State objects specifically for stochastic games domains. 
 * Unlike the similar {@link burlap.mdp.auxiliary.StateGenerator} class, this class requires a list of agents that will be in the world
 * and will create an ObjecInstance for each agent that belongs the OO-MDP object class specified by each agent's {@link SGAgentType}.
 * @author James MacGlashan
 *
 */
public interface SGStateGenerator {

	/**
	 * Generates a new state with the given agents in it.
	 * @param agents the agents that should be in the state.
	 * @return a new state instance.
	 */
	State generateState(List <SGAgent> agents);


}
