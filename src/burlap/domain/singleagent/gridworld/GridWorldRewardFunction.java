package burlap.domain.singleagent.gridworld;

import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.oomdp.core.state.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * This class is used for defining reward functions in grid worlds that are a function of cell of the world to which
 * the agent transitions. That is, a double matrix (called rewardMatrix) the size of the grid world is stored. In an agent transitions
 * to cell x,y, then they will receive the double value stored in rewardMatrix[x][y]. The rewards returned for transitioning to an agent position
 * may be set with the {@link #setReward(int, int, double)} method.
 * <p>
 * This reward function is useful for simple grid worlds without any location objects or worlds for which the rewards are independent
 * of location objects. An alternative to this class is to define worlds with location objects and use the atLocation propositional function
 * and location types to define rewards.
 * @author James MacGlashan
 *
 */
public class GridWorldRewardFunction implements RewardFunction {

	protected double [][] rewardMatrix;
	protected int width;
	protected int height;
	
	
	/**
	 * Initializes the reward function for a grid world of size width and height and initializes the reward values everywhere to initializingReward.
	 * The reward returned from specific agent positions may be changed with the {@link #setReward(int, int, double)} method.
	 * @param width the width of the grid world
	 * @param height the height of the grid world
	 * @param initializingReward the reward to which all agent position transitions are initialized to return.
	 */
	public GridWorldRewardFunction(int width, int height, double initializingReward){
		this.initialize(width, height, initializingReward);
	}
	
	
	/**
	 * Initializes the reward function for a grid world of size width and height and initializes the reward values everywhere to 0.
	 * The reward returned from specific agent positions may be changed with the {@link #setReward(int, int, double)} method.
	 * @param width the width of the grid world
	 * @param height the height of the grid world
	 */
	public GridWorldRewardFunction(int width, int height){
		this(width, height, 0.);
	}

	
	
	/**
	 * Initializes the reward matrix.
	 * @param width the width of the grid world
	 * @param height the height of the grid world
	 * @param initializingReward the reward to which all agent position transitions are initialized to return.
	 */
	protected void initialize(int width, int height, double initializingReward){
		this.rewardMatrix = new double[width][height];
		this.width = width;
		this.height = height;
		for(int i = 0; i < this.width; i++){
			for(int j = 0; j < this.height; j++){
				this.rewardMatrix[i][j] = initializingReward;
			}
		}
	}
	
	/**
	 * Returns the reward matrix this reward function uses. Changes to the returned matrix *will* change this reward function.
	 * rewardMatrix[x][y] specifies the reward the agent will receive for transitioning to position x,y.
	 * @return the reward matrix this reward function uses
	 */
	public double [][] getRewardMatrix(){
		return this.rewardMatrix;
	}
	
	/**
	 * Sets the reward the agent will receive to transitioning to position x, y
	 * @param x the x position
	 * @param y the y position
	 * @param r the reward the agent will receive to transitioning to position x, y
	 */
	public void setReward(int x, int y, double r){
		this.rewardMatrix[x][y] = r;
	}
	
	/**
	 * Returns the reward this reward function will return when the agent transitions to position x, y.
	 * @param x the x position
	 * @param y the y position
	 * @return the reward this reward function will return when the agent transitions to position x, y.
	 */
	public double getRewardForTransitionsTo(int x, int y){
		return this.rewardMatrix[x][y];
	}
	
	@Override
	public double reward(State s, GroundedAction a, State sprime) {

		int x = ((GridWorldState)s).agent.x;
		int y = ((GridWorldState)s).agent.y;
		
		if(x >= this.width || x < 0 || y >= this.height || y < 0){
			throw new RuntimeException("GridWorld reward matrix is only defined for a " + this.width + "x" + 
					this.height +" world, but the agent transitioned to position (" + x + "," + y + "), which is outside the bounds.");
		}
		
		double r = this.rewardMatrix[x][y];
		return r;
	}

}
