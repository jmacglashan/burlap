/**
 * 
 */
package burlap.behavior.singleagent.planning.commonpolicies;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.affordances.AffordancesController;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;

/**
 * @author dabel
 *
 */
public class AffordanceGreedyQPolicy extends GreedyQPolicy {

	private AffordancesController affController;

	public AffordanceGreedyQPolicy(AffordancesController affController) {
		super();
		this.affController = affController;
	}
	
	public AffordanceGreedyQPolicy(AffordancesController affController, QComputablePlanner planner) {
		super(planner);
		this.affController = affController;
	}
	
	/**
	 * Returns an action from the affordance-filtered action set.
	 */
	@Override
	public AbstractGroundedAction getAction(State s) {
		List<QValue> allQValues = this.qplanner.getQs(s);
		
		List<QValue> filteredQValues = filterQValues(allQValues, s);
		
		// If Affordances prune away all actions, back off to full action set 
		if (filteredQValues.isEmpty()) {
//			System.out.println("Backing off to full action set");
			filteredQValues = allQValues;
		}
//		else {
//			System.out.println("     NOT BACKING OFF");
//		}
		List <QValue> maxActions = new ArrayList<QValue>();
		maxActions.add(filteredQValues.get(0));
		double maxQ = filteredQValues.get(0).q;
		for(int i = 1; i < filteredQValues.size(); i++){
			QValue q = filteredQValues.get(i);
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
