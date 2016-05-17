package burlap.statehashing;

import burlap.mdp.core.state.State;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * A class for producing {@link burlap.statehashing.HashableState} objects that computes hash codes and tests
 * for {@link State} equality by discretizing real-valued attributes and by masking (ignoring)
 * either state variables and/or {@link burlap.mdp.core.oo.state.OOState} clasees. For more information
 * on how discretization is performed, see the {@link DiscretizingHashableStateFactory}
 * class documentation and for more information on how
 * masking is performed see the {@link MaskedHashableStateFactory} class
 * documentation.
 * <p>
 * This class extends {@link burlap.statehashing.SimpleHashableStateFactory}, which means it can be toggled to
 * to be object identifier/name independent or dependent and can be set to use {@link burlap.statehashing.HashableState}
 * instances that cache their hash code or not. See the {@link burlap.statehashing.SimpleHashableStateFactory}
 * class documentation for more information on those features.
 * @author James MacGlashan.
 */
public class DiscretizingMaskedHashableStateFactory extends MaskedHashableStateFactory {

	/**
	 * The multiples to use for specific variable keys
	 */
	protected Map<Object, Double> keyWiseMultiples = new HashMap<Object, Double>();

	/**
	 * The default multiple to use for any continuous variables that have not been specifically set.
	 */
	protected double defaultMultiple;




	/**
	 * Initializes with object identifier independence, no hash code caching and object class or attribute masks.
	 * @param defaultMultiple The default multiple to use for any continuous variables that have not been specifically set. The default is 1.0, which
	 * corresponds to integer floor discretization.
	 */
	public DiscretizingMaskedHashableStateFactory(double defaultMultiple) {
		this.defaultMultiple = defaultMultiple;
	}


	/**
	 * Initializes with non hash code caching and object class or attribute masks
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 * @param defaultMultiple The default multiple to use for any continuous variables that have not been specifically set. The default is 1.0, which
	 * corresponds to integer floor discretization.
	 */
	public DiscretizingMaskedHashableStateFactory(boolean identifierIndependent, double defaultMultiple) {
		super(identifierIndependent);
		this.defaultMultiple = defaultMultiple;
	}


	/**
	 * Initializes.
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 * @param useCached if true then the hash code for each produced {@link burlap.statehashing.HashableState} will be cached; if false then they will not be cached.
	 * @param defaultMultiple The default multiple to use for any continuous variables that have not been specifically set.
	 * corresponds to integer floor discretization.
	 */
	public DiscretizingMaskedHashableStateFactory(boolean identifierIndependent, boolean useCached, double defaultMultiple) {
		super(identifierIndependent, useCached);
		this.defaultMultiple = defaultMultiple;
	}


	/**
	 * Initializes with a specified attribute or object class mask.
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 * @param useCached if true then the hash code for each produced {@link burlap.statehashing.HashableState} will be cached; if false then they will not be cached.
	 * @param defaultMultiple The default multiple to use for any continuous attributes that have not been specifically set.
	 * @param maskNamesAreForVariables whether the specified masks are masks for variables or object classes. True for variables, false for object classes.
	 * @param masks the names of the variable keys or object classes that will be masked (ignored from state hashing and equality checks)
	 */
	public DiscretizingMaskedHashableStateFactory(boolean identifierIndependent, boolean useCached, double defaultMultiple, boolean maskNamesAreForVariables, String... masks) {
		super(identifierIndependent, useCached, maskNamesAreForVariables, masks);
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

		if(this.maskedVariables.contains(key)){
			return ; //no need to incorporate hash codes for masked values
		}

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
		if(this.maskedVariables.contains(key)){
			return true;
		}

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
