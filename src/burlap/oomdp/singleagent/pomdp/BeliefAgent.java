package burlap.oomdp.singleagent.pomdp;

import burlap.behavior.singleagent.EpisodeAnalysis;
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
	
	public EpisodeAnalysis actUntilTerminal(){
		EpisodeAnalysis ea = new EpisodeAnalysis();
		ea.initializeEpisideWithInitialState(this.environment.getCurMDPState());
		while(!this.environment.curStateIsTerminal()){
			GroundedAction ga = this.getAction(this.curBelief);
			State observation = this.environment.executeAction(ga);
			State nextMDPState = this.environment.getCurMDPState();
			double r = this.environment.lastR;
			ea.recordTransitionTo(ga, nextMDPState, r);
			
			//update our belief
			//first get POMDP action to make sure the getAction returned the true source action
			GroundedAction pomdpAction = new GroundedAction(this.environment.getPODomain().getAction(ga.actionName()), ga.params);
			this.curBelief = this.curBelief.getUpdatedBeliefState(observation, pomdpAction);
			
		}
		
		return ea;
	}
	
	public EpisodeAnalysis actUntilTerminalOrMaxSteps(int maxSteps){
		EpisodeAnalysis ea = new EpisodeAnalysis();
		ea.initializeEpisideWithInitialState(this.environment.getCurMDPState());
		int c = 0;
		while(!this.environment.curStateIsTerminal() && c < maxSteps){
			GroundedAction ga = this.getAction(this.curBelief);
			State observation = this.environment.executeAction(ga);
			State nextMDPState = this.environment.getCurMDPState();
			double r = this.environment.lastR;
			ea.recordTransitionTo(ga, nextMDPState, r);
			
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
