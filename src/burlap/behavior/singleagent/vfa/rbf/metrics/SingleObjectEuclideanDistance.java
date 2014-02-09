package burlap.behavior.singleagent.vfa.rbf.metrics;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.vfa.rbf.DistanceMetric;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;


/**
 * This class defines a Euclidean distance metric that operates on
 * the attribute values for a single object instance in each state. The attributes to use in computing
 * the Euclidean distance are specified to this object through the constructor or mutators.
 * The object instance retrieved from each state and used for comparison is by default the
 * first observable object instance specified in the provided state objects. Alternatively,
 * an object class name may be specified in the constructor or mutators of this class,
 * which causes the used object instance of a state to be the first object instance
 * of an object of the specified class.
 * @author Anubhav Malhotra and Daniel Fernandez and Spandan Dutta; modified by James MacGlashan
 *
 */
public class SingleObjectEuclideanDistance implements DistanceMetric {

	/**
	 * The list of attributes used in the Euclidean distance calculation
	 */
	protected List<String>		atts;
	
	/**
	 * The name of the object class whose attributes are used for computing the Euclidean distance.
	 */
	protected String			objectClassName = null;
	
	
	/**
	 * Initializes with an empty list of attributes to use for computing the Euclidean distance. Use any of the
	 * attribute mutators to set which attribute to use.
	 */
	public SingleObjectEuclideanDistance(){
		this.atts = new ArrayList<String>();
	}
	
	/**
	 * Initializes with a list of attribute names to use in the Euclidean distance computaiton. Without subsequent method calls to this object,
	 * these attributes will be assumed to correspond to the first observable object instance in each state object.
	 * @param atts the names of the attributes to use in the Euclidean distance calculation
	 */
	public SingleObjectEuclideanDistance(List<String> atts){
		this.atts = atts;
	}
	
	
	/**
	 * Initializes with a list of attribute names for a given object class to use in the Euclidean distance computation. The object instance
	 * from each state used for the comparison is the first object instance in the state object that belongs to the specified class.
	 * @param objectClassName the name of the object class used for Euclidean distance calculations
	 * @param atts the names of the attributes to use in the Euclidean distance calculation
	 */
	public SingleObjectEuclideanDistance(String objectClassName, List<String> atts){
		this.atts = atts;
		this.objectClassName = objectClassName;
	}
	
	
	/**
	 * Returns the list of attribute names used for Euclidean distance calculation
	 * @return the list of attribute names used for Euclidean distance calculation
	 */
	public List<String> getAtts() {
		return atts;
	}

	/**
	 * Sets the list of attribute names to use for Euclidean distance calculation.
	 * @param atts the list of attribute names to use for Euclidean distance calculation.
	 */
	public void setAtts(List<String> atts) {
		this.atts = atts;
	}

	/**
	 * Returns the name of the object class used for Euclidean distance calculation.
	 * @return the name of the object class used for Euclidean distance calculation.
	 */
	public String getObjectClassName() {
		return objectClassName;
	}

	/**
	 * Sets the name of the object class to use for Euclidean distance calculation.
	 * @param objectClassName the name of the object class to use for Euclidean distance calculation.
	 */
	public void setObjectClassName(String objectClassName) {
		this.objectClassName = objectClassName;
	}
	
	/**
	 * Sets the name of the attributes to use for Euclidean distance calculation by providing a list of {@link burlap.oomdp.core.Attribute} objects.
	 * @param attributes a list of {@link burlap.oomdp.core.Attribute} objects that will be used for Euclidean distance calculation.
	 */
	public void setWithAttributes(List<Attribute> attributes){
		this.atts = new ArrayList<String>(attributes.size());
		for(Attribute att : attributes){
			this.atts.add(att.name);
		}
	}
	
	/**
	 * Adds an attribute name to use for distance calculation.
	 * @param attName an attribute name to use for distance calculation.
	 */
	public void addAttribute(String attName){
		this.atts.add(attName);
	}
	
	/**
	 * Adds an attribute to use for distance calculation.
	 * @param att an attribute to use for distance calculation.
	 */
	public void addAttribute(Attribute att){
		this.atts.add(att.name);
	}


	@Override
	public double distance(State s0, State s1) {
		
		ObjectInstance objInstS0 = null;
		ObjectInstance objInstS1 = null;
		
		if(this.objectClassName == null){
			objInstS0 = s0.getObservableObjects().get(0);
			objInstS1 = s1.getObservableObjects().get(0);
		}
		else{
			objInstS0 = s0.getFirstObjectOfClass(this.objectClassName);
			objInstS1 = s1.getFirstObjectOfClass(this.objectClassName);
		}

		double sum = 0.;
		for(String att: atts){
			double distForObjInstS0 = objInstS0.getValueForAttribute(att).getNumericRepresentation();
			double distForObjInstS1 = objInstS1.getValueForAttribute(att).getNumericRepresentation();
			sum += Math.pow(distForObjInstS0 - distForObjInstS1,2);
		}
		
		return Math.sqrt(sum);
	}

}
