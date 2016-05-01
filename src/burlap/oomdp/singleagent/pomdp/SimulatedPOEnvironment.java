package burlap.oomdp.singleagent.pomdp;

import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;


/**
 * An {@link burlap.oomdp.singleagent.environment.Environment} specifically for simulating interaction with a POMDP
 * environments ({@link burlap.oomdp.singleagent.pomdp.PODomain}). In this case, the {@link #getCurrentObservation()}
 * returns the last observation made from the {@link burlap.oomdp.singleagent.environment.Environment}, not the hidden
 * state, and the {@link #executeAction(burlap.oomdp.singleagent.GroundedAction)}
 * method does not return {@link burlap.oomdp.singleagent.environment.EnvironmentOutcome} objects that contain the full state
 * of the environment, but an observation drawn from the POMDP {@link burlap.oomdp.singleagent.pomdp.ObservationFunction} following
 * the execution of the action. If you would like to access the true hidden state of the environment, use the
 * {@link #getCurrentHiddenState()} method.
 */
public class SimulatedPOEnvironment extends SimulatedEnvironment {


	/**
	 * The current observation from the POMDP environment
	 */
	protected State curObservation = State.NullState.instance;




	public SimulatedPOEnvironment(PODomain domain, RewardFunction rf, TerminalFunction tf) {
		super(domain, rf, tf);
	}

	public SimulatedPOEnvironment(PODomain domain, RewardFunction rf, TerminalFunction tf, State initialHiddenState) {
		super(domain, rf, tf, initialHiddenState);
	}

	public SimulatedPOEnvironment(PODomain domain, RewardFunction rf, TerminalFunction tf, StateGenerator hiddenStateGenerator) {
		super(domain, rf, tf, hiddenStateGenerator);
	}


	public PODomain getPODomain(){
		return (PODomain)this.domain;
	}

	@Override
	public void setDomain(Domain domain) {
		if(!(domain instanceof PODomain)){
			throw new RuntimeException("Cannot set the POSimulatedEnvironment domain to a domain that is not a PODomain instance");
		}
		super.setDomain(domain);
	}

	/**
	 * Overrides the current observation of this environment to the specified value
	 * @param observation the current observation of this environment to the specified value
	 */
	public void setCurObservationTo(State observation){
		this.curObservation = observation;
	}


	@Override
	public State getCurrentObservation() {
		return this.curObservation;
	}


	/**
	 * Returns the current hidden state of this {@link burlap.oomdp.singleagent.environment.Environment}.
	 * @return a {@link State} representing the current hidden state of the environment.
	 */
	public State getCurrentHiddenState(){
		return this.curState;
	}

	@Override
	public EnvironmentOutcome executeAction(GroundedAction ga) {

		GroundedAction simGA = ga.copy();
		simGA.action = this.domain.getAction(ga.actionName());
		if(simGA.action == null){
			throw new RuntimeException("Cannot execute action " + ga.toString() + " in this SimulatedEnvironment because the action is to known in this Environment's domain");
		}
		State nextState = this.curState;
		State nextObservation = this.curObservation;
		if(this.allowActionFromTerminalStates || !this.isInTerminalState()) {
			nextState = simGA.executeIn(this.curState);
			this.lastReward = this.rf.reward(this.curState, simGA, nextState);
			nextObservation = ((PODomain)domain).getObservationFunction().sampleObservation(nextState, simGA);
		}
		else{
			this.lastReward = 0.;
		}

		EnvironmentOutcome eo = new EnvironmentOutcome(this.curObservation.copy(), ga, nextObservation.copy(), this.lastReward, this.tf.isTerminal(nextState));

		this.curState = nextState;
		this.curObservation = nextObservation;

		return eo;

	}

	@Override
	public void resetEnvironment() {
		super.resetEnvironment();
		this.curObservation = State.NullState.instance;
	}
}
