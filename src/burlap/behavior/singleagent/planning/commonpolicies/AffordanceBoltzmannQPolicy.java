package burlap.behavior.singleagent.planning.commonpolicies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.management.RuntimeErrorException;

import burlap.behavior.affordances.AffordancesController;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.Policy.PolicyUndefinedException;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.PlannerDerivedPolicy;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.datastructures.BoltzmannDistribution;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;


/**
 * This class implements a Boltzmann policy where the the Q-values represent
 * the components of the Boltzmann distribution and actions are pruned on a state-by-state
 * basis according to an affordance knowledge base. This policy requires a QComputable
 * planner to be passed to it.
 * @author James MacGlashan, David Abel
 *
 */
public class AffordanceBoltzmannQPolicy extends Policy implements PlannerDerivedPolicy{

	protected 	QComputablePlanner		qplanner;
	private		double					temperature;
	private 	AffordancesController 	affController;
	
	
	/**
	 * Initializes with a temperature value. The temperature value controls how greedy the Boltzmann distribution is.
	 * The temperature should be positive with values near zero causing the distribution to be more greedy. A high temperature
	 * causes the distribution to be more uniform.
	 * @param temperature the positive temperature value to use
	 */
	public AffordanceBoltzmannQPolicy(double temperature, AffordancesController affController){
		this.qplanner = null;
		this.temperature = temperature;
		this.affController = affController;
	}
	
	
	/**
	 * Initializes with a temperature value and the QComputable planner to use. The temperature value controls how greedy the Boltzmann distribution is.
	 * The temperature should be positive with values near zero causing the distribution to be more greedy. A high temperature
	 * causes the distribution to be more uniform.
	 * @param planner the q-computable planner to use.
	 * @param temperature the positive temperature value to use
	 */
	public AffordanceBoltzmannQPolicy(QComputablePlanner planner, double temperature, AffordancesController affController){
		this.qplanner = planner;
		this.temperature = temperature;
		this.affController = affController;
	}
	
	@Override
	public AbstractGroundedAction getAction(State s) {
		// Implicitly prunes actions according to affordance knowledge base
		List<ActionProb> actionDistribution = this.getActionDistributionForState(s);
		
		// Policy undefined
		if(actionDistribution == null || actionDistribution.size() == 0){
			throw new PolicyUndefinedException();
		}
		
		Random rand = RandomFactory.getMapped(0);
		double roll = rand.nextDouble();
		
		double sump = 0.;
		for(ActionProb ap : actionDistribution) {
			sump += ap.pSelection;
			if(roll < sump){
				return ap.ga;
			}
		}
		
		throw new RuntimeException("Tried to sample policy action distribution, but it did not sum to 1."); 
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		List<QValue> allQValues = this.qplanner.getQs(s);
		
		// Prune actions according to affordance knowledge base
		List<QValue> filteredQValues = filterQValues(allQValues, s);
		
		return this.getActionDistributionForQValues(filteredQValues);
	}

	
	
	private List<ActionProb> getActionDistributionForQValues(List <QValue> qValues){
		
		List <ActionProb> res = new ArrayList<Policy.ActionProb>();
		
		double [] rawQs = new double[qValues.size()];
		for(int i = 0; i < qValues.size(); i++){
			rawQs[i] = qValues.get(i).q;
		}
		
		BoltzmannDistribution bd = new BoltzmannDistribution(rawQs, this.temperature);
		double [] probs = bd.getProbabilities();
		for(int i = 0; i < qValues.size(); i++){
			QValue q = qValues.get(i);
			ActionProb ap = new ActionProb(q.a, probs[i]);
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
		if(!(planner instanceof QComputablePlanner)){
			throw new RuntimeErrorException(new Error("Planner is not a QComputablePlanner"));
		}
		
		this.qplanner = (QComputablePlanner)planner;
		
	}


	@Override
	public boolean isDefinedFor(State s) {
		return true; //can always find q-values with default value
	}
	
	/**
	 * Filters the set of all QValues based on which affordances are active in the current state
	 * @param allQValues: The set of q values representing all actions.
	 * @param s: The current State.
	 * @return: A list of filtered QValues
	 */
	private List<QValue> filterQValues(List<QValue> allQValues, State s) {
		
		List<QValue> affFilteredQValues = new ArrayList<QValue>();
		List<AbstractGroundedAction> qActions = new ArrayList<AbstractGroundedAction>();
		for(QValue q : allQValues){
			qActions.add(q.a);
		}
		
		qActions = this.affController.filterIrrelevantActionsInState(qActions, s);
		
		for(QValue q : allQValues){
			if(qActions.contains(q.a)){
				affFilteredQValues.add(q);
			}
		}
		return affFilteredQValues;
	}
	
}
