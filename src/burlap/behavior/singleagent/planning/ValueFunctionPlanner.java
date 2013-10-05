package burlap.behavior.singleagent.planning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


public abstract class ValueFunctionPlanner extends OOMDPPlanner implements QComputablePlanner{

	
	protected Map <StateHashTuple, List<ActionTransitions>>			transitionDynamics;
	protected Map <StateHashTuple, Double>							valueFunction;
	

	
	public abstract void planFromState(State initialState);
	
	
	
	
	public void VFPInit(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory){
		
		this.PlannerInit(domain, rf, tf, gamma, hashingFactory);
		
		this.transitionDynamics = new HashMap<StateHashTuple, List<ActionTransitions>>();
		this.valueFunction = new HashMap<StateHashTuple, Double>();
		
		
		
	}
	
	
	public double value(State s){
		StateHashTuple sh = this.hashingFactory.hashState(s);
		return valueFunction.get(sh);
	}
	
	public List <QValue> getQs(State s){
		
		StateHashTuple sh = this.stateHash(s);
		Map<String,String> matching = null;
		StateHashTuple indexSH = mapToStateIndex.get(sh);
		
		if(indexSH == null){
			//then this is an unexplored state
			indexSH = sh;
			mapToStateIndex.put(indexSH, indexSH);
		}
		
		
		if(this.containsParameterizedActions){
			matching = sh.s.getObjectMatchingTo(indexSH.s, false);
		}
		
		
		List <QValue> res = new ArrayList<QValue>();
		for(Action a : actions){
			List <GroundedAction> applications = s.getAllGroundedActionsFor(a);
			for(GroundedAction ga : applications){
				res.add(this.getQ(sh, ga, matching));
			}
		}
		
		return res;
		
	}
	
	
	public QValue getQ(State s, GroundedAction a){
		StateHashTuple sh = this.stateHash(s);
		Map<String,String> matching = null;
		StateHashTuple indexSH = mapToStateIndex.get(sh);
		
		if(indexSH == null){
			//then this is an unexplored state
			indexSH = sh;
			mapToStateIndex.put(indexSH, indexSH);
		}
		
		if(this.containsParameterizedActions){
			matching = sh.s.getObjectMatchingTo(indexSH.s, false);
		}
		return this.getQ(sh, a, matching);
	}
	
	
	protected QValue getQ(StateHashTuple sh, GroundedAction a, Map <String, String> matching){
		
		//translate grounded action if necessary
		GroundedAction ta = a;
		if(matching != null){
			ta = this.translateAction(ta, matching);
		}
		
		//find ActionTransition for the designated GA
		List <ActionTransitions> allTransitions = this.getActionsTransitions(sh);
		ActionTransitions matchingAt = null;
		for(ActionTransitions at : allTransitions){
			if(at.matchingTransitions(ta)){
				matchingAt = at;
				break;
			}
		}
		
		double q = this.computeQ(sh.s, matchingAt);
		
		return new QValue(sh.s, a, q);
	}
	
	
	protected List <ActionTransitions> getActionsTransitions(StateHashTuple sh){
		List <ActionTransitions> allTransitions = transitionDynamics.get(sh);
		
		if(allTransitions == null){
			//need to create them
			//first get all grounded actions for this state
			List <GroundedAction> gas = new ArrayList<GroundedAction>();
			for(Action a : actions){
				gas.addAll(sh.s.getAllGroundedActionsFor(a));
			}
			
			//now add transitions
			allTransitions = new ArrayList<ActionTransitions>(gas.size());
			for(GroundedAction ga : gas){
				ActionTransitions at = new ActionTransitions(sh.s, ga, hashingFactory);
				allTransitions.add(at);
			}
			
			//set it
			transitionDynamics.put(sh, allTransitions);
			
		}
		
		return allTransitions;
	}
	
	
	
	protected double computeQ(State s, ActionTransitions trans){
		
		double q = this.getDefaultValue(s);
		
		if(trans.ga.action instanceof Option){
			
			Option o = (Option)trans.ga.action;
			double expectedR = o.getExpectedRewards(s, trans.ga.params);
			q += expectedR;
			
			for(HashedTransitionProbability tp : trans.transitions){
				
				double vp = this.getComputedVForSH(tp.sh);
				
				//note that for options, tp.p will be the *discounted* probability of transition to s',
				//so there is not need for a discount factor to be included
				q += tp.p * vp; 
				
			}
			
		}
		else{
			
			for(HashedTransitionProbability tp : trans.transitions){
				
				double vp = this.getComputedVForSH(tp.sh);
				
				double discount = this.gamma;
				double r = rf.reward(s, trans.ga, tp.sh.s);
				q += tp.p * (r + (discount * vp));
				
			}
			
		}
		
		
		return q;
	}
	
	protected double getDefaultValue(State s){
		return 0.;
	}
	
	protected double getComputedVForSH(StateHashTuple sh){
		Double res = valueFunction.get(sh);
		if(res == null){
			return this.getDefaultValue(sh.s);
		}
		return res;
	}
	
	
	protected void initializeOptionsForExpectationComputations(){
		for(Action a : this.actions){
			if(a instanceof Option){
				((Option)a).setExpectationHashingFactory(hashingFactory);
			}
		}
	}
	
	
}
