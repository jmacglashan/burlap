package burlap.behavior.stochasticgame.saconversion;

import burlap.oomdp.stochasticgames.JointAction;

/**
 * 
 * @author Betsy Hilliard betsy@cs.brown.edu
 *
 */
public class JointActionProbability {

	protected JointAction ja;
	protected double p;
	protected double psp;
	
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
	
	public void setProbabilityNextState(double probNext){
		psp = probNext;
	}
	
	public double getProbabilityNextState(){
		return psp;
	}
}
