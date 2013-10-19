package burlap.oomdp.stocashticgames;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.State;


public abstract class SingleAction {

	public String actionName;
	public String [] parameterTypes;
	public String [] parameterRenames;
	
	public SGDomain domain;
	
	
	public abstract boolean isApplicableInState(State s, String actingAgent, String [] params);
	
	public SingleAction(SGDomain d, String name){
		this.init(d, name, new String[0], new String[0]);
	}
	
	public SingleAction(SGDomain d, String name, String [] types){
		String [] pr = new String[types.length];
		for(int i = 0; i < pr.length; i++){
			pr[i] = name + ".P" + i;
		}
		this.init(d, name, types, pr);
	}
	
	public SingleAction(SGDomain d, String name, String [] types, String [] renames){
		this.init(d, name, types, renames);
	}
	
	
	private void init(SGDomain d, String name, String [] pt, String [] pr){
		this.actionName = name;
		this.parameterTypes = pt;
		this.parameterRenames = pr;
		d.addSingleAction(this);
	}
	
	public boolean isPamaeterized(){
		return this.parameterTypes.length > 0;
	}
	
	
	
	public static List <GroundedSingleAction> getAllPossibleGroundedSingleActions(State s, String actingAgent, List <SingleAction> actions){
		List <GroundedSingleAction> res = new ArrayList<GroundedSingleAction>();
		for(SingleAction sa : actions){
			res.addAll(sa.getAllGroundedActionsFor(s, actingAgent));
		}
		return res;
	}
	
	public List<GroundedSingleAction> getAllGroundedActionsFor(State s, String actingAgent){
		
		List <GroundedSingleAction> res = new ArrayList<GroundedSingleAction>();
		
		if(this.parameterTypes.length == 0){
			if(this.isApplicableInState(s, actingAgent, new String[]{})){
				res.add(new GroundedSingleAction(actingAgent, this, new String[]{}));
			}
			return res; //no parameters so just the single ga without params
		}
		
		List <List <String>> bindings = s.getPossibleBindingsGivenParamOrderGroups(this.parameterTypes, this.parameterRenames);
		
		for(List <String> params : bindings){
			String [] aparams = params.toArray(new String[params.size()]);
			if(this.isApplicableInState(s, actingAgent, aparams)){
				res.add(new GroundedSingleAction(actingAgent, this, aparams));
			}
		}
		
		
		return res;
		
	}
	
	
	@Override
	public int hashCode(){
		return actionName.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof SingleAction)){
			return false;
		}
		
		return ((SingleAction)o).actionName.equals(actionName);
	}

}
