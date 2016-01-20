package burlap.domain.singleagent.minecraft;

import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.domain.singleagent.minecraft.MinecraftDomain;

public class MCPF {

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
	
	public static class AgentHasGoldBlockPF extends PropositionalFunction{

		public AgentHasGoldBlockPF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			return (agent.getDiscValForAttribute(ATTAGHASGOLDBLOCK) == 1);
		}
		@Override
		public boolean isTrue(State st) {
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			return (agent.getDiscValForAttribute(ATTAGHASGOLDBLOCK) == 1);
		}
		
	}
	
	public static class AgentHasGoldOrePF extends PropositionalFunction{

		public AgentHasGoldOrePF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			return (agent.getDiscValForAttribute(ATTAGHASGOLDORE) == 1);
		}
		@Override
		public boolean isTrue(State st) {
			ObjectInstance agent = st.getObject(CLASSAGENT + "0");
			return (agent.getDiscValForAttribute(ATTAGHASGOLDORE) == 1);
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
	public static class IsAdjFurnace extends PropositionalFunction {

		public IsAdjFurnace(String name, Domain domain, String[] parameterClasses) {
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

			
			if (isAdjFurnace(st, ax, ay, az)) {
				return true;
			}
			else {
				return false;
			}
		}
		
	}
	
	public static class IsOnGoldOre extends PropositionalFunction {

		public IsOnGoldOre(String name, Domain domain, String[] parameterClasses) {
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

			
			if (getBlockAt(st, ax, ay, az - 1) != null && getBlockAt(st, ax, ay, az - 1).getDiscValForAttribute(ATTGOLDORE) == 1) {
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
	
}
