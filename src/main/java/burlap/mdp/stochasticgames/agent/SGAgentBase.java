package burlap.mdp.stochasticgames.agent;

import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import burlap.mdp.stochasticgames.world.World;

/**
 * @author James MacGlashan.
 */
public abstract class SGAgentBase implements SGAgent {

	protected World world;
	protected SGDomain domain;
	protected JointRewardFunction internalRewardFunction;


	//data members for interaction with the world
	protected SGAgentType agentType;
	protected String				worldAgentName;

	public void init(SGDomain domain){
		this.domain = domain;
	}

	public void init(SGDomain domain, String agentName, SGAgentType type){
		this.domain = domain;
		this.worldAgentName = agentName;
		this.agentType = type;
	}

	public SGAgentBase setAgentDetails(String agentName, SGAgentType type){
		this.worldAgentName = agentName;
		this.agentType = type;
		return this;
	}

	public void setInternalRewardFunction(JointRewardFunction rf){
		this.internalRewardFunction = rf;
	}

	/**
	 * Returns the internal reward function used by the agent.
	 * @return the internal reward function used by the agent; null if the agent is not using an internal reward function.
	 */
	public JointRewardFunction getInternalRewardFunction() {
		return this.internalRewardFunction;
	}



	/**
	 * Returns this agent's name
	 * @return this agent's name
	 */
	public String agentName(){
		return worldAgentName;
	}


	/**
	 * Returns this agent's type
	 * @return this agent's type
	 */
	public SGAgentType agentType(){
		return agentType;
	}



}
