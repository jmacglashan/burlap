package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

/**
 * Class to model assigment effects under the OOMDP model -- i.e. those that assign some real value to some attribute of some
 *  object class
 * @author Dhershkowitz
 *
 */
public class AssigmentEffect extends Effect {

	private double valueToSet;

	/**
	 * 
	 * @return
	 */
	public double getValueToSet() {
		return this.valueToSet;
	}
	
	public AssigmentEffect(ObjectClass oClassEffected, Attribute atEffected, double valueToChangeBy) {
		super(oClassEffected, atEffected);
		this.valueToSet = valueToChangeBy;
	}

	
	@Override
	public State applyEffect(State iState) {
		State toReturn = iState.copy();
		
		for (ObjectInstance o: toReturn.getObjectsOfTrueClass(this.objectClassEffected.name)) {
				o.setValue(this.atEffected.name, valueToSet);
		}
		
		return toReturn;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AssigmentEffect && 
				((AssigmentEffect) o).getObjectClassEffected().equals(this.objectClassEffected) &&
				((AssigmentEffect) o).getAttributeAffected().equals(this.atEffected) &&
				((AssigmentEffect) o).getValueToSet() == this.valueToSet) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "Assigmenteffect of setting value to " + this.valueToSet + " for " + atEffected.name + " of " + objectClassEffected.name + "s";
	}

}
