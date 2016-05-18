package burlap.domain.stochasticgames.gridgame;

import burlap.domain.stochasticgames.gridgame.state.GGAgent;
import burlap.domain.stochasticgames.gridgame.state.GGGoal;
import burlap.domain.stochasticgames.gridgame.state.GGWall;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.oo.state.generic.GenericOOState;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.action.JointAction;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.action.UniversalSGActionType;
import burlap.shell.visual.SGVisualExplorer;
import burlap.mdp.stochasticgames.model.JointModel;
import burlap.mdp.stochasticgames.oo.OOSGDomain;
import burlap.visualizer.Visualizer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The GridGame domain is much like the GridWorld domain, except for arbitrarily many agents in
 * a stochastic game. Each agent in the world has an OO-MDP object instance of OO-MDP class "agent"
 * which is defined by an x position, a y position, and a player number. Agents can either move north, south, east,
 * west, or do nothing, therefore the game is symmetric for all agents. To get a standard {@link SGAgentType}
 * to use with this game, use the {@link #getStandardGridGameAgentType(SGDomain)} static method.
 * <p>
 * In this domain, there is also an OO-MDP object class for 1-dimensional walls (both for horizontal
 * walls or vertical walls). Each wall can take on a different type; a solid wall that can never be passed (type 0),
 * and a semi-wall, can be passed with some stochastic probability (type 1). Finally, there is also an OO-MDP
 * class for goal locations, which also have different types. There is a type that can be indicated
 * as a universal goal/reward location for all agents (type 0), and type that is only useful to each individual
 * agent (type i is a personal goal for player i-1).
 * <p>
 * The {@link JointModel} set for the domain is {@link burlap.domain.stochasticgames.gridgame.GridGameStandardMechanics},
 * with a default semi-wall probability of passing through of 0.5, which is changeable with the
 *
 * @author James MacGlashan
 *
 */
public class GridGame implements DomainGenerator {

	
	/**
	 * A constant for the name of the x position attribute
	 */
	public static final String VAR_X = "x";
	
	/**
	 * A constant for the name of the y position attribute
	 */
	public static final String VAR_Y = "y";
	
	/**
	 * A constant for the name of the player number attribute. The first player number is 0.
	 */
	public static final String VAR_PN = "playerNum";
	
	/**
	 * A constant for the name of the goal type attribute. Type 0 corresponds to a universal goal. Type i corresponds to a personal goal for player i-1.
	 */
	public static final String VAR_GT = "gt";
	
	/**
	 * A constant for the name of the first wall end position attribute. For a horizontal wall,
	 * this attribute represents the left end point; for a vertical wall, the bottom end point.
	 */
	public static final String VAR_E1 = "end1";
	
	/**
	 * A constant for the name of the second wall end position attribute. For a horizontal wall,
	 * this attribute represents the right end point; for a vertical wall, the top end point.
	 */
	public static final String VAR_E2 = "end2";
	
	/**
	 * A constant for the name of the attribute for defining the walls position along its orthogonal direction.
	 * For a horizontal wall, this attribute represents the y position of the wall; for a vertical wall,
	 * the x position.
	 */
	public static final String VAR_POS = "pos";
	
	/**
	 * A constant for the name of the wall type attribute.
	 */
	public static final String VAR_WT = "wallType";
	
	
	/**
	 * A constant for the name of the agent class.
	 */
	public static final String CLASS_AGENT = "agent";
	
	/**
	 * A constant for the name of the goal class.
	 */
	public static final String CLASS_GOAL = "goal";
	
	/**
	 * A constant for the name of the horizontal wall class.
	 */
	public static final String CLASS_DIM_H_WALL = "dimensionlessHorizontalWall";
	
	/**
	 * A constant for the name of the vertical wall class.
	 */
	public static final String CLASS_DIM_V_WALL = "dimensionlessVerticalWall";
	
	
	/**
	 * A constant for the name of the north action.
	 */
	public static final String ACTION_NORTH = "north";
	
	/**
	 * A constant for the name of the south action.
	 */
	public static final String ACTION_SOUTH = "south";
	
	/**
	 * A constant for the name of the east action.
	 */
	public static final String ACTION_EAST = "east";
	/**
	 * A constant for the name of the west action.
	 */
	public static final String ACTION_WEST = "west";
	
	/**
	 * A constant for the name of the no operation (do nothing) action.
	 */
	public static final String ACTION_NOOP = "noop";
	
	
	/**
	 * A constant for the name of a propositional function that evaluates whether an agent is in a universal goal location.
	 */
	public static final String PF_IN_U_GOAL = "inUniversalGoal";
	
	/**
	 * A constant for the name of a propositional function that evaluates whether an agent is in a personal goal location for just them.
	 */
	public static final String PF_IN_P_GOAL = "inPersonalGoal";
	
	
	
	
	/**
	 * The width and height of the world.
	 */
	protected int 							maxDim = 50;
	
	/**
	 * The maximum number of players that can be in the game
	 */
	protected int 							maxPlyrs = 10;
	
	/**
	 * The number of goal types
	 */
	protected int 							maxGT = maxPlyrs+1;
	
	/**
	 * The number of wall types
	 */
	protected int 							maxWT = 2;


	/**
	 * The probability that an agent will pass through a semi-wall.
	 */
	protected double						semiWallProb = 0.5;

	
	
	/**
	 * Returns the maximum dimension of the world; it's width and height.
	 * @return the maximum dimension of the world; it's width and height.
	 */
	public int getMaxDim() {
		return maxDim;
	}

	
	/**
	 * Sets the maximum dimension of the world; it's width and height.
	 * @param maxDim the maximum dimension of the world; it's width and height.
	 */
	public void setMaxDim(int maxDim) {
		this.maxDim = maxDim;
	}

	
	/**
	 * Returns the max number of players
	 * @return the max number of players
	 */
	public int getMaxPlyrs() {
		return maxPlyrs;
	}

	/**
	 * Sets the max number of players
	 * @param maxPlyrs the max number of players
	 */
	public void setMaxPlyrs(int maxPlyrs) {
		this.maxPlyrs = maxPlyrs;
	}

	/**
	 * Returns the maximum goal types
	 * @return the maximum goal types
	 */
	public int getMaxGT() {
		return maxGT;
	}

	
	/**
	 * Sets the maximum goal types
	 * @param maxGT the maximum goal types
	 */
	public void setMaxGT(int maxGT) {
		this.maxGT = maxGT;
	}

	
	/**
	 * Returns the maximum number of wall types
	 * @return the maximum number of wall types
	 */
	public int getMaxWT() {
		return maxWT;
	}

	
	/**
	 * Sets the maximum number of wall types
	 * @param maxWT the maximum number of wall types
	 */
	public void setMaxWT(int maxWT) {
		this.maxWT = maxWT;
	}


	/**
	 * Sets the probability that an agent can pass through a semi-wall.
	 * @param p the probability that an agent will pass through a semi-wall.
	 */
	public void setSemiWallPassableProbability(double p){
		this.semiWallProb = p;
	}

	/**
	 * Returns the probability that an agent can pass through a semi-wall.
	 * @return the probability that an agent can pass through a semi-wall.
	 */
	public double getSemiWallProb(){
		return this.semiWallProb;
	}
	

	List<PropositionalFunction> generatePFs(){
		return Arrays.asList(new AgentInUGoal(PF_IN_U_GOAL), new AgentInPGoal(PF_IN_P_GOAL));
	}

	@Override
	public OOSGDomain generateDomain() {
		
		OOSGDomain domain = new OOSGDomain();
		
		
		domain.addStateClass(CLASS_AGENT, GGAgent.class)
				.addStateClass(CLASS_GOAL, GGGoal.class)
				.addStateClass(CLASS_DIM_H_WALL, GGWall.GGHorizontalWall.class)
				.addStateClass(CLASS_DIM_V_WALL, GGWall.GGVerticalWall.class);
		

		domain.addSGAgentAction(new UniversalSGActionType(ACTION_NORTH))
				.addSGAgentAction(new UniversalSGActionType(ACTION_SOUTH))
				.addSGAgentAction(new UniversalSGActionType(ACTION_EAST))
				.addSGAgentAction(new UniversalSGActionType(ACTION_WEST))
				.addSGAgentAction(new UniversalSGActionType(ACTION_NOOP));

		
		
		OODomain.Helper.addPfsToDomain(domain, this.generatePFs());

		domain.setJointActionModel(new GridGameStandardMechanics(domain, this.semiWallProb));
		
		return domain;
	}

	




	/**
	 * Returns the initial state for a simple game in which both players can win without interfering with one another.
	 * @return the simple game initial state
	 */
	public static State getSimpleGameInitialState(){

		GenericOOState s = new GenericOOState(
				new GGAgent(0, 0, 0, "agent0"),
				new GGAgent(2, 0, 1, "agent1"),
				new GGGoal(0, 2, 1, "g0"),
				new GGGoal(2, 2, 2, "g1")
		);

		setBoundaryWalls(s, 3, 3);

		return s;
	}
	
	
	/**
	 * Returns the initial state for a classic coordination game, where the agent's personal goals are on opposite sides.
	 * @return the coordination game initial state
	 */
	public static State getCorrdinationGameInitialState(){

		GenericOOState s = new GenericOOState(
				new GGAgent(0, 0, 0, "agent0"),
				new GGAgent(2, 0, 1, "agent1"),
				new GGGoal(0, 2, 2, "g0"),
				new GGGoal(2, 2, 1, "g1")
		);

		setBoundaryWalls(s, 3, 3);

		return s;
	}
	
	
	/**
	 * Returns the initial state for a classic prisoner's dilemma formulated in a Grid Game.
	 * @return the grid game prisoner's dilemma initial state
	 */
	public static State getPrisonersDilemmaInitialState(){

		GenericOOState s = new GenericOOState(
				new GGAgent(3, 0, 0, "agent0"),
				new GGAgent(5, 0, 1, "agent1"),
				new GGGoal(0, 0, 1, "g0"),
				new GGGoal(4, 0, 0, "g1"),
				new GGGoal(8, 0, 2, "g2")
		);

		setBoundaryWalls(s, 9, 1);

		return s;
	}
	
	
	/**
	 * Returns the initial state for Friend Foe game.
	 * @return the initial state for Friend Foe
	 */
	public static State getFriendFoeInitialState(){

		GenericOOState s = new GenericOOState(
				new GGAgent(3, 0 ,0, "agent0"),
				new GGAgent(6, 0, 1, "agent1"),
				new GGGoal(0, 0, 1, "g0"),
				new GGGoal(4, 0, 0, "g1")
		);


		setBoundaryWalls(s, 8, 1);

		return s;
	}
	
	
	/**
	 * Returns the initial state for the Incredible game (a game in which player 0 can give an incredible threat).
	 * @return the initial state for the Incredible game.
	 */
	public static State getIncredibleInitialState(){

		GenericOOState s = new GenericOOState(
				new GGAgent(2, 0, 0, "agent0"),
				new GGAgent(3, 0, 1, "agent1"),
				new GGGoal(0, 0, 1, "g0"),
				new GGGoal(1, 0, 2, "g1")
		);

		setBoundaryWalls(s, 4, 1);
		
		return s;
		
	}
	
	
	public static State getTurkeyInitialState(){

		GenericOOState s = new GenericOOState(
				new GGAgent(0, 0, 0, "agent0"),
				new GGAgent(2, 0, 1, "agent1"),
				new GGGoal(0, 3, 1, "g0"),
				new GGGoal(1, 2, 0, "g1"),
				new GGGoal(2, 3, 2, "g2"),
				new GGWall.GGHorizontalWall(0, 0, 1, 1, "w0"),
				new GGWall.GGHorizontalWall(2, 2, 1, 1, "w1")

		);

		setBoundaryWalls(s, 3, 4);
		
		return s;
		
	}
	
	/**

	
	
	/**
	 * Sets boundary walls of a domain. This method will add 4 solid walls (top left bottom right) to create
	 * a playing field in which the agents can interact.
	 * @param s the state in which the walls should be added
	 * @param w the width of the playing field
	 * @param h the height of the playing field
	 */
	public static void setBoundaryWalls(GenericOOState s, int w, int h){

		int numV = s.objectsOfClass(CLASS_DIM_V_WALL).size();
		int numH = s.objectsOfClass(CLASS_DIM_H_WALL).size();
		
		s.addObject(new GGWall.GGVerticalWall(0, h-1, 0, 0, "h"+numH))
				.addObject(new GGWall.GGVerticalWall(0, h-1, w, 0, "h"+(numH+1)))
				.addObject(new GGWall.GGHorizontalWall(0, w-1, 0, 0, "v"+numV))
				.addObject(new GGWall.GGHorizontalWall(0, w-1, h, 0, "v"+(numV+1)) );

		
		
	}



	/**
	 * Creates and returns a standard {@link SGAgentType} for grid games. This {@link SGAgentType}
	 * is assigned the type name "agent", grid game OO-MDP object class for "agent", and has its action space set to all possible actions in the grid game domain.
	 * Typically, all agents in a grid game should be assigned to the same type.
	 *
	 * @param domain the domain object of the grid game.
	 * @return An {@link SGAgentType} that typically all {@link SGAgent}'s of the grid game should play as.
	 */
	public static SGAgentType getStandardGridGameAgentType(SGDomain domain){
		return new SGAgentType(GridGame.CLASS_AGENT, domain.getAgentActions());
	}
	
	
	
	/**
	 * Defines a propositional function that evaluates to true when a given agent is in any universal goal
	 * @author James MacGlashan
	 *
	 */
	static class AgentInUGoal extends PropositionalFunction {

		
		/**
		 * Initializes with the given name and domain and is set to evaluate on agent objects
		 * @param name the name of the propositional function
		 */
		public AgentInUGoal(String name) {
			super(name, new String[]{CLASS_AGENT});
		}

		@Override
		public boolean isTrue(OOState s, String... params) {
			
			ObjectInstance agent = s.object(params[0]);
			int ax = (Integer)agent.get(VAR_X);
			int ay = (Integer)agent.get(VAR_Y);
			
			
			//find all universal goals
			List <ObjectInstance> goals = s.objectsOfClass(CLASS_GOAL);
			for(ObjectInstance goal : goals){
				
				int gt = (Integer)goal.get(VAR_GT);
				if(gt == 0){
				
					int gx = (Integer)goal.get(VAR_X);
					int gy = (Integer)goal.get(VAR_Y);
					if(gx == ax && gy == ay){
						return true;
					}
					
				}
				
				
			}
			
			return false;
		}
		
		
	}
	
	
	/**
	 * Defines a propositional function that evaluates to true when a given agent is in any of its personal goals
	 * @author James MacGlashan
	 *
	 */
	static class AgentInPGoal extends PropositionalFunction{

		
		/**
		 * Initializes with the given name and domain and is set to evaluate on agent objects
		 * @param name the name of the propositional function
		 */
		public AgentInPGoal(String name) {
			super(name, new String[]{CLASS_AGENT});
		}

		@Override
		public boolean isTrue(OOState s, String... params) {
			
			ObjectInstance agent = s.object(params[0]);
			int ax = (Integer)agent.get(VAR_X);
			int ay = (Integer)agent.get(VAR_Y);
			int apn = (Integer)agent.get(VAR_PN);
			
			//find all universal goals
			List <ObjectInstance> goals = s.objectsOfClass(CLASS_GOAL);
			for(ObjectInstance goal : goals){
				
				int gt = (Integer)goal.get(VAR_GT);
				if(gt == apn+1){
				
					int gx = (Integer)goal.get(VAR_X);
					int gy = (Integer)goal.get(VAR_Y);
					if(gx == ax && gy == ay){
						return true;
					}
					
				}
				
				
			}
			
			return false;
		}

		
		
	}
	
	
	/**
	 * Specifies goal rewards and default rewards for agents. Defaults rewards to 0 reward everywhere except transition to unviersal or personal goals which return a reward 1.
	 * @author James MacGlashan
	 *
	 */
	public static class GGJointRewardFunctionFunction implements JointRewardFunction {

		PropositionalFunction agentInPersonalGoal;
		PropositionalFunction agentInUniversalGoal;
		
		double stepCost = 0.;
		double pGoalReward = 1.;
		double uGoalReward = 1.;
		boolean noopIncursCost = false;
		Map<Integer, Double> personalGoalRewards = null;
		
		/**
		 * Initializes for a given domain. Defaults rewards to 0 reward everywhere except transition to unviersal or personal goals which return a reward 1.
		 * @param ggDomain the domain
		 */
		public GGJointRewardFunctionFunction(OODomain ggDomain){
			agentInPersonalGoal = ggDomain.getPropFunction(GridGame.PF_IN_P_GOAL);
			agentInUniversalGoal = ggDomain.getPropFunction(GridGame.PF_IN_U_GOAL);
		}
		
		/**
		 * Initializes for a given domain, step cost reward and goal reward.
		 * @param ggDomain the domain
		 * @param stepCost the reward returned for all transitions except transtions to goal locations
		 * @param goalReward the reward returned for transitioning to a personal or universal goal
		 * @param noopIncursStepCost if true, then noop actions also incur the stepCost reward; if false, then noops always return 0 reward.
		 */
		public GGJointRewardFunctionFunction(OODomain ggDomain, double stepCost, double goalReward, boolean noopIncursStepCost){
			agentInPersonalGoal = ggDomain.getPropFunction(GridGame.PF_IN_P_GOAL);
			agentInUniversalGoal = ggDomain.getPropFunction(GridGame.PF_IN_U_GOAL);
			this.stepCost = stepCost;
			this.pGoalReward = this.uGoalReward = goalReward;
			this.noopIncursCost = noopIncursStepCost;
		}
		
		
		/**
		 * Initializes for a given domain, step cost reward, personal goal reward, and universal goal reward.
		 * @param ggDomain the domain
		 * @param stepCost the reward returned for all transitions except transtions to goal locations
		 * @param personalGoalReward the reward returned for transitions to a personal goal
		 * @param universalGoalReward the reward returned for transitions to a universal goal
		 * @param noopIncursStepCost if true, then noop actions also incur the stepCost reward; if false, then noops always return 0 reward.
		 */
		public GGJointRewardFunctionFunction(OODomain ggDomain, double stepCost, double personalGoalReward, double universalGoalReward, boolean noopIncursStepCost){
			agentInPersonalGoal = ggDomain.getPropFunction(GridGame.PF_IN_P_GOAL);
			agentInUniversalGoal = ggDomain.getPropFunction(GridGame.PF_IN_U_GOAL);
			this.stepCost = stepCost;
			this.pGoalReward = personalGoalReward;
			this.uGoalReward = universalGoalReward;
			this.noopIncursCost = noopIncursStepCost;
		}
		
		/**
		 * Initializes for a given domain, step cost reward, universal goal reward, and unique personal goal reward for each player.
		 * @param ggDomain the domain
		 * @param stepCost the reward returned for all transitions except transtions to goal locations
		 * @param universalGoalReward the reward returned for transitions to a universal goal
		 * @param noopIncursStepCost if true, then noop actions also incur the stepCost reward; if false, then noops always return 0 reward.
		 * @param personalGoalRewards a map from player numbers to their personal goal reward (the first player number is 0)
		 */
		public GGJointRewardFunctionFunction(OODomain ggDomain, double stepCost, double universalGoalReward, boolean noopIncursStepCost, Map<Integer, Double> personalGoalRewards){
			
			agentInPersonalGoal = ggDomain.getPropFunction(GridGame.PF_IN_P_GOAL);
			agentInUniversalGoal = ggDomain.getPropFunction(GridGame.PF_IN_U_GOAL);
			this.stepCost = stepCost;
			this.uGoalReward = universalGoalReward;
			this.noopIncursCost = noopIncursStepCost;
			this.personalGoalRewards = personalGoalRewards;
			
		}
		
		@Override
		public Map<String, Double> reward(State s, JointAction ja, State sp) {

			OOState osp = (OOState)sp;

			Map <String, Double> rewards = new HashMap<String, Double>();
			
			//get all agents and initialize reward to default
			List <ObjectInstance> obs = osp.objectsOfClass(GridGame.CLASS_AGENT);
			for(ObjectInstance o : obs){
				rewards.put(o.name(), this.defaultCost(o.name(), ja));
			}
			
			
			//check for any agents that reached a universal goal location and give them a goal reward if they did
			//List<GroundedProp> upgps = sp.getAllGroundedPropsFor(agentInUniversalGoal);
			List<GroundedProp> upgps = agentInUniversalGoal.getAllGroundedPropsForState(sp);
			for(GroundedProp gp : upgps){
				String agentName = gp.params[0];
				if(gp.isTrue(osp)){
					rewards.put(agentName, uGoalReward);
				}
			}
			
			
			//check for any agents that reached a personal goal location and give them a goal reward if they did
			//List<GroundedProp> ipgps = sp.getAllGroundedPropsFor(agentInPersonalGoal);
			List<GroundedProp> ipgps = agentInPersonalGoal.getAllGroundedPropsForState(sp);
			for(GroundedProp gp : ipgps){
				String agentName = gp.params[0];
				if(gp.isTrue(osp)){
					rewards.put(agentName, this.getPersonalGoalReward(osp, agentName));
				}
			}
			
			
			return rewards;
			
		}
		
		
		/**
		 * Returns a default cost for an agent assuming the agent didn't transition to a goal state. If noops incur step cost, then this is always the step cost.
		 * If noops do not incur step costs and the agent took a noop, then 0 is returned.
		 * @param aname the name of the agent for which the default reward should be returned.
		 * @param ja the joint action set
		 * @return the default reward; either step cost or 0.
		 */
		protected double defaultCost(String aname, JointAction ja){
			if(this.noopIncursCost){
				return this.stepCost;
			}
			else if(ja.action(aname) == null || ja.action(aname).actionName().equals(GridGame.ACTION_NOOP)){
				return 0.;
			}
			return this.stepCost;
		}
		
		
		/**
		 * Returns the personal goal rewards. If a single common personal goal reward was set then that is returned. If different personal goal rewards were defined for each
		 * player number, then that is queried and returned instead.
		 * @param s the state in which the agent player numbers are defined
		 * @param agentName the agent name for which the person goal reward is to be returned
		 * @return the personal goal reward for the specified agent.
		 */
		protected double getPersonalGoalReward(OOState s, String agentName){
			if(this.personalGoalRewards == null){
				return this.pGoalReward;
			}
			
			int pn = (Integer)s.object(agentName).get(GridGame.VAR_PN);
			return this.personalGoalRewards.get(pn);
			
		}

	}
	
	
	/**
	 * Causes termination when any agent reaches a personal or universal goal location.
	 * @author James MacGlashan
	 *
	 */
	public static class GGTerminalFunction implements TerminalFunction {

		PropositionalFunction agentInPersonalGoal;
		PropositionalFunction agentInUniversalGoal;
		
		
		/**
		 * Initializes for the given domain
		 * @param ggDomain the specific grid world domain.
		 */
		public GGTerminalFunction(OODomain ggDomain){
			agentInPersonalGoal = ggDomain.getPropFunction(GridGame.PF_IN_P_GOAL);
			agentInUniversalGoal = ggDomain.getPropFunction(GridGame.PF_IN_U_GOAL);
		}
		
		
		@Override
		public boolean isTerminal(State s) {
			
			//check personal goals; if anyone reached their personal goal, it's game over
			//List<GroundedProp> ipgps = s.getAllGroundedPropsFor(agentInPersonalGoal);
			List<GroundedProp> ipgps = agentInPersonalGoal.getAllGroundedPropsForState(s);
			for(GroundedProp gp : ipgps){
				if(gp.isTrue((OOState)s)){
					return true;
				}
			}
			
			
			//check universal goals; if anyone reached a universal goal, it's game over
			//List<GroundedProp> upgps = s.getAllGroundedPropsFor(agentInUniversalGoal);
			List<GroundedProp> upgps = agentInUniversalGoal.getAllGroundedPropsForState(s);
			for(GroundedProp gp : upgps){
				if(gp.isTrue((OOState)s)){
					return true;
				}
			}
			
			return false;
		}

	}


	/**
	 * Creates a visual explorer for a simple domain with two agents in it. The
	 * w-a-s-d keys control the movement of the first agent; the i-k-j-l keys control
	 * the movement direction of the second agent. q sets the first agent to do nothing
	 * and u sets the second agent to do nothing. When the actions for both agents have been set,
	 * the actions can be committed to affect the world by pressing the c key.
	 *
	 * <p>
	 * If "t" is passed as an argument then a terminal explorer is used instead.
	 *
	 * @param args command line args
	 */
	public static void main(String [] args){

		GridGame gg = new GridGame();

		OOSGDomain d = gg.generateDomain();


		State s = GridGame.getTurkeyInitialState();


		Visualizer v = GGVisualizer.getVisualizer(9, 9);
		SGVisualExplorer exp = new SGVisualExplorer(d, v, s);


		exp.addKeyAction("w", CLASS_AGENT +"0", ACTION_NORTH, "");
		exp.addKeyAction("s", CLASS_AGENT +"0", ACTION_SOUTH, "");
		exp.addKeyAction("d", CLASS_AGENT +"0", ACTION_EAST, "");
		exp.addKeyAction("a", CLASS_AGENT +"0", ACTION_WEST, "");
		exp.addKeyAction("q", CLASS_AGENT +"0", ACTION_NOOP, "");

		exp.addKeyAction("i", CLASS_AGENT +"1", ACTION_NORTH, "");
		exp.addKeyAction("k", CLASS_AGENT +"1", ACTION_SOUTH, "");
		exp.addKeyAction("l", CLASS_AGENT +"1", ACTION_EAST, "");
		exp.addKeyAction("j", CLASS_AGENT +"1", ACTION_WEST, "");
		exp.addKeyAction("u", CLASS_AGENT +"1", ACTION_NOOP, "");

		exp.initGUI();




	}
	
	
}
