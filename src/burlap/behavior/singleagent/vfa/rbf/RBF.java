package burlap.behavior.singleagent.vfa.rbf;

import burlap.oomdp.core.State;

/**
 * An abstract class for defining RBF units. An RBF unit is defined by a centered state and a distance metric that can be used
 * to measure the distance between the unit's center and an input state. RBF units return a response value to an input state that is a
 * function of the distance from the unit's centered state to the input state. Different RBF subclasses implement the response function differently.
 * The less distant an input state is from a unit's center state, the greater the response value.
 * @author Anubhav Malhotra and Daniel Fernandez and Spandan Dutta
 *
 */
public abstract class RBF {
	
	/**
	 * The center state of this unit
	 */
	protected State centeredState;
	
	/**
	 * The distance metric used to compare input states to this unit's center state.
	 */
	protected DistanceMetric metric;
	

	/**
	 * Initializes with a center state for this unit and a distance metric to compare input states to it.
	 * @param centeredState the center state to use for this unit.
	 * @param metric the distance metric to use to compare this unit's center state to input states.
	 */
	public RBF(State centeredState, DistanceMetric metric){
		this.centeredState = centeredState;
		this.metric = metric;
	}
	
	
	/**
	 * Returns a response value to an input state that is a function of the distance between the input and this unit's center state.
	 * The less distant a query state is from this unit's center state, the greater the resposne value.
	 * @param input the input state for which a response value is returned.
	 * @return a response value to the given input state
	 */
	public abstract double responseFor(State input);
}
