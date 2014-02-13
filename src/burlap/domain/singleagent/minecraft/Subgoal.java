package burlap.domain.singleagent.minecraft;

import burlap.oomdp.core.PropositionalFunction;

/*
 * CLASS: Subgoal
 * For use in Subgoal Planning (see: SubgoalPlanner in MinecraftBehavior)
 */

public class Subgoal {
	
	private PropositionalFunction pre;
	private PropositionalFunction post;
	private PropositionalFunction[] conditions;
	
	
	public Subgoal(PropositionalFunction pre, PropositionalFunction post) {
		this.pre = pre;
		this.post = post;
		
		this.conditions = new PropositionalFunction[2];
		
		this.conditions[0] = pre;
		this.conditions[1] = post;
		
	}
	
	public PropositionalFunction getPre() {
		return this.pre;
	}

	public PropositionalFunction getPost() {
		return this.post;
	}
	
	public boolean isPost(PropositionalFunction pf) {
		return this.post == pf;
	}
}
