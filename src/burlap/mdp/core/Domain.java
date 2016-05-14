package burlap.mdp.core;


import burlap.mdp.singleagent.ActionType;
import burlap.mdp.stochasticgames.agentactions.SGAgentActionType;

import java.util.List;


/**
 * This is the base interface for a problem domain. For single-agent
 * domains, the physics and actions are defined with the {@link ActionType} object and for
 * multi-agent stochastic games, they are defined with {@link SGAgentActionType} and a
 * {@link burlap.mdp.stochasticgames.JointActionModel}. See the respective single-agent {@link burlap.mdp.singleagent.SADomain}
 * and stochastic games {@link burlap.mdp.stochasticgames.SGDomain} subclasses for more information on their definitions.
 * <p>
 * Note that a {@link burlap.mdp.core.Domain} does *not* include task information, which will be defined separately with
 * a {@link burlap.mdp.singleagent.RewardFunction} or {@link burlap.mdp.stochasticgames.JointReward}, and a {@link burlap.mdp.core.TerminalFunction}.
 * @author James MacGlashan
 */
public interface Domain {

	/**
	 * Add a single agent action that defines this domain. This method
	 * will throw a runtime exception if this domain is not an instance of
	 * the single agent domain (SADomain). The action will not be added if this domain already has a instance with the same name.
	 * @param act the single agent action to add.
	 */
	void addAction(ActionType act);
	
	/**
	 * Add a {@link SGAgentActionType} that can be executed by an agent in the game.
	 * The set of {@link SGAgentActionType}s defines the set of joint actions in the stochastic domain (as the cross product).
	 * This method will throw a runtime exception if this domain is not an instance of the stochastic
	 * game domain ({@link burlap.mdp.stochasticgames.SGDomain}). The action will not be added if this domain already has a instance with the same name.
	 * @param sa the {@link SGAgentActionType} that can be executed by an agent in the game.
	 */
	void addSGAgentAction(SGAgentActionType sa);
	
	

	
	/**
	 * Returns a list of the single agent actions that define this domain. Modifying the returned list
	 * will not alter the list of actions that define this domain, because it returns a
	 * shallow copy. Modifying the actions in the returned list will, however, 
	 * modify the actions in this domain. This method will throw a runtime exception
	 * if it is not an instance of the single agent domain (SADomain).
	 * @return a list of the single agent actions that define this domain
	 */
	List <ActionType> getActionTypes();
	
	/**
	 * Returns a list of the stochastic game actions that that can be taken by individual agents in this domain. Modifying the returned list
	 * will not alter the list of actions that define this domain, because it returns a
	 * shallow copy. Modifying the actions in the returned list will, however, 
	 * modify the actions in this domain. This method will throw a runtime exception
	 * if it is not an instance of the stochastic game domain (SGDomain).
	 * @return a list of the stochastic game actions that that can be taken by individual agents in this domain
	 */
	List <SGAgentActionType> getAgentActions();
	
	
	
	/**
	 * Returns the single agent action with the given name. This method will throw a runtime exception
	 * if it is not an instance of the single agent domain (SADomain).
	 * @param name the name of the action to return
	 * @return the action with the given name or null if it does not exist. 
	 */
	ActionType getAction(String name);


	/**
	 * Return the stochastic game action ({@link SGAgentActionType}) with the given name.
	 * This method will throw a runtime exception
	 * if it is not an instance of the stochastic game domain (SGDomain).
	 * @param name the name of the action to return
	 * @return the {@link SGAgentActionType} with the given name or null if it does not exist.
	 */
	SGAgentActionType getSGAgentAction(String name);


}
