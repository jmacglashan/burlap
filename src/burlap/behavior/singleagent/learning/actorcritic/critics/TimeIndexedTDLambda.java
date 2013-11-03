package burlap.behavior.singleagent.learning.actorcritic.critics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.ValueFunctionInitialization;
import burlap.behavior.singleagent.learning.actorcritic.CritiqueResult;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


public class TimeIndexedTDLambda extends TDLambda {

	protected List<Map<StateHashTuple, VValue>>			vTIndex;
	protected int										curTime;
	protected int										maxEpisodeSize = Integer.MAX_VALUE;
	
	public TimeIndexedTDLambda(RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, double learningRate, double vinit, double lambda) {
		super(rf, tf, gamma, hashingFactory, learningRate, vinit, lambda);
		
		this.vTIndex = new ArrayList<Map<StateHashTuple,VValue>>();
		
	}
	
	public TimeIndexedTDLambda(RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, double learningRate, double vinit, double lambda, int maxEpisodeSize) {
		super(rf, tf, gamma, hashingFactory, learningRate, vinit, lambda);
		
		this.maxEpisodeSize = maxEpisodeSize;
		this.vTIndex = new ArrayList<Map<StateHashTuple,VValue>>();
		
	}
	
	public TimeIndexedTDLambda(RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, double learningRate, ValueFunctionInitialization vinit, double lambda, int maxEpisodeSize) {
		super(rf, tf, gamma, hashingFactory, learningRate, vinit, lambda);
		
		this.maxEpisodeSize = maxEpisodeSize;
		this.vTIndex = new ArrayList<Map<StateHashTuple,VValue>>();
		
	}

	
	public int getCurTime(){
		return curTime;
	}
	
	public void setCurTime(int t){
		this.curTime = t;
	}
	
	@Override
	public void initializeEpisode(State s) {
		super.initializeEpisode(s);
		this.curTime = 0;
	}

	@Override
	public void endEpisode() {
		super.endEpisode();
	}
	
	
	@Override
	public CritiqueResult critiqueAndUpdate(State s, GroundedAction ga, State sprime) {
		
		StateHashTuple sh = hashingFactory.hashState(s);
		StateHashTuple shprime = hashingFactory.hashState(sprime);
		
		double r = this.rf.reward(s, ga, sprime);
		double discount = gamma;
		int n = 1;
		if(ga.action instanceof Option){
			Option o = (Option)ga.action;
			discount = Math.pow(gamma, o.getLastNumSteps());
			n = o.getLastNumSteps();
		}
		
		VValue vs = this.getV(sh, curTime);
		double nextV = 0.;
		if(!this.tf.isTerminal(sprime) && this.curTime < this.maxEpisodeSize-1){
			nextV = this.getV(shprime, curTime+n).v;
		}
		
		double delta = r + discount*nextV - vs.v;
		
		//update all traces
		for(StateEligibilityTrace t : traces){
			t.v.v = t.v.v + this.learningRate * delta * t.eligibility;
			t.eligibility = t.eligibility * lambda * discount;
		}
		
		//always need to add the current state since it's a different time stamp for each state
		vs.v = vs.v + this.learningRate * delta;
		StateEligibilityTrace t = new StateTimeElibilityTrace(sh, curTime, discount*this.lambda, vs);
		traces.add(t);

		//update time stamp for next visit
		curTime += n;
		
		
		CritiqueResult critique = new CritiqueResult(s, ga, sprime, delta);
		
		return critique;
	}
	
	
	protected VValue getV(StateHashTuple sh, int t){
		
		while(vTIndex.size() <= t){
			vTIndex.add(new HashMap<StateHashTuple, TDLambda.VValue>());
		}
		
		Map <StateHashTuple, VValue> timeMap = vTIndex.get(t);
		
		VValue v = timeMap.get(sh);
		if(v == null){
			v = new VValue(this.vInitFunction.value(sh.s));
			timeMap.put(sh, v);
		}
		return v;
	}
	
	
	
	public static class StateTimeElibilityTrace extends StateEligibilityTrace{

		public int timeIndex;
		
		public StateTimeElibilityTrace(StateHashTuple sh, int time, double eligibility, VValue v) {
			super(sh, eligibility, v);
			this.timeIndex = time;
		}
		
		
		
		
		
	}
	
	
}
