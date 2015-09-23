package burlap.tutorials.bd;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.*;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.*;
import burlap.oomdp.singleagent.common.SimpleAction;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.StateRenderLayer;
import burlap.oomdp.visualizer.StaticPainter;
import burlap.oomdp.visualizer.Visualizer;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;


public class ExampleGridWorld implements DomainGenerator{

	public static final String ATTX = "x";
	public static final String ATTY = "y";

	public static final String CLASSAGENT = "agent";
	public static final String CLASSLOCATION = "location";

	public static final String ACTIONNORTH = "north";
	public static final String ACTIONSOUTH = "south";
	public static final String ACTIONEAST = "east";
	public static final String ACTIONWEST = "west";

	public static final String PFAT = "at";


	//ordered so first dimension is x
	protected int [][] map = new int[][]{
			{0,0,0,0,0,1,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,1,0,0,0,0,0},
			{0,0,0,0,0,1,0,0,0,0,0},
			{0,0,0,0,0,1,0,0,0,0,0},
			{1,0,1,1,1,1,1,1,0,1,1},
			{0,0,0,0,1,0,0,0,0,0,0},
			{0,0,0,0,1,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,1,0,0,0,0,0,0},
			{0,0,0,0,1,0,0,0,0,0,0},
	};

	@Override
	public Domain generateDomain() {

		SADomain domain = new SADomain();

		Attribute xatt = new Attribute(domain, ATTX, Attribute.AttributeType.INT);
		xatt.setLims(0, 10);

		Attribute yatt = new Attribute(domain, ATTY, Attribute.AttributeType.INT);
		yatt.setLims(0, 10);

		ObjectClass agentClass = new ObjectClass(domain, CLASSAGENT);
		agentClass.addAttribute(xatt);
		agentClass.addAttribute(yatt);

		ObjectClass locationClass = new ObjectClass(domain, CLASSLOCATION);
		locationClass.addAttribute(xatt);
		locationClass.addAttribute(yatt);

		new Movement(ACTIONNORTH, domain, 0);
		new Movement(ACTIONSOUTH, domain, 1);
		new Movement(ACTIONEAST, domain, 2);
		new Movement(ACTIONWEST, domain, 3);

		new AtLocation(domain);

		return domain;
	}

	public static State getExampleState(Domain domain){
		State s = new MutableState();
		ObjectInstance agent = new MutableObjectInstance(domain.getObjectClass(CLASSAGENT), "agent0");
		agent.setValue(ATTX, 0);
		agent.setValue(ATTY, 0);

		ObjectInstance location = new MutableObjectInstance(domain.getObjectClass(CLASSLOCATION), "location0");
		location.setValue(ATTX, 10);
		location.setValue(ATTY, 10);

		s.addObject(agent);
		s.addObject(location);

		return s;
	}

	public StateRenderLayer getStateRenderLayer(){
		StateRenderLayer rl = new StateRenderLayer();
		rl.addStaticPainter(new WallPainter());
		rl.addObjectClassPainter(CLASSLOCATION, new LocationPainter());
		rl.addObjectClassPainter(CLASSAGENT, new AgentPainter());


		return rl;
	}

	public Visualizer getVisualizer(){
		return new Visualizer(this.getStateRenderLayer());
	}


	protected class Movement extends SimpleAction implements FullActionModel {

		//0: north; 1: south; 2:east; 3: west
		protected double [] directionProbs = new double[4];


		public Movement(String actionName, Domain domain, int direction){
			super(actionName, domain);
			for(int i = 0; i < 4; i++){
				if(i == direction){
					directionProbs[i] = 0.8;
				}
				else{
					directionProbs[i] = 0.2/3.;
				}
			}
		}

		@Override
		protected State performActionHelper(State s, GroundedAction groundedAction) {
			//get agent and current position
			ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
			int curX = agent.getIntValForAttribute(ATTX);
			int curY = agent.getIntValForAttribute(ATTY);

			//sample directon with random roll
			double r = Math.random();
			double sumProb = 0.;
			int dir = 0;
			for(int i = 0; i < this.directionProbs.length; i++){
				sumProb += this.directionProbs[i];
				if(r < sumProb){
					dir = i;
					break; //found direction
				}
			}

			//get resulting position
			int [] newPos = this.moveResult(curX, curY, dir);

			//set the new position
			agent.setValue(ATTX, newPos[0]);
			agent.setValue(ATTY, newPos[1]);

			//return the state we just modified
			return s;
		}

		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
			//get agent and current position
			ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
			int curX = agent.getIntValForAttribute(ATTX);
			int curY = agent.getIntValForAttribute(ATTY);

			List<TransitionProbability> tps = new ArrayList<TransitionProbability>(4);
			TransitionProbability noChangeTransition = null;
			for(int i = 0; i < this.directionProbs.length; i++){
				int [] newPos = this.moveResult(curX, curY, i);
				if(newPos[0] != curX || newPos[1] != curY){
					//new possible outcome
					State ns = s.copy();
					ObjectInstance nagent = ns.getFirstObjectOfClass(CLASSAGENT);
					nagent.setValue(ATTX, newPos[0]);
					nagent.setValue(ATTY, newPos[1]);

					//create transition probability object and add to our list of outcomes
					tps.add(new TransitionProbability(ns, this.directionProbs[i]));
				}
				else{
					//this direction didn't lead anywhere new
					//if there are existing possible directions
					//that wouldn't lead anywhere, aggregate with them
					if(noChangeTransition != null){
						noChangeTransition.p += this.directionProbs[i];
					}
					else{
						//otherwise create this new state and transition
						noChangeTransition = new TransitionProbability(s.copy(),
								this.directionProbs[i]);
						tps.add(noChangeTransition);
					}
				}
			}


			return tps;
		}

		protected int [] moveResult(int curX, int curY, int direction){

			//first get change in x and y from direction using 0: north; 1: south; 2:east; 3: west
			int xdelta = 0;
			int ydelta = 0;
			if(direction == 0){
				ydelta = 1;
			}
			else if(direction == 1){
				ydelta = -1;
			}
			else if(direction == 2){
				xdelta = 1;
			}
			else{
				xdelta = -1;
			}

			int nx = curX + xdelta;
			int ny = curY + ydelta;

			int width = ExampleGridWorld.this.map.length;
			int height = ExampleGridWorld.this.map[0].length;

			//make sure new position is valid (not a wall or off bounds)
			if(nx < 0 || nx >= width || ny < 0 || ny >= height ||
					ExampleGridWorld.this.map[nx][ny] == 1){
				nx = curX;
				ny = curY;
			}


			return new int[]{nx,ny};

		}


	}


	protected class AtLocation extends PropositionalFunction {

		public AtLocation(Domain domain){
			super(PFAT, domain, new String []{CLASSAGENT,CLASSLOCATION});
		}

		@Override
		public boolean isTrue(State s, String... params) {
			ObjectInstance agent = s.getObject(params[0]);
			ObjectInstance location = s.getObject(params[1]);

			int ax = agent.getIntValForAttribute(ATTX);
			int ay = agent.getIntValForAttribute(ATTY);

			int lx = location.getIntValForAttribute(ATTX);
			int ly = location.getIntValForAttribute(ATTY);

			return ax == lx && ay == ly;
		}



	}



	public class WallPainter implements StaticPainter {

		@Override
		public void paint(Graphics2D g2, State s, float cWidth, float cHeight) {

			//walls will be filled in black
			g2.setColor(Color.BLACK);

			//set up floats for the width and height of our domain
			float fWidth = ExampleGridWorld.this.map.length;
			float fHeight = ExampleGridWorld.this.map[0].length;

			//determine the width of a single cell
			//on our canvas such that the whole map can be painted
			float width = cWidth / fWidth;
			float height = cHeight / fHeight;

			//pass through each cell of our map and if it's a wall, paint a black rectangle on our
			//cavas of dimension widthxheight
			for(int i = 0; i < ExampleGridWorld.this.map.length; i++){
				for(int j = 0; j < ExampleGridWorld.this.map[0].length; j++){

					//is there a wall here?
					if(ExampleGridWorld.this.map[i][j] == 1){

						//left coordinate of cell on our canvas
						float rx = i*width;

						//top coordinate of cell on our canvas
						//coordinate system adjustment because the java canvas
						//origin is in the top left instead of the bottom right
						float ry = cHeight - height - j*height;

						//paint the rectangle
						g2.fill(new Rectangle2D.Float(rx, ry, width, height));

					}


				}
			}

		}


	}


	public class AgentPainter implements ObjectPainter{

		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob,
								float cWidth, float cHeight) {

			//agent will be filled in gray
			g2.setColor(Color.GRAY);

			//set up floats for the width and height of our domain
			float fWidth = ExampleGridWorld.this.map.length;
			float fHeight = ExampleGridWorld.this.map[0].length;

			//determine the width of a single cell on our canvas
			//such that the whole map can be painted
			float width = cWidth / fWidth;
			float height = cHeight / fHeight;

			int ax = ob.getIntValForAttribute(ATTX);
			int ay = ob.getIntValForAttribute(ATTY);

			//left coordinate of cell on our canvas
			float rx = ax*width;

			//top coordinate of cell on our canvas
			//coordinate system adjustment because the java canvas
			//origin is in the top left instead of the bottom right
			float ry = cHeight - height - ay*height;

			//paint the rectangle
			g2.fill(new Ellipse2D.Float(rx, ry, width, height));


		}



	}

	public class LocationPainter implements ObjectPainter {

		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob,
								float cWidth, float cHeight) {

			//agent will be filled in blue
			g2.setColor(Color.BLUE);

			//set up floats for the width and height of our domain
			float fWidth = ExampleGridWorld.this.map.length;
			float fHeight = ExampleGridWorld.this.map[0].length;

			//determine the width of a single cell on our canvas
			//such that the whole map can be painted
			float width = cWidth / fWidth;
			float height = cHeight / fHeight;

			int ax = ob.getIntValForAttribute(ATTX);
			int ay = ob.getIntValForAttribute(ATTY);

			//left coordinate of cell on our canvas
			float rx = ax*width;

			//top coordinate of cell on our canvas
			//coordinate system adjustment because the java canvas
			//origin is in the top left instead of the bottom right
			float ry = cHeight - height - ay*height;

			//paint the rectangle
			g2.fill(new Rectangle2D.Float(rx, ry, width, height));


		}



	}


	public static class ExampleRF implements RewardFunction {

		int goalX;
		int goalY;

		public ExampleRF(int goalX, int goalY){
			this.goalX = goalX;
			this.goalY = goalY;
		}

		@Override
		public double reward(State s, GroundedAction a, State sprime) {

			//get location of agent in next state
			ObjectInstance agent = sprime.getFirstObjectOfClass(CLASSAGENT);
			int ax = agent.getIntValForAttribute(ATTX);
			int ay = agent.getIntValForAttribute(ATTY);

			//are they at goal location?
			if(ax == this.goalX && ay == this.goalY){
				return 100.;
			}

			return -1;
		}


	}

	public static class ExampleTF implements TerminalFunction {

		int goalX;
		int goalY;

		public ExampleTF(int goalX, int goalY){
			this.goalX = goalX;
			this.goalY = goalY;
		}

		@Override
		public boolean isTerminal(State s) {

			//get location of agent in next state
			ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
			int ax = agent.getIntValForAttribute(ATTX);
			int ay = agent.getIntValForAttribute(ATTY);

			//are they at goal location?
			if(ax == this.goalX && ay == this.goalY){
				return true;
			}

			return false;
		}



	}



	public static void main(String [] args){

		ExampleGridWorld gen = new ExampleGridWorld();
		Domain domain = gen.generateDomain();

		State initialState = ExampleGridWorld.getExampleState(domain);

		RewardFunction rf = new ExampleRF(10, 10);
		TerminalFunction tf = new ExampleTF(10, 10);

		SimulatedEnvironment env = new SimulatedEnvironment(domain, rf, tf, initialState);

		//TerminalExplorer exp = new TerminalExplorer(domain, env);
		//exp.explore();


		Visualizer v = gen.getVisualizer();
		VisualExplorer exp = new VisualExplorer(domain, env, v);

		exp.addKeyAction("w", ACTIONNORTH);
		exp.addKeyAction("s", ACTIONSOUTH);
		exp.addKeyAction("d", ACTIONEAST);
		exp.addKeyAction("a", ACTIONWEST);

		exp.initGUI();


	}

}
