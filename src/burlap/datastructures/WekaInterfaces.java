package burlap.datastructures;

import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.mdp.core.state.State;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Class that includes general methods for interfacing BURLAP with Weka, in particular for turning sets of states
 * into a Weka data set that can then be used with Weka learning algorithms.
 * @author James MacGlashan.
 */
public class WekaInterfaces {
    
    private WekaInterfaces() {
        // do nothing
    }

	/**
	 * Creates a Weka {@link Instance} for a state by first converting the state into a feature vector and then creating
	 * the Instance to hold that feature vector with the provided target supervised output value. The Weka
	 * {@link weka.core.Instance} is associated added to the provided {@link Instances} object. If it is null, then it makes one and
	 * adds it to that.
	 * @param s the state for which the instance is to be constructed
	 * @param fvgen the {@link DenseStateFeatures} to turn the state into a feature vector.
	 * @param targetValue the target supervised output value for the instance
	 * @param dataset the dataset to which the created instance will be added.
	 * @return the created {@link weka.core.Instance}
	 */
	public static Instance getInstance(State s, DenseStateFeatures fvgen, double targetValue, Instances dataset){

		double [] fv = fvgen.generateFeatureVectorFrom(s);
		double [] labeled = new double[fv.length+1];
		for(int i = 0; i < fv.length; i++){
			labeled[i] = fv[i];
		}
		labeled[fv.length] = targetValue;
		Instance inst = new Instance(1., labeled);


		if(dataset == null){
			dataset = getInstancesShell(s, fvgen, 1);
		}

		dataset.add(inst);
		inst.setDataset(dataset);



		return inst;

	}

	/**
	 * Creates an empty Weka dataset ({@link Instances}) that can accommodate the feature set necessary for the input state.
	 * @param s the input state from which the Weka dataset features will be modeled.
	 * @param fvgen The {@link DenseStateFeatures} that converts the state into a feature vector
	 * @param capacity the reserved capacity for the dataset.
	 * @return the created {@link Instances} Weka dataset.
	 */
	public static Instances getInstancesShell(State s, DenseStateFeatures fvgen, int capacity){
		double [] exfv = fvgen.generateFeatureVectorFrom(s);
		FastVector attInfo = new FastVector(exfv.length+1);
		for(int i = 0; i < exfv.length; i++){
			Attribute att = new Attribute("f"+i);
			attInfo.addElement(att);
		}

		Attribute classAtt = new Attribute("discountedReturn");
		attInfo.addElement(classAtt);

		Instances dataset = new Instances("burlap_data", attInfo, capacity);
		dataset.setClassIndex(exfv.length);

		return dataset;
	}


	/**
	 * Creates a Weka {@link weka.core.Instance} from the provided feature vector and target supervised output value and
	 * adds it to the provided dataset. If the dataset is null, then it is created first.
	 * @param fv the input feature vector.
	 * @param targetValue the target supervised output value
	 * @param dataset the dataset ({@link weka.core.Instances}) to which the created {@link weka.core.Instance} will be added.
	 * @return the created {@link weka.core.Instance}.
	 */
	public static Instance getInstance(double [] fv, double targetValue, Instances dataset){

		double [] labeled = new double[fv.length+1];
		for(int i = 0; i < fv.length; i++){
			labeled[i] = fv[i];
		}
		labeled[fv.length] = targetValue;
		Instance inst = new Instance(1., labeled);


		if(dataset == null){
			dataset = getInstancesShell(fv, 1);
		}

		dataset.add(inst);
		inst.setDataset(dataset);



		return inst;

	}

	/**
	 * Creates an empty Weka dataset ({@link Instances}) that can accommodate the provided feature vector.
	 * @param exfv the input feature vector for which the dataset is to be modeled.
	 * @param capacity the reserved capacity for the dataset.
	 * @return the created {@link Instances} Weka dataset.
	 */
	public static Instances getInstancesShell(double [] exfv, int capacity){
		FastVector attInfo = new FastVector(exfv.length+1);
		for(int i = 0; i < exfv.length; i++){
			Attribute att = new Attribute("f"+i);
			attInfo.addElement(att);
		}

		Attribute classAtt = new Attribute("v");
		attInfo.addElement(classAtt);

		Instances dataset = new Instances("burlap_data", attInfo, capacity);
		dataset.setClassIndex(exfv.length);

		return dataset;
	}


	public static interface WekaClassifierGenerator{

		public Classifier generateClassifier();

	}

}
