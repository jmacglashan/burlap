package burlap.behavior.singleagent.planning.stochastic;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.statehashing.HashableStateFactory;
import burlap.mdp.core.state.State;
import burlap.mdp.core.TransitionProbability;
import burlap.mdp.singleagent.GroundedAction;


/**
 * This class is used as a wrapper for specifying the possible state transitions that can occur when an action is applied from a particular state (which is
 * kept hidden). The transition dynamics stores state-hashed transition dynamics. This class can be useful to planners like tabular VI that need to work with transition
 * dynamics of the domain.
 * @author James MacGlashan
 *
 */
public class ActionTransitions {

	public GroundedAction ga;
	public List <HashedTransitionProbability> transitions;
	
	
	/**
	 * Constructs the the ActionTransitions from the {@link burlap.mdp.singleagent.GroundedAction} that generated the transitions and the
	 * set of transition probabilities.
	 * @param ga the GroundedAction that generated the possible transitions
	 * @param transitions the transition probabilities
	 * @param hashingFactory a hashingFactory that can be used to hash the states in the TransitionProbability data structure.
	 */
	public ActionTransitions(GroundedAction ga, List <TransitionProbability> transitions, HashableStateFactory hashingFactory){
		this.ga = ga;
		this.transitions = this.getHashedTransitions(transitions, hashingFactory);
	}
	
	
	/**
	 * Constructs the the ActionTransitions from a source state, and an action to apply in that source state.
	 * @param s the state from which to generate the hashed transitions
	 * @param ga the GroundedAction to be applied in the source state
	 * @param hashingFactory a hashingFactory that can be used to hash the states in the TransitionProbability data structures that are returned from applying ga in s.
	 */
	public ActionTransitions(State s, GroundedAction ga, HashableStateFactory hashingFactory){
		this.ga = ga;
		this.transitions = this.getHashedTransitions(ga.getTransitions(s), hashingFactory);
	}
	
	
	/**
	 * Returns whether these action transitions are for the specified {@link burlap.mdp.singleagent.GroundedAction}
	 * @param oga the {@link burlap.mdp.singleagent.GroundedAction} to check against
	 * @return true if this object's transition probabilities were generated with {@link burlap.mdp.singleagent.GroundedAction} oga; false otherwise.
	 */
	public boolean matchingTransitions(GroundedAction oga){
		return ga.equals(oga);
	}
	
	private List <HashedTransitionProbability> getHashedTransitions(List <TransitionProbability> tps, HashableStateFactory hashingFactory){
		List <HashedTransitionProbability> htps = new ArrayList<HashedTransitionProbability>();
		for(TransitionProbability tp : tps){
			htps.add(new HashedTransitionProbability(tp, hashingFactory));
		}
		return htps;
	}
	
}
