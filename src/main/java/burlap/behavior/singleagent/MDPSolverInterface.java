package burlap.behavior.singleagent;

import burlap.mdp.core.Domain;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.statehashing.HashableStateFactory;

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
	 * @param gamma the MDP discount factor
	 * @param hashingFactory the hashing factory used to store states (may be set to null if the solver is not tabular)
	 */
	void solverInit(SADomain domain, double gamma, HashableStateFactory hashingFactory);


	/**
	 * This method resets all solver results so that a solver can be restarted fresh
	 * as if had never solved the MDP.
	 */
	void resetSolver();

	/**
	 * Sets the domain of this solver. NOTE: this will also reset the actions this solver uses to the actions of the
	 * provided domain. If you have previously added non-domain referenced actions through the {@link #addActionType(ActionType)}
	 * method, you will have to do so again.
	 * @param domain the domain this solver should use.
	 */
	void setDomain(SADomain domain);


	/**
	 * Sets the model to use for this solver
	 * @param model the model to use
	 */
	void setModel(SampleModel model);


	/**
	 * Returns the model being used by this solver
	 * @return a {@link SampleModel}
	 */
	SampleModel getModel();

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
	void addActionType(ActionType a);

	/**
	 * Sets the action set the solver should use.
	 * @param actionTypes the actions the solver should use.
	 */
	void setActionTypes(List<ActionType> actionTypes);

	/**
	 * Returns a copy of all actions this solver uses for reasoning; including added actions that are not part of the
	 * domain specification (e.g., {@link burlap.behavior.singleagent.options.Option}s). Modifying
	 * the returned list will not modify the action list this solver uses.
	 * @return a {@link java.util.List} of all actions this solver uses.
	 */
	List<ActionType> getActionTypes();


	/**
	 * Sets the {@link burlap.statehashing.HashableStateFactory} used to hash states for tabular solvers.
	 * @param hashingFactory the {@link burlap.statehashing.HashableStateFactory} used to hash states for tabular solvers.
	 */
	void setHashingFactory(HashableStateFactory hashingFactory);

	/**
	 * Returns the {@link burlap.statehashing.HashableStateFactory} this solver uses.
	 * @return the {@link burlap.statehashing.HashableStateFactory} this solver uses.
	 */
	HashableStateFactory getHashingFactory();


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
