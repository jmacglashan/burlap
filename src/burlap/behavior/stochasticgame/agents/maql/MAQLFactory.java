package burlap.behavior.stochasticgame.agents.maql;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.singleagent.ValueFunctionInitialization;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.stochasticgame.PolicyFromJointPolicy;
import burlap.behavior.stochasticgame.mavaluefunction.SGBackupOperator;
import burlap.behavior.stochasticgame.mavaluefunction.backupOperators.CoCoQ;
import burlap.behavior.stochasticgame.mavaluefunction.backupOperators.MaxQ;
import burlap.behavior.stochasticgame.mavaluefunction.policies.EGreedyJointPolicy;
import burlap.behavior.stochasticgame.mavaluefunction.policies.EGreedyMaxWellfare;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.AgentFactory;
import burlap.oomdp.stochasticgames.SGDomain;


/**
 * This class provides a factory for {@link MultiAgentQLearning} agents. Subclasses for specifc kinds of multi-agent Q-learning are also included.
 * The policy given to this factory is always copied when generating a new agent to ensure that multiple agents generated from the same factory
 * have unique policies tailored to their perspective.
 * @author James MacGlashan
 *
 */
public class MAQLFactory implements AgentFactory {

	protected SGDomain						domain;
	protected double						discount;
	protected LearningRate					learningRate;
	protected ValueFunctionInitialization	qInit;
	protected StateHashFactory				hashingFactory;
	protected SGBackupOperator				backupOperator;
	protected PolicyFromJointPolicy			learningPolicy;
	protected boolean						queryOtherAgentsQSource;
	
	
	/**
	 * Empty constructor. All parameters will need to be set with the {@link #init(SGDomain, double, LearningRate, StateHashFactory, ValueFunctionInitialization, SGBackupOperator, boolean, PolicyFromJointPolicy)} function
	 * after construction.
	 */
	public MAQLFactory(){
		
	}
	
	
	/**
	 * Initializes. The policy will be defaulted to a epsilon-greey max wellfare policy.
	 * @param d the domain in which to perform learing
	 * @param discount the discount factor
	 * @param learningRate the constant learning rate
	 * @param hashFactory the hashing factory used to index states and Q-values
	 * @param qInit the default Q-value to which all initial Q-values will be initialized
	 * @param backupOperator the backup operator to use that defines the solution concept being learned
	 * @param queryOtherAgentsForTheirQValues it true, then the agent uses the Q-values for other agents that are stored by them; if false then the agent stores a Q-value for each other agent in the world.
	 */
	public MAQLFactory(SGDomain d, double discount, double learningRate, StateHashFactory hashFactory, double qInit, SGBackupOperator backupOperator, boolean queryOtherAgentsForTheirQValues){
		this.domain = d;
		this.discount = discount;
		this.learningRate = new ConstantLR(learningRate);
		this.hashingFactory = hashFactory;
		this.qInit = new ValueFunctionInitialization.ConstantValueFunctionInitialization(qInit);
		this.backupOperator = backupOperator;
		this.queryOtherAgentsQSource = queryOtherAgentsForTheirQValues;
		this.learningPolicy = new PolicyFromJointPolicy(new EGreedyMaxWellfare(0.1));
	}
	
	
	/**
	 * Initializes. The policy will be defaulted to a epsilon-greey max wellfare policy.
	 * @param d the domain in which to perform learing
	 * @param discount the discount factor
	 * @param learningRate the learning rate function
	 * @param hashFactory the hashing factory used to index states and Q-values
	 * @param qInit the Q-value initialization function
	 * @param backupOperator the backup operator to use that defines the solution concept being learned
	 * @param queryOtherAgentsForTheirQValues it true, then the agent uses the Q-values for other agents that are stored by them; if false then the agent stores a Q-value for each other agent in the world.
	 * @param learningPolicy the learningPolicy to follow
	 */
	public MAQLFactory(SGDomain d, double discount, LearningRate learningRate, StateHashFactory hashFactory, ValueFunctionInitialization qInit, SGBackupOperator backupOperator, boolean queryOtherAgentsForTheirQValues, PolicyFromJointPolicy learningPolicy){
		this.domain = d;
		this.discount = discount;
		this.learningRate = learningRate;
		this.hashingFactory = hashFactory;
		this.qInit = qInit;
		this.backupOperator = backupOperator;
		this.queryOtherAgentsQSource = queryOtherAgentsForTheirQValues;
		this.learningPolicy = learningPolicy;
	}
	
	
	/**
	 * Initializes. The policy will be defaulted to a epsilon-greey max wellfare policy.
	 * @param d the domain in which to perform learing
	 * @param discount the discount factor
	 * @param learningRate the learning rate function
	 * @param hashFactory the hashing factory used to index states and Q-values
	 * @param qInit the Q-value initialization function
	 * @param backupOperator the backup operator to use that defines the solution concept being learned
	 * @param queryOtherAgentsForTheirQValues it true, then the agent uses the Q-values for other agents that are stored by them; if false then the agent stores a Q-value for each other agent in the world.
	 * @param learningPolicy the learningPolicy to follow
	 */
	public void init(SGDomain d, double discount, LearningRate learningRate, StateHashFactory hashFactory, ValueFunctionInitialization qInit, SGBackupOperator backupOperator, boolean queryOtherAgentsForTheirQValues, PolicyFromJointPolicy learningPolicy){
		this.domain = d;
		this.discount = discount;
		this.learningRate = learningRate;
		this.hashingFactory = hashFactory;
		this.qInit = qInit;
		this.backupOperator = backupOperator;
		this.queryOtherAgentsQSource = queryOtherAgentsForTheirQValues;
		this.learningPolicy = learningPolicy;
		
	}
	
	
	@Override
	public Agent generateAgent() {
		MultiAgentQLearning agent = new MultiAgentQLearning(domain, discount, learningRate, hashingFactory, qInit, backupOperator, queryOtherAgentsQSource);
		agent.setLearningPolicy(this.learningPolicy.copy());
		return agent;
	}
	
	
	
	
	
	
	/**
	 * Factory for generating CoCo-Q agents. Fixes the backup operator to CoCo-Q and the policy to an epsilon-greedy max wellfare policy.
	 * @author James MacGlashan
	 *
	 */
	public static class CoCoQLearningFactory extends MAQLFactory{
		
		public CoCoQLearningFactory(SGDomain d, double discount, LearningRate learningRate, StateHashFactory hashFactory, ValueFunctionInitialization qInit, boolean queryOtherAgentsForTheirQValues, double epsilon){
			this.init(d, discount, learningRate, hashFactory, qInit, new CoCoQ(), queryOtherAgentsForTheirQValues, new PolicyFromJointPolicy(new EGreedyMaxWellfare(epsilon)));
		}
		
	}
	
	
	/**
	 * Factory for generating Max multiagent Q-learning agents. This is also known as "maxmax-Q" or "friend-Q." Fixes the backup operator to the max operator and the policy to an epsilon-greedy policy.
	 * @author James MacGlashan
	 *
	 */
	public static class MAMaxQLearningFactory extends MAQLFactory{
		
		public MAMaxQLearningFactory(SGDomain d, double discount, LearningRate learningRate, StateHashFactory hashFactory, ValueFunctionInitialization qInit, boolean queryOtherAgentsForTheirQValues, double epsilon){
			this.init(d, discount, learningRate, hashFactory, qInit, new MaxQ(), queryOtherAgentsForTheirQValues, new PolicyFromJointPolicy(new EGreedyJointPolicy(epsilon)));
		}
		
	}
	

}
