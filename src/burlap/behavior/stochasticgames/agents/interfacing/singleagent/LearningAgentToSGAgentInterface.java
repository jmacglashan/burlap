package burlap.behavior.stochasticgames.agents.interfacing.singleagent;

import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import burlap.oomdp.stochasticgames.SGAgent;
import burlap.oomdp.stochasticgames.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;

import java.util.Map;

/**
 * A stochastic games {@link burlap.oomdp.stochasticgames.SGAgent} that takes as input a single agent {@link burlap.behavior.singleagent.learning.LearningAgent}
 * to handle behavior. The interface from the single agent paradigm to the multi-agent paradigm is handled by this class
 * also implementing the {@link burlap.oomdp.singleagent.environment.Environment} interface. When a game starts, a new
 * thread is launched in which the provided {@link burlap.behavior.singleagent.learning.LearningAgent} interacts with this
 * class's {@link burlap.oomdp.singleagent.environment.Environment} methods.
 * <br/><br/>
 * When constructing a {@link burlap.behavior.singleagent.learning.LearningAgent} to use with this class, you should
 * set its {@link burlap.oomdp.core.Domain} to a {@link burlap.oomdp.core.Domain} generated from the {@link burlap.behavior.stochasticgames.agents.interfacing.singleagent.SGToSADomain},
 * which takes as input the standard {@link burlap.oomdp.stochasticgames.SGDomain}.
 * @author James MacGlashan.
 */
public class LearningAgentToSGAgentInterface extends SGAgent implements Environment {


	/**
	 * The single agent {@link burlap.behavior.singleagent.learning.LearningAgent} that will be learning
	 * in this stochastic game as if the other players are part of the environment.
	 */
	protected LearningAgent					learningAgent;


	/**
	 * Whether the last state was a terminal state
	 */
	protected boolean 						curStateIsTerminal = false;

	/**
	 * The last reward received by this agent
	 */
	protected double						lastReward;

	/**
	 * The current state of the world
	 */
	protected State							currentState;

	/**
	 * The thread that runs the single agent learning algorithm
	 */
	protected Thread						saThread;


	/**
	 * The next action selected by the single agent
	 */
	protected ActionReference				nextAction = new ActionReference();


	/**
	 * The next state received
	 */
	protected StateReference				nextState = new StateReference();


	/**
	 * Initializes.
	 * @param domain The stochastic games {@link burlap.oomdp.stochasticgames.SGDomain} in which this agent will interact.
	 * @param learningAgent the {@link burlap.behavior.singleagent.learning.LearningAgent} that will handle this {@link burlap.oomdp.stochasticgames.SGAgent}'s control.
	 */
	public LearningAgentToSGAgentInterface(SGDomain domain, LearningAgent learningAgent){
		this.init(domain);
		this.learningAgent = learningAgent;
	}


	@Override
	public void gameStarting() {
		//nothing to do
	}

	@Override
	public GroundedSGAgentAction getAction(State s) {


		synchronized(this.nextState){
			this.currentState = s;
			this.nextState.val = s;
			this.curStateIsTerminal = false;
			this.nextState.notifyAll();
		}


		if(this.saThread == null){
			this.saThread = new Thread(new Runnable() {
				@Override
				public void run() {
					learningAgent.runLearningEpisode(LearningAgentToSGAgentInterface.this);
				}
			});
			this.saThread.start();
		}

		GroundedSGAgentAction toRet = null;
		synchronized(nextAction){
			while(nextAction.val == null){
				try{
					nextAction.wait();
				} catch(InterruptedException ex){
					ex.printStackTrace();
				}
			}
			toRet = nextAction.val;
			nextAction.val = null;
		}


		return toRet;
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction, Map<String, Double> jointReward, State sprime, boolean isTerminal) {
		this.lastReward = jointReward.get(this.getAgentName());
		this.currentState = sprime;
	}

	@Override
	public void gameTerminated() {

		//notify the thread that it's terminal
		synchronized(this.nextState) {
			this.curStateIsTerminal = true;
			this.nextState.val = this.currentState;
			this.nextState.notifyAll();
		}

		//then join the thread to end it
		try {
			this.saThread.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		this.saThread = null;
	}

	@Override
	public State getCurrentObservation() {
		return this.currentState;
	}

	@Override
	public EnvironmentOutcome executeAction(GroundedAction ga) {

		State prevState = this.currentState;
		synchronized(this.nextAction){
			GroundedSGAgentAction gsa = new GroundedSGAgentAction(this.getAgentName(), this.domain.getSingleAction(ga.actionName()),ga.params);
			this.nextAction.val = gsa;
			this.nextAction.notifyAll();
		}


		synchronized(this.nextState){
			while(this.nextState.val == null){
				try{
					nextState.wait();
				} catch(InterruptedException ex){
					ex.printStackTrace();
				}
			}
			this.nextState.val = null;
		}

		EnvironmentOutcome eo = new EnvironmentOutcome(prevState, ga, this.currentState, this.lastReward, this.curStateIsTerminal);

		return eo;
	}

	@Override
	public double getLastReward() {
		return this.lastReward;
	}

	@Override
	public boolean isInTerminalState() {
		return this.curStateIsTerminal;
	}

	@Override
	public void resetEnvironment() {
		//nothing to do
	}


	/**
	 *  A wrapper that maintains a reference to a {@link burlap.oomdp.stochasticgames.GroundedSGAgentAction} or null.
	 */
	protected static class ActionReference{
		protected GroundedSGAgentAction val;
	}


	/**
	 *  A wrapper that maintains a reference to a {@link burlap.oomdp.core.states.State} or null.
	 */
	protected static class StateReference{
		protected State val = null;
	}
}
