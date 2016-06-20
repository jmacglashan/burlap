package burlap.behavior.valuefunction;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

/**
 * A {@link QFunction} implementation that always returns a constant value. Useful for value function initialization.
 * @author James MacGlashan
 *
 */
public class ConstantValueFunction implements QFunction{

	/**
	 * The constant value to return for all initializations.
	 */
	public double value = 0;


	/**
	 * Will cause this object to return 0 for all initialization values.
	 */
	public ConstantValueFunction(){
		//defaults value to zero
	}


	/**
	 * Will cause this object to return <code>value</code> for all initialization values.
	 * @param value the value to return for all initializations.
	 */
	public ConstantValueFunction(double value){
		this.value = value;
	}

	@Override
	public double value(State s) {
		return value;
	}

	@Override
	public double qValue(State s, Action a) {
		return value;
	}




}
