package burlap.domain.singleagent.gridworld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.debugtools.RandomFactory;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.explorer.TerminalExplorer;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;



/**
 * A domain generator for basic grid worlds. This domain generator allows for the creation
 * of arbitrarily sized grid worlds with user defined layouts. The grid world supports
 * classic north, south, east, west movement actions that may be either deterministic
 * or stochastic with user defined stochastic failures. The domain consists of only
 * object classes: an agent class and a location class, each of which is defined by
 * and x and y position. Walls are not considered objects; instead walls are
 * considered part of the transition dynamics. There are five propositional functions
 * supported: atLocation(agent, location), wallToNorth(agent), wallToSouth(agent),
 * wallToEast(agent), and wallToWest(agent). 
 * @author James MacGlashan
 *
 */
public class GridWorldDomain implements DomainGenerator {

	/**
	 * Constant for the name of the x attribute
	 */
	public static final String							ATTX = "x";
	
	/**
	 * Constant for the name of the y attribute
	 */
	public static final String							ATTY = "y";
	
	
	/**
	 * Constant for the name of the agent class
	 */
	public static final String							CLASSAGENT = "agent";
	
	/**
	 * Constant for the name of the location class
	 */
	public static final String							CLASSLOCATION = "location";
	
	
	/**
	 * Constant for the name of the north action
	 */
	public static final String							ACTIONNORTH = "north";
	
	/**
	 * Constant for the name of the south action
	 */
	public static final String							ACTIONSOUTH = "south";
	
	/**
	 * Constant for the name of the east action
	 */
	public static final String							ACTIONEAST = "east";
	
	/**
	 * Constant for the name of the west action
	 */
	public static final String							ACTIONWEST = "west";
	
	
	
	/**
	 * Constant for the name of the at location propositional function
	 */
	public static final String							PFATLOCATION = "atLocation";
	
	/**
	 * Constant for the name of the wall to north propositional function
	 */
	public static final String							PFWALLNORTH = "wallToNorth";
	
	/**
	 * Constant for the name of the wall to south propositional function
	 */
	public static final String							PFWALLSOUTH = "wallToSouth";
	
	/**
	 * Constant for the name of the wall to east propositional function
	 */
	public static final String							PFWALLEAST = "wallToEast";
	
	/**
	 * Constant for the name of the wall to west propositional function
	 */
	public static final String							PFWALLWEST = "wallToWest";
	
	
	
	/**
	 * The width of the grid world
	 */
	protected int										width;
	
	/**
	 * The height of grid world
	 */
	protected int										height;
	
	/**
	 * The wall map where the first index is the x position and the second index is the y position.
	 * Values of 1 indicate a wall is there, values of 0 indicate an empty cell
	 */
	protected int [][]									map;
	
	/**
	 * Matrix specifying the transition dynamics in terms of movement directions. The first index
	 * indicates the action direction attempted (ordered north, south, east, west) the second index
	 * indicates the actual resulting direction the agent will go (assuming there is no wall in the way).
	 * The value is the probability of that outcome. The existence of walls does not affect the probability
	 * of the direction the agent will actually go, but if a wall is in the way, it will affect the outcome.
	 * For instance, if the agent selects north, but there is a 0.2 probability of actually going east and
	 * there is a wall to the east, then with 0.2 probability, the agent will stay in place.
	 */
	protected double[][]								transitionDynamics;
	
	
	/**
	 * Constructs an empty map with deterministic transitions
	 * @param width width of the map
	 * @param height height of the map
	 */
	public GridWorldDomain(int width, int height){
		this.width = width;
		this.height = height;
		this.setDeterministicTransitionDynamics();
		this.makeEmptyMap();
	}
	
	
	/**
	 * Constructs a deterministic world based on the provided map.
	 * @param map the first index is the x index, the second the y; 1 entries indicate a wall
	 */
	public GridWorldDomain(int [][] map){
		this.setMap(map);
		this.setDeterministicTransitionDynamics();
	}
	
	
	/**
	 * Will set the domain to use deterministic action transitions.
	 */
	public void setDeterministicTransitionDynamics(){
		int na = 4;
		transitionDynamics = new double[na][na];
		for(int i = 0; i < na; i++){
			for(int j = 0; j < na; j++){
				if(i != j){
					transitionDynamics[i][j] = 0.;
				}
				else{
					transitionDynamics[i][j] = 1.;
				}
			}
		}
	}
	
	/**
	 * Sets the domain to use probabilistic transitions. Agent will move in the intended direction with probability probSucceed. Agent
	 * will move in a random direction with probability 1 - probSucceed
	 * @param probSucceed probability to move the in intended direction
	 */
	public void setProbSucceedTransitionDynamics(double probSucceed){
		int na = 4;
		double pAlt = (1.-probSucceed)/3.;
		transitionDynamics = new double[na][na];
		for(int i = 0; i < na; i++){
			for(int j = 0; j < na; j++){
				if(i != j){
					transitionDynamics[i][j] = pAlt;
				}
				else{
					transitionDynamics[i][j] = probSucceed;
				}
			}
		}
	}
	
	/**
	 * Will set the movement direction probabilities based on the action chosen. The index (0,1,2,3) indicates the
	 * direction north,south,east,west, respectively and the matrix is organized by transitionDynamics[selectedDirection][actualDirection].
	 * For instance, the probability of the agent moving east when selecting north would be specified in the entry transitionDynamics[0][2]
	 * 
	 * @param transitionDynamics entries indicate the probability of movement in the given direction (second index) for the given action selected (first index).
	 */
	public void setTransitionDynamics(double [][] transitionDynamics){
		this.transitionDynamics = transitionDynamics.clone();
	}
	
	
	/**
	 * Makes the map empty
	 */
	public void makeEmptyMap(){
		this.map = new int[this.width][this.height];
		for(int i = 0; i < this.width; i++){
			for(int j = 0; j < this.height; j++){
				this.map[i][j] = 0;
			}
		}
	}
	
	/**
	 * Set the map of the world.
	 * @param map the first index is the x index, the second the y; 1 entries indicate a wall
	 */
	public void setMap(int [][] map){
		this.width = map.length;
		this.height = map[0].length;
		this.map = map.clone();
	}
	
	
	/**
	 * Will set the map of the world to the classic Four Rooms map used the original options work (Sutton, R.S. and Precup, D. and Singh, S., 1999).
	 */
	public void setMapToFourRooms(){
		this.width = 11;
		this.height = 11;
		this.makeEmptyMap();
		
		horizontalWall(0, 0, 5);
		horizontalWall(2, 4, 5);
		horizontalWall(6, 7, 4);
		horizontalWall(9, 10, 4);
		
		verticalWall(0, 0, 5);
		verticalWall(2, 7, 5);
		verticalWall(9, 10, 5);
		
		
	}
	
	
	/**
	 * Creates a horizontal wall.
	 * @param xi The starting x coordinate of the wall
	 * @param xf The ending x coordinate of the wall
	 * @param y The y coordinate of the wall
	 */
	public void horizontalWall(int xi, int xf, int y){
		for(int x = xi; x <= xf; x++){
			this.map[x][y] = 1;
		}
	}
	
	/**
	 * Creates a horizontal wall.
	 * @param yi The stating y coordinate of the wall
	 * @param yf The ending y coordinate of the wall
	 * @param x	The x coordinate of the wall
	 */
	public void verticalWall(int yi, int yf, int x){
		for(int y = yi; y <= yf; y++){
			this.map[x][y] = 1;
		}
	}
	
	public void setObstacleInCell(int x, int y){
		this.map[x][y] = 1;
	}
	
	
	/**
	 * Returns the map being used for the domain
	 * @return the map being used in the domain
	 */
	public int [][] getMap(){
		return this.map.clone();
	}
	

	@Override
	public Domain generateDomain() {
		
		Domain DOMAIN = new SADomain();
		
		//Creates a new Attribute object
		Attribute xatt = new Attribute(DOMAIN, ATTX, Attribute.AttributeType.DISC);
		xatt.setDiscValuesForRange(0, this.width-1, 1); //-1 due to inclusivity vs exclusivity
		
		Attribute yatt = new Attribute(DOMAIN, ATTY, Attribute.AttributeType.DISC);
		yatt.setDiscValuesForRange(0, this.height-1, 1); //-1 due to inclusivity vs exclusivity
		
		
		ObjectClass agentClass = new ObjectClass(DOMAIN, CLASSAGENT);
		agentClass.addAttribute(xatt);
		agentClass.addAttribute(yatt);
		
		ObjectClass locationClass = new ObjectClass(DOMAIN, CLASSLOCATION);
		locationClass.addAttribute(xatt);
		locationClass.addAttribute(yatt);
		
		Action north = new MovementAction(ACTIONNORTH, DOMAIN, this.transitionDynamics[0]);
		Action south = new MovementAction(ACTIONSOUTH, DOMAIN, this.transitionDynamics[1]);
		Action east = new MovementAction(ACTIONEAST, DOMAIN, this.transitionDynamics[2]);
		Action west = new MovementAction(ACTIONWEST, DOMAIN, this.transitionDynamics[3]);
		
		
		PropositionalFunction atLocationPF = new AtLocationPF(PFATLOCATION, DOMAIN, new String[]{CLASSAGENT, CLASSLOCATION});
		
		PropositionalFunction wallToNorthPF = new WallToPF(PFWALLNORTH, DOMAIN, new String[]{CLASSAGENT}, 0);
		PropositionalFunction wallToSouthPF = new WallToPF(PFWALLSOUTH, DOMAIN, new String[]{CLASSAGENT}, 1);
		PropositionalFunction wallToEastPF = new WallToPF(PFWALLEAST, DOMAIN, new String[]{CLASSAGENT}, 2);
		PropositionalFunction wallToWestPF = new WallToPF(PFWALLWEST, DOMAIN, new String[]{CLASSAGENT}, 3);
		
		return DOMAIN;
	}

	
	/**
	 * Will return a state object with a single agent object and a single location object
	 * @param d the domain object that is used to specify the min/max dimensions
	 * @return a state object with a single agent object and a single location object
	 */
	public static State getOneAgentOneLocationState(Domain d){
		
		State s = new State();
		
		s.addObject(new ObjectInstance(d.getObjectClass(CLASSLOCATION), CLASSLOCATION+0));
		s.addObject(new ObjectInstance(d.getObjectClass(CLASSAGENT), CLASSAGENT+0));
		
		
		return s;
		
	}
	
	/**
	 * Will return a state object with a single agent object and n location objects
	 * @param d the domain object that is used to specify the min/max dimensions
	 * @param n the number of location objects
	 * @return a state object with a single agent object and n location objects
	 */
	public static State getOneAgentNLocationState(Domain d, int n){
		
		State s = new State();
		
		for(int i = 0; i < n; i++){
			s.addObject(new ObjectInstance(d.getObjectClass(CLASSLOCATION), CLASSLOCATION+i));
		}
		s.addObject(new ObjectInstance(d.getObjectClass(CLASSAGENT), CLASSAGENT+0));
		
		return s;
	}
	
	
	/**
	 * Sets the first agent object in s to the specified x and y position.
	 * @param s the state with the agent whose position to set
	 * @param x the x position of the agent
	 * @param y the y position of the agent
	 */
	public static void setAgent(State s, int x, int y){
		ObjectInstance o = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
		
		o.setValue(ATTX, x);
		o.setValue(ATTY, y);
	}
	
	/**
	 * Sets the i'th location object to the specified x and y position
	 * @param s the state with the location object
	 * @param i specifies which location object index to set
	 * @param x the x position of the location
	 * @param y the y position of the location
	 */
	public static void setLocation(State s, int i, int x, int y){
		ObjectInstance o = s.getObjectsOfTrueClass(CLASSLOCATION).get(i);
		
		o.setValue(ATTX, x);
		o.setValue(ATTY, y);
	}
	
	
	
	/**
	 * Creates a visual explorer or terminal explorer. By default a visual explorer is presented; use the "t" argument
	 * to create terminal explorer. Will create a 4 rooms grid world with the agent in lower left corner and a location in
	 * the upper right. Use w-a-s-d to move.
	 * @param args
	 */
	public static void main(String[] args) {
	
		GridWorldDomain gwdg = new GridWorldDomain(11, 11);
		gwdg.setMapToFourRooms();
		gwdg.setProbSucceedTransitionDynamics(0.75);
		
		Domain d = gwdg.generateDomain();
		
		State s = getOneAgentOneLocationState(d);
		setAgent(s, 0, 0);
		setLocation(s, 0, 10, 10);
		
		
		int expMode = 1;
		if(args.length > 0){
			if(args[0].equals("v")){
				expMode = 1;
			}
			else if(args[0].equals("t")){
				expMode = 0;
			}
		}
		
		if(expMode == 0){
			
			TerminalExplorer exp = new TerminalExplorer(d);
			exp.addActionShortHand("n", ACTIONNORTH);
			exp.addActionShortHand("e", ACTIONEAST);
			exp.addActionShortHand("w", ACTIONWEST);
			exp.addActionShortHand("s", ACTIONSOUTH);
			
			exp.exploreFromState(s);
			
		}
		else if(expMode == 1){
			
			Visualizer v = GridWorldVisualizer.getVisualizer(d, gwdg.getMap());
			VisualExplorer exp = new VisualExplorer(d, v, s);
			
			//use w-s-a-d-x
			exp.addKeyAction("w", ACTIONNORTH);
			exp.addKeyAction("s", ACTIONSOUTH);
			exp.addKeyAction("a", ACTIONWEST);
			exp.addKeyAction("d", ACTIONEAST);
			
			exp.initGUI();
		}
		
		
	}
	
	
	
	
	
	/**
	 * Attempts to move the agent into the given position, taking into account walls and blocks
	 * @param the current state
	 * @param the attempted new X position of the agent
	 * @param the attempted new Y position of the agent
	 */
	protected void move(State s, int xd, int yd){
		
		ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
		int ax = agent.getDiscValForAttribute(ATTX);
		int ay = agent.getDiscValForAttribute(ATTY);
		
		int nx = ax+xd;
		int ny = ay+yd;
		
		//hit wall, so do not change position
		if(nx < 0 || nx >= this.width || ny < 0 || ny >= this.height || this.map[nx][ny] == 1){
			nx = ax;
			ny = ay;
		}
		
		agent.setValue(ATTX, nx);
		agent.setValue(ATTY, ny);
	}
	
	/**
	 * Returns the change in x and y position for a given direction number.
	 * @param i the direction number (0,1,2,3 indicates north,south,east,west, respectively)
	 * @return the change in direction for x and y; the first index of the returned double is change in x, the second index is change in y.
	 */
	protected int [] movementDirectionFromIndex(int i){
		
		int [] result = null;
		
		switch (i) {
		case 0:
			result = new int[]{0,1};
			break;
			
		case 1:
			result = new int[]{0,-1};
			break;
			
		case 2:
			result = new int[]{1,0};
			break;
			
		case 3:
			result = new int[]{-1,0};

		default:
			break;
		}
		
		return result;
	}
	
	
	
	/**
	 * Action class for movement actions in grid world.
	 * @author James MacGlashan
	 *
	 */
	public class MovementAction extends Action{

		/**
		 * Probabilities of the actual direction the agent will go
		 */
		protected double [] directionProbs;
		
		/**
		 * Random object for sampling distribution
		 */
		protected Random rand;
		
		
		/**
		 * Initializes for the given name, domain and actually direction probabilities the agent will go
		 * @param name name of the action
		 * @param domain the domain of the action
		 * @param directions the probability for each direction (index 0,1,2,3 corresponds to north,south,east,west, respectively).
		 */
		public MovementAction(String name, Domain domain, double [] directions){
			super(name, domain, "");
			this.directionProbs = directions;
			this.rand = RandomFactory.getMapped(0);
		}
		
		@Override
		protected State performActionHelper(State st, String[] params) {
			
			double roll = rand.nextDouble();
			double curSum = 0.;
			int dir = 0;
			for(int i = 0; i < directionProbs.length; i++){
				curSum += directionProbs[i];
				if(roll < curSum){
					dir = i;
					break;
				}
			}
			
			int [] dcomps = GridWorldDomain.this.movementDirectionFromIndex(dir);
			GridWorldDomain.this.move(st, dcomps[0], dcomps[1]);
			
			return st;
		}
		
		@Override
		public List<TransitionProbability> getTransitions(State st, String [] params){
			
			List <TransitionProbability> transitions = new ArrayList<TransitionProbability>();
			for(int i = 0; i < directionProbs.length; i++){
				double p = directionProbs[i];
				if(p == 0.){
					continue; //cannot transition in this direction
				}
				State ns = st.copy();
				int [] dcomps = GridWorldDomain.this.movementDirectionFromIndex(i);
				GridWorldDomain.this.move(ns, dcomps[0], dcomps[1]);
				
				//make sure this direction doesn't actually stay in the same place and replicate another no-op
				boolean isNew = true;
				for(TransitionProbability tp : transitions){
					if(tp.s.equals(ns)){
						isNew = false;
						tp.p += p;
						break;
					}
				}
				
				if(isNew){
					TransitionProbability tp = new TransitionProbability(ns, p);
					transitions.add(tp);
				}
			
				
			}
			
			
			return transitions;
		}
		
		
		
	}
	
	
	
	/**
	 * Propositional function for determining if the agent is at the same position as a given location object
	 * @author James MacGlashan
	 *
	 */
	public class AtLocationPF extends PropositionalFunction{

		
		/**
		 * Initializes with given name domain and parameter object class types
		 * @param name name of function
		 * @param domain the domain of the function
		 * @param parameterClasses the object class types for the parameters
		 */
		public AtLocationPF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			
			ObjectInstance agent = st.getObject(params[0]);
			ObjectInstance location = st.getObject(params[1]);
			
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			
			int lx = location.getDiscValForAttribute(ATTX);
			int ly = location.getDiscValForAttribute(ATTY);
			
			if(ax == lx && ay == ly){
				return true;
			}
			
			return false;
		}
		

	}
	
	
	
	/**
	 * Propositional function for indicating if a wall is in a given position relative to the agent position
	 * @author James MacGlashan
	 *
	 */
	public class WallToPF extends PropositionalFunction{

		/**
		 * The relative x distance from the agent of the cell to check
		 */
		protected int xdelta;
		
		/**
		 * The relative y distance from the agent of the cell to check
		 */
		protected int ydelta;
		
		
		
		/**
		 * Initializes the function.
		 * @param name the name of the function
		 * @param domain the domain of the function
		 * @param parameterClasses the object class parameter types
		 * @param direction the unit distance direction from the agent to check for a wall (0,1,2,3 corresponds to north,south,east,west).
		 */
		public WallToPF(String name, Domain domain, String[] parameterClasses, int direction) {
			super(name, domain, parameterClasses);
			int [] dcomps = GridWorldDomain.this.movementDirectionFromIndex(direction);
			xdelta = dcomps[0];
			ydelta = dcomps[1];
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			
			ObjectInstance agent = st.getObject(params[0]);
			
			int cx = agent.getDiscValForAttribute(ATTX) + xdelta;
			int cy = agent.getDiscValForAttribute(ATTY) + ydelta;
			
			if(cx < 0 || cx >= GridWorldDomain.this.width || cy < 0 || cy >= GridWorldDomain.this.height || GridWorldDomain.this.map[cx][cy] == 1){
				return true;
			}
			
			return false;
		}
		
		
		
	}
	
	

}
