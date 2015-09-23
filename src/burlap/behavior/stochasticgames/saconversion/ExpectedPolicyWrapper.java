package burlap.behavior.stochasticgames.saconversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.Policy.ActionProb;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;

/**
 * 
 * @author Betsy Hilliard betsy@cs.brown.edu
 *
 */
public class ExpectedPolicyWrapper extends Policy {
	
	protected Map<Integer,Policy> otherAgentPolicies;
	protected Map<Integer,Double> distributionOverOtherAgentPolicies;
	

	public ExpectedPolicyWrapper(Map<Integer,Policy> otherAgentPolicies, 
			Map<Integer,Double> distributionOverOtherAgentPolicies) {
		this.otherAgentPolicies = otherAgentPolicies;
		this.distributionOverOtherAgentPolicies = distributionOverOtherAgentPolicies;
		
	}

	@Override
	public AbstractGroundedAction getAction(State s) {
		List<ActionProb> expectedPolicy = getActionDistributionForState(s);
		for (ActionProb ap : expectedPolicy){
			System.out.print( ap.ga +","+ap.pSelection+ " ; ");
		}
		System.out.println();
		Random rand = new Random();
		double draw = rand.nextDouble();
		double total = 0.0;
		int i = -1;
		while(total<=draw){
			i++;
			total+=expectedPolicy.get(i).pSelection;
		}
		return expectedPolicy.get(i).ga;
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		List<ActionProb> finalActionProbs = new ArrayList<ActionProb>();
		double total = 0.0;
		for(Integer klevel : otherAgentPolicies.keySet()){
			List<ActionProb> policy = otherAgentPolicies.get(klevel).getActionDistributionForState(s);
			
			for(ActionProb ap : policy){
				
				//System.out.println(ap.pSelection);
				//System.out.println(distributionOverOtherAgentPolicies);
				//System.out.println(klevel);
				double p = ap.pSelection*distributionOverOtherAgentPolicies.get(klevel);
				total = total+p;
				int location = findGroundedActionProb(finalActionProbs, ap.ga);
				if(location>=0){
					finalActionProbs.get(location).pSelection = finalActionProbs.get(location).pSelection+p;
				}else{
					ActionProb newAP = new ActionProb(ap.ga.copy(),p);
					finalActionProbs.add(newAP);
				}
			}
			
		}
		
		for(ActionProb ap : finalActionProbs){
			ap.pSelection=ap.pSelection/total;
		}
		
		return finalActionProbs;
	}

	private int findGroundedActionProb(List<ActionProb> finalActionProbs, AbstractGroundedAction aga) {
		
		for(int i=0;i<finalActionProbs.size();i++){
			if(finalActionProbs.get(i).ga.equals(aga)){
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean isStochastic() {
		if(distributionOverOtherAgentPolicies.size()>1){
			return true;
		}
		return true;
	}

	@Override
	public boolean isDefinedFor(State s) {
		for(Integer level : distributionOverOtherAgentPolicies.keySet()){
			if(distributionOverOtherAgentPolicies.get(level)>0 &&
					otherAgentPolicies.get(level).isDefinedFor(s)){
				return true;
			}
		}
		return false;
	}

}
