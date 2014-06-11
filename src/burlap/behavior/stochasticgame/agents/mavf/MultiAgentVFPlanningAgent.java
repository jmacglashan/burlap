package burlap.behavior.stochasticgame.agents.mavf;

import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.stochasticgame.PolicyFromJointPolicy;
import burlap.behavior.stochasticgame.mavaluefunction.MAQSourcePolicy;
import burlap.behavior.stochasticgame.mavaluefunction.MAValueFunctionPlanner;
import burlap.behavior.stochasticgame.mavaluefunction.backupOperators.CoCoQ;
import burlap.behavior.stochasticgame.mavaluefunction.backupOperators.CorrelatedQ;
import burlap.behavior.stochasticgame.mavaluefunction.policies.ECorrelatedQJointPolicy;
import burlap.behavior.stochasticgame.mavaluefunction.policies.EGreedyMaxWellfare;
import burlap.behavior.stochasticgame.mavaluefunction.vfplanners.MAValueIteration;
import burlap.behavior.stochasticgame.solvers.CorrelatedEquilibriumSolver.CorrelatedEquilibriumObjective;
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


/**
 * A agent that using a multi agent value function planning algorithm (instance of {@link MAValueFunctionPlanner}) to compute the value of each state and then follow
 * a policy derived from a joint policy that is derived from that estimated value function. This is achieved by at each step by the {@link MAValueFunctionPlanner#planFromState(State)} being first
 * called and then following the policy. Ideally, the planning object should only perform planning for a state if it has not already planned for it. The joint policy
 * underlining the policy the agent follows must be an instance of {@link MAQSourcePolicy}. Furthermore, when the policy is set, the underlining joint policy
 * will automatically be set to use this agent's planning object as the value function source and the set of agents will automatically be set to the involved in this agent's
 * world. The {@link PolicyFromJointPolicy} will also be told that this agent is its target.
 * 
 * @author James MacGlashan
 *
 */
public class MultiAgentVFPlanningAgent extends Agent {

	
	/**
	 * The planner this agent will use to estiamte the value function and thereby determine its policy.
	 */
	protected MAValueFunctionPlanner		planner;
	
	/**
	 * The policy dervied from a joint policy derived from the planner's value function estimate that this agent will follow.
	 */
	protected PolicyFromJointPolicy			policy;
	
	/**
	 * Whether the agent definitions for this planner have been set yet.
	 */
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
		//final State s = GridGame.getPrisonersDilemmaInitialState(domain);
		
		JointReward rf = new GridGame.GGJointRewardFunction(domain, -1, 100, false);
		TerminalFunction tf = new GridGame.GGTerminalFunction(domain);
		JointActionModel jam = new GridGameStandardMechanics(domain);
		
		//make a single agent type that can use all actions and refers to the agent class of grid game that we will use for both our agents
		AgentType at = new AgentType("default", domain.getObjectClass(GridGame.CLASSAGENT), domain.getSingleActions());
		
		//MAValueIteration vi = new MAValueIteration(domain, jam, rf, tf, 0.99, hashingFactory, 0., new CoCoQ(), 0.0001, 30);
		MAValueIteration vi = new MAValueIteration(domain, jam, rf, tf, 0.99, hashingFactory, 0., new CorrelatedQ(CorrelatedEquilibriumObjective.UTILITARIAN), 0.0001, 30);
		
		//create our world
		World w = new World(domain, new GridGameStandardMechanics(domain), rf, new GridGame.GGTerminalFunction(domain), 
				new ConstantSGStateGenerator(s));
		
		Visualizer v = GGVisualizer.getVisualizer(9, 9);
		VisualWorldObserver wob = new VisualWorldObserver(domain, v);
		wob.setFrameDelay(1000);
		wob.initGUI();
		
		
		/*
		EGreedyMaxWellfare jp0 = new EGreedyMaxWellfare(0.0);
		jp0.setBreakTiesRandomly(false);
		
		EGreedyMaxWellfare jp1 = new EGreedyMaxWellfare(0.0);
		jp1.setBreakTiesRandomly(false);
		*/
		
		
		ECorrelatedQJointPolicy jp0 = new ECorrelatedQJointPolicy(0.0);
		//ECorrelatedQJointPolicy jp1 = new ECorrelatedQJointPolicy(0.0);
		
		
		MultiAgentVFPlanningAgent a0 = new MultiAgentVFPlanningAgent(domain, vi, new PolicyFromJointPolicy(jp0, true));
		MultiAgentVFPlanningAgent a1 = new MultiAgentVFPlanningAgent(domain, vi, new PolicyFromJointPolicy(jp0, true));
		
		a0.joinWorld(w, at);
		a1.joinWorld(w, at);
		
		w.addWorldObserver(wob);
		
		EGreedyMaxWellfare jp = new EGreedyMaxWellfare(0.0);
		jp.setAgentsInJointPolicyFromWorld(w);
		jp.setQSourceProvider(vi);
		jp.setBreakTiesRandomly(false);
		
		
		
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
		
		List<ActionProb> aps = jp.getActionDistributionForState(s);
		
		for(ActionProb ap : aps){
			System.out.println(ap.pSelection + ": " + ap.ga.toString());
		}
		
		
		
		
	}
	
	/**
	 * Initializes. The underlining joint policy of the policy must be an instance of {@link MAQSourcePolicy} or a runtime exception will be thrown.
	 * The joint policy will automatically be set to use the provided planner as the value function source.
	 * @param domain the domain in which the agent will act
	 * @param planner the planner the agent should use for determining its policy
	 * @param policy the policy that will use the planners value function as a source.
	 */
	public MultiAgentVFPlanningAgent(SGDomain domain, MAValueFunctionPlanner planner, PolicyFromJointPolicy policy){
		if(!(policy.getJointPolicy() instanceof MAQSourcePolicy)){
			throw new RuntimeException("The underlining joint policy must be of type MAQSourcePolicy for the MultiAgentVFPlanningAgent.");
		}
		super.init(domain);
		this.planner = planner;
		this.policy = policy;
		((MAQSourcePolicy)this.policy.getJointPolicy()).setQSourceProvider(planner);
	}
	
	
	
	/**
	 * Sets the policy derived from this agents planner to follow. he underlining joint policy of the policy must be an instance of {@link MAQSourcePolicy} 
	 * or a runtime exception will be thrown.
	 * The joint policy will automatically be set to use the provided planner as the value function source.
	 * @param policy the policy that will use the planners value function as a source.
	 */
	public void setPolicy(PolicyFromJointPolicy policy){
		if(!(policy.getJointPolicy() instanceof MAQSourcePolicy)){
			throw new RuntimeException("The underlining joint policy must be of type MAQSourcePolicy for the MultiAgentVFPlanningAgent.");
		}
		this.policy = policy;
		((MAQSourcePolicy)this.policy.getJointPolicy()).setQSourceProvider(planner);
		this.policy.setActingAgentName(this.worldAgentName);
		
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
