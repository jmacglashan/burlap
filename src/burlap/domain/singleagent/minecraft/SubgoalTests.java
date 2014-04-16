package burlap.domain.singleagent.minecraft;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class SubgoalTests {
	
	public static class BridgeSubgoal implements StateConditionTest {

		private PropositionalFunction pf;

		public BridgeSubgoal(PropositionalFunction pf) {
			this.pf = pf;
		}
		
		@Override
		public boolean satisfies(State s) {
			return this.pf.isTrue(s);
		}
		
	}

}
