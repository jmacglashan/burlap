package burlap.domain.singleagent.blockdude;

import burlap.domain.singleagent.blockdude.state.BlockDudeAgent;
import burlap.domain.singleagent.blockdude.state.BlockDudeCell;
import burlap.domain.singleagent.blockdude.state.BlockDudeMap;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.action.UniversalActionType;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.shell.visual.VisualExplorer;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.visualizer.Visualizer;

import java.util.Arrays;
import java.util.List;

/**
 * An implementation of the Block Dude Texas Instruments calculator puzzle game. The goal is for the player to reach
 * an exit. The player has three movement options: west, east, and up if the platform in front of them is only
 * one unit higher. However, because of the landscape, the player will never be able to reach the exit just by moving.
 * Instead, the player must manipulate a set of blocks scattered across the world using a pick up action (which raises
 * the block the player is facing above their head to be carried), and a put down action that places a held block directly
 * in front of the agent.
 * <p>
 * Block Dude has a very large state action space that makes planning problems in it difficult. States are encoded
 * by the player's position, facing direction, whether they are holding a block, the place of every block in the world
 * and an int array specifying the map of the "bricks" that define the landscape.
 * <p>
 * States representing the first three levels of Block Dude can be generated from the
 * {@link burlap.domain.singleagent.blockdude.BlockDudeLevelConstructor} and states can be visualized with the
 * {@link burlap.domain.singleagent.blockdude.BlockDudeVisualizer}. You can run this class' main method
 * to launch an interactive visualizer for the first level with keys: w, a, d, s, x for
 * the actions up, west, east, pickup, putdown, respectively.
 *
 *
 * @author James MacGlashan.
 */
public class BlockDude implements DomainGenerator{


	/**
	 * X position attribute name
	 */
	public static final String VAR_X = "x";

	/**
	 * Y position attribute name
	 */
	public static final String VAR_Y = "y";

	/**
	 * Direction attribute name
	 */
	public static final String VAR_DIR = "dir";

	/**
	 * Name for the boolean attribute that indicates whether the agent is holding a block
	 */
	public static final String VAR_HOLD = "holding";

	/**
	 * Name for the attribute that holds the brick map
	 */
	public static final String VAR_MAP = "map";


	/**
	 * Name for the agent OO-MDP class
	 */
	public static final String CLASS_AGENT = "agent";

	/**
	 * Name for the block OO-MDP class
	 */
	public static final String CLASS_BLOCK = "block";

	/**
	 * Name for the bricks OO-MDP class
	 */
	public static final String CLASS_MAP = "map";

	/**
	 * Name for the exit OO-MDP class
	 */
	public static final String CLASS_EXIT = "exit";


	/**
	 * Name for the up action
	 */
	public static final String ACTION_UP = "up";

	/**
	 * Name for the east action
	 */
	public static final String ACTION_EAST = "east";

	/**
	 * Name for the west action
	 */
	public static final String ACTION_WEST = "west";

	/**
	 * Name for the pickup action
	 */
	public static final String ACTION_PICKUP = "pickup";

	/**
	 * Name for the put down action
	 */
	public static final String ACTION_PUT_DOWN = "putdown";

	/**
	 * Name for the propositional function that tests whether the agent is holding a block.
	 */
	public static final String PF_HOLDING_BLOCK = "holdingBlock";

	/**
	 * Name for the propositional function that tests whether the agent is at an exit
	 */
	public static final String PF_AT_EXIT = "atExit";


	/**
	 * Domain parameter specifying the maximum x dimension value of the world
	 */
	protected int										maxx = 25;

	/**
	 * Domain parameter specifying the maximum y dimension value of the world
	 */
	protected int										maxy = 25;



	protected RewardFunction rf;
	protected TerminalFunction tf;

	/**
	 * Initializes a world with a maximum 25x25 dimensionality and actions that use semi-deep state copies.
	 */
	public BlockDude(){
		//do nothing
	}


	/**
	 * Initializes a world with the maximum space dimensionality provided and actions that use semi-deep state copies.
	 * @param maxx max x size of the world
	 * @param maxy max y size of the world
	 */
	public BlockDude(int maxx, int maxy){
		this.maxx = maxx;
		this.maxy = maxy;
	}

	public RewardFunction getRf() {
		return rf;
	}

	public void setRf(RewardFunction rf) {
		this.rf = rf;
	}

	public TerminalFunction getTf() {
		return tf;
	}

	public void setTf(TerminalFunction tf) {
		this.tf = tf;
	}

	public List<PropositionalFunction> generatePfs(){
		return Arrays.asList(new HoldingBlockPF(), new AtExitPF());
	}

	@Override
	public OOSADomain generateDomain() {

		OOSADomain domain = new OOSADomain();

		domain.addStateClass(CLASS_AGENT, BlockDudeAgent.class)
				.addStateClass(CLASS_MAP, BlockDudeMap.class)
				.addStateClass(CLASS_EXIT, BlockDudeCell.class)
				.addStateClass(CLASS_BLOCK, BlockDudeCell.class);


		domain.addActionType(new UniversalActionType(ACTION_EAST))
				.addActionType(new UniversalActionType(ACTION_WEST))
				.addActionType(new UniversalActionType(ACTION_UP))
				.addActionType(new UniversalActionType(ACTION_PICKUP))
				.addActionType(new UniversalActionType(ACTION_PUT_DOWN));

		OODomain.Helper.addPfsToDomain(domain, this.generatePfs());

		RewardFunction rf = this.rf;
		TerminalFunction tf = this.tf;

		if(tf == null){
			tf = new BlockDudeTF();
		}
		if(rf == null){
			rf = new UniformCostRF();
		}

		BlockDudeModel smodel = new BlockDudeModel(maxx, maxy);
		FactoredModel model = new FactoredModel(smodel, rf, tf);
		domain.setModel(model);


		return domain;
	}



	public int getMaxx() {
		return maxx;
	}

	public void setMaxx(int maxx) {
		this.maxx = maxx;
	}

	public int getMaxy() {
		return maxy;
	}

	public void setMaxy(int maxy) {
		this.maxy = maxy;
	}



	/**
	 * A {@link PropositionalFunction} that takes as arguments an agent object and a block objects and evaluates whether
	 * the agent is holding the block.
	 */
	public class HoldingBlockPF extends PropositionalFunction{

		public HoldingBlockPF() {
			super(PF_HOLDING_BLOCK, new String[]{CLASS_AGENT, CLASS_BLOCK});
		}


		@Override
		public boolean isTrue(OOState st, String... params) {

			BlockDudeAgent a = (BlockDudeAgent)st.object(params[0]);

			if(!a.holding){
				return false;
			}

			BlockDudeCell b = (BlockDudeCell)st.object(params[1]);


			if(a.x == b.x && a.y == b.y-1){
				return true;
			}

			return false;
		}



	}


	/**
	 * A {@link PropositionalFunction} that takes as arguments an agent object and an exit object
	 * and evaluates whether the agent is at the exit.
	 */
	public class AtExitPF extends PropositionalFunction{

		public AtExitPF() {
			super(PF_AT_EXIT, new String[]{CLASS_AGENT, CLASS_EXIT});
		}



		@Override
		public boolean isTrue(OOState st, String... params) {

			BlockDudeAgent a = (BlockDudeAgent)st.object(params[0]);
			BlockDudeCell e = (BlockDudeCell)st.object(params[1]);

			if(a.x == e.x && a.y == e.y){
				return true;
			}

			return false;
		}



	}


	/**
	 * Runs an interactive visual explorer for level one of Block Dude. The keys w,a,d,s,x correspond to actions
	 * up, west, east, pick up, put down.
	 * @param args can be empty.
	 */
	public static void main(String[] args) {

		BlockDude bd = new BlockDude();
		SADomain domain = bd.generateDomain();
		State s = BlockDudeLevelConstructor.getLevel2(domain);

		Visualizer v = BlockDudeVisualizer.getVisualizer(bd.maxx, bd.maxy);



		VisualExplorer exp = new VisualExplorer(domain, v, s);

		exp.addKeyAction("w", ACTION_UP, "");
		exp.addKeyAction("d", ACTION_EAST, "");
		exp.addKeyAction("a", ACTION_WEST, "");
		exp.addKeyAction("s", ACTION_PICKUP, "");
		exp.addKeyAction("x", ACTION_PUT_DOWN, "");

		exp.initGUI();



	}


}
