package burlap.oomdp.stocashticgames.tournament.common;

import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.auxiliary.common.NullAbstraction;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.stocashticgames.JointActionModel;
import burlap.oomdp.stocashticgames.JointReward;
import burlap.oomdp.stocashticgames.SGDomain;
import burlap.oomdp.stocashticgames.SGStateGenerator;
import burlap.oomdp.stocashticgames.World;
import burlap.oomdp.stocashticgames.WorldGenerator;


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
	 * Initializes the WorldGenerator.
	 * @param domain the SGDomain the world will use
	 * @param jam the joint action model that specifies the transition dynamics
	 * @param jr the joint reward function
	 * @param tf the terminal function
	 * @param sg a state generator for generating initial states of a game
	 */
	public ConstantWorldGenerator(SGDomain domain, JointActionModel jam, JointReward jr, TerminalFunction tf, SGStateGenerator sg){
		this.CWGInit(domain, jam, jr, tf, sg, new NullAbstraction());
	}
	
	
	/**
	 * Initializes the WorldGenerator.
	 * @param domain the SGDomain the world will use
	 * @param jam the joint action model that specifies the transition dynamics
	 * @param jr the joint reward function
	 * @param tf the terminal function
	 * @param sg a state generator for generating initial states of a game
	 * @param abstractionForAgents the abstract state representation that agents will be provided
	 */
	public ConstantWorldGenerator(SGDomain domain, JointActionModel jam, JointReward jr, TerminalFunction tf, SGStateGenerator sg, StateAbstraction abstractionForAgents){
		this.CWGInit(domain, jam, jr, tf, sg, abstractionForAgents);
	}
	
	protected void CWGInit(SGDomain domain, JointActionModel jam, JointReward jr, TerminalFunction tf, SGStateGenerator sg, StateAbstraction abstractionForAgents){
		this.domain = domain;
		this.worldModel = jam;
		this.jointRewardModel = jr;
		this.tf = tf;
		this.initialStateGenerator = sg;
		this.abstractionForAgents = abstractionForAgents;
	}
	
	
	@Override
	public World generateWorld() {
		return new World(domain, worldModel, jointRewardModel, tf, initialStateGenerator, abstractionForAgents);
	}

}
