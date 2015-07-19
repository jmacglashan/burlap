package burlap.oomdp.singleagent.environment;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.LinkedList;
import java.util.List;

/**
 * A server that delegates all {@link burlap.oomdp.singleagent.environment.Environment} interactions and request
 * to a provided {@link burlap.oomdp.singleagent.environment.Environment} delegate. This class will also
 * intercept all interactions through the {@link #executeAction(burlap.oomdp.singleagent.GroundedAction)} method
 * and tell all {@link burlap.oomdp.singleagent.environment.EnvironmentOutcome} instances registered with this server
 * about the event.
 * @author James MacGlashan.
 */
public class EnvironmentServer implements Environment {

	/**
	 * the {@link burlap.oomdp.singleagent.environment.Environment} delegate that handles all primary {@link burlap.oomdp.singleagent.environment.Environment}
	 * functionality.
	 */
	protected Environment delegate;

	protected List<EnvironmentObserver> observers = new LinkedList<EnvironmentObserver>();

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
	public State getCurState() {
		return this.delegate.getCurState();
	}

	@Override
	public EnvironmentOutcome executeAction(GroundedAction ga) {
		EnvironmentOutcome eo = this.delegate.executeAction(ga);
		for(EnvironmentObserver observer : this.observers){
			observer.observeEnvironment(eo);
		}
		return eo;
	}

	@Override
	public double getLastReward() {
		return this.delegate.getLastReward();
	}

	@Override
	public boolean curStateIsTerminal() {
		return this.delegate.curStateIsTerminal();
	}

	@Override
	public void resetEnvironment() {
		this.delegate.resetEnvironment();
	}
}
