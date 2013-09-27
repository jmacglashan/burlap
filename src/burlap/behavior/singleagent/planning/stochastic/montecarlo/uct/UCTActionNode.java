package burlap.behavior.singleagent.planning.stochastic.montecarlo.uct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.singleagent.GroundedAction;


public class UCTActionNode {

	public GroundedAction								action;
	public double										sumReturn;
	public int											n;
	public Map<StateHashTuple, List<UCTStateNode>>		successorStates;
	
	
	public UCTActionNode(GroundedAction a){
		action = a;
		sumReturn = 0.;
		n = 0;
		successorStates = new HashMap<StateHashTuple, List<UCTStateNode>>();
	}
	
	public double averageReturn(){
		return sumReturn / n;
	}
	
	public void update(double sampledReturn){
		sumReturn += sampledReturn;
		n++;
	}
	
	public void addSuccessor(UCTStateNode node){
		
		List <UCTStateNode> succesorsMatchingState = successorStates.get(node.state);
		if(succesorsMatchingState == null){
			succesorsMatchingState = new ArrayList<UCTStateNode>();
			successorStates.put(node.state, succesorsMatchingState);
		}
		
		if(!succesorsMatchingState.contains(node)){
			succesorsMatchingState.add(node);
		}
		
	}
	
	public boolean referencesSuccessor(UCTStateNode node){
		
		List <UCTStateNode> succesorsMatchingState = successorStates.get(node.state);
		if(succesorsMatchingState == null){
			return false;
		}
		
		else if(succesorsMatchingState.contains(node)){
			return true;
		}
		
		return false;
		
	}
	
	
	public List <UCTStateNode> getAllSuccessors(){
		List <UCTStateNode> res = new ArrayList<UCTStateNode>();
		for(List <UCTStateNode> nodes : successorStates.values()){
			for(UCTStateNode node : nodes){
				res.add(node);
			}
		}
		
		return res;
	}
	
	
	
	
	
	
	
	
	public static class UCTActionConstructor{
		
		
		public UCTActionNode generate(GroundedAction a){
			return new UCTActionNode(a);
		}
		
	}
	
}
