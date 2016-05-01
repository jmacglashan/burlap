package burlap.oomdp.core;


import burlap.debugtools.DPrint;
import burlap.oomdp.core.oo.propositional.PropositionalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;

import java.util.*;


/**
 * This is the base class for a problem domain. A problem domain consists of its state variables, as defined with an
 * OO-MDP ({@link burlap.oomdp.core.Attribute}s, {@link burlap.oomdp.core.ObjectClass}s, and {@link PropositionalFunction}s),
 * and the physics of the world, which are typically specified with some set of action definitions. For single-agent
 * domains, the physics and actions are defined with the {@link burlap.oomdp.singleagent.Action} object and for
 * multi-agent stochastic games, they are defined with {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} and a
 * {@link burlap.oomdp.stochasticgames.JointActionModel}. See the respective single-agent {@link burlap.oomdp.singleagent.SADomain}
 * and stochastic games {@link burlap.oomdp.stochasticgames.SGDomain} subclasses for more information on their definitions.
 * <p>
 * Note that a {@link burlap.oomdp.core.Domain} does *not* include task information, which will be defined separately with
 * a {@link burlap.oomdp.singleagent.RewardFunction} or {@link burlap.oomdp.stochasticgames.JointReward}, and a {@link burlap.oomdp.core.TerminalFunction}.
 * @author James MacGlashan
 */
public interface Domain {

	/**
	 * Add a single agent action that defines this domain. This method
	 * will throw a runtime exception if this domain is not an instance of
	 * the single agent domain (SADomain). The action will not be added if this domain already has a instance with the same name.
	 * @param act the single agent action to add.
	 */
	void addAction(Action act);
	
	/**
	 * Add a {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} that can be executed by an agent in the game.
	 * The set of {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction}s defines the set of joint actions in the stochastic domain (as the cross product).
	 * This method will throw a runtime exception if this domain is not an instance of the stochastic
	 * game domain ({@link burlap.oomdp.stochasticgames.SGDomain}). The action will not be added if this domain already has a instance with the same name.
	 * @param sa the {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} that can be executed by an agent in the game.
	 */
	void addSGAgentAction(SGAgentAction sa);
	
	

	
	/**
	 * Returns a list of the single agent actions that define this domain. Modifying the returned list
	 * will not alter the list of actions that define this domain, because it returns a
	 * shallow copy. Modifying the actions in the returned list will, however, 
	 * modify the actions in this domain. This method will throw a runtime exception
	 * if it is not an instance of the single agent domain (SADomain).
	 * @return a list of the single agent actions that define this domain
	 */
	List <Action> getActions();
	
	/**
	 * Returns a list of the stochastic game actions that that can be taken by individual agents in this domain. Modifying the returned list
	 * will not alter the list of actions that define this domain, because it returns a
	 * shallow copy. Modifying the actions in the returned list will, however, 
	 * modify the actions in this domain. This method will throw a runtime exception
	 * if it is not an instance of the stochastic game domain (SGDomain).
	 * @return a list of the stochastic game actions that that can be taken by individual agents in this domain
	 */
	List <SGAgentAction> getAgentActions();
	
	
	
	/**
	 * Returns the single agent action with the given name. This method will throw a runtime exception
	 * if it is not an instance of the single agent domain (SADomain).
	 * @param name the name of the action to return
	 * @return the action with the given name or null if it does not exist. 
	 */
	Action getAction(String name);


	/**
	 * Return the stochastic game action ({@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction}) with the given name.
	 * This method will throw a runtime exception
	 * if it is not an instance of the stochastic game domain (SGDomain).
	 * @param name the name of the action to return
	 * @return the {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} with the given name or null if it does not exist.
	 */
	SGAgentAction getSGAgentAction(String name);


}
