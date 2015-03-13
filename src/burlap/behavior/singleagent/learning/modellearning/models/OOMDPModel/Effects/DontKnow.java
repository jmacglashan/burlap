package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.State;

public class DontKnow extends Effect {

	public DontKnow(String effectTypeStringName,
			ObjectClass objectClassEffected, Attribute atEffected) {
		super(effectTypeStringName, objectClassEffected, atEffected);
	}

	@Override
	public State applyEffect(State state) {
		return null;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof DontKnow) return true;
		return false;
	}
	
	@Override
	public boolean isDontKnow() {
		return true;
	}

}
