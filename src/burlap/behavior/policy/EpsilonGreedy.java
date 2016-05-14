package burlap.behavior.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.management.RuntimeErrorException;

import burlap.behavior.singleagent.MDPSolverInterface;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.QFunction;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;


/**
 * This class defines a an epsilon-greedy policy over Q-values and requires a QComputable valueFunction to be specified.
 * With probability epsilon the policy will return a random action (with uniform distribution over all possible action).
 * With probability 1 - epsilon the policy will return the greedy action. If multiple actions tie for the highest Q-value,
 * then one of the tied actions is randomly selected.
 * @author James MacGlashan
 *
 */
public class EpsilonGreedy extends Policy implements SolverDerivedPolicy {

	protected QFunction qplanner;
	protected double					epsilon;
	protected Random 					rand;
	
	
	/**
	 * Initializes with the value of epsilon, where epsilon is the probability of taking a random action.
	 * @param epsilon the probability of taking a random action.
	 */
	public EpsilonGreedy(double epsilon) {
		qplanner = null;
		this.epsilon = epsilon;
		rand = RandomFactory.getMapped(0);
	}
	
	/**
	 * Initializes with the QComputablePlanner to use and the value of epsilon to use, where epsilon is the probability of taking a random action.
	 * @param planner the QComputablePlanner to use
	 * @param epsilon the probability of taking a random action.
	 */
	public EpsilonGreedy(QFunction planner, double epsilon) {
		qplanner = planner;
		this.epsilon = epsilon;
		rand = RandomFactory.getMapped(0);
	}

	
	/**
	 * Returns the epsilon value, where epsilon is the probability of taking a random action.
	 * @return the epsilon value
	 */
	public double getEpsilon() {
		return epsilon;
	}

	/**
	 * Sets the epsilon value, where epsilon is the probability of taking a random action.
	 * @param epsilon the probability of taking a random action.
	 */
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	@Override
	public void setSolver(MDPSolverInterface solver){
		
		if(!(solver instanceof QFunction)){
			throw new RuntimeErrorException(new Error("Planner is not a QComputablePlanner"));
		}
		
		this.qplanner = (QFunction) solver;
	}
	
	@Override
	public Action getAction(State s) {
		
		
		List<QValue> qValues = this.qplanner.getQs(s);
		
		
		double roll = rand.nextDouble();
		if(roll <= epsilon){
			int selected = rand.nextInt(qValues.size());
			Action ga = qValues.get(selected).a;
			return ObjectParameterizedAction.Helper.translateParameters(ga, qValues.get(selected).s, s);
		}
		
		
		List <QValue> maxActions = new ArrayList<QValue>();
		maxActions.add(qValues.get(0));
		double maxQ = qValues.get(0).q;
		for(int i = 1; i < qValues.size(); i++){
			QValue q = qValues.get(i);
			if(q.q == maxQ){
				maxActions.add(q);
			}
			else if(q.q > maxQ){
				maxActions.clear();
				maxActions.add(q);
				maxQ = q.q;
			}
		}
		int selected = rand.nextInt(maxActions.size());
		//return translated action parameters if the action is parameterized with objects in a object identifier indepdent domain
		Action ga =  maxActions.get(selected).a;
		return ObjectParameterizedAction.Helper.translateParameters(ga, maxActions.get(selected).s, s);
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		
		List<QValue> qValues = this.qplanner.getQs(s);
		
		List <ActionProb> dist = new ArrayList<Policy.ActionProb>(qValues.size());
		double maxQ = Double.NEGATIVE_INFINITY;
		int nMax = 0;
		for(QValue q : qValues){
			if(q.q > maxQ){
				maxQ = q.q;
				nMax = 1;
			}
			else if(q.q == maxQ){
				nMax++;
			}
			Action ta = ObjectParameterizedAction.Helper.translateParameters(q.a, q.s, s);
			ActionProb ap = new ActionProb(ta, this.epsilon*(1. / qValues.size()));
			dist.add(ap);
		}
		for(int i = 0; i < dist.size(); i++){
			QValue q = qValues.get(i);
			if(q.q == maxQ){
				dist.get(i).pSelection += (1. - this.epsilon) / nMax;
			}
		}
		
		
		return dist;
	}

	@Override
	public boolean isStochastic() {
		return true;
	}
	
	@Override
	public boolean isDefinedFor(State s) {
		return true; //can always find q-values with default value
	}

}
