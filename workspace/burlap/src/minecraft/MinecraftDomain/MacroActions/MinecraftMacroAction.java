package minecraft.MinecraftDomain.MacroActions;

import java.util.List;

import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


//Taken largely from James*
public abstract class MinecraftMacroAction extends Option{

	/**
	 * The list of actions that will be executed in order when this macro-action is called.
	 */
	protected List<GroundedAction>				actionSequence;

	/**
	 * the current execution index of the macro-action sequence. Every time this action is executed,
	 * it will start at index 0.
	 */
	protected int								curIndex;
	
	protected Domain domain;
	protected State state;
	
	protected abstract List<GroundedAction> getGroundedActions();

	private List<GroundedAction> getGroundedActionWrapper() {
		if (this.actionSequence == null) {
			this.actionSequence = getGroundedActions();
		}
		return this.actionSequence;
	}

	/**
	 * 
	 * @param name
	 * @param rf
	 * @param gamma
	 * @param hashFactory
	 * @param domain
	 * @param state
	 */
	public MinecraftMacroAction(String name, RewardFunction rf, double gamma, StateHashFactory hashFactory, Domain domain, State state){
		this.name = name;
		this.domain = domain;
		this.state = state;
		this.keepTrackOfRewardWith(rf, gamma);
		this.setExpectationHashingFactory(hashFactory);
	}

	@Override
	public boolean isMarkov() {
		return true;
	}

	@Override
	public boolean usesDeterministicTermination() {
		return true;
	}

	@Override
	public boolean usesDeterministicPolicy() {
		return true;
	}

	@Override
	public double probabilityOfTermination(State s, String[] params) {
		if(curIndex >= getGroundedActionWrapper().size()){
			return 1.;
		}
		return 0.;
	}

	@Override
	public void initiateInStateHelper(State s, String[] params) {
		curIndex = 0;
		
	}

	@Override
	public GroundedAction oneStepActionSelection(State s, String[] params) {
		GroundedAction a = getGroundedActionWrapper().get(curIndex);
		curIndex++;

		return a;
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s, String[] params) {
		return this.getDeterministicPolicy(s, params);
	}
	
	protected GroundedAction getGAByActionName(String name) {
		GroundedAction toReturn = this.domain.getAction(name).getAllApplicableGroundedActions(state).get(0);
		return toReturn;
	}

}
