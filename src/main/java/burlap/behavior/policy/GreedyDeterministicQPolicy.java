package burlap.behavior.policy;

import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.MDPSolverInterface;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;

import javax.management.RuntimeErrorException;
import java.util.List;


/**
 * A greedy policy that breaks ties by choosing the first action with the maximum value. This class requires a QComputablePlanner
 * @author James MacGlashan
 *
 */
public class GreedyDeterministicQPolicy implements SolverDerivedPolicy {

	protected QProvider qplanner;
	
	public GreedyDeterministicQPolicy() {
		qplanner = null;
	}
	
	/**
	 * Initializes with a QComputablePlanner
	 * @param qplanner the QComputablePlanner to use
	 */
	public GreedyDeterministicQPolicy(QProvider qplanner){
		this.qplanner = qplanner;
	}
	
	@Override
	public void setSolver(MDPSolverInterface solver){
		
		if(!(solver instanceof QProvider)){
			throw new RuntimeErrorException(new Error("Planner is not a QComputablePlanner"));
		}
		
		this.qplanner = (QProvider) solver;
	}
	

	@Override
	public Action action(State s) {
		
		List<QValue> qValues = this.qplanner.qValues(s);
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
	public List<ActionProb> policyDistribution(State s) {
		return PolicyUtils.deterministicPolicyDistribution(this, s);
	}

	@Override
	public boolean stochastic() {
		return false;
	}
	
	@Override
	public boolean definedFor(State s) {
		return true; //can always find q-values with default value
	}

}
