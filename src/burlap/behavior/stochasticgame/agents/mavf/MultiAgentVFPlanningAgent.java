package burlap.behavior.stochasticgame.agents.mavf;

import java.util.Map;

import burlap.behavior.stochasticgame.PolicyFromJointPolicy;
import burlap.behavior.stochasticgame.mavaluefunction.MAQSourcePolicy;
import burlap.behavior.stochasticgame.mavaluefunction.MAValueFunctionPlanner;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.AgentType;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.World;


/**
 * A agent that using a multi agent value function planning algorithm (instance of {@link MAValueFunctionPlanner}) to compute the value of each state and then follow
 * a policy derived from a joint policy that is derived from that estimated value function. This is achieved by at each step by the {@link MAValueFunctionPlanner#planFromState(State)} being first
 * called and then following the policy. Ideally, the planning object should only perform planning for a state if it has not already planned for it. The joint policy
 * underlining the policy the agent follows must be an instance of {@link MAQSourcePolicy}. Furthermore, when the policy is set, the underlining joint policy
 * will automatically be set to use this agent's planning object as the value function source and the set of agents will automatically be set to the involved in this agent's
 * world. The {@link PolicyFromJointPolicy} will also be told that this agent is its target.
 * 
 * @author James MacGlashan
 *
 */
public class MultiAgentVFPlanningAgent extends Agent {

	
	/**
	 * The planner this agent will use to estiamte the value function and thereby determine its policy.
	 */
	protected MAValueFunctionPlanner		planner;
	
	/**
	 * The policy dervied from a joint policy derived from the planner's value function estimate that this agent will follow.
	 */
	protected PolicyFromJointPolicy			policy;
	
	/**
	 * Whether the agent definitions for this planner have been set yet.
	 */
	protected boolean						setAgentDefinitions = false;
	
	
	
	/**
	 * Initializes. The underlining joint policy of the policy must be an instance of {@link MAQSourcePolicy} or a runtime exception will be thrown.
	 * The joint policy will automatically be set to use the provided planner as the value function source.
	 * @param domain the domain in which the agent will act
	 * @param planner the planner the agent should use for determining its policy
	 * @param policy the policy that will use the planners value function as a source.
	 */
	public MultiAgentVFPlanningAgent(SGDomain domain, MAValueFunctionPlanner planner, PolicyFromJointPolicy policy){
		if(!(policy.getJointPolicy() instanceof MAQSourcePolicy)){
			throw new RuntimeException("The underlining joint policy must be of type MAQSourcePolicy for the MultiAgentVFPlanningAgent.");
		}
		super.init(domain);
		this.planner = planner;
		this.policy = policy;
		((MAQSourcePolicy)this.policy.getJointPolicy()).setQSourceProvider(planner);
	}
	
	
	
	/**
	 * Sets the policy derived from this agents planner to follow. he underlining joint policy of the policy must be an instance of {@link MAQSourcePolicy} 
	 * or a runtime exception will be thrown.
	 * The joint policy will automatically be set to use the provided planner as the value function source.
	 * @param policy the policy that will use the planners value function as a source.
	 */
	public void setPolicy(PolicyFromJointPolicy policy){
		if(!(policy.getJointPolicy() instanceof MAQSourcePolicy)){
			throw new RuntimeException("The underlining joint policy must be of type MAQSourcePolicy for the MultiAgentVFPlanningAgent.");
		}
		this.policy = policy;
		((MAQSourcePolicy)this.policy.getJointPolicy()).setQSourceProvider(planner);
		this.policy.setActingAgentName(this.worldAgentName);
		
	}
	
	@Override
	public void joinWorld(World w, AgentType as){
		super.joinWorld(w, as);
		this.policy.setActingAgentName(this.worldAgentName);
	}
	
	
	@Override
	public void gameStarting() {
		if(!this.setAgentDefinitions){
			this.planner.setAgentDefinitions(this.world.getAgentDefinitions());
			this.policy.getJointPolicy().setAgentsInJointPolicy(this.world.getAgentDefinitions());
			this.setAgentDefinitions = true;
		}
	}

	@Override
	public GroundedSingleAction getAction(State s) {
		this.planner.planFromState(s);
		return (GroundedSingleAction)this.policy.getAction(s);
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction,
			Map<String, Double> jointReward, State sprime, boolean isTerminal) {
		
		//nothing to do

	}

	@Override
	public void gameTerminated() {
		//nothing to do
	}

}
