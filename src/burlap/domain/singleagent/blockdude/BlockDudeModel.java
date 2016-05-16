package burlap.domain.singleagent.blockdude;

import burlap.domain.singleagent.blockdude.state.BlockDudeAgent;
import burlap.domain.singleagent.blockdude.state.BlockDudeCell;
import burlap.domain.singleagent.blockdude.state.BlockDudeState;
import burlap.mdp.core.Action;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.List;

import static burlap.domain.singleagent.blockdude.BlockDude.*;

/**
 * @author James MacGlashan.
 */
public class BlockDudeModel implements FullStateModel {

	protected int maxx;
	protected int maxy;

	public BlockDudeModel(int maxx, int maxy) {
		this.maxx = maxx;
		this.maxy = maxy;
	}

	@Override
	public List<StateTransitionProb> stateTransitions(State s, Action a) {
		return FullStateModel.Helper.deterministicTransition(this, s, a);
	}

	@Override
	public State sampleStateTransition(State s, Action a) {

		BlockDudeState bs = (BlockDudeState)s.copy();
		String aname = a.actionName();
		if(aname.equals(ACTION_WEST)){
			moveHorizontally(bs, -1);
		}
		else if(aname.equals(ACTION_EAST)){
			moveHorizontally(bs, 1);
		}
		else if(aname.equals(ACTION_UP)){
			moveUp(bs);
		}
		else if(aname.equals(ACTION_PICKUP)){
			putdownBlock(bs);
		}
		else if(aname.equals(ACTION_PUT_DOWN)){
			pickupBlock(bs);
		}
		else {
			throw new RuntimeException("Unknown action " + aname);
		}
		return bs;
	}


	/**
	 * Modifies state s to be the result of a horizontal movement. This method will also move any held blocks
	 * by the agent and cause the agent (and its held block) to fall if it walks off a cliff. The agent will not
	 * be able to move to an x position &lt; 0 or &gt;= the maximum x dimensionality
	 * @param s the state to modify
	 * @param dx the change in x direction; should only be +1 (east) or -1 (west).
	 */
	public void moveHorizontally(BlockDudeState s, int dx){

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
	 */
	public void moveUp(BlockDudeState s){

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
	 */
	public void pickupBlock(BlockDudeState s){

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
	 */
	public void putdownBlock(BlockDudeState s){

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
	protected void moveCarriedBlockToNewAgentPosition(BlockDudeState s, BlockDudeAgent agent, int ax, int ay, int nx, int ny){
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
	protected BlockDudeCell getBlockAt(BlockDudeState s, int x, int y){

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
	public int greatestHeightBelow(BlockDudeState s, int [][] map, int xWidth, int x, int maxY){

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

}
