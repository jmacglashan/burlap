package burlap.domain.singleagent.frostbite;

import burlap.debugtools.RandomFactory;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.*;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A simplified version of the classic Atari Frostbite domain. In this game, the agent must jump between different
 * ice blocks. Each time the agent jumps on an ice block, it adds a layer to an igloo that is being built and "activates"
 * all ice blocks on the same row. Jumping on an activated ice block does not ad a layer to the igloo. Once all rows
 * of ice blocks are activated, they reset and can be activated by jumping on them again. Once the igloo is built,
 * the agent can go to it to win the game. If the agent jumps or walks into the water, the game is over.
 * <br/>
 * <br/>
 * If you run the main method of this class, it will launch of a visual explorer that you can play. They keys
 * w,s,a,d,x correspond to the actions jump north, jump south, move west, move east, do nothing. If you win or lose
 * the visual explorer will automatically terminate. If you want it to keep running, you can set this class' public static
 * {@link #visualizingDomain} data member to false.
 *
 * @author Phillipe Morere
 */
public class FrostbiteDomain implements DomainGenerator{

	/**
	 * Constant for the name of the x position attribute.
	 */
	public static final String XATTNAME = "x";
	/**
	 * Constant for the name of the y position attribute.
	 */
	public static final String YATTNAME = "y";

	/**
	 * Attribute name for height
	 */
	public static final String HEIGHTATTNAME = "height";

	/**
	 * Constant for the name of the size of a frozen platform
	 */
	public static final String SIZEATTNAME = "size";
	/**
	 * Constant for the name of the building step of the igloo
	 */
	public static final String BUILDINGATTNAME = "building";
	/**
	 * Constant for the name of the activated status of a platform
	 */
	public static final String ACTIVATEDATTNAME = "activated";

	/**
	 * Constant for the name of the agent OO-MDP class
	 */
	public static final String AGENTCLASS = "agent";
	/**
	 * Constant for the name of the igloo OO-MDP class
	 */
	public static final String IGLOOCLASS = "igloo";
	/**
	 * Constant for the name of the obstacle OO-MDP class
	 */
	public static final String PLATFORMCLASS = "platform";

	/**
	 * Constant for the name of the north action
	 */
	public static final String ACTIONNORTH = "north";
	/**
	 * Constant for the name of the south action
	 */
	public static final String ACTIONSOUTH = "south";
	/**
	 * Constant for the name of the east action
	 */
	public static final String ACTIONEAST = "east";
	/**
	 * Constant for the name of the west action
	 */
	public static final String ACTIONWEST = "west";
	/**
	 * Constant for the name of the west action
	 */
	public static final String ACTIONIDLE = "idle";

	/**
	 * Constant for the name of the propositional function "agent is on platform"
	 */
	public static final String PFONPLATFORM = "pfOnPlatform";
	/**
	 * Constant for the name of the propositional function "platform is active"
	 */
	public static final String PFPLATFORMACTIVE = "pfPlatformActive";
	/**
	 * Constant for the name of the propositional function "agent is on ice"
	 */
	public static final String PFONICE = "pfOnIce";
	/**
	 * Constant for the name of the propositional function "igloo is built"
	 */
	public static final String PFIGLOOBUILT = "pfIglooBuilt";
	/**
	 * Constant for the name of the propositional function "agent is in water"
	 */
	public static final String PFINWATER = "pfInWater";
	/**
	 * Constant to adjust the scale of the game
	 */
	private static final int SCALE = 5;


	/**
	 * Game parameters
	 */
	protected static final int gameHeight = 130 * SCALE;
	protected static final int gameIceHeight = gameHeight / 4;
	protected static final int gameWidth = 160 * SCALE;
	private static final int jumpSize = 22 * SCALE;
	private static final int stepSize = 2 * SCALE;
	private static final int jumpSpeed = jumpSize / 4;
	private static final int platformSpeed = 1 * SCALE;
	private static int numberPlatformRow = 4;
	private static int numberPlatformCol = 4;
	private static int agentSize = 8 * SCALE;
	private static int platformSize = 15 * SCALE;
	private static int spaceBetweenPlatforms = 26 * SCALE;
	private static boolean visualizingDomain = false;
	protected int buildingStepsToWin = 16;

	/**
	 * Matrix specifying the transition dynamics in terms of movement directions. The first index
	 * indicates the action direction attempted (ordered north, south, east, west) the second index
	 * indicates the actual resulting direction the agent will go (assuming there is no wall in the way).
	 * The value is the probability of that outcome. The existence of walls does not affect the probability
	 * of the direction the agent will actually go, but if a wall is in the way, it will affect the outcome.
	 * For instance, if the agent selects north, but there is a 0.2 probability of actually going east and
	 * there is a wall to the east, then with 0.2 probability, the agent will stay in place.
	 */
	protected double[][] transitionDynamics;

	public FrostbiteDomain() {
		setDeterministicTransitionDynamics();
	}

	/**
	 * Main function to test the domain.
	 * Note: The termination conditions are not checked when testing the domain this way, which means it is
	 * impossible to win or die and might trigger bugs. To enable them, uncomment the code in the "update" function.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		FrostbiteDomain fd = new FrostbiteDomain();
		fd.visualizingDomain = true;
		Domain d = fd.generateDomain();
		State s = fd.getCleanState(d);

		Visualizer vis = FrostbiteVisualizer.getVisualizer(fd);
		VisualExplorer exp = new VisualExplorer(d, vis, s);

		exp.addKeyAction("a", ACTIONWEST);
		exp.addKeyAction("d", ACTIONEAST);
		exp.addKeyAction("w", ACTIONNORTH);
		exp.addKeyAction("s", ACTIONSOUTH);
		exp.addKeyAction("x", ACTIONIDLE);

		exp.initGUI();
	}

	/**
	 * Sets the agent s position, with a height of 0 (on the ground)
	 *
	 * @param s the state in which to set the agent
	 * @param x the x position of the agent
	 * @param y the y position of the agent
	 */
	public static void setAgent(State s, int x, int y) {
		ObjectInstance agent = s.getObjectsOfClass(AGENTCLASS).get(0);

		agent.setValue(XATTNAME, x);
		agent.setValue(YATTNAME, y);
		agent.setValue(HEIGHTATTNAME, 0);
	}

	/**
	 * Sets the agent s position and height
	 *
	 * @param s the state in which to set the agent
	 * @param x the x position of the agent
	 * @param y the y position of the agent
	 * @param h the height of the agent (0 is ground)
	 */
	public static void setAgent(State s, int x, int y, int h) {
		ObjectInstance agent = s.getObjectsOfClass(AGENTCLASS).get(0);

		agent.setValue(XATTNAME, x);
		agent.setValue(YATTNAME, y);
		agent.setValue(HEIGHTATTNAME, h);
	}

	/**
	 * Sets the igloo building status
	 *
	 * @param s        the state in which to set the agent
	 * @param building igloo building status
	 */
	public static void setIgloo(State s, int building) {
		ObjectInstance agent = s.getObjectsOfClass(IGLOOCLASS).get(0);

		agent.setValue(BUILDINGATTNAME, building);
	}

	/**
	 * Sets a platform position, size and status
	 *
	 * @param s               the state in which the platform should be set
	 * @param i               specifies the ith platform object to be set to these values
	 * @param x               the x coordinate of the top left corner
	 * @param y               the y coordinate of the top left corner
	 * @param ss              the platform size
	 * @param activatedStatus the platform status
	 */
	public static void setPlatform(State s, int i, int x, int y, int ss, boolean activatedStatus) {
		ObjectInstance platform = s.getObjectsOfClass(PLATFORMCLASS).get(i);

		platform.setValue(XATTNAME, x);
		platform.setValue(YATTNAME, y);
		platform.setValue(SIZEATTNAME, ss);
		platform.setValue(ACTIVATEDATTNAME, activatedStatus);
	}

	/**
	 * Initializes a full row of platforms.
	 *
	 * @param d   domain
	 * @param s   the state to initialize the platforms in
	 * @param row the row id (starts at 0). Defines the direction platforms are going to move.
	 */
	private static void setPlatformRow(Domain d, State s, int row) {
		for (int i = 0; i < numberPlatformCol; i++) {
			ObjectInstance platform = new ObjectInstance(d.getObjectClass(PLATFORMCLASS), PLATFORMCLASS + (i + row * numberPlatformCol));
			s.addObject(platform);

			platform.setValue(XATTNAME, spaceBetweenPlatforms * i + ((row % 2 == 0) ? 0 : gameWidth / 3));
			platform.setValue(YATTNAME, gameIceHeight + jumpSize / 2 - platformSize / 2 + agentSize / 2 + jumpSize * row);
			platform.setValue(SIZEATTNAME, platformSize);
			platform.setValue(ACTIVATEDATTNAME, false);
		}
	}

	/**
	 * Creates a state with one agent, one igloo, and 4 rows of 4 platforms. The object values are uninitialised and will
	 * have to be set manually or with methods like {@link #setAgent(burlap.oomdp.core.State, int, int)}.
	 *
	 * @param domain the domain of the state to generate
	 * @return a state object
	 */
	public static State getCleanState(Domain domain) {

		State s = new State();

		ObjectInstance agent = new ObjectInstance(domain.getObjectClass(AGENTCLASS), AGENTCLASS + "0");
		s.addObject(agent);
		ObjectInstance igloo = new ObjectInstance(domain.getObjectClass(IGLOOCLASS), IGLOOCLASS + "0");
		s.addObject(igloo);

		for (int i = 0; i < numberPlatformRow; i++)
			setPlatformRow(domain, s, i);

		setAgent(s, platformSize / 2 + agentSize / 2, gameIceHeight - jumpSize / 2);
		return s;
	}

	/**
	 * Returns the agent size
	 *
	 * @return the agent size
	 */
	public int getAgentSize() {
		return agentSize;
	}

	/**
	 * Will set the domain to use deterministic action transitions.
	 */
	public void setDeterministicTransitionDynamics() {
		int na = 4;
		transitionDynamics = new double[na][na];
		for (int i = 0; i < na; i++) {
			for (int j = 0; j < na; j++) {
				if (i != j) {
					transitionDynamics[i][j] = 0.;
				} else {
					transitionDynamics[i][j] = 1.;
				}
			}
		}
	}

	/**
	 * Creates a new frostbite domain.
	 *
	 * @return the generated domain object
	 */
	@Override
	public Domain generateDomain() {

		Domain domain = new SADomain();

		//create attributes
		Attribute xatt = new Attribute(domain, XATTNAME, Attribute.AttributeType.INT);
		xatt.setLims(0, gameWidth);

		Attribute yatt = new Attribute(domain, YATTNAME, Attribute.AttributeType.INT);
		yatt.setLims(0, gameHeight);

		Attribute hatt = new Attribute(domain, HEIGHTATTNAME, Attribute.AttributeType.INT);
		hatt.setLims(-83, 83);

		Attribute satt = new Attribute(domain, SIZEATTNAME, Attribute.AttributeType.INT);
		satt.setLims(0, gameWidth);

		Attribute batt = new Attribute(domain, BUILDINGATTNAME, Attribute.AttributeType.INT);
		satt.setLims(0, 256); // It's an Atari game, it should crash at some point!

		Attribute aatt = new Attribute(domain, ACTIVATEDATTNAME, Attribute.AttributeType.BOOLEAN);

		//create classes
		ObjectClass agentclass = new ObjectClass(domain, AGENTCLASS);
		agentclass.addAttribute(xatt);
		agentclass.addAttribute(yatt);
		agentclass.addAttribute(hatt);

		ObjectClass platformclass = new ObjectClass(domain, PLATFORMCLASS);
		platformclass.addAttribute(xatt);
		platformclass.addAttribute(yatt);
		platformclass.addAttribute(satt);
		platformclass.addAttribute(aatt);

		ObjectClass iglooclass = new ObjectClass(domain, IGLOOCLASS);
		iglooclass.addAttribute(batt);

		//add actions
		new MovementAction(ACTIONSOUTH, domain, this.transitionDynamics[0]);
		new MovementAction(ACTIONNORTH, domain, this.transitionDynamics[1]);
		new MovementAction(ACTIONEAST, domain, this.transitionDynamics[2]);
		new MovementAction(ACTIONWEST, domain, this.transitionDynamics[3]);
		new ActionIdle(ACTIONIDLE, domain);


		//add pfs
		new PlatformActivePF(PFPLATFORMACTIVE, domain);
		new OnPlatformPF(PFONPLATFORM, domain);
		new InWaterPF(PFINWATER, domain);
		new OnIcePF(PFONICE, domain);
		new IglooBuiltPF(PFIGLOOBUILT, domain);

		return domain;
	}

	/**
	 * Returns the change in x and y position for a given direction number.
	 *
	 * @param i the direction number (0,1,2,3 indicates north,south,east,west, respectively)
	 * @return the change in direction for x and y; the first index of the returned double is change in x, the second index is change in y.
	 */
	protected int[] movementDirectionFromIndex(int i) {

		int[] result = null;

		switch (i) {
			case 0:
				result = new int[]{0, 1};
				break;

			case 1:
				result = new int[]{0, -1};
				break;

			case 2:
				result = new int[]{1, 0};
				break;

			case 3:
				result = new int[]{-1, 0};

			default:
				break;
		}

		return result;
	}

	int moveStep = 0;

	/**
	 * Attempts to move the agent into the given position, taking into account platforms and screen borders
	 *
	 * @param s  the current state
	 * @param xd the attempted X position increment of the agent
	 * @param yd the attempted Y position increment of the agent
	 */
	protected void move(State s, int xd, int yd) {

		ObjectInstance agent = s.getObjectsOfClass(AGENTCLASS).get(0);
		int ax = agent.getIntValForAttribute(XATTNAME);
		int ay = agent.getIntValForAttribute(YATTNAME);
		int leftToJump = agent.getIntValForAttribute(HEIGHTATTNAME);

		int nx = ax + xd * stepSize;
		int ny = ay;

		boolean inAir = leftToJump != 0;
		int platformSpeedOnAgent = this.getLandedPlatformSpeed(s);

		// Is a jump triggered while player is on the ground?
		if (leftToJump == 0 && yd != 0) {
			// Player can only jump when on a platform (except last line), or when hitting down on the top part
			if ((platformSpeedOnAgent != 0 && ay + yd * jumpSize < gameHeight - agentSize) || (platformSpeedOnAgent == 0 && yd > 0)) {
				leftToJump = yd * jumpSize;
				platformSpeedOnAgent = 0;
			}
		}

		// If the player is in the air, move it.
		if (leftToJump < 0) {
			int jumpIncrement = Math.max(-jumpSpeed, leftToJump);
			leftToJump -= jumpIncrement;
			ny += jumpIncrement;
		} else if (leftToJump > 0) {
			int jumpIncrement = Math.min(jumpSpeed, leftToJump);
			leftToJump -= jumpIncrement;
			ny += jumpIncrement;
		}

		// If agent is on platform make it move with the platform
		if (leftToJump == 0)
			nx += platformSpeedOnAgent;

		// If agent goes out of the screen, stop it.
		if (nx < 0 || nx >= gameWidth - agentSize || ny < 0 || ny >= gameHeight - agentSize) {
			nx = ax;
			ny = ay;
		}

		agent.setValue(XATTNAME, nx);
		agent.setValue(YATTNAME, ny);
		agent.setValue(HEIGHTATTNAME, leftToJump);

		boolean justLanded = false;
		if(inAir && leftToJump == 0){
			justLanded = true;
		}

		update(s, leftToJump, justLanded, platformSpeedOnAgent);

		moveStep++;
	}

	/**
	 * Executes update step on state. Handles everything that is not player specific.
	 * @param s the state to apply the update step on
	 */
	private void update(State s, int leftToJump, boolean justLanded, int platformSpeedOnAgent) {
		// Move the platforms
		List<ObjectInstance> platforms = s.getObjectsOfClass(PLATFORMCLASS);
		for (int i = 0; i < platforms.size(); i++) {
			int directionL = ((i / numberPlatformCol) % 2 == 0) ? 1 : -1;
			int x = platforms.get(i).getIntValForAttribute(XATTNAME) + directionL * platformSpeed;
			if (x < 0)
				x += gameWidth;
			platforms.get(i).setValue(XATTNAME, x % gameWidth);
		}

		// Player landed
		if (leftToJump == 0) {
			// Just landed: Potentially activate some platforms
			if (justLanded)
				activatePlatforms(s);


			// Termination conditions (only used to test the domain)
			if (visualizingDomain) {
				ObjectInstance agent = s.getObjectsOfClass(AGENTCLASS).get(0);
				int ay = agent.getIntValForAttribute(YATTNAME) + agentSize / 2;
				ObjectInstance igloo = s.getObjectsOfClass(IGLOOCLASS).get(0);
				int building = igloo.getIntValForAttribute(BUILDINGATTNAME);
				if (platformSpeedOnAgent == 0 && ay > gameIceHeight) {
					System.out.println("Game over.");
					System.exit(0);
				} else if (ay <= gameIceHeight && building >= buildingStepsToWin) {
					System.out.println("You won.");
					System.exit(0);
				}
			}
		}

		// If all platforms are active, deactivate them
		for (int i = 0; i < platforms.size(); i++)
			if (!platforms.get(i).getBooleanValue(ACTIVATEDATTNAME))
				return;
		for (int i = 0; i < platforms.size(); i++)
			platforms.get(i).setValue(ACTIVATEDATTNAME, false);
	}

	/**
	 * Activates platforms on which the user has landed (and the rest of the row).
	 * @param s State on which to activate the platforms
	 */
	private void activatePlatforms(State s) {
		ObjectInstance agent = s.getObjectsOfClass(AGENTCLASS).get(0);
		int ax = agent.getIntValForAttribute(XATTNAME) + agentSize / 2;
		int ay = agent.getIntValForAttribute(YATTNAME) + agentSize / 2;
		List<ObjectInstance> platforms = s.getObjectsOfClass(PLATFORMCLASS);
		for (int i = 0; i < platforms.size(); i++) {
			ObjectInstance platform = platforms.get(i);
			if (!platform.getBooleanValue(ACTIVATEDATTNAME))
				if (pointInPlatform(ax, ay, platform.getIntValForAttribute(XATTNAME), platform.getIntValForAttribute(YATTNAME), platform.getIntValForAttribute(SIZEATTNAME))) {
					for (int j = numberPlatformCol * (i / numberPlatformCol); j < numberPlatformCol * (1 + i / numberPlatformCol); j++)
						platforms.get(j).setValue(ACTIVATEDATTNAME, true);
					ObjectInstance igloo = s.getFirstObjectOfClass(IGLOOCLASS);
					igloo.setValue(BUILDINGATTNAME, igloo.getIntValForAttribute(BUILDINGATTNAME) + 1);
					break;
				}
		}
	}

	/**
	 * Checks whether the player is on a platform and return its platform speed if so.
	 * @param s State on which the check is made
	 * @return 0 if the player is not on a platform. Otherwise returns the platform speed of the platform the player is on.
	 */
	private int getLandedPlatformSpeed(State s) {
		ObjectInstance agent = s.getObjectsOfClass(AGENTCLASS).get(0);
		int ax = agent.getIntValForAttribute(XATTNAME) + agentSize / 2;
		int ay = agent.getIntValForAttribute(YATTNAME) + agentSize / 2;
		List<ObjectInstance> platforms = s.getObjectsOfClass(PLATFORMCLASS);
		for (int i = 0; i < platforms.size(); i++) {
			ObjectInstance platform = platforms.get(i);
			if (pointInPlatform(ax, ay, platform.getIntValForAttribute(XATTNAME), platform.getIntValForAttribute(YATTNAME), platform.getIntValForAttribute(SIZEATTNAME)))
				return ((i / numberPlatformCol) % 2 == 0) ? platformSpeed : -platformSpeed;
		}
		return 0;
	}

	/**
	 * Collision check between a point (player center) and a platform, including wrapping around edges
	 * @param px point X coordinate
	 * @param py point Y coordinate
	 * @param x platform top left corner X coordinate
	 * @param y platform top left corner Y coordinate
	 * @param s platform size
	 * @return true if the point is in the platform, false otherwise
	 */
	private boolean pointInPlatform(int px, int py, int x, int y, int s) {
		if (pointInPlatformHelper(px, py, x, y, s))
			return true;
		if (x + s > FrostbiteDomain.gameWidth && pointInPlatformHelper(px, py, x - gameWidth, y, s))
			return true;
		else if (x < 0 && pointInPlatformHelper(px, py, x + gameWidth, y, s))
			return true;
		return false;
	}

	/**
	 * Collision check between a point (player center) and a platform.
	 * @param px point X coordinate
	 * @param py point Y coordinate
	 * @param x platform top left corner X coordinate
	 * @param y platform top left corner Y coordinate
	 * @param s platform size
	 * @return true if the point is in the platform, false otherwise
	 */
	private boolean pointInPlatformHelper(int px, int py, int x, int y, int s) {
		return px > x && px < x + s && py > y && py < y + s;
	}

	/**
	 * An action class for moving the agent.
	 */
	public class MovementAction extends Action {

		/**
		 * Probabilities of the actual direction the agent will go
		 */
		protected double[] directionProbs;

		/**
		 * Random object for sampling distribution
		 */
		protected Random rand;


		/**
		 * Initializes for the given name, domain and actually direction probabilities the agent will go
		 *
		 * @param name       name of the action
		 * @param domain     the domain of the action
		 * @param directions the probability for each direction (index 0,1,2,3 corresponds to north,south,east,west, respectively).
		 */
		public MovementAction(String name, Domain domain, double[] directions) {
			super(name, domain, "");
			this.directionProbs = directions.clone();
			this.rand = RandomFactory.getMapped(0);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {

			double roll = rand.nextDouble();
			double curSum = 0.;
			int dir = 0;
			for (int i = 0; i < directionProbs.length; i++) {
				curSum += directionProbs[i];
				if (roll < curSum) {
					dir = i;
					break;
				}
			}

			int[] dcomps = FrostbiteDomain.this.movementDirectionFromIndex(dir);
			FrostbiteDomain.this.move(st, dcomps[0], dcomps[1]);

			return st;
		}

		@Override
		public List<TransitionProbability> getTransitions(State st, String[] params) {

			List<TransitionProbability> transitions = new ArrayList<TransitionProbability>();
			for (int i = 0; i < directionProbs.length; i++) {
				double p = directionProbs[i];
				if (p == 0.) {
					continue; //cannot transition in this direction
				}
				State ns = st.copy();
				int[] dcomps = FrostbiteDomain.this.movementDirectionFromIndex(i);
				FrostbiteDomain.this.move(ns, dcomps[0], dcomps[1]);

				//make sure this direction doesn't actually stay in the same place and replicate another no-op
				boolean isNew = true;
				for (TransitionProbability tp : transitions) {
					if (tp.s.equals(ns)) {
						isNew = false;
						tp.p += p;
						break;
					}
				}

				if (isNew) {
					TransitionProbability tp = new TransitionProbability(ns, p);
					transitions.add(tp);
				}


			}


			return transitions;
		}
	}

	public class ActionIdle extends Action {

		/**
		 * Initializes the idle action.
		 *
		 * @param name   the name of the action
		 * @param domain the domain of the action.
		 */
		public ActionIdle(String name, Domain domain) {
			super(name, domain, "");
		}


		@Override
		protected State performActionHelper(State st, String[] params) {
			FrostbiteDomain.this.move(st, 0, 0);
			return st;
		}

		@Override
		public List<TransitionProbability> getTransitions(State s, String[] params) {
			return this.deterministicTransition(s, params);
		}
	}

	public class OnPlatformPF extends PropositionalFunction {
		/**
		 * Initializes to be evaluated on an agent object and platform object.
		 *
		 * @param name   the name of the propositional function
		 * @param domain the domain of the propositional function
		 */
		public OnPlatformPF(String name, Domain domain) {
			super(name, domain, new String[]{AGENTCLASS, PLATFORMCLASS});
		}


		@Override
		public boolean isTrue(State st, String[] params) {

			ObjectInstance agent = st.getObject(params[0]);
			ObjectInstance platform = st.getObject(params[1]);

			int x = platform.getIntValForAttribute(XATTNAME);
			int y = platform.getIntValForAttribute(YATTNAME);
			int s = platform.getIntValForAttribute(SIZEATTNAME);

			int ax = agent.getIntValForAttribute(XATTNAME) + agentSize / 2;
			int ay = agent.getIntValForAttribute(YATTNAME) + agentSize / 2;
			int ah = agent.getIntValForAttribute(HEIGHTATTNAME);

			if(ah != 0){
				return false;
			}

			return pointInPlatform(ax, ay, x, y, s);
		}
	}

	public class PlatformActivePF extends PropositionalFunction {
		/**
		 * Initializes to be evaluated on an agent object and platform object.
		 *
		 * @param name   the name of the propositional function
		 * @param domain the domain of the propositional function
		 */
		public PlatformActivePF(String name, Domain domain) {
			super(name, domain, new String[]{PLATFORMCLASS});
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			ObjectInstance platform = st.getObject(params[0]);
			return platform.getBooleanValue(ACTIVATEDATTNAME);
		}
	}

	public class InWaterPF extends PropositionalFunction {
		/**
		 * Initializes to be evaluated on an agent object.
		 *
		 * @param name   the name of the propositional function
		 * @param domain the domain of the propositional function
		 */
		public InWaterPF(String name, Domain domain) {
			super(name, domain, new String[]{AGENTCLASS});
		}


		@Override
		public boolean isTrue(State st, String[] params) {

			ObjectInstance agent = st.getObject(params[0]);
			int ah = agent.getIntValForAttribute(HEIGHTATTNAME);

			if (ah != 0)
				return false;

			// Agent is on a platform
			if (getLandedPlatformSpeed(st) != 0)
				return false;

			int ay = agent.getIntValForAttribute(YATTNAME) + agentSize / 2;
			return ay >= gameIceHeight;
		}
	}

	public class OnIcePF extends PropositionalFunction {
		/**
		 * Initializes to be evaluated on an agent object.
		 *
		 * @param name   the name of the propositional function
		 * @param domain the domain of the propositional function
		 */
		public OnIcePF(String name, Domain domain) {
			super(name, domain, new String[]{AGENTCLASS});
		}


		@Override
		public boolean isTrue(State st, String[] params) {
			ObjectInstance agent = st.getObject(params[0]);

			int ay = agent.getIntValForAttribute(YATTNAME) + agentSize / 2;
			return ay < gameIceHeight;
		}
	}

	public class IglooBuiltPF extends PropositionalFunction {
		/**
		 * Initializes to be evaluated on an agent object.
		 *
		 * @param name   the name of the propositional function
		 * @param domain the domain of the propositional function
		 */
		public IglooBuiltPF(String name, Domain domain) {
			super(name, domain, new String[]{IGLOOCLASS});
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			ObjectInstance igloo = st.getObject(params[0]);

			int building = igloo.getIntValForAttribute(BUILDINGATTNAME);
			return building >= buildingStepsToWin;
		}
	}

}
