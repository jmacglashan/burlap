package burlap.behavior.singleagent.planning.commonpolicies;

import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.PlannerDerivedPolicy;
import burlap.behavior.singleagent.planning.QFunction;
import burlap.datastructures.BoltzmannDistribution;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;


/**
 * This class implements a Boltzmann policy where the the Q-values represent
 * the components of the Boltzmann distribution. This policy requires a QComputable
 * planner to be passed to it.
 * @author James MacGlashan
 *
 */
public class BoltzmannQPolicy extends Policy implements PlannerDerivedPolicy{

	protected QFunction qplanner;
	double								temperature;
	
	
	/**
	 * Initializes with a temperature value. The temperature value controls how greedy the Boltzmann distribution is.
	 * The temperature should be positive with values near zero causing the distribution to be more greedy. A high temperature
	 * causes the distribution to be more uniform.
	 * @param temperature the positive temperature value to use
	 */
	public BoltzmannQPolicy(double temperature){
		this.qplanner = null;
		this.temperature = temperature;
	}
	
	
	/**
	 * Initializes with a temperature value and the QComputable planner to use. The temperature value controls how greedy the Boltzmann distribution is.
	 * The temperature should be positive with values near zero causing the distribution to be more greedy. A high temperature
	 * causes the distribution to be more uniform.
	 * @param planner the q-computable planner to use.
	 * @param temperature the positive temperature value to use
	 */
	public BoltzmannQPolicy(QFunction planner, double temperature){
		this.qplanner = planner;
		this.temperature = temperature;
	}
	
	@Override
	public AbstractGroundedAction getAction(State s) {
		return this.sampleFromActionDistribution(s);
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		List<QValue> qValues = this.qplanner.getQs(s);
		return this.getActionDistributionForQValues(s, qValues);
	}

	
	
	private List<ActionProb> getActionDistributionForQValues(State queryState, List <QValue> qValues){
		
		List <ActionProb> res = new ArrayList<Policy.ActionProb>();
		
		double [] rawQs = new double[qValues.size()];
		for(int i = 0; i < qValues.size(); i++){
			rawQs[i] = qValues.get(i).q;
		}
		
		BoltzmannDistribution bd = new BoltzmannDistribution(rawQs, this.temperature);
		double [] probs = bd.getProbabilities();
		for(int i = 0; i < qValues.size(); i++){
			QValue q = qValues.get(i);
			ActionProb ap = new ActionProb(q.a.translateParameters(q.s, queryState), probs[i]);
			res.add(ap);
		}
		
		return res;
	}

	@Override
	public boolean isStochastic() {
		return true;
	}

	@Override
	public void setPlanner(OOMDPPlanner planner) {
		if(!(planner instanceof QFunction)){
			throw new RuntimeErrorException(new Error("Planner is not a QComputablePlanner"));
		}
		
		this.qplanner = (QFunction)planner;
		
	}


	@Override
	public boolean isDefinedFor(State s) {
		return true; //can always find q-values with default value
	}
	

}
