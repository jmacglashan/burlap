package burlap.oomdp.singleagent.pomdp.beliefstate.tabular;

import burlap.oomdp.core.states.State;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.HashableStateFactory;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

/**
 * A {@link burlap.oomdp.statehashing.HashableStateFactory} for {@link burlap.oomdp.singleagent.pomdp.beliefstate.tabular.TabularBeliefState} instances.
 * @author James MacGlashan.
 */
public class HashableTabularBeliefStateFactory implements HashableStateFactory{

	@Override
	public HashableState hashState(State s) {

		if(!(s instanceof TabularBeliefState)){
			throw new RuntimeException("Cannot generate HashableState for input state, because it is a " + s.getClass().getName() + " instance and HashableTabularBeliefStateFactory only hashes TabularBeliefState instances.");
		}

		return new HashableTabularBeliefState(s);
	}

	@Override
	public boolean objectIdentifierIndependent() {
		return true;
	}


	public static class HashableTabularBeliefState extends HashableState{

		public HashableTabularBeliefState(State s) {
			super(s);
		}

		@Override
		public int hashCode() {

			HashCodeBuilder builder = new HashCodeBuilder(17, 31);
			for(Map.Entry<Integer, Double> e : ((TabularBeliefState)this.s).beliefValues.entrySet()){
				int entryHash = 31 * e.getKey().hashCode() + e.getValue().hashCode();
				builder.append(entryHash);
			}

			return builder.toHashCode();
		}

		@Override
		public boolean equals(Object obj) {

			if(!(obj instanceof HashableTabularBeliefState)){
				return false;
			}

			return this.s.equals(((HashableTabularBeliefState) obj).s);
		}

		@Override
		public State copy() {
			return new HashableTabularBeliefState(s);
		}
	}
}
