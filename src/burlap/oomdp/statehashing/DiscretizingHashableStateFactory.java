package burlap.oomdp.statehashing;

import burlap.oomdp.core.values.Value;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * A factory for producing {@link burlap.oomdp.statehashing.HashableState} objects that computes hash codes
 * and test for state equality after discretizing any real values. Discretizing is performed by flooring
 * real values to the nearest user-defined multiple. For example, if the multiple is set to 0.5, then 9.73
 * would become 9.5, and 102.3 would become 102. Note using a
 * multiple value of 1.0 is equivalent to floor values to their corresponding int value.
 * Large multiple values result in course discretization and small
 * multiple values result in a fine discretization.
 * <br/><br/>
 * The multiple used can be specified for individual attributes so that different attributes have different degrees of discretization. To set
 * the multiple used for an specific attribute, use the {@link #addFloorDiscretizingMultipleFor(String, double)} method. When a continuous attribute is to be
 * hashed or compared, it is first checked if there has been a specific multiple value set for it. If so, that multiple is used for discretization. If not,
 * the default multiple is used. The default multiple may be set
 * though the constructors or by
 * using the {@link #setDefaultFloorDiscretizingMultiple(double)} method.
 * <br/><br/>
 * This class extends {@link burlap.oomdp.statehashing.SimpleHashableStateFactory}, which means it can be toggled to
 * to be object identifier/name independent or dependent and can be set to use {@link burlap.oomdp.statehashing.HashableState}
 * instances that hash their hash code or not. See the {@link burlap.oomdp.statehashing.SimpleHashableStateFactory}
 * class documentation for more information on those features.
 *
 * @author James MacGlashan.
 */
public class DiscretizingHashableStateFactory extends SimpleHashableStateFactory{

	/**
	 * The multiples to use for specific attributes
	 */
	protected Map<String, Double> attributeWiseMultiples = new HashMap<String, Double>();

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
	 * @param useCached if true then the hash code for each produced {@link burlap.oomdp.statehashing.HashableState} will be cached; if false then they will not be cached.
	 * @param defaultMultiple The default multiple to use for any continuous attributes that have not been specifically set.
	 */
	public DiscretizingHashableStateFactory(boolean identifierIndependent, boolean useCached, double defaultMultiple) {
		super(identifierIndependent, useCached);
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
