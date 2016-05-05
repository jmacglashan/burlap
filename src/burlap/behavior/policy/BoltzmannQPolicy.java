package burlap.behavior.policy;

import burlap.behavior.singleagent.MDPSolverInterface;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.datastructures.BoltzmannDistribution;
import burlap.mdp.core.AbstractGroundedAction;
import burlap.mdp.core.oo.AbstractObjectParameterizedGroundedAction;
import burlap.mdp.core.state.State;

import javax.management.RuntimeErrorException;
import java.util.ArrayList;
import java.util.List;


/**
 * This class implements a Boltzmann policy where the the Q-values represent
 * the components of the Boltzmann distribution. This policy requires a QComputable
 * valueFunction to be passed to it.
 * @author James MacGlashan
 *
 */
public class BoltzmannQPolicy extends Policy implements SolverDerivedPolicy {

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
	 * Initializes with a temperature value and the QComputable valueFunction to use. The temperature value controls how greedy the Boltzmann distribution is.
	 * The temperature should be positive with values near zero causing the distribution to be more greedy. A high temperature
	 * causes the distribution to be more uniform.
	 * @param planner the q-computable valueFunction to use.
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
			AbstractGroundedAction translated = AbstractObjectParameterizedGroundedAction.Helper.translateParameters(q.a, q.s, queryState);
			ActionProb ap = new ActionProb(translated, probs[i]);
			res.add(ap);
		}
		
		return res;
	}

	@Override
	public boolean isStochastic() {
		return true;
	}

	@Override
	public void setSolver(MDPSolverInterface solver) {
		if(!(solver instanceof QFunction)){
			throw new RuntimeErrorException(new Error("Planner is not a QComputablePlanner"));
		}
		
		this.qplanner = (QFunction) solver;
		
	}


	@Override
	public boolean isDefinedFor(State s) {
		return true; //can always find q-values with default value
	}
	

}
