package burlap.domain.singleagent.minecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class OldAffordance{
	
	private String name;
	private HashMap<String,OldAffordanceSubgoal> children; // Subgoals
	
	public OldAffordance(String name) {
		this.name = name;
		this.children = new HashMap<String,OldAffordanceSubgoal>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public HashMap<String,OldAffordanceSubgoal> getChildren() {
		return this.children;
	}
	
	public void addChild(OldAffordanceSubgoal sg) {
		this.children.put(sg.getName(), sg);
	}
	
	public List<OldAffordanceSubgoal> getSubgoals() {
		return new ArrayList<OldAffordanceSubgoal>(children.values());
	}
	
	public void setSubGoalParams(String[] sgParams) {
		for(String s: children.keySet()) {
			children.get(s).setParams(sgParams);
		}
	}
	
}
//