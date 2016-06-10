package burlap.behavior.singleagent.learning.modellearning.rmax;

import burlap.behavior.singleagent.learning.modellearning.KWIKModel;
import burlap.behavior.singleagent.shaping.potential.PotentialFunction;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.ActionType;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.TransitionProb;

import java.util.List;


/**
 * @author James MacGlashan.
 */
public class RMaxModel implements KWIKModel{


	protected KWIKModel sourceModel;
	protected PotentialFunction potentialFunction;
	protected double gamma;
	protected List<ActionType> actionsTypes;


	public RMaxModel(KWIKModel sourceModel, PotentialFunction potentialFunction, double gamma, List<ActionType> actionsTypes) {
		this.sourceModel = sourceModel;
		this.potentialFunction = potentialFunction;
		this.gamma = gamma;
		this.actionsTypes = actionsTypes;
	}

	@Override
	public boolean transitionIsModeled(State s, Action a) {
		return sourceModel.transitionIsModeled(s, a);
	}

	@Override
	public void updateModel(EnvironmentOutcome eo) {
		this.sourceModel.updateModel(eo);
	}

	@Override
	public void resetModel() {
		this.sourceModel.resetModel();
	}

	@Override
	public List<TransitionProb> transitions(State s, Action a) {
		List<TransitionProb> tps = sourceModel.transitions(s, a);
		for(TransitionProb tp : tps){
			EnvironmentOutcome eo = tp.eo;
			this.modifyEO(eo);
		}
		return tps;
	}

	@Override
	public EnvironmentOutcome sample(State s, Action a) {
		EnvironmentOutcome eo = sourceModel.sample(s, a);
		modifyEO(eo);
		return eo;
	}

	@Override
	public boolean terminal(State s) {
		return sourceModel.terminal(s);
	}

	protected void modifyEO(EnvironmentOutcome eo){
		double oldPotential = potentialFunction.potentialValue(eo.o);
		double nextPotential = 0.;
		if(!eo.terminated){
			nextPotential = potentialFunction.potentialValue(eo.op);
		}
		double bonus = gamma * nextPotential - oldPotential;
		eo.r = eo.r + bonus;

		if(!KWIKModel.Helper.stateTransitionsModeled(this, actionsTypes, eo.o)){
			eo.terminated = true;
		}
	}

	public KWIKModel getSourceModel() {
		return sourceModel;
	}

	public void setSourceModel(KWIKModel sourceModel) {
		this.sourceModel = sourceModel;
	}

	public PotentialFunction getPotentialFunction() {
		return potentialFunction;
	}

	public void setPotentialFunction(PotentialFunction potentialFunction) {
		this.potentialFunction = potentialFunction;
	}

	public double getGamma() {
		return gamma;
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	public List<ActionType> getActionsTypes() {
		return actionsTypes;
	}

	public void setActionsTypes(List<ActionType> actionsTypes) {
		this.actionsTypes = actionsTypes;
	}
}
