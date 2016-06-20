package burlap.statehashing.discretized;

import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.simple.IDSimpleHashableState;
import burlap.statehashing.simple.IISimpleHashableState;
import burlap.statehashing.simple.SimpleHashableStateFactory;

/**
 * A factory for producing {@link burlap.statehashing.HashableState} objects that computes hash codes
 * and test for state equality after discretizing any real values (Float or Double). Discretizing is performed by flooring
 * real values to the nearest user-defined multiple. For example, if the multiple is set to 0.5, then 9.73
 * would become 9.5, and 102.3 would become 102. Note using a
 * multiple value of 1.0 is equivalent to floor values to their corresponding int value.
 * Large multiple values result in course discretization and small
 * multiple values result in a fine discretization.
 * <p>
 * The multiple used can be specified for individual variables so that different variables have different degrees of discretization. To set
 * the multiple used for a specific attribute, use the {@link #addFloorDiscretizingMultipleFor(Object, double)} method. When a real value is to be
 * hashed or compared, it is first checked if there has been a variable-specific discretization set for it. If not,
 * the default multiple is used. The default multiple may be set
 * through the constructors or by
 * using the {@link #setDefaultFloorDiscretizingMultiple(double)} method.
 * <p>
 * This class extends {@link SimpleHashableStateFactory}, which means it can be toggled to
 * to be object identifier independent or dependent for {@link burlap.mdp.core.oo.state.OOState}s.
 *
 * @author James MacGlashan.
 */
public class DiscretizingHashableStateFactory extends SimpleHashableStateFactory{


	/**
	 * The discretization config
	 */
	protected DiscConfig config;


	/**
	 * Initializes with object identifier independence and no hash code caching.
	 * @param defaultMultiple The default multiple to use for any continuous attributes that have not been specifically set.
	 */
	public DiscretizingHashableStateFactory(double defaultMultiple) {
		this.config = new DiscConfig(defaultMultiple);
	}


	/**
	 * Initializes with non hash code caching
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 * @param defaultMultiple The default multiple to use for any continuous attributes that have not been specifically set.
	 */
	public DiscretizingHashableStateFactory(boolean identifierIndependent, double defaultMultiple) {
		super(identifierIndependent);
		config = new DiscConfig(defaultMultiple);
	}


	/**
	 * Sets the multiple to use for discretization for the given key. See the class documentation
	 * for more information on how the multiple works.
	 * @param key the name of the state variable key whose discretization multiple is being set.
	 * @param nearestMultipleValue the multiple to which values are floored.
	 */
	public void addFloorDiscretizingMultipleFor(Object key, double nearestMultipleValue){
		config.keyWiseMultiples.put(key, nearestMultipleValue);
	}


	/**
	 * Sets the default multiple to use for continuous values that do not have specific multiples set
	 * for them. See the documentation
	 * of this class for more information on how the multiple works. In short, continuous values will be floored
	 * to the greatest value that is a multiple of the multiple given and less than or equal to the true value.
	 * @param defaultMultiple the default multiple to which values are floored
	 */
	public void setDefaultFloorDiscretizingMultiple(double defaultMultiple){
		config.defaultMultiple = defaultMultiple;
	}

	@Override
	public HashableState hashState(State s) {
		if(s instanceof IISimpleHashableState || s instanceof IDSimpleHashableState){
			return (HashableState)s;
		}

		if(identifierIndependent){
			return new IIDiscHashableState(s, config);
		}
		return new IDDiscHashableState(s, config);
	}
}
