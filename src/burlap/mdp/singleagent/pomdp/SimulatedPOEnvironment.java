package burlap.mdp.singleagent.pomdp;

import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.NullState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentObserver;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.pomdp.observations.ObservationFunction;


/**
 * An {@link burlap.mdp.singleagent.environment.Environment} specifically for simulating interaction with a POMDP
 * environments ({@link burlap.mdp.singleagent.pomdp.PODomain}). In this case, the {@link #currentObservation()}
 * returns the last observation made from the {@link burlap.mdp.singleagent.environment.Environment}, not the hidden
 * state, and the {@link #executeAction(Action)}
 * method does not return {@link burlap.mdp.singleagent.environment.EnvironmentOutcome} objects that contain the full state
 * of the environment, but an observation drawn from the POMDP {@link ObservationFunction} following
 * the execution of the action. If you would like to access the true hidden state of the environment, use the
 * {@link #getCurrentHiddenState()} method.
 */
public class SimulatedPOEnvironment extends SimulatedEnvironment {


	/**
	 * The current observation from the POMDP environment
	 */
	protected State curObservation = NullState.instance;

	protected PODomain poDomain;


	public SimulatedPOEnvironment(PODomain domain) {
		super(domain);
		this.poDomain = domain;
	}

	public SimulatedPOEnvironment(PODomain domain, State initialHiddenState) {
		super(domain, initialHiddenState);
		this.poDomain = domain;
	}

	public SimulatedPOEnvironment(PODomain domain, StateGenerator hiddenStateGenerator) {
		super(domain, hiddenStateGenerator);
		this.poDomain = domain;
	}



	/**
	 * Overrides the current observation of this environment to the specified value
	 * @param observation the current observation of this environment to the specified value
	 */
	public void setCurObservationTo(State observation){
		this.curObservation = observation;
	}


	@Override
	public State currentObservation() {
		return this.curObservation;
	}


	/**
	 * Returns the current hidden state of this {@link burlap.mdp.singleagent.environment.Environment}.
	 * @return a {@link State} representing the current hidden state of the environment.
	 */
	public State getCurrentHiddenState(){
		return this.curState;
	}

	@Override
	public EnvironmentOutcome executeAction(Action a) {



		for(EnvironmentObserver observer : this.observers){
			observer.observeEnvironmentActionInitiation(this.currentObservation(), a);
		}

		State nextObservation = curObservation;

		EnvironmentOutcome eo;
		if(this.allowActionFromTerminalStates || !this.isInTerminalState()) {
			eo = model.sample(this.curState, a);
			nextObservation = poDomain.getObservationFunction().sample(eo.op, a);
		}
		else{
			eo = new EnvironmentOutcome(this.curState, a, this.curState.copy(), 0., true);
		}
		this.lastReward = eo.r;
		this.terminated = eo.terminated;
		this.curState = eo.op;

		EnvironmentOutcome observedOutcome = new EnvironmentOutcome(this.curObservation, a, nextObservation, eo.r, this.terminated);
		this.curObservation = nextObservation;

		for(EnvironmentObserver observer : this.observers){
			observer.observeEnvironmentInteraction(observedOutcome);
		}

		return observedOutcome;


	}

	@Override
	public void resetEnvironment() {
		super.resetEnvironment();
		this.curObservation = NullState.instance;
	}
}
