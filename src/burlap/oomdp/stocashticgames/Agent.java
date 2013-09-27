package burlap.oomdp.stocashticgames;

import java.util.Map;

import burlap.oomdp.core.State;


public abstract class Agent {

	protected SGDomain				domain;
	protected JointReward			internalRewardFunction;
	
	
	//data members for interaction with the world
	protected AgentType				agentType;
	protected String				worldAgentName;
	protected World					world;
	
	
	protected void init(SGDomain d){
		this.domain = d;
		internalRewardFunction = null;
	}
	
	public void setInternalRewardFunction(JointReward jr){
		this.internalRewardFunction = jr;
	}
	
	public JointReward getInternalRewardFunction() {
		return this.internalRewardFunction;
	}
	
	
	public void joinWorld(World w, AgentType as){
		agentType = as;
		world = w;
		worldAgentName = world.registerAgent(this, as);
	}
	
	public String getAgentName(){
		return worldAgentName;
	}
	
	public AgentType getAgentType(){
		return agentType;
	}
	
	public abstract void gameStarting();
	public abstract GroundedSingleAction getAction(State s);
	public abstract void observeOutcome(State s, JointAction jointAction, Map<String, Double> jointReward, State sprime, boolean isTerminal);
	public abstract void gameTerminated();

}
