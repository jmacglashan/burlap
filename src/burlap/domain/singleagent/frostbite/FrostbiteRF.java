package burlap.domain.singleagent.frostbite;

import burlap.domain.singleagent.frostbite.state.FrostbitePlatform;
import burlap.domain.singleagent.frostbite.state.FrostbiteState;
import burlap.mdp.core.Action;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;

import java.util.List;

/**
 * @author Phillipe Morere
 */
public class FrostbiteRF implements RewardFunction{

	public double goalReward = 1000.0;
	public double lostReward = -1000.0;
	public double activatedPlatformReward = 10.0;
	public double defaultReward = -1.0;
	private PropositionalFunction onIce;
	private PropositionalFunction inWater;
	private PropositionalFunction iglooBuilt;

	public FrostbiteRF(OODomain domain) {
		this.inWater = domain.propFunction(FrostbiteDomain.PF_IN_WATER);
		this.onIce = domain.propFunction(FrostbiteDomain.PF_ON_ICE);
		this.iglooBuilt = domain.propFunction(FrostbiteDomain.PF_IGLOO_BUILT);
	}

	@Override
	public double reward(State s, Action a, State sprime) {
		if (inWater.somePFGroundingIsTrue((OOState)sprime))
			return lostReward;
		if (iglooBuilt.somePFGroundingIsTrue((OOState)sprime) && onIce.somePFGroundingIsTrue((OOState)s))
			return goalReward;
		if (numberPlatformsActive((FrostbiteState)s) != numberPlatformsActive((FrostbiteState)sprime))
			return activatedPlatformReward;
		return defaultReward;
	}

	private int numberPlatformsActive(FrostbiteState s) {
		List<FrostbitePlatform> platforms = s.platforms;
		int nb = 0;
		for (FrostbitePlatform platform : platforms)
			if (platform.activated)
				nb++;
		return nb;
	}

}
