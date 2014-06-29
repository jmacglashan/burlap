package burlap.oomdp.singleagent.pomdp.common;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.pomdp.BeliefAgent;
import burlap.oomdp.singleagent.pomdp.BeliefState;
import burlap.oomdp.singleagent.pomdp.BeliefState.StateBelief;

public class QMDPBeliefAgent extends BeliefAgent {

	QComputablePlanner mdpQSource;
	
	public QMDPBeliefAgent(QComputablePlanner mdpQSource){
		this.mdpQSource = mdpQSource;
	}
	
	@Override
	public GroundedAction getAction(BeliefState curBelief) {
		
		List<StateBelief> sbs = curBelief.getStatesAndBeliefsWithNonZeroProbability();
		List<GroundedAction> actions = Action.getAllApplicableGroundedActionsFromActionList(this.environment.getPODomain().getActions(), sbs.get(0).s);
		List<GroundedAction> candidates = new ArrayList<GroundedAction>(actions.size());
		double maxQ = Double.NEGATIVE_INFINITY;
		
		for(GroundedAction ga : actions){
			
			double q = 0.;
			for(StateBelief sb : sbs){
				q += sb.belief * this.mdpQSource.getQ(sb.s, ga).q;
			}
			
			if(q == maxQ){
				candidates.add(ga);
			}
			else if(q > maxQ){
				candidates.clear();
				candidates.add(ga);
				maxQ = q;
			}
			
		}
		
		int ind = RandomFactory.getMapped(0).nextInt(candidates.size());
		GroundedAction ga = candidates.get(ind);
		
		return ga;
	}

}
