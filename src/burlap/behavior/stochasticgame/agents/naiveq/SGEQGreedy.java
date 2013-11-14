package burlap.behavior.stochasticgame.agents.naiveq;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.behavior.stochasticgame.Strategy;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.AgentType;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.SingleAction;


/**
 * An epsilon greedy Q-value strategy that the agent can follow.
 * @author James MacGlashan
 *
 */
public class SGEQGreedy extends Strategy {

	
	/**
	 * The Q-learning agent for which this strategy is defined.
	 */
	SGQLAgent		agent;
	
	/**
	 * A random object for selecting actions
	 */
	Random			rand;
	
	/**
	 * Epsilon; the probability that a random action will be taken instead of the greedy action
	 */
	double			e;
	
	
	/**
	 * Initializes for a given Q-learning agent an epsilon value
	 * @param a The Q-learning agent for which this strategy is defined.
	 * @param e The probability that a random action will be taken instead of the greedy action
	 */
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
	
	
	
	/**
	 * Returns a list of the index of actions with the maximum Q-value. Typically this list will be of size 1, but 
	 * if there are ties for the max action (which is typically in an unvisited state) it will return all of the tied actions.
	 * @param s the state in which to get Q-values
	 * @param srcGSAs the actions the agent can take and which is used to define the index of actions
	 * @return a list of the index actions with the maximum Q-value.
	 */
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
