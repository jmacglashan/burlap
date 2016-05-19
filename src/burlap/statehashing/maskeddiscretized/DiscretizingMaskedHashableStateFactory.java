package burlap.statehashing.maskeddiscretized;

import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.discretized.DiscretizingHashableStateFactory;
import burlap.statehashing.masked.MaskedHashableStateFactory;
import burlap.statehashing.simple.IDSimpleHashableState;
import burlap.statehashing.simple.IISimpleHashableState;
import burlap.statehashing.simple.SimpleHashableStateFactory;

/**
 * A class for producing {@link burlap.statehashing.HashableState} objects that computes hash codes and tests
 * for {@link State} equality by discretizing real-valued attributes and by masking (ignoring)
 * either state variables and/or {@link burlap.mdp.core.oo.state.OOState} clasees. For more information
 * on how discretization is performed, see the {@link DiscretizingHashableStateFactory}
 * class documentation and for more information on how
 * masking is performed see the {@link MaskedHashableStateFactory} class
 * documentation.
 * <p>
 * This class extends {@link SimpleHashableStateFactory}, which means it can be toggled to
 * to be object identifier/name independent or dependent and can be set to use {@link burlap.statehashing.HashableState}
 * instances that cache their hash code or not. See the {@link SimpleHashableStateFactory}
 * class documentation for more information on those features.
 * @author James MacGlashan.
 */
public class DiscretizingMaskedHashableStateFactory extends SimpleHashableStateFactory {


	DiscMaskedConfig config;


	/**
	 * Initializes with object identifier independence, no hash code caching and object class or attribute masks.
	 * @param defaultMultiple The default multiple to use for any continuous variables that have not been specifically set. The default is 1.0, which
	 * corresponds to integer floor discretization.
	 */
	public DiscretizingMaskedHashableStateFactory(double defaultMultiple) {
		config = new DiscMaskedConfig(defaultMultiple);
	}


	/**
	 * Initializes with non hash code caching and object class or attribute masks
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 * @param defaultMultiple The default multiple to use for any continuous variables that have not been specifically set. The default is 1.0, which
	 * corresponds to integer floor discretization.
	 */
	public DiscretizingMaskedHashableStateFactory(boolean identifierIndependent, double defaultMultiple) {
		super(identifierIndependent);
		config = new DiscMaskedConfig(defaultMultiple);
	}


	/**
	 * Initializes with a specified attribute or object class mask.
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 * @param defaultMultiple The default multiple to use for any continuous attributes that have not been specifically set.
	 * @param maskNamesAreForVariables whether the specified masks are masks for variables or object classes. True for variables, false for object classes.
	 * @param masks the names of the variable keys or object classes that will be masked (ignored from state hashing and equality checks)
	 */
	public DiscretizingMaskedHashableStateFactory(boolean identifierIndependent, double defaultMultiple, boolean maskNamesAreForVariables, String... masks) {
		super(identifierIndependent);
		config = new DiscMaskedConfig(defaultMultiple);
		if(maskNamesAreForVariables){
			for(String mask : masks){
				config.maskedVariables.add(mask);
			}
		}
		else{
			for(String mask : masks){
				config.maskedObjectClasses.add(mask);
			}
		}
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
			return new IIDiscMaskedHashableState(s, config);
		}
		return new IDDiscMaskedHashableState(s, config);
	}
}
