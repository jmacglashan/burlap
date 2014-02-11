package burlap.domain.singleagent.minecraft;

import java.util.List;

import burlap.oomdp.singleagent.Action;

public abstract class Node {
	
	private List<Node> children;
	private Node parent;
	private List<Action> actionList;
	
	public List<Node> getChildren() {
		return children;
	}
	
	public void addChild(Node ch) {
		ch.setParent(this);
		children.add(ch);
	}

	public Node getParent() {
		return parent;
	}
	
	public void setParent(Node p) {
		parent = p;
	}
	
	public List<Action> getActions() {
		return actionList;
	}
	
	public void addAction(Action a) {
		actionList.add(a);
	}

}