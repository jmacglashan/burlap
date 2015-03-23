package burlap.behavior.singleagent.planning;

import java.util.List;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;

/**
 * An interface for planning classes that can compute Q-values.
 * @author James MacGlashan
 *
 */
public interface QComputablePlanner {

	/**
	 * Returns a {@link java.util.List} of {@link burlap.behavior.singleagent.QValue} objects for ever permissible action for the given input state.
	 * @param s the state for which Q-values are to be returned.
	 * @return a {@link java.util.List} of {@link burlap.behavior.singleagent.QValue} objects for ever permissible action for the given input state.
	 */
	public List <QValue> getQs(State s);

	/**
	 * Returns the {@link burlap.behavior.singleagent.QValue} for the given state-action pair.
	 * @param s the input state
	 * @param a the input action
	 * @return the {@link burlap.behavior.singleagent.QValue} for the given state-action pair.
	 */
	public QValue getQ(State s, AbstractGroundedAction a);


	/**
	 * A class of helper static methods that may be commonly used code that uses a QComputable planner. In particular,
	 * methods for computing the value function of a state, given the Q-values
	 */
	public static class QComputablePlannerHelper{

		/**
		 * Returns the optimal state value function for a state given a {@link burlap.behavior.singleagent.planning.QComputablePlanner}.
		 * The optimal value is the max Q-value. If no actions are permissible in the input state, then zero is returned.
		 * @param planner the {@link burlap.behavior.singleagent.planning.QComputablePlanner} capable of producing Q-values.
		 * @param s the query {@link burlap.oomdp.core.State} for which the value should be returned.
		 * @return the max Q-value for all possible Q-values in the state.
		 */
		public static double getOptimalValue(QComputablePlanner planner, State s){
			List <QValue> qs = planner.getQs(s);
			if(qs.size() == 0){
				return 0.;
			}
			double max = Double.NEGATIVE_INFINITY;
			for(QValue q : qs){
				max = Math.max(q.q, max);
			}
			return max;
		}

		/**
		 * Returns the optimal state value for a state given a {@link burlap.behavior.singleagent.planning.QComputablePlanner}.
		 * The optimal value is the max Q-value. If no actions are permissible in the input state or the input state is a terminal state, then zero is returned.
		 * @param planner the {@link burlap.behavior.singleagent.planning.QComputablePlanner} capable of producing Q-values.
		 * @param s the query {@link burlap.oomdp.core.State} for which the value should be returned.
		 * @param tf a terminal function.
		 * @return the max Q-value for all possible Q-values in the state or zero if there are not permissible actions or if the state is a terminal state.
		 */
		public static double getOptimalValue(QComputablePlanner planner, State s, TerminalFunction tf){

			if(tf.isTerminal(s)){
				return 0.;
			}

			return getOptimalValue(planner, s);
		}


		/**
		 * Returns the state value under a given policy for a state and {@link burlap.behavior.singleagent.planning.QComputablePlanner}.
		 * The value is the expected Q-value under the input policy action distribution. If no actions are permissible in the input state, then zero is returned.
		 * @param planner the {@link burlap.behavior.singleagent.planning.QComputablePlanner} capable of producing Q-values.
		 * @param s the query {@link burlap.oomdp.core.State} for which the value should be returned.
		 * @param p the policy defining the action distribution.
		 * @return the expected Q-value under the input policy action distribution
		 */
		public static double getPolicyValue(QComputablePlanner planner, State s, Policy p){

			double expectedValue = 0.;
			List <Policy.ActionProb> aps = p.getActionDistributionForState(s);
			if(aps.size() == 0){
				return 0.;
			}
			for(Policy.ActionProb ap : aps){
				double q = planner.getQ(s, ap.ga).q;
				expectedValue += q * ap.pSelection;
			}
			return expectedValue;
		}


		/**
		 * Returns the state value under a given policy for a state and {@link burlap.behavior.singleagent.planning.QComputablePlanner}.
		 * The value is the expected Q-value under the input policy action distribution. If no actions are permissible in the input state, then zero is returned.
		 * @param planner the {@link burlap.behavior.singleagent.planning.QComputablePlanner} capable of producing Q-values.
		 * @param s the query {@link burlap.oomdp.core.State} for which the value should be returned.
		 * @param p the policy defining the action distribution.
		 * @param tf a terminal function.
		 * @return the expected Q-value under the input policy action distribution or zero if there are not permissible actions or if the state is a terminal state.
		 */
		public static double getPolicyValue(QComputablePlanner planner, State s, Policy p, TerminalFunction tf){

			if(tf.isTerminal(s)){
				return 0.;
			}

			return getPolicyValue(planner, s, p);
		}

	}

}
