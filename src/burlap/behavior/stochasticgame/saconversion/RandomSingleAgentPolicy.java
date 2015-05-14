package burlap.behavior.stochasticgame.saconversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.behavior.singleagent.Policy;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.SingleAction;

/**
 * 
 * @author Betsy Hilliard betsy@cs.brown.edu
 *
 */
public class RandomSingleAgentPolicy extends Policy {
	
	
	List<SingleAction> actions;
	String agentName; 
	
	public RandomSingleAgentPolicy(String agentName, List<SingleAction> actions){
		this.actions = actions;
		this.agentName = agentName;
	}

	@Override
	public AbstractGroundedAction getAction(State s) {
		Random rand = new Random();
		List<GroundedSingleAction> gsas = actions.get(rand.nextInt(actions.size())).getAllGroundedActionsFor(s, agentName);
		//choose from groundings again
		GroundedSingleAction gsa = gsas.get(rand.nextInt(gsas.size()));
		return gsa;
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		List<ActionProb> actionProbs = new ArrayList<ActionProb>();
		for(SingleAction sa : actions){
			//weight by 1/m for num GSA
			List<GroundedSingleAction> gsas = sa.getAllGroundedActionsFor(s, agentName);
			for(GroundedSingleAction gsa : gsas){
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
