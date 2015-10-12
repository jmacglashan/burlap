package burlap.behavior.singleagent.auxiliary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import burlap.debugtools.DPrint;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.HashableStateFactory;


/**
 * This class provides methods for finding the set of reachable states from a source state.
 * @author James MacGlashan
 *
 */
public class StateReachability {
	
	/**
	 * The debugID used for making calls to {@link burlap.debugtools.DPrint}.
	 */
	public static int			debugID = 837493;
	
	
	/**
	 * Returns the list of {@link burlap.oomdp.core.states.State} objects that are reachable from a source state.
	 * @param from the source state
	 * @param inDomain the domain of the state
	 * @param usingHashFactory the state hashing factory to use for indexing states and testing equality.
	 * @return the list of {@link burlap.oomdp.core.states.State} objects that are reachable from a source state.
	 */
	public static List <State> getReachableStates(State from, SADomain inDomain, HashableStateFactory usingHashFactory){
		return getReachableStates(from, inDomain, usingHashFactory, new NullTermination());
	}
	
	
	/**
	 * Returns the list of {@link burlap.oomdp.core.states.State} objects that are reachable from a source state.
	 * @param from the source state
	 * @param inDomain the domain of the state
	 * @param usingHashFactory the state hashing factory to use for indexing states and testing equality.
	 * @param tf a terminal function that prevents expansion from terminal states.
	 * @return the list of {@link burlap.oomdp.core.states.State} objects that are reachable from a source state.
	 */
	public static List <State> getReachableStates(State from, SADomain inDomain, HashableStateFactory usingHashFactory, TerminalFunction tf){
		Set <HashableState> hashedStates = getReachableHashedStates(from, inDomain, usingHashFactory, tf);
		List <State> states = new ArrayList<State>(hashedStates.size());
		for(HashableState sh : hashedStates){
			states.add(sh.s);
		}
		
		return states;
	}
	
	
	/**
	 * Returns the set of {@link burlap.oomdp.core.states.State} objects that are reachable from a source state.
	 * @param from the source state
	 * @param inDomain the domain of the state
	 * @param usingHashFactory the state hashing factory to use for indexing states and testing equality.
	 * @return the set of {@link burlap.oomdp.core.states.State} objects that are reachable from a source state.
	 */
	public static Set <HashableState> getReachableHashedStates(State from, SADomain inDomain, HashableStateFactory usingHashFactory){
		return getReachableHashedStates(from, inDomain, usingHashFactory, new NullTermination());
	}
	
	
	
	/**
	 * Returns the set of {@link burlap.oomdp.core.states.State} objects that are reachable from a source state.
	 * @param from the source state
	 * @param inDomain the domain of the state
	 * @param usingHashFactory the state hashing factory to use for indexing states and testing equality.
	 * @param tf a terminal function that prevents expansion from terminal states.
	 * @return the set of {@link burlap.oomdp.core.states.State} objects that are reachable from a source state.
	 */
	public static Set <HashableState> getReachableHashedStates(State from, SADomain inDomain, HashableStateFactory usingHashFactory, TerminalFunction tf){
		
		
		Set<HashableState> hashedStates = new HashSet<HashableState>();
		HashableState shi = usingHashFactory.hashState(from);
		List <Action> actions = inDomain.getActions();
		int nGenerated = 0;
		
		LinkedList <HashableState> openList = new LinkedList<HashableState>();
		openList.offer(shi);
		hashedStates.add(shi);
		long lastTime = System.currentTimeMillis();
		int lastSize = 1;
		int lastGenerated = 1;
		int numProcessed = 1;
		int lastProcessed = 1;
		while(openList.size() > 0){
			HashableState sh = openList.poll();
			numProcessed++;
			if(tf.isTerminal(sh.s)){
				continue; //don't expand
			}
			
			List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(actions, sh.s);
			for(GroundedAction ga : gas){
				List <TransitionProbability> tps = ga.getTransitions(sh.s);
				nGenerated += tps.size();
				for(TransitionProbability tp : tps){
					//openList.offer(sh);
					HashableState nsh = usingHashFactory.hashState(tp.s);
					
					if (hashedStates.add(nsh)) {
						openList.offer(nsh);
					}
				}
			}
			long currentTime = System.currentTimeMillis();
			if (currentTime - 1000 > lastTime) {
				System.out.println("Processed " + (numProcessed-lastProcessed) + " Size: " + (hashedStates.size()-lastSize) + " generated: " + (nGenerated - lastGenerated) + " time: " + ((double)currentTime - lastTime)/1000.0);
				lastTime = currentTime;
				lastSize = hashedStates.size();
				lastGenerated = nGenerated;
				lastProcessed = numProcessed;
				
			}
		}
		
		DPrint.cl(debugID, "Num generated: " + nGenerated + "; num unique: " + hashedStates.size());
		
		return hashedStates;
	}
	
	private static void checkHashing(Set<HashableState> hashed, HashableState nsh) {
		for (HashableState hs : hashed) {
			
			if (hs.equals(nsh) && hs.hashCode() != nsh.hashCode()) {
				boolean eq = hs.equals(nsh);
				throw new RuntimeException("Equality issue");
			}
			ObjectInstance agent1 = hs.getObject("agent0");
			ObjectInstance agent2 = nsh.getObject("agent0");
			if (agent1.equals(agent2) && !hs.equals(nsh)) {
				boolean eq = hs.equals(nsh);
				throw new RuntimeException("Equality issue");
			}
		}
	}
}
