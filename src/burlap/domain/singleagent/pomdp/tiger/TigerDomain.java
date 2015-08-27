package burlap.domain.singleagent.pomdp.tiger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Attribute.AttributeType;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.NullAction;
import burlap.oomdp.singleagent.pomdp.ObservationFunction;
import burlap.oomdp.singleagent.pomdp.PODomain;
import burlap.oomdp.singleagent.pomdp.beliefstate.tabular.TabularBeliefState;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;


/**
 * An implementation of the classic Tiger domain. In this problem an agent is faced with two closed doors side by side:
 * a left door and a right door. Behind one is a prize (or something corresponding to high reward); behind the other
 * is a tiger that will eat the agent. The goal is the for the agent to open the door with the prize. However, the catch
 * is that the agent cannot know for certain which door the tiger is behind. To gain information, the agent can listen to
 * the doors, which will produce a noisy observation regarding whether they hear the tiger behind the left or right door. Listening incurs a small
 * reward cost, choosing the door with the prize gives a large positive reward, and choosing the door with the tiger incurs a large
 * reward cost. This domain may optionally be specified to include a "do nothing" action which has no cost and provides no reward.
 * It is useful when testing an algorithms willingness to take exploratory actions.
 */
public class TigerDomain implements DomainGenerator {


	/**
	 * The attribute name that defines which door the tiger is behind
	 */
	public static final String				ATTTIGERDOOR = "behindDoor";

	/**
	 * The attribute name for an observation
	 */
	public static final String				ATTOBSERVATION = "observation";


	/**
	 * The class name for the object class that specifies where the tiger is
	 */
	public static final String				CLASSTIGER = "tiger";

	/**
	 * The class name for observation objects
	 */
	public static final String				CLASSOBSERVATION = "observation";


	/**
	 * The open left door action name
	 */
	public static final String				ACTIONLEFT = "openLeft";

	/**
	 * The open right door action name
	 */
	public static final String				ACTIONRIGHT = "openRight";

	/**
	 * The listen action name
	 */
	public static final String				ACTIONLISTEN = "listen";

	/**
	 * The do nothing action name
	 */
	public static final String				ACTIONDONOTHING = "doNothing";


	/**
	 * The discrete attribute value for the tiger being behind the left door
	 */
	public static final String				VALLEFT = "behindLeft";

	/**
	 * The discrete attribtue value for the tiger being behind the right door
	 */
	public static final String				VALRIGHT = "behindRight";


	/**
	 * The observation attribute value for hearing the tiger behind the left door.
	 */
	public static final String				OBHEARLEFT = "hearLeft";

	/**
	 * The observation attribute value for hearing the tiger behind the right door
	 */
	public static final String				OBHEARRIGHT = "hearRight";

	/**
	 * The observation value for when reaching a new pair of doors (occurs after opening a door)
	 */
	public static final String				OBRESET = "reset";

	/**
	 * The observation of hearing nothing (occurs when taking the do nothing action)
	 */
	public static final String				OBNOTHING = "hearNothing";


	/**
	 * Whether this domain should include the do nothing action or not
	 */
	protected boolean 						includeDoNothing = false;

	/**
	 * The probability of hearing accurately where the tiger is
	 */
	protected double						listenAccuracy = 0.85;

	/**
	 * Initializes. There will be no "do nothing" action and the listen accuracy will be set to 0.85
	 */
	public TigerDomain(){
		
	}

	/**
	 * Initializes. The listen accuracy will be set to 0.85
	 * @param includeDoNothing if true, then the do nothing action will be included; if false, then it will not be included
	 */
	public TigerDomain(boolean includeDoNothing){
		this.includeDoNothing = includeDoNothing;
	}

	/**
	 * Initializes
	 * @param includeDoNothing if true, then the do nothing action will be included; if false, then it will not be included
	 * @param listenAccuracy the listen accuracy
	 */
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
		new NullAction(ACTIONLISTEN, domain);
		if(this.includeDoNothing){
			new NullAction(ACTIONDONOTHING, domain);
		}
		
		new TigerObservations(domain, this.listenAccuracy);
		
		StateEnumerator senum = new StateEnumerator(domain, new SimpleHashableStateFactory());
		senum.getEnumeratedID(tigerLeftState(domain));
		senum.getEnumeratedID(tigerRightState(domain));
		
		domain.setStateEnumerator(senum);
		
		return domain;
	}

	/**
	 * Returns the hidden state for when the tiger is behind the left door
	 * @param domain the domain
	 * @return the hidden state for when the tiger is behind the left door
	 */
	public static State tigerLeftState(PODomain domain){
		State s = new MutableState();
		ObjectInstance o = new MutableObjectInstance(domain.getObjectClass(CLASSTIGER), CLASSTIGER);
		o.setValue(ATTTIGERDOOR, VALLEFT);
		s.addObject(o);
		return s;
	}


	/**
	 * Returns the hidden state for when the tiger is behind the right door
	 * @param domain the domain
	 * @return the hidden state for when the tiger is behind the right door
	 */
	public static State tigerRightState(PODomain domain){
		State s = new MutableState();
		ObjectInstance o = new MutableObjectInstance(domain.getObjectClass(CLASSTIGER), CLASSTIGER);
		o.setValue(ATTTIGERDOOR, VALRIGHT);
		s.addObject(o);
		return s;
	}


	/**
	 * Generates an initial {@link TabularBeliefState} in which the it is equally uncertain where the tiger is (50/50).
	 * @param domain the domain
	 * @return an initial {@link TabularBeliefState} in which the it is equally uncertain where the tiger is (50/50).
	 */
	public static TabularBeliefState getInitialBeliefState(PODomain domain){
		TabularBeliefState bs = new TabularBeliefState(domain, domain.getStateEnumerator());
		bs.initializeBeliefsUniformly();
		return bs;
	}


	/**
	 * Specifies an action for opening a door. When a door is opened, then the agent automatically faces a new pair of doors.
	 * With the tiger's position randomly specified
	 */
	public class OpenAction extends Action implements FullActionModel{

		public OpenAction(String actionName, Domain domain){
			super(actionName, domain);
		}
		
		@Override
		protected State performActionHelper(State s, GroundedAction ga) {
			
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
		public List<TransitionProbability> getTransitions(State s, GroundedAction ga){
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


	/**
	 * Defines the Tiger domain observation function
	 */
	public class TigerObservations extends ObservationFunction{

		protected double listenAccuracy;
		
		public TigerObservations(PODomain domain, double listenAccuracy){
			super(domain);
			this.listenAccuracy = listenAccuracy;
		}

		@Override
		public boolean canEnumerateObservations() {
			return true;
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

		@Override
		public List<ObservationProbability> getObservationProbabilities(State state, GroundedAction action) {
			return this.getObservationProbabilitiesByEnumeration(state, action);
		}

		/**
		 * Returns the observation of hearing the tiger behind the left door
		 * @return a {@link burlap.oomdp.core.states.State} specifying the observation of hearing the tiger behind the left door
		 */
		protected State observationLeft(){
			State hearLeft = new MutableState();
			ObjectInstance obL = new MutableObjectInstance(this.domain.getObjectClass(CLASSOBSERVATION), CLASSOBSERVATION);
			obL.setValue(ATTOBSERVATION, OBHEARLEFT);
			hearLeft.addObject(obL);
			return hearLeft;
		}


		/**
		 * Returns the observation of hearing the tiger behind the right door
		 * @return a {@link burlap.oomdp.core.states.State} specifying the observation of hearing the tiger behind the right door
		 */
		protected State observationRight(){
			State hearRight = new MutableState();
			ObjectInstance obR = new MutableObjectInstance(this.domain.getObjectClass(CLASSOBSERVATION), CLASSOBSERVATION);
			obR.setValue(ATTOBSERVATION, OBHEARRIGHT);
			hearRight.addObject(obR);
			return hearRight;
		}


		/**
		 * Returns the observation of approaching a new pair of doors
		 * @return a {@link burlap.oomdp.core.states.State} specifying the observation of approaching a new pair of doors
		 */
		protected State observationReset(){
			State reset = new MutableState();
			ObjectInstance obReset = new MutableObjectInstance(this.domain.getObjectClass(CLASSOBSERVATION), CLASSOBSERVATION);
			obReset.setValue(ATTOBSERVATION, OBRESET);
			reset.addObject(obReset);
			return reset;
		}


		/**
		 * Returns the observation of hearing nothing; occurs when the do nothing action is selected
		 * @return a {@link burlap.oomdp.core.states.State} specifying the observation of hearing nothing; occurs when the do nothing action is selected
		 */
		protected State observationNothing(){
			State nothing = new MutableState();
			ObjectInstance obNothing = new MutableObjectInstance(this.domain.getObjectClass(CLASSOBSERVATION), CLASSOBSERVATION);
			obNothing.setValue(ATTOBSERVATION, OBNOTHING);
			nothing.addObject(obNothing);
			return nothing;
		}
		
		
	}


	/**
	 * Defines the reward function for the tiger domain, which is defined by four values:
	 * opening the correct door with the prize; opening the wrong door with the tiger; listening; and doing nothing.
	 * By default these will have the values: 10, -100, -1, 0, respectively.
	 */
	public static class TigerRF implements RewardFunction{

		/**
		 * the reward for opening the correct door
		 */
		public double correctDoor = 10.;

		/**
		 * The reward for opening the wrong door
		 */
		public double wrongDoor = -100.;

		/**
		 * The reward for listening
		 */
		public double listen = -1.;

		/**
		 * The reward for do nothing.
		 */
		public double nothing = 0.;
		
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




//		TigerDomain dgen = new TigerDomain(false);
//		PODomain domain = (PODomain)dgen.generateDomain();
//
//		RewardFunction rf = new TigerRF();
//		TerminalFunction tf = new NullTermination();
//		BeliefSarsa sarsa = new BeliefSarsa(domain, rf, 0.99, 20, 1, false, 10., 0.1, 0.5, 10000);
//		BeliefState bs = TigerDomain.getInitialBeliefState(domain);
//
//		System.out.println("Begining sarsa planning.");
//		sarsa.planFromState(bs);
//		System.out.println("End sarsa planning.");
//
//		Policy p = new GreedyQPolicy(sarsa);
//
//		POEnvironment env = new POEnvironment(domain, rf, tf);
//		env.setCurPOMPDStateTo(bs.sampleStateFromBelief());
//
//		BeliefPolicyAgent agent = new BeliefPolicyAgent(domain, env, p);
//		agent.setEnvironment(env);
//		agent.setBeliefState(bs);
//		EpisodeAnalysis ea = agent.actUntilTerminalOrMaxSteps(20);
//
//		for(int i = 0; i < ea.numTimeSteps()-1; i++){
//			if(i > 0) {
//				String tval = ea.getState(i).getFirstObjectOfClass(CLASSOBSERVATION).getStringValForAttribute(ATTOBSERVATION);
//				System.out.println(tval + ": " + ea.getAction(i).toString() + "; " + ea.getReward(i + 1));
//			}
//			else{
//				System.out.println(ea.getAction(i).toString() + "; " + ea.getReward(i+1));
//			}
//		}
//
//		QMDP qmdp = new QMDP(domain, rf, tf, 0.99, new DiscreteStateHashFactory(), 0.01, 200);
//		System.out.println("Beginning QMDP Planning.");
//		qmdp.planFromState(bs);
//		System.out.println("Ending QMDP Planning.");
//		Policy qp = new GreedyQPolicy(qmdp);
//
//		BeliefPolicyAgent qagent = new BeliefPolicyAgent(domain, env, qp);
//		qagent.setEnvironment(env);
//		qagent.setBeliefState(bs);
//		ea = qagent.actUntilTerminalOrMaxSteps(20);
//
//		for(int i = 0; i < ea.numTimeSteps()-1; i++){
//			if(i > 0) {
//				String tval = ea.getState(i).getFirstObjectOfClass(CLASSOBSERVATION).getStringValForAttribute(ATTOBSERVATION);
//				System.out.println(tval + ": " + ea.getAction(i).toString() + "; " + ea.getReward(i + 1));
//			}
//			else{
//				System.out.println(ea.getAction(i).toString() + "; " + ea.getReward(i+1));
//			}
//		}
		
		
	}

}
