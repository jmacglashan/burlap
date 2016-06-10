package burlap.domain.singleagent.lunarlander;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;

/**
 * A {@link burlap.mdp.core.TerminalFunction} for the {@link burlap.domain.singleagent.lunarlander.LunarLanderDomain}.
 * This method sets all states in which the lunar lander is on a landing pad to be terminal states.
 * @author James MacGlashan.
 */
public class LunarLanderTF implements TerminalFunction{

	private PropositionalFunction onPad;

	/**
	 * Initializes.
	 * @param domain a {@link burlap.domain.singleagent.lunarlander.LunarLanderDomain} generated {@link burlap.mdp.core.Domain} object.
	 */
	public LunarLanderTF(OODomain domain){
		this.onPad = domain.propFunction(LunarLanderDomain.PF_ON_PAD);
	}


	@Override
	public boolean isTerminal(State s) {
		return this.onPad.someGroundingIsTrue((OOState)s);
	}
}
