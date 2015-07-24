package burlap.behavior.stochasticgames.madynamicprogramming;

import burlap.behavior.stochasticgames.JointPolicy;


/**
 * An abstract extension of the JointPolicy class that adds a required interface of being able to a {@link MultiAgentQSourceProvider}. This extension is useful
 * if the joint policy is derived from a set of multi-agent Q-values.
 * @author James MacGlashan
 *
 */
public abstract class MAQSourcePolicy extends JointPolicy{
	
	/**
	 * Sets the {@link MultiAgentQSourceProvider} that will be used to define this object's joint policy.
	 * @param provider the {@link MultiAgentQSourceProvider} that will be used to define this object's joint policy.
	 */
	public abstract void setQSourceProvider(MultiAgentQSourceProvider provider);
}
