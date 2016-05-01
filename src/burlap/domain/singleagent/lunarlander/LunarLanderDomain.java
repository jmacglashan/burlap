package burlap.domain.singleagent.lunarlander;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.oo.propositional.PropositionalFunction;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.OldObjectInstance;
import burlap.oomdp.core.state.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.SimpleAction;
import burlap.oomdp.singleagent.explorer.TerminalExplorer;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;

import java.util.ArrayList;
import java.util.List;


/**
 * This domain generator is used for producing a Lunar Lander domain, based on the classic
 *  Lunar Lander Atari game (1979). The agent pilots a lunar lander that has a variety of
 *  thrust actions and can rotate clockwise or counter clockwise. The agent may also do nothing
 *  and just drift. Using a thrust
 *  action exerts force in the opposite direction the lander is oriented. Gravity acts
 *  on the agent so the agent must find the right balance of thrust to take off
 *  and reach some designated landing position.
 *  <p>
 *  The agent object class is made up of 5 attributes, x-y position, x-y velocity,
 *  and the angle or orientation (in radians from the default vertical orientation). Two
 *  other object classes are defined for landing pads and obstacles. Both are objects
 *  are defined as a rectangular region with a left, right, bottom, and top attribute
 *  defining the space. Typically, the goal is for the agent to land on the land pad
 *  while avoiding the obstacles.
 *  <p>
 *  The domain generator allows the client to specify the physics and action definitions
 *  for the lunar lander. In particular the client can add as many thrust actions (each
 *  with their own thrust force) as desired, the force of gravity can be changed, the
 *  size of the domain (that is, the x space and y space in which the agent can fly before
 *  hitting a "wall") can be changed, the maximum lander rotation can be set, and the
 *  rotate increment size that results from applying a clockwise/counterclockwise 
 *  rotate action can also be set. There is also a method to set the domain
 *  to a standard set of physics and actions.
 *  <p>
 *  If the domain generator physics parameters are changed after a domain has been generated,
 *  the previously generated domain will remain unaffected, allowing you to reuse the same
 *  domain generator to produce different versions of the domain without conflict.
 *  
 * @author James MacGlashan
 *
 */
public class LunarLanderDomain implements DomainGenerator {

	
	/**
	 * Constant for the name of the x position attribute.
	 */
	public static final String				XATTNAME = "xAtt";
	
	/**
	 * Constant for the name of the y position attribute.
	 */
	public static final String				YATTNAME = "yAtt";
	
	
	/**
	 * Constant for the name of the x velocity attribute.
	 */
	public static final String				VXATTNAME = "vxAtt";
	
	/**
	 * Constant for the name of the y velocity attribute.
	 */
	public static final String				VYATTNAME = "vyAtt";
	
	
	/**
	 * Constant for the name of the angle of orientation attribute.
	 */
	public static final String				AATTNAME = "angAtt"; //angle of lander
	
	
	/**
	 * Constant for the name of the left boundary attribute for rectangular obstacles and landing pads
	 */
	public static final String				LATTNAME = "lAtt";
	
	/**
	 * Constant for the name of the right boundary attribute for rectangular obstacles and landing pads
	 */
	public static final String				RATTNAME = "rAtt";
	
	/**
	 * Constant for the name of the bottom boundary attribute for rectangular obstacles and landing pads
	 */
	public static final String				BATTNAME = "bAtt";
	
	/**
	 * Constant for the name of the top boundary attribute for rectangular obstacles and landing pads
	 */
	public static final String				TATTNAME = "tAtt"; //top boundary
	
	
	/**
	 * Constant for the name of the agent OO-MDP class
	 */
	public static final String				AGENTCLASS = "agent";
	
	/**
	 * Constant for the name of the obstacle OO-MDP class
	 */
	public static final String				OBSTACLECLASS = "obstacle";
	
	/**
	 * Constant for the name of the goal landing pad OO-MDP class
	 */
	public static final String				PADCLASS = "goal";
	
	
	/**
	 * Constant for the name of the turn/rotate left/counterclockwise action
	 */
	public static final String				ACTIONTURNL = "turnLeft";
	
	/**
	 * Constant for the name of the turn/rotate right/clockwise action
	 */
	public static final String				ACTIONTURNR = "turnRight";
	
	/**
	 * Constant for the base name of thrust actions. Each thrust action will have a number appended to this name to indicate which thrust action it is.
	 */
	public static final String				ACTIONTHRUST = "thrust";
	
	/**
	 * Constant for the name of the idle action which causes the agent to do nothing by drift for a time step
	 */
	public static final String				ACTIONIDLE = "idle";
	
	
	/**
	 * Constant for the name of the propositional function that indicates whether the agent/lander is on a landing pad
	 */
	public static final String				PFONPAD = "onLandingPad";
	
	/**
	 * Constant for the name of the propositional function that indicates whether the agent/lander is *touching* a landing pad.
	 * Note that the agent may be touching the pad on the side of its boundaries and therefore not be landed on it. However
	 * if the agent is landed on the pad, then they will also be touching it.
	 */
	public static final String				PFTPAD = "touchingLandingPad";
	
	/**
	 * Constant for the name of the propositional function that indicates whether the agent/lander is touching
	 * an obstacle surface.
	 */
	public static final String				PFTOUCHSURFACE = "touchingSurface";
	
	/**
	 * Constant for the name of the propositional function that indicates whether the agent/lander is on the ground
	 */
	public static final String				PFONGROUND = "onGround"; //landed on ground

	/**
	 * List of the thrust forces for each thrust action
	 */
	protected List <Double>					thrustValues;


	/**
	 * An object for holding the physics parameters of this domain.
	 */
	protected LLPhysicsParams				physParams = new LLPhysicsParams();


	/**
	 * A class for holding the physics parameters
	 */
	public static class LLPhysicsParams{

		/**
		 * The force of gravity
		 */
		protected double						gravity = -0.2;

		/**
		 * The minimum x value of the world
		 */
		protected double						xmin = 0.;

		/**
		 * The maximum x value of the world
		 */
		protected double						xmax = 100.;

		/**
		 * The minimum y value of the world
		 */
		protected double						ymin = 0.;

		/**
		 * The maximum y value of the world
		 */
		protected double						ymax = 50.;

		/**
		 * The maximum speed in any velocity component that the agent can move
		 */
		protected double						vmax = 4.;

		/**
		 * The maximum angle the lander can be rotated in either the clockwise or counterclockwise direction
		 */
		protected double						angmax = Math.PI/4.;


		/**
		 * The change in orientation angle the lander makes when a turn/rotate action is taken
		 */
		protected double						anginc = Math.PI/20.;


		public LLPhysicsParams copy(){

			LLPhysicsParams c = new LLPhysicsParams();

			c.gravity = this.gravity;
			c.xmin = this.xmin;
			c.xmax = this.xmax;
			c.ymin = this.ymin;
			c.ymax = this.ymax;
			c.vmax = this.vmax;
			c.angmax = this.angmax;
			c.anginc = this.anginc;

			return c;

		}

		public double getGravity() {
			return gravity;
		}

		public void setGravity(double gravity) {
			this.gravity = gravity;
		}

		public double getXmin() {
			return xmin;
		}

		public void setXmin(double xmin) {
			this.xmin = xmin;
		}

		public double getXmax() {
			return xmax;
		}

		public void setXmax(double xmax) {
			this.xmax = xmax;
		}

		public double getYmin() {
			return ymin;
		}

		public void setYmin(double ymin) {
			this.ymin = ymin;
		}

		public double getYmax() {
			return ymax;
		}

		public void setYmax(double ymax) {
			this.ymax = ymax;
		}

		public double getVmax() {
			return vmax;
		}

		public void setVmax(double vmax) {
			this.vmax = vmax;
		}

		public double getAngmax() {
			return angmax;
		}

		public void setAngmax(double angmax) {
			this.angmax = angmax;
		}

		public double getAnginc() {
			return anginc;
		}

		public void setAnginc(double anginc) {
			this.anginc = anginc;
		}
	}
	
	
	/**
	 * Sets the agent/lander position/orientation and zeros out the lander velocity.
	 * @param s the state in which to set the agent
	 * @param a the angle of the lander
	 * @param x the x position
	 * @param y the y position
	 */
	public static void setAgent(State s, double a, double x, double y){
		setAgent(s, a, x, y, 0., 0.);
	}
	
	
	/**
	 * Sets the agent/lander position/orientation and the velocity.
	 * @param s the state in which to set the agent
	 * @param a the angle of the lander
	 * @param x the x position of the lander
	 * @param y the y position of the lander
	 * @param vx the x velocity component of the lander
	 * @param vy the y velocity component of the lander
	 */
	public static void setAgent(State s, double a, double x, double y, double vx, double vy){
		OldObjectInstance agent = s.getObjectsOfClass(AGENTCLASS).get(0);
		
		agent.setValue(AATTNAME, a);
		agent.setValue(XATTNAME, x);
		agent.setValue(YATTNAME, y);
		agent.setValue(VXATTNAME, vx);
		agent.setValue(VYATTNAME, vy);
	}
	
	
	/**
	 * Sets an obstacles boundaries/position
	 * @param s the state in which the obstacle should be set
	 * @param i specifies the ith landing pad object to be set to these values
	 * @param l the left boundary
	 * @param r the right boundary
	 * @param b the bottom boundary
	 * @param t the top boundary
	 */
	public static void setObstacle(State s, int i, double l, double r, double b, double t){
		OldObjectInstance obst = s.getObjectsOfClass(OBSTACLECLASS).get(i);
		
		obst.setValue(LATTNAME, l);
		obst.setValue(RATTNAME, r);
		obst.setValue(BATTNAME, b);
		obst.setValue(TATTNAME, t);
	}
	
	
	/**
	 * Sets the first landing pad's boundaries/position
	 * @param s the state in which the obstacle should be set
	 * @param l the left boundary
	 * @param r the right boundary
	 * @param b the bottom boundary
	 * @param t the top boundary
	 */
	public static void setPad(State s, double l, double r, double b, double t){
		setPad(s, 0, l, r, b, t);
	}
	
	
	/**
	 * Sets a landing pad's boundaries/position
	 * @param s the state in which the obstacle should be set
	 * @param i specifies the ith landing pad object to be set to these values
	 * @param l the left boundary
	 * @param r the right boundary
	 * @param b the bottom boundary
	 * @param t the top boundary
	 */
	public static void setPad(State s, int i, double l, double r, double b, double t){
		OldObjectInstance pad = s.getObjectsOfClass(PADCLASS).get(i);
		
		pad.setValue(LATTNAME, l);
		pad.setValue(RATTNAME, r);
		pad.setValue(BATTNAME, b);
		pad.setValue(TATTNAME, t);
	}
	
	
	
	/**
	 * Initializes with no thrust actions set.
	 */
	public LunarLanderDomain(){
		thrustValues = new ArrayList<Double>();
	}
	
	/**
	 * Adds a thrust action with thrust force t
	 * @param t the thrust of the thrust force to add
	 */
	public void addThrustActionWithThrust(double t){
		this.thrustValues.add(t);
	}


	public LLPhysicsParams getPhysParams() {
		return physParams;
	}

	public void setPhysParams(LLPhysicsParams physParams) {
		this.physParams = physParams;
	}

	/**
	 * Sets the gravity of the domain
	 * @param g the force of gravity
	 */
	public void setGravity(double g){
		this.physParams.gravity = g;
	}
	
	
	/**
	 * Returns the minimum x position of the lander (the agent cannot cross this boundary)
	 * @return the minimum x position of the lander (the agent cannot cross this boundary)
	 */
	public double getXmin() {
		return this.physParams.xmin;
	}


	/**
	 * Sets the minimum x position of the lander (the agent cannot cross this boundary)
	 * @param xmin the minimum x position of the lander (the agent cannot cross this boundary)
	 */
	public void setXmin(double xmin) {
		this.physParams.xmin = xmin;
	}


	/**
	 * Returns the maximum x position of the lander (the agent cannot cross this boundary)
	 * @return the maximum x position of the lander (the agent cannot cross this boundary)
	 */
	public double getXmax() {
		return this.physParams.xmax;
	}


	/**
	 * Sets the maximum x position of the lander (the agent cannot cross this boundary)
	 * @param xmax the maximum x position of the lander (the agent cannot cross this boundary)
	 */
	public void setXmax(double xmax) {
		this.physParams.xmax = xmax;
	}


	/**
	 * Returns the minimum y position of the lander (the agent cannot cross this boundary)
	 * @return the minimum y position of the lander (the agent cannot cross this boundary)
	 */
	public double getYmin() {
		return this.physParams.ymin;
	}


	/**
	 * Sets the minimum y position of the lander (the agent cannot cross this boundary)
	 * @param ymin the minimum y position of the lander (the agent cannot cross this boundary)
	 */
	public void setYmin(double ymin) {
		this.physParams.ymin = ymin;
	}


	/**
	 * Returns the maximum y position of the lander (the agent cannot cross this boundary)
	 * @return the maximum y position of the lander (the agent cannot cross this boundary)
	 */
	public double getYmax() {
		return this.physParams.ymax;
	}


	/**
	 * Sets the maximum y position of the lander (the agent cannot cross this boundary)
	 * @param ymax the maximum y position of the lander (the agent cannot cross this boundary)
	 */
	public void setYmax(double ymax) {
		this.physParams.ymax = ymax;
	}


	/**
	 * Returns the maximum velocity of the agent  (the agent cannot move faster than this value).
	 * @return the maximum velocity of the agent  (the agent cannot move faster than this value).
	 */
	public double getVmax() {
		return this.physParams.vmax;
	}


	/**
	 * Sets the maximum velocity of the agent (the agent cannot move faster than this value).
	 * @param vmax the maximum velocity of the agent (the agent cannot move faster than this value).
	 */
	public void setVmax(double vmax) {
		this.physParams.vmax = vmax;
	}


	/**
	 * Returns the maximum rotate angle (in radians) that the lander can be rotated from the vertical orientation in either
	 * clockwise or counterclockwise direction.
	 * @return the maximum rotate angle (in radians) that the lander can be rotated
	 */
	public double getAngmax() {
		return this.physParams.angmax;
	}


	/**
	 * Sets the maximum rotate angle (in radians) that the lander can be rotated from the vertical orientation in either
	 * clockwise or counterclockwise direction.
	 * @param angmax the maximum rotate angle (in radians) that the lander can be rotated
	 */
	public void setAngmax(double angmax) {
		this.physParams.angmax = angmax;
	}


	/**
	 * Returns how many radians the agent will rotate from its current orientation when a turn/rotate action is applied
	 * @return how many radians the agent will rotate from its current orientation when a turn/rotate action is applied
	 */
	public double getAnginc() {
		return this.physParams.anginc;
	}


	/**
	 * Sets how many radians the agent will rotate from its current orientation when a turn/rotate action is applied
	 * @param anginc how many radians the agent will rotate from its current orientation when a turn/rotate action is applied
	 */
	public void setAnginc(double anginc) {
		this.physParams.anginc = anginc;
	}
	
	
	
	/**
	 * Sets the domain to use a standard set of physics and with a standard set of two thrust actions.<p>
	 * gravity = -0.2<p>
	 * xmin = 0<p>
	 * xmax = 100<p>
	 * ymin = 0<p>
	 * ymax = 50<p>
	 * max velocity component speed = 4<p>
	 * maximum angle of rotation = pi/4<p>
	 * change in angle from turning = pi/20<p>
	 * thrust1 force = 0.32<p>
	 * thrust2 force = 0.2 (opposite gravity) 
	 */
	public void setToStandardLunarLander(){
		this.addStandardThrustActions();
		this.physParams = new LLPhysicsParams();
	}
	
	
	/**
	 * Adds two standard thrust actions.<p>
	 * thrust1 force = 0.32<p>
	 * thrust2 force = 0.2 (opposite gravity) 
	 */
	public void addStandardThrustActions(){
		this.thrustValues.add(0.32);
		this.thrustValues.add(-physParams.gravity);
	}
	
	@Override
	public Domain generateDomain() {
		
		Domain domain = new SADomain();
		
		List <Double> thrustValuesTemp = this.thrustValues;
		if(thrustValuesTemp.isEmpty()){
			thrustValuesTemp.add(0.32);
			thrustValuesTemp.add(-physParams.gravity);
		}
		
		
		//create attributes
		Attribute xatt = new Attribute(domain, XATTNAME, Attribute.AttributeType.REAL);
		xatt.setLims(physParams.xmin, physParams.xmax);
		
		Attribute yatt = new Attribute(domain, YATTNAME, Attribute.AttributeType.REAL);
		yatt.setLims(physParams.ymin, physParams.ymax);
		
		Attribute vxatt = new Attribute(domain, VXATTNAME, Attribute.AttributeType.REAL);
		vxatt.setLims(-physParams.vmax, physParams.vmax);
		
		Attribute vyatt = new Attribute(domain, VYATTNAME, Attribute.AttributeType.REAL);
		vyatt.setLims(-physParams.vmax, physParams.vmax);
		
		Attribute aatt = new Attribute(domain, AATTNAME, Attribute.AttributeType.REAL);
		aatt.setLims(-physParams.angmax, physParams.angmax);
		
		Attribute latt = new Attribute(domain, LATTNAME, Attribute.AttributeType.REAL);
		latt.setLims(physParams.xmin, physParams.xmax);
		
		Attribute ratt = new Attribute(domain, RATTNAME, Attribute.AttributeType.REAL);
		ratt.setLims(physParams.xmin, physParams.xmax);
		
		Attribute batt = new Attribute(domain, BATTNAME, Attribute.AttributeType.REAL);
		batt.setLims(physParams.ymin, physParams.ymax);
		
		Attribute tatt = new Attribute(domain, TATTNAME, Attribute.AttributeType.REAL);
		tatt.setLims(physParams.ymin, physParams.ymax);
		
		
		
		
		
		//create classes
		ObjectClass agentclass = new ObjectClass(domain, AGENTCLASS);
		agentclass.addAttribute(xatt);
		agentclass.addAttribute(yatt);
		agentclass.addAttribute(vxatt);
		agentclass.addAttribute(vyatt);
		agentclass.addAttribute(aatt);
		
		ObjectClass obstclss = new ObjectClass(domain, OBSTACLECLASS);
		obstclss.addAttribute(latt);
		obstclss.addAttribute(ratt);
		obstclss.addAttribute(batt);
		obstclss.addAttribute(tatt);
		
		
		ObjectClass padclass = new ObjectClass(domain, PADCLASS);
		padclass.addAttribute(latt);
		padclass.addAttribute(ratt);
		padclass.addAttribute(batt);
		padclass.addAttribute(tatt);

		//make copy of physics parameters
		LLPhysicsParams cphys = this.physParams.copy();
		
		//add actions
		new ActionTurn(ACTIONTURNL, domain, -1., cphys);
		new ActionTurn(ACTIONTURNR, domain, 1., cphys);
		new ActionIdle(ACTIONIDLE, domain, cphys);
		
		for(int i = 0; i < thrustValuesTemp.size(); i++){
			double t = thrustValuesTemp.get(i);
			new ActionThrust(ACTIONTHRUST+i, domain, t, cphys);
		}
		
		
		//add pfs
		new OnPadPF(PFONPAD, domain);
		new TouchPadPF(PFTPAD, domain);
		new TouchSurfacePF(PFTOUCHSURFACE, domain);
		new TouchGroundPF(PFONGROUND, domain);
		
		
		
		return domain;
		
	}
	
	
	/**
	 * Creates a state with one agent/lander, one landing pad, and no number of obstacles. The attribute values
	 * of these objects will be uninitialized and will need to be set either manually or with this class's methods
	 * like {@link #setAgent(State, double, double, double)}.
	 * @param domain the domain of the state to generate
	 * @param no the number of obstacle objects to create
	 * @return a state object
	 */
	public static State getCleanState(Domain domain, int no){
		
		State s = new CMutableState();
		
		OldObjectInstance agent = new MutableObjectInstance(domain.getObjectClass(AGENTCLASS), AGENTCLASS + "0");
		s.addObject(agent);
		
		OldObjectInstance pad = new MutableObjectInstance(domain.getObjectClass(PADCLASS), PADCLASS + "0");
		s.addObject(pad);
		
		for(int i = 0; i < no; i++){
			OldObjectInstance obst = new MutableObjectInstance(domain.getObjectClass(OBSTACLECLASS), OBSTACLECLASS + i);
			s.addObject(obst);
		}

		return s;
		
	}

	
	/**
	 * Turns the lander in the direction indicated by the domains defined change in angle for turn actions.
	 * @param s the state in which the lander's angle should be changed
	 * @param dir the direction to turn; +1 is clockwise, -1 is counterclockwise
	 */
	protected static void incAngle(State s, double dir, LLPhysicsParams physParams){
		
		OldObjectInstance agent = s.getObjectsOfClass(AGENTCLASS).get(0);
		double curA = agent.getRealValForAttribute(AATTNAME);
		
		double newa = curA + (dir * physParams.anginc);
		if(newa > physParams.angmax){
			newa = physParams.angmax;
		}
		else if(newa < -physParams.angmax){
			newa = -physParams.angmax;
		}
		
		agent.setValue(AATTNAME, newa);
		
	}
	
	
	/**
	 * Updates the position of the agent/lander given the provided thrust force that has been exerted
	 * @param s the state in which the agent/lander should be modified
	 * @param thrust the amount of thrust force exerted by the lander.
	 */
	protected static void updateMotion(State s, double thrust, LLPhysicsParams physParams){
		
		double ti = 1.;
		double tt = ti*ti;
		
		OldObjectInstance agent = s.getObjectsOfClass(AGENTCLASS).get(0);
		double ang = agent.getRealValForAttribute(AATTNAME);
		double x = agent.getRealValForAttribute(XATTNAME);
		double y = agent.getRealValForAttribute(YATTNAME);
		double vx = agent.getRealValForAttribute(VXATTNAME);
		double vy = agent.getRealValForAttribute(VYATTNAME);
		
		double worldAngle = (Math.PI/2.) - ang;
		
		double tx = Math.cos(worldAngle)*thrust;
		double ty = Math.sin(worldAngle)*thrust;
		
		double ax = tx;
		double ay = ty + physParams.gravity;
		
		double nx = x + vx*ti + (0.5*ax*tt);
		double ny = y + vy*ti + (0.5*ay*tt);
		
		double nvx = vx + ax*ti;
		double nvy = vy + ay*ti;
		
		double nang = ang;
		
		//check for boundaries
		if(ny > physParams.ymax){
			ny = physParams.ymax;
			nvy = 0.;
		}
		else if(ny <= physParams.ymin){
			ny = physParams.ymin;
			nvy = 0.;
			nang = 0.;
			nvx = 0.;
		}
		
		if(nx > physParams.xmax){
			nx = physParams.xmax;
			nvx = 0.;
		}
		else if(nx < physParams.xmin){
			nx = physParams.xmin;
			nvx = 0.;
		}
		
		if(nvx > physParams.vmax){
			nvx = physParams.vmax;
		}
		else if(nvx < -physParams.vmax){
			nvx = -physParams.vmax;
		}
		
		if(nvy > physParams.vmax){
			nvy = physParams.vmax;
		}
		else if(nvy < -physParams.vmax){
			nvy = -physParams.vmax;
		}
		
		
		
		//check for collisions
		List <OldObjectInstance> obstacles = s.getObjectsOfClass(OBSTACLECLASS);
		for(OldObjectInstance o : obstacles){
			double l = o.getRealValForAttribute(LATTNAME);
			double r = o.getRealValForAttribute(RATTNAME);
			double b = o.getRealValForAttribute(BATTNAME);
			double t = o.getRealValForAttribute(TATTNAME);
			
			//are we intersecting?
			if(nx > l && nx < r && ny >= b && ny < t){
				//intersection!
				
				//from which direction did we hit it (check previous position)?
				if(x <= l){
					nx = l;
					nvx = 0.;
				}
				else if(x >= r){
					nx = r;
					nvx = 0.;
				}
				
				if(y <= b){
					ny = b;
					nvy = 0.;
				}
				else if(y >= t){
					ny = t;
					nvy = 0.;
					nang = 0.;
					nvx = 0.;
				}
				
				
				//can only hit one obstacle so break out of search
				break;
				
			}
			
			
		}
		
		
		//check the pad collision
		OldObjectInstance pad = s.getObjectsOfClass(PADCLASS).get(0);
		double l = pad.getRealValForAttribute(LATTNAME);
		double r = pad.getRealValForAttribute(RATTNAME);
		double b = pad.getRealValForAttribute(BATTNAME);
		double t = pad.getRealValForAttribute(TATTNAME);
		
		//did we collide?
		if(nx > l && nx < r && ny >= b && ny < t){
			//intersection!
			
			//from which direction did we hit it (check previous position)?
			if(x <= l){
				nx = l;
				nvx = 0.;
			}
			else if(x >= r){
				nx = r;
				nvx = 0.;
			}
			
			if(y <= b){
				ny = b;
				nvy = 0.;
			}
			else if(y >= t){
				ny = t;
				nvy = 0.;
				nang = 0.;
				nvx = 0.;
			}

			
		}
		
		
		
		
		//now set the new values
		agent.setValue(XATTNAME, nx);
		agent.setValue(YATTNAME, ny);
		agent.setValue(VXATTNAME, nvx);
		agent.setValue(VYATTNAME, nvy);
		agent.setValue(AATTNAME, nang);
		
		
	}
	
	
	
	/**
	 * An action class for turning the lander
	 * @author James MacGlashan
	 *
	 */
	public class ActionTurn extends SimpleAction.SimpleDeterministicAction implements FullActionModel{

		LLPhysicsParams physParams;
		double dir;
		
		/**
		 * Creates a turn action for the indicated direction.
		 * @param name the name of the action
		 * @param domain the domain in which the action exists
		 * @param dir the direction this action will turn; +1 for clockwise, -1 for counterclockwise.
		 */
		public ActionTurn(String name, Domain domain, double dir, LLPhysicsParams physParams) {
			super(name, domain);
			this.dir = dir;
			this.physParams = physParams;
		}
		
		

		@Override
		protected State performActionHelper(State st, GroundedAction groundedAction) {
			incAngle(st, dir, this.physParams);
			updateMotion(st, 0.0, this.physParams);
			return st;
		}


		public LLPhysicsParams getPhysParams() {
			return physParams;
		}

		public void setPhysParams(LLPhysicsParams physParams) {
			this.physParams = physParams;
		}
	}
	
	
	
	/**
	 * An action class for having the agent idle (its current velocity and the force of gravity will be all that acts on the lander).
	 * @author James MacGlashan
	 *
	 */
	public class ActionIdle extends SimpleAction.SimpleDeterministicAction implements FullActionModel{

		LLPhysicsParams physParams;
		
		/**
		 * Initializes the idle action.
		 * @param name the name of the action
		 * @param domain the domain of the action.
		 */
		public ActionIdle(String name, Domain domain, LLPhysicsParams physParams) {
			super(name, domain);
			this.physParams = physParams;
		}
		

		@Override
		protected State performActionHelper(State st, GroundedAction groundedAction) {
			updateMotion(st, 0.0, this.physParams);
			return st;
		}


		public LLPhysicsParams getPhysParams() {
			return physParams;
		}

		public void setPhysParams(LLPhysicsParams physParams) {
			this.physParams = physParams;
		}
	}
	
	
	
	/**
	 * An action class for exerting a thrust. 
	 * @author James MacGlashan
	 *
	 */
	public class ActionThrust extends SimpleAction.SimpleDeterministicAction implements FullActionModel{

		protected double thrustValue;
		LLPhysicsParams physParams;
		
		
		/**
		 * Initializes a thrust action for a given thrust force
		 * @param name the name of the action
		 * @param domain the domain of the action
		 * @param thrustValue the force of thrust for this thrust action
		 */
		public ActionThrust(String name, Domain domain, double thrustValue, LLPhysicsParams physParams){
			super(name, domain);
			this.thrustValue = thrustValue;
			this.physParams = physParams;
		}
		
		
		@Override
		protected State performActionHelper(State st, GroundedAction groundedAction) {
			updateMotion(st, thrustValue, this.physParams);
			return st;
		}


		public double getThrustValue() {
			return thrustValue;
		}

		public void setThrustValue(double thrustValue) {
			this.thrustValue = thrustValue;
		}

		public LLPhysicsParams getPhysParams() {
			return physParams;
		}

		public void setPhysParams(LLPhysicsParams physParams) {
			this.physParams = physParams;
		}
	}
	
	
	
	
	
	
	/**
	 * A propositional function that evaluates to true if the agent has landed on the top surface of a landing pad.
	 * @author James MacGlashan
	 *
	 */
	public class OnPadPF extends PropositionalFunction{

		
		/**
		 * Initializes to be evaluated on an agent object and landing pad object.
		 * @param name the name of the propositional function
		 * @param domain the domain of the propositional function
		 */
		public OnPadPF(String name, Domain domain) {
			super(name, domain, new String[]{AGENTCLASS, PADCLASS});
		}
		

		@Override
		public boolean isTrue(State st, String... params) {
	
			OldObjectInstance agent = st.getObject(params[0]);
			OldObjectInstance pad = st.getObject(params[1]);
			
			
			double l = pad.getRealValForAttribute(LATTNAME);
			double r = pad.getRealValForAttribute(RATTNAME);
			double t = pad.getRealValForAttribute(TATTNAME);
			
			double x = agent.getRealValForAttribute(XATTNAME);
			double y = agent.getRealValForAttribute(YATTNAME);
			
			//on pad means landed on surface, so y should be equal to top
			if(x > l && x < r && y == t){
				return true;
			}
			

			return false;
		}
		
		
		
	}
	
	
	
	
	/**
	 * A propositional function that evaluates to true if the agent is touching any part of the landing pad, including its
	 * side boundaries. This means this can evaluate to true even if the agent has not landed on the landing pad.
	 * @author James MacGlashan
	 *
	 */
	public class TouchPadPF extends PropositionalFunction{

		
		/**
		 * Initializes to be evaluated on an agent object and landing pad object.
		 * @param name the name of the propositional function
		 * @param domain the domain of the propositional function
		 */
		public TouchPadPF(String name, Domain domain) {
			super(name, domain, new String[]{AGENTCLASS, PADCLASS});
		}
		

		@Override
		public boolean isTrue(State st, String... params) {
	
			OldObjectInstance agent = st.getObject(params[0]);
			OldObjectInstance pad = st.getObject(params[1]);
			
			
			double l = pad.getRealValForAttribute(LATTNAME);
			double r = pad.getRealValForAttribute(RATTNAME);
			double b = pad.getRealValForAttribute(BATTNAME);
			double t = pad.getRealValForAttribute(TATTNAME);
			
			double x = agent.getRealValForAttribute(XATTNAME);
			double y = agent.getRealValForAttribute(YATTNAME);
			
			//on pad means landed on surface, so y should be equal to top
			if(x >= l && x < r && y >= b && y <= t){
				return true;
			}
			

			return false;
		}
		
		
		
	}
	
	
	
	
	/**
	 * A propositional function that evaluates to true if the agent is touching any part of an obstacle, including its
	 * side boundaries.
	 * @author James MacGlashan
	 *
	 */
	public class TouchSurfacePF extends PropositionalFunction{

		
		/**
		 * Initializes to be evaluated on an agent object and obstacle object.
		 * @param name the name of the propositional function
		 * @param domain the domain of the propositional function
		 */
		public TouchSurfacePF(String name, Domain domain) {
			super(name, domain, new String[]{AGENTCLASS, OBSTACLECLASS});
		}
		

		@Override
		public boolean isTrue(State st, String... params) {
			
			
			OldObjectInstance agent = st.getObject(params[0]);
			OldObjectInstance o = st.getObject(params[1]);
			double x = agent.getRealValForAttribute(XATTNAME);
			double y = agent.getRealValForAttribute(YATTNAME);
			
			double l = o.getRealValForAttribute(LATTNAME);
			double r = o.getRealValForAttribute(RATTNAME);
			double b = o.getRealValForAttribute(BATTNAME);
			double t = o.getRealValForAttribute(TATTNAME);
			
			if(x >= l && x <= r && y >= b && y <= t){
				return true;
			}
			
			return false;
		}
		
		
		
	}
	

	
	/**
	 * A propositional function that evaluates to true if the agent is touching the ground.
	 * @author James MacGlashan
	 *
	 */
	public class TouchGroundPF extends PropositionalFunction{

		/**
		 * Initializes to be evaluated on an agent object.
		 * @param name the name of the propositional function
		 * @param domain the domain of the propositional function
		 */
		public TouchGroundPF(String name, Domain domain) {
			super(name, domain, new String[]{AGENTCLASS});
		}
		

		@Override
		public boolean isTrue(State st, String... params) {
			
			OldObjectInstance agent = st.getObject(params[0]);
			double y = agent.getRealValForAttribute(YATTNAME);
			double ymin = agent.getObjectClass().domain.getAttribute(YATTNAME).lowerLim;
			
			if(y == ymin){
				return true;
			}
			
			return false;
		}
		
		
		
	}


	/**
	 * This method will launch a visual explorer for the lunar lander domain. It will use the default
	 * physics, start the agent on the left side of the world with a landing pad on the right
	 * and an obstacle in between. The agent is controlled with the following keys: <p>
	 * w: heavy thrust<p>
	 * s: weak thrust<p>
	 * a: turn/rotate counterclockwise<p>
	 * d: turn/rotate clockwise<p>
	 * x: idle (drift for one time step)
	 * <p>
	 * If you pass the main method "t" as an argument, a terminal explorer will be used instead of a visual explorer.
	 * @param args optionally pass "t" asn argument to use a terminal explorer instead of a visual explorer.
	 */
	public static void main(String[] args) {

		LunarLanderDomain lld = new LunarLanderDomain();
		Domain domain = lld.generateDomain();


		State clean = getCleanState(domain, 0);

		/*//these commented items just have different task configuration; just choose one
		lld.setAgent(clean, 0., 5, 0.);
		lld.setObstacle(clean, 0, 30., 45., 0., 20.);
		lld.setPad(clean, 75., 95., 0., 10.);
		*/

		/*
		lld.setAgent(clean, 0., 5, 0.);
		lld.setObstacle(clean, 0, 20., 40., 0., 20.);
		lld.setPad(clean, 65., 85., 0., 10.);
		*/


		setAgent(clean, 0., 5, 0.);
		//setObstacle(clean, 0, 20., 50., 0., 20.);
		setPad(clean, 80., 95., 0., 10.);


		int expMode = 1;

		if(args.length > 0){
			if(args[0].equals("v")){
				expMode = 1;
			}
			else if(args[0].equals("t")){
				expMode = 0;
			}
		}

		if(expMode == 0){

			TerminalExplorer te = new TerminalExplorer(domain, clean);

			te.explore();

		}
		else if(expMode == 1){

			Visualizer vis = LLVisualizer.getVisualizer(lld);
			VisualExplorer exp = new VisualExplorer(domain, vis, clean);

			exp.addKeyAction("w", ACTIONTHRUST + 0);
			exp.addKeyAction("s", ACTIONTHRUST + 1);
			exp.addKeyAction("a", ACTIONTURNL);
			exp.addKeyAction("d", ACTIONTURNR);
			exp.addKeyAction("x", ACTIONIDLE);

			exp.initGUI();

		}

	}
	
	

}
