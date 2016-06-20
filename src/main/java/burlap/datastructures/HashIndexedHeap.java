package burlap.datastructures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


//developer note: all methods are implemented for max heap; however, setting the max heap boolean to false causes the comparisons of all objects in this
//method to flip integer sign, thereby making it a min heap


/**
 * An implementation of a hash map backed heap/priority queue. This data structure allows efficient O(1) "contains" checks and efficient
 * O(lg(n)) modifications of entries already in the heap that with a typical Java PriorityQueue would require linear updates. This data structure is especially
 * useful for planning algorithms like A* that keep a priority queue of states, but may need to update their priority if a better path to them is found.
 * <p>
 * By default, the heap is a max heap (which results in elements with the highest priority being dequeued first), but it may also be set to be a min heap.
 * 
 * @author James MacGlashan
 *
 * @param <T> any Java object
 */
public class HashIndexedHeap <T> implements Iterable<T>{

	/**
	 * Heap ordered list of objects
	 */
	protected List<T> 						nodesArray;
	
	/**
	 * Number of objects in the heap
	 */
	protected int							size;
	
	/**
	 * Hash map from objects to their index in the heap
	 */
	protected Map<T, Integer>				arrayIndexMap;
	
	/**
	 * If true, this is ordered according to a max heap; if false ordered according to a min heap.
	 */
	protected boolean						maxHeap;
	
	/**
	 * A comparator to compare objects
	 */
	protected Comparator<T>					priorityCompare;
	
	
	
	
	
	/**
	 * Initializes the heap with a comparator
	 * @param pcompare the comparator to compare the priority of elements of this heap
	 */
	public HashIndexedHeap(Comparator<T> pcompare){
		nodesArray = new ArrayList<T>();
		arrayIndexMap = new HashMap<T, Integer>();
		size = 0;
		maxHeap = true;
		priorityCompare = pcompare;
	}
	
	
	/**
	 * Initializes the heap with a comparator and an initial capacity size
	 * @param pcompare the comparator to compare the priority of elements of this heap
	 * @param capacity the initial compacity size of the heap
	 */
	public HashIndexedHeap(Comparator<T> pcompare, int capacity){
		
		nodesArray = new ArrayList<T>(capacity);
		arrayIndexMap = new HashMap<T, Integer>(capacity);
		size = 0;
		maxHeap = true;
		priorityCompare = pcompare;
		
	}
	
	
	/**
	 * Returns the size of the heap
	 * @return the size of the heap
	 */
	public int size(){
		return size;
	}
	
	
	/**
	 * Sets whether this heap is a max heap or a min heap
	 * @param max if true, sets to be ma heap; if false sets to be min heap.
	 */
	public void setUseMaxHeap(boolean max){
		this.maxHeap = max;
	}
	
	

	/**
	 * Checks if the heap contains this object and returns the pointer to the stored object if it does; otherwise null is returned.
	 * @param inst the inst to look for in the heap
	 * @return the pointer to the stored object if it is in the heap; null otherwise.
	 */
	public T containsInstance(T inst){
		Integer I = arrayIndexMap.get(inst);
		if(I == null){
			return null;
		}
		int i = I;
		return nodesArray.get(i);
	}
	
	
	/**
	 * Returns a pointer to the head of the heap without removing it
	 * @return a pointer to the head of the heap
	 */
	public T peek(){
		if(size == 0){
			return null;
		}
		
		return nodesArray.get(0);
	}
	
	
	/**
	 * Returns a pointer to the head of the heap and removes it
	 * @return a pointer to the head of the heap
	 */
	public T poll(){
		
		if(size == 0){
			return null;
		}
		
		T top = this.peek();
		arrayIndexMap.remove(top);
		
		if(size != 1){
			T last = nodesArray.get(size-1);
			this.set(0, last);
			size--;
			this.maxHeapify(0);
		}
		else{
			size--;
		}
		nodesArray.remove(size);
		
		return top;
	}
	
	
	/**
	 * Inserts the element into the heap
	 * @param el the element to be inserted
	 */
	public void insert(T el){
		
		int i = size;
		size++;
		if(nodesArray.size() < size){
			nodesArray.add(el);
			arrayIndexMap.put(el, i);
		}
		else{
			this.set(i, el);
		}
		
		this.refreshPriority(i, el);
		
	}
	
	
	/**
	 * Calling this method indicates that the priority of the object passed to the method has been modified and that this heap needs to reorder its elements
	 * as a result
	 * @param el the element whose priority was modified
	 */
	public void refreshPriority(T el){
		Integer I = arrayIndexMap.get(el);
		if(I == null){
			return ; //el is not in the priority queue
		}
		
		int i = I;
		this.refreshPriority(i, el);
		
		
	}
	
	@Override
	public Iterator<T> iterator() {
		return this.nodesArray.iterator();
	}
	
	
	/**
	 * This method returns whether the data structure stored is in fact a heap (costs linear time).
	 * This method should only be used for debug purposes such as when a heap's elements have their priority
	 * changed from multiple sources and it needs to be made sure that each element is being properly refreshed
	 * within the heap.
	 * Note that to be a heap, each node must be greater than or equal to its children.
	 * @return true if the stored data is a valid heap; false otherwise.
	 */
	public boolean satisifiesHeap(){
		
		for(int i = 0; i < this.nodesArray.size(); i++){
			T n = this.nodesArray.get(i);
			int l = this.left(i);
			if(l < this.nodesArray.size()){
				T ln = this.nodesArray.get(l);
				if(this.compare(n, ln) < 0){
					return false;
				}
			}
			
			int r = this.right(i);
			if(r < this.nodesArray.size()){
				T rn = this.nodesArray.get(r);
				if(this.compare(n, rn) < 0){
					return false;
				}
			}
		}
		
		return true;
		
	}
	
	
	/**
	 * Adjusts the heap position of the given element 
	 * @param i the index of the given element
	 * @param el the element to re-index
	 */
	private void refreshPriority(int i, T el){
		
		boolean shiftedDown = this.maxHeapify(i);
		
		if(!shiftedDown){
			//if it didn't shift down then we might need to push it up the tree
			if(i > 0){
				T cur = el;
				int p = this.parent(i);
				T pel = nodesArray.get(p);
				while(i > 0 && this.compare(pel, cur) < 0){
					this.set(p, cur);
					this.set(i, pel);
					i = p;
					p = this.parent(i);
					if(i > 0){
						pel = nodesArray.get(p);
					}
				}
				
			}
		}
	}
	
	

	/**
	 * Performs the heapify operation
	 * @param i the index on which to perform heapify
	 * @return true if changes had to be made; false otherwise.
	 */
	private boolean maxHeapify(int i){
		
		int l = this.left(i);
		int r = this.right(i);
		
		T ni = nodesArray.get(i);
		
		int largest = i;
		T nlargest = ni;
		
		if(l < size){
			T nl = nodesArray.get(l);
			if(this.compare(nl, ni) > 0){
				largest = l;
				nlargest = nl;
			}
			
		}
		
		if(r < size){
			T nr = nodesArray.get(r);
			if(this.compare(nr, nlargest) > 0){
				largest = r;
				nlargest = nr;
			}
		}
		
		if(largest != i){
			//swap
			this.set(i, nlargest);
			this.set(largest, ni);
			
			this.maxHeapify(largest);
			return true;
		}
		
		return false;
		
	}
	
	private void set(int i, T e){
		nodesArray.set(i, e);
		arrayIndexMap.put(e, i);	
	}
	
	private int compare(T a, T b){
		int signFlip = 1;
		if(!maxHeap){
			signFlip = -1;
		}
		
		return signFlip * priorityCompare.compare(a, b);
		
	}
	
	
	private int parent(int i){
		return ((i+1) / 2)-1;
	}
	
	private int left(int i ){
		return (2*(i+1))-1;
	}
	
	private int right(int i ){
		return 2*(i+1);
	}

	
}
