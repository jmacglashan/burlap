package burlap.behavior.singleagent.learnbydemo.apprenticeship;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.core.Domain;


/**
 * A datastructure for setting all the parameters of Max Margin Apprenticeship learning.
 * 
 * 
 * @author Stephen Brawner and Mark Ho
 *
 */
public class ApprenticeshipLearningRequest {
	
	/**
	 * The domain in which IRL is to be performed
	 */
	protected Domain 								domain;
	
	/**
	 * The planning algorithm used to compute the policy for a given reward function
	 */
	protected OOMDPPlanner 							planner;
	
	/**
	 * The state feature generator that turns a state into a feature vector on which the reward function is assumed to be modeled
	 */
	protected StateToFeatureVectorGenerator 		featureGenerator;
	
	/**
	 * The input trajectories/episodes that are to be modeled.
	 */
	protected List<EpisodeAnalysis> 				expertEpisodes;
	
	/**
	 * The initial state generator that models the initial states from which the expert trajectories were drawn
	 */
	protected StateGenerator 						startStateGenerator;
	
	/**
	 * The discount factor of the problem
	 */
	protected double 								gamma;
	
	/**
	 * The maximum feature score to cause termination of Apprenticeship learning
	 */
	protected double 								epsilon;
	
	/**
	 * The maximum number of iterations of apprenticeship learning
	 */
	protected int 									maxIterations;
	
	/**
	 * The maximum number of times a policy is rolled out and evaluated
	 */
	protected int 									policyCount;
	
	/**
	 * the history of scores across each reward function improvement
	 */
	protected double[] 								tHistory;
	
	/**
	 * If true, use the full max margin method (expensive); if false, use the cheaper projection method 
	 */
	protected boolean 								useMaxMargin;


	public static final double 			DEFAULT_GAMMA = 0.99;
	public static final double 			DEFAULT_EPSILON = 0.01;
	public static final int 			DEFAULT_MAXITERATIONS = 100;
	public static final int 			DEFAULT_POLICYCOUNT = 5;
	public static final boolean 		DEFAULT_USEMAXMARGIN = false;

	public ApprenticeshipLearningRequest() {
		this.initDefaults();
	}

	public ApprenticeshipLearningRequest(Domain domain, OOMDPPlanner planner, StateToFeatureVectorGenerator featureGenerator, List<EpisodeAnalysis> expertEpisodes, StateGenerator startStateGenerator) {
		this.initDefaults();
		this.setDomain(domain);
		this.setPlanner(planner);
		this.setFeatureGenerator(featureGenerator);
		this.setExpertEpisodes(expertEpisodes);
		this.setStartStateGenerator(startStateGenerator);
	}

	private void initDefaults() {
		this.gamma = ApprenticeshipLearningRequest.DEFAULT_GAMMA;
		this.epsilon = ApprenticeshipLearningRequest.DEFAULT_EPSILON;
		this.maxIterations = ApprenticeshipLearningRequest.DEFAULT_MAXITERATIONS;
		this.policyCount = ApprenticeshipLearningRequest.DEFAULT_POLICYCOUNT;
		this.useMaxMargin = ApprenticeshipLearningRequest.DEFAULT_USEMAXMARGIN;
	}

	public boolean isValid() {
		if (this.domain == null) {
			return false;
		}
		if (this.planner == null) {
			return false;
		}
		if (this.featureGenerator == null) {
			return false;
		}
		if (this.expertEpisodes.size() == 0) {
			return false;
		}
		if (this.startStateGenerator == null) {
			return false;
		}
		if (this.gamma > 1 || this.gamma < 0 || Double.isNaN(this.gamma)) {
			return false;
		}
		if (this.epsilon < 0 || Double.isNaN(this.epsilon)) {
			return false;
		}
		if (this.maxIterations <= 0) {
			return false;
		}
		if (this.policyCount <= 0) {
			return false;
		}
		return true;
	}

	public void setDomain(Domain d) {
		this.domain = d;
	}


	public void setPlanner(OOMDPPlanner p) {
		this.planner = p;
	}

	public void setFeatureGenerator(StateToFeatureVectorGenerator stateFeaturesGenerator) {
		this.featureGenerator = stateFeaturesGenerator;
	}

	public void setExpertEpisodes(List<EpisodeAnalysis> episodeList) {
		this.expertEpisodes = new ArrayList<EpisodeAnalysis>(episodeList);
	}

	public void setStartStateGenerator(StateGenerator startStateGenerator) { this.startStateGenerator = startStateGenerator;}

	public void setGamma(double gamma) { this.gamma = gamma;}

	public void setEpsilon(double epsilon) {this.epsilon = epsilon;}

	public void setMaxIterations(int maxIterations) {this.maxIterations = maxIterations;}

	public void setPolicyCount(int policyCount) {this.policyCount = policyCount;}

	public void setTHistory(double[] tHistory) {this.tHistory = tHistory.clone();}

	public void setUsingMaxMargin(boolean useMaxMargin) {this.useMaxMargin = useMaxMargin;}

	public Domain getDomain() {return this.domain;}

	public OOMDPPlanner getPlanner() {return this.planner;}

	public StateToFeatureVectorGenerator getFeatureGenerator() {return this.featureGenerator;}	

	public List<EpisodeAnalysis> getExpertEpisodes() { return new ArrayList<EpisodeAnalysis>(this.expertEpisodes);}

	public StateGenerator getStartStateGenerator() {return this.startStateGenerator;}

	public double getGamma() {return this.gamma;}

	public double getEpsilon() {return this.epsilon;}

	public int getMaxIterations() {return this.maxIterations;}

	public int getPolicyCount() {return this.policyCount;}

	public double[] getTHistory() {return this.tHistory.clone();}

	public boolean getUsingMaxMargin() {return this.useMaxMargin;}
}
