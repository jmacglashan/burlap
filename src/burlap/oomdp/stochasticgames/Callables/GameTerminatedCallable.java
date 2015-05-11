package burlap.oomdp.stochasticgames.Callables;

import burlap.oomdp.stochasticgames.Agent;
import burlap.parallel.Parallel.ForEachCallable;

public class GameTerminatedCallable extends ForEachCallable<Agent, Boolean> {
	// call to agents need to be threaded, and timed out
		public GameTerminatedCallable(){}		
		
		@Override
		public Boolean perform(Agent current) {
			current.gameTerminated();
			return true;
		}

		@Override
		public ForEachCallable<Agent, Boolean> copy() {
			return new GameTerminatedCallable();
		}
}
