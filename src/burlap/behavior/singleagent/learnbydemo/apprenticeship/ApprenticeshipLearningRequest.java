package burlap.behavior.singleagent.learnbydemo.apprenticeship;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;

public class ApprenticeshipLearningRequest {
	private Domain 						domain;
	private OOMDPPlanner 				planner;
	private PropositionalFunction[] 	featureFunctions;
	private List<EpisodeAnalysis> 		expertEpisodes;
	private StateGenerator 				startStateGenerator;
	private double 						gamma;
	private double 						epsilon;
	private int 						maxIterations;
	private int 						policyCount;
	private double[] 					tHistory;
	private boolean 					useMaxMargin;


	public static final double 			DEFAULT_GAMMA = 0.99;
	public static final double 			DEFAULT_EPSILON = 0.01;
	public static final int 			DEFAULT_MAXITERATIONS = 100;
	public static final int 			DEFAULT_POLICYCOUNT = 5;
	public static final boolean 		DEFAULT_USEMAXMARGIN = false;

	public ApprenticeshipLearningRequest() {
		this.initDefaults();
	}

	public ApprenticeshipLearningRequest(Domain domain, OOMDPPlanner planner, PropositionalFunction[] featureFunctions, List<EpisodeAnalysis> expertEpisodes, StateGenerator startStateGenerator) {
		this.initDefaults();
		this.setDomain(domain);
		this.setPlanner(planner);
		this.setFeatureFunctions(featureFunctions);
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
		if (this.featureFunctions == null || this.featureFunctions.length == 0) {
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

	public void setFeatureFunctions(PropositionalFunction[] functions) {
		this.featureFunctions= functions.clone();
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

	public PropositionalFunction[] getFeatureFunctions() {return this.featureFunctions.clone();}	

	public List<EpisodeAnalysis> getExpertEpisodes() { return new ArrayList<EpisodeAnalysis>(this.expertEpisodes);}

	public StateGenerator getStartStateGenerator() {return this.startStateGenerator;}

	public double getGamma() {return this.gamma;}

	public double getEpsilon() {return this.epsilon;}

	public int getMaxIterations() {return this.maxIterations;}

	public int getPolicyCount() {return this.policyCount;}

	public double[] getTHistory() {return this.tHistory.clone();}

	public boolean getUsingMaxMargin() {return this.useMaxMargin;}
}
