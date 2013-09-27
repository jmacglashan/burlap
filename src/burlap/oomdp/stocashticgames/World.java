package burlap.oomdp.stocashticgames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.debugtools.DPrint;
import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.auxiliary.common.NullAbstraction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;


public class World {

	protected SGDomain							domain;
	protected State								currentState;
	protected List <Agent>						agents;
	protected Map<AgentType, List<Agent>>		agentsByType;
	protected Map<String, Double>				agentCumulativeReward;
	
	protected JointActionModel 					worldModel;
	protected JointReward						jointRewardModel;
	protected TerminalFunction					tf;
	protected SGStateGenerator					initialStateGenerator;
	
	protected StateAbstraction					abstractionForAgents;
	
	
	protected JointAction						lastJointAction;
	
	
	protected int								debugId;
	
	
	public World(SGDomain domain, JointActionModel jam, JointReward jr, TerminalFunction tf, SGStateGenerator sg){
		this.init(domain, jam, jr, tf, sg, new NullAbstraction());
	}
	
	public World(SGDomain domain, JointActionModel jam, JointReward jr, TerminalFunction tf, SGStateGenerator sg, StateAbstraction abstractionForAgents){
		this.init(domain, jam, jr, tf, sg, abstractionForAgents);
	}
	
	public void init(SGDomain domain, JointActionModel jam, JointReward jr, TerminalFunction tf, SGStateGenerator sg, StateAbstraction abstractionForAgents){
		this.domain = domain;
		this.worldModel = jam;
		this.jointRewardModel = jr;
		this.tf = tf;
		this.initialStateGenerator = sg;
		this.abstractionForAgents = abstractionForAgents;
		
		agents = new ArrayList<Agent>();
		agentsByType = new HashMap<AgentType, List<Agent>>();
		
		agentCumulativeReward = new HashMap<String, Double>();
		
		
		
		debugId = 284673923;
	}
	
	
	public int getDebugId(){
		return debugId;
	}
	
	public void setDebugId(int id){
		debugId = id;
	}
	
	
	public double getCumulativeRewardForAgent(String aname){
		return agentCumulativeReward.get(aname);
	}
	
	public String registerAgent(Agent a, AgentType at){
		//don't register the same agent multiple times
		if(this.agentInstanceExists(a)){
			return a.worldAgentName;
		}
		
		String agentName = this.getNewWorldNameForAgentAndIndex(a, at);
		
		return agentName;
		
	}
	
	public State getCurrentWorldState(){
		return this.currentState;
	}
	
	public void generateNewCurrentState(){
		currentState = initialStateGenerator.generateState(agents);
	}
	
	public JointAction getLastJointAction(){
		return this.lastJointAction;
	}
	
	public void runGame(){
		
		for(Agent a : agents){
			a.gameStarting();
		}
		
		currentState = initialStateGenerator.generateState(agents);
		
		while(!tf.isTerminal(currentState)){
			this.runStage();
		}
		
		for(Agent a : agents){
			a.gameTerminated();
		}
		
		DPrint.cl(debugId, currentState.getCompleteStateDescription());
		
	}
	
	public void runGame(int maxStages){
		
		for(Agent a : agents){
			a.gameStarting();
		}
		
		currentState = initialStateGenerator.generateState(agents);
		int t = 0;
		
		while(!tf.isTerminal(currentState) && t < maxStages){
			this.runStage();
			t++;
		}
		
		for(Agent a : agents){
			a.gameTerminated();
		}
		
		DPrint.cl(debugId, currentState.getCompleteStateDescription());
		
	}
	
	public void runStage(){
		if(tf.isTerminal(currentState)){
			return ; //cannot continue this game
		}
		
		
		
		JointAction ja = new JointAction(agents.size());
		State abstractedCurrent = abstractionForAgents.abstraction(currentState);
		for(Agent a : agents){
			ja.addAction(a.getAction(abstractedCurrent));
		}
		this.lastJointAction = ja;
		
		
		DPrint.cl(debugId, ja.toString());
		
		
		//now that we have the joint action, perform it
		State sp = worldModel.performJointAction(currentState, ja);
		State abstractedPrime = this.abstractionForAgents.abstraction(sp);
		Map<String, Double> jointReward = jointRewardModel.reward(currentState, ja, sp);
		
		DPrint.cl(debugId, jointReward.toString());
		
		//index reward
		for(String aname : jointReward.keySet()){
			double curCumR = agentCumulativeReward.get(aname);
			curCumR += jointReward.get(aname);
			agentCumulativeReward.put(aname, curCumR);
		}
		
		//tell all the agents about it
		for(Agent a : agents){
			a.observeOutcome(abstractedCurrent, ja, jointReward, abstractedPrime, tf.isTerminal(sp));
		}
		
		//update the state
		currentState = sp;
		
	}
	
	
	public JointActionModel getActionModel(){
		return worldModel;
	}
	
	public JointReward getRewardModel(){
		return jointRewardModel;
	}
	
	public TerminalFunction getTF(){
		return tf;
	}
	
	public List <Agent> getRegisteredAgents(){
		return new ArrayList<Agent>(agents);
	}
	
	public int getPlayerNumberForAgent(String aname){
		for(int i = 0; i < agents.size(); i++){
			Agent a = agents.get(i);
			if(a.worldAgentName.equals(aname)){
				return i;
			}
		}
		
		return -1;
	}
	
	
	protected String getNewWorldNameForAgentAndIndex(Agent a, AgentType type){
	
		
		List <Agent> aots = agentsByType.get(type);
		if(aots == null){
			aots = new ArrayList<Agent>();
			agentsByType.put(type, aots);
		}
		
		String name = type.typeName + aots.size();
		agents.add(a);
		aots.add(a);
		
		agentCumulativeReward.put(name, 0.);
		
		return name;
	}
	
	protected boolean agentInstanceExists(Agent a){
		for(Agent A : agents){
			if(A == a){
				return true;
			}
		}
		
		return false;
	}

}
