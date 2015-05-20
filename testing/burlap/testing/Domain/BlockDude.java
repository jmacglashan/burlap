package burlap.testing.Domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.states.ImmutableState;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;

public class BlockDude implements DomainGenerator {

	
	public static final String							ATTX = "x";
	public static final String							ATTY = "y";
	public static final String							ATTDIR = "dir";
	public static final String							ATTHOLD = "holding";
	public static final String							ATTHEIGHT = "height";
	
	public static final String							CLASSAGENT = "agent";
	public static final String							CLASSBLOCK = "block";
	public static final String							CLASSPLATFORM = "platform";
	public static final String							CLASSEXIT = "exit";
	
	public static final String							ACTIONUP = "up";
	public static final String							ACTIONEAST = "east";
	public static final String							ACTIONWEST = "west";
	public static final String							ACTIONPICKUP = "pickup";
	public static final String							ACTIONPUTDOWN = "putdown";
	
	public static final String							PFHOLDINGBLOCK = "holdingBlock";
	public static final String							PFATEXIT = "atExit";
	
	
	public int											minx = 0;
	public int											maxx = 25;
	public int											miny = 0;
	public int											maxy = 25;
	
	
	public static boolean								useSemiDeep = false;
	
	public BlockDude(){
		//do nothing
	}
	
	public BlockDude(int maxx, int maxy){
		this.maxx = maxx;
		this.maxy = maxy;
	}
	
	public BlockDude(int minx, int maxx, int miny, int maxy){
		
		this.minx = minx;
		this.miny = miny;
		
		this.maxx = maxx;
		this.maxy = maxy;
	}
	
	@Override
	public Domain generateDomain() {
		
		Domain domain = new SADomain();
		
		//setup attributes
		Attribute xAtt = new Attribute(domain, ATTX, Attribute.AttributeType.DISC);
		xAtt.setDiscValuesForRange(minx, maxx, 1);
		
		Attribute yAtt = new Attribute(domain, ATTY, Attribute.AttributeType.DISC);
		yAtt.setDiscValuesForRange(miny, miny, 1);
		
		Attribute dirAtt = new Attribute(domain, ATTDIR, Attribute.AttributeType.DISC);
		dirAtt.setDiscValuesForRange(0, 1, 1);
		
		Attribute holdAtt = new Attribute(domain, ATTHOLD, Attribute.AttributeType.DISC);
		holdAtt.setDiscValuesForRange(0, 1, 1);
		
		Attribute heightAtt = new Attribute(domain, ATTHEIGHT, Attribute.AttributeType.DISC);
		heightAtt.setDiscValuesForRange(miny, maxy, 1);
		
		
		
		//setup object classes
		ObjectClass aclass = new ObjectClass(domain, CLASSAGENT);
		aclass.addAttribute(xAtt);
		aclass.addAttribute(yAtt);
		aclass.addAttribute(dirAtt);
		aclass.addAttribute(holdAtt);
		
		ObjectClass bclass = new ObjectClass(domain, CLASSBLOCK);
		bclass.addAttribute(xAtt);
		bclass.addAttribute(yAtt);
		
		ObjectClass pclass = new ObjectClass(domain, CLASSPLATFORM);
		pclass.addAttribute(xAtt);
		pclass.addAttribute(heightAtt);
		
		ObjectClass eclass = new ObjectClass(domain, CLASSEXIT);
		eclass.addAttribute(xAtt);
		eclass.addAttribute(yAtt);
		
		
		//setup actions
		Action upAction = new UpAction(ACTIONUP, domain, "");
		Action eastAction = new EastAction(ACTIONEAST, domain, "");
		Action westAction = new WestAction(ACTIONWEST, domain, "");
		Action pickupAction = new PickupAction(ACTIONPICKUP, domain, "");
		Action putdownAction = new PutdownAction(ACTIONPUTDOWN, domain, "");
		
		
		//setup propositional functions
		PropositionalFunction holdingBlockPF = new HoldingBlockPF(PFHOLDINGBLOCK, domain, new String[]{CLASSAGENT, CLASSBLOCK});
		PropositionalFunction atExitPF = new AtExitPF(PFATEXIT, domain, new String[]{CLASSAGENT, CLASSEXIT});
		
		return domain;
	}

	
	
	public static State getCleanState(Domain domain, List <Integer> platformX, List <Integer> platformH, int nb){
		
		List<ObjectInstance> objects = new ArrayList<ObjectInstance>();
		//start by creating the platform objects
		for(int i = 0; i < platformX.size(); i++){
			int x = platformX.get(i);
			int h = platformH.get(i);
			
			ObjectInstance plat = new MutableObjectInstance(domain.getObjectClass(CLASSPLATFORM), CLASSPLATFORM+x);
			plat = plat.setValue(ATTX, x);
			plat = plat.setValue(ATTHEIGHT, h);
			
			objects.add(plat);
			
		}
		
		//create n blocks
		for(int i = 0; i < nb; i++){
			objects.add(new MutableObjectInstance(domain.getObjectClass(CLASSBLOCK), CLASSBLOCK+i));
		}
		
		//create exit
		objects.add(new MutableObjectInstance(domain.getObjectClass(CLASSEXIT), CLASSEXIT+0));
		
		//create agent
		objects.add(new MutableObjectInstance(domain.getObjectClass(CLASSAGENT), CLASSAGENT+0));
		
		
		return new ImmutableState(objects);
		
	}
	
	
	public static State setAgent(State s, int x, int y, int dir, int holding){
		ImmutableState immState = (ImmutableState)s;
		ObjectInstance agent = s.getObjectsOfClass(CLASSAGENT).get(0);
		
		ObjectInstance newAgent = agent.setValue(ATTX, x);
		newAgent = newAgent.setValue(ATTY, y);
		newAgent = newAgent.setValue(ATTDIR, dir);
		newAgent = newAgent.setValue(ATTHOLD, holding);
		
		return immState.replaceObject(agent, newAgent);
	}
	
	
	public static State setExit(State s, int x, int y){
		ImmutableState immState = (ImmutableState)s;
		
		ObjectInstance exit = s.getObjectsOfClass(CLASSEXIT).get(0);
		ObjectInstance newExit = exit.setValue(ATTX, x);
		newExit = newExit.setValue(ATTY, y);
		
		return immState.replaceObject(exit, newExit);
	}
	
	public static State setBlock(State s, int i, int x, int y){
		ImmutableState immState = (ImmutableState)s;
		
		ObjectInstance block = s.getObjectsOfClass(CLASSBLOCK).get(i);
		ObjectInstance newBlock = block.setValue(ATTX, x);
		newBlock = newBlock.setValue(ATTY, y);
		
		return immState.replaceObject(block, newBlock);
	}
	
	
	
	public static State moveHorizontally(State s, int dx){
		
		ObjectInstance agent = s.getObjectsOfClass(CLASSAGENT).get(0);
		ObjectInstance newAgent = agent;
		//always set direction
		if(dx > 0){
			newAgent = agent.setValue(ATTDIR, 1);
		}
		else{
			newAgent = agent.setValue(ATTDIR, 0);
		}
		
		
		int ax = agent.getIntValForAttribute(ATTX);
		int ay = agent.getIntValForAttribute(ATTY);
		
		int nx = ax+dx;
		
		int heightAtNX = totalHeightAtXPos(s, nx);
		
		ImmutableState immState = (ImmutableState)s;
		
		//can only move if new position is below agent height
		if(heightAtNX >= ay){
			return immState.replaceObject(agent, newAgent); //do nothing; walled off
		}
		
		int ny = heightAtNX + 1; //stand on top of stack
		
		newAgent = newAgent.setValue(ATTX, nx);
		newAgent = newAgent.setValue(ATTY, ny);
		
		State newState = immState.replaceObject(agent, newAgent);
		
		return moveCarriedBlockToNewAgentPosition(newState, newAgent, ax, ay, nx, ny);
		
		
	}
	
	
	public static State moveUp(State s){
		
		ObjectInstance agent = s.getObjectsOfClass(CLASSAGENT).get(0);
		
		int ax = agent.getIntValForAttribute(ATTX);
		int ay = agent.getIntValForAttribute(ATTY);
		int dir = agent.getIntValForAttribute(ATTDIR);
		
		if(dir == 0){
			dir = -1;
		}
		
		int nx = ax+dir;
		int ny = ay+1;
		
		int heightAtNX = totalHeightAtXPos(s, nx);
		
		//in order to move up, the height of world in new x position must be at the same current agent position
		if(heightAtNX != ay){
			return s; //not a viable move up condition, so do nothing
		}
		
		ObjectInstance newAgent = agent.setValue(ATTX, nx);
		newAgent = newAgent.setValue(ATTY, ny);
		
		ImmutableState immState = (ImmutableState)s;
		State nextState = immState.replaceObject(agent, newAgent);
		return moveCarriedBlockToNewAgentPosition(nextState, newAgent, ax, ay, nx, ny);
		
		
	}
	
	public static State pickupBlock(State s){
		
		ObjectInstance agent = s.getObjectsOfClass(CLASSAGENT).get(0);
		
		int holding = agent.getIntValForAttribute(ATTHOLD);
		if(holding == 1){
			return s; //already holding a block
		}
		
		int ax = agent.getIntValForAttribute(ATTX);
		int ay = agent.getIntValForAttribute(ATTY);
		int dir = agent.getIntValForAttribute(ATTDIR);
		
		if(dir == 0){
			dir = -1;
		}
		
		//can only pick up blocks one unit away in agent facing direction and at same height as agent
		int bx = ax+dir;
		ObjectInstance block = getBlockAt(s, bx, ay);
		
		if(block != null){
			
			//make sure that block is the top of the world, otherwise something is stacked above it and you cannot pick it up
			int mxh = totalHeightAtXPos(s, bx);
			if(mxh > block.getIntValForAttribute(ATTY)){
				return s;
			}
			
			ObjectInstance newBlock = block.setValue(ATTX, ax);
			newBlock = newBlock.setValue(ATTY, ay+1);
			
			ObjectInstance newAgent = agent.setValue(ATTHOLD, 1);
			
			ImmutableState immState = (ImmutableState)s;
			return immState.replaceAllObjects(Arrays.asList(block, agent), Arrays.asList(newBlock, newAgent));
		}
		
		return s;
	}
	
	public static State putdownBlock(State s){
		
		ObjectInstance agent = s.getObjectsOfClass(CLASSAGENT).get(0);
		
		int holding = agent.getIntValForAttribute(ATTHOLD);
		if(holding == 0){
			return s; //not holding a block
		}
		
		int ax = agent.getIntValForAttribute(ATTX);
		int ay = agent.getIntValForAttribute(ATTY);
		int dir = agent.getIntValForAttribute(ATTDIR);
		
		if(dir == 0){
			dir = -1;
		}
		
		
		int nx = ax + dir;
		
		int heightAtNX = totalHeightAtXPos(s, nx);
		if(heightAtNX > ay){
			return s; //cannot drop block if walled off from throw position
		}
		
		ObjectInstance block = getBlockAt(s, ax, ay+1); //carried block is one unit above agent
		ObjectInstance newBlock = block.setValue(ATTX, nx);
		newBlock = newBlock.setValue(ATTY, heightAtNX+1); //stacked on top of this position
		
		ObjectInstance newAgent = agent.setValue(ATTHOLD, 0);
		
		ImmutableState immState = (ImmutableState)s;
		return immState.replaceAllObjects(Arrays.asList(block, agent), Arrays.asList(newBlock, newAgent));
	}
	
	
	private static State moveCarriedBlockToNewAgentPosition(State s, ObjectInstance agent, int ax, int ay, int nx, int ny){
		int holding = agent.getIntValForAttribute(ATTHOLD);
		ImmutableState immState = (ImmutableState)s;
		if(holding == 1){
			//then move the box being carried too
			ObjectInstance carriedBlock = getBlockAt(s, ax, ay+1); //carried block is one unit above agent
			ObjectInstance newBlock = carriedBlock.setValue(ATTX, nx);
			newBlock = newBlock.setValue(ATTY, ny+1);
			immState = immState.replaceObject(carriedBlock, newBlock);
		}
		return immState;
	}
	
	private static ObjectInstance getBlockAt(State s, int x, int y){
		
		List<ObjectInstance> blocks = s.getObjectsOfClass(CLASSBLOCK);
		for(ObjectInstance block : blocks){
			int bx = block.getIntValForAttribute(ATTX);
			int by = block.getIntValForAttribute(ATTY);
			if(bx == x && by == y){
				return block;
			}
		}
		
		return null;
		
	}
	
	private static int totalHeightAtXPos(State s, int x){
		
		//first see if there are any blocks at this x pos and if so, what highest one is
		List<ObjectInstance> blocks = s.getObjectsOfClass(CLASSBLOCK);
		int maxBlock = -1;
		for(ObjectInstance block : blocks){
			int bx = block.getIntValForAttribute(ATTX);
			if(bx != x){
				continue;
			}
			int by = block.getIntValForAttribute(ATTY);
			if(by > maxBlock){
				maxBlock = by;
			}
			
		}
		
		if(maxBlock > -1){
			return maxBlock; //there are stacked blocks here which must be the highest point
		}
		

		
		//get platform at pos x
		ObjectInstance plat = s.getObject(CLASSPLATFORM+x);
		
		if(plat != null){
			return plat.getIntValForAttribute(ATTHEIGHT);
		}
		
		return -1;
	}
	
	public class UpAction extends Action{

		public UpAction(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public UpAction(String name, Domain domain, String [] parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		@Override
		protected State performActionHelper(State st, String[] params) {
			return moveUp(st);
		}
		
		
	}
	
	
	public class EastAction extends Action{

		public EastAction(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public EastAction(String name, Domain domain, String [] parameterClasses){
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			return moveHorizontally(st, 1);
		}
		
		
	}
	
	
	public class WestAction extends Action{

		public WestAction(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public WestAction(String name, Domain domain, String [] parameterClasses){
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			return moveHorizontally(st, -1);
		}
		
		
	}
	
	
	public class PickupAction extends Action{

		public PickupAction(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public PickupAction(String name, Domain domain, String [] parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		@Override
		protected State performActionHelper(State st, String[] params) {
			return pickupBlock(st);
		}
		
		
	}
	
	
	
	public class PutdownAction extends Action{

		public PutdownAction(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public PutdownAction(String name, Domain domain, String [] parameterClasses){
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			return putdownBlock(st);
		}
		
		
	}
	
	
	
	public class HoldingBlockPF extends PropositionalFunction{

		public HoldingBlockPF(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public HoldingBlockPF(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			
			ObjectInstance agent = st.getObject(params[0]);
			ObjectInstance block = st.getObject(params[1]);
			
			int ax = agent.getIntValForAttribute(ATTX);
			int ay = agent.getIntValForAttribute(ATTY);
			int ah = agent.getIntValForAttribute(ATTHOLD);
			
			int bx = block.getIntValForAttribute(ATTX);
			int by = block.getIntValForAttribute(ATTY);
			
			if(ax == bx && ay == by-1 && ah == 1){
				return true;
			}
			
			return false;
		}
		
		
		
	}
	
	
	public class AtExitPF extends PropositionalFunction{

		public AtExitPF(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public AtExitPF(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			
			ObjectInstance agent = st.getObject(params[0]);
			ObjectInstance exit = st.getObject(params[1]);
			
			int ax = agent.getIntValForAttribute(ATTX);
			int ay = agent.getIntValForAttribute(ATTY);
			
			
			int ex = exit.getIntValForAttribute(ATTX);
			int ey = exit.getIntValForAttribute(ATTY);
			
			if(ax == ex && ay == ey){
				return true;
			}
			
			return false;
		}
		
		
		
	}

}

