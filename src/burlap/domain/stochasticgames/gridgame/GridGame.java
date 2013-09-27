package burlap.domain.stochasticgames.gridgame;

import java.util.List;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.stocashticgames.JointActionModel;
import burlap.oomdp.stocashticgames.SGDomain;
import burlap.oomdp.stocashticgames.SingleAction;
import burlap.oomdp.stocashticgames.common.UniversalSingleAction;
import burlap.oomdp.stocashticgames.explorers.SGVisualExplorer;
import burlap.oomdp.visualizer.Visualizer;


public class GridGame implements DomainGenerator {

	public static final String				ATTX = "x";
	public static final String				ATTY = "y";
	public static final String				ATTPN = "playerNum";
	public static final String				ATTGT = "gt";
	public static final String				ATTE1 = "end1";
	public static final String				ATTE2 = "end2";
	public static final String				ATTP = "pos";
	public static final String				ATTWT = "wallType";
	
	public static final String				CLASSAGENT = "agent";
	public static final String				CLASSGOAL = "goal";
	public static final String				CLASSDIMHWALL = "dimensionlessHorizontalWall";
	public static final String				CLASSDIMVWALL = "dimensionlessVerticalWall";
	
	public static final String				ACTIONNORTH = "north";
	public static final String				ACTIONSOUTH = "south";
	public static final String				ACTIONEAST = "east";
	public static final String				ACTIONWEST = "west";
	public static final String				ACTIONNOOP = "noop";
	
	public static final String				PFINUGOAL = "agentInUniversalGoal";
	public static final String				PFINPGOAL = "agentInPersonalGoal";
	
	
	
	protected int 							maxDim = 50;
	protected int 							maxPlyrs = 10;
	protected int 							maxGT = 20;
	protected int 							maxWT = 3;
	

	
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
		
		exp.setJAC("c"); //press c to execute the constructed joint aciton
		
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
	
	
	
	public int getMaxDim() {
		return maxDim;
	}

	public void setMaxDim(int maxDim) {
		this.maxDim = maxDim;
	}

	public int getMaxPlyrs() {
		return maxPlyrs;
	}

	public void setMaxPlyrs(int maxPlyrs) {
		this.maxPlyrs = maxPlyrs;
	}

	public int getMaxGT() {
		return maxGT;
	}

	public void setMaxGT(int maxGT) {
		this.maxGT = maxGT;
	}

	public int getMaxWT() {
		return maxWT;
	}

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
		
		
		PropositionalFunction aug = new AgentInUGoal(PFINUGOAL, domain, new String[]{CLASSAGENT});
		PropositionalFunction apg = new AgentInPGoal(PFINPGOAL, domain, new String[]{CLASSAGENT});
		
		
		return domain;
	}

	
	
	
	public static State getCleanState(Domain d, int na, int ng, int nhw, int nvw, int width, int height){
		
		State s = new State();
		addNObjects(d, s, CLASSGOAL, ng);
		addNObjects(d, s, CLASSAGENT, na);
		addNObjects(d, s, CLASSDIMHWALL, nhw);
		addNObjects(d, s, CLASSDIMVWALL, nvw);
		
		setBoundaryWalls(s, width, height);
		
		
		return s;
	}
	
	
	public static void addNObjects(Domain d, State s, String className, int n){
		for(int i = 0; i < n; i++){
			ObjectInstance o = new ObjectInstance(d.getObjectClass(className), className+i);
			s.addObject(o);
		}
	}
	
	
	public static void setAgent(State s, int i, int x, int y, int pn){
		ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(i);
		agent.setValue(ATTX, x);
		agent.setValue(ATTY, y);
		agent.setValue(ATTPN, pn);
	}
	
	public static void setGoal(State s, int i, int x, int y, int gt){
		ObjectInstance goal = s.getObjectsOfTrueClass(CLASSGOAL).get(i);
		goal.setValue(ATTX, x);
		goal.setValue(ATTY, y);
		goal.setValue(ATTGT, gt);
	}
	
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
	
	
	public static void setWallInstance(ObjectInstance w, int p, int e1, int e2, int wt){
		w.setValue(ATTP, p);
		w.setValue(ATTE1, e1);
		w.setValue(ATTE2, e2);
		w.setValue(ATTWT, wt);
	}

	
	public static void setVerticalWall(State s, int i, int p, int e1, int e2, int wt){
		setWallInstance(s.getObjectsOfTrueClass(CLASSDIMVWALL).get(i), p, e1, e2, wt);
	}
	
	public static void setHorizontalWall(State s, int i, int p, int e1, int e2, int wt){
		setWallInstance(s.getObjectsOfTrueClass(CLASSDIMHWALL).get(i), p, e1, e2, wt);
	}
	
	
	
	
	
	
	static class AgentInUGoal extends PropositionalFunction{

		public AgentInUGoal(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
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
	
	
	static class AgentInPGoal extends PropositionalFunction{

		public AgentInPGoal(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
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
