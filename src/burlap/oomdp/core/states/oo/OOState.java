package burlap.oomdp.core.states.oo;

import burlap.oomdp.core.states.State;

import java.util.List;

/**
 * This interface
 * provides common methods for working with states that are represented with the
 * the OO-MDP paradigm in which states are a collection of objects. Each object itself implements
 * {@link ObjectInstance} and is an
 * implementation of {@link State}, but otherwise is just a java object of your own definition, which makes defining
 * and OO-MDP object class the same procedure as defining a Java object class.
 *
 * @author James MacGlashan.
 */
public interface OOState extends State{


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
	ObjectInstance object(String oname);


	/**
	 * Returns the list of object instances in this state.
	 * @return the list object instances in this state.
	 */
	List<ObjectInstance> objects();


	/**
	 * Returns all objects that belong to the object class named oclass
	 * @param oclass the name of the object class for which objects should be returned
	 * @return all objects that belong to the object class named oclass
	 */
	List <ObjectInstance> objectsOfClass(String oclass);



}
