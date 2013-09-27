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
import burlap.datastructures.BoltzmannDistribution;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;


public class BoltzmannQPolicy extends Policy implements PlannerDerivedPolicy{

	protected QComputablePlanner		qplanner;
	double								temperature;
	
	
	
	public BoltzmannQPolicy(double temperature){
		this.qplanner = null;
		this.temperature = temperature;
	}
	
	public BoltzmannQPolicy(QComputablePlanner vf, double temperature){
		this.qplanner = vf;
		this.temperature = temperature;
	}
	
	@Override
	public GroundedAction getAction(State s) {
		return this.sampleFromActionDistribution(s);
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		List<QValue> qValues = this.qplanner.getQs(s);
		return this.getActionDistributionForQValues(qValues);
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
	

}
