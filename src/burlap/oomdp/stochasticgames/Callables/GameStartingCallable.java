package burlap.oomdp.stochasticgames.Callables;

import burlap.oomdp.stochasticgames.Agent;
import burlap.parallel.Parallel.ForEachCallable;

public class GameStartingCallable extends ForEachCallable<Agent, Boolean> {
	// call to agents need to be threaded, and timed out
	public GameStartingCallable(){}		
	
	@Override
	public Boolean perform(Agent current) {
		current.gameStarting();
		return true;
	}

	@Override
	public ForEachCallable<Agent, Boolean> copy() {
		return new GameStartingCallable();
	}
				
}
