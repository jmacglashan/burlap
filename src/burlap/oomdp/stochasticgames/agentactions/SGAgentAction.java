package burlap.oomdp.stochasticgames.agentactions;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.SGDomain;


/**
 * An abstract class for providing action definitions that are selectable by agents
 * ({@link burlap.oomdp.stochasticgames.SGAgent}) in a stochastic game. This class is analogous
 * to the single-agent action definition class, {@link burlap.oomdp.singleagent.Action}, except it does
 * not include the transition dynamic information which is instead provided by a {@link burlap.oomdp.stochasticgames.JointActionModel}
 * that takes the simultaneous action selections of all agents in a game and determines the outcome.
 * <p>
 * A {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} is closely associated with an implementation of
 * the {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction} class. An {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction}
 * differs from a {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} in that the {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction}
 * includes parameter assignment information necessary for the agent to execute the action.
 * <p>
 * Implementing this class requires subclassing the following abstract methods:<p>
 * {@link #applicableInState(burlap.oomdp.core.states.State, GroundedSGAgentAction)}, <p>
 * {@link #isParameterized()}, <p>
 * {@link #getAssociatedGroundedAction(String)}, and<p>
 * {@link #getAllApplicableGroundedActions(burlap.oomdp.core.states.State, String)}. <p>
 * The {@link #applicableInState(burlap.oomdp.core.states.State, GroundedSGAgentAction)}
 * method defines the preconditions of the action definition. The latter three methods are important
 * for defining parameterized actions. Note that if you If you are defining a parameter-less action that has no
 * preconditions (can be executed in any state), then you may simply use the {@link SimpleSGAgentAction}
 * implementation for creating your action definition. Otherwise, the {@link #isParameterized()} method should
 * return true and you will also need to implementing the remaining methods.
 * <p>
 * As noted previously, an {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction} implementation
 * stores a set of parameter assignments that need to be provided to apply your parameterized {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction}.
 * For custom parameterizations, you will need to subclass {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction} to include data
 * members for parameter assignments. The {@link #getAssociatedGroundedAction(String)} should then return an instance of your custom
 * {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction} with its {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction#action} datamember
 * pointing to this  {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} and its {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction#actingAgent}
 * data member pointing to the actingAgent argument. The parameter assignments in the returned {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction}
 * do not need to be specified; this method serves as a means for simply generating an instance of the associated {@link burlap.oomdp.singleagent.GroundedAction}.
 * <p>
 * The {@link #getAllApplicableGroundedActions(burlap.oomdp.core.states.State, String)} method should return a list of {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction}
 * instances, covering all possible parameterizations of this action for the acting agent in the given state. However,
 * the returned list should only include {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction} instances that satisfy the
 * {@link #applicableInState(burlap.oomdp.core.states.State, GroundedSGAgentAction)} method.
 * <p>
 * Overriding these two methods and having them return a custom {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction} subclass
 * is all that is required to implement a parameterized {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} and by allowing you to
 * define your own subclass of {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction},
 * you can have any kind of {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction}
 * parametrization that you'd like.
 * That said, a common form of {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} parameterization is an action that operates on OO-MDP
 * {@link burlap.oomdp.core.objects.ObjectInstance} references in a state. If you would like to have a OO-MDP object parameterization,
 * rather than define your own subclass, you should consider subclassing the {@link burlap.oomdp.stochasticgames.agentactions.ObParamSGAgentAction}
 * class. See it's documentation for more details.
 *
 * @author James MacGlashan
 *
 */
public abstract class SGAgentAction {

	public String actionName;
	public SGDomain domain;
	
	

	
	/**
	 * Initializes this single action to be for the given domain and with the given name. This action
	 * is automatically added to the given domain
	 * @param d the domain to which this action belongs
	 * @param name the name of this action
	 */
	public SGAgentAction(SGDomain d, String name){
		this.actionName = name;
		this.domain = d;
		this.domain.addSGAgentAction(this);
	}




	/**
	 * Returns true if this action can be applied in the given state by the given agent with the given parameters.
	 * @param s the state in which the action would be executed.
	 * @param gsa a {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction} containing the agent name and any parameters of the action
	 * @return true if this action can be executed; false otherwise.
	 */
	public abstract boolean applicableInState(State s, GroundedSGAgentAction gsa);

	
	/**
	 * Returns true if this action is parameterized.
	 * @return true if this action is parameterized; false otherwise.
	 */
	public abstract boolean isParameterized();
	


	/**
	 * Returns a {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction} instance that
	 * is associated with this {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} for
	 * the provided acting agent, without any parameters set (if there are parameters to set).
	 * @param actingAgent the name of the acting agent
	 * @return the {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction} associated with this {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction}.
	 */
	public abstract GroundedSGAgentAction getAssociatedGroundedAction(String actingAgent);

	/**
	 * Returns all possible grounded versions of this single action for a given state and acting agent.
	 * @param s the state in which the agent would execute this action
	 * @param actingAgent the agent who would execute the action
	 * @return all possible grounded versions of this single action for a given state and acting agent.
	 */
	public abstract List<GroundedSGAgentAction> getAllApplicableGroundedActions(State s, String actingAgent);

	/**
	 * Returns all possible grounded versions of the provided list of SingleAction objects that an agent can take in the given state. 
	 * @param s the state in which to execute actions
	 * @param actingAgent the agent who will be executing the actions
	 * @param actions the list of actions that to get the grounded version for
	 * @return all possible grounded versions of the provided list of SingleAction objects
	 */
	public static List <GroundedSGAgentAction> getAllApplicableGroundedActionsFromActionList(State s, String actingAgent, List<SGAgentAction> actions){
		List <GroundedSGAgentAction> res = new ArrayList<GroundedSGAgentAction>();
		for(SGAgentAction sa : actions){
			res.addAll(sa.getAllApplicableGroundedActions(s, actingAgent));
		}
		return res;
	}
	
	

	
	
	@Override
	public int hashCode(){
		return actionName.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof SGAgentAction)){
			return false;
		}
		
		return ((SGAgentAction)o).actionName.equals(actionName);
	}

}
