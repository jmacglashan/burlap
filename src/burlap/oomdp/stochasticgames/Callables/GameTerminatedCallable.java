package burlap.oomdp.stochasticgames.Callables;

import burlap.oomdp.stochasticgames.SGAgent;
import burlap.parallel.Parallel.ForEachCallable;

public class GameTerminatedCallable extends ForEachCallable<SGAgent, Boolean> {
	// call to agents need to be threaded, and timed out
		public GameTerminatedCallable(){}		
		
		@Override
		public Boolean perform(SGAgent current) {
			current.gameTerminated();
			return true;
		}

		@Override
		public ForEachCallable<SGAgent, Boolean> copy() {
			return new GameTerminatedCallable();
		}
}
