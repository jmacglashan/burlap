package burlap.domain.singleagent.minecraft;

import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.explorer.TerminalExplorer;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.HashMap;

	
public class MinecraftDomain implements DomainGenerator{

	// ----- ATTRIBUTES -----
	public static final String					ATTX = "x";
	public static final String					ATTY = "y";
	public static final String					ATTZ = "z";
	public static final String 					ATTDEST = "attDestroyable";
	public static final String					ATTBLKNUM = "bNum";
	public static final String					ATTDOOROPEN = "doorOpen";
	public static final String					ATTGRAIN = "isGrain";
	public static final String					ATTAGHASGRAIN = "agentHasGrain";
	public static final String					ATTAGHASBREAD = "agentHasBread";
	public static final String					ATTHASLADDER = "hasLadder";
	
	// ----- ACTION STRINGS -----
	public static final String					ACTIONFORWARD = "forward";
	public static final String					ACTIONBACKWARD = "back";
	public static final String					ACTIONLEFT = "left";
	public static final String					ACTIONRIGHT = "right";
	
	public static final String					ACTIONPLACEF = "placeForward";
	public static final String					ACTIONPLACEB = "placeBack";
	public static final String					ACTIONPLACER = "placeRight";
	public static final String					ACTIONPLACEL = "placeLeft";
	public static final String					ACTIONPLACED = "placeDown";
	
	public static final String					ACTIONDESTF = "destroyForward";
	public static final String					ACTIONDESTB = "destroyBack";
	public static final String					ACTIONDESTR = "destroyRight";
	public static final String					ACTIONDESTL = "destroyLeft";
	
	public static final String					ACTIONOPENF = "openForward";
	public static final String					ACTIONOPENB = "openBack";
	public static final String					ACTIONOPENR = "openRight";
	public static final String					ACTIONOPENL = "openLeft";
	
	public static final String					ACTIONGRAIN = "pickUpGrain";
	
//	public static final String					ACTIONUSEOVENF = "useOvenForward";
//	public static final String					ACTIONUSEOVENB = "useOvenBack";
//	public static final String					ACTIONUSEOVENR = "useOvenRight";
//	public static final String					ACTIONUSEOVENL = "useOvenLeft";
	
	public static final String					ACTIONJUMP = "jump";
//	public static final String					ACTIONJUMPB = "jumpB";
//	public static final String					ACTIONJUMPR = "jumpR";
//	public static final String					ACTIONJUMPL = "jumpL";

	public static final String					ACTIONPLACEGRAIN = "PlaceGrain";

	// ----- ACTIONS -----
	public Action								forward;
	public Action								backward;
	public Action								left;
	public Action								right;
	public Action								placeF;
	public Action								placeB;
	public Action								placeR;
	public Action								placeL;
	public Action								placeD; // placeDown
	public Action		 						destF;
	public Action								destB;
	public Action		 						destL;
	public Action								destR;
	public Action								openF;
	public Action								openB;
	public Action								openR;
	public Action								openL;
	public Action								pickUpGrain;
	public Action								placeGrain;
	public Action								jump;
//	public Action								jumpB;
//	public Action								jumpR;
//	public Action								jumpL;
	
	// ----- PROPOSITIONAL FUNCTION STRINGS -----
	public static final String					PFATGOAL = "AtGoal";
	public static final String					PFNINWORLD	 = "NotInWorld";
	public static final String					ISATLOC = "IsAtLocation";
	public static final String					ISWALK = "IsWalkable";
	public static final String					ISHALF = "IsHalfWay";
	public static final String					ISAXLESS = "IsAgentXLess";
	public static final String					ISAYLESS = "IsAgentYLess";
	public static final String					ISAXMORE = "IsAgentXMore";
	public static final String					ISAYMORE = "IsAgentYMore";
	public static final String					ISPLANE = "IsAdjPlane";
	public static final String					ISADJTRENCH = "IsAdjTrench";
	public static final String 					ISADJDOOR = "IsAdjDoor";
	public static final String 					ISADJOVEN = "IsAdjOven";
	public static final String 					ISONGRAIN = "IsOnGrain";
	public static final String 					ISINLAVA = "IsInLava";
	public static final String					ISADJDWALL = "IsAdjDstableWall";
	public static final String 					AGENTHASBREAD = "AgentHasBread";
	public static final String 					ISDOOROPEN = "IsDoorOpen";

	// ----- CONSTANTS -----
	public static final String					CLASSAGENT = "agent";
	public static final String					CLASSGOAL = "goal";
	public static final String					CLASSBLOCK = "block";
	public static final String					CLASSDOOR = "door";
	public static final String					CLASSOVEN = "oven";
	public static final String					CLASSLAVA = "lava";
	public static int							MAXX = 14; // 0 - 9, gives us a 10x10 surface
	public static int							MAXY = 14;
	public static final int						MAXZ = 8;
	public static final int						MAXBLKNUM = 4;
	
	public static final double					nondetThreshold = 0.1;


	
	// ----- MAP & DOMAIN -----
	public static AtGoalPF 							AtGoalPF = null;
	public static int[][][]							MAP;
	public static HashMap<String,OldAffordance>		affordances;
	public static Stack<OldAffordanceSubgoal>		goalStack;
	private ObjectClass 							agentClass = null;
	private ObjectClass 							goalClass = null;
	public SADomain									DOMAIN = null;
	public static boolean 							deterministicMode = true;
	
	
	/**
	 * Constructs an empty map with deterministic transitions
	 * @param width width of the map
	 * @param height height of the map
	 * 
	 * NOTE: Refactor this at some point so it is a constructor.
	 */
	public Domain generateDomain(int rows, int cols, boolean placeMode, boolean destMode) {
		MAXX = rows;
		MAXY = cols;
		
		if(DOMAIN != null){
			return DOMAIN;
		}
		
		DOMAIN = new SADomain();
		
		// ==== CREATE ATTRIBUTES ====
		Attribute xatt = new Attribute(DOMAIN, ATTX, Attribute.AttributeType.DISC);
		xatt.setDiscValuesForRange(0, MAXX, 1);
		
		Attribute yatt = new Attribute(DOMAIN, ATTY, Attribute.AttributeType.DISC);
		yatt.setDiscValuesForRange(0, MAXY, 1);

		Attribute zatt = new Attribute(DOMAIN, ATTZ, Attribute.AttributeType.DISC);
		zatt.setDiscValuesForRange(0, MAXZ, 1);
		
		// Number of blocks the agent may carry
		Attribute blknumatt = new Attribute(DOMAIN, ATTBLKNUM, Attribute.AttributeType.DISC);
		blknumatt.setDiscValuesForRange(0, MAXBLKNUM, 1);

		Attribute destroyableatt = new Attribute(DOMAIN, ATTDEST, Attribute.AttributeType.DISC);
		destroyableatt.setDiscValuesForRange(0, 1, 1);
		
		Attribute dooropenatt = new Attribute(DOMAIN, ATTDOOROPEN, Attribute.AttributeType.DISC);
		dooropenatt.setDiscValuesForRange(0, 1, 1);
		
		Attribute isgrainatt = new Attribute(DOMAIN, ATTGRAIN, Attribute.AttributeType.DISC);
		isgrainatt.setDiscValuesForRange(0, 1, 1);
		
		Attribute hasgrainatt = new Attribute(DOMAIN, ATTAGHASGRAIN, Attribute.AttributeType.DISC);
		hasgrainatt.setDiscValuesForRange(0, 1, 1);
//		 String actionSequenc

		Attribute hasbreadatt = new Attribute(DOMAIN, ATTAGHASBREAD, Attribute.AttributeType.DISC);
		hasbreadatt.setDiscValuesForRange(0, 1, 1);
		
		Attribute hasLadder = new Attribute(DOMAIN, ATTHASLADDER, Attribute.AttributeType.DISC);
		hasLadder.setDiscValuesForRange(0, 1, 1);

		
		// CREATE AGENT
		agentClass = new ObjectClass(DOMAIN, CLASSAGENT);
		agentClass.addAttribute(xatt);
		agentClass.addAttribute(yatt);
		agentClass.addAttribute(zatt);
		agentClass.addAttribute(blknumatt);
		agentClass.addAttribute(hasgrainatt);
		agentClass.addAttribute(hasbreadatt);
		
		// CREATE GOAL
		ObjectClass goalClass = new ObjectClass(DOMAIN, CLASSGOAL);
		goalClass.addAttribute(xatt);
		goalClass.addAttribute(yatt);
		goalClass.addAttribute(zatt);
		
		// CREATE BLOCKS
		ObjectClass blockClass = new ObjectClass(DOMAIN, CLASSBLOCK);
		blockClass.addAttribute(xatt);
		blockClass.addAttribute(yatt);
		blockClass.addAttribute(zatt);
		blockClass.addAttribute(destroyableatt);
		blockClass.addAttribute(isgrainatt);
		blockClass.addAttribute(hasLadder);
		
		// CREATE DOORS
		ObjectClass doorClass = new ObjectClass(DOMAIN, CLASSDOOR);
		doorClass.addAttribute(xatt);
		doorClass.addAttribute(yatt);
		doorClass.addAttribute(zatt);
		doorClass.addAttribute(dooropenatt);
		
		// CREATE OVENS
		ObjectClass ovenClass = new ObjectClass(DOMAIN, CLASSOVEN);
		ovenClass.addAttribute(xatt);
		ovenClass.addAttribute(yatt);
		ovenClass.addAttribute(zatt);
		
		// CREATE LAVA
		ObjectClass lavaClass = new ObjectClass(DOMAIN, CLASSLAVA);
		lavaClass.addAttribute(xatt);
		lavaClass.addAttribute(yatt);
		lavaClass.addAttribute(zatt);
		
		// ==== CREATE ACTIONS ====
		
		// Movement
		this.forward = new ForwardAction(ACTIONFORWARD, DOMAIN, "");
		this.backward = new BackwardAction(ACTIONBACKWARD, DOMAIN, "");
		this.right = new RightAction(ACTIONRIGHT, DOMAIN, "");
		this.left = new LeftAction(ACTIONLEFT, DOMAIN, "");

		boolean allActMode = true;
		
		if (allActMode) {
		
			if (placeMode) {
				// Placement
				this.placeF = new PlaceActionF(ACTIONPLACEF, DOMAIN, "");
				this.placeB = new PlaceActionB(ACTIONPLACEB, DOMAIN, "");
				this.placeL = new PlaceActionL(ACTIONPLACEL, DOMAIN, "");
				this.placeR = new PlaceActionR(ACTIONPLACER, DOMAIN, "");
				this.placeD = new PlaceActionD(ACTIONPLACED, DOMAIN, "");
			}
			if (destMode) {
				// Destruction
				this.destF = new DestActionF(ACTIONDESTF, DOMAIN, "");
				this.destB = new DestActionB(ACTIONDESTB, DOMAIN, "");
				this.destR = new DestActionL(ACTIONDESTR, DOMAIN, "");
				this.destL = new DestActionR(ACTIONDESTL, DOMAIN, "");
			}

			
			// Open Door
			this.openF = new OpenActionF(ACTIONOPENF, DOMAIN, "");
			this.openB = new OpenActionB(ACTIONOPENB, DOMAIN, "");
			this.openR = new OpenActionL(ACTIONOPENR, DOMAIN, "");
			this.openL = new OpenActionR(ACTIONOPENL, DOMAIN, "");

			// Pick Up Grain
			this.pickUpGrain = new pickUpGrainAction(ACTIONGRAIN, DOMAIN, "");
			
			// Use Oven
			this.placeGrain = new placeGrainAction(ACTIONPLACEGRAIN, DOMAIN, "");

			// Jump
			this.jump = new JumpAction(ACTIONJUMP, DOMAIN, "");
//			this.jumpB = new JumpActionB(ACTIONJUMPB, DOMAIN, "") 
		}
		
		// ==== PROPOSITIONAL FUNCTIONS ====
		
		return DOMAIN;
	}
	

	public HashMap<String,OldAffordance> getAffordances() {
		return affordances;
	}
	
	public Stack<OldAffordanceSubgoal> getGoalStack() {
		return goalStack;
	}
	
	/* === Mutators === */
	
	public void addBlock(State s, int x, int y, int z){
		ObjectInstance block = new ObjectInstance(this.DOMAIN.getObjectClass(CLASSBLOCK), CLASSBLOCK+x+y+z);
		block.setValue(ATTX, x);
		block.setValue(ATTY, y);
		block.setValue(ATTZ, z);
		block.setValue(ATTDEST, 1); // blocks you place can be destroyed
		block.setValue(ATTGRAIN, 0); // Block is not grain.
		s.addObject(block);
	}
	
	public static void removeBlock(State s, int x, int y, int z) {
		ObjectInstance block = s.getObject("block" + Integer.toString(x) + Integer.toString(y) + Integer.toString(z));
		s.removeObject(block);
	}
	
	/* === Class Accessors === */
	
	public static ObjectInstance getBlockAt(State st, int x, int y, int z){
		ObjectInstance o = st.getObject("block" + Integer.toString(x) + Integer.toString(y) + Integer.toString(z));
		return o;
	}
	
	public static boolean isInstanceOfAt(State st, int x, int y, int z, String className) {
		ObjectInstance o = st.getObject(className + Integer.toString(x) + Integer.toString(y) + Integer.toString(z));
		if (o != null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static ObjectInstance getInstanceOfAt(State st, int x, int y, int z, String className) {
		ObjectInstance o = st.getObject(className + Integer.toString(x) + Integer.toString(y) + Integer.toString(z));
		return o;
	}
	
	
	public static boolean isCellEmpty(State st, int x, int y, int z){
		
		if (x < 0 || x > MAXX || y < 0 || y > MAXY || z < 0 || z > MAXZ) {
			return false; // TODO: make sure this works.
		}
		
		return (getBlockAt(st, x, y, z) == null);
	}
	
	public static int getCellContents(State st, int x, int y, int z){
		
		if (x < 0 || x > MAXX || y < 0 || y > MAXY || z < 0 || z > MAXZ) {
			// Beyond the edge of the universe
			return -1;
		}
		
		if (getBlockAt(st, x, y, z) == null) {
			// There is nothing at this location
			return 0;
		}
		
		return 1; //There is something there
	}
	
	public static boolean isAdjPlane(State st, int ax, int ay, int az) {
		// Returns true if the block in front of, behind, to the left of, and to the right of the agent are EMPTY, and have a block under them
		// Otherwise, false
//		if ((!isCellEmpty(st, ax + 1, ay, az - 1)) && (!isCellEmpty(st, ax - 1, ay, az - 1))
//			&& (!isCellEmpty(st, ax, ay + 1, az - 1)) && (!isCellEmpty(st, ax, ay - 1, az - 1))
//			&& (isCellEmpty(st, ax, ay + 1, az)) && (isCellEmpty(st, ax, ay - 1, az))
//			&& (isCellEmpty(st, ax, ay + 1, az)) && (isCellEmpty(st, ax, ay - 1, az))) {
//			return true;
//		}
		if ((getCellContents(st, ax + 1, ay, az - 1) != 0) && (getCellContents(st, ax - 1, ay, az - 1) != 0)
				&& (getCellContents(st, ax, ay + 1, az - 1) != 0) && (getCellContents(st, ax, ay - 1, az - 1) != 0)) {
				return true;
			}
		else {
			return false;
		}
	}
	
	public static boolean isAdjDoor(State st, int ax, int ay, int az) {
		// Returns true if the block in front of, behind, to the left of, or to the right of the agent contains a door

		if ((isInstanceOfAt(st, ax + 1, ay, az, CLASSDOOR)) || (isInstanceOfAt(st, ax - 1, ay, az, CLASSDOOR))
				|| (isInstanceOfAt(st, ax, ay + 1, az, CLASSDOOR)) || (isInstanceOfAt(st, ax, ay - 1, az, CLASSDOOR))) {
				return true;
			}
		else {
			return false;
		}
	}
	
	public static boolean isAdjOven(State st, int ax, int ay, int az) {
		// Returns true if the block in front of, behind, to the left of, or to the right of the agent contains a door

		if ((isInstanceOfAt(st, ax + 1, ay, az, CLASSOVEN)) || (isInstanceOfAt(st, ax - 1, ay, az, CLASSOVEN))
				|| (isInstanceOfAt(st, ax, ay + 1, az, CLASSOVEN)) || (isInstanceOfAt(st, ax, ay - 1, az, CLASSOVEN))) {
				return true;
			}
		else {
			return false;
		}
	}
	
	public static boolean isAdjTrench(State st, int ax, int ay, int az) {
		// Returns true if the block in front of, behind, to the left of, or to the right of the agent is a trench
		
		
		if (getCellContents(st, ax, ay - 1, az - 1) == 0 || getCellContents(st, ax, ay + 1, az - 1) == 0
				|| getCellContents(st, ax - 1, ay, az - 1) == 0 || getCellContents(st, ax + 1, ay, az - 1) == 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isAdjDstableWall(State st, int ax, int ay, int az) {
		// Returns true if the block in front of, behind, to the left of, or to the right of the agent is a trench
		
		if (((getBlockAt(st, ax + 1, ay, az) != null) && (getBlockAt(st, ax + 1, ay, az).getDiscValForAttribute(ATTDEST) == 1))
				|| ((getBlockAt(st, ax - 1, ay, az) != null) && (getBlockAt(st, ax - 1, ay, az).getDiscValForAttribute(ATTDEST) == 1))
				|| ((getBlockAt(st, ax, ay + 1, az) != null) && (getBlockAt(st, ax, ay + 1, az).getDiscValForAttribute(ATTDEST) == 1))
				|| ((getBlockAt(st, ax, ay - 1, az) != null) && (getBlockAt(st, ax, ay - 1, az).getDiscValForAttribute(ATTDEST) == 1))){
				// We're next to a destroyable block
				return true;
			}
		else {
			return false;
		}
	}
	
	/* =====ACTIONS===== */
	
	/**
	 * Attempts to move the agent into the given position, taking into account blocks in the world (holes and walls)
	 * @param the current state
	 * @param the attempted new X position of the agent
	 * @param the attempted new Y position of the agent
	 * @param the attempted new Z position of the agent
	 */
	public static void move(State s, int xd, int yd, int zd){
		
		ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
		int ax = agent.getDiscValForAttribute(ATTX);
		int ay = agent.getDiscValForAttribute(ATTY);
		int az = agent.getDiscValForAttribute(ATTZ);
		

		if (!deterministicMode) {
			Random rand = new Random();
			double threshhold = rand.nextDouble();
			
			if (threshhold < nondetThreshold) {
				xd = -xd;
				yd = -yd;
			}
		}
		
		int nx = ax+xd;
		int ny = ay+yd;
		int nz = az+zd;
		
		if (nx < 0 || nx > MAXX || ny < 0 || ny > MAXY || nz < 0 || nz > MAXZ) {
			// Trying to move out of bounds, return.
			return;
		}
		
		if (isInstanceOfAt(s, nx, ny, nz, CLASSDOOR)) {
			ObjectInstance o = getInstanceOfAt(s, nx, ny, nz, CLASSDOOR);
			if (o.getDiscValForAttribute(ATTDOOROPEN) == 1) {
				// There is an open door where we are trying to move.
				agent.setValue(ATTX, nx);
				agent.setValue(ATTY, ny);
				agent.setValue(ATTZ, nz);
				return;
			}
			else {
				// Closed door in the way, can't move there.
//				System.out.println("CLOSED DOOR IN THE WAY");
				return;
			}
		}
		
//		if (nz - 1 > -1 && getBlockAt(s, nx, ny, nz - 1) == null && !isInstanceOfAt(s, nx, ny, nz - 1, CLASSLAVA)) {
//			// There is no block under us, return.
//			return;
//		}
		if (getBlockAt(s, nx, ny, nz) != null) {
			// There is a block where we are trying to move, return.
			return;
		}
		else {
			// Place we're moving is unobstructed // and there is solid ground below us, move
			agent.setValue(ATTX, nx);
			agent.setValue(ATTY, ny);
			agent.setValue(ATTZ, nz);
		}

	}
	
	// After each action execution, handle falling logic
	public static void fall(State s) {
		ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
		int ax = agent.getDiscValForAttribute(ATTX);
		int ay = agent.getDiscValForAttribute(ATTY);
		int az = agent.getDiscValForAttribute(ATTZ);
		
		if (getCellContents(s, ax, ay, az - 1) != 1) {
			// No block below agent -- fall
			agent.setValue(ATTZ, az - 1);
		}
	}
	
	public static void jump(State st){
		
		ObjectInstance agent = st.getObjectsOfTrueClass(CLASSAGENT).get(0);
		int ax = agent.getDiscValForAttribute(ATTX);
		int ay = agent.getDiscValForAttribute(ATTY);
		int az = agent.getDiscValForAttribute(ATTZ);
		
		if (az + 1 > MAXZ) {
			// Trying to move out of bounds, return.
			return;
		}
		
		// Check to see if there is a floor or ceiling
		if (getCellContents(st, ax, ay, az - 1) == 0 || getCellContents(st, ax, ay, az + 1) != 0) {
			return;
		}
		
		// jump!
		agent.setValue(ATTZ, az + 1);

	}
	
	public void place(State s, int dx, int dy, int dz) {
		
		ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
		
		// Check to see if the agent is out of blocks
		int numAgentsBlocks = agent.getDiscValForAttribute(ATTBLKNUM);
		if (numAgentsBlocks <= 0) {
			return;
		}
		
		// Agent's global coordinates
		int ax = agent.getDiscValForAttribute(ATTX);
		int ay = agent.getDiscValForAttribute(ATTY);
		int az = agent.getDiscValForAttribute(ATTZ);
		
		// For now, block placement down is deterministic
		if (!deterministicMode && dz == 0) {
			Random rand = new Random();
			double threshhold = rand.nextDouble();
			
			if (threshhold < nondetThreshold) {
				dz = -dz;
				dy = -dy;
			}
		}
		

		// Get global coordinates of the loc to place the block
		int bx = ax+dx;
		int by = ay+dy;
		int bz = az+dz;
		
		
		// Make sure we are placing a block in bounds
		if (bx < 0 || bx > MAXX || by < 0 || by > MAXY || bz < 0 || bz > MAXZ) {
			return;
		}
		
//		System.out.println("numBlocks: " + numAgentsBlocks);

		// If block loc is empty (z-1 from bot), and the loc above is empty (i.e. we can "see" the bottom loc), place it.
		if (numAgentsBlocks > 0 && bz - 1 >= 0 && isPlaceable(s,bx,by,bz - 1)){
			
			addBlock(s, bx, by, bz - 1);
			
			// Remove the block from the agent's inventory
			agent.setValue(ATTBLKNUM, numAgentsBlocks - 1);
//			System.out.println("placing BELOW: (dx,dy,dz) | (ax,ay,az) (" + dx + ", " + dy + ", " + dz + ") " + " (" + ax + ", " + ay + ", " + az + ")" + "    " + numAgentsBlocks);
			
			
		}
		// Now try placing one on agent's z level if it couldn't place one at z - 1
		else if (isPlaceable(s, bz, by, bz) && numAgentsBlocks > 0){
			
			// Place block
			addBlock(s, bx, by, bz);
			
			// Remove the block from the agent's inventory
			agent.setValue(ATTBLKNUM, numAgentsBlocks - 1);
//			System.out.println("placing EVEN: (dx,dy,dz) | (ax,ay,az) (" + dx + ", " + dy + ", " + dz + ") " + " (" + ax + ", " + ay + ", " + az + ")" + "    " + numAgentsBlocks);
			
			
		}
		
	}
	
	public static boolean isPlaceable(State s, int bx, int by, int bz) {
		// Check that cell contents at location are 0
		if (getCellContents(s, bx, by, bz) != 0)
			return false;
		
		// Check all 6 faces to make sure that at least one of them is adjacent
		// to a block.
		if (getCellContents(s, bx - 1, by, bz) == 1)
			return true;
		if (getCellContents(s, bx + 1, by, bz) == 1)
			return true;
		if (getCellContents(s, bx, by - 1, bz) == 1)
			return true;
		if (getCellContents(s, bx, by + 1, bz) == 1)
			return true;
		if (getCellContents(s, bx, by, bz - 1) == 1)
			return true;
		if (getCellContents(s, bx, by, bz + 1) == 1)
			return true;
		
		return false;
	}

	public static void destroy(State s, int dx, int dy, int dz) {
		
		// If non-deterministic mode, there's a chance we act incorrectly.
		if (!deterministicMode) {
			Random rand = new Random();
			double threshhold = rand.nextDouble();
			
			if (threshhold < nondetThreshold) {
				dx = -dx;
				dy = -dy;
			}
		}
		
		ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
		
		// Agent's global coordinates
		int ax = agent.getDiscValForAttribute(ATTX);
		int ay = agent.getDiscValForAttribute(ATTY);
		int az = agent.getDiscValForAttribute(ATTZ);
		
		// Get global coordinates of the loc to destroy the block
		int bx = ax+dx;
		int by = ay+dy;
		int bz = az+dz; // Try one below, first
		
		
		// Make sure we are destroying a block in bounds
		if (bx < 0 || bx > MAXX || by < 0 || by > MAXY || bz < 0 || bz > MAXZ) {
			return;
		}
		
		// If block loc is not empty (z-1 from bot), and the loc above is empty, we can destroy there.
		if (bz - 1 >= 0 && getBlockAt(s,bx,by,bz - 1) != null && getBlockAt(s,bx,by,bz) == null){
			// Remove the block
			if (getBlockAt(s,bx,by,bz - 1).getDiscValForAttribute("attDestroyable") == 1) {
				// Only destroy destroyable blocks
//				System.out.println("DEST BELOW: (A) (B)" + "(" + ax + "," + ay + "," + az + ") " + " (" + bx + "," + by + "," + (bz - 1) + ")");
				removeBlock(s, bx, by, bz - 1);
			}

		}
		// Now try destroying one on agent's z level if it couldn't destroy one at z - 1
		else if (getBlockAt(s, bx, by, bz) != null){
			// Remove the block
			if (getBlockAt(s,bx,by,bz).getDiscValForAttribute("attDestroyable") == 1) {
				// Only destroy destroyable blocks
				System.out.println("DEST EVEN: (A) (B)" + "(" + ax + "," + ay + "," + az + ") " + " (" + bx + "," + by + "," + bz + ")");
				
				removeBlock(s, bx, by, bz);
			}
		}
	}

	public static void open(State s, int dx, int dy, int dz) {
		
		ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
		
		// Agent's global coordinates
		int ax = agent.getDiscValForAttribute(ATTX);
		int ay = agent.getDiscValForAttribute(ATTY);
		int az = agent.getDiscValForAttribute(ATTZ);
		
		// Get global coordinates of the loc to open the door
		int bx = ax+dx;
		int by = ay+dy;
		int bz = az+dz; // Try one below, first
		
		
		// Make sure we are trying to open a door in bounds
		if (bx < 0 || bx > MAXX || by < 0 || by > MAXY || bz < 0 || bz > MAXZ) {
			return;
		}
		
		if (isInstanceOfAt(s, bx, by, bz, CLASSDOOR)) {
			ObjectInstance o = getInstanceOfAt(s, bx, by, bz, CLASSDOOR);
			int newVal = o.getDiscValForAttribute(ATTDOOROPEN) ^ 1;
//			System.out.println("CHANGING DOORS STATE");
			o.setValue(ATTDOOROPEN, newVal);
		}
		else {
			return;
		}
		
		
	}

	public static void pickup(State s, int dx, int dy, int dz) {
		
		ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
		
		// Agent's global coordinates
		int ax = agent.getDiscValForAttribute(ATTX);
		int ay = agent.getDiscValForAttribute(ATTY);
		int az = agent.getDiscValForAttribute(ATTZ);
		
		// Get global coordinates of the loc to open the door
		int bx = ax+dx;
		int by = ay+dy;
		int bz = az+dz; // Try one below, first
		
		
		// Make sure we are trying to open a door in bounds
		if (bx < 0 || bx > MAXX || by < 0 || by > MAXY || bz < 0 || bz > MAXZ) {
			return;
		}

		ObjectInstance block = getBlockAt(s, bx, by, bz);
		if ((block != null) && (block.getDiscValForAttribute(ATTGRAIN) == 1)) {
			// The block we're on "contain's grain", so "pick it up"
			agent.setValue(ATTAGHASGRAIN, 1);
			block.setValue(ATTGRAIN, 0);
		}
		else {
			return;
		}
		
		
	}

	public static void placeGrain(State st) {
		
		ObjectInstance agent = st.getObjectsOfTrueClass(CLASSAGENT).get(0);
		
		// Agent's global coordinates
		int ax = agent.getDiscValForAttribute(ATTX);
		int ay = agent.getDiscValForAttribute(ATTY);
		int az = agent.getDiscValForAttribute(ATTZ);
		
		if (agent.getDiscValForAttribute(ATTAGHASGRAIN) == 1) {
			agent.setValue(ATTAGHASGRAIN, 0);  // Remove grain from agent
			
			if (isAdjOven(st, ax, ay, az)) {
				agent.setValue(ATTAGHASBREAD, 1);				
			} else {
				// Put the grain "on the ground"
				ObjectInstance belowBlock = st.getObject("block" + ax + ay + (az-1));
				belowBlock.setValue(ATTGRAIN, 1);
			}
		}
		return;
		
	}
	
	public static class ForwardAction extends Action{

		public ForwardAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			move(st, 0, -1, 0);
			fall(st);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}
		
	}
	public static class BackwardAction extends Action{

		public BackwardAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			move(st, 0, 1, 0);
			fall(st);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}		
	}
	public static class RightAction extends Action{

		public RightAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			move(st, 1, 0, 0);
			fall(st);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}
	}
	public static class LeftAction extends Action{

		public LeftAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			move(st, -1, 0, 0);
			fall(st);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public static class JumpAction extends Action{

		public JumpAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			jump(st);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}
		
	}
		
		
	public class PlaceActionF extends Action{

		public PlaceActionF(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			place(st, 0, -1, 0);
			fall(st);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}
	}
	
	public class PlaceActionB extends Action{

		public PlaceActionB(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			place(st, 0, 1, 0);
			fall(st);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public class PlaceActionL extends Action{

		public PlaceActionL(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			place(st, -1, 0, 0);
			fall(st);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public class PlaceActionR extends Action{

	public PlaceActionR(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			place(st, 1, 0, 0);
			fall(st);
			return st;
		}	
	}
	
	public class PlaceActionD extends Action{

		public PlaceActionD(String name, Domain domain, String parameterClasses){
				super(name, domain, parameterClasses);
			}
			
			protected State performActionHelper(State st, String[] params) {
				place(st, 0, 0, -1);
				fall(st);
				return st;
			}	
		}
	
	
	public static class DestActionF extends Action{

		public DestActionF(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			destroy(st, 0, -1, 0);
			fall(st);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}
	}
	
	public static class DestActionB extends Action{

		public DestActionB(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			destroy(st, 0, 1, 0);
			fall(st);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public static class DestActionR extends Action{

		public DestActionR(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			destroy(st, -1, 0, 0);
			fall(st);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public static class DestActionL extends Action{

		public DestActionL(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			destroy(st, 1, 0, 0);
			fall(st);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public static class OpenActionF extends Action{

		public OpenActionF(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			fall(st);
			open(st, 0, -1, 0);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}
	}
	
	public static class OpenActionB extends Action{

		public OpenActionB(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			open(st, 0, 1, 0);
			fall(st);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public static class OpenActionR extends Action{

		public OpenActionR(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			open(st, -1, 0, 0);
			fall(st);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public static class OpenActionL extends Action{

		public OpenActionL(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			open(st, 1, 0, 0);
			fall(st);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public static class pickUpGrainAction extends Action{

		public pickUpGrainAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			
			fall(st);
			pickup(st, 0, 0, -1);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public static class placeGrainAction extends Action{

		public placeGrainAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			placeGrain(st);
			fall(st);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}
	}
	
	
	
	/* ==== Propositional Functions ==== */
	public static class IsAgentXLess extends PropositionalFunction{
		
		public IsAgentXLess(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public boolean isTrue(State st, String[] params) {
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			
			//get the agent coordinates
			int ax = agent.getDiscValForAttribute(ATTX);
			
			//get destination coordinates
			int nx = Integer.parseInt(params[0]);

			return (ax < nx);
		}

		@Override
		public boolean isTrue(State s) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	public static class IsAgentXMore extends PropositionalFunction{
		
		private int destX;

		public IsAgentXMore(String name, Domain domain, String[] parameterClasses, int destX) {
			super(name, domain, parameterClasses);
			this.destX = destX;
		}
		
		public boolean isTrue(State st, String[] params) {
			return isTrue(st);
		}

		@Override
		public boolean isTrue(State st) {
			// TODO Auto-generated method stub
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			
			//get the agent coordinates
			int ay = agent.getDiscValForAttribute(ATTX);
						
			return (ay > this.destX);
		}
	}
	
	public static class IsAgentYLess extends PropositionalFunction{
		
		public IsAgentYLess(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public boolean isTrue(State st, String[] params) {
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			
			//get the agent coordinates
			int ay = agent.getDiscValForAttribute(ATTY);
			
			//get destination coordinates
			int ny = Integer.parseInt(params[1]);
			
			return (ay < ny);
		}

		@Override
		public boolean isTrue(State s) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	public static class IsAgentYMore extends PropositionalFunction{
		
		private int destY;

		public IsAgentYMore(String name, Domain domain, String[] parameterClasses, int destY) {
			super(name, domain, parameterClasses);
			this.destY = destY;
		}
		
		public boolean isTrue(State st, String[] params) {
			return isTrue(st);
		}

		@Override
		public boolean isTrue(State st) {
			// TODO Auto-generated method stub
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			
			//get the agent coordinates
			int ay = agent.getDiscValForAttribute(ATTY);
						
			return (ay > this.destY);
		}
	}
	
	public static class IsAgentYAt extends PropositionalFunction {
		private int destY;
		private int blockNum;

		public IsAgentYAt(String name, Domain domain, String[] parameterClasses, int destY, int blockNum) {
			super(name, domain, parameterClasses);
			this.destY = destY;
			this.blockNum = blockNum;  // This is a quick hack to get the agent to a Y location with the correct number of blocks.
		}
		
		public boolean isTrue(State st) {
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			
			// Get the agent's current coordinates
			int ay = agent.getDiscValForAttribute(ATTY);
			int agentBlockNum = agent.getDiscValForAttribute(ATTBLKNUM);
			return (ay == this.destY);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			// TODO Auto-generated method stub
			return isTrue(st);
		}
	}
	
	public static class IsNthOfTheWay extends PropositionalFunction {
		
		private int ox;
		private int oy;
		private int oz;
		private double frac;

		public IsNthOfTheWay(String name, Domain domain, String[] parameterClasses, String[] params, double frac) {
			super(name, domain, parameterClasses);
			this.frac = frac;
			// Get the agent's origin coordinates
			this.ox = Integer.parseInt(params[0]);
			this.oy = Integer.parseInt(params[1]);
			this.oz = Integer.parseInt(params[2]);
		}
		
		@Override
		public boolean isTrue(State st) {
			//get the goal coordinates
			ObjectInstance goal = st.getObject(CLASSGOAL + "0");
			
			// Get the goal coordinates
			int gx = goal.getDiscValForAttribute(ATTX);
			int gy = goal.getDiscValForAttribute(ATTY);
			int gz = goal.getDiscValForAttribute(ATTZ);
			
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			
			// Get the agent's current coordinates
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			int az = agent.getDiscValForAttribute(ATTZ);
			
			int hx = (int) Math.ceil(this.frac * gx + (1 - this.frac) * ox);
			int hy = (int) Math.ceil(this.frac * gy + (1 - this.frac) * oy);
			int hz = (int) Math.ceil(this.frac * gz + (1 - this.frac) * oz);
//			System.out.println("frac: " + this.frac +  " hx: " + hx + " hy: " + hy + " hz: " + hz);

			return (ax == hx && ay == hy && az == hz);
		}

		@Override
		public boolean isTrue(State s, String[] params) {
			// TODO Auto-generated method stub
			return isTrue(s);
		}
	}
	
	public static class IsAtLocationPF extends PropositionalFunction{

		private int locX;
		private int locY;
		private int locZ;
		private int bNum = -1;
		

		public IsAtLocationPF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public IsAtLocationPF(String name, Domain domain, String[] parameterClasses, int x, int y, int z) {
			super(name, domain, parameterClasses);
			this.locX = x;
			this.locY = y;
			this.locZ = z;
		}
		
		public IsAtLocationPF(String name, Domain domain, String[] parameterClasses, int x, int y, int z, int bNum) {
			super(name, domain, parameterClasses);
			this.locX = x;
			this.locY = y;
			this.locZ = z;
			this.bNum = bNum;
		}
		
		@Override
		public boolean isTrue(State st) {
			// Version of isTrue that assumes paramaters were given in the construction of this prop func
			
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			
			//get the agent coordinates
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			int az = agent.getDiscValForAttribute(ATTZ);
			int bNum = agent.getDiscValForAttribute(ATTBLKNUM);
			
			if(ax == this.locX && ay == this.locY && az == this.locZ && (this.bNum == -1 || this.bNum <= bNum)){
				return true;
			}
			
			return false;
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			return isTrue(st);
		}
		
		// Returns the x,y,z delta(s) needed to satisfy the atGoalPF
		public int[] delta(State st, String[] params) {
			
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			
			//get the agent coordinates
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			int az = agent.getDiscValForAttribute(ATTZ);
			
			ObjectInstance goal = st.getObject(CLASSGOAL + "0");
			
			int nx = Integer.parseInt(params[0]);
			int ny = Integer.parseInt(params[1]);
			int nz = Integer.parseInt(params[2]);
			
			int[] dist = new int[3];
			dist[0] = nx - ax;
			dist[1] = ny - ay;
			dist[2] = nz - az;
			
			return dist;
		}
		
	}
	
	public static class NotInWorldPF extends PropositionalFunction {

		public NotInWorldPF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		@Override
		public boolean isTrue(State st, String[] params) {
			return isTrue(st);
		}
		
		@Override
		public boolean isTrue(State st) {
			// TODO Auto-generated method stub
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			
			//get the agent coordinates
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			int az = agent.getDiscValForAttribute(ATTZ);
			
			return (getCellContents(st, ax, ay, az) == -1);
		}
	}
	
	public static class AtGoalPF extends PropositionalFunction{

		public AtGoalPF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			
			//get the agent coordinates
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			int az = agent.getDiscValForAttribute(ATTZ);
			
			ObjectInstance goal = st.getObject(CLASSGOAL + "0");
			
			//get the goal coordinates
			int gx = goal.getDiscValForAttribute(ATTX);
			int gy = goal.getDiscValForAttribute(ATTY);
			int gz = goal.getDiscValForAttribute(ATTZ);
			
			if(ax == gx && ay == gy && az == gz){
				return true;
			}
			
			return false;
		}
		
		// Returns the x,y,z delta(s) needed to satisfy the atGoalPF
		public int[] delta(State st) {
			
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			
			//get the agent coordinates
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			int az = agent.getDiscValForAttribute(ATTZ);
			
			ObjectInstance goal = st.getObject(CLASSGOAL + "0");
			
			//get the goal coordinates
			int gx = goal.getDiscValForAttribute(ATTX);
			int gy = goal.getDiscValForAttribute(ATTY);
			int gz = goal.getDiscValForAttribute(ATTZ);
			
			int[] dist = new int[3];
			dist[0] = gx - ax;
			dist[1] = gy - ay;
			dist[2] = gz - az;
			
			return dist;
		}

		@Override
		public boolean isTrue(State s) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	
	public static class IsInLava extends PropositionalFunction{

		public IsInLava(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			return isTrue(st);
			
		}
		
		@Override
		public boolean isTrue(State st) {
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			
			//get the agent coordinates
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			int az = agent.getDiscValForAttribute(ATTZ);
						
			return isInstanceOfAt(st, ax, ay, az - 1, CLASSLAVA);
		}
		
	}
	
	public static class AgentHasBreadPF extends PropositionalFunction{

		public AgentHasBreadPF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			return (agent.getDiscValForAttribute(ATTAGHASBREAD) == 1);
		}
		@Override
		public boolean isTrue(State st) {
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			return (agent.getDiscValForAttribute(ATTAGHASBREAD) == 1);
		}
		
	}
	
	public static class AgentHasGrainPF extends PropositionalFunction{

		public AgentHasGrainPF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			return (agent.getDiscValForAttribute(ATTAGHASGRAIN) == 1);
		}
		@Override
		public boolean isTrue(State st) {
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			return (agent.getDiscValForAttribute(ATTAGHASGRAIN) == 1);
		}
		
	}
	
	public static class IsWalkablePF extends PropositionalFunction {

		public IsWalkablePF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		@Override
		public boolean isTrue(State st, String[] params) {
			// Assume everything is walkable for now.
//			// The first three elements of params are the amount of change
//			// in the x, y, and z directions
			ObjectInstance agent = st.getObjectsOfTrueClass(CLASSAGENT).get(0);
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			int az = agent.getDiscValForAttribute(ATTZ);

			int nx = ax + Integer.parseInt(params[0]);
			int ny = ay + Integer.parseInt(params[1]);
			int nz = az + Integer.parseInt(params[2]);
			
			if (nx < 0 || nx > MAXX || ny < 0 || ny > MAXY || nz < 0 || nz > MAXZ) {
				// Trying to move out of bounds, return.
				return false;
			}
			
			if (nz - 1 > 0 && MinecraftDomain.getBlockAt(st, nx, ny, nz - 1) == null) {
				// There is no block under us, return.
				return false;
			}
			else if (getBlockAt(st, nx, ny, nz) != null) {
				// There is a block where we are trying to move, return.
				return false;
			}
			return true;
		}

		@Override
		public boolean isTrue(State st) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	
	public static class IsAdjPlane extends PropositionalFunction {

		private int dx;
		private int dy;
		private int dz;
		private boolean dirFlag = false;
		
		public IsAdjPlane(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public IsAdjPlane(String name, Domain domain, String[] parameterClasses, int[] dir) {			
			super(name, domain, parameterClasses);
			this.dx = dir[0];
			this.dy = dir[1];
			this.dz = dir[2];
			this.dirFlag = true;
		}
		
		@Override
		public boolean isTrue(State st, String[] params) {
			return isTrue(st);
		}

		@Override
		public boolean isTrue(State st) {
			// Assume everything is walkable for now.
//			// The first three elements of params are the amount of change
//			// in the x, y, and z directions
			ObjectInstance agent = st.getObjectsOfTrueClass(CLASSAGENT).get(0);
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			int az = agent.getDiscValForAttribute(ATTZ);

			
//			Dirflag is set and there is a block beneath agent and no block at its level (in the direction)
			if (this.dirFlag && (getCellContents(st, ax + dx, ay + dy, az - 1) == 1 && getCellContents(st, ax + dx, ay + dy, az) == 0)) {
				return true;
			}
			else if (!this.dirFlag && isAdjPlane(st, ax, ay, az)) {
				return true;
			}
			else {
				return false;
			}
		}
		
	}
	
	public static class IsAdjTrench extends PropositionalFunction {

		private int dx;
		private int dy;
		private int dz;
		private boolean dirFlag = false;
		private int dist;
		
		public IsAdjTrench(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public IsAdjTrench(String name, Domain domain, String[] parameterClasses, int[] dir, int dist) {			
			super(name, domain, parameterClasses);
			this.dx = dir[0];
			this.dy = dir[1];
			this.dz = dir[2];
			this.dirFlag = true;
			this.dist = dist;
		}
		
		@Override
		public boolean isTrue(State st, String[] params) {
			return isTrue(st);
		}

		@Override
		public boolean isTrue(State st) {
			ObjectInstance agent = st.getObjectsOfTrueClass(CLASSAGENT).get(0);
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			int az = agent.getDiscValForAttribute(ATTZ);

			if (this.dirFlag && getCellContents(st, ax + this.dx * this.dist, ay + this.dy * this.dist, az - 1) == 0) {
				return true;
			}
			else if (!this.dirFlag && isAdjTrench(st, ax, ay, az)) {
				return true;
			}
			else {
				return false;
			}

		}
		
	}
	
	
	public static class IsAdjDoor extends PropositionalFunction {

		public IsAdjDoor(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		@Override
		public boolean isTrue(State st, String[] params) {
			return isTrue(st);
		}

		@Override
		public boolean isTrue(State st) {
			// Assume everything is walkable for now.
//			// The first three elements of params are the amount of change
//			// in the x, y, and z directions
			ObjectInstance agent = st.getObjectsOfTrueClass(CLASSAGENT).get(0);
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			int az = agent.getDiscValForAttribute(ATTZ);

			
			if (isAdjDoor(st, ax, ay, az)) {
				return true;
			}
			else {
				return false;
			}
		}
		
	}
	public static class IsAdjOven extends PropositionalFunction {

		public IsAdjOven(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		@Override
		public boolean isTrue(State st, String[] params) {
			return isTrue(st);
		}

		@Override
		public boolean isTrue(State st) {
			// Assume everything is walkable for now.
//			// The first three elements of params are the amount of change
//			// in the x, y, and z directions
			ObjectInstance agent = st.getObjectsOfTrueClass(CLASSAGENT).get(0);
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			int az = agent.getDiscValForAttribute(ATTZ);

			
			if (isAdjOven(st, ax, ay, az)) {
				return true;
			}
			else {
				return false;
			}
		}
		
	}
	
	public static class IsOnGrain extends PropositionalFunction {

		public IsOnGrain(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		@Override
		public boolean isTrue(State st, String[] params) {
			return isTrue(st);
		}

		@Override
		public boolean isTrue(State st) {
			// Assume everything is walkable for now.
//			// The first three elements of params are the amount of change
//			// in the x, y, and z directions
			ObjectInstance agent = st.getObjectsOfTrueClass(CLASSAGENT).get(0);
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			int az = agent.getDiscValForAttribute(ATTZ);

			
			if (getBlockAt(st, ax, ay, az - 1) != null && getBlockAt(st, ax, ay, az - 1).getDiscValForAttribute(ATTGRAIN) == 1) {
				return true;
			}
			else {
				return false;
			}
		}
		
	}
	
	public static class IsDoorOpen extends PropositionalFunction {

		private int doorX;
		private int doorY;
		private int doorZ;

		public IsDoorOpen(String name, Domain domain, String[] parameterClasses, String[] params) {
			super(name, domain, parameterClasses);
			this.doorX = Integer.parseInt(params[0]);
			this.doorY = Integer.parseInt(params[1]);
			this.doorZ = Integer.parseInt(params[2]);		}
		
		@Override
		public boolean isTrue(State st, String[] params) {
			return isTrue(st);
		}

		@Override
		public boolean isTrue(State st) {

			List<ObjectInstance> doors = st.getObjectsOfTrueClass(CLASSDOOR);
			
			for (ObjectInstance d: doors) {
				int x = d.getDiscValForAttribute(ATTX);
				int y = d.getDiscValForAttribute(ATTY);
				int z= d.getDiscValForAttribute(ATTZ);
				
				if (x == doorX && y == doorY && z == doorZ) {
					// We've found the correct door
					return (d.getDiscValForAttribute(ATTDOOROPEN) == 1);
				}
				
			}

			
			return false;
		}
		
	}
	
	public static class IsAdjDstableWall extends PropositionalFunction {

		private int dx;
		private int dy;
		private int dz;
		private boolean dirFlag = false;
		
		public IsAdjDstableWall(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public IsAdjDstableWall(String name, Domain domain, String[] parameterClasses, int[] dir) {			
			super(name, domain, parameterClasses);
			this.dx = dir[0];
			this.dy = dir[1];
			this.dz = dir[2];
			this.dirFlag = true;
		}
		
		@Override
		public boolean isTrue(State st, String[] params) {
			return isTrue(st);
		}

		@Override
		public boolean isTrue(State st) {
			// Assume everything is walkable for now.
//			// The first three elements of params are the amount of change
//			// in the x, y, and z directions
			ObjectInstance agent = st.getObjectsOfTrueClass(CLASSAGENT).get(0);
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			int az = agent.getDiscValForAttribute(ATTZ);

			
			if (dirFlag && getCellContents(st, ax + dx, ay + dy, az) == 1) {
//				There is a block in the direction, check to see if it is destroyable
				return (getBlockAt(st, ax + dx, ay + dy, az).getDiscValForAttribute(ATTDEST) == 1);
			}
			else if (!dirFlag && isAdjDstableWall(st, ax, ay, az)) {
				return true;
			}
			else {
				return false;
			}
		}
		
	}
	
	
	public static void main(String[] args) {
		
		MinecraftDomain mcd = new MinecraftDomain();
		
		Domain d = mcd.generateDomain();
		
		// === Build Map === //

		MCStateGenerator mcsg = new MCStateGenerator("hardworld.map");

		State initialState = mcsg.getCleanState(d);
			
		
		// Explorer for testing
		TerminalExplorer exp = new TerminalExplorer(d);
		exp.addActionShortHand("f", ACTIONFORWARD);
		exp.addActionShortHand("b", ACTIONBACKWARD);
		exp.addActionShortHand("r", ACTIONRIGHT);
		exp.addActionShortHand("l", ACTIONLEFT);
		exp.addActionShortHand("pf", ACTIONPLACEF);
		exp.addActionShortHand("pb", ACTIONPLACEB);
		exp.addActionShortHand("pr", ACTIONPLACER);
		exp.addActionShortHand("pl", ACTIONPLACEL);
		exp.addActionShortHand("df", ACTIONDESTF);
		exp.addActionShortHand("db", ACTIONDESTB);
		exp.addActionShortHand("dr", ACTIONDESTR);
		exp.addActionShortHand("dl", ACTIONDESTL);
		exp.addActionShortHand("of", ACTIONOPENF);
		exp.addActionShortHand("ob", ACTIONOPENB);
		exp.addActionShortHand("or", ACTIONOPENR);
		exp.addActionShortHand("ol", ACTIONOPENL);
		exp.addActionShortHand("pu", ACTIONGRAIN);
		exp.addActionShortHand("pg", ACTIONPLACEGRAIN);
//		exp.addActionShortHand("bbf", ACTIONUSEOVENF);
//		exp.addActionShortHand("bbb", ACTIONUSEOVENB);
//		exp.addActionShortHand("bbl", ACTIONUSEOVENL);
//		exp.addActionShortHand("bbr", ACTIONUSEOVENR);
		
		exp.addActionShortHand("jf", ACTIONJUMP);
//		exp.addActionShortHand("jb", ACTIONJUMPB);
//		exp.addActionShortHand("jl", ACTIONJUMPL);
//		exp.addActionShortHand("jr", ACTIONJUMPR);
		
		exp.exploreFromState(initialState);
	}

	
	public static String[] locCoordsToGlobal(State s, String[] locCoords) {
		// Converts agent relative coordinates to global coordinates
		ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
		int ax = agent.getDiscValForAttribute(ATTX);
		int ay = agent.getDiscValForAttribute(ATTY);
		int az = agent.getDiscValForAttribute(ATTZ);

		int dx = Integer.parseInt(locCoords[0]);
		int dy = Integer.parseInt(locCoords[1]);
		int dz = Integer.parseInt(locCoords[2]);
		
		Integer nx = ax+dx;
		Integer ny = ay+dy;
		Integer nz = az+dz;
		
		String[] globalCoords = {nx.toString(), ny.toString(), nz.toString()};
		
		return globalCoords;
	}
	
	public static String[] globCoordsToLocal(State s, String[] globCoords) {
		// Converts agent relative coordinates to global coordinates
		ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
		int ax = agent.getDiscValForAttribute(ATTX);
		int ay = agent.getDiscValForAttribute(ATTY);
		int az = agent.getDiscValForAttribute(ATTZ);

		int dx = Integer.parseInt(globCoords[0]);
		int dy = Integer.parseInt(globCoords[1]);
		int dz = Integer.parseInt(globCoords[2]);
		
		Integer nx = dx - ax;
		Integer ny = dy - ay;
		Integer nz = dz - az;
		
		String[] localCoords = {nx.toString(), ny.toString(), nz.toString()};
		
		return localCoords;
	}

	@Override
	public Domain generateDomain() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
