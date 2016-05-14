package burlap.behavior.stochasticgames.agents.interfacing.singleagent;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Action;
import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.singleagent.ActionType;
import burlap.mdp.singleagent.GroundedAction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.stochasticgames.SGAgentType;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.agentactions.SGAgentAction;
import burlap.mdp.stochasticgames.agentactions.SGAgentActionType;

import java.util.ArrayList;
import java.util.List;


/**
 * This domain generator is used to produce single agent domain version of a stochastic games domain for an agent of a given type
 * (specified by an {@link burlap.mdp.stochasticgames.SGAgentType} object or for a given list of stochastic games agent actions ({@link SGAgentActionType}).
 * Each of the stochastic game agent actions is converted into a single agent {@link ActionType} object with the same
 * action name and parametrization. The created {@link burlap.mdp.singleagent.SADomain}'s {@link ActionType} objects maintain the action specification of
 * the input {@link burlap.mdp.stochasticgames.SGDomain}'s {@link SGAgentActionType} (that is, their name and parameter types), but
 * the {@link ActionType#sample(State, burlap.mdp.singleagent.GroundedAction)}
 * method is undefined since the transition dynamics would depend on the action selection of other agents, which is unknown. Instead, actions can only
 * be executed through the {@link ActionType#executeIn(burlap.mdp.singleagent.environment.Environment, burlap.mdp.singleagent.GroundedAction)} method only
 * in which the specified {@link burlap.mdp.singleagent.environment.Environment} handles the decisions of the other agents. For example, this domain
 * can typically be paired with the {@link LearningAgentToSGAgentInterface}, which will handle these calls
 * indirectly by simultaneously acting as a stochastic game {@link burlap.mdp.stochasticgames.SGAgent}.
 * @author James MacGlashan
 *
 */
public class SGToSADomain implements DomainGenerator {


	SGDomain srcDomain;
	List<SGAgentActionType> useableActions;
	
	
	/**
	 * Initializes.
	 * @param srcDomain the source stochastic games domain
	 * @param asAgentType the {@link burlap.mdp.stochasticgames.SGAgentType} object specifying the actions that should be created in the single agent domain.
	 */
	public SGToSADomain(SGDomain srcDomain, SGAgentType asAgentType){
		this(srcDomain, asAgentType.actions);
	}
	
	
	/**
	 * Initializes.
	 * @param srcDomain the source stochastic games domain
	 * @param useableActions the stochastic game actions for which single agent actions should be created created in the single agent domain.
	 */
	public SGToSADomain(SGDomain srcDomain, List<SGAgentActionType> useableActions){
		
		this.srcDomain = srcDomain;
		this.useableActions = useableActions;
		
	}
	
	@Override
	public Domain generateDomain() {

		SADomain domainWrapper = new SADomain();


		for(SGAgentActionType sa : useableActions){
			new SAActionTypeWrapper(sa, domainWrapper);
		}


		return domainWrapper;
	}
	
	
	/**
	 * A single agent action wrapper for a stochastic game action. Calling this action will cause it to call the corresponding single interface to inform it
	 * of the action selection. The constructed action will have the same name and object parametrization specification as the source stochastic game
	 * {@link SGAgentActionType} object.
	 * @author James MacGlashan
	 *
	 */
	public static class SAActionTypeWrapper extends ActionType {

		public String agentName = "tmpAgentNAme";
		public SGAgentActionType srcAction;

		/**
		 * Initializes for a given stochastic games action.
		 * @param srcAction the source stochastic games {@link SGAgentActionType} object.
		 * @param domainWrapper SADomain to which this action will be attached
		 */
		public SAActionTypeWrapper(SGAgentActionType srcAction, Domain domainWrapper){
			super(srcAction.actionName, domainWrapper);
			this.srcAction = srcAction;
		}

		@Override
		public boolean applicableInState(State s, GroundedAction groundedAction) {
			return this.srcAction.applicableInState(s, ((GroundedSAAActionWrapper)groundedAction).wrappedSGAction);
		}
		
		@Override
		protected State sampleHelper(State s, GroundedAction groundedAction) {
			throw new RuntimeException("The SGToSADomain Action instances cannot execute the performAction method, because the transition dynamics depend on the other agent decisions which are unknown. Instead, use the performInEnvironment method or use these action indirectly with a LearningAgentToSGAgentInterface instance.");
		}

		@Override
		public boolean isParameterized() {
			return srcAction.isParameterized();
		}

		@Override
		public GroundedAction associatedGroundedAction() {
			SGAgentAction tmp = this.srcAction.getAssociatedGroundedAction(agentName);
			if(tmp instanceof ObjectParameterizedAction){
				return new GroundedSObParamedAAActionWrapper(this, tmp);
			}
			return new GroundedSAAActionWrapper(this, null);
		}

		@Override
		public List<GroundedAction> allApplicableGroundedActions(State s) {

			List<SGAgentAction> sgGroundigns = this.srcAction.getAllApplicableGroundedActions(s, agentName);
			List<GroundedAction> gas = new ArrayList<GroundedAction>(sgGroundigns.size());
			for(SGAgentAction gsa : sgGroundigns){
				if(gsa instanceof ObjectParameterizedAction){
					gas.add(new GroundedSObParamedAAActionWrapper(this, gsa));
				}
				else {
					gas.add(new GroundedSAAActionWrapper(this, gsa));
				}
			}

			return gas;
		}

		@Override
		public boolean isPrimitive() {
			return true;
		}
	}

	public static class GroundedSAAActionWrapper extends GroundedAction{

		SGAgentAction wrappedSGAction;

		public GroundedSAAActionWrapper(ActionType actionType, SGAgentAction wrappedSGAction) {
			super(actionType);
			this.wrappedSGAction = wrappedSGAction;
		}

		@Override
		public void initParamsWithStringRep(String[] params) {
			wrappedSGAction.initParamsWithStringRep(params);
		}

		@Override
		public String[] getParametersAsString() {
			return wrappedSGAction.getParametersAsString();
		}

		@Override
		public String toString() {
			return wrappedSGAction.toString();
		}

		@Override
		public GroundedAction translateParameters(State source, State target) {
			Action translatedWrapped = ObjectParameterizedAction.Helper.translateParameters(this.wrappedSGAction, source, target);
			GroundedSAAActionWrapper translated = new GroundedSAAActionWrapper(this.actionType, (SGAgentAction)translatedWrapped);
			return translated;
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			return super.equals(other);
		}

		@Override
		public GroundedAction copy() {
			return new GroundedSAAActionWrapper(this.actionType, this.wrappedSGAction);
		}
	}

	public static class GroundedSObParamedAAActionWrapper extends GroundedSAAActionWrapper implements ObjectParameterizedAction {

		public GroundedSObParamedAAActionWrapper(ActionType actionType, SGAgentAction wrappedSGAction) {
			super(actionType, wrappedSGAction);
		}

		@Override
		public String[] getObjectParameters() {
			return ((ObjectParameterizedAction)this.wrappedSGAction).getObjectParameters();
		}

		@Override
		public void setObjectParameters(String[] params) {
			((ObjectParameterizedAction)this.wrappedSGAction).setObjectParameters(params);
		}

		@Override
		public boolean actionDomainIsObjectIdentifierIndependent() {
			return ((ObjectParameterizedAction)this.wrappedSGAction).actionDomainIsObjectIdentifierIndependent();
		}

		@Override
		public GroundedAction copy() {
			return new GroundedSObParamedAAActionWrapper(this.actionType, this.wrappedSGAction);
		}
	}

}
