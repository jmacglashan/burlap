package burlap.behavior.singleagent.auxiliary;

import burlap.behavior.policy.Policy;
import burlap.debugtools.DPrint;
import burlap.debugtools.MyTimer;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.HashableStateFactory;

import java.util.*;


/**
 * This class provides methods for finding the set of reachable states from a source state. Alternate methods
 * for finding the reachable states under a specific policy are also provided. See the Java doc for each
 * method for more information.
 * @author James MacGlashan
 *
 */
public class StateReachability {
	
	/**
	 * The debugID used for making calls to {@link burlap.debugtools.DPrint}.
	 */
	public static final int			debugID = 837493;
	
	private StateReachability() {
	    // do nothing
	}
	
	
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
		long firstTime = System.currentTimeMillis();
		long lastTime = firstTime;
		while(openList.size() > 0){
			HashableState sh = openList.poll();
			if(tf.isTerminal(sh.s)){
				continue; //don't expand
			}
			
			List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(actions, sh.s);
			for(GroundedAction ga : gas){
				List <TransitionProbability> tps = ga.getTransitions(sh.s);
				nGenerated += tps.size();
				for(TransitionProbability tp : tps){
					HashableState nsh = usingHashFactory.hashState(tp.s);
					
					if (hashedStates.add(nsh)) {
						openList.offer(nsh);
					}
				}
			}
			
			long currentTime = System.currentTimeMillis();
			if (currentTime - 1000 >= lastTime) {
				DPrint.cl(debugID, "Num generated: " + (nGenerated) + " Unique: " + (hashedStates.size()) + 
						" time: " + ((double)currentTime - firstTime)/1000.0);				
				lastTime = currentTime;
			}
		}
		
		DPrint.cl(debugID, "Num generated: " + nGenerated + "; num unique: " + hashedStates.size());
		
		return hashedStates;
	}



	/**
	 * Finds the set of states that are reachable under a policy from a source state. Reachability under a source policy means
	 * that the space of actions considered are those that have non-zero probability of being selected by the
	 * policy and all possible outcomes of those states are considered.
	 * @param p the policy that must be followed
	 * @param from the source {@link burlap.oomdp.core.states.State} from which the policy would be initiated.
	 * @param usingHashFactory the {@link burlap.oomdp.statehashing.HashableStateFactory} used to hash states and test equality.
	 * @return a {@link java.util.List} of {@link burlap.oomdp.core.states.State} objects that could be reached.
	 */
	public static List<State> getPolicyReachableStates(Policy p, State from, HashableStateFactory usingHashFactory){
		return getPolicyReachableStates(p, from, usingHashFactory, new NullTermination());
	}


	/**
	 * Finds the set of states that are reachable under a policy from a source state. Reachability under a source policy means
	 * that the space of actions considered are those that have non-zero probability of being selected by the
	 * policy and all possible outcomes of those states are considered.
	 * @param p the policy that must be followed
	 * @param from the source {@link burlap.oomdp.core.states.State} from which the policy would be initiated.
	 * @param usingHashFactory the {@link burlap.oomdp.statehashing.HashableStateFactory} used to hash states and test equality.
	 * @param tf a {@link burlap.oomdp.core.TerminalFunction} that prevents further state expansion from states that are terminal states.
	 * @return a {@link java.util.List} of {@link burlap.oomdp.core.states.State} objects that could be reached.
	 */
	public static List<State> getPolicyReachableStates(Policy p, State from, HashableStateFactory usingHashFactory, TerminalFunction tf){
		Set<HashableState> hashedStates = getPolicyReachableHashedStates(p, from, usingHashFactory, tf);
		List <State> states = new ArrayList<State>(hashedStates.size());
		for(HashableState sh : hashedStates){
			states.add(sh.s);
		}

		return states;
	}



	/**
	 * Finds the set of states ({@link burlap.oomdp.statehashing.HashableState}) that are reachable under a policy from a source state. Reachability under a source policy means
	 * that the space of actions considered are those that have non-zero probability of being selected by the
	 * policy and all possible outcomes of those states are considered.
	 * @param p the policy that must be followed
	 * @param from the source {@link burlap.oomdp.core.states.State} from which the policy would be initiated.
	 * @param usingHashFactory the {@link burlap.oomdp.statehashing.HashableStateFactory} used to hash states and test equality.
	 * @return a {@link java.util.Set} of {@link burlap.oomdp.statehashing.HashableState} objects that could be reached.
	 */
	public static Set<HashableState> getPolicyReachableHashedStates(Policy p, State from, HashableStateFactory usingHashFactory){
		return getPolicyReachableHashedStates(p, from, usingHashFactory, new NullTermination());
	}


	/**
	 * Finds the set of states ({@link burlap.oomdp.statehashing.HashableState}) that are reachable under a policy from a source state. Reachability under a source policy means
	 * that the space of actions considered are those that have non-zero probability of being selected by the
	 * policy and all possible outcomes of those states are considered.
	 * @param p the policy that must be followed
	 * @param from the source {@link burlap.oomdp.core.states.State} from which the policy would be initiated.
	 * @param usingHashFactory the {@link burlap.oomdp.statehashing.HashableStateFactory} used to hash states and test equality.
	 * @param tf a {@link burlap.oomdp.core.TerminalFunction} that prevents further state expansion from states that are terminal states.
	 * @return a {@link java.util.Set} of {@link burlap.oomdp.statehashing.HashableState} objects that could be reached.
	 */
	public static Set<HashableState> getPolicyReachableHashedStates(Policy p, State from, HashableStateFactory usingHashFactory, TerminalFunction tf){

		Set<HashableState> hashedStates = new HashSet<HashableState>();
		HashableState shi = usingHashFactory.hashState(from);
		int nGenerated = 0;

		LinkedList <HashableState> openList = new LinkedList<HashableState>();
		openList.offer(shi);
		hashedStates.add(shi);

		MyTimer timer = new MyTimer(true);
		while(openList.size() > 0){
			HashableState sh = openList.poll();
			if(tf.isTerminal(sh.s)){
				continue; //don't expand
			}

			List<Policy.ActionProb> policyActions = p.getActionDistributionForState(sh.s);
			for(Policy.ActionProb ap : policyActions){
				if(ap.pSelection > 0){
					List <TransitionProbability> tps = ((GroundedAction)ap.ga).getTransitions(sh.s);
					nGenerated += tps.size();
					for(TransitionProbability tp : tps){
						HashableState nsh = usingHashFactory.hashState(tp.s);

						if (hashedStates.add(nsh)) {
							openList.offer(nsh);
						}
					}
				}
			}

			if(timer.peekAtTime() > 1){
				timer.stop();
				DPrint.cl(debugID, "Num generated: " + (nGenerated) + " Unique: " + (hashedStates.size()) +
						" time: " + timer.getTime());
				timer.start();
			}
		}

		timer.stop();

		DPrint.cl(debugID, "Num generated: " + nGenerated + "; num unique: " + hashedStates.size());

		return hashedStates;
	}

}
