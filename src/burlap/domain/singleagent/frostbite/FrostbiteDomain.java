package burlap.domain.singleagent.frostbite;

import burlap.debugtools.RandomFactory;
import burlap.domain.singleagent.frostbite.state.FrostbiteAgent;
import burlap.domain.singleagent.frostbite.state.FrostbiteIgloo;
import burlap.domain.singleagent.frostbite.state.FrostbitePlatform;
import burlap.domain.singleagent.frostbite.state.FrostbiteState;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TransitionProbability;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A simplified version of the classic Atari Frostbite domain. In this game, the agent must jump between different
 * ice blocks. Each time the agent jumps on an ice block, it adds a layer to an igloo that is being built and "activates"
 * all ice blocks on the same row. Jumping on an activated ice block does not ad a layer to the igloo. Once all rows
 * of ice blocks are activated, they reset and can be activated by jumping on them again. Once the igloo is built,
 * the agent can go to it to win the game. If the agent jumps or walks into the water, the game is over.
 * <p>
 * <p>
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
	public static final String VAR_X = "x";
	/**
	 * Constant for the name of the y position attribute.
	 */
	public static final String VAR_Y = "y";

	/**
	 * Attribute name for height
	 */
	public static final String VAR_HEIGHT = "height";

	/**
	 * Constant for the name of the size of a frozen platform
	 */
	public static final String VAR_SIZE = "size";
	/**
	 * Constant for the name of the building step of the igloo
	 */
	public static final String VAR_BUILDING = "building";
	/**
	 * Constant for the name of the activated status of a platform
	 */
	public static final String VAR_ACTIVATED = "activated";

	/**
	 * Constant for the name of the agent OO-MDP class
	 */
	public static final String CLASS_AGENT = "agent";
	/**
	 * Constant for the name of the igloo OO-MDP class
	 */
	public static final String CLASS_IGLOO = "igloo";
	/**
	 * Constant for the name of the obstacle OO-MDP class
	 */
	public static final String CLASS_PLATFORM = "platform";

	/**
	 * Constant for the name of the north action
	 */
	public static final String ACTION_NORTH = "north";
	/**
	 * Constant for the name of the south action
	 */
	public static final String ACTION_SOUTH = "south";
	/**
	 * Constant for the name of the east action
	 */
	public static final String ACTION_EAST = "east";
	/**
	 * Constant for the name of the west action
	 */
	public static final String ACTION_WEST = "west";
	/**
	 * Constant for the name of the west action
	 */
	public static final String ACTION_IDLE = "idle";

	/**
	 * Constant for the name of the propositional function "agent is on platform"
	 */
	public static final String PF_ON_PLATFORM = "pfOnPlatform";
	/**
	 * Constant for the name of the propositional function "platform is active"
	 */
	public static final String PF_PLATFORM_ACTIVE = "pfPlatformActive";
	/**
	 * Constant for the name of the propositional function "agent is on ice"
	 */
	public static final String PF_ON_ICE = "pfOnIce";
	/**
	 * Constant for the name of the propositional function "igloo is built"
	 */
	public static final String PF_IGLOO_BUILT = "pfIglooBuilt";
	/**
	 * Constant for the name of the propositional function "agent is in water"
	 */
	public static final String PF_IN_WATER = "pfInWater";


	/**
	 * Constant to adjust the scale of the game
	 */
	private static final int SCALE = 5;


	/**
	 * Game parameters
	 */

	public static final int gameHeight = 130 * SCALE;
	public static final int gameIceHeight = gameHeight / 4;
	public static final int gameWidth = 160 * SCALE;
	public static final int jumpSize = 22 * SCALE;
	public static final int stepSize = 2 * SCALE;
	public static final int jumpSpeed = jumpSize / 4;
	public static final int platformSpeed = 1 * SCALE;

	public static int numberPlatformRow = 4;
	public static int numberPlatformCol = 4;
	public static int agentSize = 8 * SCALE;
	public static int platformSize = 15 * SCALE;
	public static int spaceBetweenPlatforms = 26 * SCALE;
	public static boolean visualizingDomain = false;
	public int buildingStepsToWin = 16;



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
		State s = new FrostbiteState();

		Visualizer vis = FrostbiteVisualizer.getVisualizer(fd);
		VisualExplorer exp = new VisualExplorer(d, vis, s);

		exp.addKeyAction("a", ACTION_WEST);
		exp.addKeyAction("d", ACTION_EAST);
		exp.addKeyAction("w", ACTION_NORTH);
		exp.addKeyAction("s", ACTION_SOUTH);
		exp.addKeyAction("x", ACTION_IDLE);

		exp.initGUI();
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

		OOSADomain domain = new OOSADomain();

		domain.addStateClass(CLASS_AGENT, FrostbiteAgent.class)
				.addStateClass(CLASS_IGLOO, FrostbiteIgloo.class)
				.addStateClass(CLASS_PLATFORM, FrostbitePlatform.class);

		//add actions
		new MovementAction(ACTION_SOUTH, domain, this.transitionDynamics[0]);
		new MovementAction(ACTION_NORTH, domain, this.transitionDynamics[1]);
		new MovementAction(ACTION_EAST, domain, this.transitionDynamics[2]);
		new MovementAction(ACTION_WEST, domain, this.transitionDynamics[3]);
		new ActionIdle(ACTION_IDLE, domain);


		//add pfs
		new PlatformActivePF(PF_PLATFORM_ACTIVE, domain);
		new OnPlatformPF(PF_ON_PLATFORM, domain);
		new InWaterPF(PF_IN_WATER, domain);
		new OnIcePF(PF_ON_ICE, domain);
		new IglooBuiltPF(PF_IGLOO_BUILT, domain);

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
				break;

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
	protected void move(FrostbiteState s, int xd, int yd) {

		FrostbiteAgent agent = s.touchAgent();

		int ax = agent.x;
		int ay = agent.y;
		int leftToJump = agent.height;

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

		agent.x = nx;
		agent.y = ny;
		agent.height = leftToJump;


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
	private void update(FrostbiteState s, int leftToJump, boolean justLanded, int platformSpeedOnAgent) {
		// Move the platforms; first copy all of them
		List<FrostbitePlatform> platforms = s.deepTouchPlatforms();

		for (int i = 0; i < platforms.size(); i++) {
			int directionL = ((i / numberPlatformCol) % 2 == 0) ? 1 : -1;
			int x = (Integer)platforms.get(i).get(VAR_X) + directionL * platformSpeed;
			if (x < 0)
				x += gameWidth;
			platforms.get(i).x = x % gameWidth;
		}

		// Player landed
		if (leftToJump == 0) {
			// Just landed: Potentially activate some platforms
			if (justLanded)
				activatePlatforms(s);

		}

		// If all platforms are active, deactivate them
		for (int i = 0; i < platforms.size(); i++)
			if (!platforms.get(i).activated)
				return;
		for (int i = 0; i < platforms.size(); i++)
			platforms.get(i).activated = false;
	}

	/**
	 * Activates platforms on which the user has landed (and the rest of the row). Assumes platforms have
	 * already been deep copied
	 * @param s State on which to activate the platforms
	 */
	private void activatePlatforms(FrostbiteState s) {
		FrostbiteAgent agent = s.agent;
		int ax = agent.x + agentSize / 2;
		int ay = agent.y + agentSize / 2;
		List<FrostbitePlatform> platforms = s.platforms;
		for (int i = 0; i < platforms.size(); i++) {
			FrostbitePlatform platform = platforms.get(i);
			if (!platform.activated)

				if (pointInPlatform(ax, ay, platform.x, platform.y, platform.size)) {
					for (int j = numberPlatformCol * (i / numberPlatformCol); j < numberPlatformCol * (1 + i / numberPlatformCol); j++)
						platforms.get(j).activated = true;
					FrostbiteIgloo igloo = s.touchIgloo();
					igloo.height = igloo.height + 1;
					break;
				}
		}
	}

	/**
	 * Checks whether the player is on a platform and return its platform speed if so.
	 * @param s State on which the check is made
	 * @return 0 if the player is not on a platform. Otherwise returns the platform speed of the platform the player is on.
	 */
	private int getLandedPlatformSpeed(FrostbiteState s) {
		FrostbiteAgent agent = s.agent;
		int ax = agent.x + agentSize / 2;
		int ay = agent.y + agentSize / 2;
		List<FrostbitePlatform> platforms = s.platforms;
		for (int i = 0; i < platforms.size(); i++) {
			FrostbitePlatform platform = platforms.get(i);
			if (pointInPlatform(ax, ay, platform.x, platform.y, platform.size))
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
	public class MovementAction extends SimpleAction implements FullActionModel{

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
			super(name, domain);
			this.directionProbs = directions.clone();
			this.rand = RandomFactory.getMapped(0);
		}

		@Override
		protected State performActionHelper(State st, GroundedAction groundedAction) {

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
			FrostbiteDomain.this.move((FrostbiteState)st, dcomps[0], dcomps[1]);

			return st;
		}

		@Override
		public List<TransitionProbability> getTransitions(State st, GroundedAction groundedAction) {

			List<TransitionProbability> transitions = new ArrayList<TransitionProbability>();
			for (int i = 0; i < directionProbs.length; i++) {
				double p = directionProbs[i];
				if (p == 0.) {
					continue; //cannot transition in this direction
				}
				State ns = st.copy();
				int[] dcomps = FrostbiteDomain.this.movementDirectionFromIndex(i);
				FrostbiteDomain.this.move((FrostbiteState)ns, dcomps[0], dcomps[1]);

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

	public class ActionIdle extends SimpleAction.SimpleDeterministicAction implements FullActionModel{

		/**
		 * Initializes the idle action.
		 *
		 * @param name   the name of the action
		 * @param domain the domain of the action.
		 */
		public ActionIdle(String name, Domain domain) {
			super(name, domain);
		}


		@Override
		protected State performActionHelper(State st, GroundedAction groundedAction) {
			FrostbiteDomain.this.move((FrostbiteState)st, 0, 0);
			return st;
		}

	}

	public class OnPlatformPF extends PropositionalFunction {
		/**
		 * Initializes to be evaluated on an agent object and platform object.
		 *
		 * @param name   the name of the propositional function
		 * @param domain the domain of the propositional function
		 */
		public OnPlatformPF(String name, OODomain domain) {
			super(name, domain, new String[]{CLASS_AGENT, CLASS_PLATFORM});
		}


		@Override
		public boolean isTrue(OOState st, String... params) {

			FrostbiteAgent agent = (FrostbiteAgent)st.object(params[0]);
			FrostbitePlatform platform = (FrostbitePlatform)st.object(params[1]);

			int x = platform.x;
			int y = platform.y;
			int s = platform.size;

			int ax = agent.x + agentSize / 2;
			int ay = agent.y + agentSize / 2;
			int ah = agent.height;

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
		public PlatformActivePF(String name, OODomain domain) {
			super(name, domain, new String[]{CLASS_PLATFORM});
		}

		@Override
		public boolean isTrue(OOState st, String... params) {
			FrostbitePlatform platform = (FrostbitePlatform)st.object(params[0]);
			return platform.activated;
		}
	}

	public class InWaterPF extends PropositionalFunction {
		/**
		 * Initializes to be evaluated on an agent object.
		 *
		 * @param name   the name of the propositional function
		 * @param domain the domain of the propositional function
		 */
		public InWaterPF(String name, OODomain domain) {
			super(name, domain, new String[]{CLASS_AGENT});
		}


		@Override
		public boolean isTrue(OOState st, String... params) {

			FrostbiteAgent agent = (FrostbiteAgent)st.object(params[0]);
			int ah = agent.height;

			if (ah != 0)
				return false;

			// Agent is on a platform
			if (getLandedPlatformSpeed((FrostbiteState)st) != 0)
				return false;

			int ay = agent.y + agentSize / 2;
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
		public OnIcePF(String name, OODomain domain) {
			super(name, domain, new String[]{CLASS_AGENT});
		}


		@Override
		public boolean isTrue(OOState st, String... params) {
			FrostbiteAgent agent = (FrostbiteAgent)st.object(params[0]);

			int ay = agent.x + agentSize / 2;
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
		public IglooBuiltPF(String name, OODomain domain) {
			super(name, domain, new String[]{CLASS_IGLOO});
		}

		@Override
		public boolean isTrue(OOState st, String... params) {
			FrostbiteIgloo igloo = (FrostbiteIgloo)st.object(params[0]);

			int building = igloo.height;
			return building >= buildingStepsToWin;
		}
	}

}
