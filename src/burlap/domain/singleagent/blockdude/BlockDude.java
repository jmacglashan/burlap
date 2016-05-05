package burlap.domain.singleagent.blockdude;

import burlap.domain.singleagent.blockdude.state.BlockDudeAgent;
import burlap.domain.singleagent.blockdude.state.BlockDudeCell;
import burlap.domain.singleagent.blockdude.state.BlockDudeMap;
import burlap.domain.singleagent.blockdude.state.BlockDudeState;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.FullActionModel;
import burlap.mdp.singleagent.GroundedAction;
import burlap.mdp.singleagent.common.SimpleAction;
import burlap.mdp.singleagent.explorer.VisualExplorer;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.mdp.visualizer.Visualizer;

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



	@Override
	public Domain generateDomain() {

		OOSADomain domain = new OOSADomain();

		domain.addStateClass(CLASS_AGENT, BlockDudeAgent.class)
				.addStateClass(CLASS_MAP, BlockDudeMap.class)
				.addStateClass(CLASS_EXIT, BlockDudeCell.class)
				.addStateClass(CLASS_BLOCK, BlockDudeCell.class);


		new MoveAction(ACTION_EAST, domain, 1, maxx);
		new MoveAction(ACTION_WEST, domain, -1, maxx);
		new MoveUpAction(domain, maxx);
		new PickupAction(domain, maxx);
		new PutdownAction(domain, maxx);

		new HoldingBlockPF(domain);
		new AtExitPF(domain);


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
	 * Modifies state s to be the result of a horizontal movement. This method will also move any held blocks
	 * by the agent and cause the agent (and its held block) to fall if it walks off a cliff. The agent will not
	 * be able to move to an x position &lt; 0 or &gt;= the maximum x dimensionality
	 * @param s the state to modify
	 * @param dx the change in x direction; should only be +1 (east) or -1 (west).
	 * @param maxx the maximum x dimensionality of the world
	 */
	public static void moveHorizontally(BlockDudeState s, int dx, int maxx){

		if(dx != 1 && dx != -1){
			throw new RuntimeException("Agent horizontal movement can only be a difference of +1 (east) or -1 (west).");
		}

		BlockDudeAgent agent = s.agent.copy();
		s.agent = agent;

		int [][] map = s.map.map;

		//always set direction
		if(dx > 0){
			agent.dir = 1;
		}
		else{
			agent.dir = 0;
		}


		int ax = agent.x;
		int ay = agent.y;

		int nx = ax+dx;

		if(nx < 0 || nx >= maxx){
			return;
		}

		int heightAtNX = greatestHeightBelow(s, map, maxx, nx, ay);


		//can only move if new position is below agent height
		if(heightAtNX >= ay){
			return ; //do nothing; walled off
		}

		int ny = heightAtNX + 1; //stand on top of stack

		agent.x = nx;
		agent.y = ny;


		moveCarriedBlockToNewAgentPosition(s, agent, ax, ay, nx, ny);


	}


	/**
	 * Modifies state s to be the result of a vertical movement, that will result in the agent onto the platform adjacent
	 * to its current location in the direction the agent is facing, provided that there is room for the agent (and any block
	 * it's holding) to step onto it.
	 * @param s the state to modify.
	 * @param maxx the maximum x dimensionality of the world
	 */
	public static void moveUp(BlockDudeState s, int maxx){

		BlockDudeAgent agent = s.agent.copy();
		s.agent = agent;

		int [][] map = s.map.map;

		int ax = agent.x;
		int ay = agent.y;
		int dir = agent.dir;
		boolean holding = agent.holding;

		if(dir == 0){
			dir = -1;
		}

		int nx = ax+dir;
		int ny = ay+1;

		if(nx < 0 || nx > maxx){
			return;
		}

		int clearing = holding ? ny+1 : ny;

		int heightAtNX = greatestHeightBelow(s, map, maxx, nx, clearing);

		//in order to move up, the height of world in new x position must be at the same current agent position
		if(heightAtNX != ay){
			return ; //not a viable move up condition, so do nothing
		}

		agent.x = nx;
		agent.y = ny;

		moveCarriedBlockToNewAgentPosition(s, agent, ax, ay, nx, ny);


	}

	/**
	 * Modifies state s to be the result of the pick up action. If no block that is clear (i.e., no block on top of it)
	 * is in front of the agent, then a block is not picked up.
	 * @param s the state to modify.
	 * @param maxx the maximum x dimensionality of the world
	 */
	public static void pickupBlock(BlockDudeState s, int maxx){

		BlockDudeAgent agent = s.agent.copy();
		s.agent = agent;

		int [][] map = s.map.map;

		if(agent.holding){
			return; //already holding a block
		}

		int ax = agent.x;
		int ay = agent.y;
		int dir = agent.dir;

		if(dir == 0){
			dir = -1;
		}

		//can only pick up blocks one unit away in agent facing direction and at same height as agent
		int bx = ax+dir;
		BlockDudeCell block = getBlockAt(s, bx, ay);

		if(block != null){

			//make sure that block is the top of the world, otherwise something is stacked above it and you cannot pick it up
			BlockDudeCell blockAbove = getBlockAt(s, bx, ay+1);
			if(blockAbove != null){
				return;
			}

			if(map[bx][ay+1] == 1){
				return ;
			}

			s.copyBlocks();
			s.blocks.remove(block);
			block = block.copy();
			s.blocks.add(block);


			block.x = ax;
			block.y = ay+1;

			agent.holding = true;

		}


	}

	/**
	 * Modifies state s to put down the block the agent is holding. If the agent is not holding a block or there is
	 * not a clear place to put the block, then the action does nothing.
	 * @param s the state to modify
	 * @param maxx the maximum x dimensionality of the world
	 */
	public static void putdownBlock(BlockDudeState s, int maxx){

		BlockDudeAgent agent = s.agent.copy();
		s.agent = agent;

		int [][] map = s.map.map;

		if(!agent.holding){
			return; //not holding a block
		}

		int ax = agent.x;
		int ay = agent.y;
		int dir = agent.dir;

		if(dir == 0){
			dir = -1;
		}


		int nx = ax + dir;

		int heightAtNX = greatestHeightBelow(s, map, maxx, nx, ay+1);
		if(heightAtNX > ay){
			return; //cannot drop block if walled off from throw position
		}

		BlockDudeCell block = getBlockAt(s, ax, ay+1); //carried block is one unit above agent
		s.copyBlocks();
		s.blocks.remove(block);
		block = block.copy();
		s.blocks.add(block);

		block.x = nx;
		block.y = heightAtNX+1;
		agent.holding = false;


	}


	/**
	 * Moves a carried block to a new position of the agent
	 * @param s the state to modify
	 * @param agent the agent
	 * @param ax the previous x position of the agent
	 * @param ay the previous y position of the agent
	 * @param nx the new x position of the *agent*
	 * @param ny the new y position of the *agent*
	 */
	protected static void moveCarriedBlockToNewAgentPosition(BlockDudeState s, BlockDudeAgent agent, int ax, int ay, int nx, int ny){
		if(agent.holding){
			//then move the box being carried too; make sure to copy data to prevent contamination
			BlockDudeCell carriedBlock = getBlockAt(s, ax, ay+1); //carried block is one unit above agent

			s.copyBlocks();
			s.blocks.remove(carriedBlock);

			carriedBlock = carriedBlock.copy();
			carriedBlock.x = nx;
			carriedBlock.y = ny+1;
			s.blocks.add(carriedBlock);
		}
	}


	/**
	 * Finds a block object in the {@link State} located at the provided position and returns it
	 * @param s the state to check
	 * @param x the x position
	 * @param y the y position
	 * @return the {@link BlockDudeCell} for the corresponding block object in the state at the given position or null if one does not exist.
	 */
	protected static BlockDudeCell getBlockAt(BlockDudeState s, int x, int y){

		for(BlockDudeCell b : s.blocks){
			if(b.x == x && b.y == y){
				return b;
			}
		}

		return null;

	}


	/**
	 * Returns the maximum height of the world at the provided x coordinate that is &lt;= the value maxY. The height
	 * is based on either the highest brick at x and y &lt;= maxY, or the highest block at x and y &lt;= maxY.
	 * @param s the state to search
	 * @param map the brick map
	 * @param xWidth the maximum x dimensionality of the world
	 * @param x the x position to search
	 * @param maxY the y position under which the highest point is searched
	 * @return the maximum height or zero if there are no bricks or blocks at x, y &lt;= maxY.
	 */
	public static int greatestHeightBelow(BlockDudeState s, int [][] map, int xWidth, int x, int maxY){

		int maxHeight = 0;
		for(int y = maxY; y >= 0; y--){
			if(map[x][y] == 1){
				maxHeight = y;
				break;
			}
		}

		if(maxHeight < maxY){
			//then check the blocks
			for(BlockDudeCell b : s.blocks){
				if(b.x == x){
					if(b.y > maxHeight && b.y <= maxY){
						maxHeight = b.y;
					}
				}
			}
		}


		return maxHeight;
	}


	/**
	 * A class for performing a horizontal movement either east or west.
	 */
	public static class MoveAction extends SimpleAction.SimpleDeterministicAction implements FullActionModel{

		protected int dir;
		protected int maxx;

		/**
		 * Initializes.
		 * @param name the name of the action
		 * @param domain the domain to which it will be associated
		 * @param dir the direction of movement: +1 for east; -1 for west.
		 * @param maxx the max x size of the world
		 */
		public MoveAction(String name, Domain domain, int dir, int maxx){
			super(name, domain);
			this.dir = dir;
			this.maxx = maxx;
		}

		@Override
		protected State performActionHelper(State s, GroundedAction groundedAction) {
			moveHorizontally((BlockDudeState)s, dir, maxx);
			return s;
		}


	}


	/**
	 * And action class for performing an up movement action.
	 */
	public static class MoveUpAction extends SimpleAction.SimpleDeterministicAction implements FullActionModel{

		protected boolean useSemiDeep;
		protected int maxx;

		public MoveUpAction(Domain domain, int maxx){
			super(ACTION_UP, domain);
			this.maxx = maxx;
		}


		@Override
		protected State performActionHelper(State s, GroundedAction groundedAction) {
			moveUp((BlockDudeState)s, maxx);
			return s;
		}


	}


	/**
	 * An action class for performing a pickup action.
	 */
	public static class PickupAction extends SimpleAction.SimpleDeterministicAction implements FullActionModel{

		protected boolean useSemiDeep;
		protected int maxx;

		public PickupAction(Domain domain, int maxx){
			super(ACTION_PICKUP, domain);
			this.maxx = maxx;
		}


		@Override
		protected State performActionHelper(State s, GroundedAction groundedAction) {
			pickupBlock((BlockDudeState)s, maxx);
			return s;
		}

	}


	/**
	 * An action class for performing a put down action.
	 */
	public static class PutdownAction extends SimpleAction.SimpleDeterministicAction implements FullActionModel{

		protected boolean useSemiDeep;
		protected int maxx;

		public PutdownAction(Domain domain, int maxx){
			super(ACTION_PUT_DOWN, domain);
			this.maxx = maxx;
		}


		@Override
		protected State performActionHelper(State s, GroundedAction groundedAction) {
			putdownBlock((BlockDudeState) s, maxx);
			return s;
		}

	}


	/**
	 * A {@link PropositionalFunction} that takes as arguments an agent object and a block objects and evaluates whether
	 * the agent is holding the block.
	 */
	public class HoldingBlockPF extends PropositionalFunction{

		public HoldingBlockPF(OODomain domain) {
			super(PF_HOLDING_BLOCK, domain, new String[]{CLASS_AGENT, CLASS_BLOCK});
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

		public AtExitPF(OODomain domain) {
			super(PF_AT_EXIT, domain, new String[]{CLASS_AGENT, CLASS_EXIT});
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
		Domain domain = bd.generateDomain();
		State s = BlockDudeLevelConstructor.getLevel2(domain);

		Visualizer v = BlockDudeVisualizer.getVisualizer(bd.maxx, bd.maxy);



		VisualExplorer exp = new VisualExplorer(domain, v, s);

		exp.addKeyAction("w", ACTION_UP);
		exp.addKeyAction("d", ACTION_EAST);
		exp.addKeyAction("a", ACTION_WEST);
		exp.addKeyAction("s", ACTION_PICKUP);
		exp.addKeyAction("x", ACTION_PUT_DOWN);

		exp.initGUI();



	}


}
