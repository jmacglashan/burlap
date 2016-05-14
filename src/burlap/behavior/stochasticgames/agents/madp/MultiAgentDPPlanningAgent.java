package burlap.behavior.stochasticgames.agents.madp;

import java.util.Map;

import burlap.behavior.stochasticgames.PolicyFromJointPolicy;
import burlap.behavior.stochasticgames.madynamicprogramming.MAQSourcePolicy;
import burlap.behavior.stochasticgames.madynamicprogramming.MADynamicProgramming;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.SGAgent;
import burlap.mdp.stochasticgames.SGAgentType;
import burlap.mdp.stochasticgames.agentactions.SGAgentAction;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.World;


/**
 * A agent that using a {@link burlap.behavior.stochasticgames.madynamicprogramming.MADynamicProgramming} planning algorithm to compute the value of each state and then follow
 * a policy derived from a joint policy that is derived from that estimated value function. This is achieved by at each step by the {@link burlap.behavior.stochasticgames.madynamicprogramming.MADynamicProgramming#planFromState(State)} being first
 * called and then following the policy. Ideally, the planning object should only perform planning for a state if it has not already planned for it. The joint policy
 * underlining the policy the agent follows must be an instance of {@link MAQSourcePolicy}. Furthermore, when the policy is set, the underlining joint policy
 * will automatically be set to use this agent's planning object as the value function source and the set of agents will automatically be set to the involved in this agent's
 * world. The {@link PolicyFromJointPolicy} will also be told that this agent is its target.
 * 
 * @author James MacGlashan
 *
 */
public class MultiAgentDPPlanningAgent extends SGAgent {

	
	/**
	 * The valueFunction this agent will use to estiamte the value function and thereby determine its policy.
	 */
	protected MADynamicProgramming planner;
	
	/**
	 * The policy dervied from a joint policy derived from the valueFunction's value function estimate that this agent will follow.
	 */
	protected PolicyFromJointPolicy			policy;
	
	/**
	 * Whether the agent definitions for this valueFunction have been set yet.
	 */
	protected boolean						setAgentDefinitions = false;
	
	
	
	/**
	 * Initializes. The underlining joint policy of the policy must be an instance of {@link MAQSourcePolicy} or a runtime exception will be thrown.
	 * The joint policy will automatically be set to use the provided valueFunction as the value function source.
	 * @param domain the domain in which the agent will act
	 * @param planner the valueFunction the agent should use for determining its policy
	 * @param policy the policy that will use the planners value function as a source.
	 */
	public MultiAgentDPPlanningAgent(SGDomain domain, MADynamicProgramming planner, PolicyFromJointPolicy policy){
		if(!(policy.getJointPolicy() instanceof MAQSourcePolicy)){
			throw new RuntimeException("The underlining joint policy must be of type MAQSourcePolicy for the MultiAgentVFPlanningAgent.");
		}
		super.init(domain);
		this.planner = planner;
		this.policy = policy;
		((MAQSourcePolicy)this.policy.getJointPolicy()).setQSourceProvider(planner);
	}
	
	
	
	/**
	 * Sets the policy derived from this agents valueFunction to follow. he underlining joint policy of the policy must be an instance of {@link MAQSourcePolicy}
	 * or a runtime exception will be thrown.
	 * The joint policy will automatically be set to use the provided valueFunction as the value function source.
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
	public void joinWorld(World w, SGAgentType as){
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
	public SGAgentAction getAction(State s) {
		this.planner.planFromState(s);
		return (SGAgentAction)this.policy.getAction(s);
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
