package burlap.oomdp.singleagent.pomdp;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.pomdp.POMDPEpisodeAnalysis;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

public abstract class BeliefAgent {

	protected POEnvironment environment;
	protected BeliefState curBelief;
	
	
	public void setEnvironment(POEnvironment environment){
		this.environment = environment;
	}
	
	public void setBeliefState(BeliefState beliefState){
		this.curBelief = beliefState;
	}
	
	public POMDPEpisodeAnalysis actUntilTerminal(){
		POMDPEpisodeAnalysis ea = new POMDPEpisodeAnalysis();
		ea.initializeEpisideWithInitialState(this.environment.getCurMDPState());
		while(!this.environment.curStateIsTerminal()){
			GroundedAction ga = this.getAction(this.curBelief);
			State observation = this.environment.executeAction(ga);
			State nextMDPState = this.environment.getCurMDPState();
			double r = this.environment.lastR;
			ea.recordTransitionTo(ga, nextMDPState, r,observation);
			
			//update our belief
			//first get POMDP action to make sure the getAction returned the true source action
			GroundedAction pomdpAction = new GroundedAction(this.environment.getPODomain().getAction(ga.actionName()), ga.params);
			this.curBelief = this.curBelief.getUpdatedBeliefState(observation, pomdpAction);
			
		}
		
		return ea;
	}
	
	public POMDPEpisodeAnalysis actUntilTerminalOrMaxSteps(int maxSteps){
		POMDPEpisodeAnalysis ea = new POMDPEpisodeAnalysis();
		ea.initializeEpisideWithInitialState(this.environment.getCurMDPState());
		int c = 0;
		while(!this.environment.curStateIsTerminal() && c < maxSteps){
			GroundedAction ga = this.getAction(this.curBelief);
			State observation = this.environment.executeAction(ga);
			State nextMDPState = this.environment.getCurMDPState();
			double r = this.environment.lastR;
			ea.recordTransitionTo(ga, nextMDPState, r,observation);
			
			//update our belief
			//first get POMDP action to make sure the getAction returned the true source action
			GroundedAction pomdpAction = new GroundedAction(this.environment.getPODomain().getAction(ga.actionName()), ga.params);
			this.curBelief = this.curBelief.getUpdatedBeliefState(observation, pomdpAction);
			
			c++;
			
		}
		
		return ea;
	}
	
	public abstract GroundedAction getAction(BeliefState curBelief);
	
	
}
