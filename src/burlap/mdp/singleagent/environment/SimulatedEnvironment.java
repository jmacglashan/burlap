package burlap.mdp.singleagent.environment;

import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.auxiliary.common.ConstantStateGenerator;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.SampleModel;

import java.util.LinkedList;
import java.util.List;

/**
 * An {@link burlap.mdp.singleagent.environment.Environment} that simulates interactions using a {@link SampleModel} that is provided
 * in an input domain.
 * Initial states of the environment
 * are defined using a {@link burlap.mdp.auxiliary.StateGenerator}. If no {@link burlap.mdp.auxiliary.StateGenerator} is specified,
 * but an initial {@link State} is provided in a constructor, then the {@link burlap.mdp.auxiliary.StateGenerator} is
 * set to a {@link burlap.mdp.auxiliary.common.ConstantStateGenerator} so that upon {@link #resetEnvironment()} method calls,
 * the initial state is the same as the original input state.
 * <p>
 * All returned environment observations are fully observable returning a copy of the true internal {@link State} of
 * the environment. Copies of the state are returned to prevent tampering of the internal environment state.
 * <p>
 * By default, this {@link burlap.mdp.singleagent.environment.Environment} will not allow states to change when the current
 * environment state is a terminal state (as specified by the input {@link burlap.mdp.core.TerminalFunction}); instead, the
 * same current state will be returned with a reward of zero if someone attempts to interact with the environment through {@link #executeAction(burlap.mdp.core.Action)}.
 * In this case, the environment state will have to be manually changed with {@link #resetEnvironment()} or {@link #setCurStateTo(State)}
 * to a non-terminal state before actions will affect the state again. Alternatively, you can allow actions to affect the state from
 * terminal states with the {@link #setAllowActionFromTerminalStates(boolean)} method.
 * @author James MacGlashan.
 */
public class SimulatedEnvironment implements StateSettableEnvironment, EnvironmentServerInterface{

	protected SampleModel model;


	/**
	 * The state generator used to generate new states when the environment is reset with {@link #resetEnvironment()};
	 */
	protected StateGenerator stateGenerator;

	/**
	 * The current state of the environment
	 */
	protected State curState;

	/**
	 * The last reward generated from this environment.
	 */
	protected double lastReward = 0.;

	protected boolean terminated = false;

	/**
	 * A flag indicating whether the environment will respond to actions from a terminal state. If false,
	 * then once a the environment transitions to a terminal state, any action attempted by the {@link #executeAction(burlap.mdp.core.Action)}
	 * method will result in no change in state and to enable action again, the Environment state will have to be
	 * manually changed with the {@link #resetEnvironment()} method or the {@link #setCurStateTo(State)} method.
	 * If this value is true, then actions will be carried out according to the domain's transition dynamics.
	 */
	protected boolean allowActionFromTerminalStates = false;


	/**
	 * The {@link burlap.mdp.singleagent.environment.EnvironmentObserver} objects that will be notified of {@link burlap.mdp.singleagent.environment.Environment}
	 * events.
	 */
	protected List<EnvironmentObserver> observers = new LinkedList<EnvironmentObserver>();



	public SimulatedEnvironment(SADomain domain){
		if(domain.getModel() == null){
			throw new RuntimeException("SimulatedEnvironment requires a Domain with a model, but the input domain does not have one.");
		}
		this.model = domain.getModel();
	}

	public SimulatedEnvironment(SADomain domain, State initialState) {

		this.stateGenerator = new ConstantStateGenerator(initialState);
		this.curState = initialState;
		if(domain.getModel() == null){
			throw new RuntimeException("SimulatedEnvironment requires a Domain with a model, but the input domain does not have one.");
		}
		this.model = domain.getModel();
	}

	public SimulatedEnvironment(SADomain domain, StateGenerator stateGenerator) {
		this.stateGenerator = stateGenerator;
		this.curState = stateGenerator.generateState();
		if(domain.getModel() == null){
			throw new RuntimeException("SimulatedEnvironment requires a Domain with a model, but the input domain does not have one.");
		}
		this.model = domain.getModel();
	}

	public SimulatedEnvironment(SampleModel model){
		this.model = model;
	}

	public SimulatedEnvironment(SampleModel model, State initialState) {

		this.stateGenerator = new ConstantStateGenerator(initialState);
		this.curState = initialState;
		this.model = model;
	}

	public SimulatedEnvironment(SampleModel model, StateGenerator stateGenerator) {
		this.stateGenerator = stateGenerator;
		this.curState = stateGenerator.generateState();
		this.model = model;
	}



	public StateGenerator getStateGenerator() {
		return stateGenerator;
	}

	public void setStateGenerator(StateGenerator stateGenerator) {
		this.stateGenerator = stateGenerator;
	}

	@Override
	public void addObservers(EnvironmentObserver... observers) {
		for(EnvironmentObserver o : observers){
			this.observers.add(o);
		}
	}

	@Override
	public void clearAllObservers() {
		this.observers.clear();
	}

	@Override
	public void removeObservers(EnvironmentObserver... observers) {
		for(EnvironmentObserver o : observers){
			this.observers.remove(o);
		}
	}

	@Override
	public List<EnvironmentObserver> observers() {
		return this.observers;
	}

	/**
	 * Sets whether the environment will respond to actions from a terminal state. If false,
	 * then once a the environment transitions to a terminal state, any action attempted by the {@link #executeAction(burlap.mdp.core.Action)}
	 * method will result in no change in state and to enable action again, the Environment state will have to be
	 * manually changed with the {@link #resetEnvironment()} method or the {@link #setCurStateTo(State)} method.
	 * If this value is true, then actions will be carried out according to the domain's transition dynamics.
	 * @param allowActionFromTerminalStates if false, then actions are not allowed from terminal states; if true, then they are allowed.
	 */
	public void setAllowActionFromTerminalStates(boolean allowActionFromTerminalStates){
		this.allowActionFromTerminalStates = true;
	}

	@Override
	public void setCurStateTo(State s) {
		if(this.stateGenerator == null){
			this.stateGenerator = new ConstantStateGenerator(s);
		}
		this.curState = s;
	}

	@Override
	public State currentObservation() {
		return this.curState.copy();
	}

	@Override
	public EnvironmentOutcome executeAction(Action a) {

		for(EnvironmentObserver observer : this.observers){
			observer.observeEnvironmentActionInitiation(this.currentObservation(), a);
		}

		EnvironmentOutcome eo;
		if(this.allowActionFromTerminalStates || !this.isInTerminalState()) {
			eo = model.sampleTransition(this.curState, a);
		}
		else{
			eo = new EnvironmentOutcome(this.curState, a, this.curState.copy(), 0., true);
		}
		this.lastReward = eo.r;
		this.terminated = eo.terminated;
		this.curState = eo.op;

		for(EnvironmentObserver observer : this.observers){
			observer.observeEnvironmentInteraction(eo);
		}

		return eo;
	}

	@Override
	public double lastReward() {
		return this.lastReward;
	}

	@Override
	public boolean isInTerminalState() {
		return this.terminated;
	}

	@Override
	public void resetEnvironment() {
		this.lastReward = 0.;
		this.terminated = false;
		this.curState = stateGenerator.generateState();
		for(EnvironmentObserver observer : this.observers){
			observer.observeEnvironmentReset(this);
		}
	}
}
