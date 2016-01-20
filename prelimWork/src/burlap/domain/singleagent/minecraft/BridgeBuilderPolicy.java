package burlap.domain.singleagent.minecraft;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.Policy;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.Action;

public class BridgeBuilderPolicy extends Policy {

	private Action actionA;
	private Action actionB;
	private PropositionalFunction pfA;
	private PropositionalFunction pfB;

	public BridgeBuilderPolicy(Action a, Action b, PropositionalFunction pfA, PropositionalFunction pfB) {
		this.actionA = a;
		this.actionB = b;
		this.pfA = pfA;
		this.pfB = pfB;
	}
	
	@Override
	public GroundedAction getAction(State s) {
		if (this.pfA.isTrue(s)) {
			return groundAction(s, this.actionA);
		}
		else if (this.pfB.isTrue(s)) {
			return groundAction(s, this.actionB);
		}
		
		return null;
	}
	
	private GroundedAction groundAction(State s, Action act) {
		List<GroundedAction> gas = new ArrayList<GroundedAction>();

		List <List <String>> bindings = s.getPossibleBindingsGivenParamOrderGroups(act.getParameterClasses(), act.getParameterOrderGroups());
		for(List <String> params : bindings){
			String [] aprams = params.toArray(new String[params.size()]);
				GroundedAction gp = new GroundedAction(act, aprams);
				gas.add(gp);
		}
		
		if (gas.size() > 1) {
			System.out.println("More than 1 binding for BBPolicy!");
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
		List<ActionProb> actionProbs = new ArrayList<ActionProb>();
		actionProbs.add(new ActionProb(this.getAction(s), 1.0));
		
		return actionProbs;
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
