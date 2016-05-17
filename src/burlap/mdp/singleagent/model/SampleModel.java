package burlap.mdp.singleagent.model;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

/**
 * @author James MacGlashan.
 */
public interface SampleModel {
	EnvironmentOutcome sample(State s, Action a);
	boolean terminal(State s);
}
