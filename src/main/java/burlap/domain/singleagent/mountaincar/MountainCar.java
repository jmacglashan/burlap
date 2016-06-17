package burlap.domain.singleagent.mountaincar;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.shell.visual.VisualExplorer;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.visualizer.Visualizer;

import java.util.List;


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
	public static final String ACTION_FORWARD = "forward";
	
	/**
	 * A constant for the name of the backwards action
	 */
	public static final String ACTION_BACKWARDS = "backwards";
	
	
	/**
	 * A constant for the name of the coast action
	 */
	public static final String ACTION_COAST = "coast";


	/**
	 * The physics parameters for mountain car.
	 */
	public MCPhysicsParams physParams = new MCPhysicsParams();

	protected RewardFunction rf;

	protected TerminalFunction tf;


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


	public TerminalFunction getTf() {
		return tf;
	}

	public void setTf(TerminalFunction tf) {
		this.tf = tf;
	}

	public RewardFunction getRf() {
		return rf;
	}

	public void setRf(RewardFunction rf) {
		this.rf = rf;
	}

	@Override
	public SADomain generateDomain() {
		
		SADomain domain = new SADomain();


		MCModel smodel = new MCModel(this.physParams.copy());
		if(tf == null){
			tf = new ClassicMCTF(physParams.xmax);
		}
		if(rf == null){
			rf = new GoalBasedRF(tf, 100, 0);
		}

		FactoredModel model = new FactoredModel(smodel, rf, tf);

		domain.setModel(model);

		domain.addActionType(new UniversalActionType(ACTION_FORWARD))
				.addActionType(new UniversalActionType(ACTION_BACKWARDS))
				.addActionType(new UniversalActionType(ACTION_COAST));

		
		
		return domain;
	}

	public MCState valleyState(){
		return this.physParams.valleyState();
	}
	


	public static class MCModel implements FullStateModel{

		protected MCPhysicsParams physParams;


		public MCModel(MCPhysicsParams physParams) {
			this.physParams = physParams;
		}

		@Override
		public List<StateTransitionProb> stateTransitions(State s, Action a) {
			return FullStateModel.Helper.deterministicTransition(this, s, a);
		}

		@Override
		public State sample(State s, Action a) {
			s = s.copy();
			return move(s, dir(a.actionName()));
		}

		/**
		 * Changes the agents position in the provided state using car engine acceleration in the specified direction.
		 * dir=+1 indicates forward acceleration; -1 backwards acceleration; 0 no acceleration (coast).
		 * @param s the state in which the agents position should be modified
		 * @param dir the direction of acceleration
		 * @return the modified state s
		 */
		public State move(State s, int dir){

			double p0 = (Double)s.get(ATT_X);
			double v0 = (Double)s.get(ATT_V);

			double netAccel = (physParams.acceleration * dir) - (physParams.gravity * Math.cos(physParams.cosScale*p0));

			double v1 = v0 + physParams.timeDelta * netAccel;
			if(v1 < physParams.vmin){
				v1 = physParams.vmin;
			}
			else if(v1 > physParams.vmax){
				v1 = physParams.vmax;
			}

			double p1 = p0 + physParams.timeDelta*v1; //original mechanics in paper defined this way
			//double p1 = p0 + this.timeDelta*v0 + .5*netAccel*this.timeDelta*this.timeDelta; //more accurate estimate

			if(p1 < physParams.xmin){
				p1 = physParams.xmin;
				v1 = 0.;
			}
			else if(p1 > physParams.xmax){
				p1 = physParams.xmax;
				v1 = 0.;
			}


			((MutableState)s).set(ATT_X, p1);
			((MutableState)s).set(ATT_V, v1);

			return s;

		}

		protected int dir(String actionName){
			if(actionName.equals(ACTION_FORWARD)){
				return 1;
			}
			else if(actionName.equals(ACTION_BACKWARDS)){
				return -1;
			}
			else if(actionName.equals(ACTION_COAST)){
				return 0;
			}
			throw new RuntimeException("Unknown action " + actionName);
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
		SADomain domain = mcGen.generateDomain();
		State s = mcGen.valleyState();
		

		Visualizer vis = MountainCarVisualizer.getVisualizer(mcGen);
		VisualExplorer exp = new VisualExplorer(domain, vis, s);
		
		exp.addKeyAction("d", ACTION_FORWARD, "");
		exp.addKeyAction("s", ACTION_COAST, "");
		exp.addKeyAction("a", ACTION_BACKWARDS, "");
		
		exp.initGUI();
		
	}
	
	
	
}
