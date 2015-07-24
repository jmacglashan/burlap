package burlap.behavior.singleagent.learning.lspi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import burlap.oomdp.singleagent.environment.Environment;
import org.ejml.simple.SimpleMatrix;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.lspi.SARSCollector.UniformRandomSARSCollector;
import burlap.behavior.singleagent.learning.lspi.SARSData.SARS;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QFunction;
import burlap.behavior.singleagent.planning.commonpolicies.EpsilonGreedy;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.vfa.ActionApproximationResult;
import burlap.behavior.singleagent.vfa.ActionFeaturesQuery;
import burlap.behavior.singleagent.vfa.FeatureDatabase;
import burlap.behavior.singleagent.vfa.StateFeature;
import burlap.behavior.singleagent.vfa.ValueFunctionApproximation;
import burlap.behavior.singleagent.vfa.common.LinearVFA;
import burlap.debugtools.DPrint;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * This class implements the optimized version of last squares policy iteration [1] (runs in quadratic time of the number of state features). Unlike other planning and learning algorithms,
 * it is reccomended that you use this class differently than the conventional ways. That is, rather than using the {@link #planFromState(State)} or {@link #runLearningEpisodeFrom(State)}
 * methods, you should instead use a {@link SARSCollector} object to gather a bunch of example state-action-reward-state tuples that are then used for policy iteration. You can
 * set the dataset to use using the {@link #setDataset(SARSData)} method and then you can run LSPI on it using the {@link #runPolicyIteration(int, double)} method. LSPI requires
 * initializing a matrix to an identity matrix multiplied by some large positive constant (see the reference for more information). 
 * By default this constant is 100, but you can change it with the {@link #setIdentityScalar(double)}
 * method.
 * <p/>
 * If you do use the {@link #planFromState(State)} method, it will work by creating a {@link UniformRandomSARSCollector} and collecting SARS data from the input state and then calling
 * the {@link #runPolicyIteration(int, double)} method. You can change the {@link SARSCollector} this method uses, the number of samples it acquires, the maximum weight change for PI termination,
 * and the maximum number of policy iterations by using the {@link #setPlanningCollector(SARSCollector)}, {@link #setNumSamplesForPlanning(int)}, {@link #setMaxChange(double)}, and
 * {@link #setMaxNumPlanningIterations(int)} methods repsectively.
 * <p/>
 * If you do use the {@link #runLearningEpisodeFrom(State)} method (or the {@link #runLearningEpisodeFrom(State, int)} method), it will work by following a learning policy for the episode and adding its observations to its dataset for its
 * policy iteration. After enough new data has been acquired, policy iteration will be rereun. You can adjust the learning policy, the maximum number of allowed learning steps in an
 * episode, and the minimum number of new observations until LSPI is rerun using the {@link #setLearningPolicy(Policy)}, {@link #setMaxLearningSteps(int)}, {@link #setMinNewStepsForLearningPI(int)}
 * methods respectively. The LSPI  termination parameters are set using the same methods that you use for adjusting the results from the {@link #planFromState(State)} method discussed above.
 * <p/>
 * This data gathering and replanning behavior from learning episodes is not expected to be an especailly good choice. Therefore, if you want a better online data acquisition, you should consider subclassing this class
 * and overriding the methods {@link #updateDatasetWithLearningEpisode(EpisodeAnalysis)} and {@link #shouldRereunPolicyIteration(EpisodeAnalysis)}, or the {@link #runLearningEpisodeFrom(State, int)} method
 * itself.
 * 
 * <p/>
 * 1. Lagoudakis, Michail G., and Ronald Parr. "Least-squares policy iteration." The Journal of Machine Learning Research 4 (2003): 1107-1149.
 * 
 * @author James MacGlashan
 *
 */
public class LSPI extends OOMDPPlanner implements QFunction, LearningAgent {

	/**
	 * The object that performs value function approximation given the weights that are estimated
	 */
	protected ValueFunctionApproximation							vfa;
	
	/**
	 * The SARS dataset on which LSPI is performed
	 */
	protected SARSData												dataset;
	
	/**
	 * The state feature database on which the linear VFA is performed
	 */
	protected FeatureDatabase										featureDatabase;
	
	
	/**
	 * The initial LSPI identity matrix scalar; default is 100.
	 */
	protected double												identityScalar = 100.;
	
	/**
	 * The last weight values set from LSTDQ
	 */
	protected SimpleMatrix											lastWeights;
	
	/**
	 * the number of samples that are acquired for this object's dataset when the {@link #planFromState(State)} method is called.
	 */
	protected int													numSamplesForPlanning = 10000;
	
	/**
	 * The maximum change in weights permitted to terminate LSPI. Default is 1e-6.
	 */
	protected double												maxChange = 1e-6;
	
	/**
	 * The data collector used by the {@link #planFromState(State)} method.
	 */
	protected SARSCollector											planningCollector;
	
	/**
	 * The maximum number of policy iterations permitted when LSPI is run from the {@link #planFromState(State)} or {@link #runLearningEpisodeFrom(State)} methods.
	 */
	protected int													maxNumPlanningIterations = 30;
	
	
	/**
	 * The learning policy followed in {@link #runLearningEpisodeFrom(State)} method calls. Default is 0.1 epsilon greedy.
	 */
	protected Policy												learningPolicy;
	
	/**
	 * The maximum number of learning steps in an episode when the {@link #runLearningEpisodeFrom(State)} method is called. Default is INT_MAX.
	 */
	protected int													maxLearningSteps = Integer.MAX_VALUE;
	
	/**
	 * Number of new observations received from learning episodes since LSPI was run
	 */
	protected int													numStepsSinceLastLearningPI = 0;
	
	/**
	 * The minimum number of new observations received from learning episodes before LSPI will be run again.
	 */
	protected int													minNewStepsForLearningPI = 100;
	
	
	/**
	 * the saved previous learning episodes
	 */
	protected LinkedList<EpisodeAnalysis>							episodeHistory = new LinkedList<EpisodeAnalysis>();
	
	/**
	 * The number of the most recent learning episodes to store.
	 */
	protected int													numEpisodesToStore;
	
	
	
	
	
	
	/**
	 * Initializes for the given domain, reward function, terminal state function, discount factor and the feature database that provides the state features used by LSPI.
	 * @param domain the problem domain
	 * @param rf the reward function
	 * @param tf the terminal state function
	 * @param gamma the discount factor
	 * @param fd the feature database defining state features on which LSPI will run.
	 */
	public LSPI(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, FeatureDatabase fd){
		this.plannerInit(domain, rf, tf, gamma, null);
		this.featureDatabase = fd;
		this.vfa = new LinearVFA(this.featureDatabase);
		this.learningPolicy = new EpsilonGreedy(this, 0.1);
	}
	
	
	/**
	 * Sets the SARS dataset this object will use for LSPI
	 * @param dataset the SARSA dataset
	 */
	public void setDataset(SARSData dataset){
		this.dataset = dataset;
	}
	
	/**
	 * Returns the dataset this object uses for LSPI
	 * @return the dataset this object uses for LSPI
	 */
	public SARSData getDataset(){
		return this.dataset;
	}
	
	
	/**
	 * Returns the feature database defining state features
	 * @return the feature database defining state features
	 */
	public FeatureDatabase getFeatureDatabase() {
		return featureDatabase;
	}

	/**
	 * Sets the feature datbase defining state features
	 * @param featureDatabase the feature database defining state features
	 */
	public void setFeatureDatabase(FeatureDatabase featureDatabase) {
		this.featureDatabase = featureDatabase;
	}

	
	/**
	 * Returns the initial LSPI identity matrix scalar used
	 * @return the initial LSPI identity matrix scalar used
	 */
	public double getIdentityScalar() {
		return identityScalar;
	}

	/**
	 * Sets the initial LSPI identity matrix scalar used.
	 * @param identityScalar the initial LSPI identity matrix scalar used.
	 */
	public void setIdentityScalar(double identityScalar) {
		this.identityScalar = identityScalar;
	}

	
	/**
	 * Gets the number of SARS samples that will be gathered by the {@link #planFromState(State)} method.
	 * @return the number of SARS samples that will be gathered by the {@link #planFromState(State)} method.
	 */
	public int getNumSamplesForPlanning() {
		return numSamplesForPlanning;
	}

	/**
	 * Sets the number of SARS samples that will be gathered by the {@link #planFromState(State)} method.
	 * @param numSamplesForPlanning the number of SARS samples that will be gathered by the {@link #planFromState(State)} method.
	 */
	public void setNumSamplesForPlanning(int numSamplesForPlanning) {
		this.numSamplesForPlanning = numSamplesForPlanning;
	}

	/**
	 * Gets the {@link SARSCollector} used by the {@link #planFromState(State)} method for collecting data.
	 * @return the {@link SARSCollector} used by the {@link #planFromState(State)} method for collecting data.
	 */
	public SARSCollector getPlanningCollector() {
		return planningCollector;
	}

	
	/**
	 * Sets the {@link SARSCollector} used by the {@link #planFromState(State)} method for collecting data.
	 * @param planningCollector the {@link SARSCollector} used by the {@link #planFromState(State)} method for collecting data.
	 */
	public void setPlanningCollector(SARSCollector planningCollector) {
		this.planningCollector = planningCollector;
	}

	
	/**
	 * The maximum number of policy iterations that will be used by the {@link #planFromState(State)} method.
	 * @return the maximum number of policy iterations that will be used by the {@link #planFromState(State)} method.
	 */
	public int getMaxNumPlanningIterations() {
		return maxNumPlanningIterations;
	}

	/**
	 * Sets the maximum number of policy iterations that will be used by the {@link #planFromState(State)} method.
	 * @param maxNumPlanningIterations the maximum number of policy iterations that will be used by the {@link #planFromState(State)} method.
	 */
	public void setMaxNumPlanningIterations(int maxNumPlanningIterations) {
		this.maxNumPlanningIterations = maxNumPlanningIterations;
	}

	
	/**
	 * The learning policy followed by the {@link #runLearningEpisodeFrom(State)} and {@link #runLearningEpisodeFrom(State, int)} methods.
	 * @return learning policy followed by the {@link #runLearningEpisodeFrom(State)} and {@link #runLearningEpisodeFrom(State, int)} methods.
	 */
	public Policy getLearningPolicy() {
		return learningPolicy;
	}

	/**
	 * Sets the learning policy followed by the {@link #runLearningEpisodeFrom(State)} and {@link #runLearningEpisodeFrom(State, int)} methods.
	 * @param learningPolicy the learning policy followed by the {@link #runLearningEpisodeFrom(State)} and {@link #runLearningEpisodeFrom(State, int)} methods.
	 */
	public void setLearningPolicy(Policy learningPolicy) {
		this.learningPolicy = learningPolicy;
	}

	
	/**
	 * The maximum number of learning steps permitted by the {@link #runLearningEpisodeFrom(State)} method.
	 * @return maximum number of learning steps permitted by the {@link #runLearningEpisodeFrom(State)} method.
	 */
	public int getMaxLearningSteps() {
		return maxLearningSteps;
	}

	/**
	 * Sets the maximum number of learning steps permitted by the {@link #runLearningEpisodeFrom(State)} method.
	 * @param maxLearningSteps the maximum number of learning steps permitted by the {@link #runLearningEpisodeFrom(State)} method.
	 */
	public void setMaxLearningSteps(int maxLearningSteps) {
		this.maxLearningSteps = maxLearningSteps;
	}

	/**
	 * The minimum number of new learning observations before policy iteration is run again.
	 * @return the minimum number of new learning observations before policy iteration is run again.
	 */
	public int getMinNewStepsForLearningPI() {
		return minNewStepsForLearningPI;
	}

	/**
	 * Sets the minimum number of new learning observations before policy iteration is run again.
	 * @param minNewStepsForLearningPI the minimum number of new learning observations before policy iteration is run again.
	 */
	public void setMinNewStepsForLearningPI(int minNewStepsForLearningPI) {
		this.minNewStepsForLearningPI = minNewStepsForLearningPI;
	}
	
	

	/**
	 * The maximum change in weights required to terminate policy iteration when called from the {@link #planFromState(State)}, {@link #runLearningEpisodeFrom(State)} or {@link #runLearningEpisodeFrom(State, int)} methods.
	 * @return the maximum change in weights required to terminate policy iteration when called from the {@link #planFromState(State)}, {@link #runLearningEpisodeFrom(State)} or {@link #runLearningEpisodeFrom(State, int)} methods.
	 */
	public double getMaxChange() {
		return maxChange;
	}

	/**
	 * Sets the maximum change in weights required to terminate policy iteration when called from the {@link #planFromState(State)}, {@link #runLearningEpisodeFrom(State)} or {@link #runLearningEpisodeFrom(State, int)} methods.
	 * @param maxChange the maximum change in weights required to terminate policy iteration when called from the {@link #planFromState(State)}, {@link #runLearningEpisodeFrom(State)} or {@link #runLearningEpisodeFrom(State, int)} methods.
	 */
	public void setMaxChange(double maxChange) {
		this.maxChange = maxChange;
	}

	
	/**
	 * Runs LSTDQ on this object's current {@link SARSData} dataset.
	 * @return the new weight matrix as a {@link SimpleMatrix} object.
	 */
	public SimpleMatrix LSTDQ(){
		
		//set our policy
		Policy p = new GreedyQPolicy(this);
		
		//first we want to get all the features for all of our states in our data set; this is important if our feature database generates new features on the fly
		//and will also restrict our focus to only the action features that we want
		List<SSFeatures> features = new ArrayList<LSPI.SSFeatures>(this.dataset.size());
		for(SARS sars : this.dataset.dataset){
			features.add(new SSFeatures(this.featureDatabase.getActionFeaturesSets(sars.s, this.gaListWrapper(sars.a)), 
					this.featureDatabase.getActionFeaturesSets(sars.sp, this.gaListWrapper(p.getAction(sars.sp)))));
		}
		
		int nf = this.featureDatabase.numberOfFeatures();
		SimpleMatrix B = SimpleMatrix.identity(nf).scale(this.identityScalar);
		SimpleMatrix b = new SimpleMatrix(nf, 1);
		
		
		
		for(int i = 0; i < features.size(); i++){

			SimpleMatrix phi = this.phiConstructor(features.get(i).sActionFeatures, nf);
			SimpleMatrix phiPrime = this.phiConstructor(features.get(i).sPrimeActionFeatures, nf);
			double r = this.dataset.get(i).r;
			

			SimpleMatrix numerator = B.mult(phi).mult(phi.minus(phiPrime.scale(gamma)).transpose()).mult(B);
			SimpleMatrix denomenatorM = phi.minus(phiPrime.scale(this.gamma)).transpose().mult(B).mult(phi);
			double denomenator = denomenatorM.get(0) + 1;
			
			B = B.minus(numerator.scale(1./denomenator));
			b = b.plus(phi.scale(r));
			
			//DPrint.cl(0, "updated matrix for row " + i + "/" + features.size());
			
		}
		
		
		SimpleMatrix w = B.mult(b);
		
		this.vfa = new LinearVFA(this.featureDatabase);
		for(int i = 0; i < nf; i++){
			this.vfa.setWeight(i, w.get(i, 0));
		}
		
		return w;
		
		
	}
	
	/**
	 * Runs LSPI for either numIterations or until the change in the weight matrix is no greater than maxChange.
	 * @param numIterations the maximum number of policy iterations.
	 * @param maxChange when the weight change is smaller than this value, LSPI terminates.
	 */
	public void runPolicyIteration(int numIterations, double maxChange){
		
		boolean converged = false;
		for(int i = 0; i < numIterations && !converged; i++){
			SimpleMatrix nw = this.LSTDQ();
			double change = Double.POSITIVE_INFINITY;
			if(this.lastWeights != null){
				change = this.lastWeights.minus(nw).normF();
				if(change <= maxChange){
					converged = true;
				}
			}
			this.lastWeights = nw;
			
			DPrint.cl(0, "Finished iteration: " + i + ". Weight change: " + change);
			
		}
		DPrint.cl(0, "Finished Policy Iteration.");
	}
	
	
	/**
	 * Constructs the state-action feature vector as a {@link SimpleMatrix}.
	 * @param features the state-action features that have non-zero values
	 * @param nf the total number of state-action features.
	 * @return the state-action feature vector as a {@link SimpleMatrix}.
	 */
	protected SimpleMatrix phiConstructor(List<ActionFeaturesQuery> features, int nf){
		SimpleMatrix phi = new SimpleMatrix(nf, 1);
		if(features.size() != 1){
			throw new RuntimeException("Expected only one actions's set of features.");
		}
		for(StateFeature f : features.get(0).features){
			phi.set(f.id, f.value);
		}
		
		return phi;
	}
	
	/**
	 * Wraps a {@link GroundedAction} in a list of size 1.
	 * @param ga the {@link GroundedAction} to wrap.
	 * @return a {@link List} consisting of just the input {@link GroundedAction} object.
	 */
	protected List<GroundedAction> gaListWrapper(AbstractGroundedAction ga){
		List<GroundedAction> la = new ArrayList<GroundedAction>(1);
		la.add((GroundedAction)ga);
		return la;
	}
	
	
	@Override
	public List<QValue> getQs(State s) {
		
		List<GroundedAction> gas = this.getAllGroundedActions(s);
		List <QValue> qs = new ArrayList<QValue>(gas.size());
		
		
		List<ActionApproximationResult> results = vfa.getStateActionValues(s, gas);
		for(GroundedAction ga : gas){
			qs.add(this.getQFromFeaturesFor(results, s, ga));
		}
		
		return qs;
		
		
	}

	@Override
	public QValue getQ(State s, AbstractGroundedAction a) {
		
		List <GroundedAction> gaList = new ArrayList<GroundedAction>(1);
		gaList.add((GroundedAction)a);
		
		List<ActionApproximationResult> results = vfa.getStateActionValues(s, gaList);
		
		return this.getQFromFeaturesFor(results, s, (GroundedAction)a);
		
	}
	
	
	/**
	 * Creates a Q-value object in which the Q-value is determined from VFA.
	 * @param results the VFA prediction results for each action.
	 * @param s the state of the Q-value
	 * @param ga the action taken
	 * @return a Q-value object in which the Q-value is determined from VFA.
	 */
	protected QValue getQFromFeaturesFor(List<ActionApproximationResult> results, State s, GroundedAction ga){
		
		ActionApproximationResult result = ActionApproximationResult.extractApproximationForAction(results, ga);
		QValue q = new QValue(s, ga, result.approximationResult.predictedValue);
		
		return q;
	}

	@Override
	public void planFromState(State initialState) {
		
		if(planningCollector == null){
			this.planningCollector = new SARSCollector.UniformRandomSARSCollector(this.actions);
		}
		this.dataset = this.planningCollector.collectNInstances(new ConstantStateGenerator(initialState), this.rf, this.numSamplesForPlanning, Integer.MAX_VALUE, this.tf, this.dataset);
		this.runPolicyIteration(this.maxNumPlanningIterations, this.maxChange);
		

	}

	@Override
	public void resetPlannerResults() {
		this.dataset.clear();
		this.vfa.resetWeights();
	}
	
	
	
	
	/**
	 * Pair of the the state-action features and the next state-action features.
	 * @author James MacGlashan
	 *
	 */
	protected class SSFeatures{
		
		/**
		 * State-action features
		 */
		public List<ActionFeaturesQuery> sActionFeatures;
		
		/**
		 * Next state-action features.
		 */
		public List<ActionFeaturesQuery> sPrimeActionFeatures;
		
		
		/**
		 * Initializes.
		 * @param sActionFeatures state-action features
		 * @param sPrimeActionFeatures next state-action features
		 */
		public SSFeatures(List<ActionFeaturesQuery> sActionFeatures, List<ActionFeaturesQuery> sPrimeActionFeatures){
			this.sActionFeatures = sActionFeatures;
			this.sPrimeActionFeatures = sPrimeActionFeatures;
		}
		
	}


	@Override
	public EpisodeAnalysis runLearningEpisode(Environment env) {
		return this.runLearningEpisode(env, -1);
	}

	@Override
	public EpisodeAnalysis runLearningEpisode(Environment env, int maxSteps) {

		EpisodeAnalysis ea = maxSteps != -1 ? this.learningPolicy.evaluateBehavior(env, maxSteps) : this.learningPolicy.evaluateBehavior(env);

		this.updateDatasetWithLearningEpisode(ea);

		if(this.shouldRereunPolicyIteration(ea)){
			this.runPolicyIteration(this.maxNumPlanningIterations, this.maxChange);
			this.numStepsSinceLastLearningPI = 0;
		}
		else{
			this.numStepsSinceLastLearningPI += ea.numTimeSteps()-1;
		}

		if(episodeHistory.size() >= numEpisodesToStore){
			episodeHistory.poll();
		}
		episodeHistory.offer(ea);

		return ea;
	}

	
	
	
	/**
	 * Updates this object's {@link SARSData} to include the results of a learning episode.
	 * @param ea the learning episode as an {@link EpisodeAnalysis} object.
	 */
	protected void updateDatasetWithLearningEpisode(EpisodeAnalysis ea){
		if(this.dataset == null){
			this.dataset = new SARSData(ea.numTimeSteps()-1);
		}
		for(int i = 0; i < ea.numTimeSteps()-1; i++){
			this.dataset.add(ea.getState(i), ea.getAction(i), ea.getReward(i+1), ea.getState(i+1));
		}
	}
	
	
	/**
	 * Returns whether LSPI should be rereun given the latest learning episode results. Default behavior is to return true
	 * if the number of leanring episode steps plus the number of steps since the last run is greater than the {@link #numStepsSinceLastLearningPI} threshold.
	 * @param ea the most recent learning episode
	 * @return true if LSPI should be rerun; false otherwise.
	 */
	protected boolean shouldRereunPolicyIteration(EpisodeAnalysis ea){
		if(this.numStepsSinceLastLearningPI+ea.numTimeSteps()-1 > this.minNewStepsForLearningPI){
			return true;
		}
		return false;
	}

	public EpisodeAnalysis getLastLearningEpisode() {
		return this.episodeHistory.getLast();
	}

	public void setNumEpisodesToStore(int numEps) {
		this.numEpisodesToStore = numEps;
	}

	public List<EpisodeAnalysis> getAllStoredLearningEpisodes() {
		return this.episodeHistory;
	}

}
