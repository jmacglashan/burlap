package burlap.oomdp.stochasticgames.tournament.common;

import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.auxiliary.common.NullAbstraction;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.JointReward;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SGStateGenerator;
import burlap.oomdp.stochasticgames.World;
import burlap.oomdp.stochasticgames.WorldGenerator;


/**
 * A WorldGenerator that always generators the same WorldConfiguraiton. It takes the same parameters as a {@link burlap.oomdp.stochasticgames.World} constructor
 * and simply passes them to the World Constructor when a new World needs to be generated.
 * @author James MacGlashan
 *
 */
public class ConstantWorldGenerator implements WorldGenerator {

	protected SGDomain							domain;
	protected JointActionModel 					worldModel;
	protected JointReward						jointRewardModel;
	protected TerminalFunction					tf;
	protected SGStateGenerator					initialStateGenerator;
	
	protected StateAbstraction					abstractionForAgents;
	
	
	
	/**
	 * This constructor is deprecated, because {@link burlap.oomdp.stochasticgames.SGDomain} objects are now expected
	 * to have a {@link burlap.oomdp.stochasticgames.JointActionModel} associated with them, making the constructor parameter for it
	 * unnecessary. Instead use the constructor {@link #ConstantWorldGenerator(burlap.oomdp.stochasticgames.SGDomain, burlap.oomdp.stochasticgames.JointReward, burlap.oomdp.core.TerminalFunction, burlap.oomdp.stochasticgames.SGStateGenerator)}
	 * @param domain the SGDomain the world will use
	 * @param jam the joint action model that specifies the transition dynamics
	 * @param jr the joint reward function
	 * @param tf the terminal function
	 * @param sg a state generator for generating initial states of a game
	 */
	@Deprecated
	public ConstantWorldGenerator(SGDomain domain, JointActionModel jam, JointReward jr, TerminalFunction tf, SGStateGenerator sg){
		this.CWGInit(domain, jr, tf, sg, new NullAbstraction());
	}

	/**
	 * Initializes the WorldGenerator.
	 * @param domain the SGDomain the world will use
	 * @param jr the joint reward function
	 * @param tf the terminal function
	 * @param sg a state generator for generating initial states of a game
	 */
	public ConstantWorldGenerator(SGDomain domain, JointReward jr, TerminalFunction tf, SGStateGenerator sg){
		this.CWGInit(domain, jr, tf, sg, new NullAbstraction());
	}
	
	
	/**
	 * This constructor is deprecated, because {@link burlap.oomdp.stochasticgames.SGDomain} objects are now expected
	 * to have a {@link burlap.oomdp.stochasticgames.JointActionModel} associated with them, making the constructor parameter for it
	 * unnecessary. Instead use the constructor {@link #ConstantWorldGenerator(burlap.oomdp.stochasticgames.SGDomain, burlap.oomdp.stochasticgames.JointReward, burlap.oomdp.core.TerminalFunction, burlap.oomdp.stochasticgames.SGStateGenerator, burlap.oomdp.auxiliary.StateAbstraction)}
	 * @param domain the SGDomain the world will use
	 * @param jam the joint action model that specifies the transition dynamics
	 * @param jr the joint reward function
	 * @param tf the terminal function
	 * @param sg a state generator for generating initial states of a game
	 * @param abstractionForAgents the abstract state representation that agents will be provided
	 */
	@Deprecated
	public ConstantWorldGenerator(SGDomain domain, JointActionModel jam, JointReward jr, TerminalFunction tf, SGStateGenerator sg, StateAbstraction abstractionForAgents){
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
	public ConstantWorldGenerator(SGDomain domain, JointReward jr, TerminalFunction tf, SGStateGenerator sg, StateAbstraction abstractionForAgents){
		this.CWGInit(domain, jr, tf, sg, abstractionForAgents);
	}
	
	protected void CWGInit(SGDomain domain, JointReward jr, TerminalFunction tf, SGStateGenerator sg, StateAbstraction abstractionForAgents){
		this.domain = domain;
		this.jointRewardModel = jr;
		this.tf = tf;
		this.initialStateGenerator = sg;
		this.abstractionForAgents = abstractionForAgents;
	}
	
	
	@Override
	public World generateWorld() {
		return new World(domain, jointRewardModel, tf, initialStateGenerator, abstractionForAgents);
	}

}
