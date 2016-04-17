package burlap.behavior.singleagent.vfa.rbf.metrics;

import burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator;
import burlap.behavior.singleagent.vfa.rbf.DistanceMetric;
import burlap.oomdp.core.states.State;

public class EuclideanDistance implements DistanceMetric {

	protected StateToFeatureVectorGenerator vectorGenerator;
	
	
	public EuclideanDistance(StateToFeatureVectorGenerator vectorGenerator){
		this.vectorGenerator = vectorGenerator;
	}
	
	
	@Override
	public double distance(State s0, State s1) {
		
		double [] f0 = this.vectorGenerator.generateFeatureVectorFrom(s0);
		double [] f1 = this.vectorGenerator.generateFeatureVectorFrom(s1);
		
		if(f0.length != f1.length){
			throw new RuntimeException("Cannot compute Euclidean distance; feature vectors for the two input states are not equal in size.");
		}
		
		double sum = 0.;
		for(int i = 0; i < f0.length; i++){
			double diff = f0[i] - f1[i];
			sum += diff*diff;
		}
		
		// Euclidean Distance
		return Math.sqrt(sum);
	}

}
