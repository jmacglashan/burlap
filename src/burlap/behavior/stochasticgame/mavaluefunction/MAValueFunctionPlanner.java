package burlap.behavior.stochasticgame.mavaluefunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.ValueFunctionInitialization;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.stochasticgame.mavaluefunction.AgentQSourceMap.HashMapAgentQSourceMap;
import burlap.behavior.stochasticgame.mavaluefunction.QSourceForSingleAgent.HashBackedQSource;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.stochasticgames.AgentType;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.JointReward;
import burlap.oomdp.stochasticgames.SGDomain;


/**
 * An abstract value function based planning algorithm base for sequential stochastic games that require the computation of Q-values for each agent for each joint action. Value function
 * updates are perfored using a Bellman-like backup operator; however, planning for different solution concepts is achieved by providing different backup operators via the
 * {@link SGBackupOperator} object.
 * <p/>
 * Note that the agent definitions can only be changed up until planning begins. Once planning has begun, the agent definitions must remain fixed
 * for consistency of planning results. If the the client tries to change the agent definitions after planning has already begun, then a runtime exception will
 * be thrown.
 * <p/>
 * Since value function planning algorithms compute multi-agent Q-values, this object implements the {@link MultiAgentQSourceProvider} interface. 
 * 
 * @author James MacGlashan
 *
 */
public abstract class MAValueFunctionPlanner implements MultiAgentQSourceProvider{

	/**
	 * The domain in which planning is to be performed
	 */
	protected SGDomain						domain;
	
	/**
	 * The agent definitions for which planning is performed.
	 */
	protected Map<String, AgentType>		agentDefinitions;
	
	/**
	 * The joint action model to use in planning.
	 */
	protected JointActionModel				jointActionModel;
	
	/**
	 * The joint reward function
	 */
	protected JointReward					jointReward;
	
	/**
	 * The state terminal function.
	 */
	protected TerminalFunction				terminalFunction;
	
	/**
	 * The discount factor in [0, 1]
	 */
	protected double						discount;
	
	/**
	 * The state hashing factory used to query the value function for individual states
	 */
	protected StateHashFactory				hashingFactory;
	
	/**
	 * The Q-value initialization function to use.
	 */
	protected ValueFunctionInitialization	qInit;
	
	/**
	 * The backup operating defining the solution concept to use.
	 */
	protected SGBackupOperator				backupOperator;
	
	/**
	 * The Hash map backed multi-agent Q-source in which to store Q-values.
	 */
	protected HashMapAgentQSourceMap		qSources;
	
	
	/**
	 * Whether planning has begun or not.
	 */
	protected boolean						planningStarted = false;
	
	
	
	
	/**
	 * Initializes all the main datstructres of the value function planner
	 * @param domain the domain in which to perform planning
	 * @param agentDefinitions the definitions of the agents involved in the planning problem.
	 * @param jointActionModel the joint action model
	 * @param jointReward the joint reward function
	 * @param terminalFunction the terminal state function
	 * @param discount the discount factor
	 * @param hashingFactory the state hashing factorying to use to lookup Q-values for individual states
	 * @param qInit the Q-value initialization function to use
	 * @param backupOperator the solution concept backup operator to use.
	 */
	public void initMAVF(SGDomain domain, Map<String, AgentType> agentDefinitions, JointActionModel jointActionModel, JointReward jointReward, TerminalFunction terminalFunction, 
			double discount, StateHashFactory hashingFactory, ValueFunctionInitialization qInit, SGBackupOperator backupOperator){
	
		this.domain = domain;
		this.jointActionModel = jointActionModel;
		this.jointReward = jointReward;
		this.terminalFunction = terminalFunction;
		this.discount = discount;
		this.hashingFactory = hashingFactory;
		this.qInit = qInit;
		this.backupOperator = backupOperator;
		
		
		this.setAgentDefinitions(agentDefinitions);
		
	}
	
	
	/**
	 * Indicates whether planning has begun or not. Once planning has begun, the agent defintions cannot be changed or a runtime exception will be thrown.
	 * @return true is planning has started; false if it has not.
	 */
	public boolean hasStartedPlanning(){
		return this.planningStarted;
	}
	
	
	/**
	 * Sets/changes the agent definitions to use in planning. This can only be change up until planning begins, after which a runtime exception will
	 * be thrown. To check if the planning has already begun, use the {@link #hasStartedPlanning()} method.
	 * @param agentDefinitions the definitions of agents involve in the planning problem.
	 */
	public void setAgentDefinitions(Map<String, AgentType> agentDefinitions){
		
		if(this.planningStarted){
			throw new RuntimeException("Cannot reset the agent definitions after planning has already started.");
		}
		
		if(agentDefinitions == null){
			return;
		}
		
		if(this.agentDefinitions == agentDefinitions){
			return ;
		}
		
		this.agentDefinitions = agentDefinitions;
		
		Map<String, QSourceForSingleAgent> hQSources = new HashMap<String, QSourceForSingleAgent>();
		for(String agent : this.agentDefinitions.keySet()){
			HashBackedQSource qs = new QSourceForSingleAgent.HashBackedQSource(this.hashingFactory, this.qInit);
			hQSources.put(agent, qs);
		}
		this.qSources = new AgentQSourceMap.HashMapAgentQSourceMap(hQSources);
	}
	
	
	/**
	 * Calling this method causes planning to be performed from State s.
	 * @param s the state from which planning is to be performed.
	 */
	public abstract void planFromState(State s);
	
	
	@Override
	public AgentQSourceMap getQSources(){
		return qSources;
	}
	
	
	
	/**
	 * Backups all the q-values for the given state for all agents defined in the agent definitions.
	 * @param s the state for which Q-value backups will be performed.
	 * @return the maximum Q-value change from this backup.
	 */
	public double backupAllQs(State s){
		
		this.planningStarted = true;
		
		List<JointActionTransitions> jats = this.getAllJATs(s);
		
		double maxChange = Double.NEGATIVE_INFINITY;
		
		for(JointActionTransitions jat : jats){
			double change = this.backupAllQsForAction(s, jat);
			maxChange = Math.max(maxChange, change);
		}
		
		return maxChange;
	}
	
	
	/**
	 * Returns all possible joint action transition dynamics from a state in a list of {@link JointActionTransitions} objects.
	 * @param s the state from which all joint action transtion dynamics will be returned.
	 * @return all possible joint action transition dynamics from a state in a list of {@link JointActionTransitions} objects.
	 */
	protected List<JointActionTransitions> getAllJATs(State s){
		List<JointAction> jas = JointAction.getAllJointActions(s, this.agentDefinitions);
		List<JointActionTransitions> jats = new ArrayList<MAValueFunctionPlanner.JointActionTransitions>(jas.size());
		for(JointAction ja : jas){
			jats.add(new JointActionTransitions(s, ja));
		}
		return jats;
	}
	
	/**
	 * Perfoms all agent's Q-value backup for the given state and joint action specified in the {@link JointActionTransitions}.
	 * @param s the state for which Q-value backups will be performed.
	 * @param jat the {@link JointActionTransitions} object containing all possible joint action transitons from state s for a given joint action.
	 * @return the maximum Q-value change.
	 */
	protected double backupAllQsForAction(State s, JointActionTransitions jat){
		
		this.planningStarted = true;
		
		double maxChange = Double.NEGATIVE_INFINITY;
		
		for(String a : agentDefinitions.keySet()){
			
			QSourceForSingleAgent qs = qSources.agentQSource(a);
			JAQValue curQ = qs.getQValueFor(s, jat.ja);
			
			double sumNextQ = 0.;
			
			if(!this.terminalFunction.isTerminal(s)){
			
				for(int i = 0; i < jat.tps.size(); i++){
					TransitionProbability tp = jat.tps.get(i);
					double p = tp.p;
					State sp = tp.s;
					double r = jat.jrs.get(i).get(a);
					double bpQ = 0.;
					
					if(!this.terminalFunction.isTerminal(sp)){
						bpQ = this.backupOperator.performBackup(sp, a, this.agentDefinitions, this.qSources);
					}
					
					
					double contribution = r + this.discount*bpQ;
					double weightedContribution = p*contribution;
					
					sumNextQ += weightedContribution;
					
				}
				
			}
			
			double change = Math.abs(sumNextQ - curQ.q);
			maxChange = Math.max(maxChange, change);
			curQ.q = sumNextQ;
			
		}
		
		return maxChange;
	}
	
	
	/**
	 * A class for holding all of the transition dynamic information for a given joint action in a given state. This includes
	 * state transitions as well as joint rewards. Information is stored as a triple consisting of the {@link JointAction},
	 * the list of state transtitions ({@link TransitionProbability} objects), and a list of joint rewards (A map from agent names
	 * to rewards received).
	 * @author James MacGlashan
	 *
	 */
	public class JointActionTransitions{
		public JointAction ja;
		public List<TransitionProbability> tps;
		public List<Map<String, Double>> jrs;
		
		
		/**
		 * Generates the transition information for the given state and joint aciton
		 * @param s the state in which the joint action is applied
		 * @param ja the joint action applied to the given state
		 */
		public JointActionTransitions(State s, JointAction ja){
			this.ja = ja;
			this.tps = MAValueFunctionPlanner.this.jointActionModel.transitionProbsFor(s, ja);
			this.jrs = new ArrayList<Map<String,Double>>(this.tps.size());
			for(TransitionProbability tp : this.tps){
				Map<String, Double> jr = MAValueFunctionPlanner.this.jointReward.reward(s, ja, tp.s);
				this.jrs.add(jr);
			}
		}
		
	}
	
}
