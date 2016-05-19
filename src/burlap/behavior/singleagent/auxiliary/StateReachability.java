package burlap.behavior.singleagent.auxiliary;

import burlap.behavior.policy.Policy;
import burlap.debugtools.DPrint;
import burlap.debugtools.MyTimer;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.ActionType;
import burlap.mdp.singleagent.action.ActionUtils;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

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
	 * Returns the list of {@link State} objects that are reachable from a source state.
	 * @param from the source state
	 * @param inDomain the domain of the state
	 * @param usingHashFactory the state hashing factory to use for indexing states and testing equality.
	 * @return the list of {@link State} objects that are reachable from a source state.
	 */
	public static List <State> getReachableStates(State from, SADomain inDomain, HashableStateFactory usingHashFactory){
		Set<HashableState> hashed = getReachableHashedStates(from, inDomain, usingHashFactory);
		List<State> states = new ArrayList<State>(hashed.size());
		for(HashableState sh : hashed){
			states.add(sh.s());
		}
		return states;

	}

	
	
	/**
	 * Returns the set of {@link State} objects that are reachable from a source state.
	 * @param from the source state
	 * @param inDomain the domain of the state
	 * @param usingHashFactory the state hashing factory to use for indexing states and testing equality.
	 * @return the set of {@link State} objects that are reachable from a source state.
	 */
	public static Set <HashableState> getReachableHashedStates(State from, SADomain inDomain, HashableStateFactory usingHashFactory){

		if(!(inDomain.getModel() instanceof FullModel)){
			throw new RuntimeException( "State reachablity requires a domain with a FullModel, but one is not provided");
		}

		FullModel model = (FullModel)inDomain.getModel();

		Set<HashableState> hashedStates = new HashSet<HashableState>();
		HashableState shi = usingHashFactory.hashState(from);
		List <ActionType> actionTypes = inDomain.getActionTypes();
		int nGenerated = 0;
		
		LinkedList <HashableState> openList = new LinkedList<HashableState>();
		openList.offer(shi);
		hashedStates.add(shi);
		long firstTime = System.currentTimeMillis();
		long lastTime = firstTime;
		while(!openList.isEmpty()){
			HashableState sh = openList.poll();

			
			List<Action> gas = ActionUtils.allApplicableActionsForTypes(actionTypes, sh.s());
			for(Action ga : gas){
				List <TransitionProb> tps = model.transitions(sh.s(), ga);
				nGenerated += tps.size();
				for(TransitionProb tp : tps){
					HashableState nsh = usingHashFactory.hashState(tp.eo.op);
					
					if (hashedStates.add(nsh) && !tp.eo.terminated) {
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
	 * @param from the source {@link State} from which the policy would be initiated.
	 * @param usingHashFactory the {@link burlap.statehashing.HashableStateFactory} used to hash states and test equality.
	 * @return a {@link java.util.List} of {@link State} objects that could be reached.
	 */
	public static List<State> getPolicyReachableStates(SADomain domain, Policy p, State from, HashableStateFactory usingHashFactory){

		Set<HashableState> hashed = getPolicyReachableHashedStates(domain, p, from, usingHashFactory);
		List<State> states = new ArrayList<State>(hashed.size());
		for(HashableState sh : hashed){
			states.add(sh.s());
		}
		return states;

	}



	/**
	 * Finds the set of states ({@link burlap.statehashing.HashableState}) that are reachable under a policy from a source state. Reachability under a source policy means
	 * that the space of actions considered are those that have non-zero probability of being selected by the
	 * policy and all possible outcomes of those states are considered.
	 * @param domain the domain containing the model to use for evaluating reachable states
	 * @param p the policy that must be followed
	 * @param from the source {@link State} from which the policy would be initiated.
	 * @param usingHashFactory the {@link burlap.statehashing.HashableStateFactory} used to hash states and test equality.
	 * @return a {@link java.util.Set} of {@link burlap.statehashing.HashableState} objects that could be reached.
	 */
	public static Set<HashableState> getPolicyReachableHashedStates(SADomain domain, Policy p, State from, HashableStateFactory usingHashFactory){

		if(!(domain.getModel() instanceof FullModel)){
			throw new RuntimeException( "State reachablity requires a domain with a FullModel, but one is not provided");
		}

		FullModel model = (FullModel)domain.getModel();

		Set<HashableState> hashedStates = new HashSet<HashableState>();
		HashableState shi = usingHashFactory.hashState(from);
		int nGenerated = 0;

		LinkedList <HashableState> openList = new LinkedList<HashableState>();
		openList.offer(shi);
		hashedStates.add(shi);

		MyTimer timer = new MyTimer(true);
		while(!openList.isEmpty()){
			HashableState sh = openList.poll();


			List<Policy.ActionProb> policyActions = p.getActionDistributionForState(sh.s());
			for(Policy.ActionProb ap : policyActions){
				if(ap.pSelection > 0){
					List <TransitionProb> tps = model.transitions(sh.s(), ap.ga);
					nGenerated += tps.size();
					for(TransitionProb tp : tps){
						HashableState nsh = usingHashFactory.hashState(tp.eo.op);

						if (hashedStates.add(nsh) && !tp.eo.terminated) {
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
