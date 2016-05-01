package burlap.oomdp.stochasticgames.common;

import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.SGAgent;
import burlap.oomdp.stochasticgames.SGStateGenerator;

import java.util.List;


/**
 * A stochastic games state generator that always returns the same base state, which is specified via the constructor. The
 * provided source state does *not* need to worry about the object name of OO-MDP objects corresponding to agent states.
 * This generator will automatically reassign the relevant OO-MDP object names to the names of each agent by querying the agent type
 * and agent name in the list of agents provides to the {@link #generateState(List)} method. This reassignment is done
 * each time the {@link #generateState(List)} method is called on a copy of the originally provided state.
 * @author James MacGlashan
 *
 */
public class ConstantSGStateGenerator implements SGStateGenerator {

	/**
	 * The source state that will be copied and returned by the {@link #generateState(List)} method.
	 */
	protected State srcState;
	
	
	/**
	 * Initializes.
	 * @param srcState The source state that will be copied and returned by the {@link #generateState(List)} method.
	 */
	public ConstantSGStateGenerator(State srcState){
		this.srcState = srcState;
	}

	@Override
	public State generateState(List<SGAgent> agents) {
		return srcState.copy();
	}
}
