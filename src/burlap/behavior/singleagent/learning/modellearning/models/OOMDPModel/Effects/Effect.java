package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Value;

public abstract class Effect {

	protected ObjectClass objectClassEffected;
	protected Attribute atEffected;
	
	public ObjectClass getObjectClassEffected() {
		return this.objectClassEffected;
	}
	
	public Attribute getAtEffected() {
		return this.atEffected;
	}
	
	public Effect(ObjectClass objectClassEffected, Attribute atEffected) {
		this.objectClassEffected = objectClassEffected;
		this.atEffected = atEffected;
		
	}
		
	public abstract State applyEffect(State state);
	
	@Override
	public String toString() {
		return "Effect on " + atEffected.name + " of " + objectClassEffected.name + "s";
	}
	
}
