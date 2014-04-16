package burlap.domain.singleagent.minecraft;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.Policy;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.Action;

public class ForwardPolicy extends Policy {

	private Action a;

	public ForwardPolicy(Action a) {
		this.a = a;
	}
	
	@Override
	public GroundedAction getAction(State s) {
		List<GroundedAction> gas = new ArrayList<GroundedAction>();

		List <List <String>> bindings = s.getPossibleBindingsGivenParamOrderGroups(a.getParameterClasses(), a.getParameterOrderGroups());
		for(List <String> params : bindings){
			String [] aprams = params.toArray(new String[params.size()]);
				GroundedAction gp = new GroundedAction(a, aprams);
				gas.add(gp);
		}
		
		if (gas.size() > 1) {
			System.out.println("More than 1 binding for ForwardPolicy!");
		}

		return gas.get(0);
	}

	@Override
	public GroundedAction getAffordanceAction(State s, ArrayList<Affordance> kb) {
		// TODO Auto-generated method stub
		return this.getAction(s);
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("No action distribution for this class!");
	}

	@Override
	public boolean isStochastic() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDefinedFor(State s) {
		// TODO Auto-generated method stub
		return true;
	}

}
