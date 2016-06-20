package burlap.behavior.singleagent.learning.modellearning;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.ActionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link LearnedModel} that "knows what it knows." That is, it can report whether it knows a transition or not.
 * @author James MacGlashan.
 */
public interface KWIKModel extends LearnedModel{

	/**
	 * Indicates whether this model "knows" how the transition dynamics from the given input state and action work.
	 * @param s the state that is checked
	 * @param a the action to take in state s
	 * @return true if the transition dynamics from the input state and action are "known;" false otherwise.
	 */
	boolean transitionIsModeled(State s, Action a);



	class Helper{
		public static boolean stateTransitionsModeled(KWIKModel model, List<ActionType> actionTypes, State s){
			List<Action> actions = ActionUtils.allApplicableActionsForTypes(actionTypes, s);
			for(Action a : actions){
				if(!model.transitionIsModeled(s, a)){
					return false;
				}
			}
			return true;
		}

		public static List<Action> unmodeledActions(KWIKModel model, List<ActionType> actionTypes, State s){
			List<Action> actions = ActionUtils.allApplicableActionsForTypes(actionTypes, s);
			List<Action> unmodeled = new ArrayList<Action>(actions.size());
			for(Action a : actions){
				if(!model.transitionIsModeled(s, a)){
					unmodeled.add(a);
				}
			}
			return unmodeled;
		}
	}

}
