package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.State;
/**
 * Class to model effects under the OOMDP model
 * @author Dhershkowitz
 * 
 */
public abstract class Effect {
		 
	
	protected ObjectClass objectClassEffected;
	protected Attribute atEffected;
	
	/**
	 * 
	 * @return the ObjectClass that this effect acts upon
	 */
	public ObjectClass getObjectClassEffected() {
		return this.objectClassEffected;
	}
	
	/**
	 * 
	 * @return the Attribute that this effect acts upon
	 */
	public Attribute getAttributeAffected() {
		return this.atEffected;
	}
	
	/**
	 * 
	 * @param objectClassEffected the ObjectClass for the effect to act on
	 * @param atEffected the Attribute for the effect to act on
	 */
	public Effect(ObjectClass objectClassEffected, Attribute atEffected) {
		this.objectClassEffected = objectClassEffected;
		this.atEffected = atEffected;
	}
		
	/**
	 * 
	 * @param state the state to apply the the effect to
	 * @return the state resulting from applying the effect
	 */
	public abstract State applyEffect(State state);
	
	@Override
	public String toString() {
		return "Effect on " + atEffected.name + " of " + objectClassEffected.name + "s";
	}
	
	public boolean isNullEffect() {
		return false;
	}
	
	public boolean actOnTheSameObjectClassAndAttribute(Effect otherEffect) {
		return this.getObjectClassEffected().equals(otherEffect.getObjectClassEffected()) &&
				this.getAttributeAffected().equals(otherEffect.getAttributeAffected());
	}
	
}
