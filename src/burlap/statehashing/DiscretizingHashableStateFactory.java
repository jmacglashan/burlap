package burlap.statehashing;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

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
 * This class extends {@link burlap.statehashing.SimpleHashableStateFactory}, which means it can be toggled to
 * to be object identifier independent or dependent for {@link burlap.mdp.core.oo.state.OOState}s and to cache hash codes or not.
 * See the {@link burlap.statehashing.SimpleHashableStateFactory}
 * class documentation for more information on those features.
 *
 * @author James MacGlashan.
 */
public class DiscretizingHashableStateFactory extends SimpleHashableStateFactory{

	/**
	 * The multiples to use for specific attributes
	 */
	protected Map<Object, Double> keyWiseMultiples = new HashMap<Object, Double>();

	/**
	 * The default multiple to use for any continuous attributes that have not been specifically set.
	 */
	protected double defaultMultiple;


	/**
	 * Initializes with object identifier independence and no hash code caching.
	 * @param defaultMultiple The default multiple to use for any continuous attributes that have not been specifically set.
	 */
	public DiscretizingHashableStateFactory(double defaultMultiple) {
		this.defaultMultiple = defaultMultiple;
	}


	/**
	 * Initializes with non hash code caching
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 * @param defaultMultiple The default multiple to use for any continuous attributes that have not been specifically set.
	 */
	public DiscretizingHashableStateFactory(boolean identifierIndependent, double defaultMultiple) {
		super(identifierIndependent);
		this.defaultMultiple = defaultMultiple;
	}

	/**
	 * Initializes.
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 * @param useCached if true then the hash code for each produced {@link burlap.statehashing.HashableState} will be cached; if false then they will not be cached.
	 * @param defaultMultiple The default multiple to use for any continuous attributes that have not been specifically set.
	 */
	public DiscretizingHashableStateFactory(boolean identifierIndependent, boolean useCached, double defaultMultiple) {
		super(identifierIndependent, useCached);
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


	@Override
	protected void appendHashCodeForValue(HashCodeBuilder hashCodeBuilder, Object key, Object value) {

		Double mult = keyWiseMultiples.get(key);
		if(mult == null){
			mult = this.defaultMultiple;
		}

		if(value instanceof Double || value instanceof Float){
			hashCodeBuilder.append(intMultiples(mult, ((Number)value).doubleValue()));
		}
		else if(value.getClass().isArray()){
			if(value instanceof double[]){
				double [] vals = (double[])value;
				for(int i = 0; i < vals.length; i++){
					hashCodeBuilder.append(intMultiples(mult, vals[i]));
				}
			}
			else if(value instanceof float[]){
				float [] vals = (float[])value;
				for(int i = 0; i < vals.length; i++){
					hashCodeBuilder.append(intMultiples(mult, vals[i]));
				}
			}
			else{
				super.appendHashCodeForValue(hashCodeBuilder, key, value);
			}
		}
		else{
			super.appendHashCodeForValue(hashCodeBuilder, key, value);
		}

	}

	@Override
	protected boolean valuesEqual(Object key, Object v1, Object v2) {

		Double mult = keyWiseMultiples.get(key);
		if(mult == null){
			mult = this.defaultMultiple;
		}

		if(v1 instanceof Double || v1 instanceof Float){

			Double dv1 = ((Number)v1).doubleValue();
			Double dv2 = ((Number)v2).doubleValue();

			return intMultiples(mult, dv1) == intMultiples(mult, dv2);

		}
		else if(v1.getClass().isArray()){
			if(v1 instanceof double[]){
				double [] vals1 = (double[])v1;
				double [] vals2 = (double[])v2;
				if(vals1.length != vals2.length){
					return false;
				}
				for(int i = 0; i < vals1.length; i++){
					if(intMultiples(mult, vals1[i]) != intMultiples(mult, vals2[i])){
						return false;
					}
				}
				return true;
			}
			else if(v1 instanceof float[]){
				float [] vals1 = (float[])v1;
				float [] vals2 = (float[])v2;
				if(vals1.length != vals2.length){
					return false;
				}
				for(int i = 0; i < vals1.length; i++){
					if(intMultiples(mult, vals1[i]) != intMultiples(mult, vals2[i])){
						return false;
					}
				}
				return true;
			}
			else{
				return super.valuesEqual(key, v1, v2);
			}
		}
		else{
			return super.valuesEqual(key, v1, v2);
		}

	}


	/**
	 * Returns int result of num / mult; that is, (int)(num / mult).
	 * @param mult the multiple
	 * @param num the number
	 * @return the int result of num / mult
	 */
	protected static int intMultiples(double mult, double num){
		int div = (int)Math.floor(num / mult);
		return div;
	}

}
