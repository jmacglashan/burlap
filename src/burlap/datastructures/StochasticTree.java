package burlap.datastructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import burlap.debugtools.RandomFactory;


public class StochasticTree <T>{

	protected STNode					root;
	protected Map<T, STNode>			nodeMap;
	protected Random					rand;
	
	
	
	
	public StochasticTree(){
		this.init();
	}
	
	
	public StochasticTree(List <Double> weights, List <T> elements){
		this.init();
		for(int i = 0; i < weights.size(); i++){
			this.insert(weights.get(i), elements.get(i));
		}
	}
	
	protected void init(){
		root = null;
		nodeMap = new HashMap<T, StochasticTree<T>.STNode>();
		rand = RandomFactory.getMapped(2347636);
	}
	
	public void setRandom(Random r){
		this.rand = r;
	}
	
	
	public int size(){
		return nodeMap.size();
	}
	
	public T getStoredEntry(T el){
		STNode node = nodeMap.get(el);
		if(node == null){
			return null;
		}
		return node.element;
	}
	
	public void insert(double w, T el){
		
		if(root == null){
			root = new STNode(el, w, null);
		}
		else{
			this.insertHelper(root, w, el);
		}
		
	}
	
	public void changeWeight(T element, double w){
		STNode node = nodeMap.get(element);
		double delta = w - node.width;
		node.width = w;
		if(node.parent != null){
			this.perculateWeightChange(node.parent, delta);
		}
	}
	
	public void remove(T element){
		STNode node = nodeMap.get(element);
		this.removeHelper(node);
	}
	
	public T sample(){
		if(root == null){
			return null;
		}
		double v = rand.nextDouble()*root.width;
		return sampleHelper(root, v).element;
	}
	
	public T poll(){
		if(root == null){
			return null;
		}
		double v = rand.nextDouble()*root.width;
		STNode node = this.sampleHelper(root, v);
		T el = node.element;
		this.removeHelper(node);
		return el;
	}
	
	protected void insertHelper(STNode node, double w, T el){
		if(!node.isLeaf()){
			node.width += w;
			if(node.left == null){
				node.left = new STNode(el, w, node);
			}
			else if(node.right == null){
				node.right = new STNode(el, w, node);
			}
			else{
				//this node has both a left and right child, so insert along path with least weight
				if(node.left.width < node.right.width){
					this.insertHelper(node.left, w, el);
				}
				else{
					this.insertHelper(node.right, w, el);
				}
			}
			
		}
		else{
			
			//it's a leaf and we need to split it
			node.left = new STNode(node.element, node.width, node);
			node.right = new STNode(el, w, node);
			
			//make internal node
			node.width += w;
			node.element = null;
			
		}
		
		
	}
	
	
	protected void perculateWeightChange(STNode node, double delta){
		node.width += delta;
		if(node.parent != null){
			this.perculateWeightChange(node.parent, delta);
		}
	}
	
	protected void removeHelper(STNode node){
		
		if(node.isLeaf()){
			nodeMap.remove(node.element);
		}
		
		double delta = -node.width;
		if(node.parent == null){
			root = null;
		}
		else{
			STNode parent = node.parent;
			STNode target;
			if(parent.left == node){
				//replace parent with right child
				target = parent.right;
				
			}
			else{
				//replace parent with left child
				target = parent.left;
			}
			if(target != null){
				parent.element = target.element;
				parent.width = target.width;
				parent.left = target.left;
				parent.right = target.right;
				
				if(parent.element != null){
					nodeMap.put(parent.element, parent);
				}
				
				if(parent.left != null){
					parent.left.parent = parent;
				}
				if(parent.right != null){
					parent.right.parent = parent;
				}
				
				/*
				if(parent.left != null && !parent.left.isLeaf()){
					parent.left.parent = parent;
				}
				if(parent.right != null && !parent.right.isLeaf()){
					parent.right.parent = parent;
				}
				*/
				
				
				if(parent.parent != null){
					perculateWeightChange(parent.parent, delta);
				}
			}
			else{
				System.err.println("DOUBLE STOCHASTIC TREE REMOVAL; THIS SHOULD NOT HAPPEN UNDER NORMAL USE");
				this.removeHelper(parent);
			}
			
			
		}
		
		
	}
	
	
	
	protected STNode sampleHelper(STNode node, double v){
		if(node.isLeaf()){
			return node;
		}
		double lval = node.left.width;
		if(v <= lval){
			return this.sampleHelper(node.left, v);
		}
		return this.sampleHelper(node.right, v-lval);
	}
	
	
	
	
	public class STNode{
		
		T			element;
		
		double		width;
		
		STNode		left;
		STNode		right;
		
		STNode		parent;
		
		
		public STNode(T el, double w, STNode p){
			element = el;
			width = w;
			parent = p;
			
			left = null;
			right = null;
			
			nodeMap.put(el, this);
		}
		
		
		public STNode(double w){
			element = null;
			width = w;
			parent = null;
			
			left = null;
			right = null;
		}
		
		public STNode(double w, STNode p){
			element = null;
			width = w;
			parent = p;
			
			left = null;
			right = null;
		}
		
		
		public boolean isLeaf(){
			return element != null;
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public static void main(String args []){
		
		System.out.println("Starting");
		test2();
		//test1();
		
	}
	
	
	public static void test2(){
		
		StochasticTree<Integer> st = new StochasticTree<Integer>();
		
		st.insert(1.,0);
		st.poll();
		st.insert(0.25, 1);
		st.insert(0.0625, 2);
		st.insert(0.015625, 3);
		st.insert(0.00390625, 4);
		st.insert(9.765625E-4, 5);
		st.insert(2.44140625E-4, 6);
		st.insert(6.103515625E-5, 7);
		st.insert(6.103515625E-5, 8);
		st.insert(1.52587890625E-5, 9);
		st.insert(9.5367431640625E-7, 10);
		st.insert(9.5367431640625E-7, 11);
		st.insert(2.384185791015625E-7, 12);
		
		
		st.remove(1);
		st.remove(4);
		System.out.println(st.poll());
		System.out.println(st.poll());
		System.out.println(st.poll());
		
		/*st.remove(2);
		st.remove(2);
		st.remove(2);*/
		
		/*
		System.out.println(st.poll());
		System.out.println(st.poll());
		System.out.println(st.poll());
		System.out.println(st.poll());
		System.out.println(st.poll());
		System.out.println(st.poll());
		*/
	}
	
	
	public static void test1(){
		
		List <Double> weights = new ArrayList<Double>();
		List <Integer> elements = new ArrayList<Integer>();
		
		weights.add(0.4);
		elements.add(0);
		
		weights.add(0.1);
		elements.add(1);
		
		weights.add(0.06);
		elements.add(2);
		
		weights.add(0.05);
		elements.add(3);
		
		weights.add(0.13);
		elements.add(4);
		
		weights.add(0.22);
		elements.add(5);
		
		weights.add(0.04);
		elements.add(6);
		
		StochasticTree <Integer> st = new StochasticTree<Integer>(weights, elements);
		
		System.out.println("size: " + st.size());
		
		int [] counts = new int[elements.size()];
		for(int i = 0; i < counts.length; i++){
			counts[i] = 0;
		}
		
		
		int n = 10000;
		for(int i = 0; i < n; i++){
			int el = st.sample();
			counts[el]++;
		}
		
		for(int i = 0; i < counts.length; i++){
			System.out.printf("%.2f : %.5f\n", weights.get(i), (double)counts[i]/(double)n);
		}
	
	
		System.out.println("----------------");
		
		//now test removal
		st.remove(0);
		
		System.out.println("size: " + st.size());
		
		for(int i = 0; i < counts.length; i++){
			counts[i] = 0;
		}
		
		
		for(int i = 0; i < n; i++){
			int el = st.sample();
			counts[el]++;
		}
		
		for(int i = 0; i < counts.length; i++){
			double sample = counts[i] > 0 ? (double)counts[i]/(double)n : 0.;
			System.out.printf("%.2f : %.5f\n", weights.get(i)/(1. - 0.4), sample);
		}
		
		
		System.out.println("-----------------");
		
		int polled = st.poll();
		double pweight = weights.get(polled);
		
		System.out.println("size: " + st.size());
		
		for(int i = 0; i < counts.length; i++){
			counts[i] = 0;
		}
		
		for(int i = 0; i < n; i++){
			int el = st.sample();
			counts[el]++;
		}
		
		for(int i = 0; i < counts.length; i++){
			double sample = counts[i] > 0 ? (double)counts[i]/(double)n : 0.;
			System.out.printf("%.2f : %.5f\n", weights.get(i)/(1. - 0.4 - pweight), sample);
		}
		
		
		System.out.println("-----------------");
		
		while(st.size() > 0){
			Integer res = st.poll();
			System.out.println(res);
		}
		
		System.out.println("size: " + st.size());
		
		Integer res = st.poll();
		if(res == null){
			System.out.println("tree is now empty");
		}
		
		
	}
	
	
}
