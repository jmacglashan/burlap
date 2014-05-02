package burlap.behavior.stochasticgame.agents.maql;

import java.util.HashMap;
import java.util.Map;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.singleagent.ValueFunctionInitialization;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.stochasticgame.PolicyFromJointPolicy;
import burlap.behavior.stochasticgame.mavaluefunction.AgentQSourceMap;
import burlap.behavior.stochasticgame.mavaluefunction.AgentQSourceMap.HashMapAgentQSourceMap;
import burlap.behavior.stochasticgame.mavaluefunction.AgentQSourceMap.MAQLControlledQSourceMap;
import burlap.behavior.stochasticgame.mavaluefunction.JAQValue;
import burlap.behavior.stochasticgame.mavaluefunction.MAQSourcePolicy;
import burlap.behavior.stochasticgame.mavaluefunction.MultiAgentQSourceProvider;
import burlap.behavior.stochasticgame.mavaluefunction.QSourceForSingleAgent;
import burlap.behavior.stochasticgame.mavaluefunction.SGBackupOperator;
import burlap.behavior.stochasticgame.mavaluefunction.backupOperators.CoCoQ;
import burlap.behavior.stochasticgame.mavaluefunction.policies.EGreedyMaxWellfare;
import burlap.debugtools.DPrint;
import burlap.domain.stochasticgames.gridgame.GGVisualizer;
import burlap.domain.stochasticgames.gridgame.GridGame;
import burlap.domain.stochasticgames.gridgame.GridGameStandardMechanics;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.AgentType;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.JointReward;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.World;
import burlap.oomdp.stochasticgames.common.ConstantSGStateGenerator;
import burlap.oomdp.stochasticgames.common.VisualWorldObserver;
import burlap.oomdp.visualizer.Visualizer;


/**
 * A class for performing multi-agent Q-learning in which different Q-value backup operators can be provided to enable the learning
 * of different soution concepts. Multi-agent Q-learning differs from single agent Q-learning in that Q-values are associated
 * with joint actions, rather than actions, and in that a different Q-value is stored for each agent in the game.
 * <p/>
 * In this class, each agent stores its own Q-value and an object that provides a source for the Q-values of other agents. This allows
 * the storage of Q-values to vary so that an agent can store the Q-values forall other agents, or the map can provide access to the
 * Q-values stored by other MultiAgentQLearning agents in the world so that only one copy of each agent's Q-value is ever stored.
 * In the case of the latter, all agents should be implementing the same solution concept learning algorithm. Otherwise, each agent
 * should maintain their own set of Q-values.
 * <p/>
 * After an agent observes an outcome, it determines the change in Q-value. However, the agent will not actually update its Q-value
 * to the new value until it is asked for its next action ({@link #getAction(State)}) or until the {@link #gameTerminated()} message is sent.
 * Q-value updates are delayed in this way because if Q-values for each agent are shared and distributed among the agents, this ensures
 * that the Q-values are all updated after the next Q-value has been determined for each agent.
 * <p/>
 * In general the learning policy followed by this agent should reflect the needs of the solution concept being learned. For instance,
 * CoCo-Q should use some variant of a maximum wellfare joint policy.
 * <p/>
 * The learning policy and its underlining joint policy will automatically be told that this agent is its target agent, the agent definitions
 * in the world, and that this agent is the Q-source provider of the joint policy {@link MAQSourcePolicy}. If the set joint policy
 * is not an instance of {@link MAQSourcePolicy}, then an exception will be thrown.
 * 
 * 
 * @author James MacGlashan; adapted from code provided by Esha Gosh, John Meehan, Michalis Michaelidis
 *
 */
public class MultiAgentQLearning extends Agent implements MultiAgentQSourceProvider{

	
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
	protected StateHashFactory							hashingFactory;
	
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
	
	
	
	public static void main(String [] args){
		
		//create domain
		GridGame domainGen = new GridGame();
		final SGDomain domain = (SGDomain)domainGen.generateDomain();
		
		//create hashing factory that only hashes on the agent positions (ignores wall attributes)
		final DiscreteStateHashFactory hashingFactory = new DiscreteStateHashFactory();
		hashingFactory.addAttributeForClass(GridGame.CLASSAGENT, domain.getAttribute(GridGame.ATTX));
		hashingFactory.addAttributeForClass(GridGame.CLASSAGENT, domain.getAttribute(GridGame.ATTY));
		hashingFactory.addAttributeForClass(GridGame.CLASSAGENT, domain.getAttribute(GridGame.ATTPN));
		
		//parameters for q-learning
		final double discount = 0.95;
		final double learningRate = 0.1;
		final double defaultQ = 100;
		
		/*
		final State s = GridGame.getCleanState(domain, 2, 3, 3, 2, 5, 5);
		GridGame.setAgent(s, 0, 0, 0, 0);
		GridGame.setAgent(s, 1, 4, 0, 1);
		GridGame.setGoal(s, 0, 0, 4, 1);
		GridGame.setGoal(s, 1, 2, 4, 0);
		GridGame.setGoal(s, 2, 4, 4, 2);
		GridGame.setHorizontalWall(s, 2, 4, 1, 3, 0);
		*/
		final State s = GridGame.getTurkeyInitialState(domain);
		
		JointReward rf = new GridGame.GGJointRewardFunction(domain, -1, 100, false);
		
		//create our world
		World w = new World(domain, new GridGameStandardMechanics(domain), rf, new GridGame.GGTerminalFunction(domain), 
				new ConstantSGStateGenerator(s));
		
		Visualizer v = GGVisualizer.getVisualizer(9, 9);
		VisualWorldObserver wob = new VisualWorldObserver(domain, v);
		wob.setFrameDelay(1000);
		wob.initGUI();
		
		
		//make a single agent type that can use all actions and refers to the agent class of grid game that we will use for both our agents
		AgentType at = new AgentType("default", domain.getObjectClass(GridGame.CLASSAGENT), domain.getSingleActions());
		
		/*
		MultiAgentQLearning a0 = new MultiAgentQLearning(domain, discount, learningRate, hashingFactory, defaultQ, new MaxBackup(), true);
		MultiAgentQLearning a1 = new MultiAgentQLearning(domain, discount, learningRate, hashingFactory, defaultQ, new MaxBackup(), true);
		*/
		
		MultiAgentQLearning a0 = new MultiAgentQLearning(domain, discount, learningRate, hashingFactory, defaultQ, new CoCoQ(), true);
		MultiAgentQLearning a1 = new MultiAgentQLearning(domain, discount, learningRate, hashingFactory, defaultQ, new CoCoQ(), true);
		
		/*
		SetStrategyAgent a1 = new SetStrategyAgent(domain, new Policy() {
			
			@Override
			public boolean isStochastic() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isDefinedFor(State s) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public List<ActionProb> getActionDistributionForState(State s) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public AbstractGroundedAction getAction(State s) {
				GroundedSingleAction gsas[] = new GroundedSingleAction[]{new GroundedSingleAction("me", domain.getSingleAction(GridGame.ACTIONNOOP), ""),
																		 new GroundedSingleAction("me", domain.getSingleAction(GridGame.ACTIONNORTH), ""),
																		 new GroundedSingleAction("me", domain.getSingleAction(GridGame.ACTIONSOUTH), ""),
																		 new GroundedSingleAction("me", domain.getSingleAction(GridGame.ACTIONEAST), ""),
																		 new GroundedSingleAction("me", domain.getSingleAction(GridGame.ACTIONWEST), "")};
				return gsas[RandomFactory.getMapped(0).nextInt(5)];
				//return gsas[0];
			}
		});*/
		
		a0.joinWorld(w, at);
		a1.joinWorld(w, at);
		
		
		//don't have the world print out debug info (comment out if you want to see it!)
		DPrint.toggleCode(w.getDebugId(), false);
		
		
		System.out.println("Starting training");
		int ngames = 5000;
		for(int i = 0; i < ngames; i++){
			if(i % 10 == 0){
				System.out.println("Game: " + i);
			}
			w.runGame();
		}
		
		System.out.println("Finished training");
		
		v.updateState(s);
		w.addWorldObserver(wob);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//turn debug back on if we want to observe the behavior of agents after they have already learned how to behave
		DPrint.toggleCode(w.getDebugId(), true);
		
		a0.setLearningPolicy(new PolicyFromJointPolicy(a0.getAgentName(), new EGreedyMaxWellfare(a0, 0.0)));
		a1.setLearningPolicy(new PolicyFromJointPolicy(a1.getAgentName(), new EGreedyMaxWellfare(a0, 0.0)));
		
		//run game to observe behavior
		w.runGame();
		
		
	}
	
	
	/**
	 * Initializes this Q-learning agent. This agent's Q-source will use a {@link QSourceForSingleAgent.HashBackedQSource} q-source and the learning policy is defaulted
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
	public MultiAgentQLearning(SGDomain d, double discount, double learningRate, StateHashFactory hashFactory, double qInit, SGBackupOperator backupOperator, boolean queryOtherAgentsForTheirQValues){
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
	 * Initializes this Q-learning agent. This agent's Q-source will use a {@link QSourceForSingleAgent.HashBackedQSource} q-source and the learning policy is defaulted
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
	public MultiAgentQLearning(SGDomain d, double discount, LearningRate learningRate, StateHashFactory hashFactory, ValueFunctionInitialization qInit, SGBackupOperator backupOperator, boolean queryOtherAgentsForTheirQValues){
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
	public void joinWorld(World w, AgentType as){
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
				for(Agent a : this.world.getRegisteredAgents()){
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
	public GroundedSingleAction getAction(State s) {
		this.updateLatestQValue();
		this.learningPolicy.getJointPolicy().setAgentsInJointPolicyFromWorld(this.world);
		return (GroundedSingleAction)this.learningPolicy.getAction(s);
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
		
		this.nextQValue = qToUpdate.q + this.learningRate.pollLearningRate(s, jointAction) * (r + (this.discount * backUpValue) - this.qToUpdate.q);
		
		

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
