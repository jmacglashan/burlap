package burlap.behavior.stochasticgames.agents.interfacing.singleagent;

import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.SGAgentBase;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.world.World;

/**
 * A stochastic games {@link SGAgent} that takes as input a single agent {@link burlap.behavior.singleagent.learning.LearningAgent}
 * to handle behavior. The interface from the single agent paradigm to the multi-agent paradigm is handled by this class
 * also implementing the {@link burlap.mdp.singleagent.environment.Environment} interface. When a game starts, a new
 * thread is launched in which the provided {@link burlap.behavior.singleagent.learning.LearningAgent} interacts with this
 * class's {@link burlap.mdp.singleagent.environment.Environment} methods.
 * @author James MacGlashan.
 */
public class LearningAgentToSGAgentInterface extends SGAgentBase implements Environment {


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

	protected int agentNum;


	/**
	 * Initializes.
	 * @param domain The stochastic games {@link burlap.mdp.stochasticgames.SGDomain} in which this agent will interact.
	 * @param learningAgent the {@link burlap.behavior.singleagent.learning.LearningAgent} that will handle this {@link SGAgent}'s control.
	 */
	public LearningAgentToSGAgentInterface(SGDomain domain, LearningAgent learningAgent, String agentName, SGAgentType agentType){
		this.init(domain, agentName, agentType);
		this.learningAgent = learningAgent;
		if(this.learningAgent instanceof MDPSolver){
			SADomain sadomain = new SADomain();
			for(ActionType actionType : agentType.actions){
				sadomain.addActionType(actionType);
			}
			((MDPSolver) this.learningAgent).setDomain(sadomain);

		}
	}


	@Override
	public void gameStarting(World w, int agentNum) {
		this.world = w;
		this.agentNum = agentNum;
	}

	@Override
	public Action action(State s) {


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

		Action toRet;
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
	public void observeOutcome(State s, JointAction jointAction, double[] jointReward, State sprime, boolean isTerminal) {
		this.lastReward = jointReward[this.agentNum];
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
	public State currentObservation() {
		return this.currentState;
	}

	@Override
	public EnvironmentOutcome executeAction(Action ga) {

		State prevState = this.currentState;
		synchronized(this.nextAction){
			this.nextAction.val = ga;
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
	public double lastReward() {
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
	 *  A wrapper that maintains a reference to a {@link Action} or null.
	 */
	protected static class ActionReference{
		protected Action val;
	}


	/**
	 *  A wrapper that maintains a reference to a {@link State} or null.
	 */
	protected static class StateReference{
		protected State val = null;
	}
}
