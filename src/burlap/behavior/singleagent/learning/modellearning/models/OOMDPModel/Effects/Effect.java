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
		 
	protected String effectTypeString;
	protected ObjectClass objectClassEffected;
	protected Attribute atEffected;
	
	
	/**
	 * 
	 * @return the string name of the effect type
	 */
	public String getEffectTypeString() {
		return this.effectTypeString;
	}
	
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
	public Effect(String effectTypeStringName, ObjectClass objectClassEffected, Attribute atEffected) {
		this.effectTypeString = effectTypeStringName;
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
	
	/**
	 * 
	 * @param otherEffect the other effect to compare against
	 * @return a boolean of whether or not these two effects act on the same attribute of the same object classes (they need not be of the same type)
	 */
	public boolean actOnTheSameObjectClassAndAttribute(Effect otherEffect) {
		return this.objectClassEffected.equals(otherEffect.getObjectClassEffected()) &&
				this.atEffected.equals(otherEffect.getAttributeAffected());
	}
	
}
