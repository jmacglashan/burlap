package burlap.behavior.stochasticgames.saconversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.Policy.ActionProb;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;

/**
 * 
 * @author Betsy Hilliard betsy@cs.brown.edu
 *
 */
public class RandomSingleAgentPolicy extends Policy {
	
	
	List<SGAgentAction> actions;
	String agentName; 
	
	public RandomSingleAgentPolicy(String agentName, List<SGAgentAction> actions){
		this.actions = actions;
		this.agentName = agentName;
	}

	@Override
	public AbstractGroundedAction getAction(State s) {
		Random rand = new Random();
		List<GroundedSGAgentAction> gsas = actions.get(rand.nextInt(actions.size())).getAllApplicableGroundedActions(s, agentName);
		//choose from groundings again
		GroundedSGAgentAction gsa = gsas.get(rand.nextInt(gsas.size()));
		return gsa;
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		List<ActionProb> actionProbs = new ArrayList<ActionProb>();
		for(SGAgentAction sa : actions){
			//weight by 1/m for num GSA
			List<GroundedSGAgentAction> gsas = sa.getAllApplicableGroundedActions(s, agentName);
			for(GroundedSGAgentAction gsa : gsas){
				actionProbs.add(new ActionProb(gsa,(1.0/actions.size())*(1.0/gsas.size())));
			}
			
			
		}
		return actionProbs;
	}

	@Override
	public boolean isStochastic() {
		return true;
	}

	@Override
	public boolean isDefinedFor(State s) {
		return true;
	}

}
