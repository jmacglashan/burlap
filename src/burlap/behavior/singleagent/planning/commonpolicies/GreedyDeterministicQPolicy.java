package burlap.behavior.singleagent.planning.commonpolicies;

import java.util.List;

import javax.management.RuntimeErrorException;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.PlannerDerivedPolicy;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;


/**
 * A greedy policy that breaks ties by choosing the first action with the maximum value. This class requires a QComputablePlanner
 * @author James MacGlashan
 *
 */
public class GreedyDeterministicQPolicy extends Policy implements PlannerDerivedPolicy{

	protected QComputablePlanner		qplanner;
	
	public GreedyDeterministicQPolicy() {
		qplanner = null;
	}
	
	/**
	 * Initializes with a QComputablePlanner
	 * @param qplanner the QComputablePlanner to use
	 */
	public GreedyDeterministicQPolicy(QComputablePlanner qplanner){
		this.qplanner = qplanner;
	}
	
	@Override
	public void setPlanner(OOMDPPlanner planner){
		
		if(!(planner instanceof QComputablePlanner)){
			throw new RuntimeErrorException(new Error("Planner is not a QComputablePlanner"));
		}
		
		this.qplanner = (QComputablePlanner)planner;
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
		
		return maxQ.a;
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
