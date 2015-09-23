package burlap.oomdp.singleagent.common;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An abstract subclass of {@link burlap.oomdp.singleagent.Action} for actions that are not parameterized, are primitive,
 * and have no preconditions (applicable everywhere). Only the {@link burlap.oomdp.singleagent.Action#performActionHelper(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
 * method needs to be implemented by a subclass and the {@link burlap.oomdp.singleagent.FullActionModel#getTransitions(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
 * method if the subclass implements the {@link burlap.oomdp.singleagent.FullActionModel} interface. There is also an existing
 * subclass for simple deterministic actions ({@link burlap.oomdp.singleagent.common.SimpleAction.SimpleDeterministicAction}) that fills in the {@link burlap.oomdp.singleagent.FullActionModel#getTransitions(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
 * method using the implementation of {@link burlap.oomdp.singleagent.Action#performActionHelper(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
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
	public GroundedAction getAssociatedGroundedAction() {
		return new SimpleGroundedAction(this);
	}

	@Override
	public List<GroundedAction> getAllApplicableGroundedActions(State s) {
		GroundedAction ga = new SimpleGroundedAction(this);
		return this.applicableInState(s, ga) ? Arrays.asList(ga) : new ArrayList<GroundedAction>(0);
	}


	/**
	 * A abstract class for deterministic actions that are not parameterized, are primitive,
	 * and have no preconditions (applicable everywhere). Only the {@link burlap.oomdp.singleagent.Action#performActionHelper(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
	 * method needs to be implemented by a subclass The {@link burlap.oomdp.singleagent.FullActionModel#getTransitions(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
	 * method is pre-implemented by getting the deterministic result from the implementation of {@link burlap.oomdp.singleagent.Action#performActionHelper(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}.
	 */
	public static abstract class SimpleDeterministicAction extends SimpleAction implements FullActionModel{

		public SimpleDeterministicAction() {
		}

		public SimpleDeterministicAction(String name, Domain domain) {
			super(name, domain);
		}

		@Override
		public List<TransitionProbability> getTransitions(State s, GroundedAction groundedAction) {
			return this.deterministicTransition(s, groundedAction);
		}
	}

}
