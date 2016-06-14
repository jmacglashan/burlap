package burlap.behavior.singleagent.learnfromdemo;

import burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.Action;
import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.ActionUtils;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.statehashing.simple.SimpleHashableStateFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is a {@link QProvider}/{@link burlap.behavior.valuefunction.ValueFunction}
 * wrapper to provide the immediate reward signals for a source {@link RewardFunction}.
 * It is useful for analyzing learned reward function through IRL, for example, for passing a learned reward function
 * to a {@link burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI} to visualize what
 * was learned. This class returns values based one of four possible reward projection types
 * ({@link burlap.behavior.singleagent.learnfromdemo.RewardValueProjection.RewardProjectionType}):<p>
 * SOURCESTATE: when the reward function only depends on the source state<p>
 * DESTINATIONSTATE: when the reward function only depends on the destination state (the state to which the agent transitions)<p>
 * STATEACTION: when the reward function only depends on the state-action pair<p>
 * ONESTEP: when the reward function depends on a transition of some sort (e.g., from a source state to a target state)<p>
 * The default assumption is DESTINATIONSTATE.<p>
 * When the {@link #value(State)} of a state is queried, it returns the value of the
 * {@link RewardFunction} using the most minimal information. For example, if the projection
 * type is DESTINATIONSTATE, then the value returned is rf.reward(null, null, s), where rf is the input {@link RewardFunction}
 * and s is the input {@link State} to the {@link #value(State)} method.
 * If it's SOURCESTATE, then it returns rf.reward(s, null, null). If it is STATEACTION or ONESTEP,
 * then the {@link burlap.mdp.core.Domain} will need to have been input with the {@link #RewardValueProjection(RewardFunction, RewardProjectionType, SADomain)}
 * constructor so that the actions can be enumerated (and in the case of ONESTEP, the transitions enumerated) and the max reward taken.
 * Similarly, the {@link #qValue(State, Action)} and
 * {@link #qValues(State)} methods may need the {@link Domain} provided to properly answer the query.
 *
 *
 * @author James MacGlashan.
 */
public class RewardValueProjection implements QProvider {

	protected RewardFunction rf;
	protected RewardProjectionType projectionType = RewardProjectionType.ONESTEP.DESTINATIONSTATE;
	protected SparseSampling oneStepBellmanPlanner;
	protected SADomain domain;

	public enum RewardProjectionType {SOURCESTATE, DESTINATIONSTATE, STATEACTION, ONESTEP}


	/**
	 * Initializes for the given {@link RewardFunction} assuming that it only depends on the destination state.
	 * @param rf the input {@link RewardFunction} to project for one step.
	 */
	public RewardValueProjection(RewardFunction rf){
		this.rf = rf;
	}

	/**
	 * Initializes. Note that if projectionType is ONESTEP a runtime exception will be thrown because projecting a one step
	 * value requires the {@link burlap.mdp.core.Domain} to enumerate the actions and transition dynamics. Use the
	 * {@link #RewardValueProjection(RewardFunction, RewardProjectionType, SADomain)}
	 * constructor instead.
	 * @param rf the input {@link RewardFunction} to project for one step.
	 * @param projectionType the type of reward projection to use.
	 */
	public RewardValueProjection(RewardFunction rf, RewardProjectionType projectionType){
		this.rf = rf;
		this.projectionType = projectionType;
		if(projectionType == RewardProjectionType.ONESTEP){
			throw new RuntimeException("If the reward function depends on a 1 step transition (e.g., from a source state to a target state) " +
					"then to project the value the Domain is needed evaluate the transition dynamics. Use the RewardValueProjection(RewardFunction, RewardProjectionType, Domain) " +
					"constructor instead to specify.");
		}
	}

	/**
	 * Initializes.
	 * @param rf the input {@link RewardFunction} to project for one step.
	 * @param projectionType the type of reward projection to use.
	 * @param domain the {@link burlap.mdp.core.Domain} in which the {@link RewardFunction} is evaluated.
	 */
	public RewardValueProjection(RewardFunction rf, RewardProjectionType projectionType, SADomain domain){
		this.rf = rf;
		this.projectionType = projectionType;
		this.domain = domain;
		if(this.projectionType == RewardProjectionType.ONESTEP){
			this.oneStepBellmanPlanner = new SparseSampling(domain, 1., new SimpleHashableStateFactory(), 1, -1);
			this.oneStepBellmanPlanner.setModel(new CustomRewardNoTermModel(domain.getModel(), rf));
			this.oneStepBellmanPlanner.toggleDebugPrinting(false);
			this.oneStepBellmanPlanner.setForgetPreviousPlanResults(true);
		}
	}

	@Override
	public List<QValue> qValues(State s) {

		if(this.domain != null){
			List<Action> actions = ActionUtils.allApplicableActionsForTypes(this.domain.getActionTypes(), s);
			List<QValue> qs = new ArrayList<QValue>(actions.size());
			for(Action ga : actions){
				qs.add(new QValue(s, ga, this.qValue(s, ga)));
			}
			return qs;
		}

		if(this.projectionType == RewardProjectionType.DESTINATIONSTATE){
			return Arrays.asList(new QValue(s, null, this.rf.reward(null, null, s)));
		}
		else if(this.projectionType == RewardProjectionType.SOURCESTATE){
			return Arrays.asList(new QValue(s, null, this.rf.reward(null, null, s)));
		}
		else if(this.projectionType == RewardProjectionType.STATEACTION){
			throw new RuntimeException("RewardValueProjection cannot generate all state-action Q-values because it was not" +
					"provided the Domain to enumerate the actions. Use the RewardValueProjection(RewardFunction, RewardProjectionType, Domain) " +
					"constructor to specify it.");
		}

		throw new RuntimeException("Unknown RewardProjectionType... this shouldn't happen.");
	}

	@Override
	public double qValue(State s, Action a) {

		switch(this.projectionType){
			case DESTINATIONSTATE: return this.rf.reward(null, a, s);
			case SOURCESTATE:
			case STATEACTION: return this.rf.reward(s, a, null);
			case ONESTEP: return this.oneStepBellmanPlanner.qValue(s, a);

		}

		throw new RuntimeException("Unknown RewardProjectionType... this shouldn't happen.");

	}

	@Override
	public double value(State s) {

		switch(this.projectionType){
			case DESTINATIONSTATE: return this.rf.reward(null, null, s);
			case SOURCESTATE: return this.rf.reward(s, null, null);
			case STATEACTION: return Helper.maxQ(this, s);
			case ONESTEP: return this.oneStepBellmanPlanner.value(s);
		}

		throw new RuntimeException("Unknown RewardProjectionType... this shouldn't happen.");
	}



	public static class CustomRewardNoTermModel extends CustomRewardModel{

		public CustomRewardNoTermModel(SampleModel model, RewardFunction rewardFunction) {
			super(model, rewardFunction);
		}
		@Override
		public boolean terminal(State s) {
			return false; //always non-terminal
		}

		@Override
		protected EnvironmentOutcome modifyOutcome(EnvironmentOutcome eo) {
			eo.terminated = false;
			return super.modifyOutcome(eo);
		}
	}

}
