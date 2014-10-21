package burlap.domain.singleagent.lunarlander;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;

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
	public LunarLanderTF(Domain domain){
		this.onPad = domain.getPropFunction(LunarLanderDomain.PFONPAD);
	}


	@Override
	public boolean isTerminal(State s) {
		return this.onPad.somePFGroundingIsTrue(s);
	}
}
