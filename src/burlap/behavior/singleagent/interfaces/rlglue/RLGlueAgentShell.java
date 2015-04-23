package burlap.behavior.singleagent.interfaces.rlglue;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.util.AgentLoader;

import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * This class is used to to serve as an interface between a BURLAP learning agent and an RLGlue hosted environment and experiment.
 * When the RLGLue agent_init function is called, the RLGlue task spec will be parsed into a BURLAP domain and passed to
 * the agent generated along with a reward function, terminal function, and discount factor induced by the RLGlue environment.
 * The created BURLAP actions make calls to this interface which is then passed along to RLGlue. A BURLAP action call's
 * return state is stalled until RLGlue informs this interface what the resulting state is, which is then passed back to the agent.
 * 
 * @author James MacGlashan
 *
 */
public class RLGlueAgentShell implements AgentInterface{
	
	/**
	 * Debug code used for printing debug information.
	 */
	protected int							debugCode = 836402;
	
	/**
	 * Maintains the current RLGlue action to be performed.
	 */
	protected final MutableInt				nextAction = new MutableInt();
	
	/**
	 * Maintains the state received from RLGlue
	 */
	protected final MutableState			nextState = new MutableState();
	
	/**
	 * The BURALP learning agent factor
	 */
	protected RLGlueLearningAgentFactory 	buralpAgentFactory;
	
	/**
	 * The BURLAP domain that wraps the RLGlue environment
	 */
	protected Domain						domain;
	
	/**
	 * The BURLAP domain generator which can take as input an RLGlue task spec and produce a corresponding domain.
	 */
	protected RLGlueWrappedDomainGenerator	domainGenerator;
	
	/**
	 * The BURLAP learning agent that is being used.
	 */
	protected LearningAgent					burlapAgent;
	
	/**
	 * The last reward returned by RLGlue
	 */
	protected double 						lastReward;
	
	/**
	 * Whether the last state was a terminal state
	 */
	protected boolean						lastStateIsTerminal = false;
	
	
	/**
	 * The thread in which the current BURLAP learning algorithm thread is running
	 */
	protected Thread						burlapThread;
	
	
	/**
	 * Whether to print debug statements.
	 */
	protected boolean						printDebug = false;
	
	
	
	/**
	 * Initializes an RLGlue agent to use a BURLAP agent that will be generated from the specified factory.
	 * @param burlapAgentFactory the BURLAP learning agent factory to use
	 */
	public RLGlueAgentShell(RLGlueLearningAgentFactory burlapAgentFactory){
		this.buralpAgentFactory = burlapAgentFactory;
	}
	
	
	/**
	 * Loads this agent into RLGlue using the default host and port.
	 */
	public void loadAgent(){
		DPrint.toggleCode(debugCode, this.printDebug);
		AgentLoader loader = new AgentLoader(this);
		loader.run();
	}
	
	/**
	 * Loads this agent into RLGlue using the specified host address and port.
	 * @param hostAddress the RLGlue host address.
	 * @param portString the port on which to connect to RLGlue.
	 */
	public void loadAgent(String hostAddress, String portString){
		DPrint.toggleCode(debugCode, this.printDebug);
		AgentLoader loader = new AgentLoader(hostAddress, portString, this);
		loader.run();
	}
	
	/**
	 * Toggles whether debug information should be printed
	 * @param printDebug whether to print debug logs or not
	 */
	public void toggleDebug(boolean printDebug){
		this.printDebug = printDebug;
		DPrint.toggleCode(this.debugCode, this.printDebug);
	}
	
	@Override
	public void agent_cleanup() {
		this.nextAction.val = null;
		this.nextState.val = null;
		
	}

	@Override
	public void agent_end(double arg0) {
		
		synchronized (nextState) {
			nextState.val = this.domainGenerator.getTerminalState();
			this.lastReward = arg0;
			this.lastStateIsTerminal = true;
			nextState.notifyAll();
		}
		
		try {
			this.burlapThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void agent_init(String arg0) {
		DPrint.cl(debugCode, "Started init");
		DPrint.cl(debugCode, arg0);
		TaskSpec theTaskSpec = new TaskSpec(arg0);
		this.domainGenerator = new RLGlueWrappedDomainGenerator(this, theTaskSpec);
		this.domain = domainGenerator.generateDomain();
		double discount = theTaskSpec.getDiscountFactor();
		this.burlapAgent = this.buralpAgentFactory.generateAgentForRLDomain(this.domain, discount, new RLGlueRF(), new RLGlueTF());
		DPrint.cl(debugCode, "Finished init");
	}

	@Override
	public String agent_message(String arg0) {
		return "BURLAP agent does not support messages.";
	}

	@Override
	public Action agent_start(Observation arg0) {
		
		DPrint.cl(debugCode, "got agent start message, launching agent.");
		
		this.lastStateIsTerminal = false;
		
		final State s = this.domainGenerator.stateFromObservation(arg0);
		this.burlapThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				RLGlueAgentShell.this.burlapAgent.runLearningEpisodeFrom(s);
			}
		});
		burlapThread.start();
		
		
		Action toRet = null;
		synchronized (nextAction) {
			while(nextAction.val == null){
				try{
					DPrint.cl(debugCode, "Waiting for action...");
					nextAction.wait();
				} catch(InterruptedException ex){
					ex.printStackTrace();
				}
			}
			toRet = this.domainGenerator.getRLGlueAction(nextAction.val);
			nextAction.val = null;
		}
		
		DPrint.cl(debugCode, "Returning first action.");
		
		return toRet;
	}

	@Override
	public Action agent_step(double arg0, Observation arg1) {
		
		DPrint.cl(this.debugCode, "Got agent step message");
		
		synchronized (nextState) {
			nextState.val = this.domainGenerator.stateFromObservation(arg1);
			this.lastReward = arg0;
			nextState.notifyAll();
		}
		
		Action toRet = null;
		synchronized (nextAction) {
			while(nextAction.val == null){
				try{
					DPrint.cl(this.debugCode, "Waiting for action...");
					nextAction.wait();
				} catch(InterruptedException ex){
					ex.printStackTrace();
				}
			}
			toRet = this.domainGenerator.getRLGlueAction(nextAction.val);
			nextAction.val = null;
		}
		
		return toRet;
		
	}

	
	/**
	 * This method is called by the BURLAP actions and interfaces the action results and state return with RLGlue
	 * @param actionId the RLGlue action id
	 * @return the resulting state from applying RLGlue action actionID in the current state.
	 */
	public State actionCall(int actionId){
		
		DPrint.cl(debugCode, "Got action call message, setting action");
		
		synchronized (nextAction) {
			this.nextAction.val = actionId;
			this.nextAction.notifyAll();
		}
		
		DPrint.cl(debugCode, "Set action (" + this.nextAction.val + ")");
		
		State toRet = null;
		synchronized (this.nextState) {
			while(this.nextState.val == null){
				try{
					DPrint.cl(debugCode, "Waiting for state...");
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
	 * A mutable int wrapper
	 * @author James MacGlashan
	 *
	 */
	protected class MutableInt{
		protected Integer val = null;
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
	 * A reward function for returning the last RLGlue reward.
	 * @author James MacGlashan
	 *
	 */
	protected class RLGlueRF implements RewardFunction{

		@Override
		public double reward(State s, GroundedAction a, State sprime) {
			return RLGlueAgentShell.this.lastReward;
		}
		
	}
	
	
	/**
	 * A termianl function that returns true when the last RLGlue state was terminal.
	 * @author James MacGlashan
	 *
	 */
	protected class RLGlueTF implements TerminalFunction{

		@Override
		public boolean isTerminal(State s) {
			return RLGlueAgentShell.this.lastStateIsTerminal;
		}
		
		
	}
	
	
}
