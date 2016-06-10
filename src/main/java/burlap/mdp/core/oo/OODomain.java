package burlap.mdp.core.oo;

import burlap.mdp.core.Domain;
import burlap.mdp.core.oo.propositional.PropositionalFunction;

import java.util.List;

/**
 * A {@link Domain} for OO-MDP problems.
 * @author James MacGlashan.
 */
public interface OODomain extends Domain {

	/**
	 * Returns the Java classes used to define OO-MDP object classes.
	 * @return the Java classes used to define OO-MDP object classes.
	 */
	List<Class<?>> stateClasses();


	/**
	 * Returns the Java class used to define an OO-MDP object class with the given name. Note that the
	 * OO-MDP class name does not have to match the Java class name.
	 * @param className the name of the OO-MDP class.
	 * @return the Java class used to define an OO-MDP object class with the given name
	 */
	Class<?> stateClass(String className);

	/**
	 * Adds the Java class definition for an OO-MDP class with the given name
	 * @param className the OO-MDP class name
	 * @param stateClass the Java class used to define it
	 * @return this object to support the builder paradigm
	 */
	OODomain addStateClass(String className, Class<?> stateClass);


	/**
	 * Returns a list of the propositional functions that define this domain. Modifying the returned list
	 * will not alter the list of propositional functions that define this domain, because it returns a
	 * shallow copy. Modifying the propositional functions in the returned list will, however,
	 * modify the propositional functions in this domain.
	 * @return a list of the propositional functions that define this domain
	 */
	List <PropositionalFunction> propFunctions();


	/**
	 * Returns the {@link PropositionalFunction} with the given name
	 * @param name the name of the {@link PropositionalFunction}
	 * @return the {@link PropositionalFunction} with the given name
	 */
	PropositionalFunction propFunction(String name);


	/**
	 * Add a propositional function that can be used to evaluate objects that belong to object classes
	 * of this domain. The function will not be added if this domain already has a instance with the same name.
	 * @param prop the propositional function to add.
	 * @return this {@link OODomain}
	 */
	OODomain addPropFunction(PropositionalFunction prop);


	class Helper{

		/**
		 * Adds all {@link PropositionalFunction} objects to the given {@link OODomain}
		 * @param domain the receiving {@link OODomain}
		 * @param pfs a list of the {@link PropositionalFunction}s to add.
		 */
		public static void addPfsToDomain(OODomain domain, List<PropositionalFunction> pfs){
			for(PropositionalFunction pf : pfs){
				domain.addPropFunction(pf);
			}
		}

		/**
		 * Adds all {@link PropositionalFunction} objects to the given {@link OODomain}
		 * @param domain the receiving {@link OODomain}
		 * @param pfs the {@link PropositionalFunction}s to add.
		 */
		public static void addPfsToDomain(OODomain domain, PropositionalFunction...pfs){
			for(PropositionalFunction pf : pfs){
				domain.addPropFunction(pf);
			}
		}
	}


}
