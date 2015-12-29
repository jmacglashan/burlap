package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.State;

public class NullEffect extends Effect {

	public NullEffect(String effectTypeStringName,
			ObjectClass objectClassEffected, Attribute atEffected) {
		super(effectTypeStringName, objectClassEffected, atEffected);
	}

	@Override
	public State applyEffect(State state) {
		return state;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof NullEffect) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isNullEffect() {
		return true;
	}
	
	@Override
	public String toString() {
		return "Null effect on " + this.atEffected.name + " of " + this.objectClassEffected.name;
	}

}
