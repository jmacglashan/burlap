package burlap.oomdp.stochasticgames.Callables;

import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.SGAgent;
import burlap.parallel.Parallel.ForEachCallable;

/**
 * Calls game starting for an agent. Useful for concurrent pieces of burlap.
 * @author brawner
 *
 */
public class GameStartingCallable extends ForEachCallable<SGAgent, Boolean> {
	// call to agents need to be threaded, and timed out
	private final State state;
	private final StateAbstraction abstraction;
	
	public GameStartingCallable(State state, StateAbstraction abstraction){
		this.state = state;
		this.abstraction = abstraction;
	}		
	
	@Override
	public Boolean perform(SGAgent current) {
		State abstracted = this.abstraction.abstraction(this.state, current);
		current.gameStarting(abstracted);
		return true;
	}

	@Override
	public ForEachCallable<SGAgent, Boolean> copy() {
		return new GameStartingCallable(this.state, this.abstraction);
	}
				
}
