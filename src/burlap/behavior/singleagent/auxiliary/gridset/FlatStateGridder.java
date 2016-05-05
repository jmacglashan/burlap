package burlap.behavior.singleagent.auxiliary.gridset;

import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;

import java.util.*;

/**
 * This class is used to generate a set of continuous states that are spaced over grid points. The grid dimensions are specified
 * using the {@link #gridDimension(Object, VariableGridSpec)} or {@link #gridDimension(Object, double, double, int)}
 * method. Gridding is performed by manipulating (and copying) an input {@link MutableState}. If the input state
 * contains state variables that do not have a grid spec, then those values will hold constant.
 * @author James MacGlashan.
 */
public class FlatStateGridder {

	protected Map<Object, VariableGridSpec> gridSpecs = new HashMap<Object, VariableGridSpec>();

	/**
	 * Specify a state variable as a dimension of the grid
	 * @param varKey the variable key
	 * @param lowerVal the lower value of grid
	 * @param upperVal the upper value of the grid
	 * @param numGridPoints the number of grid points that spans the lower to upper bound
	 * @return this object, so that the builder paradigm can be used for specifying multiple dimensions.
	 */
	public FlatStateGridder gridDimension(Object varKey, double lowerVal, double upperVal, int numGridPoints){
		gridSpecs.put(varKey, new VariableGridSpec(lowerVal, upperVal, numGridPoints));
		return this;
	}

	/**
	 * Specify a state variable as a dimension of a the grid
	 * @param varKey the variable key
	 * @param gridSpec the grid specification
	 * @return this object, so that the builder paradigm can be used for specifying multiple dimensions.
	 */
	public FlatStateGridder gridDimension(Object varKey, VariableGridSpec gridSpec){
		gridSpecs.put(varKey, gridSpec);
		return this;
	}

	/**
	 * Returns the grid spec defined for the variable key
	 * @param varKey the variable key
	 * @return the grid spec
	 */
	public VariableGridSpec gridSpec(Object varKey){
		return this.gridSpecs.get(varKey);
	}

	/**
	 * Returns the set of all grid specs defined. The key in each returned entry is the variable key.
	 * @return the set of all grid specs defined.
	 */
	public Set<Map.Entry<Object, VariableGridSpec>> specs(){
		return this.gridSpecs.entrySet();
	}


	/**
	 * Grids the input state. Any variables in the state for which grid specifications have not be specified will be held
	 * constant in the set of returned states.
	 * @param s the input state to grid.
	 * @return the list of states spaced on the grid
	 */
	public List<State> gridState(MutableState s){
		s = (MutableState)s.copy();
		List<State> gridded = new ArrayList<State>();
		this.gridStateHelper(s, new ArrayList<Map.Entry<Object, VariableGridSpec>>(gridSpecs.entrySet()), 0, gridded);
		return gridded;
	}

	protected void gridStateHelper(MutableState s, List<Map.Entry<Object, VariableGridSpec>> gridDims, int index, List<State> createdStates){
		if(index == gridDims.size()){
			createdStates.add(s.copy());
		}
		else{
			Object key = gridDims.get(index).getKey();
			VariableGridSpec spec = gridDims.get(index).getValue();
			double cellWidth = spec.cellWidth();
			for(int i = 0; i < spec.numGridPoints; i++){
				double value = i*cellWidth + spec.lowerVal;
				s.set(key, value);
				this.gridStateHelper(s, gridDims, index+1, createdStates);
			}
		}
	}

}
