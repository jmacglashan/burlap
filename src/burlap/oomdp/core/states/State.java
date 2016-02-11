package burlap.oomdp.core.states;

import burlap.oomdp.core.objects.ObjectInstance;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A State instance is used to define the state of an environment or an observation from the environment. This interface
 * provides common methods for working with states that are represented with the
 * the OO-MDP paradigm in which states are a collection of objects (defined with the {@link burlap.oomdp.core.objects.ObjectInstance}
 * interface). Each object has its own value assignment to a set of attributes. OO-MDPs can represent
 * a very large range of different kinds of MDP problems and so you can typically formulate any MDP state as an OO-MDP
 * state. Two standard implementations of State include {@link burlap.oomdp.core.states.MutableState} and
 * {@link burlap.oomdp.core.states.ImmutableState} which store state information as explicit collections of
 * {@link burlap.oomdp.core.objects.MutableObjectInstance} and {@link burlap.oomdp.core.objects.ImmutableObjectInstance} objects.
 * However, in special cases and for efficiency reasons, you may want make a custom implementation of {@link burlap.oomdp.core.states.State}
 * that maintains state information in a different way. For full compatibility with all BURLAP tools, like the {@link burlap.oomdp.visualizer.Visualizer},
 * and for making using of the existing {@link burlap.oomdp.statehashing.HashableStateFactory} implementations, you should make
 * sure you implement the OO-MDP methods of this interface, even if your {@link burlap.oomdp.core.states.State} implementation
 * does not explicitly store some collection of {@link burlap.oomdp.core.objects.ObjectInstance} elements. However,
 * if you are willing to write your own implementations of those tools for a custom {@link burlap.oomdp.core.states.State}
 * implementation that does not implement all the methods, you can do so. For example, If your {@link burlap.oomdp.core.states.State}
 * implementation does not implement all OO-MDP methods, you can write your own {@link burlap.oomdp.statehashing.HashableStateFactory}
 * to work with it and then use any BURLAP tabular planning or learning algorithm, like {@link burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar},
 * {@link burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration}, etc.
 *
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
	
	<T> State setObjectsValue(String objectName, String attName, T value);
	
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
	 * Returns the number of object instances in this state.
	 * @return the number of object instances in this state.
	 */
	int numTotalObjects();

	
	
	/**
	 * Returns the object in this state with the name oname
	 * @param oname the name of the object instance to return
	 * @return the object instance with the name oname or null if there is no object in this state named oname
	 */
	ObjectInstance getObject(String oname);
	
	
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
	List <List <ObjectInstance>> getAllObjectsByClass();


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
	 * Returns a string description of the state with unset attribute values listed as null. This avoids
	 * runtime exceptions when attributes are unset and informs you which they are.
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
