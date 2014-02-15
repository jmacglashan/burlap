package burlap.domain.singleagent.minecraft;

import java.util.List;

import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;

public class Affordance {
	
	private PropositionalFunction pf;
	private PropositionalFunction goal;
	private List<Action> actions;
	
	public Affordance(PropositionalFunction pf, PropositionalFunction goal, List<Action> actions) {
		this.pf = pf;
		this.goal = goal;
		this.actions = actions;
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
		
		if ((this.pf.isTrue(s)) && (goal.getClass() == this.goal.getClass())) {
			return true;
		}
		
		return false;
	}

}
