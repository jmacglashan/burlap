package burlap.behavior.stochasticgame.agents.interfacing.singleagent;

import java.util.Map;

import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SingleAction;



/**
 * For a number of reasons outside the scope of this class description, BURLAP single agent learning algorithms use a different interface for interacting
 * with the world than stochastic games agents do. Specifically, single agent learning algorithms make calls to actions that modify the world,
 * whereas in stochastic games, the world requests actions from the agent and then subsequently tells the agent about the results. This stochastic
 * games agent class provides an interface so that any BURALP single agent learning algorithm can be used in a stochastic games world. Specifically,
 * this class works by running the single agent learning algorithm in a separate thread and synchronizing action selection and state outcomes
 * between the two paradigms. The only information that neeeds to be provided to this class is the stochastic games domain in which
 * this agent will play and a special single agent learning algorithm factory to produce the single agent learning algorithm that will be used.
 * @author James MacGlashan
 *
 */
public class SingleAgentInterface extends Agent {

	
	/**
	 * The single agent version of the domain
	 */
	protected SADomain						saDomain = null;;
	
	/**
	 * The single agent learning factory
	 */
	protected SALearningAgentFactoryForSG	saAgentFactory;
	
	/**
	 * The BURLAP single agent learning agent that is being used.
	 */
	protected LearningAgent					saAgent = null;
	
	/**
	 * Whether the last state was a terminal state
	 */
	protected boolean						lastStateIsTerminal = false;
	
	
	/**
	 * The last reward received by this agent
	 */
	protected double						lastReward;
	
	
	/**
	 * whether a new single agent learning episode needs to be started for the next action request
	 */
	protected boolean						needsToStartEpisode = true;
	
	
	/**
	 * A mutable state holding the next state for the single agent
	 */
	protected MutableState					nextState = new MutableState();
	
	
	/**
	 * A mutable action holding the next action to be taken by the agent
	 */
	protected MutableGroundedSingleAction	nextAction = new MutableGroundedSingleAction();
	
	
	/**
	 * The thread that runs the single agent learning algorithm
	 */
	protected Thread						saThread;
	

	
	/**
	 * Initializes for a given stochastic games domain and a factory to produce the single agent learning object
	 * @param sgDomain the source stochastic games domain to be played
	 * @param saAgentFactory the single learning agent factory to use to perform learning and action selection
	 */
	public SingleAgentInterface(SGDomain sgDomain, SALearningAgentFactoryForSG saAgentFactory){
		this.domain = sgDomain;
		this.saAgentFactory = saAgentFactory;
	}
	
	
	@Override
	public void gameStarting() {
		
		this.lastStateIsTerminal = false;
		this.needsToStartEpisode = true;
		
		if(this.saAgent == null){
			SGToSADomain dg = new SGToSADomain(this.domain, this.agentType, this);
			this.saDomain = (SADomain)dg.generateDomain();
			this.saAgent = this.saAgentFactory.generateAgentForRLDomain(this.saDomain, new SARFWrapper(), new SATFWrapper());
		}

	}

	@Override
	public GroundedSingleAction getAction(State s) {
	
		if(this.needsToStartEpisode == true){
			
			final State fs = s;
			this.saThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					SingleAgentInterface.this.saAgent.runLearningEpisodeFrom(fs);
				}
			});
			saThread.start();
			
			this.needsToStartEpisode = false;
		}
		
		
		GroundedSingleAction toRet = null;
		synchronized (nextAction) {
			while(nextAction.gsa == null){
				try{
					//DPrint.cl(this.debugCode, "Waiting for action...");
					nextAction.wait();
				} catch(InterruptedException ex){
					ex.printStackTrace();
				}
			}
			toRet = this.nextAction.gsa;
			nextAction.gsa = null;
		}
		
		return toRet;

	}

	@Override
	public void observeOutcome(State s, JointAction jointAction,
			Map<String, Double> jointReward, State sprime, boolean isTerminal) {
		
		synchronized (this.nextState) {
			this.nextState.val = sprime;
			this.lastStateIsTerminal = isTerminal;
			this.lastReward = jointReward.get(this.worldAgentName);
			
			this.nextState.notifyAll();
		}
		
		if(isTerminal){
			try {
				this.saThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			this.needsToStartEpisode = true;
		}
		

	}

	@Override
	public void gameTerminated() {
		//nothing to do
	}
	
	
	
	/**
	 * A method that receives calls from the single agent domain actions to inform this stochastic games agent which action to take next when
	 * requested by the world.
	 * @param ga the single agent grounded aciton selection
	 * @return the state that will be the result of the agent applying the corresponding action in the stochastic games world.
	 */
	public State receiveSAAction(GroundedAction ga){
		
		SingleAction sgaction = this.domain.getSingleAction(ga.actionName());
		GroundedSingleAction gsa = new GroundedSingleAction(this.worldAgentName, sgaction, ga.params);
		
		synchronized (this.nextAction) {
			this.nextAction.gsa = gsa;
			this.nextAction.notifyAll();
		}
		
		State toRet = null;
		synchronized (this.nextState) {
			while(this.nextState.val == null){
				try{
					//DPrint.cl(debugCode, "Waiting for state...");
					nextState.wait();
				} catch(InterruptedException ex){
					ex.printStackTrace();
				}
			}
			toRet = this.nextState.val;
			this.nextState.val = null;
		}
		
		
		return toRet;
	}
	
	
	/**
	 * A mutable OO-MDP state wrapper
	 * @author James MacGlashan
	 *
	 */
	protected class MutableState{
		protected State val = null;
	}
	
	
	/**
	 * A mutable grounded singled action
	 * @author James MacGlashan
	 *
	 */
	protected class MutableGroundedSingleAction{
		protected GroundedSingleAction gsa = null;
	}

	
	
	/**
	 * A reward function for returning the last RLGlue reward.
	 * @author James MacGlashan
	 *
	 */
	protected class SARFWrapper implements RewardFunction{

		@Override
		public double reward(State s, GroundedAction a, State sprime) {
			return SingleAgentInterface.this.lastReward;
		}
		
	}
	
	
	/**
	 * A termianl function that returns true when the last RLGlue state was terminal.
	 * @author James MacGlashan
	 *
	 */
	protected class SATFWrapper implements TerminalFunction{

		@Override
		public boolean isTerminal(State s) {
			return SingleAgentInterface.this.lastStateIsTerminal;
		}
		
		
	}
		
	
}
