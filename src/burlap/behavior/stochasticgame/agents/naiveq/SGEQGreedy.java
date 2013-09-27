package burlap.behavior.stochasticgame.agents.naiveq;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.behavior.stochasticgame.Strategy;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.State;
import burlap.oomdp.stocashticgames.AgentType;
import burlap.oomdp.stocashticgames.GroundedSingleAction;
import burlap.oomdp.stocashticgames.SingleAction;


public class SGEQGreedy extends Strategy {

	SGQLAgent		agent;
	Random			rand;
	double			e;
	
	
	public SGEQGreedy(SGQLAgent a, double e) {
		this.agent = a;
		this.rand = RandomFactory.getMapped(0);
		this.e = e;
	}

	@Override
	public GroundedSingleAction getAction(State s) {
		
		AgentType at = agent.getAgentType();
		String aname = agent.getAgentName();
		
		List<GroundedSingleAction> gas = SingleAction.getAllPossibleGroundedSingleActions(s, aname, at.actions);
		
		double roll = rand.nextDouble();
		if(roll > e){
			//choose randomly among max satisfying actions
			
			List <Integer> maxCands = this.getMaxActions(s, gas);
			
			if(maxCands.size() == 1){
				return gas.get(maxCands.get(0));
			}
			else{
				int ind = maxCands.get(rand.nextInt(maxCands.size()));
				return gas.get(ind);
			}
			
			
		}
		
		
		//otherwise select randomly
		return gas.get(rand.nextInt(gas.size()));
	}

	@Override
	public List<SingleActionProb> getActionDistributionForState(State s) {
		
		AgentType at = agent.getAgentType();
		String aname = agent.getAgentName();
		
		List<GroundedSingleAction> gsas = SingleAction.getAllPossibleGroundedSingleActions(s, aname, at.actions);
		
		double de = this.e / gsas.size();
		
		List <SingleActionProb> probs = new ArrayList<Strategy.SingleActionProb>(gsas.size());
		for(GroundedSingleAction gsa : gsas){
			SingleActionProb ap = new SingleActionProb(gsa, de);
			probs.add(ap);
		}
		
		List <Integer> maxCands = this.getMaxActions(s, gsas);
		double die = (1. - this.e) / maxCands.size();
		for(int i : maxCands){
			probs.get(i).pSelection += die;
		}
		
		return probs;
	}

	@Override
	public boolean isStochastic() {
		return true;
	}
	
	
	
	protected List <Integer> getMaxActions(State s, List<GroundedSingleAction> srcGSAs){
		List <Integer> maxCands = new ArrayList<Integer>(srcGSAs.size());
		List <SGQValue> entries = agent.getAllQsFor(s, srcGSAs);
		double maxQ = Double.NEGATIVE_INFINITY;
		for(int i = 0; i < entries.size(); i++){
			SGQValue qe = entries.get(i);
			if(qe.q > maxQ){
				maxCands.clear();
				maxCands.add(i);
				maxQ = qe.q;
			}
			else if(qe.q == maxQ){
				maxCands.add(i);
			}
		}
		
		return maxCands;
		
	}

}
