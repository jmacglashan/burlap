package burlap.domain.singleagent.minecraft;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public class Affordance {
	
	private PropositionalFunction pf;
	private PropositionalFunction goal;
	private List<Action> actions;
	private boolean dirFlag;
	
	public Affordance(PropositionalFunction pf, PropositionalFunction goal, List<Action> actions) {
		this.pf = pf;
		this.goal = goal;
		this.actions = actions;
		this.dirFlag = false;
	}
	
	public Affordance(PropositionalFunction pf, PropositionalFunction goal, List<Action> actions, boolean dirFlag) {
		
		this.goal = goal;
		this.actions = actions;
		this.dirFlag = dirFlag;
		
	}
	
	public PropositionalFunction getPreCondition() {
		return this.pf;
	}
	
	public PropositionalFunction getPostCondition() {
		return this.goal;
	}
	
	public List<Action> getActions() {
		return this.actions;
	}
	
	public boolean isApplicable(State s, PropositionalFunction goal) {
		
		// Ignores goal right now
		if (this.pf.isTrue(s)) {
			return true;
		}
		
		return false;
	}
	
	public List<GroundedAction> getApplicableActions(State st, PropositionalFunction goal) {
		List<GroundedAction> result = new ArrayList<GroundedAction>();
		
		for(Action a : this.actions) {
			
			// Check if this affordance applies (NEED TO ADD GOAL RELATIVE PART)
			if (this.pf.isTrue(st)) {
				
				// Do weird state binding thing
				List <List <String>> bindings = st.getPossibleBindingsGivenParamOrderGroups(a.getParameterClasses(), a.getParameterOrderGroups());
				for(List <String> params : bindings){
					String [] aprams = params.toArray(new String[params.size()]);
						GroundedAction gp = new GroundedAction(a, aprams);
						result.add(gp);
				}				
			}
		}
		return result;
	}
	
	public boolean containsAction(Action a) {
		return this.actions.contains(a);
	}
	

}
