package burlap.datastructures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//all methods are implemented for max heap; however, setting the max heap boolean to false causes the comparisons of all objects in this
//method to flip integer sign, thereby making it a min heap

public class HashIndexedHeap <T> {

	protected List<T> 						nodesArray;
	protected int							size;
	protected Map<T, Integer>				arrayIndexMap;
	protected boolean						maxHeap;
	protected Comparator<T>					priorityCompare;
	
	
	
	
	
	
	public HashIndexedHeap(Comparator<T> pcompare){
		nodesArray = new ArrayList<T>();
		arrayIndexMap = new HashMap<T, Integer>();
		size = 0;
		maxHeap = true;
		priorityCompare = pcompare;
	}
	
	public HashIndexedHeap(Comparator<T> pcompare, int capacity){
		
		nodesArray = new ArrayList<T>(capacity);
		arrayIndexMap = new HashMap<T, Integer>(capacity);
		size = 0;
		maxHeap = true;
		priorityCompare = pcompare;
		
	}
	
	public int size(){
		return size;
	}
	
	public void setUseMaxHeap(boolean max){
		this.maxHeap = max;
	}
	
	
	//returns of a ptr to the instance matching this
	public T containsInstance(T inst){
		Integer I = arrayIndexMap.get(inst);
		if(I == null){
			return null;
		}
		int i = I;
		return nodesArray.get(i);
	}
	
	public T peek(){
		if(size == 0){
			return null;
		}
		
		return nodesArray.get(0);
	}
	
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
	
	public void refreshPriority(T el){
		Integer I = arrayIndexMap.get(el);
		if(I == null){
			return ; //el is not in the priority queue
		}
		
		int i = I;
		this.refreshPriority(i, el);
		
		
	}
	
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
	
	
	//returns true if changes had to be made
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
		return (2*(i+1));
	}
	
}
