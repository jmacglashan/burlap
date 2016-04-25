package burlap.behavior.singleagent;

import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.RewardFunction;

import java.util.List;

/**
 * The top-level interface for algorithms that solve MDPs. In general, classes should probably subclass the {@link burlap.behavior.singleagent.MDPSolver}
 * abstract class instead of using this interface directly, since the {@link burlap.behavior.singleagent.MDPSolver} will provide
 * relevant data members, implement these methods, and provide additional helper methods.
 * @author James MacGlashan.
 */
public interface MDPSolverInterface {


	/**
	 * Initializes the solver with the common elements.
	 * @param domain the domain to be solved.
	 * @param rf the reward function
	 * @param tf the terminal state function
	 * @param gamma the MDP discount factor
	 * @param hashingFactory the hashing factory used to store states (may be set to null if the solver is not tabular)
	 */
	void solverInit(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, HashableStateFactory hashingFactory);


	/**
	 * This method resets all solver results so that a solver can be restarted fresh
	 * as if had never solved the MDP.
	 */
	void resetSolver();

	/**
	 * Sets the domain of this solver. NOTE: this will also reset the actions this solver uses to the actions of the
	 * provided domain. If you have previously added non-domain referenced actions through the {@link #addNonDomainReferencedAction(burlap.oomdp.singleagent.Action)}
	 * method, you will have to do so again.
	 * @param domain the domain this solver should use.
	 */
	void setDomain(Domain domain);


	/**
	 * Returns the {@link Domain} this solver solves.
	 * @return the {@link Domain} this solver solves.
	 */
	Domain getDomain();

	/**
	 * Adds an additional action the solver that is not included in the domain definition. For instance, an {@link burlap.behavior.singleagent.options.Option}
	 * should be added using this method.
	 * @param a the action to add to the solver
	 */
	void addNonDomainReferencedAction(Action a);

	/**
	 * Sets the action set the solver should use.
	 * @param actions the actions the solver should use.
	 */
	void setActions(List<Action> actions);

	/**
	 * Returns a copy of all actions this solver uses for reasoning; including added actions that are not part of the
	 * domain specification (e.g., {@link burlap.behavior.singleagent.options.Option}s). Modifying
	 * the returned list will not modify the action list this solver uses.
	 * @return a {@link java.util.List} of all actions this solver uses.
	 */
	List<Action> getActions();


	/**
	 * Sets the {@link burlap.oomdp.statehashing.HashableStateFactory} used to hash states for tabular solvers.
	 * @param hashingFactory the {@link burlap.oomdp.statehashing.HashableStateFactory} used to hash states for tabular solvers.
	 */
	void setHashingFactory(HashableStateFactory hashingFactory);

	/**
	 * Returns the {@link burlap.oomdp.statehashing.HashableStateFactory} this solver uses.
	 * @return the {@link burlap.oomdp.statehashing.HashableStateFactory} this solver uses.
	 */
	HashableStateFactory getHashingFactory();


	/**
	 * Sets the reward function used by this solver
	 * @param rf the reward function to be used by this solver
	 */
	void setRf(RewardFunction rf);


	/**
	 * Returns the {@link burlap.oomdp.singleagent.RewardFunction} this solver uses.
	 * @return the {@link burlap.oomdp.singleagent.RewardFunction} this solver uses.
	 */
	RewardFunction getRf();

	/**
	 * Sets the terminal state function used by this solver
	 * @param tf the terminal function to be used by this solver
	 */
	void setTf(TerminalFunction tf);


	/**
	 * Returns the {@link burlap.oomdp.core.TerminalFunction} this solver uses.
	 * @return the {@link burlap.oomdp.core.TerminalFunction} this solver uses.
	 */
	TerminalFunction getTf();

	/**
	 * Returns gamma, the discount factor used by this solver
	 * @return gamma, the discount factor used by this solver
	 */
	double getGamma();


	/**
	 * Sets gamma, the discount factor used by this solver
	 * @param gamma the discount factor used by this solver
	 */
	void setGamma(double gamma);


	/**
	 * Sets the debug code to be used by calls to {@link burlap.debugtools.DPrint}
	 * @param code the code to be used by {@link burlap.debugtools.DPrint}
	 */
	void setDebugCode(int code);


	/**
	 * Returns the debug code used by this solver for calls to {@link burlap.debugtools.DPrint}
	 * @return the debug code used by this solver for calls to {@link burlap.debugtools.DPrint}
	 */
	int getDebugCode();


	/**
	 * Toggles whether the solver's calls to {@link burlap.debugtools.DPrint} should be printed.
	 * @param toggle whether to print the calls to {@link burlap.debugtools.DPrint}
	 */
	void toggleDebugPrinting(boolean toggle);


}
