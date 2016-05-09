package burlap.domain.singleagent.cartpole;

import burlap.debugtools.RandomFactory;
import burlap.domain.singleagent.cartpole.states.InvertedPendulumState;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.TransitionProbability;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.FullActionModel;
import burlap.mdp.singleagent.GroundedAction;
import burlap.mdp.singleagent.RewardFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.SimpleAction;
import burlap.mdp.singleagent.explorer.VisualExplorer;
import burlap.mdp.visualizer.Visualizer;

import java.util.List;

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
	

	
	
	
	@Override
	public Domain generateDomain() {
		
		SADomain domain = new SADomain();


		IPPhysicsParams cphys = this.physParams.copy();

		new ForceAction(ACTION_LEFT, domain, -this.physParams.actionForce, cphys);
		new ForceAction(ACTION_RIGHT, domain, this.physParams.actionForce, cphys);
		new ForceAction(ACTION_NO_FORCE, domain, 0., cphys);
		
		return domain;
	}
	
	
	
	/**
	 * Updates the given state object given the control force.
	 * @param s the input state
	 * @param controlForce the control force acted upon the cart.
	 * @param physParams the {@link burlap.domain.singleagent.cartpole.InvertedPendulum.IPPhysicsParams} object specifying the physics to use for movement
	 */
	public static void updateState(State s, double controlForce, IPPhysicsParams physParams){

		InvertedPendulumState is = (InvertedPendulumState)s;
		double a0 = is.angle;
		double av0 = is.angleV;

		
		double alpha = 1./ (physParams.cartMass + physParams.poleMass);
		
		double sinA = Math.sin(a0);
		double cosA = Math.cos(a0);
		
		double num = (physParams.gravity*sinA) -
				(alpha * physParams.poleMass*physParams.poleLength*av0*av0*Math.sin(2.*a0)*0.5) -
				(alpha * cosA * controlForce);
		
		double denom = ((4./3.)*physParams.poleLength) - alpha*physParams.poleMass*physParams.poleLength*cosA*cosA;
		
		double accel = num / denom;
		
		//now perform Euler's
		double af = a0 + physParams.timeDelta*av0;
		double avf = av0 + physParams.timeDelta*accel;
		
		//clamp it
		if(Math.abs(af) >= physParams.angleRange){
			af = Math.signum(af) * physParams.angleRange;
			avf = 0.;
		}
		
		if(Math.abs(avf) > physParams.maxAngleSpeed){
			avf = Math.signum(avf) * physParams.maxAngleSpeed;
		}
		
		//set it
		is.angle = af;
		is.angleV = avf;
	}

	
	/**
	 * An action that applies a given force to the cart + uniform random noise in the range defined in the {@link InvertedPendulum#physParams} data member.
	 * @author James MacGlashan
	 *
	 */
	public class ForceAction extends SimpleAction implements FullActionModel{

		/**
		 * The base noise to which noise will be added.
		 */
		protected double baseForce;

		/**
		 * The physics parameters to use
		 */
		protected IPPhysicsParams physParams;
		
		/**
		 * Initializes the force action
		 * @param name the name of the action
		 * @param domain the domain object to which the action will belong.
		 * @param force the base force this action applies; noise will be added to this force according to the {@link InvertedPendulum#physParams} data member.
		 * @param physParams the {@link burlap.domain.singleagent.cartpole.InvertedPendulum.IPPhysicsParams} object specifying the physics to use for movement
		 */
		public ForceAction(String name, Domain domain, double force, IPPhysicsParams physParams){
			super(name, domain);
			this.baseForce = force;
			this.physParams = physParams;
		}
		
		@Override
		protected State sampleHelper(State s, GroundedAction groundedAction) {
			
			double roll = RandomFactory.getMapped(0).nextDouble() * (2 * physParams.actionNoise) - physParams.actionNoise;
			double force = this.baseForce + roll;
			InvertedPendulum.updateState(s, force, this.physParams);
			return s;
		}
		
		@Override
		public List<TransitionProbability> transitions(State s, GroundedAction groundedAction){
			if(this.physParams.actionNoise != 0.) {
				throw new RuntimeException("Transition Probabilities for the Inverted Pendulum with continuous action noise cannot be enumerated.");
			}
			return this.deterministicTransition(s, groundedAction);
		}
		
		
		
		
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
		public double reward(State s, GroundedAction a, State sprime) {
			
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
		Domain domain = ivp.generateDomain();
		
		State s = new InvertedPendulumState();
		
		Visualizer v = CartPoleVisualizer.getCartPoleVisualizer();
		
		VisualExplorer exp = new VisualExplorer(domain, v, s);
		
		exp.addKeyAction("a", ACTION_LEFT);
		exp.addKeyAction("d", ACTION_RIGHT);
		exp.addKeyAction("s", ACTION_NO_FORCE);
		
		exp.initGUI();

	}
	
	

}
