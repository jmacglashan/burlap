package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.singleagent.GroundedAction;

public class AttActionEffectTuple {
	public ObjectClass oClass;
	public Attribute att;
	public GroundedAction ga;
	public String effectType;

	public AttActionEffectTuple(ObjectClass oClass, Attribute att, GroundedAction ga, String effectType) {
		this.oClass = oClass;
		this.att = att;
		this.ga = ga;
		this.effectType = effectType;
	}


	@Override 
	public int hashCode() {
		return oClass.hashCode() + att.hashCode() + ga.hashCode() + effectType.hashCode();
	}

	@Override
	public String toString() {
		return "(" + oClass.name + ", " + att.name + ", " + ga + effectType  + ", " + ")";
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof AttActionEffectTuple) {
			return ((AttActionEffectTuple) other).oClass.equals(this.oClass) &&
					((AttActionEffectTuple) other).att.equals(this.att) &&
					((AttActionEffectTuple) other).ga.equals(this.ga) &&
					((AttActionEffectTuple) other).effectType.equals(this.effectType);
		}
		return false;
	}

}
