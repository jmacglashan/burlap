package burlap.domain.singleagent.frostbite;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;

/**
 * @author Phillipe Morere
 */
public class FrostbiteTF implements TerminalFunction{

	private PropositionalFunction onIce;
	private PropositionalFunction inWater;
	private PropositionalFunction iglooBuilt;

	public FrostbiteTF(OODomain domain) {
		this.inWater = domain.propFunction(FrostbiteDomain.PF_IN_WATER);
		this.onIce = domain.propFunction(FrostbiteDomain.PF_ON_ICE);
		this.iglooBuilt = domain.propFunction(FrostbiteDomain.PF_IGLOO_BUILT);
	}

	@Override
	public boolean isTerminal(State s) {
		if (inWater.somePFGroundingIsTrue((OOState)s))
			return true;
		return iglooBuilt.somePFGroundingIsTrue((OOState)s) && onIce.somePFGroundingIsTrue((OOState)s);
	}

}
