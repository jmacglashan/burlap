package burlap.domain.singleagent.blockdude;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.*;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An implementation of the Block Dude Texas Instruments calculator puzzle game. The goal is for the player to reach
 * an exit. The player has three movement options: west, east, and up if the platform in front of them is only
 * one unit higher. However, because of the landscape, the player will never be able to reach the exit just by moving.
 * Instead, the player must manipulate a set of blocks scattered across the world using a pick up action (which raises
 * the block the player is facing above their head to be carried), and a put down action that places a held block directly
 * in front of the agent.
 * <br/><br/>
 * Block Dude has a very large state action space that makes planning problems in it difficult. States are encoded
 * by the player's position, facing direction, whether they are holding a block, the place of every block in the world
 * and an int array specifying the map of the "bricks" that define the landscape.
 * <br/>
 * <br/>
 * States representing the first three levels of Block Dude can be generated from the
 * {@link burlap.domain.singleagent.blockdude.BlockDudeLevelConstructor} and states can be visualized with the
 * {@link burlap.domain.singleagent.blockdude.BlockDudeVisualizer}. You can run this class' main method
 * to launch an interactive visualizer for the first level with keys: w, a, d, s, x for
 * the actions up, west, east, pickup, putdown, respectively.
 * <br/><br/>
 * By default this domain's actions will use a {@link burlap.oomdp.core.states.MutableState#semiDeepCopy(java.util.Set)} instead of a
 * {@link burlap.oomdp.core.states.State#copy()}. The semi-deep copy only copies {@link burlap.oomdp.core.objects.ObjectInstance}
 * in the previous state that will have its values modified by the action execution:
 * typically, the agent and a moved block are deep copied, with the un moved block objects and brick objects
 * shallow copied to the new state. This is much more memory efficient, but you should avoid directly modifying
 * any single state outside of state construction to avoid changes to other states that may be in memory that use the
 * same shallow copy. Instead, if you wish to directly modify states, always make a
 * {@link burlap.oomdp.core.states.State#copy()} first. Alternatively, you can set Actions to always make deep copies
 * by setting this class's {@link #useSemiDeep} parameter to false with with the {@link #setUseSemiDeep(boolean)} method.
 *
 * @author James MacGlashan.
 */
public class BlockDude implements DomainGenerator{


	/**
	 * X position attribute name
	 */
	public static final String							ATTX = "x";

	/**
	 * Y position attribute name
	 */
	public static final String							ATTY = "y";

	/**
	 * Direction attribute name
	 */
	public static final String							ATTDIR = "dir";

	/**
	 * Name for the boolean attribute that indicates whether the agent is holding a block
	 */
	public static final String							ATTHOLD = "holding";

	/**
	 * Name for the attribute that holds the brick map
	 */
	public static final String							ATTMAP = "map";


	/**
	 * Name for the agent OO-MDP class
	 */
	public static final String							CLASSAGENT = "agent";

	/**
	 * Name for the block OO-MDP class
	 */
	public static final String							CLASSBLOCK = "block";

	/**
	 * Name for the bricks OO-MDP class
	 */
	public static final String							CLASSBRICKS = "bricks";

	/**
	 * Name for the exit OO-MDP class
	 */
	public static final String							CLASSEXIT = "exit";


	/**
	 * Name for the up action
	 */
	public static final String							ACTIONUP = "up";

	/**
	 * Name for the east action
	 */
	public static final String							ACTIONEAST = "east";

	/**
	 * Name for the west action
	 */
	public static final String							ACTIONWEST = "west";

	/**
	 * Name for the pickup action
	 */
	public static final String							ACTIONPICKUP = "pickup";

	/**
	 * Name for the put down action
	 */
	public static final String							ACTIONPUTDOWN = "putdown";

	/**
	 * Name for the propositional function that tests whether the agent is holding a block.
	 */
	public static final String							PFHOLDINGBLOCK = "holdingBlock";

	/**
	 * Name for the propositional function that tests whether the agent is at an exit
	 */
	public static final String							PFATEXIT = "atExit";


	/**
	 * Domain parameter specifying the maximum x dimension value of the world
	 */
	protected int										maxx = 25;

	/**
	 * Domain parameter specifying the maximum y dimension value of the world
	 */
	protected int										maxy = 25;


	/**
	 * Domain parameter specifying whether actions create semi-deep copies of states or fully deep copies of states.
	 * The default is true. If true, then actions only deep copy {@link burlap.oomdp.core.objects.ObjectInstance} between
	 * states that have their values change from the action execution
	 * (typically the agent or a specifically moved block). If false,
	 * then the states are completely deep copied by action execution.
	 */
	protected  boolean									useSemiDeep = true;


	/**
	 * Initializes a world with a maximum 25x25 dimensionality and actions that use semi-deep state copies.
	 */
	public BlockDude(){
		//do nothing
	}


	/**
	 * Initializes a world with the maximum space dimensionality provided and actions that use semi-deep state copies.
	 * @param maxx
	 * @param maxy
	 */
	public BlockDude(int maxx, int maxy){
		this.maxx = maxx;
		this.maxy = maxy;
	}



	@Override
	public Domain generateDomain() {

		Domain domain = new SADomain();

		//setup attributes
		Attribute xAtt = new Attribute(domain, ATTX, Attribute.AttributeType.INT);
		xAtt.setDiscValuesForRange(0, maxx, 1);

		Attribute yAtt = new Attribute(domain, ATTY, Attribute.AttributeType.INT);
		yAtt.setDiscValuesForRange(0, maxy, 1);

		Attribute dirAtt = new Attribute(domain, ATTDIR, Attribute.AttributeType.DISC);
		dirAtt.setDiscValues(new String[]{"west", "east"});

		Attribute holdAtt = new Attribute(domain, ATTHOLD, Attribute.AttributeType.BOOLEAN);

		Attribute map = new Attribute(domain, ATTMAP, Attribute.AttributeType.INTARRAY);



		//setup object classes
		ObjectClass aclass = new ObjectClass(domain, CLASSAGENT);
		aclass.addAttribute(xAtt);
		aclass.addAttribute(yAtt);
		aclass.addAttribute(dirAtt);
		aclass.addAttribute(holdAtt);

		ObjectClass bclass = new ObjectClass(domain, CLASSBLOCK);
		bclass.addAttribute(xAtt);
		bclass.addAttribute(yAtt);

		ObjectClass brickclass = new ObjectClass(domain, CLASSBRICKS);
		brickclass.addAttribute(map);

		ObjectClass eclass = new ObjectClass(domain, CLASSEXIT);
		eclass.addAttribute(xAtt);
		eclass.addAttribute(yAtt);


		new MoveAction(ACTIONEAST, domain, 1);
		new MoveAction(ACTIONWEST, domain, -1);
		new MoveUpAction(domain);
		new PickupAction(domain);
		new PutdownAction(domain);

		new HoldingBlockPF(domain);
		new AtExitPF(domain);


		return domain;
	}


	/**
	 * Returns whether generated domain's actions use semi-deep state copies or full deep copies..
	 * See this class's documentation for more information.
	 * @return true if actions' use semi-deep state copies; false otherwise.
	 */
	public boolean getUseSemiDeep() {
		return useSemiDeep;
	}


	/**
	 * Sets whether generated domain's actions use semi-deep state copies or full deep copies. see this class's
	 * documentation for more information.
	 * @param useSemiDeep if true, then use semi-deep; if false use full deep.
	 */
	public void setUseSemiDeep(boolean useSemiDeep) {
		this.useSemiDeep = useSemiDeep;
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
	 * Returns an uninitialized state that contains the specified number of block objects. Specifically,
	 * the state will have one agent object, one exit object, one bricks object (specifying the entire landscape in
	 * an int array attribute), and nb block objects. Their values will need to be set before being used
	 * either manually or with methods like {@link #setAgent(burlap.oomdp.core.states.State, int, int, int, boolean)},
	 * {@link #setExit(burlap.oomdp.core.states.State, int, int)}, {@link #setBlock(burlap.oomdp.core.states.State, int, int, int)},
	 * and {@link #setBrickMap(burlap.oomdp.core.states.State, int[][])} or
	 * {@link #setBrickValue(burlap.oomdp.core.states.State, int, int, int)}. If you want pre-generated states,
	 * see the {@link burlap.domain.singleagent.blockdude.BlockDudeLevelConstructor}
	 * @param domain the generated Block Dude domain to which the state will belong
	 * @param nb the number of blocks to include in the state
	 * @return a {@link burlap.oomdp.core.states.State} with 1 agent object, 1 exit object, 1 bricks object and nb block objects.
	 */
	public static State getUninitializedState(Domain domain, int nb){
		State s = new MutableState();
		ObjectInstance agent = new MutableObjectInstance(domain.getObjectClass(CLASSAGENT), CLASSAGENT);
		s.addObject(agent);

		ObjectInstance exit = new MutableObjectInstance(domain.getObjectClass(CLASSEXIT), CLASSEXIT+0);
		s.addObject(exit);

		ObjectInstance bricks = new MutableObjectInstance(domain.getObjectClass(CLASSBRICKS), CLASSBRICKS);
		s.addObject(bricks);

		for(int i = 0; i < nb; i++){
			ObjectInstance block = new MutableObjectInstance(domain.getObjectClass(CLASSBLOCK), CLASSBLOCK+i);
			s.addObject(block);
		}

		return s;
	}


	/**
	 * Sets the agent object's x, y, direction, and holding attribute to the specified values.
	 * @param s the state whose agent object should be modified
	 * @param x the x position of the agent
	 * @param y the y position of the agent
	 * @param dir the direction the agent is facing
	 * @param holding whether the agent is holding a block or not
	 */
	public static void setAgent(State s, int x, int y, int dir, boolean holding){
		ObjectInstance agent = s.getObjectsOfClass(CLASSAGENT).get(0);
		agent.setValue(ATTX, x);
		agent.setValue(ATTY, y);
		agent.setValue(ATTDIR, dir);
		agent.setValue(ATTHOLD, holding);
	}


	/**
	 * Sets the x and y position of the first exit object in the state. The first exit object is selected since
	 * states tend to only have one exit.
	 * @param s the state to modify
	 * @param x the x position of the exit
	 * @param y the y position of the exit
	 */
	public static void setExit(State s, int x, int y){
		ObjectInstance exit = s.getObjectsOfClass(CLASSEXIT).get(0);
		exit.setValue(ATTX, x);
		exit.setValue(ATTY, y);
	}


	/**
	 * Sets the ith block's x and y position in a state.
	 * @param s the state to modify
	 * @param i which block ot modify
	 * @param x the x position of the block
	 * @param y the y position of the block
	 */
	public static void setBlock(State s, int i, int x, int y){
		List<ObjectInstance> blocks = s.getObjectsOfClass(CLASSBLOCK);
		if(blocks.size() <= i){
			throw new RuntimeException("Cannot modify the " + i + "th block, because there are only " + blocks.size() + "blocks in the state");
		}
		ObjectInstance block = s.getObjectsOfClass(CLASSBLOCK).get(i);
		block.setValue(ATTX, x);
		block.setValue(ATTY, y);
	}

	/**
	 * Sets the brick value in grid location x, y. If value 1 one, then a brick is set to be present at x,y. If the value
	 * is 0 then no brick is present at x,y and the agent or blocks may be moved there.
	 * @param s the state to modify
	 * @param x the x position of the brick value to set
	 * @param y the y position of the brick value to set
	 * @param v if 1, then a brick will be at position x,y; if false then no brick will be present.
	 */
	public static void setBrickValue(State s, int x, int y, int v){
		ObjectInstance bricks = s.getFirstObjectOfClass(CLASSBRICKS);
		int [] map = bricks.getIntArrayValForAttribute(ATTMAP);

		//get max x dimension
		int xWidth = (int)bricks.getObjectClass().domain.getAttribute(ATTX).upperLim;

		map[oneDMapIndex(x, y, xWidth)] = v;
		bricks.setValue(ATTMAP, map);
	}


	/**
	 * Sets the state to use the provided brick map. The first coordinate of the int matrix is the x position; the second
	 * the y position.
	 * @param s the state to modify
	 * @param map the brick map to set.
	 */
	public static void setBrickMap(State s, int [][] map){

		ObjectInstance bricks = s.getFirstObjectOfClass(CLASSBRICKS);
		int xWidth = (int)bricks.getObjectClass().domain.getAttribute(ATTX).upperLim;
		int yWidth = (int)bricks.getObjectClass().domain.getAttribute(ATTY).upperLim;

		int [] oneDMap = new int[xWidth*yWidth];

		for(int i = 0; i < map.length; i++){
			for(int j = 0; j < map[0].length; j++){
				oneDMap[oneDMapIndex(i, j, xWidth)] = map[i][j];
			}
		}

		bricks.setValue(ATTMAP, oneDMap);
	}


	/**
	 * Returns the single dimensional array index for the brick map for 2D coordinates x, y.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param xWidth the maximum x dimensionality of the world.
	 * @return the single dimensional array index into a brick map for 2D coordinates x,y.
	 */
	public static int oneDMapIndex(int x, int y, int xWidth){
		return y*xWidth + x;
	}


	/**
	 * Modifies state s to be the result of a horizontal movement. This method will also move any held blocks
	 * by the agent and cause the agent (and its held block) to fall if it walks off a cliff. The agent will not
	 * be able to move to an x position < 0 or >= the maximum x dimensionality
	 * @param s the state to modify
	 * @param dx the change in x direction; should only be +1 (east) or -1 (west).
	 * @param maxx the maximum x dimensionality of the world
	 */
	public static void moveHorizontally(State s, int dx, int maxx){

		if(dx != 1 && dx != -1){
			throw new RuntimeException("Agent horizontal movement can only be a difference of +1 (east) or -1 (west).");
		}

		ObjectInstance agent = s.getObjectsOfClass(CLASSAGENT).get(0);
		ObjectInstance bricks = s.getFirstObjectOfClass(CLASSBRICKS);
		int [] map = bricks.getIntArrayValForAttribute(ATTMAP);

		//always set direction
		if(dx > 0){
			agent.setValue(ATTDIR, 1);
		}
		else{
			agent.setValue(ATTDIR, 0);
		}


		int ax = agent.getIntValForAttribute(ATTX);
		int ay = agent.getIntValForAttribute(ATTY);

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


		agent.setValue(ATTX, nx);
		agent.setValue(ATTY, ny);



		moveCarriedBlockToNewAgentPosition(s, agent, ax, ay, nx, ny);


	}


	/**
	 * Modifies state s to be the result of a vertical movement, that will result in the agent onto the platform adjacent
	 * to its current location in the direction the agent is facing, provided that there is room for the agent (and any block
	 * it's holding) to step onto it.
	 * @param s the state to modify.
	 * @param maxx the maximum x dimensionality of the world
	 */
	public static void moveUp(State s, int maxx){

		ObjectInstance agent = s.getObjectsOfClass(CLASSAGENT).get(0);
		ObjectInstance bricks = s.getFirstObjectOfClass(CLASSBRICKS);
		int [] map = bricks.getIntArrayValForAttribute(ATTMAP);

		int ax = agent.getIntValForAttribute(ATTX);
		int ay = agent.getIntValForAttribute(ATTY);
		int dir = agent.getIntValForAttribute(ATTDIR);
		boolean holding = agent.getBooleanValForAttribute(ATTHOLD);

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

		agent.setValue(ATTX, nx);
		agent.setValue(ATTY, ny);

		moveCarriedBlockToNewAgentPosition(s, agent, ax, ay, nx, ny);


	}

	/**
	 * Modifies state s to be the result of the pick up action. If no block that is clear (i.e., no block on top of it)
	 * is in front of the agent, then a block is not picked up.
	 * @param s the state to modify.
	 * @param maxx the maximum x dimensionality of the world
	 */
	public static void pickupBlock(State s, int maxx){

		ObjectInstance agent = s.getObjectsOfClass(CLASSAGENT).get(0);
		ObjectInstance bricks = s.getFirstObjectOfClass(CLASSBRICKS);
		int [] map = bricks.getIntArrayValForAttribute(ATTMAP);

		int holding = agent.getIntValForAttribute(ATTHOLD);
		if(holding == 1){
			return; //already holding a block
		}

		int ax = agent.getIntValForAttribute(ATTX);
		int ay = agent.getIntValForAttribute(ATTY);
		int dir = agent.getIntValForAttribute(ATTDIR);

		if(dir == 0){
			dir = -1;
		}

		//can only pick up blocks one unit away in agent facing direction and at same height as agent
		int bx = ax+dir;
		ObjectInstance block = getBlockAt(s, bx, ay);

		if(block != null){

			//make sure that block is the top of the world, otherwise something is stacked above it and you cannot pick it up
			ObjectInstance blockAbove = getBlockAt(s, bx, ay+1);
			if(blockAbove != null){
				return;
			}

			if(map[oneDMapIndex(bx, ay+1, maxx)] == 1){
				return;
			}

			block.setValue(ATTX, ax);
			block.setValue(ATTY, ay+1);

			agent.setValue(ATTHOLD, 1);

		}


	}

	/**
	 * Modifies state s to put down the block the agent is holding. If the agent is not holding a block or there is
	 * not a clear place to put the block, then the action does nothing.
	 * @param s the state to modify
	 * @param maxx the maximum x dimensionality of the world
	 */
	public static void putdownBlock(State s, int maxx){

		ObjectInstance agent = s.getObjectsOfClass(CLASSAGENT).get(0);
		ObjectInstance bricks = s.getFirstObjectOfClass(CLASSBRICKS);
		int [] map = bricks.getIntArrayValForAttribute(ATTMAP);

		int holding = agent.getIntValForAttribute(ATTHOLD);
		if(holding == 0){
			return; //not holding a block
		}

		int ax = agent.getIntValForAttribute(ATTX);
		int ay = agent.getIntValForAttribute(ATTY);
		int dir = agent.getIntValForAttribute(ATTDIR);

		if(dir == 0){
			dir = -1;
		}


		int nx = ax + dir;

		int heightAtNX = greatestHeightBelow(s, map, maxx, nx, ay+1);
		if(heightAtNX > ay){
			return; //cannot drop block if walled off from throw position
		}

		ObjectInstance block = getBlockAt(s, ax, ay+1); //carried block is one unit above agent
		block.setValue(ATTX, nx);
		block.setValue(ATTY, heightAtNX+1); //stacked on top of this position

		agent.setValue(ATTHOLD, 0);

	}


	/**
	 * Moves a carried block to a new position of the agent
	 * @param s the state to modify
	 * @param agent the agent {@link burlap.oomdp.core.objects.ObjectInstance}
	 * @param ax the previous x position of the agent
	 * @param ay the previous y position of the agent
	 * @param nx the new x position of the *agent*
	 * @param ny the new y position of the *agent*
	 */
	protected static void moveCarriedBlockToNewAgentPosition(State s, ObjectInstance agent, int ax, int ay, int nx, int ny){
		int holding = agent.getIntValForAttribute(ATTHOLD);
		if(holding == 1){
			//then move the box being carried too
			ObjectInstance carriedBlock = getBlockAt(s, ax, ay+1); //carried block is one unit above agent
			carriedBlock.setValue(ATTX, nx);
			carriedBlock.setValue(ATTY, ny+1);
		}
	}


	/**
	 * Finds a block object in the {@link State} located at the provided position and returns its
	 * {@link burlap.oomdp.core.objects.ObjectInstance}. If not block at the location exists, then null is returned.
	 * @param s the state to check
	 * @param x the x position
	 * @param y the y position
	 * @return the {@link burlap.oomdp.core.objects.ObjectInstance} for the corresponding block object in the state at the given position or null if one does not exist.
	 */
	protected static ObjectInstance getBlockAt(State s, int x, int y){

		List<ObjectInstance> blocks = s.getObjectsOfClass(CLASSBLOCK);
		for(ObjectInstance block : blocks){
			int bx = block.getIntValForAttribute(ATTX);
			int by = block.getIntValForAttribute(ATTY);
			if(bx == x && by == y){
				return block;
			}
		}

		return null;

	}


	/**
	 * Returns the maximum height of the world at the provided x coordinate that is <= the value maxY. The height
	 * is based on either the highest brick at x and y<=maxY, or the highest block at x and y<=maxY.
	 * @param s the state to search
	 * @param map the brick map
	 * @param xWidth the maximum x dimensionality of the world
	 * @param x the x position to search
	 * @param maxY the y position under which the highest point is searched
	 * @return the maximum height or zero if there are no bricks or blocks at x, y<=maxY.
	 */
	public static int greatestHeightBelow(State s, int [] map, int xWidth, int x, int maxY){

		int maxHeight = 0;
		for(int y = maxY; y >= 0; y--){
			if(map[oneDMapIndex(x, y, xWidth)] == 1){
				maxHeight = y;
				break;
			}
		}

		if(maxHeight < maxY){
			//then check the blocks
			List<ObjectInstance> blocks = s.getObjectsOfClass(CLASSBLOCK);
			for(ObjectInstance b : blocks){
				int bx = b.getIntValForAttribute(ATTX);
				if(bx == x){
					int by = b.getIntValForAttribute(ATTY);
					if(by > maxHeight && by <= maxY){
						maxHeight = by;
					}
				}
			}
		}


		return maxHeight;
	}


	/**
	 * A class for performing a horizontal movement either east or west.
	 */
	public class MoveAction extends Action implements FullActionModel{

		protected int dir;
		protected boolean useSemiDeep;
		protected int maxx;

		/**
		 * Initializes.
		 * @param name the name of the action
		 * @param domain the domain to which it will be associated
		 * @param dir the direction of movement: +1 for east; -1 for west.
		 */
		public MoveAction(String name, Domain domain, int dir){
			super(name, domain);
			this.dir = dir;
			this.useSemiDeep = BlockDude.this.useSemiDeep;
			this.maxx = BlockDude.this.maxx;
		}


		@Override
		public State performAction(State s, GroundedAction groundedAction){


			if(useSemiDeep && s instanceof MutableState){
				Set<ObjectInstance> deepCopiedObjects = new HashSet<ObjectInstance>(2);

				ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
				deepCopiedObjects.add(agent);
				int ah = agent.getIntValForAttribute(ATTHOLD);

				if(ah == 1){
					int ax = agent.getIntValForAttribute(ATTX);
					int ay = agent.getIntValForAttribute(ATTY);

					ObjectInstance block = getBlockAt(s, ax, ay+1);
					if(block != null){
						deepCopiedObjects.add(block);
					}

				}

				State copid = ((MutableState)s).semiDeepCopy(deepCopiedObjects);

				return performActionHelper(copid, groundedAction);
			}
			return super.performAction(s, groundedAction);
		}


		@Override
		protected State performActionHelper(State s, GroundedAction groundedAction) {
			moveHorizontally(s, dir, maxx);
			return s;
		}

		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
			return deterministicTransition(s, groundedAction);
		}

	}


	/**
	 * And action class for performing an up movement action.
	 */
	public class MoveUpAction extends Action implements FullActionModel{

		protected boolean useSemiDeep;
		protected int maxx;

		public MoveUpAction(Domain domain){
			super(ACTIONUP, domain);
			this.useSemiDeep = BlockDude.this.useSemiDeep;
			this.maxx = BlockDude.this.maxx;
		}

		@Override
		public State performAction(State s, GroundedAction groundedAction){


			if(useSemiDeep && s instanceof MutableState){
				Set<ObjectInstance> deepCopiedObjects = new HashSet<ObjectInstance>(2);

				ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
				deepCopiedObjects.add(agent);
				int ah = agent.getIntValForAttribute(ATTHOLD);

				if(ah == 1){
					int ax = agent.getIntValForAttribute(ATTX);
					int ay = agent.getIntValForAttribute(ATTY);

					ObjectInstance block = getBlockAt(s, ax, ay+1);
					if(block != null){
						deepCopiedObjects.add(block);
					}

				}

				State copid = ((MutableState)s).semiDeepCopy(deepCopiedObjects);

				return performActionHelper(copid, groundedAction);
			}
			return super.performAction(s, groundedAction);
		}


		@Override
		protected State performActionHelper(State s, GroundedAction groundedAction) {
			moveUp(s, maxx);
			return s;
		}

		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
			return deterministicTransition(s, groundedAction);
		}

	}


	/**
	 * An action class for performing a pickup action.
	 */
	public class PickupAction extends Action implements FullActionModel{

		protected boolean useSemiDeep;
		protected int maxx;

		public PickupAction(Domain domain){
			super(ACTIONPICKUP, domain);
			this.useSemiDeep = BlockDude.this.useSemiDeep;
			this.maxx = BlockDude.this.maxx;
		}


		@Override
		public State performAction(State s, GroundedAction groundedAction){


			if(useSemiDeep && s instanceof MutableState){
				Set<ObjectInstance> deepCopiedObjects = new HashSet<ObjectInstance>(2);

				ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
				deepCopiedObjects.add(agent);
				int ah = agent.getIntValForAttribute(ATTHOLD);

				if(ah == 0){
					int ax = agent.getIntValForAttribute(ATTX);
					int ay = agent.getIntValForAttribute(ATTY);
					int dir = agent.getIntValForAttribute(ATTDIR);

					if(dir == 0){
						dir = -1;
					}

					ObjectInstance block = getBlockAt(s, ax+dir, ay);
					if(block != null){
						deepCopiedObjects.add(block);
					}

				}

				State copid = ((MutableState)s).semiDeepCopy(deepCopiedObjects);

				return performActionHelper(copid, groundedAction);
			}
			return super.performAction(s, groundedAction);
		}


		@Override
		protected State performActionHelper(State s, GroundedAction groundedAction) {
			pickupBlock(s, maxx);
			return s;
		}

		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
			return deterministicTransition(s, groundedAction);
		}
	}


	/**
	 * An action class for performing a put down action.
	 */
	public class PutdownAction extends Action implements FullActionModel{

		protected boolean useSemiDeep;
		protected int maxx;

		public PutdownAction(Domain domain){
			super(ACTIONPUTDOWN, domain);
			this.useSemiDeep = BlockDude.this.useSemiDeep;
			this.maxx = BlockDude.this.maxx;
		}


		@Override
		public State performAction(State s, GroundedAction groundedAction){


			if(useSemiDeep && s instanceof MutableState){
				Set<ObjectInstance> deepCopiedObjects = new HashSet<ObjectInstance>(2);

				ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
				deepCopiedObjects.add(agent);
				int ah = agent.getIntValForAttribute(ATTHOLD);

				if(ah == 1){
					int ax = agent.getIntValForAttribute(ATTX);
					int ay = agent.getIntValForAttribute(ATTY);

					ObjectInstance block = getBlockAt(s, ax, ay+1);
					if(block != null){
						deepCopiedObjects.add(block);
					}

				}

				State copid = ((MutableState)s).semiDeepCopy(deepCopiedObjects);

				return performActionHelper(copid, groundedAction);
			}
			return super.performAction(s, groundedAction);
		}


		@Override
		protected State performActionHelper(State s, GroundedAction groundedAction) {
			putdownBlock(s, maxx);
			return s;
		}

		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
			return deterministicTransition(s, groundedAction);
		}
	}


	/**
	 * A {@link burlap.oomdp.core.PropositionalFunction} that takes as arguments an agent object and a block objects and evaluates whether
	 * the agent is holding the block.
	 */
	public class HoldingBlockPF extends PropositionalFunction{

		public HoldingBlockPF(Domain domain) {
			super(PFHOLDINGBLOCK, domain, new String[]{CLASSAGENT, CLASSBLOCK});
		}


		@Override
		public boolean isTrue(State st, String[] params) {

			ObjectInstance agent = st.getObject(params[0]);
			ObjectInstance block = st.getObject(params[1]);

			int ax = agent.getIntValForAttribute(ATTX);
			int ay = agent.getIntValForAttribute(ATTY);
			int ah = agent.getIntValForAttribute(ATTHOLD);

			int bx = block.getIntValForAttribute(ATTX);
			int by = block.getIntValForAttribute(ATTY);

			if(ax == bx && ay == by-1 && ah == 1){
				return true;
			}

			return false;
		}



	}


	/**
	 * A {@link burlap.oomdp.core.PropositionalFunction} that takes as arguments an agent object and an exit object
	 * and evaluates whether the agent is at the exit.
	 */
	public class AtExitPF extends PropositionalFunction{

		public AtExitPF(Domain domain) {
			super(PFATEXIT, domain, new String[]{CLASSAGENT,CLASSEXIT});
		}



		@Override
		public boolean isTrue(State st, String[] params) {

			ObjectInstance agent = st.getObject(params[0]);
			ObjectInstance exit = st.getObject(params[1]);

			int ax = agent.getIntValForAttribute(ATTX);
			int ay = agent.getIntValForAttribute(ATTY);


			int ex = exit.getIntValForAttribute(ATTX);
			int ey = exit.getIntValForAttribute(ATTY);

			if(ax == ex && ay == ey){
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

		exp.addKeyAction("w", ACTIONUP);
		exp.addKeyAction("d", ACTIONEAST);
		exp.addKeyAction("a", ACTIONWEST);
		exp.addKeyAction("s", ACTIONPICKUP);
		exp.addKeyAction("x", ACTIONPUTDOWN);

		exp.initGUI();



	}


}
