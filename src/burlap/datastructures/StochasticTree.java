package burlap.datastructures;

import burlap.debugtools.RandomFactory;

import java.util.*;



/**
 * A class for performing sampling of a set of objects at O(lg(n)) time. Elements can be added and removed dynamically. The "weights" of the objects
 * should be positive values, but do not have to specify a probability distribution. The sampling will be performed according to the relative weight
 * of all objects.
 * @author James MacGlashan
 *
 * @param <T> any Java object that will be sampled
 */
public class StochasticTree <T>{

	/**
	 * Root node of the stochastic tree
	 */
	protected STNode					root;
	
	/**
	 * A map from elements to the node that holds them.
	 */
	protected Map<T, STNode>			nodeMap;
	
	/**
	 * A random object used for sampling.
	 */
	protected Random					rand;
	
	
	
	/**
	 * Initializes with an empty tree.
	 */
	public StochasticTree(){
		this.init();
	}
	
	
	/**
	 * Initializes a tree for objects with the given weights
	 * @param weights the weights of a set objects that determine how likely they are to be sampled
	 * @param elements the elements of the tree that will be sampled
	 */
	public StochasticTree(List <Double> weights, List <T> elements){
		this.init();
		for(int i = 0; i < weights.size(); i++){
			this.insert(weights.get(i), elements.get(i));
		}
	}
	
	/**
	 * Initializes the three data structure
	 */
	protected void init(){
		root = null;
		nodeMap = new HashMap<T, StochasticTree<T>.STNode>();
		rand = RandomFactory.getMapped(2347636);
	}
	
	
	/**
	 * Sets the tree to use a specific random object when performing sampling
	 * @param r the random generator to use
	 */
	public void setRandom(Random r){
		this.rand = r;
	}
	
	/**
	 * Returns the number of objects in this tree
	 * @return the number of objects in this tree
	 */
	public int size(){
		return nodeMap.size();
	}
	
	
	/**
	 * Returns the pointer to the stored entry in this tree for the given query element. This method
	 * requires T to be a hashable object.
	 * @param el the element whose stored object in the tree is to be returned
	 * @return the pointer to the stored entry in this tree for the given query element
	 */
	public T getStoredEntry(T el){
		STNode node = nodeMap.get(el);
		if(node == null){
			return null;
		}
		return node.element;
	}
	
	/**
	 * Inserts the given element into the tree with the given weight
	 * @param w the weight of the element
	 * @param el the element to insert
	 */
	public void insert(double w, T el){
		
		if(root == null){
			root = new STNode(el, w, null);
		}
		else{
			this.insertHelper(root, w, el);
		}
		
	}
	
	
	/**
	 * Changes the weight of the given element. For this operation to be supported T must be hashable.
	 * @param element the element whose weight should be changed.
	 * @param w the new weight of the element.
	 */
	public void changeWeight(T element, double w){
		STNode node = nodeMap.get(element);
		double delta = w - node.width;
		node.width = w;
		if(node.parent != null){
			this.percolateWeightChange(node.parent, delta);
		}
	}
	
	
	/**
	 * Removes the given element from the tree. For this operation to be supported
	 * T must be hashable.
	 * @param element the element to remove
	 */
	public void remove(T element){
		STNode node = nodeMap.get(element);
		this.removeHelper(node);
	}
	
	/**
	 * Samples an element according to a probability defined by the relative weight of objects from the tree and returns it
	 * @return a sampled element
	 */
	public T sample(){
		if(root == null){
			return null;
		}
		double v = rand.nextDouble()*root.width;
		return sampleHelper(root, v).element;
	}
	
	
	/**
	 * Samples an element according to a probability defined by the relative weight of objects, removes it from the tree, and returns it. 
	 * @return a sampled element
	 */
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
	
	
	/**
	 * Helper recursive method for inserting an element
	 * @param node the node from which to insert the element
	 * @param w the weight of the element
	 * @param el the element to be inserted
	 */
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
	
	/**
	 * A recursive method for percolating a weight change of a node
	 * @param node the node whose weight change should be percolated
	 * @param delta the change in weight
	 */
	protected void percolateWeightChange(STNode node, double delta){
		node.width += delta;
		if(node.parent != null){
			this.percolateWeightChange(node.parent, delta);
		}
	}
	
	
	/**
	 * A recursive method for removing a node
	 * @param node the node to be removed
	 */
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
					percolateWeightChange(parent.parent, delta);
				}
			}
			else{
				System.err.println("DOUBLE STOCHASTIC TREE REMOVAL; THIS SHOULD NOT HAPPEN UNDER NORMAL USE");
				this.removeHelper(parent);
			}
			
			
		}
		
		
	}
	
	
	/**
	 * A recursive method for performing sampling
	 * @param node the node from which to sample
	 * @param v the random value used to determine which direction to go
	 * @return a sampled node
	 */
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
	
	
	
	/**
	 * A class for storing a stochastic tree node.
	 * @author James MacGlashan
	 *
	 */
	public class STNode{
		
		/**
		 * The element this node stores if it is a leaf node
		 */
		T			element;
		
		
		/**
		 * The total weight of all objects below this node
		 */
		double		width;
		
		
		/**
		 * The left subtree node
		 */
		STNode		left;
		
		/**
		 * The right subtree node
		 */
		STNode		right;
		
		
		/**
		 * This nodes parent
		 */
		STNode		parent;
		
		
		
		/**
		 * Initializes a leaf node with the given weight and parent
		 * @param el the element of the lead node
		 * @param w the weight of the element
		 * @param p the parent node
		 */
		public STNode(T el, double w, STNode p){
			element = el;
			width = w;
			parent = p;
			
			left = null;
			right = null;
			
			nodeMap.put(el, this);
		}
		
		/**
		 * Initializes a node with a weight only
		 * @param w the weight of the node
		 */
		public STNode(double w){
			element = null;
			width = w;
			parent = null;
			
			left = null;
			right = null;
		}
		
		
		/**
		 * Initializes a node with a given weight and parent node
		 * @param w the weight of the node
		 * @param p the parent pointer
		 */
		public STNode(double w, STNode p){
			element = null;
			width = w;
			parent = p;
			
			left = null;
			right = null;
		}
		
		
		/**
		 * Returns true if this is a left node
		 * @return true if this is a left node; false otherwise
		 */
		public boolean isLeaf(){
			return element != null;
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Demos how to use this class
	 * @param args empty
	 */
	public static void main(String[] args){
		
		System.out.println("Starting");
		test2();
		//test1();
		
	}
	
	/**
	 * An example usage
	 */
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
	
	/**
	 * Another example usage
	 */
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
