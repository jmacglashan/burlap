package burlap.oomdp.singleagent.pomdp.beliefstate;

import burlap.oomdp.core.states.State;

import java.util.List;

/**
 * An interface to be used by {@link BeliefState} implementations that also can enumerate
 * the set of states that have probability mass. The probability mass of a state is specified by the
 * {@link burlap.oomdp.singleagent.pomdp.beliefstate.EnumerableBeliefState.StateBelief} class which is a pair
 * consisting of an MDP state defined by a {@link burlap.oomdp.core.states.State} instance, and its probability mass, defined by
 * a double.
 * @author James MacGlashan.
 */
public interface EnumerableBeliefState {

	/**
	 * Returns the states, and their probability mass, that have non-zero probability mass. States that are not
	 * included in the returned listed are assumed to have probability mass zero.
	 * @return a {@link java.util.List} of {@link burlap.oomdp.singleagent.pomdp.beliefstate.EnumerableBeliefState.StateBelief} objects specifying the enumerated probability mass function.
	 */
	List<StateBelief> getStatesAndBeliefsWithNonZeroProbability();


	/**
	 * A class for specifying the probability mass of an MDP state in a {@link BeliefState}.
	 */
	public static class StateBelief{

		/**
		 * The MDP state defined by a {@link burlap.oomdp.core.states.State} instance.
		 */
		public State s;

		/**
		 * The probability mass of the MDP state.
		 */
		public double belief;


		/**
		 * Initializes
		 * @param s the MDP state defined by a {@link burlap.oomdp.core.states.State} instance.
		 * @param belief the probability mass of the state.
		 */
		public StateBelief(State s, double belief){
			this.s = s;
			this.belief = belief;
		}
	}
}
