package burlap.domain.singleagent.minecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Affordance{
	
	private String name;
	private HashMap<String,Subgoal> children; // Subgoals
	
	public Affordance(String name) {
		this.name = name;
		this.children = new HashMap<String,Subgoal>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public HashMap<String,Subgoal> getChildren() {
		return this.children;
	}
	
	public void addChild(Subgoal sg) {
		this.children.put(sg.getName(), sg);
	}
	
	public List<Subgoal> getSubgoals() {
		return new ArrayList<Subgoal>(children.values());
	}
	
	public void setSubGoalParams(String[] sgParams) {
		for(String s: children.keySet()) {
			children.get(s).setParams(sgParams);
		}
	}
	
}
//