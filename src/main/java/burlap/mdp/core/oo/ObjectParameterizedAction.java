package burlap.mdp.core.oo;

import burlap.mdp.core.Action;
import burlap.mdp.core.oo.state.ObjectInstance;

/**
 * An interface extension to the {@link Action} interface for actions whose
 * parameters included references to OO-MDP {@link ObjectInstance}s.
 * @author James MacGlashan.
 */
public interface ObjectParameterizedAction extends Action {

	/**
	 * Returns the parameters of this {@link Action} that correspond to OO-MDP objects.
	 * @return the parameters of this {@link Action} that correspond to OO-MDP objects.
	 */
	String [] getObjectParameters();

	/**
	 * Sets the object parameters for this {@link Action}.
	 * @param params the object parameters to use.
	 */
	void setObjectParameters(String [] params);


}
