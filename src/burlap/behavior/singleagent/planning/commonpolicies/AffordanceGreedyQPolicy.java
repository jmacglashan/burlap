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
 * A greedy policy that breaks ties by randomly choosing an action amongst the tied actions. This class requires a QComputablePlanner.
 * The set of actions considered is determined by pruning the action set according to a set of Affordances.
 * @author James MacGlashan, David Abel
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
			System.out.println("---BACKING OFF TO FULL ACTION SET---");

			filteredQValues = allQValues;
		}
		
//		System.out.println("(affgreedyqpolicy)filteredQs Action set: ");
//		for(QValue q : filteredQValues) {
//			System.out.println(q.a.actionName());
//		}
//		System.out.println("\n");

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
