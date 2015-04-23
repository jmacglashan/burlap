package burlap.behavior.singleagent.auxiliary;

import burlap.oomdp.core.*;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;

import java.util.*;

/**
 *
 * This class is used for creating a grid of states over a state space domain. The client specifies which object classes
 * and which attributes of those attributes the grid should span using the method
 * {@link #setObjectClassAttributesToTile(String, burlap.behavior.singleagent.auxiliary.StateGridder.AttributeSpecification...)}.
 * That method takes as input the object class, and the list of {@link burlap.behavior.singleagent.auxiliary.StateGridder.AttributeSpecification}
 * objects. Each {@link burlap.behavior.singleagent.auxiliary.StateGridder.AttributeSpecification} object specifies the name
 * of an attribute, the lower limit value that the grid spans, the upper limit value that the grid spans, and how many grid points
 * will exist along that dimension. If you simply want a even number of grid points across the entire domain
 * of attributes for an {@link burlap.oomdp.core.ObjectClass} or all {@link burlap.oomdp.core.ObjectClass} objects,
 * consider using instead the {@link #gridEntireObjectClass(burlap.oomdp.core.ObjectClass, int)}} or
 * {@link #gridEntireDomainSpace(burlap.oomdp.core.Domain, int)} methods.
 * <p/>
 * After the desired grid specifications have been set, a list of states spanning a grid according to the specification
 * can be generated using the {@link #gridInputState(burlap.oomdp.core.states.State)} method. This method takes a source input
 * state and creates a grid from it. Objects classes and attributes that did not have grid specifications
 * defined for them will remain as constant objects/values in each of the returned states. The input state values
 * will remain unaffected by the gridding.
 * <p/>
 * Note that this gridder only works with attributes that are {@link burlap.oomdp.core.Attribute.AttributeType#REAL}
 * <p/>
 * Example usage with the {@link burlap.domain.singleagent.mountaincar.MountainCar} domain below.
 * <pre>
 * {@code
 *  MountainCar mc = new MountainCar();
 *  Domain domain = mc.generateDomain();
 *  State s = mc.getCleanState(domain);
 *
 *  gridder.gridEntireDomainSpace(domain, 3);
 *
 *  List<State> gridStates = gridder.gridInputState(s);
 *  System.out.println("Num states: " + gridStates.size());
 *  for(State g : gridStates){
 *  	System.out.println(g.toString());
 *  }
 * }
 * </pre>
 * <p/>
 * Alternatively, you may replace the grid specification with something more specific; e.g., to create a 4x3 grid:
 * <pre>
 * {@code
 * gridder.setObjectClassAttributesToTile(MountainCar.CLASSAGENT,
 *     new AttributeSpecification(domain.getAttribute(MountainCar.ATTX), 4),
 *     new AttributeSpecification(domain.getAttribute(MountainCar.ATTV), 3),
 * }
 * </pre>
 *
 * @author James MacGlashan.
 */
public class StateGridder {


	/**
	 * The attribtue grid specifications for each object class
	 */
	Map<String, AttributeSpecification[]> objectClassAttriutes = new HashMap<String, AttributeSpecification[]>();


	/**
	 * Sets the attribute specifications to use for a single {@link burlap.oomdp.core.ObjectClass}
	 * @param objectClassName the name of the {@link burlap.oomdp.core.ObjectClass}
	 * @param attSpecs the {@link burlap.behavior.singleagent.auxiliary.StateGridder.AttributeSpecification} objects defining how each attribute is gridded.
	 */
	public void setObjectClassAttributesToTile(String objectClassName, AttributeSpecification...attSpecs){
		this.objectClassAttriutes.put(objectClassName, attSpecs);
	}

	/**
	 * Grids an entire Object class object with each of its associated attributes having the grid span their whole domain
	 * with the same number of grid points.
	 * @param obClass the {@link burlap.oomdp.core.ObjectClass} to grid
	 * @param numGridPointsPerAttribute the number of grid points along each {@link Attribute} associated with the {@link burlap.oomdp.core.ObjectClass}.
	 */
	public void gridEntireObjectClass(ObjectClass obClass, int numGridPointsPerAttribute){
		AttributeSpecification [] specs = new AttributeSpecification[obClass.numAttributes()];
		for(int i = 0; i < obClass.numAttributes(); i++){
			specs[i] = new AttributeSpecification(obClass.attributeList.get(i), numGridPointsPerAttribute);
		}
		this.objectClassAttriutes.put(obClass.name, specs);
	}


	/**
	 * Grids an entire domain; that is each {@link burlap.oomdp.core.ObjectClass} associated with the domain will have each of its
	 * associated Attributes gridded (each spanning the specified number of grid points).
	 * @param domain the {@link burlap.oomdp.core.Domain} to grid.
	 * @param numGridPointsPerAttribute the number of grid points per attribute
	 */
	public void gridEntireDomainSpace(Domain domain, int numGridPointsPerAttribute){
		for(ObjectClass oc : domain.getObjectClasses()){
			this.gridEntireObjectClass(oc, numGridPointsPerAttribute);
		}
	}


	/**
	 * Creates a grid using the input state a source reference. If the state contains objects that belong to {@link burlap.oomdp.core.ObjectClass}
	 * that are not defined in this object's grid specification, that each of those objects will be constant objects in the gridded list
	 * of {@link burlap.oomdp.core.states.State} objects returned. Similarly, if objects have attribute that do not have a specification set for them,
	 * then they will remain as constant values.
	 * @param s the input state to use a reference for creating the grid. This state specifies the number of objects that needed to be gridded and any constant ungridded objects/attributes
	 * @return a {@link java.util.List} of {@link burlap.oomdp.core.states.State} objects that define the grid.
	 */
	public List<State> gridInputState(State s){

		//prevent contamination
		s = s.copy();

		//first thing we want to do is get the list of objects that need to be modified
		List<ObjectInstance> objectsToGrid = new ArrayList<ObjectInstance>(s.numObservableObjects());
		for(Map.Entry<String, AttributeSpecification[]> e : objectClassAttriutes.entrySet()){
			List<ObjectInstance> obs = s.getObjectsOfClass(e.getKey());
			objectsToGrid.addAll(obs);
		}

		//now get a clean state without those object instances, but leave non-modified objects intact
		for(ObjectInstance o : objectsToGrid){
			s.removeObject(o);
		}

		//now get all the possible gridded object values for the objects to modify
		List<List<ObjectInstance>> possibleGridVals = new ArrayList<List<ObjectInstance>>(objectsToGrid.size());
		for(ObjectInstance o  : objectsToGrid){
			List<ObjectInstance> griddedVals = new LinkedList<ObjectInstance>();
			this.objectGridder(o, this.objectClassAttriutes.get(o.getObjectClass().name), 0, griddedVals);
			possibleGridVals.add(new ArrayList<ObjectInstance>(griddedVals)); //convert to array list once size is known
		}

		//now we want to grid all objects and create a separate state for each
		int [] choices = new int [objectsToGrid.size()];
		List<State> states = new LinkedList<State>();
		this.stateGridder(s, possibleGridVals, 0, choices, states);

		//turn into array list for access
		return new ArrayList<State>(states);
	}



	private void stateGridder(State cleanState, List<List<ObjectInstance>> possibleGridVals, int index, int [] choices, List<State> states){
		if(index == choices.length){
			//done
			State toAdd = cleanState.copy();
			//add all the objects
			for(int i = 0; i < choices.length; i++){
				toAdd.addObject(possibleGridVals.get(i).get(choices[i]));
			}
			states.add(toAdd);
		}
		else{
			List<ObjectInstance> thisObsPossibleVals = possibleGridVals.get(index);
			for(int i = 0; i < thisObsPossibleVals.size(); i++){
				choices[index] = i;
				this.stateGridder(cleanState, possibleGridVals, index+1, choices, states);
			}
		}
	}


	private void objectGridder(ObjectInstance o, AttributeSpecification [] specs, int index, List<ObjectInstance> objects){
		if(index == specs.length){
			//done
			objects.add(o.copy());
		}
		else{
			AttributeSpecification spec = specs[index];
			double cellWidth = spec.cellWidth();
			for(int i = 0; i < spec.numGridPoints; i++){
				o.setValue(spec.attName, i*cellWidth + spec.lowerVal);
				this.objectGridder(o, specs, index+1, objects);
			}
		}
	}


	/**
	 * Class for specifying the grid along a single attribute. It contains the name of the attribute, the lower value
	 * on the grid, the upper value on the grid, and the number of grid points along that space. The grid points will
	 * always include the lower grid value and upper grid value, unless the number of grid points is 1.
	 */
	public static class AttributeSpecification{

		/**
		 * The name of the attribute.
		 */
		public String attName;

		/**
		 * The lower value of the attribute on the gird
		 */
		public double lowerVal;

		/**
		 * The upper value of the attribute on the grid
		 */
		public double upperVal;

		/**
		 * The number of grid points along this attribute
		 */
		public int numGridPoints;


		/**
		 * Initializes. The grid points will
		 * always include the lower grid value and upper grid value, unless the number of grid points is 1.
		 * @param attName The name of the attribute.
		 * @param lowerVal The lower value of the attribute on the gird
		 * @param upperVal The upper value of the attribute on the grid
		 * @param numGridPoints The number of grid points along this attribute
		 */
		public AttributeSpecification(String attName, double lowerVal, double upperVal, int numGridPoints){
			this.attName = attName;
			this.lowerVal = lowerVal;
			this.upperVal = upperVal;
			this.numGridPoints = numGridPoints;
		}

		/**
		 * Initializes with the lower and upper values of the grid being set to the {@link burlap.oomdp.core.Attribute} objects'
		 * full domain (i.e., its {@link burlap.oomdp.core.Attribute#lowerLim} and {@link burlap.oomdp.core.Attribute#upperLim}
		 * data members. The grid points will
		 * always include the lower grid value and upper grid value, unless the number of grid points is 1.
		 * @param att the attribute to grid.
		 * @param numGridPoints the number of grid poitns along this attribute.
		 */
		public AttributeSpecification(Attribute att, int numGridPoints){
			this.attName = att.name;
			this.lowerVal = att.lowerLim;
			this.upperVal = att.upperLim;
			this.numGridPoints = numGridPoints;
		}


		/**
		 * Returns the width of a grid cell along this attribute. Returns 0 if the number of grid points
		 * is 1. This value is defined as (upperVal - lowerVal) / (numGridPoints - 1)
		 * @return the width of a grid cell along this attribute
		 */
		public double cellWidth(){
			if(numGridPoints == 1){
				return 0.;
			}
			return (this.upperVal - this.lowerVal) / (double)(this.numGridPoints-1);
		}

	}


}
