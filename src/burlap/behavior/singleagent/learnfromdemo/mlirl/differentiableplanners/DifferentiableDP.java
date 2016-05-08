package burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners;

import burlap.behavior.singleagent.learnfromdemo.mlirl.support.BoltzmannPolicyGradient;
import burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableRF;
import burlap.behavior.singleagent.learnfromdemo.mlirl.support.QGradientPlanner;
import burlap.behavior.singleagent.learnfromdemo.mlirl.support.QGradientTuple;
import burlap.behavior.singleagent.planning.stochastic.DynamicProgramming;
import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.valuefunction.QValue;
import burlap.datastructures.BoltzmannDistribution;
import burlap.mdp.core.TransitionProbability;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.GroundedAction;
import burlap.mdp.statehashing.HashableState;

import java.util.*;

/**
 * A class for performing dynamic programming with a differentiable value backup operator.
 * Specifically, all subclasses are assumed to use a Boltzmann backup operator and the reward functions
 * must be differentiable by subclassing the {@link burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableRF}
 * class. The normal {@link #performBellmanUpdateOn(burlap.mdp.statehashing.HashableState)} method
 * of the {@link burlap.behavior.singleagent.planning.stochastic.DynamicProgramming} class is overridden
 * with a method that uses the Boltzmann backup operator.
 * @author James MacGlashan.
 */
public abstract class DifferentiableDP extends DynamicProgramming implements QGradientPlanner {

	/**
	 * The value function gradient for each state.
	 */
	protected Map<HashableState, FunctionGradient> valueGradient = new HashMap<HashableState, FunctionGradient>();

	/**
	 * The Boltzmann backup operator beta parameter. The larger the beta, the more deterministic the
	 * back up.
	 */
	protected double										boltzBeta;


	@Override
	public void resetSolver(){
		super.resetSolver();
		this.valueGradient.clear();
	}


	/**
	 * Overrides the superclass method to perform a Boltzmann backup operator
	 * instead of a Bellman backup operator.
	 * Results are stored in this valueFunction's internal map.
	 * @param sh the hashed state on which to perform the Boltzmann update.
	 * @return the new value
	 */
	@Override
	protected double performBellmanUpdateOn(HashableState sh){

		if(this.tf.isTerminal(sh.s)){
			this.valueFunction.put(sh, 0.);
			return 0.;
		}

		List<QValue> qs = this.getQs(sh.s);
		double [] dqs = new double[qs.size()];
		for(int i = 0; i < qs.size(); i++){
			dqs[i] = qs.get(i).q;
		}
		BoltzmannDistribution bd = new BoltzmannDistribution(dqs, 1./this.boltzBeta);
		double [] dist = bd.getProbabilities();

		double sum = 0.;
		for(int i = 0; i < dqs.length; i++){
			sum += dqs[i] * dist[i];
		}

		this.valueFunction.put(sh, sum);

		return sum;
	}

	/**
	 * Performs the Boltzmann value function gradient backup for the given {@link burlap.mdp.statehashing.HashableState}.
	 * Results are stored in this valueFunction's internal map.
	 * @param sh the hashed state on which to perform the Boltzmann gradient update.
	 * @return the gradient.
	 */
	protected FunctionGradient performDPValueGradientUpdateOn(HashableState sh){
		//updates gradient of value function for the given state using bellman-like method


		FunctionGradient vGradient = new FunctionGradient.SparseGradient();
		//get q objects
		List<QValue> Qs = this.getQs(sh.s);
		double [] qs = new double[Qs.size()];
		for(int i = 0; i < Qs.size(); i++){
			qs[i] = Qs.get(i).q;
		}

		FunctionGradient [] qGradients = new FunctionGradient[qs.length];
		for(int i = 0; i < qs.length; i++){
			qGradients[i] = this.getQGradient(sh.s, (GroundedAction)Qs.get(i).a).gradient;
		}

		double maxBetaScaled = BoltzmannPolicyGradient.maxBetaScaled(qs, this.boltzBeta);
		double logSum = BoltzmannPolicyGradient.logSum(qs, maxBetaScaled, this.boltzBeta);

		for(int i = 0; i < qs.length; i++){

			double probA = Math.exp(this.boltzBeta * qs[i] - logSum);
			FunctionGradient policyGradient = BoltzmannPolicyGradient.computePolicyGradient(this.boltzBeta, qs, maxBetaScaled, logSum, qGradients, i);

			for(FunctionGradient.PartialDerivative pd : policyGradient.getNonZeroPartialDerivatives()){
				double curVal = vGradient.getPartialDerivative(pd.parameterId);
				double nextVal = curVal + (probA * qGradients[i].getPartialDerivative(pd.parameterId)) + qs[i] * pd.value;
				vGradient.put(pd.parameterId, nextVal);
			}


		}


		return vGradient;
	}


	/**
	 * Returns the value function gradient for the given {@link State}
	 * @param s the state for which the gradient is be returned.
	 * @return the value function gradient for the given {@link State}
	 */
	public FunctionGradient getValueGradient(State s){
		//returns deriviate value
		HashableState sh = this.hashingFactory.hashState(s);
		FunctionGradient grad = this.valueGradient.get(sh);
		if(grad == null){
			grad = new FunctionGradient.SparseGradient();
		}
		return grad;
	}

	@Override
	public List<QGradientTuple> getAllQGradients(State s){
		List<GroundedAction> gas = this.getAllGroundedActions(s);
		List<QGradientTuple> res = new ArrayList<QGradientTuple>(gas.size());
		for(GroundedAction ga : gas){
			res.add(this.getQGradient(s, ga));
		}
		return res;
	}

	@Override
	public QGradientTuple getQGradient(State s, GroundedAction a){

		FunctionGradient gradient = this.computeQGradient(s, a);
		QGradientTuple tuple = new QGradientTuple(s, a, gradient);
		return tuple;
	}


	@Override
	public void setBoltzmannBetaParameter(double beta) {
		this.boltzBeta = beta;
	}


	/**
	 * Computes the Q-value gradient for the given {@link State} and {@link burlap.mdp.singleagent.GroundedAction}.
	 * @param s the state
	 * @param ga the grounded action.
	 * @return the Q-value gradient that was computed.
	 */
	protected FunctionGradient computeQGradient(State s, GroundedAction ga){

		FunctionGradient qgradient = new FunctionGradient.SparseGradient();
		List<TransitionProbability> tps = ga.getTransitions(s);
		for(TransitionProbability tp : tps){
			FunctionGradient valueGradient = this.getValueGradient(tp.s);
			FunctionGradient rewardGradient = ((DifferentiableRF)this.rf).gradient(s, ga, tp.s);
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
