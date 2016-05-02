package burlap.behavior.singleagent.auxiliary.gridset;

import burlap.oomdp.core.state.State;
import burlap.oomdp.core.oo.state.MutableOOState;
import burlap.oomdp.core.oo.state.OOState;
import burlap.oomdp.core.oo.state.OOVariableKey;
import burlap.oomdp.core.oo.state.ObjectInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to generate a set of continuous states that are spaced over grid points that are factored as and OO-MDP
 * using {@link OOState}.  This class will generate a grid along dimensions for objects in the state belonging to specified
 * classes. To set the dimensions of objects of a specific object class to be part of the grid, use the
 * {@link #gridObjectClass(String, FlatStateGridder)} method.
 * <p>
 * In principle, the {@link FlatStateGridder} could be used to accomplish the same effect, but grids would have to
 * be specified for every object-dimension pair. That is, if there are two objects of the same class, a {@link FlatStateGridder}
 * would require you independently set the grid specification for object. This class streamlines that process by
 * having you specify the gridding along each object class of interest, and it will automatically replicate
 * the grid specification for each object in the state that belongs to that class.
 * <p>
 * Grid a state with the {@link #gridState(MutableOOState)} method. If the state contains objects that belong to
 * classes that do not have grid specifications assigned for them, then they will be held constant in the returned
 * states.
 * @author James MacGlashan.
 */
public class OOStateGridder {

	protected Map<String, FlatStateGridder> classesToGrid = new HashMap<String, FlatStateGridder>();


	/**
	 * Specifies the gridding for a given object class.
	 * @param className the OO-MDP object class name
	 * @param gridder the gridding specification along objects of that class
	 * @return this object, so that the builder paradigm can be used for specifying multiple class griddings.
	 */
	public OOStateGridder gridObjectClass(String className, FlatStateGridder gridder){
		this.classesToGrid.put(className, gridder);
		return this;
	}


	/**
	 * Generates a set of states spaced along along. If there are objects in the input state for which a gridding
	 * has not been specified, objects of those classes will be held constant in the returned states over the grid.
	 * @param s the input state to grid
	 * @return a set of states spaced along along
	 */
	public List<State> gridState(MutableOOState s){

		//generate specs for all object-wise keys
		FlatStateGridder flatGridder = new FlatStateGridder();
		for(Map.Entry<String, FlatStateGridder> classGird : this.classesToGrid.entrySet()){

			List<ObjectInstance> objects = s.objectsOfClass(classGird.getKey());
			for(ObjectInstance o : objects){
				for(Map.Entry<Object, VariableGridSpec> spec : classGird.getValue().specs()){
					OOVariableKey okey = new OOVariableKey(o.name(), spec.getKey());
					flatGridder.gridDimension(okey, spec.getValue());
				}
			}

		}

		//then grid
		return flatGridder.gridState(s);

	}


}
