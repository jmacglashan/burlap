package burlap.behavior.singleagent.learnbydemo.mlirl;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learnbydemo.mlirl.support.DifferentiableRF;
import burlap.behavior.singleagent.learnbydemo.mlirl.support.QGradientPlannerFactory;
import burlap.behavior.singleagent.planning.Planner;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.core.Domain;

import java.util.List;

/**
 * A problem request object for {@link burlap.behavior.singleagent.learnbydemo.mlirl.MultipleIntentionsMLIRL}.
 *
 * @author James MacGlashan.
 */
public class MultipleIntentionsMLIRLRequest extends MLIRLRequest {


	/**
	 * The number of clusters
	 */
	protected int k;

	/**
	 * A {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.QGradientPlannerFactory} that produces {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.QGradientPlanner} objects.
	 */
	protected QGradientPlannerFactory plannerFactory;


	/**
	 * Initializes
	 * @param domain the domain of the problem
	 * @param plannerFactory A {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.QGradientPlannerFactory} that produces {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.QGradientPlanner} objects.
	 * @param expertEpisodes the expert trajectories
	 * @param rf the {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.DifferentiableRF} model to use.
	 * @param k the number of clusters
	 */
	public MultipleIntentionsMLIRLRequest(Domain domain, QGradientPlannerFactory plannerFactory, List<EpisodeAnalysis> expertEpisodes, DifferentiableRF rf, int k) {
		super(domain, null, expertEpisodes, rf);
		this.plannerFactory = plannerFactory;
		this.k = k;
		if(this.plannerFactory != null) {
			this.setPlanner((Planner) plannerFactory.generateDifferentiablePlannerForRequest(this));
		}
	}

	/**
	 * Initializes using a default {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.QGradientPlannerFactory.DifferentiableVIFactory} that
	 * is based on the provided {@link burlap.oomdp.statehashing.HashableStateFactory} object.
	 * @param domain the domain of the problem
	 * @param expertEpisodes the expert trajectories
	 * @param rf the {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.DifferentiableRF} model to use.
	 * @param k the number of clusters
	 * @param hashableStateFactory the {@link burlap.oomdp.statehashing.HashableStateFactory} to use for the {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.QGradientPlannerFactory.DifferentiableVIFactory} that will be created.
	 */
	public MultipleIntentionsMLIRLRequest(Domain domain, List<EpisodeAnalysis> expertEpisodes, DifferentiableRF rf, int k, HashableStateFactory hashableStateFactory) {
		super(domain, null, expertEpisodes, rf);
		this.plannerFactory = new QGradientPlannerFactory.DifferentiableVIFactory(hashableStateFactory);
		this.k = k;
		this.setPlanner((Planner) plannerFactory.generateDifferentiablePlannerForRequest(this));
	}


	@Override
	public boolean isValid(){
		if(!super.isValid()){
			return false;
		}

		if(k < 1){
			return false;
		}

		if(plannerFactory == null){
			return false;
		}

		return true;

	}


	/**
	 * Returns the number of clusters.
	 * @return the number of clusters.
	 */
	public int getK() {
		return k;
	}

	/**
	 * Sets the number of clusters
	 * @param k the number of clusters
	 */
	public void setK(int k) {
		this.k = k;
	}

	public QGradientPlannerFactory getPlannerFactory() {
		return plannerFactory;
	}

	/**
	 * Sets the {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.QGradientPlannerFactory} to use and also
	 * sets this request object's valueFunction instance to a valueFunction generated from it, if it has not already been set.
	 * Setting a valueFunction instance ensures that the {@link #isValid()} methods do not return false.
	 * @param plannerFactory the {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.QGradientPlannerFactory} to use
	 */
	public void setPlannerFactory(QGradientPlannerFactory plannerFactory) {
		this.plannerFactory = plannerFactory;
		if(this.planner == null){
			this.setPlanner((Planner) plannerFactory.generateDifferentiablePlannerForRequest(this));
		}
	}
}
