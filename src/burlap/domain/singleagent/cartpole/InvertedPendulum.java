package burlap.domain.singleagent.cartpole;

import java.util.List;

import burlap.debugtools.RandomFactory;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.singleagent.*;
import burlap.oomdp.singleagent.common.SimpleAction;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;


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
	 * A constant for the name of the angle attribute
	 */
	public static final String				ATTANGLE = "angleAtt";
	
	/**
	 * A constant for the name of the angle velocity
	 */
	public static final String				ATTANGLEV = "angleVAtt";
	
	
	/**
	 * The object class for the pendulum.
	 */
	public static final String				CLASSPENDULUM = "pendulum";
	
	
	/**
	 * A constant for the name of the left action
	 */
	public static final String				ACTIONLEFT = "left";
	
	/**
	 * A constant for the name of the right action
	 */
	public static final String				ACTIONRIGHT = "right";
	
	
	/**
	 * A constant for the name of the no force action (which due to stochasticity may include a small force)
	 */
	public static final String				ACTIONNOFORCE = "noForce";
	
	
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
		 * The maximum speed (manitude) of the change in angle. The default sets it to 1
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
		
		Attribute angleatt = new Attribute(domain, ATTANGLE, Attribute.AttributeType.REAL);
		angleatt.setLims(-this.physParams.angleRange, this.physParams.angleRange);
		
		Attribute anglevatt = new Attribute(domain, ATTANGLEV, Attribute.AttributeType.REAL);
		anglevatt.setLims(-this.physParams.maxAngleSpeed, this.physParams.maxAngleSpeed);
		
		
		ObjectClass pendulum = new ObjectClass(domain, CLASSPENDULUM);
		pendulum.addAttribute(angleatt);
		pendulum.addAttribute(anglevatt);

		IPPhysicsParams cphys = this.physParams.copy();

		new ForceAction(ACTIONLEFT, domain, -this.physParams.actionForce, cphys);
		new ForceAction(ACTIONRIGHT, domain, this.physParams.actionForce, cphys);
		new ForceAction(ACTIONNOFORCE, domain, 0., cphys);
		
		return domain;
	}
	
	
	
	/**
	 * Updates the given state object given the control force.
	 * @param s the input state
	 * @param controlForce the control force acted upon the cart.
	 * @param physParams the {@link burlap.domain.singleagent.cartpole.InvertedPendulum.IPPhysicsParams} object specifying the physics to use for movement
	 */
	public static void updateState(State s, double controlForce, IPPhysicsParams physParams){
		
		ObjectInstance pend = s.getFirstObjectOfClass(CLASSPENDULUM);
		double a0 = pend.getRealValForAttribute(ATTANGLE);
		double av0 = pend.getRealValForAttribute(ATTANGLEV);
		
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
		pend.setValue(ATTANGLE, af);
		pend.setValue(ATTANGLEV, avf);
		
	}
	
	
	/**
	 * Returns an initial state with 0 angle (perfectly vertical) and 0 angle velocity.
	 * @param domain the domain object to which the state will be belong.
	 * @return an initial state with 0 angle (perfectly vertical) and 0 angle velocity.
	 */
	public static State getInitialState(Domain domain){
		return getInitialState(domain, 0., 0.);
	}
	
	
	/**
	 * Returns an initial state with the pole at the given angle and with the given angular velocity of the pole.
	 * @param domain the domain object to which the state will belong.
	 * @param angle the angle of the pole from the vertical axis.
	 * @param angleVelocity the angular velocity of the pole.
	 * @return an initial state with the pole at the given angle and with the given angular velocity of the pole.
	 */
	public static State getInitialState(Domain domain, double angle, double angleVelocity){
		State s = new MutableState();
		ObjectInstance o = new MutableObjectInstance(domain.getObjectClass(CLASSPENDULUM), CLASSPENDULUM);
		o.setValue(ATTANGLE, angle);
		o.setValue(ATTANGLEV, angleVelocity);
		s.addObject(o);
		return s;
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
		protected State performActionHelper(State s,  GroundedAction groundedAction) {
			
			double roll = RandomFactory.getMapped(0).nextDouble() * (2 * physParams.actionNoise) - physParams.actionNoise;
			double force = this.baseForce + roll;
			InvertedPendulum.updateState(s, force, this.physParams);
			return s;
		}
		
		@Override
		public List<TransitionProbability> getTransitions(State s,  GroundedAction groundedAction){
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
		double maxAbsoluteAngle = (Math.PI / 2.);
		
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
			
			ObjectInstance pendulum = s.getFirstObjectOfClass(CLASSPENDULUM);
			double a = pendulum.getRealValForAttribute(ATTANGLE);
			
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
		double maxAbsoluteAngle = (Math.PI / 2.);
		
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
			
			ObjectInstance pendulum = sprime.getFirstObjectOfClass(CLASSPENDULUM);
			double ang = pendulum.getRealValForAttribute(ATTANGLE);
			
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
		
		State s = InvertedPendulum.getInitialState(domain);
		
		Visualizer v = InvertedPendulumVisualizer.getInvertedPendulumVisualizer();
		
		VisualExplorer exp = new VisualExplorer(domain, v, s);
		
		exp.addKeyAction("a", ACTIONLEFT);
		exp.addKeyAction("d", ACTIONRIGHT);
		exp.addKeyAction("s", ACTIONNOFORCE);
		
		exp.initGUI();

	}
	
	

}
