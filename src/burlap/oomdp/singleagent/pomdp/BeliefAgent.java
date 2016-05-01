package burlap.oomdp.singleagent.pomdp;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState;


/**
 * An agent that interacts with a POMDP environment. This class contains methods
 * for acting until environment termination or some fixed number of steps and recording the results in an {@link burlap.behavior.singleagent.EpisodeAnalysis}
 * object. These methods will automatically update this agent's {@link burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState}, specified by the {@link #curBelief} data member,
 * as observations are made. Before beginning, the
 * agent's initial {@link burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState} will need to be specified with the {@link #setBeliefState(burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState)}
 * method.
 * Different agents can be specified by subclassing and implementing the {@link #getAction(burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState)} method.
 *
 */
public abstract class BeliefAgent {

	/**
	 * The POMDP  environment.
	 */
	protected Environment environment;

	/**
	 * The agent's current {@link burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState}
	 */
	protected BeliefState curBelief;


	/**
	 * The POMDP Domain defining the environment mechanics.
	 */
	protected PODomain poDomain;

	/**
	 * Initializes
	 * @param poDomain the POMDP domain defining the mechanics of the environment
	 * @param environment the environment in which the agent will be interacting.
	 */
	public BeliefAgent(PODomain poDomain, Environment environment){
		this.poDomain = poDomain;
		this.environment = environment;
	}

	/**
	 * Sets the POMDP environment
	 * @param environment the POMDP environment
	 */
	public void setEnvironment(Environment environment){
		this.environment = environment;
	}

	/**
	 * Sets this agent's current belief
	 * @param beliefState the agent' current belief
	 */
	public void setBeliefState(BeliefState beliefState){
		this.curBelief = beliefState;
	}


	/**
	 * Causes the agent to act until the environment reaches a termination condition. The agent's belief is automatically
	 * updated by this method using the the current {@link burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState}'s
	 * {@link burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState#getUpdatedBeliefState(State, burlap.oomdp.singleagent.GroundedAction)}
	 * method. The agent's action selection for the current belief state is defend by
	 * the {@link #getAction(burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState)} method. The observation, action, and reward
	 * sequence is saved and {@link burlap.behavior.singleagent.EpisodeAnalysis} object and returned.
	 * @return and {@link burlap.behavior.singleagent.EpisodeAnalysis} that recorded the observation, action, and reward sequence.
	 */
	public EpisodeAnalysis actUntilTerminal(){
		EpisodeAnalysis ea = new EpisodeAnalysis();
		ea.initializeEpisideWithInitialState(this.environment.getCurrentObservation());
		while(!this.environment.isInTerminalState()){
			GroundedAction ga = this.getAction(this.curBelief);
			EnvironmentOutcome eo = ga.executeIn(environment);
			ea.recordTransitionTo(ga, eo.op, eo.r);
			
			//update our belief
			this.curBelief = this.curBelief.getUpdatedBeliefState(eo.op, eo.a);
			
		}
		
		return ea;
	}

	/**
	 * Causes the agent to act for some fixed number of steps. The agent's belief is automatically
	 * updated by this method using the the current {@link burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState}'s
	 * {@link burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState#getUpdatedBeliefState(State, burlap.oomdp.singleagent.GroundedAction)}
	 * method. The agent's action selection for the current belief state is defend by
	 * the {@link #getAction(burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState)} method. The observation, action, and reward
	 * sequence is saved and {@link burlap.behavior.singleagent.EpisodeAnalysis} object and returned.
	 * @return and {@link burlap.behavior.singleagent.EpisodeAnalysis} that recorded the observation, action, and reward sequence.
	 */
	public EpisodeAnalysis actUntilTerminalOrMaxSteps(int maxSteps){
		EpisodeAnalysis ea = new EpisodeAnalysis();
		ea.initializeEpisideWithInitialState(this.environment.getCurrentObservation());
		int c = 0;
		while(!this.environment.isInTerminalState() && c < maxSteps){
			GroundedAction ga = this.getAction(this.curBelief);
			EnvironmentOutcome eo = ga.executeIn(environment);
			ea.recordTransitionTo(ga, eo.op, eo.r);

			//update our belief
			this.curBelief = this.curBelief.getUpdatedBeliefState(eo.op, eo.a);
			
			c++;
			
		}
		
		return ea;
	}


	/**
	 * Returns the action the agent should take for the input {@link burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState}.
	 * @param curBelief the {@link burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState} in which the agent must make a decision.
	 * @return A {@link burlap.oomdp.singleagent.GroundedAction} specifying the agent's decision for the input {@link burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState}.
	 */
	public abstract GroundedAction getAction(BeliefState curBelief);
	
	
}
