package burlap.domain.singleagent.mountaincar;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.*;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;

import java.util.List;


/**
 * A domain generator for the classic mountain car domain with default dynamics follow those implemented by Singh and Sutton [1].
 * In this domain you can change the parameters for min/max position and velocity, the scale of the cosine curve on which the car travels,
 * the force of gravity, acceleration, and the amount of time that elapses between simulation/decision steps.
 * 
 * <p/>
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
	
	
	@Override
	public Domain generateDomain() {
		
		SADomain domain = new SADomain();
		
		//add attributes
		Attribute xatt = new Attribute(domain, ATTX, Attribute.AttributeType.REAL);
		xatt.setLims(xmin, xmax);
		
		Attribute vatt = new Attribute(domain, ATTV, Attribute.AttributeType.REAL);
		vatt.setLims(vmin, vmax);
		
		//add classes
		ObjectClass agentClass = new ObjectClass(domain, CLASSAGENT);
		agentClass.addAttribute(xatt);
		agentClass.addAttribute(vatt);
		
		
		new MovementAction(ACTIONFORWARD, domain, 1);
		new MovementAction(ACTIONBACKWARDS, domain, -1);
		new MovementAction(ACTIONCOAST, domain, 0);
		
		
		return domain;
	}
	
	
	/**
	 * Changes the agents position in the provided state using car engine acceleration in the specified direction.
	 * dir=+1 indicates forward acceleration; -1 backwards acceleration; 0 no acceleration (coast).
	 * @param s the state in which the agents position should be modified
	 * @param dir the direction of acceleration
	 * @return the modified state s
	 */
	public State move(State s, int dir){
		
		
		ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
		
		double p0 = agent.getRealValForAttribute(ATTX);
		double v0 = agent.getRealValForAttribute(ATTV);
		
		double netAccel = (acceleration * dir) - (gravity * Math.cos(this.cosScale*p0));
		
		double v1 = v0 + this.timeDelta * netAccel;
		if(v1 < vmin){
			v1 = vmin;
		}
		else if(v1 > vmax){
			v1 = vmax;
		}
		
		double p1 = p0 + this.timeDelta*v1; //original mechanics in paper defined this way
		//double p1 = p0 + this.timeDelta*v0 + .5*netAccel*this.timeDelta*this.timeDelta; //more accurate estimate
		
		if(p1 < xmin){
			p1 = xmin;
			v1 = 0.;
		}
		else if(p1 > xmax){
			p1 = xmax;
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
		State s = new State();
		ObjectInstance a = new ObjectInstance(domain.getObjectClass(CLASSAGENT), CLASSAGENT);
		s.addObject(a);
		setAgent(s, -(Math.PI/2) / this.cosScale, 0.);
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
	class MovementAction extends Action{

		int dir;
		
		
		/**
		 * Initializes with the given name, domain, and direction of acceleration.
		 * @param name the name of this action
		 * @param domain the domain of this action
		 * @param dir the direction of acceleration; +1 for forward acceleration, -1 for backwards acceleration, 0 for no acceleration (coast).
		 */
		public MovementAction(String name, Domain domain, int dir){
			super(name, domain, "");
			this.dir = dir;
		}
		
		@Override
		protected State performActionHelper(State s, String[] params) {
			return MountainCar.this.move(s, dir);
		}

		@Override
		public List<TransitionProbability> getTransitions(State s, String [] params){
			return this.deterministicTransition(s, params);
		}
		
	}
	
	
	
	/**
	 * A Terminal Function for the Mountain Car domain that terminates when the agent's position is >= the max position in the world.
	 * Alternatively, a different threshold can be specified in the constructor.
	 * @author James MacGlashan
	 *
	 */
	public class ClassicMCTF implements TerminalFunction{

		public double threshold;
		
		
		/**
		 * Sets terminal states to be those that are >= the maximum position in the world.
		 */
		public ClassicMCTF(){
			this.threshold = xmax;
		}
		
		
		/**
		 * Sets terminal states to be those >= the given threshold.
		 * @param threshold position >= this will be terminal states
		 */
		public ClassicMCTF(double threshold){
			this.threshold = threshold;
		}
		
		@Override
		public boolean isTerminal(State s) {
			double x = s.getFirstObjectOfClass(CLASSAGENT).getRealValForAttribute(ATTX);
			if(x >= this.threshold){
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
