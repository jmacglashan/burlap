package burlap.mdp.stochasticgames.tournament.common;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.auxiliary.common.IdentityStateMapping;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.stochasticgames.model.JointActionModel;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.SGStateGenerator;
import burlap.mdp.stochasticgames.world.World;
import burlap.mdp.stochasticgames.world.WorldGenerator;


/**
 * A WorldGenerator that always generators the same WorldConfiguraiton. It takes the same parameters as a {@link World} constructor
 * and simply passes them to the World Constructor when a new World needs to be generated.
 * @author James MacGlashan
 *
 */
public class ConstantWorldGenerator implements WorldGenerator {

	protected SGDomain							domain;
	protected JointActionModel 					worldModel;
	protected JointRewardFunction jointRewardFunctionModel;
	protected TerminalFunction					tf;
	protected SGStateGenerator					initialStateGenerator;
	
	protected StateMapping abstractionForAgents;
	
	
	
	/**
	 * This constructor is deprecated, because {@link burlap.mdp.stochasticgames.SGDomain} objects are now expected
	 * to have a {@link JointActionModel} associated with them, making the constructor parameter for it
	 * unnecessary. Instead use the constructor {@link #ConstantWorldGenerator(burlap.mdp.stochasticgames.SGDomain, JointRewardFunction, burlap.mdp.core.TerminalFunction, burlap.mdp.stochasticgames.SGStateGenerator)}
	 * @param domain the SGDomain the world will use
	 * @param jam the joint action model that specifies the transition dynamics
	 * @param jr the joint reward function
	 * @param tf the terminal function
	 * @param sg a state generator for generating initial states of a game
	 */
	@Deprecated
	public ConstantWorldGenerator(SGDomain domain, JointActionModel jam, JointRewardFunction jr, TerminalFunction tf, SGStateGenerator sg){
		this.CWGInit(domain, jr, tf, sg, new IdentityStateMapping());
	}

	/**
	 * Initializes the WorldGenerator.
	 * @param domain the SGDomain the world will use
	 * @param jr the joint reward function
	 * @param tf the terminal function
	 * @param sg a state generator for generating initial states of a game
	 */
	public ConstantWorldGenerator(SGDomain domain, JointRewardFunction jr, TerminalFunction tf, SGStateGenerator sg){
		this.CWGInit(domain, jr, tf, sg, new IdentityStateMapping());
	}
	
	
	/**
	 * This constructor is deprecated, because {@link burlap.mdp.stochasticgames.SGDomain} objects are now expected
	 * to have a {@link JointActionModel} associated with them, making the constructor parameter for it
	 * unnecessary. Instead use the constructor {@link #ConstantWorldGenerator(burlap.mdp.stochasticgames.SGDomain, JointRewardFunction, burlap.mdp.core.TerminalFunction, burlap.mdp.stochasticgames.SGStateGenerator, burlap.mdp.auxiliary.StateMapping)}
	 * @param domain the SGDomain the world will use
	 * @param jam the joint action model that specifies the transition dynamics
	 * @param jr the joint reward function
	 * @param tf the terminal function
	 * @param sg a state generator for generating initial states of a game
	 * @param abstractionForAgents the abstract state representation that agents will be provided
	 */
	@Deprecated
	public ConstantWorldGenerator(SGDomain domain, JointActionModel jam, JointRewardFunction jr, TerminalFunction tf, SGStateGenerator sg, StateMapping abstractionForAgents){
		this.CWGInit(domain, jr, tf, sg, abstractionForAgents);
	}

	/**
	 * Initializes the WorldGenerator.
	 * @param domain the SGDomain the world will use
	 * @param jr the joint reward function
	 * @param tf the terminal function
	 * @param sg a state generator for generating initial states of a game
	 * @param abstractionForAgents the abstract state representation that agents will be provided
	 */
	public ConstantWorldGenerator(SGDomain domain, JointRewardFunction jr, TerminalFunction tf, SGStateGenerator sg, StateMapping abstractionForAgents){
		this.CWGInit(domain, jr, tf, sg, abstractionForAgents);
	}
	
	protected void CWGInit(SGDomain domain, JointRewardFunction jr, TerminalFunction tf, SGStateGenerator sg, StateMapping abstractionForAgents){
		this.domain = domain;
		this.jointRewardFunctionModel = jr;
		this.tf = tf;
		this.initialStateGenerator = sg;
		this.abstractionForAgents = abstractionForAgents;
	}
	
	
	@Override
	public World generateWorld() {
		return new World(domain, jointRewardFunctionModel, tf, initialStateGenerator, abstractionForAgents);
	}

}
