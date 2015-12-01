package burlap.behavior.singleagent.interfaces.rlglue;

import burlap.debugtools.DPrint;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.util.AgentLoader;

/**
 * A BURLAP {@link burlap.oomdp.singleagent.environment.Environment} that manages the interface to an RLGlue
 * Environment. To implement this interface, it requires that this class is both a BURLAP {@link burlap.oomdp.singleagent.environment.Environment}
 * and an RLGlue {@link org.rlcommunity.rlglue.codec.AgentInterface}, since BURLAP agents will communicate to the RL Glue
 * Environment through the an RLGlue {@link org.rlcommunity.rlglue.codec.AgentInterface}. However, you can use this
 * class like a normal BURLAP {@link burlap.oomdp.singleagent.environment.Environment}.
 * <br/><br/>
 * To initialize this environment, use the {@link #loadAgent()} or {@link #loadAgent(String, String)} methods so that
 * RLGlue knows to tell this class about the environment this class is wrapping. After the load method,
 * you can get the corresponding BURLAP domain that represents it by calling the {@link #getDomain()} method.
 * You can also get the RLGlue preferred discount factor using the {@link #getDiscountFactor()} method.
 * <br/><br/>
 * As with normal BURLAP {@link burlap.oomdp.singleagent.environment.Environment} implementations, you should call
 * the {@link #resetEnvironment()} method whenever a terminal state is reached. This will cause this class to
 * block until RLGlue has started a new episode with a new initial state, unless the RLGlue experiment has finished, in which case
 * the method will return immediately. If you want to check whether the RLGLue
 * experiment has finished manually, use the {@link #rlGlueExperimentFinished} method.
 * @author James MacGlashan.
 */
public class RLGlueEnvironmentInterface implements Environment, AgentInterface {

	/**
	 * Debug code used for printing debug information.
	 */
	protected int							debugCode = 836402;

	/**
	 * Whether to print debug statements.
	 */
	protected boolean						printDebug = false;


	/**
	 * Maintains the current RLGlue action to be performed.
	 */
	protected final MutableInt				nextAction = new MutableInt();

	/**
	 * Maintains the field to wait for the next state received from RLGlue
	 */
	protected  StateReference nextStateReference = new StateReference();


	/**
	 * The current state of the environment
	 */
	protected State curState;


	/**
	 * Whether the current state is a terminal state
	 */
	protected boolean curStateIsTerminal = false;


	/**
	 * The last reward received
	 */
	protected double lastReward = 0.;

	/**
	 * The BURLAP {@link Domain} specifying the RLGlue problem representation and action space.
	 */
	protected Domain domain;


	/**
	 * The RLGlue specified discount factor
	 */
	protected double discount = 0.;


	/**
	 * A variable for synchronized checking if the domain has been set.
	 */
	protected MutableInt domainSet = new MutableInt();


	protected boolean rlGlueExperimentFinished = false;


	/**
	 * Loads this RLGlue {@link org.rlcommunity.rlglue.codec.AgentInterface} into RLGlue and runs its event loop in a
	 * separate thread.
	 */
	public void loadAgent(){
		DPrint.toggleCode(debugCode, this.printDebug);
		final AgentLoader loader = new AgentLoader(this);
		Thread eventThread = new Thread(new Runnable() {
			@Override
			public void run() {
				loader.run();
			}
		});
		eventThread.start();

	}

	/**
	 * Loads this RLGlue {@link org.rlcommunity.rlglue.codec.AgentInterface} into RLGlue using the specified host address and port
	 * nd runs its event loop in a separate thread.
	 * @param hostAddress the RLGlue host address.
	 * @param portString the port on which to connect to RLGlue.
	 */
	public void loadAgent(String hostAddress, String portString){
		DPrint.toggleCode(debugCode, this.printDebug);
		final AgentLoader loader = new AgentLoader(hostAddress, portString, this);
		Thread eventThread = new Thread(new Runnable() {
			@Override
			public void run() {
				loader.run();
			}
		});
		eventThread.start();
	}

	/**
	 * Toggles whether debug information should be printed
	 * @param printDebug whether to print debug logs or not
	 */
	public void toggleDebug(boolean printDebug){
		this.printDebug = printDebug;
		DPrint.toggleCode(this.debugCode, this.printDebug);
	}


	public int getDebugCode() {
		return debugCode;
	}

	/**
	 * Returns the domain for this environment. This method will block until the domain for this environment is
	 * set by RLGLue via the {@link #agent_init(String)} method, which means you ought to have called
	 * {@link #loadAgent()} or {@link #loadAgent(String, String)} before calling this method, otherwise
	 * RLGlue will not know to set the environment.
	 * @return the BURLAP {@link burlap.oomdp.core.Domain} specification for this RLGlue environment.
	 */
	public Domain getDomain() {
		if(this.domainSet.val == null){
			synchronized(this.domainSet){
				while(this.domainSet.val == null){
					try {
						this.domainSet.wait();
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return domain;
	}

	/**
	 * Returns the discount factor for this environment. This method will block until the domain for this environment is
	 * set by RLGLue via the {@link #agent_init(String)} method, which means you ought to have called
	 * {@link #loadAgent()} or {@link #loadAgent(String, String)} before calling this method, otherwise
	 * RLGlue will not know to set the environment.
	 * @return the discount factor to use for this RLGlue problem.
	 */
	public double getDiscountFactor() {
		if(this.domainSet.val == null){
			synchronized(this.domainSet){
				while(this.domainSet.val == null){
					try {
						this.domainSet.wait();
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return discount;
	}

	/**
	 * Returns true if the RLGlue experiment is finished; false otherwise.
	 * @return true if the RLGlue experiment is finished; false otherwise
	 */
	public boolean rlGlueExperimentFinished(){
		return this.rlGlueExperimentFinished;
	}

	/**
	 * Blocks the calling thread until a state is provided by the RLGlue server or the RLGlue experiment has ended.
	 */
	public void blockUntilStateReceived(){
		synchronized(nextStateReference){
			while(this.nextStateReference.val == null && !this.rlGlueExperimentFinished){
				try{
					DPrint.cl(debugCode, "Waiting for state from RLGlue Server...");
					nextStateReference.wait();
				} catch(InterruptedException ex){
					ex.printStackTrace();
				}
			}
		}
	}

	@Override
	public void agent_init(String arg0) {

		DPrint.cl(debugCode, "Started init");
		DPrint.cl(debugCode, arg0);
		TaskSpec theTaskSpec = new TaskSpec(arg0);
		RLGlueDomain domainGenerator = new RLGlueDomain(theTaskSpec);
		this.discount = theTaskSpec.getDiscountFactor();
		this.domain = domainGenerator.generateDomain();
		synchronized(this.domainSet){
			this.domainSet.val = 1;
			this.domainSet.notifyAll();
		}


	}

	@Override
	public Action agent_start(Observation observation) {

		DPrint.cl(debugCode, "got agent start message, launching agent.");

		synchronized (nextStateReference) {
			this.curStateIsTerminal = false;
			this.lastReward = 0.;
			final State s = RLGlueDomain.stateFromObservation(domain, observation);
			this.curState = s;
			this.nextStateReference.val = s;
			nextStateReference.notifyAll();
		}



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
			toRet = getRLGlueAction(nextAction.val);
			nextAction.val = null;
		}

		DPrint.cl(debugCode, "Returning first action.");

		return toRet;
	}

	@Override
	public Action agent_step(double v, Observation observation) {

		DPrint.cl(this.debugCode, "Got agent step message");
		synchronized (nextStateReference) {
			nextStateReference.val = RLGlueDomain.stateFromObservation(this.domain, observation);
			this.lastReward = v;
			this.curState = nextStateReference.val;
			nextStateReference.notifyAll();
		}

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
			toRet = getRLGlueAction(nextAction.val);
			nextAction.val = null;
		}


		return toRet;
	}

	@Override
	public void agent_end(double v) {
		DPrint.cl(this.debugCode, "Got agent end message");
		synchronized (nextStateReference) {
			this.lastReward = v;
			this.curStateIsTerminal = true;
			nextStateReference.val = curState;
			nextStateReference.notifyAll();
		}
	}

	@Override
	public void agent_cleanup() {
		this.nextAction.val = null;
		this.nextStateReference.val = null;
		this.rlGlueExperimentFinished = true;
		synchronized(this.nextStateReference) {
			nextStateReference.notifyAll(); //notify to stop blocking
		}
	}

	@Override
	public String agent_message(String s) {
		return "BURLAP agent does not support messages.";
	}

	@Override
	public State getCurrentObservation() {
		if(this.curState == null){
			this.blockUntilStateReceived();
		}
		return this.curState;
	}

	@Override
	public EnvironmentOutcome executeAction(GroundedAction ga) {

		if(this.curState == null){
			this.blockUntilStateReceived();
		}

		if(!(ga.action instanceof RLGlueDomain.RLGlueActionSpecification)){
			throw new RuntimeException("RLGlueEnvironment cannot execute actions that are not instances of RLGlueDomain.RLGlueSpecification.");
		}

		State prevState = this.curState;

		int actionId = ((RLGlueDomain.RLGlueActionSpecification)ga.action).getInd();
		synchronized (nextAction) {
			this.nextStateReference.val = null;
			this.nextAction.val = actionId;
			this.nextAction.notifyAll();
		}

		DPrint.cl(debugCode, "Set action (" + this.nextAction.val + ")");

		State toRet = null;
		synchronized (this.nextStateReference) {
			while(this.nextStateReference.val == null){
				try{
					DPrint.cl(debugCode, "Waiting for state from RLGlue Server...");
					nextStateReference.wait();
				} catch(InterruptedException ex){
					ex.printStackTrace();
				}
			}
			toRet = this.curState;
			this.nextStateReference.val = null;
		}

		EnvironmentOutcome eo = new EnvironmentOutcome(prevState, ga, toRet, this.lastReward, this.curStateIsTerminal);

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
		this.blockUntilStateReceived();
	}


	/**
	 * Returns the corresponding RLGlue action for the given action id.
	 * @param id the action id
	 * @return An RLGlue action for the corresponding aciton id.
	 */
	public static org.rlcommunity.rlglue.codec.types.Action getRLGlueAction(int id){

		org.rlcommunity.rlglue.codec.types.Action act = new org.rlcommunity.rlglue.codec.types.Action();
		act.intArray = new int[]{id};

		return act;

	}


	/**
	 * A mutable int wrapper
	 * @author James MacGlashan
	 *
	 */
	public static class MutableInt{
		public Integer val = null;
	}


	/**
	 * A wrapper that maintains a reference to a {@link burlap.oomdp.core.states.State} or null.
	 * @author James MacGlashan
	 *
	 */
	public static class StateReference{
		public State val = null;
	}
}
