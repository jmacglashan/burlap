package burlap.behavior.valuefunction;

import burlap.behavior.policy.Policy;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 * An interface for MDP solvers that can return/compute Q-values.
 * @author James MacGlashan
 *
 */
public interface QFunction extends ValueFunction{

	/**
	 * Returns a {@link java.util.List} of {@link burlap.behavior.valuefunction.QValue} objects for ever permissible action for the given input state.
	 * @param s the state for which Q-values are to be returned.
	 * @return a {@link java.util.List} of {@link burlap.behavior.valuefunction.QValue} objects for ever permissible action for the given input state.
	 */
	public List <QValue> getQs(State s);

	/**
	 * Returns the {@link burlap.behavior.valuefunction.QValue} for the given state-action pair.
	 * @param s the input state
	 * @param a the input action
	 * @return the {@link burlap.behavior.valuefunction.QValue} for the given state-action pair.
	 */
	public QValue getQ(State s, Action a);


	/**
	 * A class of helper static methods that may be commonly used by code that uses a QFunction instance. In particular,
	 * methods for computing the value function of a state, given the Q-values (the max Q-value or policy weighted value).
	 */
	public static class QFunctionHelper {
	    
	    private QFunctionHelper() {
	        // do nothing
	    }

		/**
		 * Returns the optimal state value function for a state given a {@link QFunction}.
		 * The optimal value is the max Q-value. If no actions are permissible in the input state, then zero is returned.
		 * @param qSource the {@link QFunction} capable of producing Q-values.
		 * @param s the query {@link State} for which the value should be returned.
		 * @return the max Q-value for all possible Q-values in the state.
		 */
		public static double getOptimalValue(QFunction qSource, State s){
			List <QValue> qs = qSource.getQs(s);
			if(qs.isEmpty()){
				return 0.;
			}
			double max = Double.NEGATIVE_INFINITY;
			for(QValue q : qs){
				max = Math.max(q.q, max);
			}
			return max;
		}



		/**
		 * Returns the state value under a given policy for a state and {@link QFunction}.
		 * The value is the expected Q-value under the input policy action distribution. If no actions are permissible in the input state, then zero is returned.
		 * @param qSource the {@link QFunction} capable of producing Q-values.
		 * @param s the query {@link State} for which the value should be returned.
		 * @param p the policy defining the action distribution.
		 * @return the expected Q-value under the input policy action distribution
		 */
		public static double getPolicyValue(QFunction qSource, State s, Policy p){

			double expectedValue = 0.;
			List <Policy.ActionProb> aps = p.getActionDistributionForState(s);
			if(aps.isEmpty()){
				return 0.;
			}
			for(Policy.ActionProb ap : aps){
				double q = qSource.getQ(s, ap.ga).q;
				expectedValue += q * ap.pSelection;
			}
			return expectedValue;
		}


	}

}
