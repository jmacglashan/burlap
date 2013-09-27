package burlap.domain.singleagent.lunarlander;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.explorer.TerminalExplorer;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;



public class LunarLanderDomain implements DomainGenerator {

	public static final String				XATTNAME = "xAtt"; //x attribute
	public static final String				YATTNAME = "yAtt"; //y attribute
	
	public static final String				VXATTNAME = "vxAtt"; //velocity x attribute
	public static final String				VYATTNAME = "vyAtt"; //velocity y attribute
	
	public static final String				AATTNAME = "angAtt"; //angle of lander
	
	public static final String				LATTNAME = "lAtt"; //left boundary
	public static final String				RATTNAME = "rAtt"; //right boundary
	public static final String				BATTNAME = "bAtt"; //bottom boundary
	public static final String				TATTNAME = "tAtt"; //top boundary
	
	
	
	public static final String				AGENTCLASS = "agent";
	public static final String				OBSTACLECLASS = "obstacle";
	public static final String				PADCLASS = "goal";
	
	
	public static final String				ACTIONTURNL = "turnLeft";
	public static final String				ACTIONTURNR = "turnRight";
	public static final String				ACTIONTHRUST = "thrust";
	public static final String				ACTIONIDLE = "idle";
	
	
	public static final String				PFONPAD = "onLandingPad";
	public static final String				PFTPAD = "touchingLandingPad";
	public static final String				PFTOUCHSURFACE = "touchingSurface"; //either horizontally or landed on obstacle
	public static final String				PFONGROUND = "onGround"; //landed on ground
	
	
	
	
	//data members
	protected List <Double>					thrustValues;
	protected double						gravity = -0.2;
	protected double						xmin = 0.;
	protected double						xmax = 100.;
	protected double						ymin = 0.;
	protected double						ymax = 50.;
	protected double						vmax = 4.;
	protected double						angmax = Math.PI/4.;
	protected double						anginc = Math.PI/20.;
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		LunarLanderDomain lld = new LunarLanderDomain();
		Domain domain = lld.generateDomain();
		
		State clean = getCleanState(domain, 1);

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
		setObstacle(clean, 0, 20., 50., 0., 20.);
		setPad(clean, 80., 95., 0., 10.);
		
		
		int expMode = 1;
		
		if(args.length > 0){
			if(args[0].equals("v")){
				expMode = 1;
			}
		}
		
		if(expMode == 0){
			
			TerminalExplorer te = new TerminalExplorer(domain);
			
			te.addActionShortHand("a", ACTIONTURNL);
			te.addActionShortHand("d", ACTIONTURNR);
			te.addActionShortHand("w", ACTIONTHRUST+0);
			te.addActionShortHand("s", ACTIONTHRUST+1);
			te.addActionShortHand("x", ACTIONIDLE);
			
			te.exploreFromState(clean);
			
		}
		else if(expMode == 1){
			
			Visualizer vis = LLVisualizer.getVisualizer(lld);
			VisualExplorer exp = new VisualExplorer(domain, vis, clean);
			
			exp.addKeyAction("w", ACTIONTHRUST+0);
			exp.addKeyAction("s", ACTIONTHRUST+1);
			exp.addKeyAction("a", ACTIONTURNL);
			exp.addKeyAction("d", ACTIONTURNR);
			exp.addKeyAction("x", ACTIONIDLE);
			
			exp.initGUI();
			
		}

	}
	
	
	
	public static void setAgent(State s, double a, double x, double y){
		setAgent(s, a, x, y, 0., 0.);
	}
	
	public static void setAgent(State s, double a, double x, double y, double vx, double vy){
		ObjectInstance agent = s.getObjectsOfTrueClass(AGENTCLASS).get(0);
		
		agent.setValue(AATTNAME, a);
		agent.setValue(XATTNAME, x);
		agent.setValue(YATTNAME, y);
		agent.setValue(VXATTNAME, vx);
		agent.setValue(VYATTNAME, vy);
	}
	
	public static void setObstacle(State s, int i, double l, double r, double b, double t){
		ObjectInstance obst = s.getObjectsOfTrueClass(OBSTACLECLASS).get(i);
		
		obst.setValue(LATTNAME, l);
		obst.setValue(RATTNAME, r);
		obst.setValue(BATTNAME, b);
		obst.setValue(TATTNAME, t);
	}
	
	public static void setPad(State s, double l, double r, double b, double t){
		ObjectInstance pad = s.getObjectsOfTrueClass(PADCLASS).get(0);
		
		pad.setValue(LATTNAME, l);
		pad.setValue(RATTNAME, r);
		pad.setValue(BATTNAME, b);
		pad.setValue(TATTNAME, t);
	}
	
	
	
	
	public LunarLanderDomain(){
		thrustValues = new ArrayList<Double>();
	}
	
	
	public void addThrustActionWithThrust(double t){
		this.thrustValues.add(t);
	}
	
	public void setGravity(double g){
		this.gravity = g;
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
	
	
	public void setToStandardLunarLander(){
		this.addStandardThrustActions();
		this.gravity = -0.2;
		this.xmin = 0.;
		this.xmax = 100.;
		this.ymin = 0.;
		this.ymax = 50.;
		this.vmax = 4.;
		this.angmax = Math.PI / 4.;
		this.anginc = Math.PI / 20.;
	}
	
	public void addStandardThrustActions(){
		this.thrustValues.add(0.32);
		this.thrustValues.add(-gravity);
	}
	
	@Override
	public Domain generateDomain() {
		
		Domain domain = new SADomain();
		
		List <Double> thrustValuesTemp = this.thrustValues;
		if(thrustValuesTemp.size() == 0){
			thrustValuesTemp.add(0.32);
			thrustValuesTemp.add(-gravity);
		}
		
		
		//create attributes
		Attribute xatt = new Attribute(domain, XATTNAME, Attribute.AttributeType.REAL);
		xatt.setLims(xmin, xmax);
		
		Attribute yatt = new Attribute(domain, YATTNAME, Attribute.AttributeType.REAL);
		yatt.setLims(ymin, ymax);
		
		Attribute vxatt = new Attribute(domain, VXATTNAME, Attribute.AttributeType.REAL);
		vxatt.setLims(-vmax, vmax);
		
		Attribute vyatt = new Attribute(domain, VYATTNAME, Attribute.AttributeType.REAL);
		vyatt.setLims(-vmax, vmax);
		
		Attribute aatt = new Attribute(domain, AATTNAME, Attribute.AttributeType.REAL);
		aatt.setLims(-anginc, anginc);
		
		Attribute latt = new Attribute(domain, LATTNAME, Attribute.AttributeType.REAL);
		latt.setLims(xmin, xmax);
		
		Attribute ratt = new Attribute(domain, RATTNAME, Attribute.AttributeType.REAL);
		ratt.setLims(xmin, xmax);
		
		Attribute batt = new Attribute(domain, BATTNAME, Attribute.AttributeType.REAL);
		batt.setLims(ymin, ymax);
		
		Attribute tatt = new Attribute(domain, TATTNAME, Attribute.AttributeType.REAL);
		tatt.setLims(ymin, ymax);
		
		
		
		
		
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
		
		
		//add actions
		Action turnl = new ActionTurn(ACTIONTURNL, domain, -1.);
		Action turnr = new ActionTurn(ACTIONTURNR, domain, 1.);
		Action idle = new ActionIdle(ACTIONIDLE, domain, "");
		
		for(int i = 0; i < thrustValuesTemp.size(); i++){
			double t = thrustValuesTemp.get(i);
			Action thrustAction = new ActionThrust(ACTIONTHRUST+i, domain, t);
		}
		
		
		//add pfs
		PropositionalFunction onpad = new OnPadPF(PFONPAD, domain, new String[]{AGENTCLASS, PADCLASS});
		PropositionalFunction touchpad = new TouchPadPF(PFTPAD, domain, new String[]{AGENTCLASS, PADCLASS});
		PropositionalFunction touchsur = new TouchSurfacePF(PFTOUCHSURFACE, domain, new String[]{AGENTCLASS, OBSTACLECLASS});
		PropositionalFunction touchgrd = new TouchGroundPF(PFONGROUND, domain, new String[]{AGENTCLASS});
		
		
		
		return domain;
		
	}
	
	
	public static State getCleanState(Domain domain, int no){
		
		State s = new State();
		
		ObjectInstance agent = new ObjectInstance(domain.getObjectClass(AGENTCLASS), AGENTCLASS + "0");
		s.addObject(agent);
		
		ObjectInstance pad = new ObjectInstance(domain.getObjectClass(PADCLASS), PADCLASS + "0");
		s.addObject(pad);
		
		for(int i = 0; i < no; i++){
			ObjectInstance obst = new ObjectInstance(domain.getObjectClass(OBSTACLECLASS), OBSTACLECLASS + i);
			s.addObject(obst);
		}

		return s;
		
	}

	
	
	public void incAngle(State s, double dir){
		
		ObjectInstance agent = s.getObjectsOfTrueClass(AGENTCLASS).get(0);
		double curA = agent.getRealValForAttribute(AATTNAME);
		
		double newa = curA + (dir * anginc);
		if(newa > angmax){
			newa = angmax;
		}
		else if(newa < -angmax){
			newa = -angmax;
		}
		
		agent.setValue(AATTNAME, newa);
		
	}
	
	public void updateMotion(State s, double thrust){
		
		double ti = 1.;
		double tt = ti*ti;
		
		ObjectInstance agent = s.getObjectsOfTrueClass(AGENTCLASS).get(0);
		double ang = agent.getRealValForAttribute(AATTNAME);
		double x = agent.getRealValForAttribute(XATTNAME);
		double y = agent.getRealValForAttribute(YATTNAME);
		double vx = agent.getRealValForAttribute(VXATTNAME);
		double vy = agent.getRealValForAttribute(VYATTNAME);
		
		double worldAngle = (Math.PI/2.) - ang;
		
		double tx = Math.cos(worldAngle)*thrust;
		double ty = Math.sin(worldAngle)*thrust;
		
		double ax = tx;
		double ay = ty + gravity;
		
		double nx = x + vx*ti + (0.5*ax*tt);
		double ny = y + vy*ti + (0.5*ay*tt);
		
		double nvx = vx + ax*ti;
		double nvy = vy + ay*ti;
		
		double nang = ang;
		
		//check for boundaries
		if(ny > ymax){
			ny = ymax;
			nvy = 0.;
		}
		else if(ny <= ymin){
			ny = ymin;
			nvy = 0.;
			nang = 0.;
			nvx = 0.;
		}
		
		if(nx > xmax){
			nx = xmax;
			nvx = 0.;
		}
		else if(nx < xmin){
			nx = xmin;
			nvx = 0.;
		}
		
		if(nvx > vmax){
			nvx = vmax;
		}
		else if(nvx < -vmax){
			nvx = -vmax;
		}
		
		if(nvy > vmax){
			nvy = vmax;
		}
		else if(nvy < -vmax){
			nvy = -vmax;
		}
		
		
		
		//check for collisions
		List <ObjectInstance> obstacles = s.getObjectsOfTrueClass(OBSTACLECLASS);
		for(ObjectInstance o : obstacles){
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
		ObjectInstance pad = s.getObjectsOfTrueClass(PADCLASS).get(0);
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
	
	
	
	public class ActionTurn extends Action{

		double dir;
		
		public ActionTurn(String name, Domain domain, double dir) {
			super(name, domain, "");
			this.dir = dir;
		}
		
		

		@Override
		protected State performActionHelper(State st, String[] params) {
			incAngle(st, dir);
			updateMotion(st, 0.0);
			return st;
		}

		
	}
	
	
	
	
	public class ActionIdle extends Action{

		public ActionIdle(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public ActionIdle(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			updateMotion(st, 0.0);
			return st;
		}
		
		
		
	}
	
	
	public class ActionThrust extends Action{

		protected double thrustValue;
		
		public ActionThrust(String name, Domain domain, double thrustValue){
			super(name, domain, "");
			this.thrustValue = thrustValue;
		}
		
		
		@Override
		protected State performActionHelper(State st, String[] params) {
			updateMotion(st, thrustValue);
			return st;
		}
		
		
		
	}
	
	
	
	
	
	/*
	 * Returns true if the agent is not only touching the landing pad, but has landed on the surface
	 */
	
	public class OnPadPF extends PropositionalFunction{

		public OnPadPF(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public OnPadPF(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
	
			ObjectInstance agent = st.getObject(params[0]);
			ObjectInstance pad = st.getObject(params[1]);
			
			
			double l = pad.getRealValForAttribute(LATTNAME);
			double r = pad.getRealValForAttribute(RATTNAME);
			double b = pad.getRealValForAttribute(BATTNAME);
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
	
	
	/*
	 * Return true if the agent is touching the landing pad
	 */
	
	public class TouchPadPF extends PropositionalFunction{

		public TouchPadPF(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public TouchPadPF(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
	
			ObjectInstance agent = st.getObject(params[0]);
			ObjectInstance pad = st.getObject(params[1]);
			
			
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
	
	
	/*
	 * Return true if the agent is touching an obstacle surface
	 */
	
	public class TouchSurfacePF extends PropositionalFunction{

		public TouchSurfacePF(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public TouchSurfacePF(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			
			
			ObjectInstance agent = st.getObject(params[0]);
			ObjectInstance o = st.getObject(params[1]);
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
	
	/*
	 * Returns true if the agent is touching the ground
	 */
	public class TouchGroundPF extends PropositionalFunction{

		public TouchGroundPF(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public TouchGroundPF(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			
			ObjectInstance agent = st.getObject(params[0]);
			double y = agent.getRealValForAttribute(YATTNAME);
			
			if(y == ymin){
				return true;
			}
			
			return false;
		}
		
		
		
	}
	
	

}
