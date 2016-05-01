package burlap.behavior.policy;

import java.util.List;

import javax.management.RuntimeErrorException;

import burlap.behavior.singleagent.MDPSolverInterface;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.QFunction;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * A greedy policy that breaks ties by choosing the first action with the maximum value. This class requires a QComputablePlanner
 * @author James MacGlashan
 *
 */
public class GreedyDeterministicQPolicy extends Policy implements SolverDerivedPolicy {

	protected QFunction qplanner;
	
	public GreedyDeterministicQPolicy() {
		qplanner = null;
	}
	
	/**
	 * Initializes with a QComputablePlanner
	 * @param qplanner the QComputablePlanner to use
	 */
	public GreedyDeterministicQPolicy(QFunction qplanner){
		this.qplanner = qplanner;
	}
	
	@Override
	public void setSolver(MDPSolverInterface solver){
		
		if(!(solver instanceof QFunction)){
			throw new RuntimeErrorException(new Error("Planner is not a QComputablePlanner"));
		}
		
		this.qplanner = (QFunction) solver;
	}
	

	@Override
	public AbstractGroundedAction getAction(State s) {
		
		List<QValue> qValues = this.qplanner.getQs(s);
		double maxQV = Double.NEGATIVE_INFINITY;
		QValue maxQ = null;
		for(QValue q : qValues){
			if(q.q > maxQV){
				maxQV = q.q;
				maxQ = q;
			}
		}
		
		return ((GroundedAction)maxQ.a).translateParameters(maxQ.s, s);
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		return this.getDeterministicPolicy(s);
	}

	@Override
	public boolean isStochastic() {
		return false;
	}
	
	@Override
	public boolean isDefinedFor(State s) {
		return true; //can always find q-values with default value
	}

}
