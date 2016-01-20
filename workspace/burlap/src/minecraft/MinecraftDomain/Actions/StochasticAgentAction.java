package minecraft.MinecraftDomain.Actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.Domain;

public abstract class StochasticAgentAction extends AgentAction {
	
	/**
	 * Random object for sampling distribution
	 */
	protected Random rand;
	
	/**
	 * hashmap from actions to probability of that action -- used to account for indeterminism
	 */
	protected HashMap<StochasticAgentAction, Double> actionToProb;
	
	/**
	 * 
	 * @param name
	 * @param domain
	 * @param rows
	 * @param cols
	 * @param height
	 * @param causesAgentToFall
	 */
	public StochasticAgentAction(String name, Domain domain, int rows, int cols, int height, boolean causesAgentToFall){
		super(name, domain, rows, cols, height, causesAgentToFall);
		this.rand = RandomFactory.getMapped(0);
		this.actionToProb = new HashMap<StochasticAgentAction, Double>();
	}
	
	/**
	 * 
	 * @param possibleAction an action that might as a result of indeterminism in the space
	 * @param weight the relative weight (representing a likelihood) of that action
	 */
	private void addPossibleResultingAction(StochasticAgentAction possibleAction, Double weight) {
		this.actionToProb.put(possibleAction, weight);
	}
	
	/**
	 * turns all the weights in this.actionToProb into a probability distribution
	 */
	private void normalizeWeights () {
		Set<StochasticAgentAction> keys =  this.actionToProb.keySet();
		double totalProb = 0.0;
		//Get total weight
		for (StochasticAgentAction key: keys) {
			totalProb += this.actionToProb.get(key);
		}
	
		//normalize weights
		for (StochasticAgentAction key: keys) {
			double oldValue = this.actionToProb.get(key);
			this.actionToProb.put(key, oldValue/totalProb);
		}

	}
	
	/**
	 * Used to cause an action to probabilistically cause other actions
	 * @param actions an array of all the actions that might occur from this action as a result of indeterminism
	 * @param weights an array of doubles of the respective likelihoods of the actions in actions
	 */
	public void addResultingActionsWithWeights(List<StochasticAgentAction> actions, double [] weights) {
		assert(actions.size() == weights.length);
		for (int i = 0; i < actions.size(); i++) {
			addPossibleResultingAction(actions.get(i), weights[i]);
		}
		normalizeWeights();
	}
	
	
	/**
	 * 
	 * @return Get an randomly action as determined by input weights
	 */
	@Override
	protected StochasticAgentAction getAction() {
		ArrayList<StochasticAgentAction> keys = new ArrayList<StochasticAgentAction>();

		for(StochasticAgentAction key: this.actionToProb.keySet()) {
			keys.add(key);
		}
		
		if (keys.isEmpty()) {
			System.out.println("Action: " + this.getName() + " has no resulting actions.");
			//assert(false);
		}
		
		//Sample actions until one is deemed probabilistic enough
		StochasticAgentAction currActionCandidate = keys.get(rand.nextInt(keys.toArray().length));
		double randProb = rand.nextDouble() ;
		while (actionToProb.get(currActionCandidate) < randProb) {
			currActionCandidate = keys.get(rand.nextInt(keys.toArray().length));
			randProb = rand.nextDouble();
		}
		//currActionCandidate is now the action to perform
		return currActionCandidate;
	}
	
}