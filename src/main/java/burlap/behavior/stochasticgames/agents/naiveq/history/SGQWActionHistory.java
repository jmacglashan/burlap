package burlap.behavior.stochasticgames.agents.naiveq.history;

import burlap.behavior.stochasticgames.agents.naiveq.SGNaiveQLAgent;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableStateFactory;
import burlap.mdp.stochasticgames.action.JointAction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.action.SGAgentAction;

import java.util.Map;


/**
 * A Tabular Q-learning [1] algorithm for stochastic games formalisms that augments states with the actions each agent took in n
 * previous time steps.
 * 
 * <p>
 * 1. Watkins, Christopher JCH, and Peter Dayan. "Q-learning." Machine learning 8.3-4 (1992): 279-292. <p>
 * @author James MacGlashan
 *
 */
public class SGQWActionHistory extends SGNaiveQLAgent {

	/**
	 * The size of action history to store.
	 */
	protected int								historySize;


	protected HistoryState						curHState;
	
	
	/**
	 * Initializes the learning algorithm using 0.1 epsilon greedy learning strategy/policy
	 * @param d the domain in which the agent will act
	 * @param discount the discount factor
	 * @param learningRate the learning rate
	 * @param hashFactory the state hashing factory to use
	 * @param historySize the number of previous steps to remember and with which to augment the state space
	 */
	public SGQWActionHistory(SGDomain d, double discount, double learningRate, HashableStateFactory hashFactory, int historySize) {
		super(d, discount, learningRate, hashFactory);
		this.historySize = historySize;

	}

	@Override
	public void gameStarting() {
		curHState = null;
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction, Map<String, Double> jointReward, State sprime, boolean isTerminal) {
		
		SGAgentAction myAction = jointAction.action(worldAgentName);
		QValue qe = this.storedQ(curHState, myAction);


		
		State augSP = curHState.incrementWithChange(sprime, jointAction);
		
		
		if(internalRewardFunction != null){
			jointReward = internalRewardFunction.reward(s, jointAction, sprime);
		}
		
		
		double r = jointReward.get(worldAgentName);
		double maxQ = 0.;
		if(!isTerminal){
			maxQ = this.getMaxQValue(augSP);
		}

		qe.q = qe.q + this.learningRate.pollLearningRate(this.totalNumberOfSteps, s, myAction) * (r + (this.discount * maxQ) - qe.q);
		
		this.totalNumberOfSteps++;

		
	}

	@Override
	public SGAgentAction getAction(State s) {
		if(this.curHState == null){
			this.curHState = new HistoryState(s, this.historySize);
		}
		return super.getAction(curHState);
	}


}
