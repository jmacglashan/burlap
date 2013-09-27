package burlap.behavior.singleagent.planning.commonpolicies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.management.RuntimeErrorException;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.PlannerDerivedPolicy;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;


public class EpsilonGreedy extends Policy implements PlannerDerivedPolicy{

	protected QComputablePlanner		qplanner;
	protected double					epsilon;
	protected Random 					rand;
	
	
	
	public EpsilonGreedy(double epsilon) {
		qplanner = null;
		this.epsilon = epsilon;
		rand = RandomFactory.getMapped(0);
	}
	
	public EpsilonGreedy(QComputablePlanner planner, double epsilon) {
		qplanner = planner;
		this.epsilon = epsilon;
		rand = RandomFactory.getMapped(0);
	}

	public void setPlanner(OOMDPPlanner planner){
		
		if(!(planner instanceof QComputablePlanner)){
			throw new RuntimeErrorException(new Error("Planner is not a QComputablePlanner"));
		}
		
		this.qplanner = (QComputablePlanner)planner;
	}
	
	@Override
	public GroundedAction getAction(State s) {
		
		
		List<QValue> qValues = this.qplanner.getQs(s);
		
		
		double roll = rand.nextDouble();
		if(roll <= epsilon){
			return qValues.get(rand.nextInt(qValues.size())).a;
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
		return maxActions.get(rand.nextInt(maxActions.size())).a;
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStochastic() {
		return true;
	}

}
