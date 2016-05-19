package burlap.domain.singleagent.pomdp.tiger;

import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.UniversalActionType;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.pomdp.PODomain;
import burlap.mdp.singleagent.pomdp.SimulatedPOEnvironment;
import burlap.mdp.singleagent.pomdp.beliefstate.TabularBeliefState;
import burlap.mdp.singleagent.pomdp.observations.ObservationFunction;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.shell.EnvironmentShell;


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
	 * the reward for opening the correct door
	 */
	public double correctDoorReward = 10.;

	/**
	 * The reward for opening the wrong door
	 */
	public double wrongDoorReward = -100.;

	/**
	 * The reward for listening
	 */
	public double listenReward = -1.;

	/**
	 * The reward for do nothing.
	 */
	public double nothingReward = 0.;


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

	public boolean isIncludeDoNothing() {
		return includeDoNothing;
	}

	public void setIncludeDoNothing(boolean includeDoNothing) {
		this.includeDoNothing = includeDoNothing;
	}

	public double getListenAccuracy() {
		return listenAccuracy;
	}

	public void setListenAccuracy(double listenAccuracy) {
		this.listenAccuracy = listenAccuracy;
	}

	public double getCorrectDoorReward() {
		return correctDoorReward;
	}

	public void setCorrectDoorReward(double correctDoorReward) {
		this.correctDoorReward = correctDoorReward;
	}

	public double getWrongDoorReward() {
		return wrongDoorReward;
	}

	public void setWrongDoorReward(double wrongDoorReward) {
		this.wrongDoorReward = wrongDoorReward;
	}

	public double getListenReward() {
		return listenReward;
	}

	public void setListenReward(double listenReward) {
		this.listenReward = listenReward;
	}

	public double getNothingReward() {
		return nothingReward;
	}

	public void setNothingReward(double nothingReward) {
		this.nothingReward = nothingReward;
	}

	@Override
	public Domain generateDomain() {
		
		PODomain domain = new PODomain();



		domain.addActionType(new UniversalActionType(ACTION_LEFT))
				.addActionType(new UniversalActionType(ACTION_RIGHT))
				.addActionType(new UniversalActionType(ACTION_LISTEN));

		if(this.includeDoNothing){
			domain.addActionType(new UniversalActionType(ACTION_DO_NOTHING));
		}


		ObservationFunction of = new TigerObservations(this.listenAccuracy, this.includeDoNothing);
		domain.setObservationFunction(of);

		TigerModel model = new TigerModel(correctDoorReward, wrongDoorReward, listenReward, nothingReward);
		domain.setModel(model);
		
		StateEnumerator senum = new StateEnumerator(domain, new SimpleHashableStateFactory());
		senum.getEnumeratedID(new TigerState(VAL_LEFT));
		senum.getEnumeratedID(new TigerState(VAL_RIGHT));
		
		domain.setStateEnumerator(senum);
		
		return domain;
	}


	/**
	 * Returns a {@link burlap.mdp.auxiliary.StateGenerator} that 50% of the time generates an hidden tiger state with the tiger on the
	 * left side, and 50% time on the right.
	 * @return a {@link burlap.mdp.auxiliary.StateGenerator}
	 */
	public static StateGenerator randomSideStateGenerator(){
		return randomSideStateGenerator(0.5);
	}

	/**
	 * Returns a {@link burlap.mdp.auxiliary.StateGenerator} that some of the of the time generates an hidden tiger state with the tiger on the
	 * left side, and others on the right. Probability of left side is specified with the argument probLeft
	 * @param probLeft the probability that a state with the tiger on the left side will be generated
	 * @return a {@link burlap.mdp.auxiliary.StateGenerator}
	 */
	public static StateGenerator randomSideStateGenerator(final double probLeft){
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
	 * Main method for interacting with the tiger domain via an {@link EnvironmentShell}
	 * By default, the TerminalExplorer interacts with the partially observable environment ({@link burlap.mdp.singleagent.pomdp.SimulatedPOEnvironment}),
	 * which means you only get to see the observations that the agent would. However, if you set the first command-line argument
	 * to be "h", then the explorer will explorer the underlying fully observable MDP states.
	 * @param args either empty or ["h"]; provide "h" to explorer the underlying fully observable tiger MDP.
	 */
	public static void main(String [] args){


		TigerDomain dgen = new TigerDomain(false);
		PODomain domain = (PODomain)dgen.generateDomain();

		StateGenerator tigerGenerator = TigerDomain.randomSideStateGenerator(0.5);

		Environment observableEnv = new SimulatedEnvironment(domain, tigerGenerator);
		Environment poEnv = new SimulatedPOEnvironment(domain, tigerGenerator);

		Environment envTouse = poEnv;
		if(args.length > 0 && args[0].equals("h")){
		    envTouse = observableEnv;
		}

		EnvironmentShell shell = new EnvironmentShell(domain, envTouse);
		shell.start();

		
		
	}

}
