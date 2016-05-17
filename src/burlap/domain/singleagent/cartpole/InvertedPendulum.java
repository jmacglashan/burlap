package burlap.domain.singleagent.cartpole;

import burlap.domain.singleagent.cartpole.model.IPModel;
import burlap.domain.singleagent.cartpole.states.InvertedPendulumState;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Action;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.RewardFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.UniversalActionType;
import burlap.mdp.singleagent.explorer.VisualExplorer;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.visualizer.Visualizer;

import static burlap.domain.singleagent.cartpole.CartPoleDomain.ACTION_LEFT;
import static burlap.domain.singleagent.cartpole.CartPoleDomain.ACTION_RIGHT;


/**
 * A simplified version of the {@link CartPoleDomain} in which the movement of the pole depends only on gravity and the force applied, and not the velocity of the
 * underlying cart. The track is also always assumed to be infinite. Therefore, the state space for this domain is fully described by two variables:
 * the angle and angular velocity of the pole. However, there is also noise included in the actions
 * of this domain as well a noop action. This version of the inverted pendulum is the version used in the original
 * Least-Squares Policy Iteration paper [1].
 * <p>
 * 
 * 
 * 1. Lagoudakis, Michail G., and Ronald Parr. "Least-squares policy iteration." The Journal of Machine Learning Research 4 (2003): 1107-1149.
 * 
 *
 *
 * @author James MacGlashan
 *
 */
public class InvertedPendulum implements DomainGenerator {

	
	
	/**
	 * A constant for the name of the no force action (which due to stochasticity may include a small force)
	 */
	public static final String ACTION_NO_FORCE = "noForce";
	
	
	public IPPhysicsParams					physParams = new IPPhysicsParams();

	protected RewardFunction rf;
	protected TerminalFunction tf;
	
	
	public static class IPPhysicsParams{

		/**
		 * The maximum radius the pole can fall. Note, physics get weird and non-realistic at pi/2;
		 * task should terminate before then.
		 */
		public double							angleRange = Math.PI/2;


		/**
		 * the force of gravity; should be *positive* for the correct mechanics.
		 */
		public double							gravity = 9.8;

		/**
		 * The mass of the cart.
		 */
		public double							cartMass = 8.;

		/**
		 * The mass of the pole.
		 */
		public double							poleMass = 2.;

		/**
		 * The length of the pole
		 */
		public double							poleLength = 0.5;



		/**
		 * The force (magnitude) applied by a left or right action.
		 */
		public double							actionForce = 50.;


		/**
		 * The force (magnitude) noise in any action, including the no force action.
		 */
		public double							actionNoise = 10.;



		/**
		 * The maximum speed (magnitude) of the change in angle. The default sets it to 1
		 */
		public double							maxAngleSpeed = 1.;


		/**
		 * The time between each action selection
		 */
		public double							timeDelta = 0.1;

		public IPPhysicsParams(){
			//do nothing
		}

		public IPPhysicsParams(double angleRange, double gravity, double cartMass, double poleMass, double poleLength,
							   double actionForce, double actionNoise, double maxAngleSpeed, double timeDelta) {
			this.angleRange = angleRange;
			this.gravity = gravity;
			this.cartMass = cartMass;
			this.poleMass = poleMass;
			this.poleLength = poleLength;
			this.actionForce = actionForce;
			this.actionNoise = actionNoise;
			this.maxAngleSpeed = maxAngleSpeed;
			this.timeDelta = timeDelta;
		}

		public IPPhysicsParams copy(){
			return new IPPhysicsParams(angleRange,gravity,cartMass,poleMass,poleLength,actionForce,actionNoise,maxAngleSpeed,timeDelta);
		}
	}


	public RewardFunction getRf() {
		return rf;
	}

	public void setRf(RewardFunction rf) {
		this.rf = rf;
	}

	public TerminalFunction getTf() {
		return tf;
	}

	public void setTf(TerminalFunction tf) {
		this.tf = tf;
	}

	@Override
	public SADomain generateDomain() {
		
		SADomain domain = new SADomain();


		IPPhysicsParams cphys = this.physParams.copy();
		IPModel smodel = new IPModel(cphys);

		RewardFunction rf = this.rf;
		TerminalFunction tf = this.tf;

		if(rf == null){
			rf = new InvertedPendulumRewardFunction();
		}
		if(tf == null){
			tf = new InvertedPendulumTerminalFunction();
		}

		FactoredModel model = new FactoredModel(smodel, rf ,tf);
		domain.setModel(model);

		domain.addActionType(new UniversalActionType(ACTION_LEFT))
				.addActionType(new UniversalActionType(ACTION_RIGHT))
				.addActionType(new UniversalActionType(ACTION_NO_FORCE));

		
		return domain;
	}
	
	
	

	
	
	/**
	 * A default terminal function for this domain. Terminates when the
	 * angle between pole and vertical axis is greater than PI/2 radians or some other user specified threshold.
	 * @author James MacGlashan
	 *
	 */
	public static class InvertedPendulumTerminalFunction implements TerminalFunction{

		/**
		 * The maximum pole angle to cause termination/failure.
		 */
		double maxAbsoluteAngle = Math.PI / 2.;
		
		public InvertedPendulumTerminalFunction() {

		}
		
		/**
		 * Initializes with a max pole angle as specified in radians
		 * @param maxAbsoluteAngle the maximum pole angle in radians that causes task termination/failure.
		 */
		public InvertedPendulumTerminalFunction(double maxAbsoluteAngle){
			this.maxAbsoluteAngle = maxAbsoluteAngle;
		}
		
		
		@Override
		public boolean isTerminal(State s) {

			InvertedPendulumState is = (InvertedPendulumState)s;
			double a = is.angle;
			
			if(Math.abs(a) >= maxAbsoluteAngle){
				return true;
			}
			
			return false;

		}
		
		
		
	}
	
	/**
	 * A default reward function for this domain. Returns 0 everywhere except at fail conditions, which return -1 and
	 * are defined by the pole being grater than some threshold (default PI/2 radians.
	 * @author James MacGlashan
	 *
	 */
	public static class InvertedPendulumRewardFunction implements RewardFunction{

		/**
		 * The maximum pole angle to cause termination/failure.
		 */
		double maxAbsoluteAngle = Math.PI / 2.;
		
		public InvertedPendulumRewardFunction() {

		}
		
		/**
		 * Initializes with a max pole angle as specified in radians
		 * @param maxAbsoluteAngle the maximum pole angle in radians that causes task termination/failure.
		 */
		public InvertedPendulumRewardFunction(double maxAbsoluteAngle){
			this.maxAbsoluteAngle = maxAbsoluteAngle;
		}
		
		
		@Override
		public double reward(State s, Action a, State sprime) {
			
			double failReward = -1;

			InvertedPendulumState is = (InvertedPendulumState)sprime;
			double ang = is.angle;
			
			if(Math.abs(ang) >= maxAbsoluteAngle){
				return failReward;
			}
			
			return 0;
		}
		
		
		
		
		
	}


	/**
	 * @param args none expected
	 */
	public static void main(String[] args) {

		InvertedPendulum ivp = new InvertedPendulum();
		SADomain domain = ivp.generateDomain();
		
		State s = new InvertedPendulumState();
		
		Visualizer v = CartPoleVisualizer.getCartPoleVisualizer();
		
		VisualExplorer exp = new VisualExplorer(domain, v, s);
		
		exp.addKeyAction("a", ACTION_LEFT, "");
		exp.addKeyAction("d", ACTION_RIGHT, "");
		exp.addKeyAction("s", ACTION_NO_FORCE, "");
		
		exp.initGUI();

	}
	
	

}
