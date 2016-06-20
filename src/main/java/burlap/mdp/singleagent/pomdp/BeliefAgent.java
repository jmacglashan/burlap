package burlap.mdp.singleagent.pomdp;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.action.Action;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.pomdp.beliefstate.BeliefState;
import burlap.mdp.singleagent.pomdp.beliefstate.BeliefUpdate;
import burlap.mdp.singleagent.pomdp.beliefstate.TabularBeliefUpdate;


/**
 * An agent that interacts with a POMDP environment. This class contains methods
 * for acting until environment termination or some fixed number of steps and recording the results in an {@link Episode}
 * object. These methods will automatically update this agent's {@link burlap.mdp.singleagent.pomdp.beliefstate.BeliefState}, specified by the {@link #curBelief} data member,
 * as observations are made. Before beginning, the
 * agent's initial {@link burlap.mdp.singleagent.pomdp.beliefstate.BeliefState} will need to be specified with the {@link #setBeliefState(burlap.mdp.singleagent.pomdp.beliefstate.BeliefState)}
 * method.
 * Different agents can be specified by subclassing and implementing the {@link #getAction(burlap.mdp.singleagent.pomdp.beliefstate.BeliefState)} method.
 *
 */
public abstract class BeliefAgent {

	/**
	 * The POMDP  environment.
	 */
	protected Environment environment;

	/**
	 * The agent's current {@link burlap.mdp.singleagent.pomdp.beliefstate.BeliefState}
	 */
	protected BeliefState curBelief;


	/**
	 * The POMDP Domain defining the environment mechanics.
	 */
	protected PODomain poDomain;


	/**
	 * The belief update to use
	 */
	protected BeliefUpdate updater;

	/**
	 * Initializes. By default will use a {@link TabularBeliefUpdate}, but you can
	 * change that with the {@link #setUpdater(BeliefUpdate)} method.
	 * @param poDomain the POMDP domain defining the mechanics of the environment
	 * @param environment the environment in which the agent will be interacting.
	 */
	public BeliefAgent(PODomain poDomain, Environment environment){
		this.poDomain = poDomain;
		this.environment = environment;
		this.updater = new TabularBeliefUpdate(poDomain);
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


	public BeliefUpdate getUpdater() {
		return updater;
	}

	public void setUpdater(BeliefUpdate updater) {
		this.updater = updater;
	}

	/**
	 * Causes the agent to act until the environment reaches a termination condition. The agent's belief is automatically
	 * updated by this method using the specified {@link BeliefUpdate}.
	 * The agent's action selection for the current belief state is defined by
	 * the {@link #getAction(burlap.mdp.singleagent.pomdp.beliefstate.BeliefState)} method. The observation, action, and reward
	 * sequence is saved and {@link Episode} object and returned.
	 * @return and {@link Episode} that recorded the observation, action, and reward sequence.
	 */
	public Episode actUntilTerminal(){
		Episode ea = new Episode();
		ea.initializeInState(this.environment.currentObservation());
		while(!this.environment.isInTerminalState()){
			Action ga = this.getAction(this.curBelief);
			EnvironmentOutcome eo = environment.executeAction(ga);
			ea.transition(ga, eo.op, eo.r);
			
			//update our belief
			this.curBelief = this.updater.update(this.curBelief, eo.op, eo.a);
		}
		
		return ea;
	}

	/**
	 * Causes the agent to act for some fixed number of steps. The agent's belief is automatically
	 * updated by this method using the specified {@link BeliefUpdate}.
	 * The agent's action selection for the current belief state is defined by
	 * the {@link #getAction(burlap.mdp.singleagent.pomdp.beliefstate.BeliefState)} method. The observation, action, and reward
	 * sequence is saved and {@link Episode} object and returned.
	 * @param maxSteps the maximum number of steps to take in the environment
	 * @return and {@link Episode} that recorded the observation, action, and reward sequence.
	 */
	public Episode actUntilTerminalOrMaxSteps(int maxSteps){
		Episode ea = new Episode();
		ea.initializeInState(this.environment.currentObservation());
		int c = 0;
		while(!this.environment.isInTerminalState() && c < maxSteps){
			Action ga = this.getAction(this.curBelief);
			EnvironmentOutcome eo = environment.executeAction(ga);
			ea.transition(ga, eo.op, eo.r);

			//update our belief
			this.curBelief = this.updater.update(this.curBelief, eo.op, eo.a);
			
			c++;
			
		}
		
		return ea;
	}


	/**
	 * Returns the action the agent should take for the input {@link burlap.mdp.singleagent.pomdp.beliefstate.BeliefState}.
	 * @param curBelief the {@link burlap.mdp.singleagent.pomdp.beliefstate.BeliefState} in which the agent must make a decision.
	 * @return A {@link Action} specifying the agent's decision for the input {@link burlap.mdp.singleagent.pomdp.beliefstate.BeliefState}.
	 */
	public abstract Action getAction(BeliefState curBelief);
	
	
}
