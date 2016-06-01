package burlap.mdp.singleagent.action;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;

import java.util.List;


/**
 * An {@link ActionType} acts as a generator for {@link Action} objects. Its job is to define any preconditions for an
 * {@link Action}, so that {@link Action} objects with unsatisfied preconditions for an input state are not generated
 * in the state as well determine the set of valid parameterizations for a given input state. For example, in
 * a {@link burlap.domain.singleagent.blocksworld.BlocksWorld}, the number of stack actions is dependent on the number
 * of clear blocks, and blocks that are not clear cannot be stacked on other blocks because their precondition
 * is not satisfied.
 * <p>
 * You can also use an {@link ActionType} to generate different parameterizations of an {@link Action}, even when
 * the parameters are not state dependent, but it is usually not necessary to do so and you might instead consider
 * creating a separate {@link ActionType} for each parameterization.
 * @author James MacGlashan
 *
 */
public interface ActionType {


	/**
	 * The unique name of this {@link ActionType}
	 * @return unique {@link String} name of this {@link ActionType}
	 */
	String typeName();

	/**
	 * Returns an {@link Action} whose parameters are specified by the given {@link String} representation (if
	 * the {@link ActionType} manages multiple parameterizations)
	 * @param strRep the {@link String} representation of the {@link Action} parameters, if any, or null if there are no parameters.
	 * @return the corresponding {@link Action}
	 */
	Action associatedAction(String strRep);

	/**
	 * Returns all possible actions of this type that can be applied in the provided {@link State}.
	 * @param s the {@link State} in which all applicable  actions of this {@link ActionType} object should be returned.
	 * @return a list of all applicable {@link Action}s of this {@link ActionType} object in in the given {@link State}
	 */
	List<Action> allApplicableActions(State s);
	

	
	
}
