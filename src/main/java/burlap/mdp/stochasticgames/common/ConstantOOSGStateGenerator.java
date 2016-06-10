package burlap.mdp.stochasticgames.common;

import burlap.mdp.core.state.State;
import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.SGStateGenerator;

import java.util.List;

/**
 * A state generator for object-oriented stochastic games where the state of each agent
 * is defined by an OO-MDP object. This class takes as input a base state that will be used
 * for generation, but will rename the OO-MDP object instances to the names of the agents in the world.
 * @author James MacGlashan.
 */
public class ConstantOOSGStateGenerator implements SGStateGenerator{

	String agentClassName;
	MutableOOState state;

	/**
	 * Init.
	 * @param agentClassName the OO-MDP class name for objects that define each agent's personal state
	 * @param state the base state that will be returned by the state generator.
	 */
	public ConstantOOSGStateGenerator(String agentClassName, MutableOOState state) {
		this.agentClassName = agentClassName;
	}

	@Override
	public State generateState(List<SGAgent> agents) {
		return this.renameAgents((MutableOOState)state.copy(), agents);
	}


	protected OOState renameAgents(MutableOOState state, List<SGAgent> agents){

		MutableOOState renamed = (MutableOOState)state.copy();

		List<ObjectInstance> agentObs = state.objectsOfClass(this.agentClassName);
		if(agents.size() != agentObs.size()){
			throw new RuntimeException("Cannot rename agent objects, because the number of agents is not the same as the number of OO-MDP objects defining the agent state");
		}

		for(int i = 0; i < agentObs.size(); i++){
			ObjectInstance aob = agentObs.get(i);
			SGAgent agent = agents.get(i);
			renamed.renameObject(aob.name(), agent.getAgentName());
		}


		return renamed;

	}

}
