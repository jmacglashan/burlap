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
	
	// ----- ACTION STRINGS -----
	public static final String					ACTIONFORWARD = "forward";
	public static final String					ACTIONBACKWARD = "back";
	public static final String					ACTIONLEFT = "left";
	public static final String					ACTIONRIGHT = "right";
	
	public static final String					ACTIONPLACEF = "placeForward";
	public static final String					ACTIONPLACEB = "placeBack";
	public static final String					ACTIONPLACER = "placeRight";
	public static final String					ACTIONPLACEL = "placeLeft";
	
	public static final String					ACTIONDESTF = "destroyForward";
	public static final String					ACTIONDESTB = "destroyBack";
	public static final String					ACTIONDESTR = "destroyRight";
	public static final String					ACTIONDESTL = "destroyLeft";
	
	public static final String					ACTIONOPENF = "openForward";
	public static final String					ACTIONOPENB = "openBack";
	public static final String					ACTIONOPENR = "openRight";
	public static final String					ACTIONOPENL = "openLeft";
	
	public static final String					ACTIONGRAIN = "pickUpGrain";
	
	public static final String					ACTIONUSEOVENF = "useOvenForward";
	public static final String					ACTIONUSEOVENB = "useOvenBack";
	public static final String					ACTIONUSEOVENR = "useOvenRight";
	public static final String					ACTIONUSEOVENL = "useOvenLeft";
	
	public static final String					ACTIONJUMPF = "jumpF";
	public static final String					ACTIONJUMPB = "jumpB";
	public static final String					ACTIONJUMPR = "jumpR";
	public static final String					ACTIONJUMPL = "jumpL";
	
	// ----- ACTIONS -----
	public Action								forward;
	public Action								backward;
	public Action								left;
	public Action								right;
	public Action								placeF;
	public Action								placeB;
	public Action								placeR;
	public Action								placeL;
	public Action		 						destF;
	public Action								destB;
	public Action		 						destL;
	public Action								destR;
	public Action								openF;
	public Action								openB;
	public Action								openR;
	public Action								openL;
	public Action								pickUpGrain;
	public Action								useOvenF;
	public Action								useOvenB;
	public Action								useOvenR;
	public Action								useOvenL;
	public Action								jumpF;
	public Action								jumpB;
	public Action								jumpR;
	public Action								jumpL;
	
	// ----- PROPOSITIONAL FUNCTION STRINGS -----
	public static final String					PFATGOAL = "AtGoal";
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
	public Domain generateDomain(int rows, int cols) {
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
		
		// CREATE OVENS
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
		
			boolean blockMode = true;
			if (blockMode) {
				// Placement
				this.placeF = new PlaceActionF(ACTIONPLACEF, DOMAIN, "");
				this.placeB = new PlaceActionB(ACTIONPLACEB, DOMAIN, "");
				this.placeR = new PlaceActionL(ACTIONPLACER, DOMAIN, "");
				this.placeL = new PlaceActionR(ACTIONPLACEL, DOMAIN, "");
				
				// Destruction
//				this.destF = new DestActionF(ACTIONDESTF, DOMAIN, "");
//				this.destB = new DestActionB(ACTIONDESTB, DOMAIN, "");
//				this.destR = new DestActionL(ACTIONDESTR, DOMAIN, "");
//				this.destL = new DestActionR(ACTIONDESTL, DOMAIN, "");
			}
			
			// Open Door
			this.openF = new OpenActionF(ACTIONOPENF, DOMAIN, "");
			this.openB = new OpenActionB(ACTIONOPENB, DOMAIN, "");
			this.openR = new OpenActionL(ACTIONOPENR, DOMAIN, "");
			this.openL = new OpenActionR(ACTIONOPENL, DOMAIN, "");

			// Pick Up Grain
//			this.pickUpGrain = new pickUpGrainAction(ACTIONGRAIN, DOMAIN, "");
			
//			// Use Oven
//			this.useOvenF = new useOvenActionF(ACTIONUSEOVENF, DOMAIN, "");
//			this.useOvenB = new useOvenActionB(ACTIONUSEOVENB, DOMAIN, "");
//			this.useOvenR = new useOvenActionR(ACTIONUSEOVENR, DOMAIN, "");
//			this.useOvenL = new useOvenActionL(ACTIONUSEOVENL, DOMAIN, "");
//			
//			// Jump
//			this.jumpF = new JumpActionF(ACTIONJUMPF, DOMAIN, "");
//			this.jumpB = new JumpActionB(ACTIONJUMPB, DOMAIN, "");
//			this.jumpR = new JumpActionR(ACTIONJUMPR, DOMAIN, "");
//			this.jumpL = new JumpActionL(ACTIONJUMPL, DOMAIN, "");
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
	
	/**
	 * Sets the first agent object in s to the specified x,y,z position.
	 * @param s the state with the agent whose position to set
	 * @param x the x position of the agent
	 * @param y the y position of the agent
	 * @param z the z position of the agent
	 */
	public static void setAgent(State s, int x, int y, int z, int numBlocks){
		ObjectInstance o = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
		
		o.setValue(ATTX, x);
		o.setValue(ATTY, y);
		o.setValue(ATTZ, z);
		o.setValue(ATTBLKNUM, numBlocks);
	}
	
	/**
	 * Sets the first goal object in s to the specified x,y,z position.
	 * @param s the state with the goal whose position to set
	 * @param x the x position of the goal
	 * @param y the y position of the goal
	 * @param z the z position of the goal
	 */
	public static void setGoal(State s, int x, int y, int z) {
		ObjectInstance o = s.getObjectsOfTrueClass(CLASSGOAL).get(0);
		
		o.setValue(ATTX, x);
		o.setValue(ATTY, y);
		o.setValue(ATTZ, z);
	}
	
	public static void setBlock(State s, int i, int x, int y, int z){
		ObjectInstance block = s.getObjectsOfTrueClass(CLASSBLOCK).get(i);
		block.setValue(ATTX, x);
		block.setValue(ATTY, y);
		block.setValue(ATTZ, z);
	}
	
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
		return (getBlockAt(st, x, y, z) == null);
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
		if ((!isCellEmpty(st, ax + 1, ay, az - 1)) && (!isCellEmpty(st, ax - 1, ay, az - 1))
				&& (!isCellEmpty(st, ax, ay + 1, az - 1)) && (!isCellEmpty(st, ax, ay - 1, az - 1))) {
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
		
		if (isCellEmpty(st, ax, ay - 1, az - 1) || isCellEmpty(st, ax, ay + 1, az - 1)
				|| isCellEmpty(st, ax - 1, ay, az - 1) || isCellEmpty(st, ax + 1, ay, az - 1)) {
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
			
			if (threshhold < 0.2) {
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
		
		if (nz - 1 > -1 && getBlockAt(s, nx, ny, nz - 1) == null && !isInstanceOfAt(s, nx, ny, nz - 1, CLASSLAVA)) {
			// There is no block under us, return.
			return;
		}
		else if (getBlockAt(s, nx, ny, nz) != null) {
			// There is a block where we are trying to move, return.
			return;
		}
		else {
			// Place we're moving is unobstructed and there is solid ground below us, move
			agent.setValue(ATTX, nx);
			agent.setValue(ATTY, ny);
			agent.setValue(ATTZ, nz);
		}

	}
	
	public static void jump(State st, int xd, int yd, int zd){
		
		ObjectInstance agent = st.getObjectsOfTrueClass(CLASSAGENT).get(0);
		int ax = agent.getDiscValForAttribute(ATTX);
		int ay = agent.getDiscValForAttribute(ATTY);
		int az = agent.getDiscValForAttribute(ATTZ);
		
		int nx = ax+xd;
		int ny = ay+yd;
		int nz = az+zd;
		
		if (nx < 0 || nx > MAXX || ny < 0 || ny > MAXY || nz < 0 || nz > MAXZ) {
			// Trying to move out of bounds, return.
			return;
		}
		
		if (isInstanceOfAt(st, nx, ny, nz, CLASSDOOR)) {
			ObjectInstance o = getInstanceOfAt(st, nx, ny, nz, CLASSDOOR);
			if (o.getDiscValForAttribute(ATTDOOROPEN) == 1) {
				// There is an open door where we are trying to move.
				agent.setValue(ATTX, nx);
				agent.setValue(ATTY, ny);
				agent.setValue(ATTZ, nz);
				return;
			}
			else {
				// Can't move here
//				System.out.println("CLOSED DOOR IN THE WAY");
				return;
			}

		}
		
		if (nz - 1 > -1 && getBlockAt(st, nx, ny, nz - 1) == null) {
			// There is no block under us, return.
			return;
		}
		else if (getBlockAt(st, nx, ny, nz) != null) {
			// There is a block where we are trying to move, return.
			return;
		}
		else {
			// Place we're moving is unobstructed and there is solid ground below us, move
			
			// Assuming only jump 2 units for now
			
			if (getBlockAt(st, ax + xd/2, ay + yd/2, az + zd/2) != null
					|| (getInstanceOfAt(st, ax + xd/2, ay + yd/2, az + zd/2, CLASSDOOR) != null
						&& getInstanceOfAt(st, ax + xd/2, ay + yd/2, az + zd/2, CLASSDOOR).getDiscValForAttribute(ATTDOOROPEN) == 0)) {
				// Something is in the way, can't jump
				return;
			}
			
			// Path is clear, jump!
			agent.setValue(ATTX, nx);
			agent.setValue(ATTY, ny);
			agent.setValue(ATTZ, nz);
		}

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
		
		// Get global coordinates of the loc to place the block
		int bx = ax+dx;
		int by = ay+dy;
		int bz = az+dz; // Try one below, first
		
		
		// Make sure we are placing a block in bounds
		if (bx < 0 || bx > MAXX || by < 0 || by > MAXY || bz < 0 || bz > MAXZ) {
			return;
		}
		
		// If block loc is empty (z-1 from bot), and the loc above is empty (i.e. we can "see" the bottom loc), place it.
		if (bz - 1 >= 0 && getBlockAt(s,bx,by,bz - 1) == null && getBlockAt(s,bx,by,bz) == null){
			
			addBlock(s, bx, by, bz - 1);
			
			// Remove the block from the agent's inventory
			agent.setValue(ATTBLKNUM, numAgentsBlocks - 1);
			numAgentsBlocks = numAgentsBlocks - 1;
		}
		// Now try placing one on agent's z level if it couldn't place one at z - 1
		else if (getBlockAt(s, bz, by, bz) == null && numAgentsBlocks > 0){
			
			// Place block
			addBlock(s, bx, by, bz);
			
			// Remove the block from the agent's inventory
			agent.setValue(ATTBLKNUM, numAgentsBlocks - 1);
		}
		
	}

	public static void destroy(State s, int dx, int dy, int dz) {
		
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
				removeBlock(s, bx, by, bz - 1);
			}

		}
		// Now try destroying one on agent's z level if it couldn't destroy one at z - 1
		else if (getBlockAt(s, bx, by, bz) != null){
			// Remove the block
			if (getBlockAt(s,bx,by,bz).getDiscValForAttribute("attDestroyable") == 1) {
				// Only destroy destroyable blocks
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

	public static void useoven(State s, int dx, int dy, int dz) {
		
		ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
		
		// Agent's global coordinates
		int ax = agent.getDiscValForAttribute(ATTX);
		int ay = agent.getDiscValForAttribute(ATTY);
		int az = agent.getDiscValForAttribute(ATTZ);
		
		// Get global coordinates of the loc to open the door
		int bx = ax+dx;
		int by = ay+dy;
		int bz = az+dz; // Try one below, first
		
		
		// Make sure we are trying to use an oven in bounds
		if (bx < 0 || bx > MAXX || by < 0 || by > MAXY || bz < 0 || bz > MAXZ) {
			return;
		}
		
		ObjectInstance oven = getInstanceOfAt(s, bx, by, bz, "oven");
		if ((oven != null) && (agent.getDiscValForAttribute(ATTAGHASGRAIN) == 1)) {
			// The block we're on "contain's grain", so "pick it up"
			agent.setValue(ATTAGHASGRAIN, 0);
			agent.setValue(ATTAGHASBREAD, 1);
		}
		else {
			return;
		}
		
		
	}
	
	public static class ForwardAction extends Action{

		public ForwardAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			move(st, 0, -1, 0);
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
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public static class JumpActionF extends Action{

		public JumpActionF(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			jump(st, 0, -2, 0);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}
		
	}
	public static class JumpActionB extends Action{

		public JumpActionB(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			jump(st, 0, 2, 0);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}		
	}
	public static class JumpActionR extends Action{

		public JumpActionR(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			jump(st, 2, 0, 0);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}
	}
	
	public static class JumpActionL extends Action{
		public JumpActionL(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			jump(st, -2, 0, 0);
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
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public class PlaceActionR extends Action{

		public PlaceActionR(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			place(st, -1, 0, 0);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public class PlaceActionL extends Action{

	public PlaceActionL(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			place(st, 1, 0, 0);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	
	public static class DestActionF extends Action{

		public DestActionF(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			destroy(st, 0, -1, 0);
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
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public static class OpenActionF extends Action{

		public OpenActionF(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
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
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public static class pickUpGrainAction extends Action{

		public pickUpGrainAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			
			pickup(st, 0, 0, -1);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public static class useOvenActionF extends Action{

		public useOvenActionF(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			useoven(st, 0, -1, 0);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}
	}
	
	public static class useOvenActionB extends Action{

		public useOvenActionB(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			useoven(st, 0, 1, 0);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public static class useOvenActionR extends Action{

		public useOvenActionR(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			useoven(st, -1, 0, 0);
//			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	public static class useOvenActionL extends Action{

	public useOvenActionL(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		protected State performActionHelper(State st, String[] params) {
			useoven(st, 1, 0, 0);
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
		
		public IsAgentXMore(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public boolean isTrue(State st, String[] params) {
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			
			//get the agent coordinates
			int ax = agent.getDiscValForAttribute(ATTX);
			
			int nx = Integer.parseInt(params[0]);
			
			return (ax > nx);
		}

		@Override
		public boolean isTrue(State s) {
			// TODO Auto-generated method stub
			return false;
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
		
		public IsAgentYMore(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public boolean isTrue(State st, String[] params) {
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			
			//get the agent coordinates
			int ay = agent.getDiscValForAttribute(ATTY);
			
			//get destination coordinates
			int ny = Integer.parseInt(params[1]);
			
			return (ay > ny);
		}

		@Override
		public boolean isTrue(State s) {
			// TODO Auto-generated method stub
			return false;
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
		

		public IsAtLocationPF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public IsAtLocationPF(String name, Domain domain, String[] parameterClasses, String[] params) {
			super(name, domain, parameterClasses);
			this.locX = Integer.parseInt(params[0]);
			this.locY = Integer.parseInt(params[1]);
			this.locZ = Integer.parseInt(params[2]);
		}
		
		@Override
		public boolean isTrue(State st) {
			// Version of isTrue that assumes paramaters were given in the construction of this prop func
			
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			
			//get the agent coordinates
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			int az = agent.getDiscValForAttribute(ATTZ);
			
			if(ax == this.locX && ay == this.locY && az == this.locZ){
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

		public IsAdjPlane(String name, Domain domain, String[] parameterClasses) {
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

			
			
//			 Works
//			if (!isAdjTrench(st, ax, ay, az)) {
//				return true;
//			}
			if ((ax == 0) || (ay == 0) || (ax == MAXX) || (ay == MAXY)) {
				return true;
			}
			if (isAdjPlane(st, ax, ay, az)) {
				return true;
			}
			else {
				return false;
			}
		}
		
	}
	
	public static class IsAdjTrench extends PropositionalFunction {

		public IsAdjTrench(String name, Domain domain, String[] parameterClasses) {
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

			
			if (isAdjTrench(st, ax, ay, az)) {
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

		public IsAdjDstableWall(String name, Domain domain, String[] parameterClasses) {
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

			
			if (isAdjDstableWall(st, ax, ay, az)) {
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
		exp.addActionShortHand("bbf", ACTIONUSEOVENF);
		exp.addActionShortHand("bbb", ACTIONUSEOVENB);
		exp.addActionShortHand("bbl", ACTIONUSEOVENL);
		exp.addActionShortHand("bbr", ACTIONUSEOVENR);
		
		exp.addActionShortHand("jf", ACTIONJUMPF);
		exp.addActionShortHand("jb", ACTIONJUMPB);
		exp.addActionShortHand("jl", ACTIONJUMPL);
		exp.addActionShortHand("jr", ACTIONJUMPR);
		
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
