package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.singleagent.GroundedAction;

public class PaperAttributeActionTuple {
	public ObjectClass oClass;
	public Attribute att;
	public GroundedAction ga;
	
	public PaperAttributeActionTuple(ObjectClass oClass, Attribute att, GroundedAction ga) {
		this.oClass = oClass;
		this.att = att;
		this.ga = ga;
	}
	
	
	@Override 
	public int hashCode() {
		return oClass.hashCode() + att.hashCode() + ga.hashCode();
	}
	
	@Override
	public String toString() {
		return "(" + oClass.name + ", " + att.name + ", " + ga + ")";
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof PaperAttributeActionTuple) {
			return ((PaperAttributeActionTuple) other).oClass.equals(this.oClass) &&
					((PaperAttributeActionTuple) other).att.equals(this.att) &&
					((PaperAttributeActionTuple) other).ga.equals(this.ga);
		}
		return false;
	}
	
}
