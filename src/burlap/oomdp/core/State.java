package burlap.oomdp.core;

import java.util.*;

import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * State objects are a collection of Object Instances.
 * @author James MacGlashan
 *
 */
public interface State {

	
	/**
	 * Returns a copy of this state, if mutable the copy should be deep.
	 * @return a copy of this state.
	 */
	State copy();
	
	/**
	 * Performs a semi-deep copy of the state in which only the objects with the names in deepCopyObjectNames are deep copied and the rest of the
	 * objects are shallowed copied.
	 * @param deepCopyObjectNames the names of the objects to be deep copied.
	 * @return a new state that is a mix of a shallow and deep copy of this state.
	 */
	State semiDeepCopy(String...deepCopyObjectNames);
	
	
	/**
	 * Performs a semi-deep copy of the state in which only the objects in deepCopyObjects are deep copied and the rest of the
	 * objects are shallowed copied.
	 * @param deepCopyObjects the objects to be deep copied
	 * @return a new state that is a mix of a shallow and deep copy of this state.
	 */
	State semiDeepCopy(ObjectInstance...deepCopyObjects);
	
	
	/**
	 * Performs a semi-deep copy of the state in which only the objects in deepCopyObjects are deep copied and the rest of the
	 * objects are shallowed copied.
	 * @param deepCopyObjects the objects to be deep copied
	 * @return a new state that is a mix of a shallow and deep copy of this state.
	 */
	State semiDeepCopy(Set<ObjectInstance> deepCopyObjects);
	
	/**
	 * Adds object instance o to this state.
	 * @param o the object instance to be added to this state.
	 * @return the modified state
	 */
	State addObject(ObjectInstance o);
	
	/**
	 * Adds the collection of objects to the state
	 * @param objects
	 * @return the modified state
	 */
	State addAllObjects(Collection<ObjectInstance> objects);
	/**
	 * Removes the object instance with the name oname from this state.
	 * @param oname the name of the object instance to remove.
	 * @return the modified state
	 */
	State removeObject(String oname);
	
	
	/**
	 * Removes the object instance o from this state.
	 * @param o the object instance to remove from this state.
	 * @return the modified state
	 */
	State removeObject(ObjectInstance o);
	
	/**
	 * Removes the collection of objects from the state
	 * @param objects
	 * @return the modified state
	 */
	State removeAllObjects(Collection<ObjectInstance> objects);
	/**
	 * Renames the identifier for the object instance currently named originalName with the name newName.
	 * @param originalName the original name of the object instance to be renamed in this state
	 * @param newName the new name of the object instance
	 * @return the modified state
	 */
	State renameObject(String originalName, String newName);
	
	/**
	 * Renames the identifier for object instance o in this state to newName.
	 * @param o the object instance to rename in this state
	 * @param newName the new name of the object instance
	 * @return the modified state
	 */
	State renameObject(ObjectInstance o, String newName);
	
	
	/**
	 * This method computes a matching from objects in the receiver to value-identical objects in the parameter state so. The matching
	 * is returned as a map from the object names in the receiving state to the matched objects in state so. If
	 * enforceStateExactness is set to true, then the returned matching will be an empty map if the two states
	 * are not OO-MDP-wise identical (i.e., if there is a not a bijection
	 *  between value-identical objects of the two states). If enforceExactness is false and the states are not identical,
	 *  the the method will return the largest matching between objects that can be made.
	 * @param so the state to whose objects the receiving state's objects should be matched
	 * @param enforceStateExactness whether to require that states are identical to return a matching
	 * @return a matching from this receiving state's objects to objects in so that have identical values. 
	 */
	Map <String, String> getObjectMatchingTo(State so, boolean enforceStateExactness);

	
	/**
	 * Define what equals means for two states
	 * @param other
	 * @return Whether the states are 'equal'
	 */
	boolean equals(Object other);
	
	/**
	 * Returns the number of observable and hidden object instances in this state.
	 * @return the number of observable and hidden object instances in this state.
	 */
	int numTotalObjects();
	
	/**
	 * Returns the number of observable object instances in this state.
	 * @return the number of observable object instances in this state.
	 */
	int numObservableObjects();
	
	/**
	 * Returns the number of hidden object instances in this state.
	 * @return the number of hideen object instances in this state.
	 */
	int numHiddenObjects();
	
	
	/**
	 * Returns the object in this state with the name oname
	 * @param oname the name of the object instance to return
	 * @return the object instance with the name oname or null if there is no object in this state named oname
	 */
	ObjectInstance getObject(String oname);
	
	/**
	 * Returns the list of observable object instances in this state.
	 * @return the list of observable object instances in this state.
	 */
	List <ObjectInstance> getObservableObjects();
	
	
	/**
	 * Returns the list of hidden object instances in this state.
	 * @return the list of hidden object instances in this state.
	 */
	List <ObjectInstance> getHiddenObjects();
	
	
	/**
	 * Returns the list of observable and hidden object instances in this state.
	 * @return the list of observable and hidden object instances in this state.
	 */
	List <ObjectInstance> getAllObjects();
	
	
	/**
	 * Returns all objects that belong to the object class named oclass
	 * @param oclass the name of the object class for which objects should be returned
	 * @return all objects that belong to the object class named oclass
	 */
	List <ObjectInstance> getObjectsOfClass(String oclass);
	
	
	/**
	 * Returns the first indexed object of the object class named oclass
	 * @param oclass the name of the object class for which the first indexed object should be returned.
	 * @return the first indexed object of the object class named oclass
	 */
	ObjectInstance getFirstObjectOfClass(String oclass);
	
	/**
	 * Returns a set of of the object class names for all object classes that have instantiated objects in this state.
	 * @return a set of of the object class names for all object classes that have instantiated objects in this state.
	 */
	Set <String> getObjectClassesPresent();
	
	
	/**
	 * Returns a list of list of object instances, grouped by object class
	 * @return a list of list of object instances, grouped by object class
	 */
	List <List <ObjectInstance>> getAllObjectsByTrueClass();
	
	
	/**
	 * Returns a string representation of this state using only observable object instances.
	 * @return a string representation of this state using only observable object instances.
	 */
	String getStateDescription();

	/**
	 * Returns a string representation of this state using observable and hidden object instances.
	 * @return a string representation of this state using observable and hidden object instances.
	 */
	String getCompleteStateDescription();


	/**
	 * Returns a mapping from object instance names to the list of attributes names that have unset values.
	 * @return a mapping from object instance names to the list of attributes names that have unset values.
	 */
	Map<String, List<String>> getAllUnsetAttributes();
	

	/**
	 * Returns a string description of the state with unset attribute values listed as null.
	 * @return a string description of the state with unset attribute values listed as null.
	 */
	String getCompleteStateDescriptionWithUnsetAttributesAsNull();
	
	
	/**
	 * Given an array of parameter object classes and an array of their corresponding parameter order groups,
	 * returns all possible object instance bindings to the parameters, excluding bindings that are equivalent due
	 * to the parameter order grouping.
	 * @param paramClasses the name of object classes to which the bound object instances must belong
	 * @param paramOrderGroups the parameter order group names.
	 * @return A list of all possible object instance bindings for the parameters, were a binding is represented by a list of object instance names
	 */
	List <List <String>> getPossibleBindingsGivenParamOrderGroups(String [] paramClasses, String [] paramOrderGroups);

	
		
}
