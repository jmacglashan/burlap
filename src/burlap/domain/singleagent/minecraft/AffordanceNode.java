package burlap.domain.singleagent.minecraft;

import java.util.List;

import burlap.oomdp.singleagent.Action;

public abstract class AffordanceNode {
	
	private List<AffordanceNode> children;
	private AffordanceNode parent;
	private List<Action> actionList;
	
	public List<AffordanceNode> getChildren() {
		return children;
	}
	
	public void addChild(AffordanceNode ch) {
		ch.setParent(this);
		children.add(ch);
	}

	public AffordanceNode getParent() {
		return parent;
	}
	
	public void setParent(AffordanceNode p) {
		parent = p;
	}
	
	public List<Action> getActions() {
		return actionList;
	}
	
	public void addAction(Action a) {
		actionList.add(a);
	}

}