package burlap.behavior.stochasticgame.agents.twoplayer;

import java.util.List;
import java.util.Map;
import java.util.Random;

import burlap.behavior.stochasticgame.solvers.BimatrixGeneralSumSolver;
import burlap.behavior.stochasticgame.solvers.BimatrixGeneralSumSolver.Joint;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.JointReward;
import burlap.oomdp.stochasticgames.SingleAction;


/**
 * This agent plays the single stage nash equilibrium for 2 player games. If there are more than two agents in the game a runtime exception will be thrown.
 * The agent only needs a reward function provided to it to compute the nash equilibrium for a provided state. This is achived by querying the joint reward
 * for each agent assuming they would transition to the same state. Alternatively, a joint action model {@link JointActionModel} can also be provided,
 * in which case the execpted reward for each joint action is computed and used for the nash equilibrium input.
 * @author James MacGlashan
 *
 */
public class SingleStageNash extends Agent {

	/**
	 * The joint reward function
	 */
	protected JointReward			jointReward;
	
	/**
	 * The optimal joint action model
	 */
	protected JointActionModel		jam;
	
	
	/**
	 * A random generator for selecting actions according to a probabiltiy distribution
	 */
	protected Random				rand = RandomFactory.getMapped(0);
	
	
	
	/**
	 * Initializes with a joint reward function. This will cause the agent to query the reward function as if all joint actions would transition to the
	 * same state deterministically. If there are stochastic transitions, a joint action model will need to be provided as well.
	 * @param jointReward the joint reward of the problem.
	 */
	public SingleStageNash(JointReward jointReward){
		this.jointReward = jointReward;
	}
	
	/**
	 * Initializes with a joint reward function and action model so that the single stage expected payoffs may be computed.
	 * @param jointReward the joint reward of the problem.
	 * @param jam the action model of the problem.
	 */
	public SingleStageNash(JointReward jointReward, JointActionModel jam){
		this.jointReward = jointReward;
		this.jam = jam;
	}
	
	/**
	 * Sets the joint reward functio of this problem
	 */
	public void setJointReward(JointReward jointReward){
		this.jointReward = jointReward;
	}
	
	
	/**
	 * Sets the joint action model of this problem so that the expected single stage joint payoffs may be computed.
	 * @param jam the joint action model of the problem
	 */
	public void setJointActionModel(JointActionModel jam){
		this.jam = jam;
	}
	
	@Override
	public void gameStarting() {
		//do nothing
	}

	@Override
	public GroundedSingleAction getAction(State s) {
		
		String otherAgentName = this.otherAgentName();
		
		List<GroundedSingleAction> myActions = SingleAction.getAllPossibleGroundedSingleActions(s, this.worldAgentName, this.agentType.actions);
		List<GroundedSingleAction> opponentActions = SingleAction.getAllPossibleGroundedSingleActions(s, otherAgentName, this.world.getAgentDefinitions().get(otherAgentName).actions);
		
		BiMatrix b = this.getPayoffMatrix(s, myActions, opponentActions);
		Joint<double[]> strategies = BimatrixGeneralSumSolver.solveForMixedStrategies(b.payoff1, b.payoff2);
		double[] player1Strategy = strategies.getForPlayer(0);
		
		double r = rand.nextDouble();
		double sumProb = 0.;
		for(int i = 0; i < player1Strategy.length; i++){
			sumProb += player1Strategy[i];
			if(r < sumProb){
				return myActions.get(i);
			}
		}
		
		throw new RuntimeException("Action strategy distribution did not sum to 1.");
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction,
			Map<String, Double> jointReward, State sprime, boolean isTerminal) {
		//do nothing
	}

	@Override
	public void gameTerminated() {
		//do nothing
	}
	
	
	/**
	 * Returns the single stage payoff matrix. If no joint aciton model is provided for this object, then the reward function is queried as if the
	 * agent determinitically transitions to the same state and the immediate rewards fill the bimatrix. If the joint action model is specified,
	 * then the expected immediate rewards is used.
	 * @param s the state in which the agents will act
	 * @param myActions this agent's action set
	 * @param opponentActions the opponent's action set
	 * @return a single stage payoff matrix
	 */
	protected BiMatrix getPayoffMatrix(State s, List<GroundedSingleAction> myActions, List<GroundedSingleAction> opponentActions){
		if(this.jam == null){
			return this.getImmediateRewardPayoffMatrix(s, myActions, opponentActions);
		}
		return this.getExpectedRewardPayoffMatrix(s, myActions, opponentActions);
	}
	
	
	/**
	 * Returns the immedaite rewards received in a bimatrix by querying the joint reward function as if each possible joint aciton
	 * would deterministically transition to the state input state.
	 * @param s the input state in which the agents will act
	 * @param myActions this agent's action set
	 * @param opponentActions the opponent's action set
	 * @return a single stage pay off matrix
	 */
	protected BiMatrix getImmediateRewardPayoffMatrix(State s, List<GroundedSingleAction> myActions, List<GroundedSingleAction> opponentActions){
		int n = myActions.size();
		int m = opponentActions.size();
		BiMatrix b = new BiMatrix(n, m);
		
		for(int i = 0; i < n; i++){
			GroundedSingleAction a1 = myActions.get(i);
			for(int j = 0; j < m; j++){
				GroundedSingleAction a2 = opponentActions.get(j);
				JointAction ja = new JointAction();
				ja.addAction(a1);
				ja.addAction(a2);
				Map<String, Double> r = this.jointReward.reward(s, ja, s);
				b.payoff1[i][j] = r.get(a1.actingAgent);
				b.payoff2[i][j] = r.get(a2.actingAgent);
			}
		}
		
		return b;
		
		
	}

	
	/**
	 * Using this object's provided joint action model, returns the single stage expected payoff bimatrix
	 * @param s the input state in which the agents will act
	 * @param myActions this agent's action set
	 * @param opponentActions the opponent's action set
	 * @return a single stage pay off matrix
	 */
	protected BiMatrix getExpectedRewardPayoffMatrix(State s, List<GroundedSingleAction> myActions, List<GroundedSingleAction> opponentActions){
		
		int n = myActions.size();
		int m = opponentActions.size();
		BiMatrix b = new BiMatrix(n, m);
		
		for(int i = 0; i < n; i++){
			GroundedSingleAction a1 = myActions.get(i);
			for(int j = 0; j < m; j++){
				GroundedSingleAction a2 = opponentActions.get(j);
				JointAction ja = new JointAction();
				ja.addAction(a1);
				ja.addAction(a2);
				
				List<TransitionProbability> aps = this.jam.transitionProbsFor(s, ja);
				double er1 = 0.;
				double er2 = 0.;
				for(TransitionProbability tp : aps){
					Map<String, Double> r = this.jointReward.reward(s, ja, tp.s);
					er1 += tp.p * r.get(a1.actingAgent);
					er2 += tp.p * r.get(a2.actingAgent);
				}
				
				
				b.payoff1[i][j] = er1;
				b.payoff2[i][j] = er2;
			}
		}
		
		return b;
		
	}
	
	
	/**
	 * Assuming there are only two players in the world, returns the opponent agent's name. If there are no other agents in the world
	 * a runtime exception will be thrown.
	 * @return the opponent agent's name.
	 */
	protected String otherAgentName(){
		for(Agent a : this.world.getRegisteredAgents()){
			if(!a.getAgentName().equals(this.worldAgentName)){
				return a.getAgentName();
			}
		}
		throw new RuntimeException("No other agents in the world but this one; cannot return opponent's agent name.");
	}
	
	
	/**
	 * A class for storing a Bimatrix payoff.
	 * @author James MacGlashan
	 *
	 */
	protected class BiMatrix{
		public double [][] payoff1;
		public double [][] payoff2;
		
		public BiMatrix(int n, int m){
			this.payoff1 = new double[n][m];
			this.payoff2 = new double[n][m];
		}
	}

}
