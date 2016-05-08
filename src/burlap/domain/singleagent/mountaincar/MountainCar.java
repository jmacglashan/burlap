package burlap.domain.singleagent.mountaincar;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.FullActionModel;
import burlap.mdp.singleagent.GroundedAction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.SimpleAction;
import burlap.mdp.singleagent.explorer.VisualExplorer;
import burlap.mdp.visualizer.Visualizer;


/**
 * A domain generator for the classic mountain car domain with default dynamics follow those implemented by Singh and Sutton [1].
 * In this domain you can change the parameters for min/max position and velocity, the scale of the cosine curve on which the car travels,
 * the force of gravity, acceleration, and the amount of time that elapses between simulation/decision steps. If you
 * previously generated a {@link burlap.mdp.core.Domain}, changing the physics parameters of this {@link burlap.mdp.auxiliary.DomainGenerator} will
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
	public static final String ATT_X = "x";
	
	/**
	 * A constant for the name of the velocity attribute
	 */
	public static final String ATT_V = "v";
	

	
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

		/**
		 * Returns the position that is in the valley
		 * @return the position that is in the valley
		 */
		public double valleyPos(){
			return -(Math.PI/2) / cosScale;
		}

		public MCState valleyState(){
			return new MCState(this.valleyPos(), 0.);
		}

	}



	
	
	@Override
	public Domain generateDomain() {
		
		SADomain domain = new SADomain();
		
		MCPhysicsParams cphys = this.physParams.copy();

		new MovementAction(ACTIONFORWARD, domain, 1, cphys);
		new MovementAction(ACTIONBACKWARDS, domain, -1, cphys);
		new MovementAction(ACTIONCOAST, domain, 0, cphys);
		
		
		return domain;
	}

	public MCState valleyState(){
		return this.physParams.valleyState();
	}
	
	
	/**
	 * Changes the agents position in the provided state using car engine acceleration in the specified direction.
	 * dir=+1 indicates forward acceleration; -1 backwards acceleration; 0 no acceleration (coast).
	 * @param s the state in which the agents position should be modified
	 * @param dir the direction of acceleration
	 * @param physParms the physics parameters used
	 * @return the modified state s
	 */
	public static State move(State s, int dir, MCPhysicsParams physParms){

		double p0 = (Double)s.get(ATT_X);
		double v0 = (Double)s.get(ATT_V);

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


		((MutableState)s).set(ATT_X, p1);
		((MutableState)s).set(ATT_V, v1);
		
		return s;
		
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
	 * A Terminal Function for the Mountain Car domain that terminates when the agent's position is &gt;= the max position in the world (0.5 default).
	 * Alternatively, a different threshold can be specified in the constructor.
	 * @author James MacGlashan
	 *
	 */
	public static class ClassicMCTF implements TerminalFunction{

		public double threshold = 0.5;
		
		
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
		}
		
		@Override
		public boolean isTerminal(State s) {

			double x = (Double)s.get(ATT_X);

			return x >= threshold;

		}
		
		
	}
	
	
	
	
	/**
	 * Will launch a visual explorer for the mountain car domain that is controlled with the a-s-d keys.
	 * @param args empty arguments.
	 */
	public static void main(String [] args){
		
		MountainCar mcGen = new MountainCar();
		Domain domain = mcGen.generateDomain();
		State s = mcGen.valleyState();
		

		Visualizer vis = MountainCarVisualizer.getVisualizer(mcGen);
		VisualExplorer exp = new VisualExplorer(domain, vis, s);
		
		exp.addKeyAction("d", ACTIONFORWARD);
		exp.addKeyAction("s", ACTIONCOAST);
		exp.addKeyAction("a", ACTIONBACKWARDS);
		
		exp.initGUI();
		
	}
	
	
	
}
