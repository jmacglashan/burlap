package burlap.behavior.stochasticgames.saconversion;

import burlap.oomdp.stochasticgames.JointAction;

/**
 * 
 * @author Betsy Hilliard betsy@cs.brown.edu
 *
 */
public class JointActionProbability {

	protected JointAction ja;
	protected double p;
	
	
	public JointActionProbability(JointAction jointAction, double probability){
		this.ja = jointAction;
		this.p = probability;
		
	}
	
	public double getProbability(){
		return p;
	}
	
	public JointAction getJointAction(){
		return ja;
	}
	
	
}
