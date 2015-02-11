package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.State;

public class NullEffect extends Effect{

	public NullEffect(ObjectClass objectClassEffected, Attribute atEffected) {
		super(objectClassEffected, atEffected);
	}
	
	public State applyEffect(State state) {
		return state;
	}
	
	@Override
	public String toString() {
		return super.toString() + "(null effect)";
	}


}
