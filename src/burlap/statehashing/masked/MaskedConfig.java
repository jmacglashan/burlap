package burlap.statehashing.masked;

import burlap.mdp.core.state.State;

import java.util.HashSet;
import java.util.Set;

/**
 * @author James MacGlashan.
 */
public class MaskedConfig {

	public Set<Object> maskedVariables = new HashSet<Object>();
	public Set<String> maskedObjectClasses = new HashSet<String>();

	public MaskedConfig() {
	}

	public MaskedConfig(Set<Object> maskedVariables, Set<String> maskedObjectClasses) {
		this.maskedVariables = maskedVariables;
		this.maskedObjectClasses = maskedObjectClasses;
	}

	/**
	 * Adds masks for specific state variables. Mask keys should match what is returned by the {@link State#variableKeys()} method.
	 * @param masks keys of the state variables to mask
	 */
	public void addVariableMasks(Object...masks){
		for(Object mask : masks){
			this.maskedVariables.add(mask);
		}
	}

	/**
	 * Adds masks for entire OO-MDP objects that belong to the specified OO-MDP object class.
	 * @param masks the names of the object classes to mask.
	 */
	public void addObjectClassMasks(String...masks){
		for(String mask : masks){
			this.maskedObjectClasses.add(mask);
		}
	}


	/**
	 * Removes variable masks.
	 * @param masks variable keys for which masks should be removed
	 */
	public void removeAttributeMasks(Object...masks){
		for(Object mask : masks){
			this.maskedVariables.remove(mask);
		}
	}


	/**
	 * Removes masks for OO-MDP object classes
	 * @param masks the names object classes that will no longer be masked.
	 */
	public void removeObjectClassMasks(String...masks){
		for(String mask : masks){
			this.maskedObjectClasses.remove(mask);
		}
	}


	/**
	 * Clears all state variable masks.
	 */
	public void clearAllAttributeMasks(){
		this.maskedVariables.clear();
	}


	/**
	 * Clears all object class masks.
	 */
	public void clearAllObjectClassMasks(){
		this.maskedObjectClasses.clear();
	}

	public Set<Object> getMaskedVariables() {
		return maskedVariables;
	}

	public Set<String> getMaskedObjectClasses() {
		return maskedObjectClasses;
	}


	public MaskedConfig copy(){
		return new MaskedConfig(new HashSet<Object>(maskedVariables), new HashSet<String>(maskedObjectClasses));
	}

}
