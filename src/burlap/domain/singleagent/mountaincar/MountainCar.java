package burlap.domain.singleagent.mountaincar;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.SimpleAction;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;


/**
 * A domain generator for the classic mountain car domain with default dynamics follow those implemented by Singh and Sutton [1].
 * In this domain you can change the parameters for min/max position and velocity, the scale of the cosine curve on which the car travels,
 * the force of gravity, acceleration, and the amount of time that elapses between simulation/decision steps. If you
 * previously generated a {@link burlap.oomdp.core.Domain}, changing the physics parameters of this {@link burlap.oomdp.auxiliary.DomainGenerator} will
 * not affect how the previously generated domain behaves, only future generated ones.
 * 
 * <p>
 * 1. Singh, Satinder P., and Richard S. Sutton. "Reinforcement learning with replacing eligibility traces." Machine learning 22.1-3 (1996): 123-158.
 * 
 * @author James MacGlashan
 *
 */
public class MountainCar implements DomainGenerator {

	
	/**
	 * A constant for the name of the x attribute
	 */
	public static final String				ATTX = "xAtt";
	
	/**
	 * A constant for the name of the velocity attribute
	 */
	public static final String				ATTV = "vAtt";
	
	
	/**
	 * A constant for the name of the agent class
	 */
	public static final String				CLASSAGENT = "agent";
	
	
	/**
	 * A constant for the name of the forward action
	 */
	public static final String				ACTIONFORWARD = "forward";
	
	/**
	 * A constant for the name of the backwards action
	 */
	public static final String				ACTIONBACKWARDS = "backwards";
	
	
	/**
	 * A constant for the name of the coast action
	 */
	public static final String				ACTIONCOAST = "coast";


	/**
	 * The physics parameters for mountain car.
	 */
	public MCPhysicsParams physParams = new MCPhysicsParams();


	public static class MCPhysicsParams {

		/**
		 * The minimum x position to which the agent can travel
		 */
		public double							xmin = -1.2;

		/**
		 * The maximum x position to which the agent can travel
		 */
		public double							xmax = 0.5;


		/**
		 * Constant factor multiplied by the agent position inside the cosine that defines the shape of the curve.
		 */
		public double							cosScale = 3.0;

		/**
		 * The minimum velocity of the agent
		 */
		public double							vmin = -0.07;

		/**
		 * The maximum velocity of the agent
		 */
		public double							vmax = 0.07;

		/**
		 * The amount of acceleration of the car engine can use
		 */
		public double							acceleration = 0.001;

		/**
		 * The force of gravity
		 */
		public double							gravity = 0.0025;

		/**
		 * The time difference to pass in each update
		 */
		public double							timeDelta = 1.;


		public MCPhysicsParams copy(){

			MCPhysicsParams c = new MCPhysicsParams();
			c.xmin = this.xmin;
			c.xmax = this.xmax;
			c.cosScale = this.cosScale;
			c.vmin = this.vmin;
			c.vmax = this.vmax;
			c.acceleration = this.acceleration;
			c.gravity = this.gravity;
			c.timeDelta = this.timeDelta;

			return c;
		}

	}



	
	
	@Override
	public Domain generateDomain() {
		
		SADomain domain = new SADomain();
		
		//add attributes
		Attribute xatt = new Attribute(domain, ATTX, Attribute.AttributeType.REAL);
		xatt.setLims(physParams.xmin, physParams.xmax);
		
		Attribute vatt = new Attribute(domain, ATTV, Attribute.AttributeType.REAL);
		vatt.setLims(physParams.vmin, physParams.vmax);
		
		//add classes
		ObjectClass agentClass = new ObjectClass(domain, CLASSAGENT);
		agentClass.addAttribute(xatt);
		agentClass.addAttribute(vatt);
		
		MCPhysicsParams cphys = this.physParams.copy();

		new MovementAction(ACTIONFORWARD, domain, 1, cphys);
		new MovementAction(ACTIONBACKWARDS, domain, -1, cphys);
		new MovementAction(ACTIONCOAST, domain, 0, cphys);
		
		
		return domain;
	}
	
	
	/**
	 * Changes the agents position in the provided state using car engine acceleration in the specified direction.
	 * dir=+1 indicates forward acceleration; -1 backwards acceleration; 0 no acceleration (coast).
	 * @param s the state in which the agents position should be modified
	 * @param dir the direction of acceleration
	 * @return the modified state s
	 */
	public static State move(State s, int dir, MCPhysicsParams physParms){
		
		
		ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
		
		double p0 = agent.getRealValForAttribute(ATTX);
		double v0 = agent.getRealValForAttribute(ATTV);
		
		double netAccel = (physParms.acceleration * dir) - (physParms.gravity * Math.cos(physParms.cosScale*p0));
		
		double v1 = v0 + physParms.timeDelta * netAccel;
		if(v1 < physParms.vmin){
			v1 = physParms.vmin;
		}
		else if(v1 > physParms.vmax){
			v1 = physParms.vmax;
		}
		
		double p1 = p0 + physParms.timeDelta*v1; //original mechanics in paper defined this way
		//double p1 = p0 + this.timeDelta*v0 + .5*netAccel*this.timeDelta*this.timeDelta; //more accurate estimate
		
		if(p1 < physParms.xmin){
			p1 = physParms.xmin;
			v1 = 0.;
		}
		else if(p1 > physParms.xmax){
			p1 = physParms.xmax;
			v1 = 0.;
		}
		
		agent.setValue(ATTX, p1);
		agent.setValue(ATTV, v1);
		
		return s;
		
	}

	
	/**
	 * Returns a new state with the agent in the bottom of the hill valley not moving.
	 * @param domain the domain object in which the state is associated
	 * @return a new state with the agent in the bottom of the hill valley not moving.
	 */
	public State getCleanState(Domain domain){
		return getCleanState(domain, this.physParams);
	}

	/**
	 * Returns a new state with the agent in the bottom of the hill valley not moving according to the hill design
	 * specified in the provided {@link burlap.domain.singleagent.mountaincar.MountainCar.MCPhysicsParams}
	 * @param domain the domain object in which the state is associated
	 * @param physParms object specifying the physics and hill design, which indicates where the valley is.
	 * @return a new state with the agent in the bottom of the hill valley not moving.
	 */
	public static State getCleanState(Domain domain, MCPhysicsParams physParms){
		State s = new MutableState();
		ObjectInstance a = new MutableObjectInstance(domain.getObjectClass(CLASSAGENT), CLASSAGENT);
		s.addObject(a);
		setAgent(s, -(Math.PI/2) / physParms.cosScale, 0.);
		return s;
	}
	
	
	/**
	 * Returns a state with the agent in the specified position at the specified velocity.
	 * @param domain the domain object in which the state is associated
	 * @param x the position of the agent
	 * @param v the velocity of the agent
	 * @return a new state with the agent in the specified position
	 */
	public State getState(Domain domain, double x, double v){
		State s = this.getCleanState(domain);
		setAgent(s, x, v);
		return s;
	}
	
	/**
	 * Sets the agent position in the provided state to the given position and with the given velocity.
	 * @param s the state in which the agent should be set.
	 * @param x the position of the agent.
	 * @param v the velocity of the agent.
	 */
	public static void setAgent(State s, double x, double v){
		ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
		agent.setValue(ATTX, x);
		agent.setValue(ATTV, v);
	}
	
	
	
	
	
	/**
	 * An action for moving in a given direction. The action should be passed a direction parameter indicating the direction of acceleration.
	 * +1 for forward acceleration, -1 for backwards acceleration, 0 for no acceleration (coast).
	 * @author James MacGlashan
	 *
	 */
	class MovementAction extends SimpleAction.SimpleDeterministicAction implements FullActionModel{

		int dir;
		MCPhysicsParams physParams;
		
		/**
		 * Initializes with the given name, domain, and direction of acceleration.
		 * @param name the name of this action
		 * @param domain the domain of this action
		 * @param dir the direction of acceleration; +1 for forward acceleration, -1 for backwards acceleration, 0 for no acceleration (coast).
		 */
		public MovementAction(String name, Domain domain, int dir, MCPhysicsParams physParams){
			super(name, domain);
			this.dir = dir;
			this.physParams = physParams;
		}
		
		@Override
		protected State performActionHelper(State s, GroundedAction groundedAction) {
			return MountainCar.move(s, dir, this.physParams);
		}


		public MCPhysicsParams getPhysParams() {
			return physParams;
		}

		public void setPhysParams(MCPhysicsParams physParams) {
			this.physParams = physParams;
		}

		public int getDir() {
			return dir;
		}

		public void setDir(int dir) {
			this.dir = dir;
		}
	}
	
	
	
	/**
	 * A Terminal Function for the Mountain Car domain that terminates when the agent's position is &gt;= the max position in the world.
	 * Alternatively, a different threshold can be specified in the constructor.
	 * @author James MacGlashan
	 *
	 */
	public static class ClassicMCTF implements TerminalFunction{

		public double threshold;
		protected boolean useThreshold = false;
		
		
		/**
		 * Sets terminal states to be those that are &gt;= the maximum position in the world.
		 */
		public ClassicMCTF(){

		}
		
		/**
		 * Sets terminal states to be those &gt;= the given threshold.
		 * @param threshold position &gt;= this will be terminal states
		 */
		public ClassicMCTF(double threshold){
			this.threshold = threshold;
			this.useThreshold = true;
		}
		
		@Override
		public boolean isTerminal(State s) {

			ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
			double x = agent.getRealValForAttribute(ATTX);

			double threshold = this.threshold;
			if(!this.useThreshold){
				threshold = agent.getObjectClass().domain.getAttribute(ATTX).upperLim;
			}


			if(x >= threshold){
				return true;
			}
			return false;
		}
		
		
	}
	
	
	
	
	/**
	 * Will launch a visual explorer for the mountain car domain that is controlled with the a-s-d keys.
	 * @param args empty arguments.
	 */
	public static void main(String [] args){
		
		MountainCar mcGen = new MountainCar();
		Domain domain = mcGen.generateDomain();
		State s = mcGen.getCleanState(domain);
		

		Visualizer vis = MountainCarVisualizer.getVisualizer(mcGen);
		VisualExplorer exp = new VisualExplorer(domain, vis, s);
		
		exp.addKeyAction("d", ACTIONFORWARD);
		exp.addKeyAction("s", ACTIONCOAST);
		exp.addKeyAction("a", ACTIONBACKWARDS);
		
		exp.initGUI();
		
	}
	
	
	
}
