package burlap.domain.singleagent.minecraft;

import java.util.List;

import burlap.oomdp.singleagent.Action;

public abstract class OldAffordanceNode {
	
	private List<OldAffordanceNode> children;
	private OldAffordanceNode parent;
	private List<Action> actionList;
	
	public List<OldAffordanceNode> getChildren() {
		return children;
	}
	
	public void addChild(OldAffordanceNode ch) {
		ch.setParent(this);
		children.add(ch);
	}

	public OldAffordanceNode getParent() {
		return parent;
	}
	
	public void setParent(OldAffordanceNode p) {
		parent = p;
	}
	
	public List<Action> getActions() {
		return actionList;
	}
	
	public void addAction(Action a) {
		actionList.add(a);
	}

}