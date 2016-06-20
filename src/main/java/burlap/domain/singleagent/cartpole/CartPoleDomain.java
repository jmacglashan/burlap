package burlap.domain.singleagent.cartpole;

import burlap.domain.singleagent.cartpole.model.CPClassicModel;
import burlap.domain.singleagent.cartpole.states.CartPoleFullState;
import burlap.domain.singleagent.cartpole.states.CartPoleState;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.core.action.UniversalActionType;
import burlap.shell.visual.VisualExplorer;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;


/**
 * The classic cart pole balancing problem as described by Barto, Sutton, and Anderson [2] with correct mechanics as described by Florian [1]. 
 * The agent controls a cart that can apply horizontal force
 * in either direction. Attached to the cart is a pole on a hinge and the goal of the agent is to apply force to the cart such that
 * the pole stays vertically balanced. If the angle between the pole and the vertical axis is greater than
 * some threshold (originally 12 degrees or about 0.2 radians), the agent fails. The track on which the cart can move is also finite in size,
 * and running into the end of the track is also considered failure; however, the track can be set to infinite by setting the {@link #physParams} isFiniteTrack
 * parameter to false. The infinite track is handled by never changing the position value of the cart. All model/physics parameters are stored in the
 * {@link burlap.domain.singleagent.cartpole.CartPoleDomain.CPPhysicsParams} object {@link #physParams}. Modifying this generator's model parameters
 * will not affected previously generated domains, so the same generator can be used to generate different domains without affecting others.
 * <p>
 * By default, this implementation will use the simulation described by Florian, which corrects two problems in the classic Barto, Sutton, and Anderson paper.
 * The two problems were (1) gravity was specified as negative in the equations when it should have been positive and (2) friction was not calculated
 * correctly. However, this domain may also be set to use the classic incorrect mechanics or the classic mechanics with correct gravity for comparison
 * purposes. To do so, use the methods {@link #setToIncorrectClassicModel()} and {@link #setToIncorrectClassicModelWithCorrectGravity()}. Note that
 * when incorrect gravity is used, the pole will "bounce" once it reaches about 90 degrees (though in most tasks the pole is never allowed to fall this far).
 * <p>
 * This domain consists of a single object with 4 real valued attributes: the x position of the cart, the x velocity of the cart, the angle between the pole
 * and the vertical axis, and the speed of the change in angle. Additionally, a 5th hidden attribute is included
 * when the corrected physics are used that maintains the sign of the normal force in the last step. If the classic mechanics are used instead,
 * then this hidden attribute is not included.
 * The physics are simulated using a non-linear differential equation
 * that is estimated using Euler's Method. All system parameters are defaulted to those used in the
 * original paper, but they may modified as desired.
 * <p>
 * Also included with this class are default classes for reward function and terminal function for this domain.
 * <p>
 * Running the main method of this class will launch and interactive visualizer with the 'a' and 'd' keys controlling left and right movement
 * force respectively.

 * 
 * 1. Florian, Razvan V. "Correct equations for the dynamics of the cart-pole system." Center for Cognitive and Neural Studies (Coneural), Romania (2007).
 * 2. Barto, Andrew G., Richard S. Sutton, and Charles W. Anderson. "Neuronlike adaptive elements that can solve difficult learning control problems." 
 * Systems, Man and Cybernetics, IEEE Transactions on 5 (1983): 834-846.
 * 
 * @author James MacGlashan
 *
 */
public class CartPoleDomain implements DomainGenerator {

	/**
	 * A constant for the name of the position attribute
	 */
	public static final String VAR_X = "xAtt";
	
	/**
	 * A constant for the name of the position velocity
	 */
	public static final String VAR_V = "xvAtt";
	
	/**
	 * A constant for the name of the angle attribute
	 */
	public static final String VAR_ANGLE = "angleAtt";
	
	/**
	 * A constant for the name of the angle velocity
	 */
	public static final String VAR_ANGLEV = "angleVAtt";
	

	/**
	 * Attribute name for maintaining the direction sign of the force normal.
	 * This attribute will only be included if the correct model is being used.
	 */
	public static final String VAR_NORM_SGN = "normalSign";

	
	/**
	 * A constant for the name of the left action
	 */
	public static final String ACTION_LEFT = "left";
	
	/**
	 * A constant for the name of the right action
	 */
	public static final String ACTION_RIGHT = "right";

	/**
	 * An object specifying the physics parameters for the cart pole domain.
	 */
	public CPPhysicsParams					physParams = new CPPhysicsParams();

	protected RewardFunction rf;
	protected TerminalFunction tf;

	public static class CPPhysicsParams{

		/*
	 	* The half length size of the track on which the cart moves.
	 	*/
		public double							halfTrackLength = 2.4;

		/**
		 * The maximum radius the pole can fall. Note, physics get weird and non-realisitc at pi/2;
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
		public double							cartMass = 1.;

		/**
		 * The mass of the pole.
		 */
		public double							poleMass = .1;

		/**
		 * The half length of the pole.
		 */
		public double							halfPoleLength = 0.5;

		/**
		 * The friction between the cart and ground
		 */
		public double							cartFriction = 0.0005;

		/**
		 * The friction between the pole and the joint on the cart.
		 */
		public double							poleFriction = 0.000002;

		/**
		 * The force magnatude that can be exterted in either direction on the cart
		 */
		public double							movementForceMag = 10.;


		/**
		 * The time between each action selection
		 */
		public double							timeDelta = 0.02;

		/**
		 * The maximum speed of the cart. A good place to set it to is somewhere around
		 * the max expected if the cart was pushed from one side of the track to the other.
		 * The default is 6.81 (which is an upperbound of that with default parameters)
		 */
		public double							maxCartSpeed = 6.81;

		/**
		 * The maximum speed of the change in angle. The default sets it to the speed that would result
		 * in 12 degress for a sinlge time step of 0.02 (which is about 10.47 radians/second), since 12 degrees
		 * is the default termination range.
		 */
		public double							maxAngleSpeed = 10.47; //12 degrees per time step of 0.02 seconds


		/**
		 * Whether the track is finite (true) or infinite (false). When the track is infinite, the position of the cart always remains the same.
		 */
		public boolean							isFiniteTrack = true;


		/**
		 * Specifies whether the correct Cart Pole physical model should be used or the classic, but incorrect, Barto Sutton and Anderson model [1].
		 */
		public boolean 						useCorrectModel = true;


		public CPPhysicsParams(){
			//do nothing
		}

		public CPPhysicsParams(double halfTrackLength, double angleRange, double gravity, double cartMass,
							   double poleMass, double halfPoleLength, double cartFriction,
							   double poleFriction, double movementForceMag, double timeDelta,
							   double maxCartSpeed, double maxAngleSpeed, boolean isFiniteTrack,
							   boolean useCorrectModel) {
			this.halfTrackLength = halfTrackLength;
			this.angleRange = angleRange;
			this.gravity = gravity;
			this.cartMass = cartMass;
			this.poleMass = poleMass;
			this.halfPoleLength = halfPoleLength;
			this.cartFriction = cartFriction;
			this.poleFriction = poleFriction;
			this.movementForceMag = movementForceMag;
			this.timeDelta = timeDelta;
			this.maxCartSpeed = maxCartSpeed;
			this.maxAngleSpeed = maxAngleSpeed;
			this.isFiniteTrack = isFiniteTrack;
			this.useCorrectModel = useCorrectModel;
		}

		public CPPhysicsParams copy(){
			return new CPPhysicsParams(halfTrackLength,angleRange,gravity,cartMass,poleMass,halfPoleLength,
					cartFriction, poleFriction, movementForceMag,timeDelta,maxCartSpeed,maxAngleSpeed,isFiniteTrack,
					useCorrectModel);
		}
	}


	
	
	
	@Override
	public SADomain generateDomain() {
		
		SADomain domain = new SADomain();

		CPPhysicsParams cphys = this.physParams.copy();

		RewardFunction rf = this.rf;
		TerminalFunction tf = this.tf;

		if(rf == null){
			rf = new CartPoleRewardFunction();
		}
		if(tf == null){
			tf = new CartPoleTerminalFunction();
		}

		FullStateModel smodel = cphys.useCorrectModel ? new CPClassicModel(cphys) : new CPClassicModel(cphys);

		FactoredModel model = new FactoredModel(smodel, rf, tf);
		domain.setModel(model);


		domain.addActionType(new UniversalActionType(ACTION_LEFT))
				.addActionType(new UniversalActionType(ACTION_RIGHT));


		return domain;
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

	/**
	 * Sets to use the classic model by Barto, Sutton, and Anderson which has incorrect friction forces, but will use
	 * correct gravity.
	 */
	public void setToIncorrectClassicModelWithCorrectGravity(){
		this.physParams.gravity = Math.abs(this.physParams.gravity);
		this.physParams.useCorrectModel = false;
	}
	
	/**
	 * Sets to the use the classic model by Barto, Sutton, and Anderson, which has incorrect friction forces and gravity
	 * in the wrong direction
	 */
	public void setToIncorrectClassicModel(){
		this.physParams.gravity = Math.abs(this.physParams.gravity)*-1;
		this.physParams.useCorrectModel = false;
	}
	
	
	/**
	 * Sets to use the correct physics model by Florian.
	 */
	public void setToCorrectModel(){
		this.physParams.gravity = Math.abs(this.physParams.gravity);
		this.physParams.useCorrectModel = true;
	}
	

	
	
	/**
	 * A default terminal function for this domain. Terminates when cart reaches end of track of
	 * angle between pole and vertical axis is greater than 12 degrees (about 0.2 radians).
	 * @author James MacGlashan
	 *
	 */
	public static class CartPoleTerminalFunction implements TerminalFunction{

		
		/**
		 * The maximum pole angle to cause termination/failure.
		 */
		double maxAbsoluteAngle = 12. * (Math.PI / 180.);

		double halfTrackLength = 2.4;
		
		
		/**
		 * Initializes with default max angle of 12 degrees (about 0.2 radians)
		 */
		public CartPoleTerminalFunction(){
			//do nothing
		}
		
		
		/**
		 * Initializes with a max pole angle as specified in radians
		 * @param maxAbsoluteAngleInRadians the maximum pole angle that causes task termination/failure.
		 */
		public CartPoleTerminalFunction(double maxAbsoluteAngleInRadians){
			this.maxAbsoluteAngle = maxAbsoluteAngleInRadians;
		}

		public double getMaxAbsoluteAngle() {
			return maxAbsoluteAngle;
		}

		public void setMaxAbsoluteAngle(double maxAbsoluteAngle) {
			this.maxAbsoluteAngle = maxAbsoluteAngle;
		}

		public double getHalfTrackLength() {
			return halfTrackLength;
		}

		public void setHalfTrackLength(double halfTrackLength) {
			this.halfTrackLength = halfTrackLength;
		}

		@Override
		public boolean isTerminal(State s) {

			CartPoleState cs = (CartPoleState)s;
			double x = cs.x;
			
			if(x <= -halfTrackLength || x >= halfTrackLength){
				return true;
			}
			
			double a = cs.angle;
			if(Math.abs(a) >= maxAbsoluteAngle){
				return true;
			}
			
			return false;
		}
		
	}
	
	
	/**
	 * A default reward function for this task. Returns 0 everywhere except at fail conditions, which return -1 and
	 * are defined by the agent reaching the end of the track or by the angle of the pole being grater than some threshold (default 12 degrees or about 0.2 radians).
	 * @author James MacGlashan
	 *
	 */
	public static class CartPoleRewardFunction implements RewardFunction{

		
		/**
		 * The maximum pole angle to cause failure.
		 */
		double maxAbsoluteAngle = 12. * (Math.PI / 180.);

		double halfTrackLength = 2.4;
		
		
		/**
		 * Initializes with max pole angle threshold of 12 degrees (about 0.2 radians)
		 */
		public CartPoleRewardFunction(){
			//do nothing
		}
		
		
		/**
		 * Initializes with a max pole angle as specified in radians
		 * @param maxAbsoluteAngleInRadians the maximum pole angle that causes task failure.
		 */
		public CartPoleRewardFunction(double maxAbsoluteAngleInRadians){
			this.maxAbsoluteAngle = maxAbsoluteAngleInRadians;
		}

		public double getMaxAbsoluteAngle() {
			return maxAbsoluteAngle;
		}

		public void setMaxAbsoluteAngle(double maxAbsoluteAngle) {
			this.maxAbsoluteAngle = maxAbsoluteAngle;
		}

		public double getHalfTrackLength() {
			return halfTrackLength;
		}

		public void setHalfTrackLength(double halfTrackLength) {
			this.halfTrackLength = halfTrackLength;
		}

		@Override
		public double reward(State s, Action a, State sprime) {

			CartPoleState cs = (CartPoleState)sprime;
			double x = cs.x;
			
			double failReward = -1;
			
			if(x <= -halfTrackLength || x >= halfTrackLength){
				return failReward;
			}
			
			double ang = cs.angle;
			if(Math.abs(ang) >= maxAbsoluteAngle){
				return failReward;
			}
			
			
			return 0.;
		}
		
		
		
	}
	
	
	/**
	 * Launches an interactive visualize in which key 'a' applies a force in the left direction and key 'd' applies force in the right direction.
	 * The corrected physics model is used.
	 * @param args ignored.
	 */
	public static void main(String [] args){
		CartPoleDomain dgen = new CartPoleDomain();

		SADomain domain = dgen.generateDomain();
		
		State s = new CartPoleFullState();
		
		VisualExplorer exp = new VisualExplorer(domain, CartPoleVisualizer.getCartPoleVisualizer(), s);
		exp.addKeyAction("a", ACTION_LEFT, "");
		exp.addKeyAction("d", ACTION_RIGHT, "");
		
		exp.initGUI();
		
	}

}
