package burlap.oomdp.statehashing;

import burlap.oomdp.core.values.Value;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * A class for producing {@link burlap.oomdp.statehashing.HashableState} objects that computes hash codes and tests
 * for {@link burlap.oomdp.core.states.State} equality by discretizing real-valued attributes and by masking (ignoring)
 * either {@link burlap.oomdp.core.Attribute}s and/or {@link burlap.oomdp.core.ObjectClass}es. For more information
 * on how discretization is performed, see the {@link DiscretizingHashableStateFactory}
 * class documentation and for more information on how {@link burlap.oomdp.core.Attribute}/{@link burlap.oomdp.core.ObjectClass}
 * masking is performed see the {@link MaskedHashableStateFactory} class
 * documentation.
 * <br/><br/>
 * This class extends {@link burlap.oomdp.statehashing.SimpleHashableStateFactory}, which means it can be toggled to
 * to be object identifier/name independent or dependent and can be set to use {@link burlap.oomdp.statehashing.HashableState}
 * instances that hash their hash code or not. See the {@link burlap.oomdp.statehashing.SimpleHashableStateFactory}
 * class documentation for more information on those features.
 * @author James MacGlashan.
 */
public class DiscretizingMaskedHashableStateFactory extends MaskedHashableStateFactory {

	/**
	 * The multiples to use for specific attributes
	 */
	protected Map<String, Double> attributeWiseMultiples = new HashMap<String, Double>();

	/**
	 * The default multiple to use for any continuous attributes that have not been specifically set.
	 */
	protected double defaultMultiple;




	/**
	 * Initializes with object identifier independence, no hash code caching and object class or attribute masks.
	 * @param defaultMultiple The default multiple to use for any continuous attributes that have not been specifically set. The default is 1.0, which
	 * corresponds to integer floor discretization.
	 */
	public DiscretizingMaskedHashableStateFactory(double defaultMultiple) {
		this.defaultMultiple = defaultMultiple;
	}


	/**
	 * Initializes with non hash code caching and object class or attribute masks
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 * @param defaultMultiple The default multiple to use for any continuous attributes that have not been specifically set. The default is 1.0, which
	 * corresponds to integer floor discretization.
	 */
	public DiscretizingMaskedHashableStateFactory(boolean identifierIndependent, double defaultMultiple) {
		super(identifierIndependent);
		this.defaultMultiple = defaultMultiple;
	}


	/**
	 * Initializes.
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 * @param useCached if true then the hash code for each produced {@link burlap.oomdp.statehashing.HashableState} will be cached; if false then they will not be cached.
	 * @param defaultMultiple The default multiple to use for any continuous attributes that have not been specifically set.
	 * corresponds to integer floor discretization.
	 */
	public DiscretizingMaskedHashableStateFactory(boolean identifierIndependent, boolean useCached, double defaultMultiple) {
		super(identifierIndependent, useCached);
		this.defaultMultiple = defaultMultiple;
	}


	/**
	 * Initializes with a specified attribute or object class mask.
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 * @param useCached if true then the hash code for each produced {@link burlap.oomdp.statehashing.HashableState} will be cached; if false then they will not be cached.
	 * @param defaultMultiple The default multiple to use for any continuous attributes that have not been specifically set.
	 * @param maskNamesAreForAttributes whether the specified masks are masks for attributes or object classes. True for attributes, false for object classes.
	 * @param masks the names of the {@link burlap.oomdp.core.Attribute}s or {@link burlap.oomdp.core.ObjectClass} that will be masks (ignored from state hashing and equality checks)
	 */
	public DiscretizingMaskedHashableStateFactory(boolean identifierIndependent, boolean useCached, double defaultMultiple, boolean maskNamesAreForAttributes, String... masks) {
		super(identifierIndependent, useCached, maskNamesAreForAttributes, masks);
		this.defaultMultiple = defaultMultiple;
	}

	/**
	 * Sets the multiple to use for discretization for the attribute with the specified name. See the documentation
	 * of this class for more information on how the multiple works. In short, continuous values will be floored
	 * to the greatest value that is a multiple of the multiple given and less than or equal to the true value.
	 * @param attributeName the name of the attribute whose discretization multiple is being set.
	 * @param nearestMultipleValue the multiple to which values are floored.
	 */
	public void addFloorDiscretizingMultipleFor(String attributeName, double nearestMultipleValue){
		this.attributeWiseMultiples.put(attributeName, nearestMultipleValue);
	}


	/**
	 * Sets the default multiple to use for continuous attributes that do not have specific multiples set
	 * for them. See the documentation
	 * of this class for more information on how the multiple works. In short, continuous values will be floored
	 * to the greatest value that is a multiple of the multiple given and less than or equal to the true value.
	 * @param defaultMultiple the default multiple to which values are floored
	 */
	public void setDefaultFloorDiscretizingMultiple(double defaultMultiple){
		this.defaultMultiple = defaultMultiple;
	}


	@Override
	protected void appendHashcodeForValue(HashCodeBuilder hashCodeBuilder, Value v) {
		AttClass attClass = getAttClass(v.getAttribute());
		if(attClass == AttClass.DOUBLE){
			Double mult = attributeWiseMultiples.get(v.attName());
			if(mult != null){
				hashCodeBuilder.append(intMultiples(mult, v.getRealVal()));
			}
			else{
				hashCodeBuilder.append(intMultiples(this.defaultMultiple, v.getRealVal()));
			}
		}
		else if(attClass == AttClass.DOUBLEARRAY){
			Double multPointer = attributeWiseMultiples.get(v.attName());
			double mult = multPointer == null ? this.defaultMultiple : multPointer;
			double [] vals = v.getDoubleArray();
			for(int i = 0; i < vals.length; i++){
				hashCodeBuilder.append(intMultiples(mult, vals[i]));
			}
		}
		else {
			super.appendHashcodeForValue(hashCodeBuilder, v);
		}
	}


	/**
	 * Returns whether two values are equal. If the values are real-valued, they are discretized before
	 * comparison.
	 * @param v1 the first value to compare
	 * @param v2 the second value to compare
	 * @return true if v1 = v2 after accounting for discretization; false otherwise.
	 */
	@Override
	protected boolean valuesEqual(Value v1, Value v2){
		//check if real valued or not
		AttClass attClass = this.getAttClass(v1.getAttribute());
		if(attClass == AttClass.DOUBLE){
			Double multP = attributeWiseMultiples.get(v1.attName());
			double mult = multP == null ? this.defaultMultiple : multP;
			return intMultiples(mult, v1.getRealVal()) == intMultiples(mult, v2.getRealVal());
		}
		else if(attClass == AttClass.DOUBLEARRAY) {
			Double multP = attributeWiseMultiples.get(v1.attName());
			double mult = multP == null ? this.defaultMultiple : multP;
			double [] array1 = v1.getDoubleArray();
			double [] array2 = v2.getDoubleArray();
			if(array1.length != array2.length){
				return  false;
			}
			for(int i = 0; i < array1.length; i++){
				if(intMultiples(mult, array1[i]) != intMultiples(mult, array2[i])){
					return false;
				}
			}
			return true;

		}
		else{
			return v1.equals(v2);
		}

	}

	/**
	 * Returns int result of num / mult; that is, (int)(num / mult).
	 * @param mult the multiple
	 * @param num the number
	 * @return the int result of num / mult
	 */
	protected static int intMultiples(double mult, double num){
		int div = (int)(num / mult);
		return div;
	}
}
