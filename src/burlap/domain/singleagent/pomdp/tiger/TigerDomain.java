package burlap.domain.singleagent.pomdp.tiger;

import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.TransitionProbability;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.FullActionModel;
import burlap.mdp.singleagent.GroundedAction;
import burlap.mdp.singleagent.RewardFunction;
import burlap.mdp.singleagent.common.NullAction;
import burlap.mdp.singleagent.common.SimpleAction;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.pomdp.ObservationFunction;
import burlap.mdp.singleagent.pomdp.PODomain;
import burlap.mdp.singleagent.pomdp.SimulatedPOEnvironment;
import burlap.mdp.singleagent.pomdp.beliefstate.tabular.TabularBeliefState;
import burlap.mdp.statehashing.SimpleHashableStateFactory;
import burlap.shell.EnvironmentShell;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


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
	public static final String VAR_DOOR = "behindDoor";

	/**
	 * The variable key for an observation
	 */
	public static final String VAR_HEAR = "observation";



	/**
	 * The open left door action name
	 */
	public static final String ACTION_LEFT = "openLeft";

	/**
	 * The open right door action name
	 */
	public static final String ACTION_RIGHT = "openRight";

	/**
	 * The listen action name
	 */
	public static final String ACTION_LISTEN = "listen";

	/**
	 * The do nothing action name
	 */
	public static final String ACTION_DO_NOTHING = "doNothing";


	/**
	 * The discrete attribute value for the tiger being behind the left door
	 */
	public static final String VAL_LEFT = "behindLeft";

	/**
	 * The discrete attribtue value for the tiger being behind the right door
	 */
	public static final String VAL_RIGHT = "behindRight";


	/**
	 * The observation attribute value for hearing the tiger behind the left door.
	 */
	public static final String HEAR_LEFT = "hearLeft";

	/**
	 * The observation attribute value for hearing the tiger behind the right door
	 */
	public static final String HEAR_RIGHT = "hearRight";

	/**
	 * The observation value for when reaching a new pair of doors (occurs after opening a door)
	 */
	public static final String DOOR_RESET = "reset";

	/**
	 * The observation of hearing nothing (occurs when taking the do nothing action)
	 */
	public static final String HEAR_NOTHING = "hearNothing";


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

		
		new OpenAction(ACTION_LEFT, domain);
		new OpenAction(ACTION_RIGHT, domain);
		new NullAction(ACTION_LISTEN, domain);
		if(this.includeDoNothing){
			new NullAction(ACTION_DO_NOTHING, domain);
		}
		
		new TigerObservations(domain, this.listenAccuracy);
		
		StateEnumerator senum = new StateEnumerator(domain, new SimpleHashableStateFactory());
		senum.getEnumeratedID(new TigerState(VAL_LEFT));
		senum.getEnumeratedID(new TigerState(VAL_RIGHT));
		
		domain.setStateEnumerator(senum);
		
		return domain;
	}


	/**
	 * Returns a {@link burlap.mdp.auxiliary.StateGenerator} that 50% of the time generates an hidden tiger state with the tiger on the
	 * left side, and 50% time on the right.
	 * @param domain the Tiger domain object
	 * @return a {@link burlap.mdp.auxiliary.StateGenerator}
	 */
	public static StateGenerator randomSideStateGenerator(final PODomain domain){
		return randomSideStateGenerator(domain, 0.5);
	}

	/**
	 * Returns a {@link burlap.mdp.auxiliary.StateGenerator} that some of the of the time generates an hidden tiger state with the tiger on the
	 * left side, and others on the right. Probability of left side is specified with the argument probLeft
	 * @param domain the Tiger domain object
	 * @param probLeft the probability that a state with the tiger on the left side will be generated
	 * @return a {@link burlap.mdp.auxiliary.StateGenerator}
	 */
	public static StateGenerator randomSideStateGenerator(final PODomain domain, final double probLeft){
		return new StateGenerator() {
			@Override
			public State generateState() {
				double roll = RandomFactory.getMapped(0).nextDouble();
				return roll < probLeft ? new TigerState(VAL_LEFT) : new TigerState(VAL_RIGHT);
			}
		};
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
	public class OpenAction extends SimpleAction implements FullActionModel{

		public OpenAction(String actionName, Domain domain){
			super(actionName, domain);
		}
		
		@Override
		protected State performActionHelper(State s, GroundedAction ga) {
			
			Random random = RandomFactory.getMapped(0);
			double r = random.nextDouble();



			if(r < 0.5){
				((MutableState)s).set(VAR_DOOR, VAL_LEFT);
			}
			else{
				((MutableState)s).set(VAR_DOOR, VAL_RIGHT);
			}
			
			return s;
		}
		
		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction ga){
			List<TransitionProbability> tps = new ArrayList<TransitionProbability>(2);
			
			State left = s.copy();
			((MutableState)left).set(VAR_DOOR, VAL_LEFT);
			tps.add(new TransitionProbability(left, 0.5));
			
			State right = s.copy();
			((MutableState)right).set(VAR_DOOR, VAL_RIGHT);
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
			if(action.actionName().equals(ACTION_LEFT) || action.actionName().equals(ACTION_RIGHT)){
				return this.observationReset();
			}
			else if(action.actionName().equals(ACTION_LISTEN)){
				String tigerVal = (String)state.get(VAR_DOOR);
				double r = RandomFactory.getMapped(0).nextDouble();
				if(r < this.listenAccuracy){
					if(tigerVal.equals(VAL_LEFT)){
						return this.observationLeft();
					}
					else{
						return this.observationRight();
					}
				}
				else{
					//then nosiy listen; reverse direction
					if(tigerVal.equals(VAL_LEFT)){
						return this.observationRight();
					}
					else{
						return this.observationLeft();
					}
				}
			}
			else if(action.actionName().equals(ACTION_DO_NOTHING)){
				return this.observationNothing();
			}
			
			throw new RuntimeException("Unknown action " + action.actionName() + "; cannot return observation sample.");
		}

		@Override
		public double getObservationProbability(State observation, State state,
				GroundedAction action) {
			
			
			String oVal = (String)observation.get(VAR_HEAR);
			String tigerVal = (String)state.get(VAR_DOOR);
			
			if(action.actionName().equals(ACTION_LEFT) || action.actionName().equals(ACTION_RIGHT)){
				if(oVal.equals(DOOR_RESET)){
					return 1.;
				}
				return 0.;
			}
			
			if(action.actionName().equals(ACTION_LISTEN)){
				if(tigerVal.equals(VAL_LEFT)){
					if(oVal.equals(HEAR_LEFT)){
						return this.listenAccuracy;
					}
					else if(oVal.equals(HEAR_RIGHT)){
						return 1.-this.listenAccuracy;
					}
					else{
						return 0.;
					}
				}
				else{
					if(oVal.equals(HEAR_LEFT)){
						return 1.-this.listenAccuracy;
					}
					else if(oVal.equals(HEAR_RIGHT)){
						return this.listenAccuracy;
					}
					else{
						return 0.;
					}
				}
			}
			
			//otherwise we're in the noop
			if(action.actionName().equals(ACTION_DO_NOTHING)){
				if(oVal.equals(HEAR_NOTHING)){
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
		 * @return a {@link State} specifying the observation of hearing the tiger behind the left door
		 */
		protected State observationLeft(){
			return new TigerObservation(HEAR_LEFT);
		}


		/**
		 * Returns the observation of hearing the tiger behind the right door
		 * @return a {@link State} specifying the observation of hearing the tiger behind the right door
		 */
		protected State observationRight(){
			return new TigerObservation(HEAR_RIGHT);
		}


		/**
		 * Returns the observation of approaching a new pair of doors
		 * @return a {@link State} specifying the observation of approaching a new pair of doors
		 */
		protected State observationReset(){
			return new TigerObservation(DOOR_RESET);
		}


		/**
		 * Returns the observation of hearing nothing; occurs when the do nothing action is selected
		 * @return a {@link State} specifying the observation of hearing nothing; occurs when the do nothing action is selected
		 */
		protected State observationNothing(){
			return new TigerObservation(HEAR_NOTHING);
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
			
			
			if(a.actionName().equals(ACTION_LEFT)){
				String tigerVal = (String)s.get(VAR_DOOR);
				
				if(tigerVal.equals(VAL_LEFT)){
					return wrongDoor;
				}
				else{
					return correctDoor;
				}
			}
			else if(a.actionName().equals(ACTION_RIGHT)){
				String tigerVal = (String)s.get(VAR_DOOR);
				if(tigerVal.equals(VAL_RIGHT)){
					return wrongDoor;
				}
				else{
					return correctDoor;
				}
			}
			else if(a.actionName().equals(ACTION_LISTEN)){
				return listen;
			}
			else if(a.actionName().equals(ACTION_DO_NOTHING)){
				return nothing;
			}
			
			
			throw new RuntimeException("Cannot return reward; unknown action: " + a.actionName());
		}
		
		
		
	}


	/**
	 * Main method for interacting with the tiger domain via an {@link EnvironmentShell}
	 * By default, the TerminalExplorer interacts with the partially observable environment ({@link burlap.mdp.singleagent.pomdp.SimulatedPOEnvironment}),
	 * which means you only get to see the observations that the agent would. However, if you set the first command-line argument
	 * to be "h", then the explorer will explorer the underlying fully observable MDP states.
	 * @param args either empty or ["h"]; provide "h" to explorer the underlying fully observable tiger MDP.
	 */
	public static void main(String [] args){


		TigerDomain dgen = new TigerDomain(false);
		PODomain domain = (PODomain)dgen.generateDomain();

		RewardFunction rf = new TigerRF();
		TerminalFunction tf = new NullTermination();
		StateGenerator tigerGenerator = TigerDomain.randomSideStateGenerator(domain, 0.5);

		Environment observableEnv = new SimulatedEnvironment(domain, rf, tf, tigerGenerator);
		Environment poEnv = new SimulatedPOEnvironment(domain, rf, tf, tigerGenerator);

		Environment envTouse = poEnv;
		if(args.length > 0 && args[0].equals("h")){
		    envTouse = observableEnv;
		}

		EnvironmentShell shell = new EnvironmentShell(domain, envTouse);
		shell.start();

		
		
	}

}
