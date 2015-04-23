package burlap.domain.singleagent.cartpole;

import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.states.State;

/**
 * A custom {@link burlap.oomdp.auxiliary.StateParser} for the {@link burlap.domain.singleagent.cartpole.InvertedPendulum.InvertedPendulumTerminalFunction}
 * domain. Format is a single space delimited line of the angle and angle velocity of the pendulum.
 *
 * @author James MacGlashan
 */
public class InvertedPendulumStateParser implements StateParser{

	/**
	 * The domain to which the returned state created by the method {@link #stringToState(String)} will belong.
	 */
	protected Domain domain;

	public InvertedPendulumStateParser(Domain domain){this.domain = domain;}

	@Override
	public String stateToString(State s) {

		ObjectInstance o = s.getFirstObjectOfClass(InvertedPendulum.CLASSPENDULUM);
		double a = o.getRealValForAttribute(InvertedPendulum.ATTANGLE);
		double av = o.getRealValForAttribute(InvertedPendulum.ATTANGLEV);

		String res = a + " " + av;
		return res;
	}

	@Override
	public State stringToState(String str) {

		String [] comps = str.split(" ");
		double a = Double.parseDouble(comps[0]);
		double av = Double.parseDouble(comps[1]);
		State s = InvertedPendulum.getInitialState(domain, a, av);
		return s;
	}
}
