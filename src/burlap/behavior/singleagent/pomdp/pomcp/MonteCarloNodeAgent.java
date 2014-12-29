package burlap.behavior.singleagent.pomdp.pomcp;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.QValue;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.pomdp.BeliefAgent;
import burlap.oomdp.singleagent.pomdp.BeliefState;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.pomdp.POMDPEpisodeAnalysis;
import burlap.debugtools.RandomFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The agent does not use a policy class at all, and directly uses policies spit out by a QComputable planner
 */
public class MonteCarloNodeAgent extends BeliefAgent{

	MonteCarloPOMDPPlanner MCPolicyPlanner;
	
	public MonteCarloNodeAgent(MonteCarloPOMDPPlanner MCP){
		super();
		this.MCPolicyPlanner = MCP; 
		this.curBelief=null;
	}
	
	
	@Override
	public void setBeliefState(BeliefState beliefState){
		this.curBelief = null;
		System.out.println("The MonteCarloNodeAgent's belief is maintained in the MonteCarloPlanner, hence this setting is not allowed");
	}
	
	@Override
	public POMDPEpisodeAnalysis actUntilTerminal(){
		POMDPEpisodeAnalysis ea = new POMDPEpisodeAnalysis();
		ea.initializeEpisideWithInitialState(this.environment.getCurMDPState());
		GroundedAction ga = this.MCPolicyPlanner.getCurrentBestAction();
		State observation = this.environment.executeAction(ga);
		State nextMDPState = this.environment.getCurMDPState();
		double r = this.environment.getLastReward();
		ea.recordTransitionTo(ga, nextMDPState, r, observation);
		
		while(!this.environment.curStateIsTerminal()){
			ga = this.MCPolicyPlanner.getAction(observation, ga);
			observation = this.environment.executeAction(ga);
			nextMDPState = this.environment.getCurMDPState();
			r = this.environment.getLastReward();
			ea.recordTransitionTo(ga, nextMDPState, r, observation);
			
			//update our belief if the state enumerator is available
			//first get POMDP action to make sure the getAction returned the true source action
//			GroundedAction pomdpAction = new GroundedAction(this.environment.getPODomain().getAction(ga.actionName()), ga.params);
			if(this.environment.getPODomain().getStateEnumerator()!=null){
				this.curBelief = this.MCPolicyPlanner.getBeliefState();
			}
			
		}
		
		return ea;
	}
	
	
	@Override
	public POMDPEpisodeAnalysis actUntilTerminalOrMaxSteps(int maxSteps){
		POMDPEpisodeAnalysis ea = new POMDPEpisodeAnalysis();
		ea.initializeEpisideWithInitialState(this.environment.getCurMDPState());
		int c = 0;
		GroundedAction ga = this.MCPolicyPlanner.getCurrentBestAction();
		State observation = this.environment.executeAction(ga);
		State nextMDPState = this.environment.getCurMDPState();
		double r = this.environment.getLastReward();
		ea.recordTransitionTo(ga, nextMDPState, r, observation);
		c=1; // first action from the initial belief state
		while(!this.environment.curStateIsTerminal() && c < maxSteps){
			ga = this.MCPolicyPlanner.getAction(observation, ga);
//			System.out.println("MonteCarloNodeAgent: " +ga.actionName());
			observation = this.environment.executeAction(ga);
			nextMDPState = this.environment.getCurMDPState();
			r = this.environment.getLastReward();
			ea.recordTransitionTo(ga, nextMDPState, r, observation);
			
			//update our belief if the state enumerator is available
			if(this.environment.getPODomain().getStateEnumerator()!=null){
				this.curBelief = this.MCPolicyPlanner.getBeliefState();
			}

			c++;
			
		}
		
		return ea;
	}
		
	@Override
	public GroundedAction getAction(BeliefState curBelief) {
		
		List<QValue> qValues = this.MCPolicyPlanner.getQs(curBelief);


		List <QValue> maxActions = new ArrayList<QValue>();
		maxActions.add(qValues.get(0));
		double maxQ = qValues.get(0).q;
		for(int i = 1; i < qValues.size(); i++){
			QValue q = qValues.get(i);
			if(q.q == maxQ){
				maxActions.add(q);
			}
			else if(q.q > maxQ){
				maxActions.clear();
				maxActions.add(q);
				maxQ = q.q;
			}

		}
		Random rand = RandomFactory.getMapped(0);

		int selected = rand.nextInt(maxActions.size());

		return (GroundedAction)maxActions.get(selected).a;
	}
	
	
	public GroundedAction getUpdateAction(State Obs, GroundedAction ga) {
		return this.MCPolicyPlanner.getAction(Obs, ga);
	}




}
