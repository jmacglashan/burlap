package burlap.behavior.functionapproximation.dense;

import burlap.mdp.core.state.State;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class is used to produce a state feature vector from an {@link burlap.mdp.core.oo.state.OOState} by iterating
 * over the objects, generating a double array for each object, and concatenating the reslting vectors into one vector.
 * You specify the ordering of the vectorization by specifying the order of the OO-MDP object classes, and which
 * {@link DenseStateFeatures} to use for objects of their class.
 * If multiple objects of a class exist, then they will be unpacked in the order defined by the {@link burlap.mdp.core.oo.state.OOState}.
 *
 * @author James MacGlashan
 *
 */
public class ConcatenatedObjectFeatures implements
		DenseStateFeatures {

	/**
	 * The order of object classes to follow when concatenating objects into a single state feature vector.
	 */
	protected List<String> objectClassOrder = new ArrayList<String>();

	/**
	 * The {@link DenseStateFeatures} to use for each object class
	 */
	protected Map<String, DenseStateFeatures> objectVectorGenerators = new HashMap<String, DenseStateFeatures>();

	public ConcatenatedObjectFeatures() {
	}

	public ConcatenatedObjectFeatures(List<String> objectClassOrder, Map<String, DenseStateFeatures> objectVectorGenerators) {
		this.objectClassOrder = objectClassOrder;
		this.objectVectorGenerators = objectVectorGenerators;
	}

	/**
	 * Adds an OO-MDP class next in the list of object classes to vectorize with the given {@link DenseStateFeatures}.
	 * @param className the name of the OO-MDP object class
	 * @param objectVectorization the {@link DenseStateFeatures} to use for objects of this class
	 * @return this object, so that a builder paradigm can be used
	 */
	public ConcatenatedObjectFeatures addObjectVectorizion(String className, DenseStateFeatures objectVectorization){
		this.objectClassOrder.add(className);
		this.objectVectorGenerators.put(className, objectVectorization);
		return this;
	}

	@Override
	public double[] features(State s) {

		if(!(s instanceof OOState)){
			throw new RuntimeException("ConcatenatedObjectFeatureVectorGenerator is only defined for OOState instances.");
		}

		List<double[]> objectVecs = new ArrayList<double[]>();
		int size = 0;
		for(String className : this.objectClassOrder){
			List<ObjectInstance> objects = ((OOState)s).objectsOfClass(className);
			DenseStateFeatures ovecGen = this.objectVectorGenerators.get(className);
			for(ObjectInstance o : objects){
				double [] ovec = ovecGen.features(o);
				size += ovec.length;
				objectVecs.add(ovec);
			}
		}

		double [] fvec = new double[size];
		int i = 0;
		for(double [] ovec : objectVecs){
			for(int j = 0; j < ovec.length; j++){
				fvec[i] = ovec[j];
				i++;
			}
		}

		return fvec;
	}

	@Override
	public ConcatenatedObjectFeatures copy() {
		return new ConcatenatedObjectFeatures(new ArrayList<String>(objectClassOrder), new HashMap<String, DenseStateFeatures>(objectVectorGenerators));
	}
}
