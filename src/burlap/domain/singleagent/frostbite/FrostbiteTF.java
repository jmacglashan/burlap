package burlap.domain.singleagent.frostbite;

import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.oo.OODomain;
import burlap.oomdp.core.oo.propositional.PropositionalFunction;
import burlap.oomdp.core.oo.state.OOState;
import burlap.oomdp.core.state.State;

/**
 * @author Phillipe Morere
 */
public class FrostbiteTF implements TerminalFunction{

	private PropositionalFunction onIce;
	private PropositionalFunction inWater;
	private PropositionalFunction iglooBuilt;

	public FrostbiteTF(OODomain domain) {
		this.inWater = domain.getPropFunction(FrostbiteDomain.PF_IN_WATER);
		this.onIce = domain.getPropFunction(FrostbiteDomain.PF_ON_ICE);
		this.iglooBuilt = domain.getPropFunction(FrostbiteDomain.PF_IGLOO_BUILT);
	}

	@Override
	public boolean isTerminal(State s) {
		if (inWater.somePFGroundingIsTrue((OOState)s))
			return true;
		return iglooBuilt.somePFGroundingIsTrue((OOState)s) && onIce.somePFGroundingIsTrue((OOState)s);
	}

}
