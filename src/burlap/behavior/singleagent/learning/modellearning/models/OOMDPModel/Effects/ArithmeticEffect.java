package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class ArithmeticEffect extends Effect {

	private double valueToChangeBy;

	public double getValueToChangeBy() {
		return this.valueToChangeBy;
	}
	
	public ArithmeticEffect(ObjectClass oClassEffected, Attribute atEffected, double valueToChangeBy) {
		super(oClassEffected, atEffected);
		this.valueToChangeBy = valueToChangeBy;
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
				((ArithmeticEffect) o).getAtEffected().equals(this.atEffected) &&
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
