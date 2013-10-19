package burlap.behavior.singleagent.planning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.OptionEvaluatingRF;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


public abstract class OOMDPPlanner {

	protected Domain												domain;
	protected StateHashFactory										hashingFactory;
	protected RewardFunction										rf;
	protected TerminalFunction										tf;
	protected double												gamma;
	protected List <Action>											actions;
	
	protected Map <StateHashTuple, StateHashTuple>					mapToStateIndex; //this is useful because two states may be equal but have different object name references and this mapping lets the user pull out which exact state (and object names) was used for the action dynamics
	
	
	protected boolean												containsParameterizedActions;
	
	protected int													debugCode;
	
	
	public abstract void planFromState(State initialState);
	
	public void PlannerInit(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory){
		
		this.domain = domain;
		this.rf = rf;
		this.tf = tf;
		this.gamma = gamma;
		this.hashingFactory = hashingFactory;
		
		mapToStateIndex = new HashMap<StateHashTuple, StateHashTuple>();
		
		containsParameterizedActions = false;
		List <Action> actions = domain.getActions();
		this.actions = new ArrayList<Action>(actions.size());
		for(Action a : actions){
			this.actions.add(a);
			if(a.getParameterClasses().length > 0){
				containsParameterizedActions = true;
			}
		}
		
	}
	
	public void addNonDomainReferencedAction(Action a){
		//make sure it doesn't already exist in the list
		if(!actions.contains(a)){
			actions.add(a);
			if(a instanceof Option){
				Option o = (Option)a;
				o.keepTrackOfRewardWith(rf, gamma);
				o.setExernalTermination(tf);
				if(!(this.rf instanceof OptionEvaluatingRF)){
					this.rf = new OptionEvaluatingRF(this.rf);
				}
			}
			if(a.getParameterClasses().length > 0){
				this.containsParameterizedActions = true;
			}
		}
		
	}
	
	public void setActions(List<Action> actions){
		this.actions = actions;
	}
	
	public TerminalFunction getTF(){
		return tf;
	}
	
	public RewardFunction getRF(){
		return rf;
	}
	
	public StateHashFactory getHashingFactory(){
		return this.hashingFactory;
	}
	
	public void setDebugCode(int code){
		this.debugCode = code;
	}
	
	public int getDebugCode(){
		return debugCode;
	}
	
	public void toggleDebugPrinting(boolean toggle){
		DPrint.toggleCode(debugCode, toggle);
	}
	
	protected GroundedAction translateAction(GroundedAction a, Map <String,String> matching){
		String [] newParams = new String[a.params.length];
		for(int i = 0; i < a.params.length; i++){
			newParams[i] = matching.get(a.params[i]);
		}
		return new GroundedAction(a.action, newParams);
	}
	
	
	public StateHashTuple stateHash(State s){
		return hashingFactory.hashState(s);
	}
	
	
	protected List <GroundedAction> getAllGroundedActions(State s){
		
		return s.getAllGroundedActionsFor(this.actions);
		
	}
	
}
