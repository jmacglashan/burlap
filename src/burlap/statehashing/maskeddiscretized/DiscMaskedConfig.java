package burlap.statehashing.maskeddiscretized;

import burlap.statehashing.masked.MaskedConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author James MacGlashan.
 */
public class DiscMaskedConfig extends MaskedConfig{

	/**
	 * The multiples to use for specific attributes
	 */
	public Map<Object, Double> keyWiseMultiples = new HashMap<Object, Double>();

	/**
	 * The default multiple to use for any continuous attributes that have not been specifically set.
	 */
	public double defaultMultiple;


	public DiscMaskedConfig() {
	}

	public DiscMaskedConfig(double defaultMultiple) {
		this.defaultMultiple = defaultMultiple;
	}

	public DiscMaskedConfig(Set<Object> maskedVariables, Set<String> maskedObjectClasses, Map<Object, Double> keyWiseMultiples, double defaultMultiple) {
		super(maskedVariables, maskedObjectClasses);
		this.keyWiseMultiples = keyWiseMultiples;
		this.defaultMultiple = defaultMultiple;
	}

	/**
	 * Sets the multiple to use for discretization for the given key. See the class documentation
	 * for more information on how the multiple works.
	 * @param key the name of the state variable key whose discretization multiple is being set.
	 * @param nearestMultipleValue the multiple to which values are floored.
	 */
	public void addFloorDiscretizingMultipleFor(Object key, double nearestMultipleValue){
		this.keyWiseMultiples.put(key, nearestMultipleValue);
	}


	/**
	 * Sets the default multiple to use for continuous values that do not have specific multiples set
	 * for them. See the documentation
	 * of this class for more information on how the multiple works. In short, continuous values will be floored
	 * to the greatest value that is a multiple of the multiple given and less than or equal to the true value.
	 * @param defaultMultiple the default multiple to which values are floored
	 */
	public void setDefaultFloorDiscretizingMultiple(double defaultMultiple){
		this.defaultMultiple = defaultMultiple;
	}

}
