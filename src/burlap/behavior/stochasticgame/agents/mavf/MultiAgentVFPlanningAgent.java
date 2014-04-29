package burlap.behavior.stochasticgame.agents.mavf;

import java.util.Map;

import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.stochasticgame.PolicyFromJointPolicy;
import burlap.behavior.stochasticgame.mavaluefunction.MAQSourcePolicy;
import burlap.behavior.stochasticgame.mavaluefunction.MAValueFunctionPlanner;
import burlap.behavior.stochasticgame.mavaluefunction.backupOperators.CoCoQ;
import burlap.behavior.stochasticgame.mavaluefunction.policies.EGreedyMaxWellfare;
import burlap.behavior.stochasticgame.mavaluefunction.vfplanners.MAValueIteration;
import burlap.domain.stochasticgames.gridgame.GGVisualizer;
import burlap.domain.stochasticgames.gridgame.GridGame;
import burlap.domain.stochasticgames.gridgame.GridGameStandardMechanics;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.AgentType;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.JointReward;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.World;
import burlap.oomdp.stochasticgames.common.ConstantSGStateGenerator;
import burlap.oomdp.stochasticgames.common.VisualWorldObserver;
import burlap.oomdp.visualizer.Visualizer;

public class MultiAgentVFPlanningAgent extends Agent {

	protected MAValueFunctionPlanner		planner;
	protected PolicyFromJointPolicy			policy;
	
	protected boolean						setAgentDefinitions = false;
	
	
	public static void main(String [] args){
		
		//create domain
		GridGame domainGen = new GridGame();
		final SGDomain domain = (SGDomain)domainGen.generateDomain();
		
		//create hashing factory that only hashes on the agent positions (ignores wall attributes)
		final DiscreteStateHashFactory hashingFactory = new DiscreteStateHashFactory();
		hashingFactory.addAttributeForClass(GridGame.CLASSAGENT, domain.getAttribute(GridGame.ATTX));
		hashingFactory.addAttributeForClass(GridGame.CLASSAGENT, domain.getAttribute(GridGame.ATTY));
		hashingFactory.addAttributeForClass(GridGame.CLASSAGENT, domain.getAttribute(GridGame.ATTPN));
		
		final State s = GridGame.getTurkeyInitialState(domain);
		
		JointReward rf = new GridGame.GGJointRewardFunction(domain, -1, 100, false);
		TerminalFunction tf = new GridGame.GGTerminalFunction(domain);
		JointActionModel jam = new GridGameStandardMechanics(domain);
		
		//make a single agent type that can use all actions and refers to the agent class of grid game that we will use for both our agents
		AgentType at = new AgentType("default", domain.getObjectClass(GridGame.CLASSAGENT), domain.getSingleActions());
		
		MAValueIteration vi = new MAValueIteration(domain, jam, rf, tf, 0.99, hashingFactory, 0., new CoCoQ(), 0.00, 30);
		
		//create our world
		World w = new World(domain, new GridGameStandardMechanics(domain), rf, new GridGame.GGTerminalFunction(domain), 
				new ConstantSGStateGenerator(s));
		
		Visualizer v = GGVisualizer.getVisualizer(9, 9);
		VisualWorldObserver wob = new VisualWorldObserver(domain, v);
		wob.setFrameDelay(1000);
		wob.initGUI();
		
		MultiAgentVFPlanningAgent a0 = new MultiAgentVFPlanningAgent(domain, vi, new PolicyFromJointPolicy(new EGreedyMaxWellfare(0.0)));
		MultiAgentVFPlanningAgent a1 = new MultiAgentVFPlanningAgent(domain, vi, new PolicyFromJointPolicy(new EGreedyMaxWellfare(0.0)));
		
		a0.joinWorld(w, at);
		a1.joinWorld(w, at);
		
		w.addWorldObserver(wob);
		
		for(int i = 0; i < 5; i++){
			v.updateState(s);
			if(i > 0){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			w.runGame();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
		
		
		
		
	}
	
	
	public MultiAgentVFPlanningAgent(SGDomain domain, MAValueFunctionPlanner planner, PolicyFromJointPolicy policy){
		if(!(policy.getJointPolicy() instanceof MAQSourcePolicy)){
			throw new RuntimeException("The underlining joint policy must be of type MAQSourcePolicy for the MultiAgentVFPlanningAgent.");
		}
		super.init(domain);
		this.planner = planner;
		this.policy = policy;
		((MAQSourcePolicy)this.policy.getJointPolicy()).setQSourceProvider(planner);
	}
	
	@Override
	public void joinWorld(World w, AgentType as){
		super.joinWorld(w, as);
		this.policy.setActingAgentName(this.worldAgentName);
	}
	
	
	@Override
	public void gameStarting() {
		if(!this.setAgentDefinitions){
			this.planner.setAgentDefinitions(this.world.getAgentDefinitions());
			this.policy.getJointPolicy().setAgentsInJointPolicy(this.world.getAgentDefinitions());
			this.setAgentDefinitions = true;
		}
	}

	@Override
	public GroundedSingleAction getAction(State s) {
		this.planner.planFromState(s);
		return (GroundedSingleAction)this.policy.getAction(s);
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction,
			Map<String, Double> jointReward, State sprime, boolean isTerminal) {
		
		//nothing to do

	}

	@Override
	public void gameTerminated() {
		//nothing to do
	}

}
