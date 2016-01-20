package burlap.domain.singleagent.minecraft;

import java.util.ArrayList;

import burlap.oomdp.core.PropositionalFunction;

public class Node {
	
	private Node parent;
	private PropositionalFunction pf;
	private ArrayList<Node> children;

	public Node(PropositionalFunction pf, Node p) {
		this.parent = p;
		this.pf = pf;
		this.children = new ArrayList<Node>();
	}
	
	public Node getParent(){
		return this.parent;
	}
	
	public void setParent(Node p) {
		this.parent = p;
	}
	
	public void addChild(Node c){
		this.children.add(c);
	}
	
	public ArrayList<Node> getChildren() {
		return this.children;
	}
	
	public PropositionalFunction getPropFunc() {
		return this.pf;
	}

}
