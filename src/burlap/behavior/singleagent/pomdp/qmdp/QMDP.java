package burlap.behavior.singleagent.pomdp.qmdp;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.Action;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.RewardFunction;
import burlap.mdp.singleagent.pomdp.PODomain;
import burlap.mdp.singleagent.pomdp.beliefstate.BeliefState;
import burlap.mdp.singleagent.pomdp.beliefstate.EnumerableBeliefState;
import burlap.mdp.statehashing.HashableStateFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * An implementation of QMDP for POMDP domains. This is a fast approximation method that has the agent acting
 * as though it would obtain perfect knowledge of the state in the next time step. It works by solving the underling
 * fully observable MDP, and then setting the Q-value for belief states to be the expected fully observable Q-value. Therefore,
 * planning is only as hard as MDP planning. This implementation can take different sources for the MDP QFunction.
 */
public class QMDP extends MDPSolver implements Planner, QFunction {

	/**
	 * The fully observable MDP {@link burlap.behavior.valuefunction.QFunction} source.
	 */
	protected QFunction mdpQSource;



	/**
	 * Initializes.
	 * @param domain the POMDP domain
	 * @param mdpQSource the underlying fully observable MDP {@link burlap.behavior.valuefunction.QFunction} source.
	 */
	public QMDP(PODomain domain, QFunction mdpQSource){
		this.mdpQSource = mdpQSource;
		Planner planner = (Planner)this.mdpQSource;
		this.solverInit(domain, planner.getGamma(), planner.getHashingFactory());
	}


	/**
	 * Initializes and creates a {@link burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration} planner
	 * to solve the underling MDP. You should call the {@link #forceMDPPlanningFromAllStates()} method after construction
	 * to have the constructed {@link burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration} instance
	 * perform planning.
	 * @param domain the POMDP domain
	 * @param rf the POMDP hidden state reward function
	 * @param tf the POMDP hidden state terminal function
	 * @param discount the discount factor
	 * @param hashingFactory the {@link burlap.mdp.statehashing.HashableStateFactory} to use for the {@link burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration} instance to use.
	 * @param maxDelta the maximum value function change threshold that will cause planning to terminiate
	 * @param maxIterations the maximum number of value iteration iterations.
	 */
	public QMDP(PODomain domain, RewardFunction rf, TerminalFunction tf, double discount, HashableStateFactory hashingFactory, double maxDelta, int maxIterations){
		this.domain = domain;
		ValueIteration vi = new ValueIteration(domain, discount, hashingFactory, maxDelta, maxIterations);
		this.mdpQSource = vi;
		this.solverInit(domain, discount, hashingFactory);
	}

	/**
	 * Calls the {@link burlap.behavior.singleagent.planning.Planner#planFromState(State)} method
	 * on all states defined in the POMDP. Calling this method requires that the PODomain provides a {@link burlap.behavior.singleagent.auxiliary.StateEnumerator},
	 * otherwise an exception will be thrown.
	 */
	public void forceMDPPlanningFromAllStates(){

		if(!((PODomain)this.domain).providesStateEnumerator()){
			throw new RuntimeException("QMDP cannot apply method forceMDPPlanningFromAllStates because the domain does not provide a StateEnumerator.");
		}

		Planner planner = (Planner)this.mdpQSource;
		StateEnumerator senum = ((PODomain)this.domain).getStateEnumerator();
		if(senum == null){
			throw new RuntimeException("QMDP cannot plan from all states because the StateEnumerator for the POMDP domain was never specified.");
		}
		for(int i = 0; i < senum.numStatesEnumerated(); i++){
			State s = senum.getStateForEnumerationId(i);
			planner.planFromState(s);
		}
	}
	
	@Override
	public List<QValue> getQs(State s) {

		if(!(s instanceof BeliefState) || !(s instanceof EnumerableBeliefState)){
			throw new RuntimeException("QMDP cannot return the Q-values for the given state, because the given state is not a EnumerableBeliefState instance. It is a " + s.getClass().getName());
		}

		BeliefState bs = (BeliefState)s;

		//get actions for any underlying MDP state
		List<Action> gas = this.getAllGroundedActions(bs.sampleStateFromBelief());
		List<QValue> result = new ArrayList<QValue>(gas.size());

		List<EnumerableBeliefState.StateBelief> beliefs = ((EnumerableBeliefState)bs).getStatesAndBeliefsWithNonZeroProbability();

		for(Action ga : gas){
			double q = this.qForBeliefList(beliefs, ga);
			QValue Q = new QValue(s, ga, q);
			result.add(Q);
		}
		
		return result;
	}

	@Override
	public QValue getQ(State s, Action a) {

		if(!(s instanceof BeliefState) || !(s instanceof EnumerableBeliefState)){
			throw new RuntimeException("QMDP cannot return the Q-values for the given state, because the given state is not a EnumerableBeliefState instance. It is a " + s.getClass().getName());
		}

		EnumerableBeliefState bs = (EnumerableBeliefState)s;


		QValue q = new QValue(s, a, this.qForBelief(bs, a));
		
		return q;
	}

	@Override
	public double value(State s) {
		return QFunction.QFunctionHelper.getOptimalValue(this, s);
	}

	/**
	 * Computes the expected Q-value of the underlying hidden MDP by marginalizing over of the states in the belief state.
	 * @param bs the belief state
	 * @param ga the action whose Q-value is to be computed
	 * @return the expected Q-value of the underlying hidden MDP by marginalizing over of the states in the belief state.
	 */
	public double qForBelief(EnumerableBeliefState bs, Action ga){
		
		List<EnumerableBeliefState.StateBelief> beliefs = bs.getStatesAndBeliefsWithNonZeroProbability();
		return this.qForBeliefList(beliefs, ga);

	}


	/**
	 * Computes the expected Q-value of the underlying hidden MDP by marginalizing over of the states in the belief state.
	 * @param beliefs belief state distribution
	 * @param ga the action whose Q-value is to be computed
	 * @return the expected Q-value of the underlying hidden MDP by marginalizing over of the states in the belief state.
	 */
	protected double qForBeliefList(List<EnumerableBeliefState.StateBelief> beliefs, Action ga){
		double q = 0.;
		for(EnumerableBeliefState.StateBelief sb : beliefs){
			q += sb.belief * this.mdpQSource.getQ(sb.s, ga).q;
		}
		return q;
	}

	@Override
	public Policy planFromState(State initialState) {
		this.forceMDPPlanningFromAllStates();
		return new GreedyQPolicy(this);
	}

	@Override
	public void resetSolver() {
		((Planner)this.mdpQSource).resetSolver();
	}





}
