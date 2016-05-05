package burlap.domain.singleagent.lunarlander;

import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.oo.OODomain;
import burlap.oomdp.core.oo.propositional.PropositionalFunction;
import burlap.oomdp.core.oo.state.OOState;
import burlap.oomdp.core.state.State;

/**
 * A {@link burlap.oomdp.core.TerminalFunction} for the {@link burlap.domain.singleagent.lunarlander.LunarLanderDomain}.
 * This method sets all states in which the lunar lander is on a landing pad to be terminal states.
 * @author James MacGlashan.
 */
public class LunarLanderTF implements TerminalFunction{

	private PropositionalFunction onPad;

	/**
	 * Initializes.
	 * @param domain a {@link burlap.domain.singleagent.lunarlander.LunarLanderDomain} generated {@link burlap.oomdp.core.Domain} object.
	 */
	public LunarLanderTF(OODomain domain){
		this.onPad = domain.getPropFunction(LunarLanderDomain.PF_ON_PAD);
	}


	@Override
	public boolean isTerminal(State s) {
		return this.onPad.somePFGroundingIsTrue((OOState)s);
	}
}
