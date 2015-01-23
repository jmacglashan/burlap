package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effect;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class AssigmentEffect extends Effect {

	private double valueToChangeBy;

	public double getValueToChangeBy() {
		return this.valueToChangeBy;
	}
	
	public AssigmentEffect(ObjectClass oClassEffected, Attribute atEffected, double valueToChangeBy) {
		super(oClassEffected, atEffected);
		this.valueToChangeBy = valueToChangeBy;
	}

	
	@Override
	public State applyEffect(State iState) {
		State toReturn = iState.copy();
		
		for (ObjectInstance o: toReturn.getObjectsOfTrueClass(this.objectClassEffected.name)) {
				o.setValue(this.atEffected.toString(), valueToChangeBy);
		}
		
		return toReturn;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AssigmentEffect && 
				((ArithmeticEffect) o).getObjectClassEffected().equals(this.objectClassEffected) &&
				((ArithmeticEffect) o).getAtEffected().equals(this.atEffected) &&
				((ArithmeticEffect) o).getValueToChangeBy() == this.valueToChangeBy) {
			return true;
		}
		return false;
	}

}
