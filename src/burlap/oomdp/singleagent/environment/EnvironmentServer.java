package burlap.oomdp.singleagent.environment;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.LinkedList;
import java.util.List;

/**
 * A {@link burlap.oomdp.singleagent.environment.EnvironmentServerInterface} implementation that delegates all {@link burlap.oomdp.singleagent.environment.Environment} interactions and request
 * to a provided {@link burlap.oomdp.singleagent.environment.Environment} delegate. This class will also
 * intercept all interactions through the {@link #executeAction(burlap.oomdp.singleagent.GroundedAction)} and
 * {@link #resetEnvironment()} methods
 * and tell all {@link burlap.oomdp.singleagent.environment.EnvironmentOutcome} instances registered with this server
 * about the event.
 * @author James MacGlashan.
 */
public class EnvironmentServer implements EnvironmentServerInterface, EnvironmentDelegation {

	/**
	 * the {@link burlap.oomdp.singleagent.environment.Environment} delegate that handles all primary {@link burlap.oomdp.singleagent.environment.Environment}
	 * functionality.
	 */
	protected Environment delegate;

	/**
	 * The {@link burlap.oomdp.singleagent.environment.EnvironmentObserver} objects that will be notified of {@link burlap.oomdp.singleagent.environment.Environment}
	 * events.
	 */
	protected List<EnvironmentObserver> observers = new LinkedList<EnvironmentObserver>();


	/**
	 * If the input {@link burlap.oomdp.singleagent.environment.Environment} is an instance {@link burlap.oomdp.singleagent.environment.EnvironmentServerInterface},
	 * then all the input observers are added to it and it is returned. Otherwise, a new {@link burlap.oomdp.singleagent.environment.EnvironmentServer}
	 * is created around it, with all of the observers added.
	 * @param env the {@link burlap.oomdp.singleagent.environment.Environment} that will have observers added to it
	 * @param observers the {@link burlap.oomdp.singleagent.environment.EnvironmentObserver} objects to add.
	 * @return the input {@link burlap.oomdp.singleagent.environment.Environment} or an {@link burlap.oomdp.singleagent.environment.EnvironmentServer}.
	 */
	public static EnvironmentServerInterface constructServerOrAddObservers(Environment env, EnvironmentObserver...observers){
		if(env instanceof EnvironmentServerInterface){
			((EnvironmentServerInterface)env).addObservers(observers);
			return (EnvironmentServerInterface)env;
		}
		else{
			return constructor(env, observers);
		}
	}

	/**
	 * Constructs an {@link burlap.oomdp.singleagent.environment.EnvironmentServer} or {@link burlap.oomdp.singleagent.environment.EnvironmentServer.StateSettableEnvironmentServer},
	 * based on whether the input delegate implements {@link burlap.oomdp.singleagent.environment.StateSettableEnvironment}.
	 * @param delegate the delegate {@link burlap.oomdp.singleagent.environment.Environment} for most environment interactions.
	 * @param observers the {@link burlap.oomdp.singleagent.environment.EnvironmentObserver} objects notified of Environment events.
	 * @return an {@link burlap.oomdp.singleagent.environment.EnvironmentServer} or {@link burlap.oomdp.singleagent.environment.EnvironmentServer.StateSettableEnvironmentServer}.
	 */
	public static EnvironmentServer constructor(Environment delegate, EnvironmentObserver...observers){
		if(delegate instanceof StateSettableEnvironment){
			return new StateSettableEnvironmentServer((StateSettableEnvironment)delegate);
		}
		return new EnvironmentServer(delegate, observers);
	}

	public EnvironmentServer(Environment delegate, EnvironmentObserver...observers){
		this.delegate = delegate;
		for(EnvironmentObserver observer : observers){
			this.observers.add(observer);
		}
	}

	/**
	 * Returns the {@link burlap.oomdp.singleagent.environment.Environment} delegate that handles all {@link burlap.oomdp.singleagent.environment.Environment}
	 * functionality
	 * @return the {@link burlap.oomdp.singleagent.environment.Environment} delegate
	 */
	public Environment getEnvironmentDelegate() {
		return delegate;
	}

	/**
	 * Sets the {@link burlap.oomdp.singleagent.environment.Environment} delegate that handles all {@link burlap.oomdp.singleagent.environment.Environment} functionality
	 * @param delegate  the {@link burlap.oomdp.singleagent.environment.Environment} delegate
	 */
	public void setEnvironmentDelegate(Environment delegate) {
		this.delegate = delegate;
	}

	/**
	 * Adds one or more {@link burlap.oomdp.singleagent.environment.EnvironmentObserver}s
	 * @param observers and {@link burlap.oomdp.singleagent.environment.EnvironmentObserver}
	 */
	public void addObservers(EnvironmentObserver...observers){
		for(EnvironmentObserver observer : observers){
			this.observers.add(observer);
		}
	}

	/**
	 * Clears all {@link burlap.oomdp.singleagent.environment.EnvironmentObserver}s from this server.
	 */
	public void clearAllObservers(){
		this.observers.clear();
	}

	/**
	 * Removes one or more {@link burlap.oomdp.singleagent.environment.EnvironmentObserver}s from this server.
	 * @param observers the {@link burlap.oomdp.singleagent.environment.EnvironmentObserver}s to remove.
	 */
	public void removeObservers(EnvironmentObserver...observers){
		for(EnvironmentObserver observer : observers){
			this.observers.remove(observer);
		}
	}


	/**
	 * Returns all {@link burlap.oomdp.singleagent.environment.EnvironmentObserver}s registered with this server.
	 * @return all {@link burlap.oomdp.singleagent.environment.EnvironmentObserver}s registered with this server.
	 */
	public List<EnvironmentObserver> getObservers(){
		return this.observers;
	}


	@Override
	public State getCurrentObservation() {
		return this.delegate.getCurrentObservation();
	}

	@Override
	public EnvironmentOutcome executeAction(GroundedAction ga) {
		for(EnvironmentObserver observer : this.observers){
			observer.observeEnvironmentActionInitiation(this.delegate.getCurrentObservation(), ga);
		}
		EnvironmentOutcome eo = this.delegate.executeAction(ga);
		for(EnvironmentObserver observer : this.observers){
			observer.observeEnvironmentInteraction(eo);
		}
		return eo;
	}

	@Override
	public double getLastReward() {
		return this.delegate.getLastReward();
	}

	@Override
	public boolean isInTerminalState() {
		return this.delegate.isInTerminalState();
	}

	@Override
	public void resetEnvironment() {
		this.delegate.resetEnvironment();
		for(EnvironmentObserver observer : this.observers){
			observer.observeEnvironmentReset(this.delegate);
		}
	}


	public static class StateSettableEnvironmentServer extends EnvironmentServer implements StateSettableEnvironment{

		public StateSettableEnvironmentServer(StateSettableEnvironment delegate, EnvironmentObserver... observers) {
			super(delegate, observers);
		}

		@Override
		public void setCurStateTo(State s) {
			((StateSettableEnvironment)this.delegate).setCurStateTo(s);
		}
	}
}
