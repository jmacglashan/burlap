package burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners;

import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.dpoperator.DifferentiableDPOperator;
import burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.dpoperator.DifferentiableSoftmaxOperator;
import burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableQFunction;
import burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableRF;
import burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableValueFunction;
import burlap.behavior.singleagent.planning.stochastic.DynamicProgramming;
import burlap.behavior.singleagent.planning.stochastic.dpoperator.DPOperator;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.*;

/**
 * A class for performing dynamic programming with a differentiable value backup operator.
 * Specifically, all subclasses are assumed to use a Boltzmann backup operator and the reward functions
 * must be differentiable by subclassing the {@link burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableRF}
 * class. The normal {@link #performBellmanUpdateOn(burlap.statehashing.HashableState)} method
 * of the {@link burlap.behavior.singleagent.planning.stochastic.DynamicProgramming} class is overridden
 * with a method that uses the Boltzmann backup operator.
 * @author James MacGlashan.
 */
public abstract class DifferentiableDP extends DynamicProgramming implements DifferentiableQFunction, DifferentiableValueFunction {

	/**
	 * The value function gradient for each state.
	 */
	protected Map<HashableState, FunctionGradient> valueGradient = new HashMap<HashableState, FunctionGradient>();

	/**
	 * The differentiable RF
	 */
	protected DifferentiableRF 								rf;


	@Override
	public void DPPInit(SADomain domain, double gamma, HashableStateFactory hashingFactory) {
		super.DPPInit(domain, gamma, hashingFactory);
		this.operator = new DifferentiableSoftmaxOperator();
	}

	@Override
	public void resetSolver(){
		super.resetSolver();
		this.valueGradient.clear();
	}

	@Override
	public void setOperator(DPOperator operator) {
		if(!(operator instanceof DifferentiableDPOperator)){
			throw new RuntimeException("DPOperator must be a DifferentiableDPOperator");
		}
		this.operator = operator;
	}

	@Override
	public DifferentiableDPOperator getOperator() {
		return (DifferentiableDPOperator)operator;
	}


	/**
	 * Performs the Boltzmann value function gradient backup for the given {@link burlap.statehashing.HashableState}.
	 * Results are stored in this valueFunction's internal map.
	 * @param sh the hashed state on which to perform the Boltzmann gradient update.
	 * @return the gradient.
	 */
	protected FunctionGradient performDPValueGradientUpdateOn(HashableState sh){


		//get q objects
		List<QValue> Qs = this.qValues(sh.s());
		double [] qs = new double[Qs.size()];
		for(int i = 0; i < Qs.size(); i++){
			qs[i] = Qs.get(i).q;
		}

		FunctionGradient [] qGradients = new FunctionGradient[qs.length];
		for(int i = 0; i < qs.length; i++){
			qGradients[i] = this.qGradient(sh.s(), Qs.get(i).a);
		}

		FunctionGradient vGradient = ((DifferentiableDPOperator)operator).gradient(qs, qGradients);
		this.valueGradient.put(sh, vGradient);

		return vGradient;
	}



	@Override
	public FunctionGradient valueGradient(State s) {
		//returns derivative value
		HashableState sh = this.hashingFactory.hashState(s);
		FunctionGradient grad = this.valueGradient.get(sh);
		if(grad == null){
			grad = new FunctionGradient.SparseGradient();
		}
		return grad;
	}

	@Override
	public FunctionGradient qGradient(State s, Action a){

		FunctionGradient gradient = this.computeQGradient(s, a);
		return  gradient;
	}


	/**
	 * Computes the Q-value gradient for the given {@link State} and {@link Action}.
	 * @param s the state
	 * @param ga the grounded action.
	 * @return the Q-value gradient that was computed.
	 */
	protected FunctionGradient computeQGradient(State s, Action ga){

		FunctionGradient qgradient = new FunctionGradient.SparseGradient();
		List<TransitionProb> tps = ((FullModel)model).transitions(s, ga);
		for(TransitionProb tp : tps){
			FunctionGradient valueGradient = this.valueGradient(tp.eo.op);
			FunctionGradient rewardGradient = this.rf.gradient(s, ga, tp.eo.op);
			Set<Integer> params = combinedNonZeroPDParameters(valueGradient, rewardGradient);

			for(int i : params){
				double curval = qgradient.getPartialDerivative(i);
				double nval = curval + tp.p * (rewardGradient.getPartialDerivative(i) + this.gamma * valueGradient.getPartialDerivative(i));
				qgradient.put(i, nval);
			}


		}

		return qgradient;

	}


	protected Set<Integer> combinedNonZeroPDParameters(FunctionGradient...gradients){

		Set<Integer> c = new HashSet<Integer>();
		for(FunctionGradient g : gradients){
			Set<FunctionGradient.PartialDerivative> p = g.getNonZeroPartialDerivatives();
			for(FunctionGradient.PartialDerivative e : p){
				c.add(e.parameterId);
			}
		}

		return c;
	}


}
