package burlap.domain.singleagent.pomdp.tiger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.pomdp.BeliefMDPPolicyAgent;
import burlap.behavior.singleagent.pomdp.qmdp.QMDP;
import burlap.behavior.singleagent.pomdp.wrappedmdpalgs.BeliefSarsa;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Attribute.AttributeType;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.NullAction;
import burlap.oomdp.singleagent.pomdp.BeliefState;
import burlap.oomdp.singleagent.pomdp.ObservationFunction;
import burlap.oomdp.singleagent.pomdp.PODomain;
import burlap.oomdp.singleagent.pomdp.POEnvironment;

public class TigerDomain implements DomainGenerator {

	public static final String				ATTTIGERDOOR = "behindDoor";
	public static final String				ATTOBSERVATION = "observation";
	
	public static final String				CLASSTIGER = "tiger";
	public static final String				CLASSOBSERVATION = "observation";
	
	public static final String				ACTIONLEFT = "openLeft";
	public static final String				ACTIONRIGHT = "openRight";
	public static final String				ACTIONLISTEN = "listen";
	public static final String				ACTIONDONOTHING = "doNothing";
	
	
	public static final String				VALLEFT = "behindLeft";
	public static final String				VALRIGHT = "behindRight";
	
	public static final String				OBHEARLEFT = "hearLeft";
	public static final String				OBHEARRIGHT = "hearRight";
	public static final String				OBRESET = "reset";
	public static final String				OBNOTHING = "hearNothing";
	
	
	protected boolean 						includeDoNothing = false;
	protected double						listenAccuracy = 0.85;
	
	
	public TigerDomain(){
		
	}
	
	public TigerDomain(boolean includeDoNothing){
		this.includeDoNothing = includeDoNothing;
	}
	
	public TigerDomain(boolean includeDoNothing, double listenAccuracy){
		this.includeDoNothing = includeDoNothing;
		this.listenAccuracy = listenAccuracy;
	}
	
	@Override
	public Domain generateDomain() {
		
		PODomain domain = new PODomain();
		
		
		Attribute tigerAtt = new Attribute(domain, ATTTIGERDOOR, AttributeType.DISC);
		tigerAtt.setDiscValues(new String[]{VALLEFT, VALRIGHT});
		
		Attribute obAtt = new Attribute(domain, ATTOBSERVATION, AttributeType.DISC);
		obAtt.setDiscValues(new String[]{OBHEARLEFT,OBHEARRIGHT,OBRESET,OBNOTHING});
		
		ObjectClass tigerClass = new ObjectClass(domain, CLASSTIGER);
		tigerClass.addAttribute(tigerAtt);
		
		ObjectClass obClass = new ObjectClass(domain, CLASSOBSERVATION);
		obClass.addAttribute(obAtt);
		
		new OpenAction(ACTIONLEFT, domain);
		new OpenAction(ACTIONRIGHT, domain);
		new NullAction(ACTIONLISTEN, domain, "");
		if(this.includeDoNothing){
			new NullAction(ACTIONDONOTHING, domain, "");
		}
		
		new TigerObservations(domain, this.listenAccuracy);
		
		StateEnumerator senum = new StateEnumerator(domain, new DiscreteStateHashFactory());
		
		senum.getEnumeratedID(tigerLeftState(domain));
		senum.getEnumeratedID(tigerRightState(domain));
		
		domain.setStateEnumerator(senum);
		
		return domain;
	}
	
	
	public static State tigerLeftState(PODomain domain){
		State s = new State();
		ObjectInstance o = new ObjectInstance(domain.getObjectClass(CLASSTIGER), CLASSTIGER);
		o.setValue(ATTTIGERDOOR, VALLEFT);
		s.addObject(o);
		return s;
	}
	
	public static State tigerRightState(PODomain domain){
		State s = new State();
		ObjectInstance o = new ObjectInstance(domain.getObjectClass(CLASSTIGER), CLASSTIGER);
		o.setValue(ATTTIGERDOOR, VALRIGHT);
		s.addObject(o);
		return s;
	}
	
	public static BeliefState getInitialBeliefState(PODomain domain){
		BeliefState bs = new BeliefState(domain);
		bs.initializeBeliefsUniformly();
		return bs;
	}
	
	
	public class OpenAction extends Action{

		public OpenAction(String actionName, Domain domain){
			super(actionName, domain, "");
		}
		
		@Override
		protected State performActionHelper(State s, String[] params) {
			
			Random random = RandomFactory.getMapped(0);
			double r = random.nextDouble();
			
			if(r < 0.5){
				s.getFirstObjectOfClass(CLASSTIGER).setValue(ATTTIGERDOOR, VALLEFT);
			}
			else{
				s.getFirstObjectOfClass(CLASSTIGER).setValue(ATTTIGERDOOR, VALRIGHT);
			}
			
			return s;
		}
		
		@Override
		public List<TransitionProbability> getTransitions(State s, String [] params){
			List<TransitionProbability> tps = new ArrayList<TransitionProbability>(2);
			
			State left = s.copy();
			left.getFirstObjectOfClass(CLASSTIGER).setValue(ATTTIGERDOOR, VALLEFT);
			tps.add(new TransitionProbability(left, 0.5));
			
			State right = s.copy();
			right.getFirstObjectOfClass(CLASSTIGER).setValue(ATTTIGERDOOR, VALRIGHT);
			tps.add(new TransitionProbability(right, 0.5));
			
			return tps;
		}
		
		
	}
	
	
	
	public class TigerObservations extends ObservationFunction{

		protected double listenAccuracy;
		
		public TigerObservations(PODomain domain, double listenAccuracy){
			super(domain);
			this.listenAccuracy = listenAccuracy;
		}
		
		@Override
		public List<State> getAllPossibleObservations() {
			
			List<State> result = new ArrayList<State>(3);
			
			result.add(this.observationLeft());
			result.add(this.observationRight());
			result.add(this.observationReset());
			if(TigerDomain.this.includeDoNothing){
				result.add(this.observationNothing());
			}
			
			return result;
		}
		
		@Override
		public State sampleObservation(State state, GroundedAction action){
			//override for faster sampling
			if(action.actionName().equals(ACTIONLEFT) || action.actionName().equals(ACTIONRIGHT)){
				return this.observationReset();
			}
			else if(action.actionName().equals(ACTIONLISTEN)){
				String tigerVal = state.getFirstObjectOfClass(CLASSTIGER).getStringValForAttribute(ATTTIGERDOOR);
				double r = RandomFactory.getMapped(0).nextDouble();
				if(r < this.listenAccuracy){
					if(tigerVal.equals(VALLEFT)){
						return this.observationLeft();
					}
					else{
						return this.observationRight();
					}
				}
				else{
					//then nosiy listen; reverse direction
					if(tigerVal.equals(VALLEFT)){
						return this.observationRight();
					}
					else{
						return this.observationLeft();
					}
				}
			}
			else if(action.actionName().equals(ACTIONDONOTHING)){
				return this.observationNothing();
			}
			
			throw new RuntimeException("Unknown aciton " + action.actionName() + "; cannot return observation sample.");
		}

		@Override
		public double getObservationProbability(State observation, State state,
				GroundedAction action) {
			
			
			String oVal = observation.getFirstObjectOfClass(CLASSOBSERVATION).getStringValForAttribute(ATTOBSERVATION);
			String tigerVal = state.getFirstObjectOfClass(CLASSTIGER).getStringValForAttribute(ATTTIGERDOOR);
			
			if(action.actionName().equals(ACTIONLEFT) || action.actionName().equals(ACTIONRIGHT)){
				if(oVal.equals(OBRESET)){
					return 1.;
				}
				return 0.;
			}
			
			if(action.actionName().equals(ACTIONLISTEN)){
				if(tigerVal.equals(VALLEFT)){
					if(oVal.equals(OBHEARLEFT)){
						return this.listenAccuracy;
					}
					else if(oVal.equals(OBHEARRIGHT)){
						return 1.-this.listenAccuracy;
					}
					else{
						return 0.;
					}
				}
				else{
					if(oVal.equals(OBHEARLEFT)){
						return 1.-this.listenAccuracy;
					}
					else if(oVal.equals(OBHEARRIGHT)){
						return this.listenAccuracy;
					}
					else{
						return 0.;
					}
				}
			}
			
			//otherwise we're in the noop
			if(action.actionName().equals(ACTIONDONOTHING)){
				if(oVal.equals(OBNOTHING)){
					return 1.;
				}
				else{
					return 0.;
				}
			}
			
			throw new RuntimeException("Unknown aciton " + action.actionName() + "; cannot return observation probability.");
		}
		
		protected State observationLeft(){
			State hearLeft = new State();
			ObjectInstance obL = new ObjectInstance(this.domain.getObjectClass(CLASSOBSERVATION), CLASSOBSERVATION);
			obL.setValue(ATTOBSERVATION, OBHEARLEFT);
			hearLeft.addObject(obL);
			return hearLeft;
		}
		
		protected State observationRight(){
			State hearRight = new State();
			ObjectInstance obR = new ObjectInstance(this.domain.getObjectClass(CLASSOBSERVATION), CLASSOBSERVATION);
			obR.setValue(ATTOBSERVATION, OBHEARRIGHT);
			hearRight.addObject(obR);
			return hearRight;
		}
		
		protected State observationReset(){
			State reset = new State();
			ObjectInstance obReset = new ObjectInstance(this.domain.getObjectClass(CLASSOBSERVATION), CLASSOBSERVATION);
			obReset.setValue(ATTOBSERVATION, OBRESET);
			reset.addObject(obReset);
			return reset;
		}
		
		protected State observationNothing(){
			State nothing = new State();
			ObjectInstance obNothing = new ObjectInstance(this.domain.getObjectClass(CLASSOBSERVATION), CLASSOBSERVATION);
			obNothing.setValue(ATTOBSERVATION, OBNOTHING);
			nothing.addObject(obNothing);
			return nothing;
		}
		
		
	}
	
	public static class TigerRF implements RewardFunction{

		protected double correctDoor = 10.;
		protected double wrongDoor = -100.;
		protected double listen = -1.;
		protected double nothing = 0.;
		
		@Override
		public double reward(State s, GroundedAction a, State sprime) {
			
			
			if(a.actionName().equals(ACTIONLEFT)){
				String tigerVal = s.getFirstObjectOfClass(CLASSTIGER).getStringValForAttribute(ATTTIGERDOOR);
				
				if(tigerVal.equals(VALLEFT)){
					return wrongDoor;
				}
				else{
					return correctDoor;
				}
			}
			else if(a.actionName().equals(ACTIONRIGHT)){
				String tigerVal = s.getFirstObjectOfClass(CLASSTIGER).getStringValForAttribute(ATTTIGERDOOR);
				if(tigerVal.equals(VALRIGHT)){
					return wrongDoor;
				}
				else{
					return correctDoor;
				}
			}
			else if(a.actionName().equals(ACTIONLISTEN)){
				return listen;
			}
			else if(a.actionName().equals(ACTIONDONOTHING)){
				return nothing;
			}
			
			
			throw new RuntimeException("Cannot return reward; unknown action: " + a.actionName());
		}
		
		
		
	}
	
	
	
	public static void main(String [] args){
		TigerDomain dgen = new TigerDomain(true);
		PODomain domain = (PODomain)dgen.generateDomain();
		
		RewardFunction rf = new TigerRF();
		TerminalFunction tf = new NullTermination();
		BeliefSarsa sarsa = new BeliefSarsa(domain, rf, tf, 0.99, 20, 1, true, 10., 0.1, 0.5, 10000);
		BeliefState bs = TigerDomain.getInitialBeliefState(domain);
		
		System.out.println("Begining sarsa planning.");
		sarsa.planFromBeliefState(bs);
		System.out.println("End sarsa planning.");
		
		Policy p = new GreedyQPolicy(sarsa);
		
		POEnvironment env = new POEnvironment(domain, rf, tf);
		env.setCurMPDStateTo(bs.sampleStateFromBelief());
		
		BeliefMDPPolicyAgent agent = new BeliefMDPPolicyAgent(domain, p);
		agent.setEnvironment(env);
		agent.setBeliefState(bs);
		EpisodeAnalysis ea = agent.actUntilTerminalOrMaxSteps(20);
		
		for(int i = 0; i < ea.numTimeSteps()-1; i++){
			String tval = ea.getState(i).getFirstObjectOfClass(CLASSTIGER).getStringValForAttribute(ATTTIGERDOOR);
			System.out.println(tval + ": " + ea.getAction(i).toString());
		}
		
		QMDP qmdp = new QMDP(domain, rf, tf, 0.99, new DiscreteStateHashFactory(), 0.01, 200);
		System.out.println("Beginning QMDP Planning.");
		qmdp.planFromBeliefState(bs);
		System.out.println("Ending QMDP Planning.");
		Policy qp = new GreedyQPolicy(qmdp);
		
		BeliefMDPPolicyAgent qagent = new BeliefMDPPolicyAgent(domain, qp);
		qagent.setEnvironment(env);
		qagent.setBeliefState(bs);
		ea = qagent.actUntilTerminalOrMaxSteps(20);
		
		for(int i = 0; i < ea.numTimeSteps()-1; i++){
			String tval = ea.getState(i).getFirstObjectOfClass(CLASSTIGER).getStringValForAttribute(ATTTIGERDOOR);
			System.out.println(tval + ": " + ea.getAction(i).toString());
		}
		
		
	}

}
