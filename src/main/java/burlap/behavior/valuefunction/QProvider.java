package burlap.behavior.valuefunction;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.support.ActionProb;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 * An interface for MDP solvers that can return/compute Q-values. Provides a method for generating the set of relevant Q-values.
 * @author James MacGlashan
 *
 */
public interface QProvider extends QFunction{

	/**
	 * Returns a {@link java.util.List} of {@link burlap.behavior.valuefunction.QValue} objects for ever permissible action for the given input state.
	 * @param s the state for which Q-values are to be returned.
	 * @return a {@link java.util.List} of {@link burlap.behavior.valuefunction.QValue} objects for ever permissible action for the given input state.
	 */
	List <QValue> qValues(State s);


	/**
	 * A class of helper static methods that may be commonly used by code that uses a QFunction instance. In particular,
	 * methods for computing the value function of a state, given the Q-values (the max Q-value or policy weighted value).
	 */
	class Helper {
	    
	    private Helper() {
	        // do nothing
	    }

		/**
		 * Returns the optimal state value function for a state given a {@link QProvider}.
		 * The optimal value is the max Q-value. If no actions are permissible in the input state, then zero is returned.
		 * @param qSource the {@link QProvider} capable of producing Q-values.
		 * @param s the query {@link State} for which the value should be returned.
		 * @return the max Q-value for all possible Q-values in the state.
		 */
		public static double maxQ(QProvider qSource, State s){
			List <QValue> qs = qSource.qValues(s);
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
		 * Returns the state value under a given policy for a state and {@link QProvider}.
		 * The value is the expected Q-value under the input policy action distribution. If no actions are permissible in the input state, then zero is returned.
		 * @param qSource the {@link QProvider} capable of producing Q-values.
		 * @param s the query {@link State} for which the value should be returned.
		 * @param p the policy defining the action distribution.
		 * @return the expected Q-value under the input policy action distribution
		 */
		public static double policyValue(QProvider qSource, State s, EnumerablePolicy p){

			double expectedValue = 0.;
			List <ActionProb> aps = p.policyDistribution(s);
			if(aps.isEmpty()){
				return 0.;
			}
			for(ActionProb ap : aps){
				double q = qSource.qValue(s, ap.ga);
				expectedValue += q * ap.pSelection;
			}
			return expectedValue;
		}


	}

}
