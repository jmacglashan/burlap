package burlap.behavior.singleagent.learnfromdemo.mlirl;

import burlap.behavior.policy.BoltzmannQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learnfromdemo.mlirl.support.BoltzmannPolicyGradient;
import burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableRF;
import burlap.behavior.singleagent.learnfromdemo.mlirl.support.QGradientPlanner;
import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.valuefunction.QFunction;
import burlap.datastructures.HashedAggregator;
import burlap.debugtools.DPrint;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.GroundedAction;

import java.util.List;
import java.util.Map;

/**
 * An implementation of Maximum-likelihood Inverse Reinforcement Learning [1]. This class takes as input (from an
 * {@link burlap.behavior.singleagent.learnfromdemo.mlirl.MLIRLRequest} object) a set of expert trajectories
 * through a domain and a {@link burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableRF} model,
 * and learns the parameters of the reward function model that maximizes the likelihood of the trajectories.
 * The reward function parameter spaces is searched using gradient ascent. Since the policy gradient it uses
 * is non-linear, it's possible that it may get stuck in local optimas. Computing the policy gradient is done
 * by iteratively replanning after each gradient ascent step with a {@link burlap.behavior.singleagent.learnfromdemo.mlirl.support.QGradientPlanner}
 * instance provided in the {@link burlap.behavior.singleagent.learnfromdemo.mlirl.MLIRLRequest} object.
 * <p>
 * The gradient ascent will stop either after a fixed number of steps or until the change in likelihood is smaller
 * than some threshold. If the max number of steps is set to -1, then it will continue until the change in likelihood
 * is smaller than the threshold.
 *
 *
 *
 * <p>
 * 1. Babes, Monica, et al. "Apprenticeship learning about multiple intentions." Proceedings of the 28th International Conference on Machine Learning (ICML-11). 2011.
 *
 * @author James MacGlashan.
 */
public class MLIRL {


	/**
	 * The MLRIL request defining the IRL problem.
	 */
	protected MLIRLRequest request;

	/**
	 * The gradient ascent learning rate
	 */
	protected double learningRate;

	/**
	 * The likelihood change threshold to stop gradient ascent.
	 */
	protected double maxLikelihoodChange;

	/**
	 * The maximum number of steps of gradient ascent. when set to -1, there is no limit and termination will be
	 * based on the {@link #maxLikelihoodChange} alone.
	 */
	protected int maxSteps;


	/**
	 * The debug code used for printing information to the terminal.
	 */
	protected int debugCode = 625420;


	/**
	 * Initializes.
	 * @param request the problem request definition
	 * @param learningRate the gradient ascent learning rate
	 * @param maxLikelihoodChange the likelihood change threshold that must be reached to terminate gradient ascent
	 * @param maxSteps the maximum number of gradient ascent steps allowed before termination is forced. Set to -1 to rely only on likelihood threshold.
	 */
	public MLIRL(MLIRLRequest request, double learningRate, double maxLikelihoodChange, int maxSteps){

		this.request = request;
		this.learningRate = learningRate;
		this.maxLikelihoodChange = maxLikelihoodChange;
		this.maxSteps = maxSteps;

		if(!request.isValid()){
			throw new RuntimeException("Provided MLIRLRequest object is not valid.");
		}

	}


	/**
	 * Sets the {@link burlap.behavior.singleagent.learnfromdemo.mlirl.MLIRLRequest} object defining the IRL problem.
	 * @param request the {@link burlap.behavior.singleagent.learnfromdemo.mlirl.MLIRLRequest} object defining the IRL problem.
	 */
	public void setRequest(MLIRLRequest request){
		this.request = request;
	}


	/**
	 * Sets whether information during learning is printed to the terminal. Will automatically toggle the debug printing
	 * for the underlying valueFunction as well.
	 * @param printDebug if true, information is printed to the terminal; if false then it is silent.
	 */
	public void toggleDebugPrinting(boolean printDebug){
		DPrint.toggleCode(this.debugCode, printDebug);
		this.request.getPlanner().toggleDebugPrinting(printDebug);
	}


	/**
	 * Returns the debug code used for printing to the terminal
	 * @return the debug code used for printing to the terminal.
	 */
	public int getDebugCode(){
		return this.debugCode;
	}


	/**
	 * Sets the debug code used for printing to the terminal
	 * @param debugCode the debug code used for printing to the terminal
	 */
	public void setDebugCode(int debugCode){
		this.debugCode = debugCode;
	}


	/**
	 * Runs gradient ascent.
	 */
	public void performIRL(){

		//reset valueFunction
		this.request.getPlanner().resetSolver();
		double lastLikelihood = this.logLikelihood();
		DPrint.cl(this.debugCode, "RF: " + this.request.getRf().toString());
		DPrint.cl(this.debugCode, "Log likelihood: " + lastLikelihood);


		DifferentiableRF rf = this.request.getRf();

		int i;
		for(i = 0; i < maxSteps || this.maxSteps == -1; i++){

			//move up gradient
			FunctionGradient gradient = this.logLikelihoodGradient();
			double maxChange = 0.;
			for(FunctionGradient.PartialDerivative pd : gradient.getNonZeroPartialDerivatives()){
				double curVal = rf.getParameter(pd.parameterId);
				double nexVal = curVal + this.learningRate*pd.value;
				rf.setParameter(pd.parameterId, nexVal);
				double delta = Math.abs(curVal-nexVal);
				maxChange = Math.max(maxChange, delta);
			}


			//reset valueFunction
			this.request.getPlanner().resetSolver();

			double newLikelihood = this.logLikelihood();
			double likelihoodChange = newLikelihood-lastLikelihood;
			lastLikelihood = newLikelihood;


			DPrint.cl(this.debugCode, "RF: " + this.request.getRf().toString());
			DPrint.cl(this.debugCode, "Log likelihood: " + lastLikelihood + " (change: " + likelihoodChange + ")");

			if(Math.abs(likelihoodChange) < this.maxLikelihoodChange){
				i++;
				break;
			}


		}


		DPrint.cl(this.debugCode, "\nNum gradient ascent steps: " + i);
		DPrint.cl(this.debugCode, "RF: " + this.request.getRf().toString());



	}


	/**
	 * Computes and returns the log-likelihood of all expert trajectories under the current reward function parameters.
	 * @return the log-likelihood of all expert trajectories under the current reward function parameters.
	 */
	public double logLikelihood(){

		double [] weights = this.request.getEpisodeWeights();
		List<EpisodeAnalysis> exampleTrajectories = this.request.getExpertEpisodes();

		double sum = 0.;
		for(int i = 0; i < exampleTrajectories.size(); i++){
			sum += this.logLikelihoodOfTrajectory(exampleTrajectories.get(i), weights[i]);
		}

		return sum;

	}


	/**
	 * Computes and returns the log-likelihood of the given trajectory under the current reward function parameters and weights it by the given weight.
	 * @param ea the trajectory
	 * @param weight the weight to assign the trajectory
	 * @return the log-likelihood of the given trajectory under the current reward function parameters and weights it by the given weight.
	 */
	public double logLikelihoodOfTrajectory(EpisodeAnalysis ea, double weight){
		double logLike = 0.;
		Policy p = new BoltzmannQPolicy((QFunction)this.request.getPlanner(), 1./this.request.getBoltzmannBeta());
		for(int i = 0; i < ea.numTimeSteps()-1; i++){
			this.request.getPlanner().planFromState(ea.getState(i));
			double actProb = p.getProbOfAction(ea.getState(i), ea.getAction(i));
			logLike += Math.log(actProb);
		}
		logLike *= weight;
		return logLike;
	}





	/**
	 * Computes and returns the gradient of the log-likelihood of all trajectories
	 * @return the gradient of the log-likelihood of all trajectories
	 */
	public FunctionGradient logLikelihoodGradient(){
		HashedAggregator<Integer> gradientSum = new HashedAggregator<Integer>();

		double [] weights = this.request.getEpisodeWeights();
		List<EpisodeAnalysis> exampleTrajectories = this.request.getExpertEpisodes();

		for(int i = 0; i < exampleTrajectories.size(); i++){
			EpisodeAnalysis ea = exampleTrajectories.get(i);
			double weight = weights[i];
			for(int t = 0; t < ea.numTimeSteps()-1; t++){
				this.request.getPlanner().planFromState(ea.getState(t));
				FunctionGradient policyGrad = this.logPolicyGrad(ea.getState(t), ea.getAction(t));
				//weigh it by trajectory strength
				for(FunctionGradient.PartialDerivative pd : policyGrad.getNonZeroPartialDerivatives()){
					double newVal = pd.value * weight;
					gradientSum.add(pd.parameterId, newVal);
				}

			}
		}

		FunctionGradient gradient = new FunctionGradient.SparseGradient(gradientSum.size());
		for(Map.Entry<Integer, Double> e : gradientSum.entrySet()){
			gradient.put(e.getKey(), e.getValue());
		}

		return gradient;
	}






	/**
	 * Computes and returns the gradient of the Boltzmann policy for the given state and action.
	 * @param s the state in which the policy is queried
	 * @param ga the action for which the policy is queried.
	 * @return s the gradient of the Boltzmann policy for the given state and action.
	 */
	public FunctionGradient logPolicyGrad(State s, GroundedAction ga){

		Policy p = new BoltzmannQPolicy((QFunction)this.request.getPlanner(), 1./this.request.getBoltzmannBeta());
		double invActProb = 1./p.getProbOfAction(s, ga);
		FunctionGradient gradient = BoltzmannPolicyGradient.computeBoltzmannPolicyGradient(s, ga, (QGradientPlanner)this.request.getPlanner(), this.request.getBoltzmannBeta());

		for(FunctionGradient.PartialDerivative pd : gradient.getNonZeroPartialDerivatives()){
			double newVal = pd.value * invActProb;
			gradient.put(pd.parameterId, newVal);
		}

		return gradient;

	}




	/**
	 * Performs a vector addition and stores the results in sumVector
	 * @param sumVector the input vector to which the values in deltaVector will be added.
	 * @param deltaVector the vector values to add to sumVector.
	 */
	protected static void addToVector(double [] sumVector, double [] deltaVector){
		for(int i = 0; i < sumVector.length; i++){
			sumVector[i] += deltaVector[i];
		}
	}


}
