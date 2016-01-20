package burlap.domain.stochasticgames.gridgame;

import java.util.List;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SingleAction;
import burlap.oomdp.stochasticgames.common.UniversalSingleAction;
import burlap.oomdp.stochasticgames.explorers.SGVisualExplorer;
import burlap.oomdp.visualizer.Visualizer;

/**
 * The GridGame domain is much like the GridWorld domain, except for arbitrarily many agents in
 * a stochastic game. Each agent in the world has an OO-MDO object instance of OO-MDP class "agent"
 * which is defined by an x position, a y position, and a player number. Agents can either move north, south, east,
 * west, or do nothing. There is also an OO-MDP object class for 1-dimensional walls (both for horizontal
 * walls or vertical walls). Each wall can take on a different type; a solid wall that can never be passed,
 * and a semi-wall, can be passed with some stochastic probability. Finally, there is also an OO-MDP
 * class for goal locations, which also have different types. There is a type that can be indicated
 * as a universal goal/reward location for all agents, and type that is only useful to each individual
 * agent.
 * @author James MacGlashan
 *
 */
public class GridGame implements DomainGenerator {

	
	/**
	 * A constant for the name of the x position attribute
	 */
	public static final String				ATTX = "x";
	
	/**
	 * A constant for the name of the y position attribute
	 */
	public static final String				ATTY = "y";
	
	/**
	 * A constant for the name of the player number attribute
	 */
	public static final String				ATTPN = "playerNum";
	
	/**
	 * A constant for the name of the goal type attribute
	 */
	public static final String				ATTGT = "gt";
	
	/**
	 * A constant for the name of the first wall end position attribute. For a horizontal wall,
	 * this attribute represents the left end point; for a vertical wall, the bottom end point.
	 */
	public static final String				ATTE1 = "end1";
	
	/**
	 * A constant for the name of the second wall end position attribute. For a horizontal wall,
	 * this attribute represents the right end point; for a vertical wall, the top end point.
	 */
	public static final String				ATTE2 = "end2";
	
	/**
	 * A constant for the name of the attribute for defining the walls position along its orthogonal direction.
	 * For a horizontal wall, this attribute represents the y position of the wall; for a vertical wall,
	 * the x position.
	 */
	public static final String				ATTP = "pos";
	
	/**
	 * A constant for the name of the wall type attribute.
	 */
	public static final String				ATTWT = "wallType";
	
	
	/**
	 * A constant for the name of the agent class.
	 */
	public static final String				CLASSAGENT = "agent";
	
	/**
	 * A constant for the name of the goal class.
	 */
	public static final String				CLASSGOAL = "goal";
	
	/**
	 * A constant for the name of the horizontal wall class.
	 */
	public static final String				CLASSDIMHWALL = "dimensionlessHorizontalWall";
	
	/**
	 * A constant for the name of the vertical wall class.
	 */
	public static final String				CLASSDIMVWALL = "dimensionlessVerticalWall";
	
	
	/**
	 * A constant for the name of the north action.
	 */
	public static final String				ACTIONNORTH = "north";
	
	/**
	 * A constant for the name of the south action.
	 */
	public static final String				ACTIONSOUTH = "south";
	
	/**
	 * A constant for the name of the east action.
	 */
	public static final String				ACTIONEAST = "east";
	/**
	 * A constant for the name of the west action.
	 */
	public static final String				ACTIONWEST = "west";
	
	/**
	 * A constant for the name of the no operation (do nothing) action.
	 */
	public static final String				ACTIONNOOP = "noop";
	
	
	/**
	 * A constant for the name of a propositional function that evaluates whether an agent is in a universal goal location.
	 */
	public static final String				PFINUGOAL = "agentInUniversalGoal";
	
	/**
	 * A constant for the name of a propositional function that evaluates whether an agent is in a personal goal location for just them.
	 */
	public static final String				PFINPGOAL = "agentInPersonalGoal";
	
	
	
	
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
	 * Creates a visual explorer for a simple domain with two agents in it. The
	 * w-a-s-d keys control the movement of the first agent; the i-k-j-l keys control
	 * the movement direction of the second agent. q sets the first agent to do nothing
	 * and u sets the second agent to do nothing. When the actions for both agents have been set,
	 * the actions can be committed to affect the world by pressing the c key.
	 * 
	 * <p/>
	 * If "t" is passed as an argument then a terminal explorer is used instead.
	 * 
	 * @param args
	 */
	public static void main(String [] args){
		
		GridGame gg = new GridGame();
		
		SGDomain d = (SGDomain)gg.generateDomain();
		
		State s = getCleanState(d, 2, 3, 3, 2, 5, 5);
		
		setAgent(s, 0, 0, 0, 0);
		setAgent(s, 1, 4, 0, 1);
		
		setGoal(s, 0, 0, 4, 1);
		setGoal(s, 1, 2, 4, 0);
		setGoal(s, 2, 4, 4, 2);
		
		setHorizontalWall(s, 2, 4, 1, 3, 1);
		
		
		//System.out.println(s.getCompleteStateDescription());
		
		
		JointActionModel jam = new GridGameStandardMechanics(d);
		
		Visualizer v = GGVisualizer.getVisualizer(5, 5);
		SGVisualExplorer exp = new SGVisualExplorer(d, v, s, jam);
		
		exp.setJAC("c"); //press c to execute the constructed joint action
		
		exp.addKeyAction("w", CLASSAGENT+"0:"+ACTIONNORTH);
		exp.addKeyAction("s", CLASSAGENT+"0:"+ACTIONSOUTH);
		exp.addKeyAction("d", CLASSAGENT+"0:"+ACTIONEAST);
		exp.addKeyAction("a", CLASSAGENT+"0:"+ACTIONWEST);
		exp.addKeyAction("q", CLASSAGENT+"0:"+ACTIONNOOP);
		
		exp.addKeyAction("i", CLASSAGENT+"1:"+ACTIONNORTH);
		exp.addKeyAction("k", CLASSAGENT+"1:"+ACTIONSOUTH);
		exp.addKeyAction("l", CLASSAGENT+"1:"+ACTIONEAST);
		exp.addKeyAction("j", CLASSAGENT+"1:"+ACTIONWEST);
		exp.addKeyAction("u", CLASSAGENT+"1:"+ACTIONNOOP);
		
		exp.initGUI();
		

		
		
	}
	
	
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
	

	@Override
	public Domain generateDomain() {
		
		SGDomain domain = new SGDomain();
		
		
		Attribute xatt = new Attribute(domain, ATTX, Attribute.AttributeType.DISC);
		xatt.setDiscValuesForRange(0, maxDim, 1);
		
		Attribute yatt = new Attribute(domain, ATTY, Attribute.AttributeType.DISC);
		yatt.setDiscValuesForRange(0, maxDim, 1);
		
		Attribute e1att = new Attribute(domain, ATTE1, Attribute.AttributeType.DISC);
		e1att.setDiscValuesForRange(0, maxDim, 1);
		
		Attribute e2att = new Attribute(domain, ATTE2, Attribute.AttributeType.DISC);
		e2att.setDiscValuesForRange(0, maxDim, 1);
		
		Attribute patt = new Attribute(domain, ATTP, Attribute.AttributeType.DISC);
		patt.setDiscValuesForRange(0, maxDim, 1);
		
		Attribute pnatt = new Attribute(domain, ATTPN, Attribute.AttributeType.DISC);
		pnatt.setDiscValuesForRange(0, maxPlyrs, 1);
		
		Attribute gtatt = new Attribute(domain, ATTGT, Attribute.AttributeType.DISC);
		gtatt.setDiscValuesForRange(0, maxGT, 1);
		
		Attribute wtatt = new Attribute(domain, ATTWT, Attribute.AttributeType.DISC);
		wtatt.setDiscValuesForRange(0, maxWT, 1);
		
		
		
		ObjectClass agentClass = new ObjectClass(domain, CLASSAGENT);
		agentClass.addAttribute(xatt);
		agentClass.addAttribute(yatt);
		agentClass.addAttribute(pnatt);
		
		ObjectClass goalClass = new ObjectClass(domain, CLASSGOAL);
		goalClass.addAttribute(xatt);
		goalClass.addAttribute(yatt);
		goalClass.addAttribute(gtatt);
		
		ObjectClass horWall = new ObjectClass(domain, CLASSDIMHWALL);
		horWall.addAttribute(e1att);
		horWall.addAttribute(e2att);
		horWall.addAttribute(patt);
		horWall.addAttribute(wtatt);
		
		ObjectClass vertWall = new ObjectClass(domain, CLASSDIMVWALL);
		vertWall.addAttribute(e1att);
		vertWall.addAttribute(e2att);
		vertWall.addAttribute(patt);
		vertWall.addAttribute(wtatt);
		
		
		SingleAction actnorth = new UniversalSingleAction(domain, ACTIONNORTH);
		SingleAction actsouth = new UniversalSingleAction(domain, ACTIONSOUTH);
		SingleAction acteast = new UniversalSingleAction(domain, ACTIONEAST);
		SingleAction actwest = new UniversalSingleAction(domain, ACTIONWEST);
		SingleAction actnoop = new UniversalSingleAction(domain, ACTIONNOOP);
		
		
		PropositionalFunction aug = new AgentInUGoal(PFINUGOAL, domain);
		PropositionalFunction apg = new AgentInPGoal(PFINPGOAL, domain);
		
		
		return domain;
	}

	
	
	/**
	 * Returns a state with with the specified number of objects for each object class and with the specified boundary of
	 * the playing area
	 * @param d the domain object of the grid world
	 * @param na the number of agents/players
	 * @param ng the number of goal objects
	 * @param nhw the number of horizontal walls
	 * @param nvw the number of vertical walls
	 * @param width the width of the playing area
	 * @param height the height of the playing area
	 * @return A state with the specified number of objects
	 */
	public static State getCleanState(Domain d, int na, int ng, int nhw, int nvw, int width, int height){
		
		State s = new State();
		addNObjects(d, s, CLASSGOAL, ng);
		addNObjects(d, s, CLASSAGENT, na);
		addNObjects(d, s, CLASSDIMHWALL, nhw);
		addNObjects(d, s, CLASSDIMVWALL, nvw);
		
		setBoundaryWalls(s, width, height);
		
		
		return s;
	}
	
	
	/**
	 * AddsN objects of a specific object class to a state object
	 * @param d the domain of the object classes
	 * @param s the state to which the objects of the specified class should be added
	 * @param className the name of the object class for which to create object instances
	 * @param n the number of object instances to create
	 */
	protected static void addNObjects(Domain d, State s, String className, int n){
		for(int i = 0; i < n; i++){
			ObjectInstance o = new ObjectInstance(d.getObjectClass(className), className+i);
			s.addObject(o);
		}
	}
	
	
	/**
	 * Sets an agent's attribute values
	 * @param s the state in which the agent exists
	 * @param i indicates the ith agent object whose values should be set
	 * @param x the x position of the agent
	 * @param y the y position of the agent
	 * @param pn the player number of the agent
	 */
	public static void setAgent(State s, int i, int x, int y, int pn){
		ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(i);
		agent.setValue(ATTX, x);
		agent.setValue(ATTY, y);
		agent.setValue(ATTPN, pn);
	}
	
	
	/**
	 * Sets a goal objects attribute values
	 * @param s the state in which the goal exists
	 * @param i indicates the ith goal object whose values should be set
	 * @param x the x position of the goal
	 * @param y the y position of the goal
	 * @param gt the goal type
	 */
	public static void setGoal(State s, int i, int x, int y, int gt){
		ObjectInstance goal = s.getObjectsOfTrueClass(CLASSGOAL).get(i);
		goal.setValue(ATTX, x);
		goal.setValue(ATTY, y);
		goal.setValue(ATTGT, gt);
	}
	
	
	/**
	 * Sets boundary walls of a domain. This method will add 4 solid walls (top left bottom right) to create
	 * a playing field in which the agents can interact.
	 * @param s the state in which the walls should be added
	 * @param w the width of the playing field
	 * @param h the height of the playing field
	 */
	public static void setBoundaryWalls(State s, int w, int h){
		
		List<ObjectInstance> verticalWalls = s.getObjectsOfTrueClass(CLASSDIMVWALL);
		List<ObjectInstance> horizontalWalls = s.getObjectsOfTrueClass(CLASSDIMHWALL);
		
		ObjectInstance leftWall = verticalWalls.get(0);
		ObjectInstance rightWall = verticalWalls.get(1);
		
		ObjectInstance bottomWall = horizontalWalls.get(0);
		ObjectInstance topWall = horizontalWalls.get(1);
		
		setWallInstance(leftWall, 0, 0, h-1, 0);
		setWallInstance(rightWall, w, 0, h-1, 0);
		setWallInstance(bottomWall, 0, 0, w-1, 0);
		setWallInstance(topWall, h, 0, w-1, 0);
		
		
	}
	
	
	/**
	 * Sets the attribute values for a wall instance
	 * @param w the wall instance to set
	 * @param p the orthogonal position of the wall instance
	 * @param e1 the first end point of the wall
	 * @param e2 the second end point of the wall
	 * @param wt the type of the wall
	 */
	public static void setWallInstance(ObjectInstance w, int p, int e1, int e2, int wt){
		w.setValue(ATTP, p);
		w.setValue(ATTE1, e1);
		w.setValue(ATTE2, e2);
		w.setValue(ATTWT, wt);
	}

	
	/**
	 * Sets the attribute values for a vertical wall
	 * @param s the state in which the wall exits
	 * @param i indicates the ith vertical wall instance whose values should be set
	 * @param p the x position of the vertical wall
	 * @param e1 the bottom end point of the wall
	 * @param e2 the top end point of the wall
	 * @param wt the type of the wall
	 */
	public static void setVerticalWall(State s, int i, int p, int e1, int e2, int wt){
		setWallInstance(s.getObjectsOfTrueClass(CLASSDIMVWALL).get(i), p, e1, e2, wt);
	}
	
	
	
	/**
	 * Sets the attribute values for a horizontal wall
	 * @param s the state in which the wall exits
	 * @param i indicates the ith horizontal wall instance whose values should be set
	 * @param p the y position of the vertical wall
	 * @param e1 the left end point of the wall
	 * @param e2 the right end point of the wall
	 * @param wt the type of the wall
	 */
	public static void setHorizontalWall(State s, int i, int p, int e1, int e2, int wt){
		setWallInstance(s.getObjectsOfTrueClass(CLASSDIMHWALL).get(i), p, e1, e2, wt);
	}
	
	
	
	
	
	/**
	 * Defines a propositional function that evaluates to true when a given agent is in any universal goal
	 * @author James MacGlashan
	 *
	 */
	static class AgentInUGoal extends PropositionalFunction{

		
		/**
		 * Initializes with the given name and domain and is set to evaluate on agent objects
		 * @param name the name of the propositional function
		 * @param domain the domain for this propositional function
		 */
		public AgentInUGoal(String name, Domain domain) {
			super(name, domain, new String[]{CLASSAGENT});
		}

		@Override
		public boolean isTrue(State s, String[] params) {
			
			ObjectInstance agent = s.getObject(params[0]);
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			
			
			//find all universal goals
			List <ObjectInstance> goals = s.getObjectsOfTrueClass(CLASSGOAL);
			for(ObjectInstance goal : goals){
				
				int gt = goal.getDiscValForAttribute(ATTGT);
				if(gt == 0){
				
					int gx = goal.getDiscValForAttribute(ATTX);
					int gy = goal.getDiscValForAttribute(ATTY);
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
		 * @param domain the domain for this propositional function
		 */
		public AgentInPGoal(String name, Domain domain) {
			super(name, domain, new String[]{CLASSAGENT});
		}

		@Override
		public boolean isTrue(State s, String[] params) {
			
			ObjectInstance agent = s.getObject(params[0]);
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			int apn = agent.getDiscValForAttribute(ATTPN);
			
			//find all universal goals
			List <ObjectInstance> goals = s.getObjectsOfTrueClass(CLASSGOAL);
			for(ObjectInstance goal : goals){
				
				int gt = goal.getDiscValForAttribute(ATTGT);
				if(gt == apn+1){
				
					int gx = goal.getDiscValForAttribute(ATTX);
					int gy = goal.getDiscValForAttribute(ATTY);
					if(gx == ax && gy == ay){
						return true;
					}
					
				}
				
				
			}
			
			return false;
		}

		
		
	}
	
	
	
}
