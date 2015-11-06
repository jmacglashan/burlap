package burlap.behavior.stochasticgames.saconversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.debugtools.DPrint;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.states.State;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGAgentType;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;


/**
 * This class provides methods for finding the set of reachable states from a source state.
 * @author Betsy Hilliard, Carl Trimbach based on the single agent class StateReachability
 *
 */
public class SGStateReachability {

	/**
	 * The debugID used for making calls to {@link burlap.debugtools.DPrint}.
	 */
	public static int			debugID = 8374937;


	/**
	 * Returns the list of {@link burlap.oomdp.core.State} objects that are reachable from a source state. 
	 * @param from the source state
	 * @param inDomain the domain of the state
	 * @param usingHashFactory the state hashing factory to use for indexing states and testing equality.
	 * @return the list of {@link burlap.oomdp.core.State} objects that are reachable from a source state. 
	 */
	public static Collection <State> getReachableStates(State from, SGDomain inDomain,Map<String,SGAgentType> SGAgentTypes, HashableStateFactory usingHashFactory){
		return getReachableStates(from, inDomain, SGAgentTypes, usingHashFactory, new NullTermination());
	}

	

	/**
	 * Returns the list of {@link burlap.oomdp.core.State} objects that are reachable from a source state. 
	 * @param from the source state
	 * @param inDomain the domain of the state
	 * @param usingHashFactory the state hashing factory to use for indexing states and testing equality.
	 * @param tf a terminal function that prevents expansion from terminal states.
	 * @return the list of {@link burlap.oomdp.core.State} objects that are reachable from a source state. 
	 */
	public static Collection <State> getReachableStates(State from, SGDomain inDomain, Map<String,SGAgentType> agentList, HashableStateFactory usingHashFactory, TerminalFunction tf){
		Collection<HashableState> res = getReachableHashedStates(from, inDomain, agentList, usingHashFactory, tf);
		return new ArrayList<State>(res);
		//return new ArrayList<State>(hashedStates);
//		for(HashableState sh : hashedStates){
//			states.add(sh.s);
//		}
//
//		return states;
	}

	public static List <State> getReachableNonTerminalStates(State from, SGDomain inDomain, Map<String,SGAgentType> agentList, HashableStateFactory usingHashFactory, TerminalFunction tf){
		Collection<HashableState> res = getReachableNonTerminalHashedStates(from, inDomain, agentList, usingHashFactory, tf);
		return new ArrayList<State>(res);
//		return new ArrayList<State>(hashedStates);
//		for(HashableState sh : hashedStates){
//			states.add(sh.s);
//		}
//
//		return states;
	}

	/**
	 * Returns the set of {@link burlap.oomdp.core.State} objects that are reachable from a source state. 
	 * @param from the source state
	 * @param inDomain the domain of the state
	 * @param usingHashFactory the state hashing factory to use for indexing states and testing equality.
	 * @return the set of {@link burlap.oomdp.core.State} objects that are reachable from a source state. 
	 */
	public static Collection <HashableState> getReachableHashedStates(State from, SGDomain inDomain,Map<String,SGAgentType> agentList,
HashableStateFactory usingHashFactory){
		return getReachableHashedStates(from, inDomain, agentList, usingHashFactory, new NullTermination());
	}



	/**
	 * Returns the set of {@link burlap.oomdp.core.State} objects that are reachable from a source state. 
	 * @param from the source state
	 * @param inDomain the domain of the state
	 * @param usingHashFactory the state hashing factory to use for indexing states and testing equality.
	 * @param tf a terminal function that prevents expansion from terminal states.
	 * @return the set of {@link burlap.oomdp.core.State} objects that are reachable from a source state. 
	 */
	public static Collection <HashableState> getReachableHashedStates(State from, 
			SGDomain inDomain, Map<String, SGAgentType> SGAgentTypes, HashableStateFactory usingHashFactory, TerminalFunction tf){

		//this keeps track of the states
		Set<HashableState> hashedStates = new HashSet<HashableState>();
		HashableState shi = usingHashFactory.hashState(from);

		int nGenerated = 0;
		
		//this keeps track of unfollowed nodes in the tree of reachable states
		LinkedList <HashableState> openList = new LinkedList<HashableState>();
		openList.offer(shi);
		hashedStates.add(shi);
		//while there are still unexplored states, explore the next state
		while(openList.size() > 0){
			
			HashableState sh = openList.poll();
			

			if(tf.isTerminal(sh.s)){
				continue; //don't expand
			}
			
			//we are going to make all the joint actions
			List<JointAction> jointActions = new ArrayList<JointAction>();
			
			//we need the grounded actions for each of the agents from this state
			List<List<GroundedSGAgentAction>> gsasList= new ArrayList< List<GroundedSGAgentAction>>();
			// for each agent get all their GAs
			for(String aName : SGAgentTypes.keySet()){
				SGAgentType at = SGAgentTypes.get(aName);
				ArrayList<GroundedSGAgentAction> newList = new ArrayList<GroundedSGAgentAction>();
				List<SGAgentAction> sas = at.actions;
				for(SGAgentAction sa :sas){
					newList.addAll(sa.getAllApplicableGroundedActions(sh.s, aName));
				}
				gsasList.add(newList);
				
			}
			
			//NOTE: this is only set up to work for two agents at the moment
			//TODO: generalize to multiple agents
			for(GroundedSGAgentAction gsa0 : gsasList.get(0)){
				for(GroundedSGAgentAction gsa1 : gsasList.get(1)){
					JointAction newJA = new JointAction();
					//add the grounded actions so we have a complete joint action
					newJA.addAction(gsa0);
					newJA.addAction(gsa1);
					jointActions.add(newJA);
				}
			}
			
			//for each joint action, get all next states and add them to the unexplored state list
			for(JointAction ja : jointActions){
				List <TransitionProbability> tps = inDomain.getJointActionModel().transitionProbsFor(sh.s, ja);
				for(TransitionProbability tp : tps){
					HashableState nsh = usingHashFactory.hashState(tp.s);
					nGenerated++;
					if(!hashedStates.contains(nsh)){
						openList.offer(nsh);
						hashedStates.add(nsh);
						
					}
				}
			}
		}

		DPrint.cl(debugID, "Num generated: " + nGenerated + "; num unique: " + hashedStates.size());
		return hashedStates;
	}
	
	/**
	 * Returns the set of {@link burlap.oomdp.core.State} objects that are reachable from a source state. 
	 * @param from the source state
	 * @param inDomain the domain of the state
	 * @param usingHashFactory the state hashing factory to use for indexing states and testing equality.
	 * @param tf a terminal function that prevents expansion from terminal states.
	 * @return the set of {@link burlap.oomdp.core.State} objects that are reachable from a source state. 
	 */
	public static Collection <HashableState> getReachableNonTerminalHashedStates(State from, 
			SGDomain inDomain, Map<String, SGAgentType> SGAgentTypes, HashableStateFactory usingHashFactory, TerminalFunction tf){

		//this keeps track of the states
		Set<HashableState> hashedStates = new HashSet<HashableState>();
		HashableState shi = usingHashFactory.hashState(from);

		int nGenerated = 0;
		
		//this keeps track of unfollowed nodes in the tree of reachable states
		LinkedList <HashableState> openList = new LinkedList<HashableState>();
		openList.offer(shi);
		hashedStates.add(shi);
		//while there are still unexplored states, explore the next state
		while(openList.size() > 0){
			
			HashableState sh = openList.poll();
			

			if(tf.isTerminal(sh.s)){
				continue; //don't expand
			}
			
			//we are going to make all the joint actions
			List<JointAction> jointActions = new ArrayList<JointAction>();
			
			//we need the grounded actions for each of the agents from this state
			List<List<GroundedSGAgentAction>> gsasList= new ArrayList< List<GroundedSGAgentAction>>();
			// for each agent get all their GAs
			for(String aName : SGAgentTypes.keySet()){
				SGAgentType at = SGAgentTypes.get(aName);
				ArrayList<GroundedSGAgentAction> newList = new ArrayList<GroundedSGAgentAction>();
				List<SGAgentAction> sas = at.actions;
				for(SGAgentAction sa :sas){
					newList.addAll(sa.getAllApplicableGroundedActions(sh.s, aName));
				}
				gsasList.add(newList);
				
			}
			
			//NOTE: this is only set up to work for two agents at the moment
			//TODO: generalize to multiple agents
			for(GroundedSGAgentAction gsa0 : gsasList.get(0)){
				for(GroundedSGAgentAction gsa1 : gsasList.get(1)){
					JointAction newJA = new JointAction();
					//add the grounded actions so we have a complete joint action
					newJA.addAction(gsa0);
					newJA.addAction(gsa1);
					jointActions.add(newJA);
				}
			}
			
			//for each joint action, get all next states and add them to the unexplored state list
			for(JointAction ja : jointActions){
				List <TransitionProbability> tps = inDomain.getJointActionModel().transitionProbsFor(sh.s, ja);
				for(TransitionProbability tp : tps){
					HashableState nsh = usingHashFactory.hashState(tp.s);
					nGenerated++;
					if(!hashedStates.contains(nsh) && !tf.isTerminal(nsh.s)){
						openList.offer(nsh);
						hashedStates.add(nsh);
						
					}
				}
			}
		}

		//DPrint.cl(debugID, "Num generated: " + nGenerated + "; num unique: " + hashedStates.size());
		return hashedStates;
	}
}
