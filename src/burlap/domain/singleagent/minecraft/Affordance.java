package burlap.domain.singleagent.minecraft;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;

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
	
	public List<Action> getApplicableActions(State s, PropositionalFunction goal) {
		List<Action> result = new ArrayList<Action>();
		
		for(Action a : this.actions) {
			if (this.pf.isTrue(s)) {
				result.add(a);
			}
		}
		return result;
	}
	
	public boolean containsAction(Action a) {
		return this.actions.contains(a);
	}

}
