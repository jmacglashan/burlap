package burlap.behavior.singleagent.auxiliary;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.SADomain;


/**
 * For some algorithms, it is useful to have an explicit unique state identifier for each possible state and the hashcode of a state cannot reliably give
 * a unique number. This class is used to take hashable states and assign them a unique number. States can be enumerated iteratively on an as needed basis
 * or all reachable states from a source state can be queried to seed the list of states. This class can also go in the reverse direction
 * by returning the state associated with an enumeration id, as long as that id has already been assigned to a state.
 * @author James MacGlashan
 *
 */
public class StateEnumerator {

	/**
	 * The domain whose states will be enumerated
	 */
	protected Domain								domain;
	
	/**
	 * The hashing factory used to hash states and perform equality tests
	 */
	protected HashableStateFactory hashingFactory;
	
	/**
	 * The forward state enumeration map
	 */
	protected Map<HashableState, Integer> 			enumeration = new HashMap<HashableState, Integer>();
	
	/**
	 * The reverse enumeration id to state map
	 */
	protected Map<Integer, State>					reverseEnumerate = new HashMap<Integer, State>();
	
	
	/**
	 * The id to use for the next unique state that is added
	 */
	protected int									nextEnumeratedID = 0;
	
	
	
	/**
	 * Constructs
	 * @param domain the domain of the states to be enumerated
	 * @param hashingFactory the hashing factory to use
	 */
	public StateEnumerator(Domain domain, HashableStateFactory hashingFactory){
		this.domain = domain;
		this.hashingFactory = hashingFactory;
	}
	
	
	/**
	 * Finds all states that are reachable from an input state and enumerates them
	 * @param from the state from which all reachable states should be searched
	 */
	public void findReachableStatesAndEnumerate(State from){
		Set<HashableState> reachable = StateReachability.getReachableHashedStates(from, (SADomain)this.domain, this.hashingFactory);
		for(HashableState sh : reachable){
			this.getEnumeratedID(sh);
		}
	}
	
	
	/**
	 * Finds all states that are reachable from an input state and enumerates them. 
	 * Will not search from states that are marked as terminal states.
	 * @param from the state from which all reachable states should be searched
	 * @param tf the terminal function that prevents expanding from terminal states
	 */
	public void findReachableStatesAndEnumerate(State from, TerminalFunction tf){
		Set<HashableState> reachable = StateReachability.getReachableHashedStates(from, (SADomain)this.domain, this.hashingFactory, tf);
		for(HashableState sh : reachable){
			this.getEnumeratedID(sh);
		}
	}
	
	
	/**
	 * Get or create and get the enumeration id for a state
	 * @param s the state to get the enumeration id
	 * @return the enumeration id
	 */
	public int getEnumeratedID(State s){
		HashableState sh = this.hashingFactory.hashState(s);
		return this.getEnumeratedID(sh);
	}
	
	
	/**
	 * Returns the state associated with the given enumeration id.
	 * A state must have previously be associated with the input enumeration id, or a runtime exception is thrown.
	 * @param id the enumeration id
	 * @return the state associated with the given enumeration id.
	 */
	public State getStateForEnumertionId(int id){
		State s = this.reverseEnumerate.get(id);
		if(s == null){
			throw new RuntimeException("There is no state stored with the enumeration id: " + id);
		}
		return s;
	}
	
	
	/**
	 * Returns the number of states that have been enumerated
	 * @return the number of states that have been enumerated
	 */
	public int numStatesEnumerated(){
		return this.enumeration.size();
	}
	
	
	
	/**
	 * Get or create and get the enumeration id for a hashed state
	 * @param sh the hased state to get the enumeration id
	 * @return the enumeration id
	 */
	protected int getEnumeratedID(HashableState sh){
		Integer storedID = this.enumeration.get(sh);
		if(storedID == null){
			this.enumeration.put(sh, this.nextEnumeratedID);
			this.reverseEnumerate.put(this.nextEnumeratedID, sh.s);
			storedID = this.nextEnumeratedID;
			this.nextEnumeratedID++;
		}
		return storedID;
	}
	
	
}
