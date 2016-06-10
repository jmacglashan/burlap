package burlap.behavior.singleagent.learning.modellearning;

import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.FullModel;

/**
 * An interface extension of {@link FullModel} for models that are learned from data. Requires a method for updating the model
 * and resetting the model.
 * @author James MacGlashan.
 */
public interface LearnedModel extends FullModel{


	/**
	 * Updates this model with respect to the observed {@link burlap.mdp.singleagent.environment.EnvironmentOutcome}.
	 * @param eo The {@link burlap.mdp.singleagent.environment.EnvironmentOutcome} specifying the observed interaction with an {@link burlap.mdp.singleagent.environment.Environment}.
	 */
	void updateModel(EnvironmentOutcome eo);


	/**
	 * Resets the model data so that learning can begin anew.
	 */
	void resetModel();

}
