package burlap.behavior.singleagent.vfa.common;

import java.util.LinkedList;
import java.util.List;

import burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;


/**
 * This class is used to produce a state feature vector that is the concatenation of the observable attributes of objects belonging to a specified sequence of object classes. The feature
 * vector attribtues are ordered first by the class of object and then by the order of the objects that appear for each object of that class. Values in the feature vector can optionally
 * be specfied to be the normalized object values, which uses the attributes lowerlims and upperlims attribute to determine normalization.
 * @author James MacGlashan
 *
 */
public class ConcatenatedObjectFeatureVectorGenerator implements
		StateToFeatureVectorGenerator {

	/**
	 * The order of object classes and their order to follow when concatenting objects into a single state feature vector.
	 */
	protected String [] objectClassOrder;
	
	/**
	 * Whether object values should be normalized.
	 */
	protected boolean normalizeValues = false;
	
	/**
	 * Initializes with an array of or object class names. The resulting state feature vector will only be made up of objects beloning to the classes
	 * that appear in the array. Furthermore, the order that the observable values of the objects are concatenated will follow the order that these
	 * object classes are specfied, and then by the order that objects appear in the BURLAP state.
	 * @param objectClassOrder the sequence of object classes to use when constructing a state feature vector.
	 */
	public ConcatenatedObjectFeatureVectorGenerator(String...objectClassOrder){
		this.objectClassOrder = objectClassOrder.clone();
	}
	
	/**
	 * Initializes with an array of or object class names. The resulting state feature vector will only be made up of objects beloning to the classes
	 * that appear in the array. Furthermore, the order that the observable values of the objects are concatenated will follow the order that these
	 * object classes are specfied, and then by the order that objects appear in the BURLAP state. If normalizeValues is set to true, then the value
	 * of all attributes are first normalized using the attribute lowerlims and upperlims fields.
	 * @param normalizeValues whether the values in the feature vector should be normalized.
	 * @param objectClassOrder the sequence of object classes to use when constructing a state feature vector.
	 */
	public ConcatenatedObjectFeatureVectorGenerator(boolean normalizeValues, String...objectClassOrder){
		this.normalizeValues = normalizeValues;
		this.objectClassOrder = objectClassOrder.clone();
	}
	
	
	/**
	 * Sets whether the object values are normalized in the returned feature vector
	 * @param normalizeValues true if values should be normalized; false if they should be unnormalized.
	 */
	public void setNormalizeValues(boolean normalizeValues){
		this.normalizeValues = normalizeValues;
	}
	
	
	@Override
	public double[] generateFeatureVectorFrom(State s) {
		
		List<ObjectInstance> objectsToAdd = new LinkedList<ObjectInstance>();
		int d = 0;
		
		for (String oclassName : this.objectClassOrder) {
			List<ObjectInstance> obs = s.getObjectsOfClass(oclassName);
			if(obs.size() > 0){
				d += obs.get(0).getObjectClass().numAttributes();
				objectsToAdd.addAll(obs);
			}
		}
		
		double [] featureVector = new double[d];
		int i = 0;
		for(ObjectInstance o : objectsToAdd){
			double [] ofv;
			if(!this.normalizeValues){
				ofv = o.getFeatureVec();
			}
			else{
				ofv = o.getNormalizedFeatureVec();
			}
			for(int j = 0; j < ofv.length; j++){
				featureVector[i] = ofv[j];
				i++;
			}
		}
		
		return featureVector;
	}

}
