package burlap.domain.singleagent.frostbite;

import burlap.domain.singleagent.frostbite.state.FrostbiteAgent;
import burlap.domain.singleagent.frostbite.state.FrostbiteIgloo;
import burlap.domain.singleagent.frostbite.state.FrostbitePlatform;
import burlap.domain.singleagent.frostbite.state.FrostbiteState;
import burlap.mdp.core.Action;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.List;

import static burlap.domain.singleagent.frostbite.FrostbiteDomain.*;

/**
 * @author James MacGlashan.
 */
public class FrostbiteModel implements FullStateModel {

	/**
	 * Constant to adjust the scale of the game
	 */
	protected int scale = 5;
	protected int numberPlatformCol = 4;

	protected int gameHeight = 130 * scale;
	protected int gameWidth = 160 * scale;
	protected int jumpSize = 22 * scale;
	protected int stepSize = 2 * scale;
	protected int jumpSpeed = jumpSize / 4;
	protected int platformSpeed = 1 * scale;
	protected int agentSize = 8 * scale;


	public FrostbiteModel(int scale) {
		this.scale = scale;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
		this.initConstants();
	}

	public int getNumberPlatformCol() {
		return numberPlatformCol;
	}

	public void setNumberPlatformCol(int numberPlatformCol) {
		this.numberPlatformCol = numberPlatformCol;
	}

	public void initConstants(){
		gameHeight = 130 * scale;
		gameWidth = 160 * scale;
		jumpSize = 22 * scale;
		stepSize = 2 * scale;
		jumpSpeed = jumpSize / 4;
		platformSpeed = 1 * scale;
		agentSize = 8 * scale;
	}

	@Override
	public List<StateTransitionProb> stateTransitions(State s, Action a) {
		return FullStateModel.Helper.deterministicTransition(this, s, a);
	}

	@Override
	public State sample(State s, Action a) {

		FrostbiteState fs = (FrostbiteState)s.copy();

		String aname = a.actionName();
		if(aname.equals(ACTION_EAST)){
			move(fs, 1, 0);
		}
		else if(aname.equals(ACTION_WEST)){
			move(fs, -1, 0);
		}
		else if(aname.equals(ACTION_NORTH)){
			move(fs, 0, -1);
		}
		else if(aname.equals(ACTION_SOUTH)){
			move(fs, 0, 1);
		}
		else if(aname.equals(ACTION_IDLE)){
			move(fs, 0, 0);
		}
		else{
			throw new RuntimeException("Unknown action " + a.toString());
		}

		return fs;
	}



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
		if (x + s > gameWidth && pointInPlatformHelper(px, py, x - gameWidth, y, s))
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

}
