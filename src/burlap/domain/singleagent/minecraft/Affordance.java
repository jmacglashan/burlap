package burlap.domain.singleagent.minecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Affordance{
	
	private String name;
	private HashMap<String,AffordanceSubgoal> children; // Subgoals
	
	public Affordance(String name) {
		this.name = name;
		this.children = new HashMap<String,AffordanceSubgoal>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public HashMap<String,AffordanceSubgoal> getChildren() {
		return this.children;
	}
	
	public void addChild(AffordanceSubgoal sg) {
		this.children.put(sg.getName(), sg);
	}
	
	public List<AffordanceSubgoal> getSubgoals() {
		return new ArrayList<AffordanceSubgoal>(children.values());
	}
	
	public void setSubGoalParams(String[] sgParams) {
		for(String s: children.keySet()) {
			children.get(s).setParams(sgParams);
		}
	}
	
}
//