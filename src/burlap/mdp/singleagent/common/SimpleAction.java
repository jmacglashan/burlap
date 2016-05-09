package burlap.mdp.singleagent.common;

import burlap.mdp.core.Domain;
import burlap.mdp.core.TransitionProbability;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.Action;
import burlap.mdp.singleagent.FullActionModel;
import burlap.mdp.singleagent.GroundedAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An abstract subclass of {@link burlap.mdp.singleagent.Action} for actions that are not parameterized, are primitive,
 * and have no preconditions (applicable everywhere). Only the {@link burlap.mdp.singleagent.Action#sampleHelper(State, burlap.mdp.singleagent.GroundedAction)}
 * method needs to be implemented by a subclass and the {@link burlap.mdp.singleagent.FullActionModel#transitions(State, burlap.mdp.singleagent.GroundedAction)}
 * method if the subclass implements the {@link burlap.mdp.singleagent.FullActionModel} interface. There is also an existing
 * subclass for simple deterministic actions ({@link burlap.mdp.singleagent.common.SimpleAction.SimpleDeterministicAction}) that fills in the {@link burlap.mdp.singleagent.FullActionModel#transitions(State, burlap.mdp.singleagent.GroundedAction)}
 * method using the implementation of {@link burlap.mdp.singleagent.Action#sampleHelper(State, burlap.mdp.singleagent.GroundedAction)}
 * method.
 * @author James MacGlashan.
 */
public abstract class SimpleAction extends Action {


	public SimpleAction() {
	}

	public SimpleAction(String name, Domain domain) {
		super(name, domain);
	}

	@Override
	public boolean applicableInState(State s, GroundedAction groundedAction) {
		return true;
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}

	@Override
	public boolean isParameterized() {
		return false;
	}

	@Override
	public GroundedAction associatedGroundedAction() {
		return new SimpleGroundedAction(this);
	}

	@Override
	public List<GroundedAction> allApplicableGroundedActions(State s) {
		GroundedAction ga = new SimpleGroundedAction(this);
		return this.applicableInState(s, ga) ? Arrays.asList(ga) : new ArrayList<GroundedAction>(0);
	}


	/**
	 * A abstract class for deterministic actions that are not parameterized, are primitive,
	 * and have no preconditions (applicable everywhere). Only the {@link burlap.mdp.singleagent.Action#sampleHelper(State, burlap.mdp.singleagent.GroundedAction)}
	 * method needs to be implemented by a subclass The {@link burlap.mdp.singleagent.FullActionModel#transitions(State, burlap.mdp.singleagent.GroundedAction)}
	 * method is pre-implemented by getting the deterministic result from the implementation of {@link burlap.mdp.singleagent.Action#sampleHelper(State, burlap.mdp.singleagent.GroundedAction)}.
	 */
	public static abstract class SimpleDeterministicAction extends SimpleAction implements FullActionModel{

		public SimpleDeterministicAction() {
		}

		public SimpleDeterministicAction(String name, Domain domain) {
			super(name, domain);
		}

		@Override
		public List<TransitionProbability> transitions(State s, GroundedAction groundedAction) {
			return this.deterministicTransition(s, groundedAction);
		}
	}

}
