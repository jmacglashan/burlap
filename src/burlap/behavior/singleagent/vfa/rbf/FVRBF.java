package burlap.behavior.singleagent.vfa.rbf;

import burlap.behavior.singleagent.vfa.rbf.FVDistanceMetric;

/**
 * @author James MacGlashan.
 */
public abstract class FVRBF {

	protected double [] centeredState;

	protected FVDistanceMetric metric;

	public FVRBF(double [] centeredState, FVDistanceMetric metric){
		this.centeredState = centeredState;
		this.metric = metric;
	}

	public abstract double responseFor(double [] input);

}
