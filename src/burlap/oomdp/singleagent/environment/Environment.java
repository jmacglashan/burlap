package burlap.oomdp.singleagent.environment;

import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * In some cases it may be useful to have agents interact with an external environment that handles the current state, execution of actions, and rewards
 * and maintains other important external information, rather than use the standard Domain, Action, RewardFunction TerminalFunction paradigm of BURLAP.
 * In particular, Environments may be useful in real time systems in which actions take some length of time to occur (such as physical robots), or in
 * which human interaction plays a critical role in affecting state, rewards, or termination events. This Environment abstract class provides an interface
 * to manage these kinds of scenarios.
 * 
 * @author James MacGlashan
 *
 */
public abstract class Environment {

	protected State curState;
	
	
	public void setCurStateTo(State s){
		this.curState = s;
	}
	
	public State getCurState(){
		return curState;
	}
	
	
	public RewardFunction getEnvironmentRewardRFWrapper(){
		return new LastRewardRF();
	}
	
	public TerminalFunction getEnvironmentTerminalStateTFWrapper(){
		return new CurStateTerminalTF();
	}
	
	public abstract State executeAction(String aname, String [] params);
	public abstract double getLastReward();
	public abstract boolean curStateIsTerminal();
	
	
	public class LastRewardRF implements RewardFunction{

		@Override
		public double reward(State s, GroundedAction a, State sprime) {
			return Environment.this.getLastReward();
		}
		
	}
	
	public class CurStateTerminalTF implements TerminalFunction{

		@Override
		public boolean isTerminal(State s) {
			return Environment.this.curStateIsTerminal();
		}
		
		
	}
	
}
