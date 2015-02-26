package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Value;

/**
 * Class to model arithmetic effects under the OOMDP model -- i.e. effects that add some real value to the attributes of some object class 
 * @author Dhershkowitz
 *
 */
public class ArithmeticEffect extends Effect {

	private double valueToChangeBy;

	/**
	 * @param effectTypeString the string name of the effect type
	 * @param oClassEffected the ObjectClass that this ArithmeticEffect acts on
	 * @param atEffected the Attribute that this ArithmeticEffect acts on
	 * @param valueBefore the value of the class/att in s
	 * @param valueAfter the value of the class/att in sPrime
	 */
	public ArithmeticEffect(String effectTypeString, ObjectClass oClassEffected, Attribute atEffected, Value valueBefore, Value valueAfter) {
		super(effectTypeString, oClassEffected, atEffected);
		this.valueToChangeBy = valueAfter.getNumericRepresentation()-valueBefore.getNumericRepresentation();
	}
	
	/**
	 * 
	 * @return the real value that this ArithmeticEffect adds to its corresponding object class's attribute
	 */
	public double getValueToChangeBy() {
		return this.valueToChangeBy;
	}
	
	@Override
	public State applyEffect(State iState) {
		State toReturn = iState.copy();
		
		for (ObjectInstance o: toReturn.getObjectsOfTrueClass(this.objectClassEffected.name)) {
				double oldVal = o.getNumericValForAttribute(this.atEffected.name);
				o.setValue(this.atEffected.name, oldVal + valueToChangeBy);
		}
		
		return toReturn;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ArithmeticEffect && 
				((ArithmeticEffect) o).getObjectClassEffected().equals(this.objectClassEffected) &&
				((ArithmeticEffect) o).getAttributeAffected().equals(this.atEffected) &&
				((ArithmeticEffect) o).getValueToChangeBy() == this.valueToChangeBy) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "ArithmeticEffect of adding " + this.valueToChangeBy + " to " + atEffected.name + " of " + objectClassEffected.name + "s";
	}
	
}
