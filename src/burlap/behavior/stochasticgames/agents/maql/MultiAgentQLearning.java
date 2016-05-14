package burlap.behavior.stochasticgames.agents.maql;

import java.util.HashMap;
import java.util.Map;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.mdp.statehashing.HashableStateFactory;
import burlap.behavior.stochasticgames.PolicyFromJointPolicy;
import burlap.behavior.stochasticgames.madynamicprogramming.AgentQSourceMap;
import burlap.behavior.stochasticgames.madynamicprogramming.AgentQSourceMap.HashMapAgentQSourceMap;
import burlap.behavior.stochasticgames.madynamicprogramming.AgentQSourceMap.MAQLControlledQSourceMap;
import burlap.behavior.stochasticgames.madynamicprogramming.JAQValue;
import burlap.behavior.stochasticgames.madynamicprogramming.MAQSourcePolicy;
import burlap.behavior.stochasticgames.madynamicprogramming.MultiAgentQSourceProvider;
import burlap.behavior.stochasticgames.madynamicprogramming.QSourceForSingleAgent;
import burlap.behavior.stochasticgames.madynamicprogramming.QSourceForSingleAgent.HashBackedQSource;
import burlap.behavior.stochasticgames.madynamicprogramming.SGBackupOperator;
import burlap.behavior.stochasticgames.madynamicprogramming.policies.EGreedyMaxWellfare;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.SGAgent;
import burlap.mdp.stochasticgames.SGAgentType;
import burlap.mdp.stochasticgames.agentactions.SGAgentAction;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.World;


/**
 * A class for performing multi-agent Q-learning in which different Q-value backup operators can be provided to enable the learning
 * of different solution concepts. Multi-agent Q-learning differs from single agent Q-learning in that Q-values are associated
 * with joint actions, rather than actions, and in that a different Q-value is stored for each agent in the game.
 * <p>
 * In this class, each agent stores its own Q-value and an object that provides a source for the Q-values of other agents. This allows
 * the storage of Q-values to vary so that an agent can store the Q-values forall other agents, or the map can provide access to the
 * Q-values stored by other MultiAgentQLearning agents in the world so that only one copy of each agent's Q-value is ever stored.
 * In the case of the latter, all agents should be implementing the same solution concept learning algorithm. Otherwise, each agent
 * should maintain their own set of Q-values.
 * <p>
 * After an agent observes an outcome, it determines the change in Q-value. However, the agent will not actually update its Q-value
 * to the new value until it is asked for its next action ({@link #getAction(State)}) or until the {@link #gameTerminated()} message is sent.
 * Q-value updates are delayed in this way because if Q-values for each agent are shared and distributed among the agents, this ensures
 * that the Q-values are all updated after the next Q-value has been determined for each agent.
 * <p>
 * In general the learning policy followed by this agent should reflect the needs of the solution concept being learned. For instance,
 * CoCo-Q should use some variant of a maximum welfare joint policy.
 * <p>
 * The learning policy and its underlining joint policy will automatically be told that this agent is its target agent, the agent definitions
 * in the world, and that this agent is the Q-source provider of the joint policy {@link MAQSourcePolicy}. If the set joint policy
 * is not an instance of {@link MAQSourcePolicy}, then an exception will be thrown.
 * 
 * 
 * @author James MacGlashan; adapted from code provided by Esha Gosh, John Meehan, Michalis Michaelidis
 *
 */
public class MultiAgentQLearning extends SGAgent implements MultiAgentQSourceProvider{

	
	/**
	 * The discount factor
	 */
	protected double									discount;
	
	
	/**
	 * This agent's Q-value source
	 */
	protected QSourceForSingleAgent						myQSource;
	
	/**
	 * The object that maps to other agent's Q-value sources
	 */
	protected AgentQSourceMap							qSourceMap;
	
	/**
	 * The learning policy to be followed
	 */
	protected PolicyFromJointPolicy						learningPolicy;
	
	/**
	 * The learning rate for updating Q-values
	 */
	protected LearningRate								learningRate;
	
	/**
	 * The Q-value initialization to use
	 */
	protected ValueFunctionInitialization				qInit;
	
	/**
	 * The state hashing factory used to index Q-values by state
	 */
	protected HashableStateFactory hashingFactory;
	
	/**
	 * The backup operator that defines the solution concept being learned
	 */
	protected SGBackupOperator							backupOperator;
	
	
	/**
	 * Whether this agent is using the Q-values stored by other agents in the world rather than keeping a separate copy of the Q-values for each agent itself.
	 */
	protected boolean									queryOtherAgentsQSource = true;
	
	
	/**
	 * Whether the agent needs to update its Q-values from a recent experience
	 */
	protected boolean									needsToUpdateQValue = false;
	
	/**
	 * The new Q-value to which the last Q-value needs to be udpated
	 */
	protected double									nextQValue = 0.;
	
	/**
	 * Which Q-value object needs to be updated
	 */
	protected JAQValue									qToUpdate = null;
	
	/**
	 * The total number of learning steps performed by this agent.
	 */
	protected int													totalNumberOfSteps = 0;
	
	
	
	/**
	 * Initializes this Q-learning agent. This agent's Q-source will use a {@link HashBackedQSource} q-source and the learning policy is defaulted
	 * to an epsilon = 0.1 maximum wellfare ({@link EGreedyMaxWellfare}) derived policy. If queryOtherAgentsForTheirQValues is set to true, then this agent will
	 * only store its own Q-values and will use the other agent's stored Q-values to determine theirs.
	 * @param d the domain in which to perform learing
	 * @param discount the discount factor
	 * @param learningRate the constant learning rate
	 * @param hashFactory the hashing factory used to index states and Q-values
	 * @param qInit the default Q-value to which all initial Q-values will be initialized
	 * @param backupOperator the backup operator to use that defines the solution concept being learned
	 * @param queryOtherAgentsForTheirQValues it true, then the agent uses the Q-values for other agents that are stored by them; if false then the agent stores a Q-value for each other agent in the world.
	 */
	public MultiAgentQLearning(SGDomain d, double discount, double learningRate, HashableStateFactory hashFactory, double qInit, SGBackupOperator backupOperator, boolean queryOtherAgentsForTheirQValues){
		this.init(d);
		this.discount = discount;
		this.learningRate = new ConstantLR(learningRate);
		this.hashingFactory = hashFactory;
		this.qInit = new ValueFunctionInitialization.ConstantValueFunctionInitialization(qInit);
		this.backupOperator = backupOperator;
		this.queryOtherAgentsQSource = queryOtherAgentsForTheirQValues;
		
		this.myQSource = new QSourceForSingleAgent.HashBackedQSource(this.hashingFactory, this.qInit);
		
		this.learningPolicy = new PolicyFromJointPolicy(new EGreedyMaxWellfare(this, 0.1));
	}
	
	
	/**
	 * Initializes this Q-learning agent. This agent's Q-source will use a {@link HashBackedQSource} q-source and the learning policy is defaulted
	 * to an epsilon = 0.1 maximum wellfare ({@link EGreedyMaxWellfare}) derived policy. If queryOtherAgentsForTheirQValues is set to true, then this agent will
	 * only store its own Q-values and will use the other agent's stored Q-values to determine theirs.
	 * @param d the domain in which to perform learing
	 * @param discount the discount factor
	 * @param learningRate the learning rate function to use
	 * @param hashFactory the hashing factory used to index states and Q-values
	 * @param qInit the q-value initialization to use
	 * @param backupOperator the backup operator to use that defines the solution concept being learned
	 * @param queryOtherAgentsForTheirQValues it true, then the agent uses the Q-values for other agents that are stored by them; if false then the agent stores a Q-value for each other agent in the world.
	 */
	public MultiAgentQLearning(SGDomain d, double discount, LearningRate learningRate, HashableStateFactory hashFactory, ValueFunctionInitialization qInit, SGBackupOperator backupOperator, boolean queryOtherAgentsForTheirQValues){
		this.init(d);
		this.discount = discount;
		this.learningRate = learningRate;
		this.hashingFactory = hashFactory;
		this.qInit = qInit;
		this.backupOperator = backupOperator;
		this.queryOtherAgentsQSource = queryOtherAgentsForTheirQValues;
		
		this.myQSource = new QSourceForSingleAgent.HashBackedQSource(this.hashingFactory, this.qInit);
		
		this.learningPolicy = new PolicyFromJointPolicy(new EGreedyMaxWellfare(this, 0.1));
	}
	
	
	@Override
	public void joinWorld(World w, SGAgentType as){
		super.joinWorld(w, as);
		this.learningPolicy.setActingAgentName(this.worldAgentName);
	}
	
	
	/**
	 * Returns this agent's individual Q-value source
	 * @return this agent's individual Q-value source
	 */
	public QSourceForSingleAgent getMyQSource(){
		return myQSource;
	}
	
	@Override
	public AgentQSourceMap getQSources(){
		return this.qSourceMap;
	}
	
	/**
	 * Sets the learning policy to be followed by the agent. The underlining joint policy of the learning policy be an instance of
	 * {@link MultiAgentQLearning} or a runtime exception will be thrown.
	 * @param p the learning policy to follow
	 */
	public void setLearningPolicy(PolicyFromJointPolicy p){
		if(!(p.getJointPolicy() instanceof MAQSourcePolicy)){
			throw new RuntimeException("The underlining joint policy must be of type MAQSourcePolicy for the MultiAgentQLearning agent");
		}
		this.learningPolicy = p;
		this.learningPolicy.setActingAgentName(this.worldAgentName);
		((MAQSourcePolicy)this.learningPolicy.getJointPolicy()).setQSourceProvider(this);
	}
	
	@Override
	public void gameStarting() {
		if(this.qSourceMap == null){
			if(this.queryOtherAgentsQSource){
				this.qSourceMap = new MAQLControlledQSourceMap(this.world.getRegisteredAgents());
			}
			else{
				Map<String, QSourceForSingleAgent> qSourceMapping = new HashMap<String, QSourceForSingleAgent>();
				for(SGAgent a : this.world.getRegisteredAgents()){
					if(a != this){
						qSourceMapping.put(a.getAgentName(), new QSourceForSingleAgent.HashBackedQSource(this.hashingFactory, this.qInit));
					}
					else{
						qSourceMapping.put(a.getAgentName(), this.myQSource);
					}
				}
				this.qSourceMap = new HashMapAgentQSourceMap(qSourceMapping);
			}
			this.learningPolicy.getJointPolicy().setAgentsInJointPolicyFromWorld(this.world);
		}
		
		
	}

	@Override
	public SGAgentAction getAction(State s) {
		this.updateLatestQValue();
		this.learningPolicy.getJointPolicy().setAgentsInJointPolicyFromWorld(this.world);
		return (SGAgentAction)this.learningPolicy.getAction(s);
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction,
			Map<String, Double> jointReward, State sprime, boolean isTerminal) {
		
		if(internalRewardFunction != null){
			jointReward = internalRewardFunction.reward(s, jointAction, sprime);
		}
		
		double r = jointReward.get(worldAgentName);
		
		if(r > 0.){
			//System.out.println("Big reward.");
		}
		
		this.needsToUpdateQValue = true;
		this.qToUpdate = this.getMyQSource().getQValueFor(s, jointAction);
		
		double backUpValue = 0.;
		if(!isTerminal){
			backUpValue = this.backupOperator.performBackup(sprime, this.worldAgentName, this.world.getAgentDefinitions(), this.qSourceMap);
		}
		
		this.nextQValue = qToUpdate.q + this.learningRate.pollLearningRate(this.totalNumberOfSteps, s, jointAction) * (r + (this.discount * backUpValue) - this.qToUpdate.q);
		
		this.totalNumberOfSteps++;

	}

	@Override
	public void gameTerminated() {
		this.updateLatestQValue();

	}
	
	
	/**
	 * Updates the Q-value for the most recent observation if it has not already been updated
	 */
	protected void updateLatestQValue(){
		if(needsToUpdateQValue){
			this.qToUpdate.q = nextQValue;
			this.qToUpdate = null;
			this.needsToUpdateQValue = false;
		}
	}

}
