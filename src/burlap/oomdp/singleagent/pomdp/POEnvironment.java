package burlap.oomdp.singleagent.pomdp;

import burlap.oomdp.core.State;

import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;

/**
 * The POEnvironment class allows a POMDP solver to execute actions in an environment and return observations,
 * while keeping the true state of the world hidden.
 * 
 * @author ngopalan
 *
 */
public class POEnvironment extends Environment {

	protected PODomain				domain;
	protected RewardFunction		rf;
	protected TerminalFunction		tf;
	protected State					mdpState;
	
	protected double 				lastR;
	
	
	public POEnvironment(PODomain domain, RewardFunction rf, TerminalFunction tf){
		this.domain = domain;
		this.rf = rf;
		this.tf = tf;
	}
	
	
	public PODomain getPODomain(){
		return this.domain;
	}
	
	@Override
	public State executeAction(String aname, String[] params) {
		
		Action a = domain.getAction(aname);
		State sp = a.performAction(this.mdpState, params);
		GroundedAction ga = new GroundedAction(a, params);
		this.curState = this.domain.getObservationFunction().sampleObservation(sp, ga);
		this.lastR = this.rf.reward(this.mdpState, ga, sp);
		
		this.mdpState = sp;
		
		return this.curState;
	}
	
	@Override
	public void setCurStateTo(State s){
		throw new RuntimeException("Cannot set current state in POEnvironment. For saftey with standard MDP algorithms, current state in" +
				" a POEnvironment refers to the current observation. If you want to set the MDP state of the PO environment, use the" +
				"setCurMDPStateTo(State) method. If you want to set the observation, use the setCurObservation(State) method");
	}
	
	public void setCurObservationTo(State observation){
		this.curState = observation;
	}

	public void setCurMPDStateTo(State s){
		this.mdpState = s;
		this.lastR = 0.;
	}
	
	public State getCurMDPState(){
		return this.mdpState;
	}
	
	@Override
	public double getLastReward() {
		return this.lastR;
	}

	@Override
	public boolean curStateIsTerminal() {
		return this.tf.isTerminal(this.mdpState);
	}

}
