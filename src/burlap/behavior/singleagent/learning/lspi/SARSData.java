package burlap.behavior.singleagent.learning.lspi;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * Class that provides a wrapper for a List holding a bunch of state-action-reward-state ({@link SARS}) tuples. The dataset is backed by an {@link ArrayList}.
 * @author James MacGlashan
 *
 */
public class SARSData {

	/**
	 * The underlying list of {@link SARS} tuples.
	 */
	public List<SARS>		dataset;
	
	
	/**
	 * Initializes with an empty dataset
	 */
	public SARSData(){
		this.dataset = new ArrayList<SARSData.SARS>();
	}
	
	/**
	 * Initializes with an empty dataset with initial capacity for the given parameter available.
	 * @param initialCapacity the initial capacity of the dataset.
	 */
	public SARSData(int initialCapacity){
		this.dataset = new ArrayList<SARSData.SARS>(initialCapacity);
	}
	
	/**
	 * The number of SARS tuples stored.
	 * @return number of SARS tuples stored.
	 */
	public int size(){
		return this.dataset.size();
	}
	
	/**
	 * Returns the {@link SARS} tuple for the ith dataset element.
	 * @param i the index of the dataset
	 * @return the ith {@link SARS} tuple.
	 */
	public SARS get(int i){
		return this.dataset.get(i);
	}
	
	/**
	 * Adds the given {@link SARS} tuple.
	 * @param sars {@link SARS} tuple to add.
	 */
	public void add(SARS sars){
		this.dataset.add(sars);
	}
	
	
	/**
	 * Adds a {@link SARS} tuple with the given component.
	 * @param s the previous state
	 * @param a the action taken in the previous state
	 * @param r the resulting reward received
	 * @param sp the next state
	 */
	public void add(State s, GroundedAction a, double r, State sp){
		this.dataset.add(new SARS(s, a, r, sp));
	}
	
	/**
	 * Removes the {@link SARS} tuple at the ith index
	 * @param i the index of {@link SARS} tuple to remove.
	 */
	public void remove(int i){
		this.dataset.remove(i);
	}
	
	/**
	 * Clears this dataset of all elements.
	 */
	public void clear(){
		this.dataset.clear();
	}
	
	
	/**
	 * State-action-reward-state tuple.
	 * @author James MacGlashan
	 *
	 */
	public static class SARS{
		
		/**
		 * The previou state
		 */
		public State			s;
		
		/**
		 * The action taken inthe previous state
		 */
		public GroundedAction	a;
		
		/**
		 * The resulting reward received
		 */
		public double			r;
		
		/**
		 * The next state
		 */
		public State			sp;
		
		
		/**
		 * Initializes.
		 * @param s the previous state
		 * @param a the action taken in the previous state
		 * @param r the resulting reward received
		 * @param sp the next state
		 */
		public SARS(State s, GroundedAction a, double r, State sp){
			this.s = s;
			this.a = a;
			this.r = r;
			this.sp = sp;
		}
		
	}
}
