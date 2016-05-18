package burlap.statehashing.masked;

import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.simple.SimpleHashableStateFactory;

/**
 * This class produces {@link burlap.statehashing.HashableState} instances in which the hash code and equality
 * of the states masks (ignores) specified state variables. For {@link burlap.mdp.core.oo.state.OOState}s,
 * this class can also be specified to mask entire OO-MDP objects belonging to specified OO-MDP classes.
 * <p>
 * If masks are specified for variables, and the state is an {@link burlap.mdp.core.oo.state.OOState} then the
 * variables names specified for the masks are assumed to be on the object-level. Therefore, if two different objects
 * have the same set of variables keys, a single mask for the variable name key will mask variable values for all objects
 * that have that key.
 * <p>
 * This class extends {@link SimpleHashableStateFactory}, which means it can be toggled to
 * to be object identifier/name independent or dependent and can be set to use {@link burlap.statehashing.HashableState}
 * instances that cache their hash code or not. See the {@link SimpleHashableStateFactory}
 * class documentation for more information on those features.
 *
 * @author James MacGlashan.
 */
public class MaskedHashableStateFactory extends SimpleHashableStateFactory {

	protected MaskedConfig config;

	/**
	 * Default constructor: object identifier independent, no hash code caching, and no object class or attribute masks.
	 */
	public MaskedHashableStateFactory() {
		config = new MaskedConfig();
	}


	/**
	 * Initializes with no hash code caching and no object class or attribute masks.
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 */
	public MaskedHashableStateFactory(boolean identifierIndependent) {
		super(identifierIndependent);
		config = new MaskedConfig();
	}



	/**
	 * Initializes with a specified variable or object class mask.
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 * @param maskNamesAreForVariables whether the specified masks are masks for state variables or object classes. True for variables, false for object classes.
	 * @param masks the names of the state variables or OO-MDP object class that will be masked (ignored from state hashing and equality checks)
	 */
	public MaskedHashableStateFactory(boolean identifierIndependent, boolean maskNamesAreForVariables, String... masks) {
		super(identifierIndependent);
		config = new MaskedConfig();
		if(maskNamesAreForVariables){
			for(String mask : masks){
				config.maskedVariables.add(mask);
			}
		}
		else{
			for(String mask : masks){
				config.maskedObjectClasses.add(mask);
			}
		}
	}

	/**
	 * Adds masks for specific state variables. Mask keys should match what is returned by the {@link State#variableKeys()} method.
	 * @param masks keys of the state variables to mask
	 */
	public void addVariableMasks(Object...masks){
		for(Object mask : masks){
			config.maskedVariables.add(mask);
		}
	}

	/**
	 * Adds masks for entire OO-MDP objects that belong to the specified OO-MDP object class.
	 * @param masks the names of the object classes to mask.
	 */
	public void addObjectClassMasks(String...masks){
		for(String mask : masks){
			config.maskedObjectClasses.add(mask);
		}
	}


	/**
	 * Removes variable masks.
	 * @param masks variable keys for which masks should be removed
	 */
	public void removeAttributeMasks(Object...masks){
		for(Object mask : masks){
			config.maskedVariables.remove(mask);
		}
	}


	/**
	 * Removes masks for OO-MDP object classes
	 * @param masks the names object classes that will no longer be masked.
	 */
	public void removeObjectClassMasks(String...masks){
		for(String mask : masks){
			config.maskedObjectClasses.remove(mask);
		}
	}


	/**
	 * Clears all state variable masks.
	 */
	public void clearAllAttributeMasks(){
		config.maskedVariables.clear();
	}


	/**
	 * Clears all object class masks.
	 */
	public void clearAllObjectClassMasks(){
		config.maskedObjectClasses.clear();
	}


	public MaskedConfig getConfig() {
		return config;
	}

	public void setConfig(MaskedConfig config) {
		this.config = config;
	}

	@Override
	public HashableState hashState(State s) {
		if(s instanceof HashableState){
			return (HashableState)s;
		}

		if(identifierIndependent){
			return new IIMaskedHashableState(s, config);
		}
		return new IDMaskedHashableState(s, config);
	}
}
