package burlap.oomdp.singleagent.environment;

import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * @author James MacGlashan.
 */
public class SimulatedEnvironment implements Environment, StateSettableEnvironment{

	/**
	 * The domain of this environment
	 */
	protected Domain domain;

	/**
	 * The reward function of this environment
	 */
	protected RewardFunction rf;

	/**
	 * The terminal function for this environment
	 */
	protected TerminalFunction tf;

	/**
	 * The state generator used to generate new states when the environment is reset with {@link #resetEnvironment()};
	 */
	protected StateGenerator stateGenerator;

	/**
	 * The current state of the environment
	 */
	protected State curState;

	/**
	 * The last reward generated from this environment.
	 */
	protected double lastReward = 0.;

	/**
	 * A flag indicating whether the environment will respond to actions from a terminal state. If false,
	 * then once a the environment transitions to a terminal state, any action attempted by the {@link #executeAction(burlap.oomdp.singleagent.GroundedAction)}
	 * method will result in no change in state and to enable action again, the Environment state will have to be
	 * manually changed with the {@link #resetEnvironment()} method or the {@link #setCurStateTo(burlap.oomdp.core.State)} method.
	 * If this value is true, then actions will be carried out according to the domain's transition dynamics.
	 */
	protected boolean allowActionFromTerminalStates = false;

	public SimulatedEnvironment(Domain domain, RewardFunction rf, TerminalFunction tf){
		this.domain = domain;
		this.rf = rf;
		this.tf = tf;
	}

	public SimulatedEnvironment(Domain domain, RewardFunction rf, TerminalFunction tf, State initialState) {
		this.domain = domain;
		this.rf = rf;
		this.tf = tf;
		this.stateGenerator = new ConstantStateGenerator(initialState);
		this.curState = initialState;
	}

	public SimulatedEnvironment(Domain domain, RewardFunction rf, TerminalFunction tf, StateGenerator stateGenerator) {
		this.domain = domain;
		this.rf = rf;
		this.tf = tf;
		this.stateGenerator = stateGenerator;
		this.curState = stateGenerator.generateState();
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public RewardFunction getRf() {
		return rf;
	}

	public void setRf(RewardFunction rf) {
		this.rf = rf;
	}

	public TerminalFunction getTf() {
		return tf;
	}

	public void setTf(TerminalFunction tf) {
		this.tf = tf;
	}

	public StateGenerator getStateGenerator() {
		return stateGenerator;
	}

	public void setStateGenerator(StateGenerator stateGenerator) {
		this.stateGenerator = stateGenerator;
	}

	/**
	 * Sets whether the environment will respond to actions from a terminal state. If false,
	 * then once a the environment transitions to a terminal state, any action attempted by the {@link #executeAction(burlap.oomdp.singleagent.GroundedAction)}
	 * method will result in no change in state and to enable action again, the Environment state will have to be
	 * manually changed with the {@link #resetEnvironment()} method or the {@link #setCurStateTo(burlap.oomdp.core.State)} method.
	 * If this value is true, then actions will be carried out according to the domain's transition dynamics.
	 * @param allowActionFromTerminalStates if false, then actions are not allowed from terminal states; if true, then they are allowed.
	 */
	public void setAllowActionFromTerminalStates(boolean allowActionFromTerminalStates){
		this.allowActionFromTerminalStates = true;
	}

	@Override
	public void setCurStateTo(State s) {
		if(this.stateGenerator == null){
			this.stateGenerator = new ConstantStateGenerator(s);
		}
		this.curState = s;
	}

	@Override
	public State getCurState() {
		return this.curState.copy();
	}

	@Override
	public EnvironmentOutcome executeAction(GroundedAction ga) {

		GroundedAction simGA = (GroundedAction)ga.copy();
		simGA.action = this.domain.getAction(ga.actionName());
		if(simGA.action == null){
			throw new RuntimeException("Cannot execute action " + ga.toString() + " in this SimulatedEnvironment because the action is to known in this Environment's domain");
		}
		State nextState;
		if(this.allowActionFromTerminalStates || !this.curStateIsTerminal()) {
			nextState = simGA.executeIn(this.curState);
			this.lastReward = this.rf.reward(this.curState, simGA, nextState);
		}
		else{
			nextState = this.curState;
			this.lastReward = 0.;
		}

		EnvironmentOutcome eo = new EnvironmentOutcome(this.curState.copy(), ga, nextState.copy(), this.lastReward, this.tf.isTerminal(nextState));

		this.curState = nextState;

		return eo;
	}

	@Override
	public double getLastReward() {
		return this.lastReward;
	}

	@Override
	public boolean curStateIsTerminal() {
		return this.tf.isTerminal(this.curState);
	}

	@Override
	public void resetEnvironment() {
		this.lastReward = 0.;
		this.curState = stateGenerator.generateState();
	}
}
