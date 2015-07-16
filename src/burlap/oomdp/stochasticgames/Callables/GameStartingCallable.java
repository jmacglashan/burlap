package burlap.oomdp.stochasticgames.Callables;

import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.Agent;
import burlap.parallel.Parallel.ForEachCallable;

public class GameStartingCallable extends ForEachCallable<Agent, Boolean> {
	// call to agents need to be threaded, and timed out
	private final State state;
	private final StateAbstraction abstraction;
	
	public GameStartingCallable(State state, StateAbstraction abstraction){
		this.state = state;
		this.abstraction = abstraction;
	}		
	
	@Override
	public Boolean perform(Agent current) {
		State abstracted = this.abstraction.abstraction(this.state, current);
		current.gameStarting(abstracted);
		return true;
	}

	@Override
	public ForEachCallable<Agent, Boolean> copy() {
		return new GameStartingCallable(this.state, this.abstraction);
	}
				
}
