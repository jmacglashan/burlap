package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects;

/**
 * An exception to be thrown when an effect is instantiated by string name but there is no effect by that name (as far as EffectHelpers knows)
 * @author Dhershkowitz
 *
 */
public class EffectNotFoundException extends Exception  {
	
	String effectNotFound;
	
	/**
	 * 
	 * @param effectName the string name of the effect that could not be found
	 */
	public EffectNotFoundException(String effectName) {
		this.effectNotFound = effectName;
	}
	
	@Override
	public String toString() {
		return "Effect not found -- " + this.effectNotFound;
	}
}
