package burlap.behavior.singleagent.learnbydemo.mlirl;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learnbydemo.mlirl.support.DifferentiableRF;
import burlap.behavior.singleagent.learnbydemo.mlirl.support.QGradientPlannerFactory;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * An implementation of Multiple Intentions Maximum-likelihood Inverse Reinforcement Learning [1]. This algorithm
 * takes as input a set of expert trajectories, a number of clusters, and a differentiable reward function model; and
 * clusters the trajectories assigning each cluster its own reward function parameter values. The algorithm uses
 * EM to find the reward function parameter values for each cluster and uses {@link burlap.behavior.singleagent.learnbydemo.mlirl.MLIRL}
 * to perform the maximization step of the parameter values. EM is run for a specified number of iterations.
 * <p/>
 * At initialization, the reward function parameters for each behavior cluster will be randomly assigned values between
 * -1 and 1. If you want to change this behavior, subclass this object and override the
 * {@link #initializeClusterRFParameters(java.util.List)} method.
 *
 * <p/>
 * 1. Babes, Monica, et al. "Apprenticeship learning about multiple intentions." Proceedings of the 28th International Conference on Machine Learning (ICML-11). 2011.
 *
 * @author James MacGlashan; code is modeled from code written by Lei Yang.
 */
public class MultipleIntentionsMLIRL {

	/**
	 * The source problem request defining the problem to be solved.
	 */
	protected MultipleIntentionsMLIRLRequest request;

	/**
	 * The invididual {@link burlap.behavior.singleagent.learnbydemo.mlirl.MLIRLRequest} objects for each behavior cluster.
	 */
	protected List<MLIRLRequest> clusterRequests;

	/**
	 * The prior probabilities on each cluster.
	 */
	protected double [] clusterPriors;


	/**
	 * The {@link burlap.behavior.singleagent.learnbydemo.mlirl.MLIRL} instance used to perform the maximization step
	 * for each clusters reward function parameter values.
	 */
	protected MLIRL mlirlInstance;

	/**
	 * The number of EM iterations to run.
	 */
	protected int numEMIterations;


	/**
	 * The debug code used for printing information to the terminal.
	 */
	protected int debugCode = 13435;


	/**
	 * A random object used for initializing each cluster's RF parameters randomly.
	 */
	protected Random rand = RandomFactory.getMapped(0);


	/**
	 * Initializes. Reward function parameters for each cluster will be initialized randomly between -1 and 1.
	 * @param request the request that defines the problem.
	 * @param emIterations the number of EM iterations to perform.
	 * @param mlIRLLearningRate the learning rate of the underlying {@link burlap.behavior.singleagent.learnbydemo.mlirl.MLIRL} instance.
	 * @param maxMLIRLLikelihoodChange the likelihood change threshold that causes {@link burlap.behavior.singleagent.learnbydemo.mlirl.MLIRL} gradient ascent to stop.
	 * @param maxMLIRLSteps the maximum number of gradient ascent steps allowd by the underlying {@link burlap.behavior.singleagent.learnbydemo.mlirl.MLIRLRequest} gradient ascent.
	 */
	public MultipleIntentionsMLIRL(MultipleIntentionsMLIRLRequest request,
								   int emIterations, double mlIRLLearningRate, double maxMLIRLLikelihoodChange, int maxMLIRLSteps){

		if(!request.isValid()){
			throw new RuntimeException("Provided MultipleIntentionsMLIRLRequest object is not valid.");
		}

		this.request = request;
		this.initializeClusters(this.request.getK(), this.request.getPlannerFactory());

		this.numEMIterations = emIterations;
		this.mlirlInstance = new MLIRL(request, mlIRLLearningRate, maxMLIRLLikelihoodChange, maxMLIRLSteps);


	}


	/**
	 * Performs multiple intention inverse reinforcement learning.
	 */
	public void performIRL(){

		int k = this.clusterPriors.length;

		for(int i = 0; i < this.numEMIterations; i++){

			DPrint.cl(this.debugCode, "Starting EM iteration " + (i+1) + "/" + this.numEMIterations);

			double [][] trajectoryPerClusterWeights = this.computePerClusterMLIRLWeights();
			for(int j = 0; j < k; j++){
				MLIRLRequest clusterRequest = this.clusterRequests.get(j);
				clusterRequest.setEpisodeWeights(trajectoryPerClusterWeights[j].clone());
				this.mlirlInstance.setRequest(clusterRequest);
				this.mlirlInstance.performIRL();
			}


		}

		DPrint.cl(this.debugCode, "Finished EM");

	}


	/**
	 * Returns the probability of each behavior cluster given the trajectory.
	 * @param t the trajectory (stored as an {@link burlap.behavior.singleagent.EpisodeAnalysis} object) to evaluate.
	 * @return the probability of each behavior cluster given the trajectory.
	 */
	public double [] computeProbabilityOfClustersGivenTrajectory(EpisodeAnalysis t){

		int k = this.clusterPriors.length;
		double [] probs = new double[k];

		//compute the log prior weighted likelihood terms and find max
		double mx = Double.NEGATIVE_INFINITY;
		for(int i = 0; i < k; i++){
			double logPrior = Math.log(this.clusterPriors[i]);

			//set the IRL request for the current cluster
			this.mlirlInstance.setRequest(this.clusterRequests.get(i));
			double logTrajectory = this.mlirlInstance.logLikelihoodOfTrajectory(t, 1.);
			double v = logTrajectory + logPrior;
			probs[i] = v;
			mx = Math.max(mx, v);
		}


		//compute logged denominator value
		double exponetiatedSum = 0.;
		for(int i = 0; i < k; i++){
			double v = probs[i] - mx;
			double expVal = Math.exp(v);
			exponetiatedSum += expVal;
		}
		double logSum = Math.log(exponetiatedSum);
		double finalSum = mx + logSum;

		//now store as final probabilities
		for(int i = 0; i < k; i++){
			double v = probs[i];
			double logProb = v - finalSum;
			double prob = Math.exp(logProb);
			probs[i] = prob;
		}

		return probs;
	}


	/**
	 * Returns the {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.DifferentiableRF} obejcts defining each behavior cluster.
	 * @return the {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.DifferentiableRF} obejcts defining each behavior cluster.
	 */
	public List<DifferentiableRF> getClusterRFs(){
		List<DifferentiableRF> rfs = new ArrayList<DifferentiableRF>(this.clusterPriors.length);
		for(MLIRLRequest request : this.clusterRequests){
			rfs.add(request.getRf());
		}
		return rfs;
	}


	/**
	 * Returns the behavior cluster prior probabilities.
	 * @return the behavior cluster prior probabilities.
	 */
	public double [] getClusterPriors(){
		return this.clusterPriors;
	}







	/**
	 * Sets whether information during learning is printed to the terminal. Will automatically toggle the debug printing
	 * for the underlying MLIRL that runs.
	 * @param printDebug if true, information is printed to the terminal; if false then it is silent.
	 */
	public void toggleDebugPrinting(boolean printDebug){
		DPrint.toggleCode(this.debugCode, printDebug);
		this.mlirlInstance.toggleDebugPrinting(printDebug);
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
	 * Computes the probability of each trajectory being generated by each cluster and returns it in a matrix. The prior probability
	 * of each cluster prior is also updated to maximize these values. The returned matrix has clusters
	 * along the rows and trajectories along the columns. These values are used to weight the contribution
	 * of each trajectory for the MLIRL performed to maxmize each cluster RF parameters.
	 * @return the probability of each trajectory being generated by each cluster
	 */
	protected double [][] computePerClusterMLIRLWeights(){

		int k = this.clusterPriors.length;
		int n = this.request.getExpertEpisodes().size();

		double [][] newWeights = new double[k][n];

		//first do pass computing log prior weighted likelihood of each trajectory
		for(int i = 0; i < k; i++){
			double logPrior = Math.log(this.clusterPriors[i]);

			//set the IRL request for the current cluster
			this.mlirlInstance.setRequest(this.clusterRequests.get(i));

			//compute the trajectory log-likelihoods and add them in
			for(int j = 0; j < n; j++){
				double trajectLogLikelihood = this.mlirlInstance.logLikelihoodOfTrajectory(
						this.request.getExpertEpisodes().get(j), 1.);

				double val = logPrior + trajectLogLikelihood;
				newWeights[i][j] = val;
			}
		}

		//now pass through normalizing in log space, and then exponentiate to get back probability
		//also maintain sum of entire matrix to normalize for new cluster priors
		double matrixSum = 0.;
		for(int j = 0; j < n; j++){
			double columnDenom = this.computeClusterTrajectoryLoggedNormalization(j, newWeights);
			for(int i = 0; i < k; i++){
				double logProb = newWeights[i][j] - columnDenom;
				double prob = Math.exp(logProb);
				newWeights[i][j] = prob;
				matrixSum += prob;
			}
		}

		//finally compute new cluster priors
		for(int i = 0; i < k; i++){
			double clusterSum = 0.;
			for(int j = 0; j < n; j++){
				clusterSum += newWeights[i][j];
			}
			double nPrior = clusterSum / matrixSum;
			this.clusterPriors[i] = nPrior;
		}

		return newWeights;
	}


	/**
	 * Given a matrix holding the log[Pr(c)] + log(Pr(t | c)] values in its entries, where
	 * Pr(c) is the probability of the cluster and Pr(t | c)] is the probability of the trajectory given the cluster,
	 * this method returns the log probability of the standard probability normalization factor for trajectory t in
	 * the matrix. That is,
	 * it returns log [ \sum_i Pr(c_i) * Pr(t | c_i) ].
	 * The matrix is ordered such that the rows are cluster indices and columns are trajectories.
	 * @param t the trajectory in question.
	 * @param logWeightedLikelihoods the matrix of log[Pr(c)] + log(Pr(t | c)] values.
	 * @return log [ \sum_i Pr(c_i) * Pr(t | c_i) ]
	 */
	protected double computeClusterTrajectoryLoggedNormalization(int t, double [][] logWeightedLikelihoods){

		double mx = Double.NEGATIVE_INFINITY;
		int k = logWeightedLikelihoods.length;

		//first find max term
		for(int i = 0; i < k; i++){
			mx = Math.max(mx, logWeightedLikelihoods[i][t]);
		}

		//now get sum of exponentials shifted by max
		double sum = 0.;
		for(int i = 0; i < k; i++){
			double v = logWeightedLikelihoods[i][t];
			double shifted = v - mx;
			double exponentiated = Math.exp(shifted);
			sum += exponentiated;
		}

		double logSum = Math.log(sum);
		double finalSum = mx + logSum;

		return finalSum;
	}


	/**
	 * Initializes cluster data; i.e., it initializes RF parameters, cluster prior parameters (to uniform), and creates {@link burlap.behavior.singleagent.learnbydemo.mlirl.MLIRLRequest}
	 * objects for each cluster.
	 * @param k the number of clusters
	 * @param plannerFactory the {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.QGradientPlannerFactory} to use to generate a planner for each cluster.
	 */
	protected void initializeClusters(int k, QGradientPlannerFactory plannerFactory){

		List<DifferentiableRF> rfs = new ArrayList<DifferentiableRF>(k);
		for(int i = 0; i < k; i++){
			rfs.add(this.request.getRf().copy());
		}

		this.initializeClusterRFParameters(rfs);

		this.clusterRequests = new ArrayList<MLIRLRequest>(k);
		this.clusterPriors = new double[k];
		double uni = 1./(double)k;
		for(int i = 0; i < k; i++){
			this.clusterPriors[i] = uni;
			MLIRLRequest nRequest = new MLIRLRequest(this.request.getDomain(),null,
					this.request.getExpertEpisodes(),rfs.get(i));

			nRequest.setGamma(this.request.getGamma());
			nRequest.setBoltzmannBeta(this.request.getBoltzmannBeta());
			nRequest.setPlanner((OOMDPPlanner)plannerFactory.generateDifferentiablePlannerForRequest(nRequest));

			this.clusterRequests.add(nRequest);

		}



	}


	/**
	 * Initializes the {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.DifferentiableRF} parameters
	 * for each cluster. Will set the parameters randomly between -1 and 1.
	 * @param rfs the {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.DifferentiableRF} whose parameters are to be initialized.
	 */
	protected void initializeClusterRFParameters(List<DifferentiableRF> rfs){
		for(DifferentiableRF rf : rfs){
			double [] params = rf.getParameters();
			this.randomizeParameters(params);
		}
	}

	/**
	 * Randomizes parameters in the given vector between -1 and 1.
	 * @param paramVec the parameter vector to randomize.
	 */
	protected void randomizeParameters(double [] paramVec){
		for(int i = 0; i < paramVec.length; i++){
			double r = this.rand.nextDouble()*2 - 1.;
			paramVec[i] = r;
		}
	}


}
