package burlap.behavior.singleagent.learnfromdemo;

import burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is a {@link burlap.behavior.valuefunction.QFunction}/{@link burlap.behavior.valuefunction.ValueFunction}
 * wrapper to provide the immediate reward signals for a source {@link burlap.oomdp.singleagent.RewardFunction}.
 * It is useful for analyzing learned reward function through IRL, for example, for passing a learned reward function
 * to a {@link burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI} to visualize what
 * was learned. This class returns values based one of four possible reward projection types
 * ({@link burlap.behavior.singleagent.learnfromdemo.RewardValueProjection.RewardProjectionType}):<p>
 * SOURCESTATE: when the reward function only depends on the source state<p>
 * DESTINATIONSTATE: when the reward function only depends on the destination state (the state to which the agent transitions)<p>
 * STATEACTION: when the reward function only depends on the state-action pair<p>
 * ONESTEP: when the reward function depends on a transition of some sort (e.g., from a source state to a target state)<p>
 * The default assumption is DESTINATIONSTATE.<p>
 * When the {@link #value(burlap.oomdp.core.states.State)} of a state is queried, it returns the value of the
 * {@link burlap.oomdp.singleagent.RewardFunction} using the most minimal information. For example, if the projection
 * type is DESTINATIONSTATE, then the value returned is rf.reward(null, null, s), where rf is the input {@link burlap.oomdp.singleagent.RewardFunction}
 * and s is the input {@link burlap.oomdp.core.states.State} to the {@link #value(burlap.oomdp.core.states.State)} method.
 * If it's SOURCESTATE, then it returns rf.reward(s, null, null). If it is STATEACTION or ONESTEP,
 * then the {@link burlap.oomdp.core.Domain} will need to have been input with the {@link #RewardValueProjection(burlap.oomdp.singleagent.RewardFunction, burlap.behavior.singleagent.learnfromdemo.RewardValueProjection.RewardProjectionType, burlap.oomdp.core.Domain)}
 * constructor so that the actions can be enumerated (and in the case of ONESTEP, the transitions enumerated) and the max reward taken.
 * Similarly, the {@link #getQ(burlap.oomdp.core.states.State, burlap.oomdp.core.AbstractGroundedAction)} and
 * {@link #getQs(burlap.oomdp.core.states.State)} methods may need the {@link Domain} provided to properly answer the query.
 *
 *
 * @author James MacGlashan.
 */
public class RewardValueProjection implements QFunction{

	protected RewardFunction rf;
	protected RewardProjectionType projectionType = RewardProjectionType.ONESTEP.DESTINATIONSTATE;
	protected SparseSampling oneStepBellmanPlanner;
	protected Domain domain;

	public static enum RewardProjectionType {SOURCESTATE, DESTINATIONSTATE, STATEACTION, ONESTEP}


	/**
	 * Initializes for the given {@link burlap.oomdp.singleagent.RewardFunction} assuming that it only depends on the destination state.
	 * @param rf the input {@link burlap.oomdp.singleagent.RewardFunction} to project for one step.
	 */
	public RewardValueProjection(RewardFunction rf){
		this.rf = rf;
	}

	/**
	 * Initializes. Note that if projectionType is ONESTEP a runtime exception will be thrown because projecting a one step
	 * value requires the {@link burlap.oomdp.core.Domain} to enumerate the actions and transition dynamics. Use the
	 * {@link #RewardValueProjection(burlap.oomdp.singleagent.RewardFunction, burlap.behavior.singleagent.learnfromdemo.RewardValueProjection.RewardProjectionType, burlap.oomdp.core.Domain)}
	 * constructor instead.
	 * @param rf the input {@link burlap.oomdp.singleagent.RewardFunction} to project for one step.
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
	 * @param rf the input {@link burlap.oomdp.singleagent.RewardFunction} to project for one step.
	 * @param projectionType the type of reward projection to use.
	 * @param domain the {@link burlap.oomdp.core.Domain} in which the {@link burlap.oomdp.singleagent.RewardFunction} is evaluated.
	 */
	public RewardValueProjection(RewardFunction rf, RewardProjectionType projectionType, Domain domain){
		this.rf = rf;
		this.projectionType = projectionType;
		this.domain = domain;
		if(this.projectionType == RewardProjectionType.ONESTEP){
			this.oneStepBellmanPlanner = new SparseSampling(domain, rf, new NullTermination(), 1., new SimpleHashableStateFactory(), 1, -1);
			this.oneStepBellmanPlanner.toggleDebugPrinting(false);
			this.oneStepBellmanPlanner.setForgetPreviousPlanResults(true);
		}
	}

	@Override
	public List<QValue> getQs(State s) {

		if(this.domain != null){
			List<GroundedAction> actions = Action.getAllApplicableGroundedActionsFromActionList(this.domain.getActions(), s);
			List<QValue> qs = new ArrayList<QValue>(actions.size());
			for(GroundedAction ga : actions){
				qs.add(this.getQ(s, ga));
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
	public QValue getQ(State s, AbstractGroundedAction a) {

		switch(this.projectionType){
			case DESTINATIONSTATE: return new QValue(s, a, this.rf.reward(null, (GroundedAction)a, s));
			case SOURCESTATE:
			case STATEACTION: return new QValue(s, a, this.rf.reward(s, (GroundedAction)a, null));
			case ONESTEP: return this.oneStepBellmanPlanner.getQ(s, a);

		}

		throw new RuntimeException("Unknown RewardProjectionType... this shouldn't happen.");

	}

	@Override
	public double value(State s) {

		switch(this.projectionType){
			case DESTINATIONSTATE: return this.rf.reward(null, null, s);
			case SOURCESTATE: return this.rf.reward(s, null, null);
			case STATEACTION: return QFunctionHelper.getOptimalValue(this, s);
			case ONESTEP: return this.oneStepBellmanPlanner.value(s);
		}

		throw new RuntimeException("Unknown RewardProjectionType... this shouldn't happen.");
	}

}
