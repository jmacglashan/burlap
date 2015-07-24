package burlap.behavior.valuefunction;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;


/**
 * This interface may be used by planning and learning algorithms that require an initialization value for the Q-value function or the value function.
 * A common implementation for initializing all values to the same constant is provided. This class extends the
 * {@link ValueFunction} class, so the initialization for the value function.
 * may be retrieved with the standard {@link ValueFunction#value(burlap.oomdp.core.states.State)} method.
 * It also adds a {@link #qValue(burlap.oomdp.core.states.State, burlap.oomdp.core.AbstractGroundedAction)} method for initializing
 * Q-values.
 * @author James MacGlashan
 *
 */
public interface ValueFunctionInitialization extends ValueFunction {


	/**
	 * Returns the initialization value of the Q-value function for a given state and action pair.
	 * @param s the state for which to get the initial value of the Q-value function.
	 * @param a the action for which to get the initial value of the Q-value function.
	 * @return the initialization value of the Q-value function for a given state and action pair.
	 */
	public double qValue(State s, AbstractGroundedAction a);

	
	
	
	
	/**
	 * A {@link ValueFunctionInitialization} implementation that always returns a constant value.
	 * @author James MacGlashan
	 *
	 */
	public class ConstantValueFunctionInitialization implements ValueFunctionInitialization{

		/**
		 * The constant value to return for all initializations.
		 */
		public double value = 0;
		
		
		/**
		 * Will cause this object to return 0 for all initialization values.
		 */
		public ConstantValueFunctionInitialization(){
			//defaults value to zero
		}
		
		
		/**
		 * Will cause this object to return <code>value</code> for all initialization values.
		 * @param value the value to return for all initializations.
		 */
		public ConstantValueFunctionInitialization(double value){
			this.value = value;
		}
		
		@Override
		public double value(State s) {
			return value;
		}

		@Override
		public double qValue(State s, AbstractGroundedAction a) {
			return value;
		}
		
		
		
		
	}
	
}
