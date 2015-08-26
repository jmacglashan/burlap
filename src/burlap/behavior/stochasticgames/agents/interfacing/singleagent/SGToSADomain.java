package burlap.behavior.stochasticgames.agents.interfacing.singleagent;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.*;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.stochasticgames.SGAgentType;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;


/**
 * This domain generator is used to produce single agent domain version of a stochastic games domain for an agent of a given type
 * (specified by an {@link burlap.oomdp.stochasticgames.SGAgentType} object or for a given list of stochastic games agent actions ({@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction}).
 * Each of the stochastic game agent actions is converted into a single agent {@link burlap.oomdp.singleagent.Action} object with the same
 * action name and parametrization. The created {@link burlap.oomdp.singleagent.SADomain}'s {@link burlap.oomdp.singleagent.Action} objects maintain the action specification of
 * the input {@link burlap.oomdp.stochasticgames.SGDomain}'s {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} (that is, their name and parameter types), but
 * the {@link burlap.oomdp.singleagent.Action#performAction(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
 * method is undefined since the transition dynamics would depend on the action selection of other agents, which is unknown. Instead, actions can only
 * be executed through the {@link burlap.oomdp.singleagent.Action#performInEnvironment(burlap.oomdp.singleagent.environment.Environment, burlap.oomdp.singleagent.GroundedAction)} method only
 * in which the specified {@link burlap.oomdp.singleagent.environment.Environment} handles the decisions of the other agents. For example, this domain
 * can typically be paired with the {@link LearningAgentToSGAgentInterface}, which will handle these calls
 * indirectly by simultaneously acting as a stochastic game {@link burlap.oomdp.stochasticgames.SGAgent}.
 * @author James MacGlashan
 *
 */
public class SGToSADomain implements DomainGenerator {


	SGDomain srcDomain;
	List<SGAgentAction> useableActions;
	
	
	/**
	 * Initializes.
	 * @param srcDomain the source stochastic games domain
	 * @param asAgentType the {@link burlap.oomdp.stochasticgames.SGAgentType} object specifying the actions that should be created in the single agent domain.
	 */
	public SGToSADomain(SGDomain srcDomain, SGAgentType asAgentType){
		this(srcDomain, asAgentType.actions);
	}
	
	
	/**
	 * Initializes.
	 * @param srcDomain the source stochastic games domain
	 * @param useableActions the stochastic game actions for which single agent actions should be created created in the single agent domain.
	 */
	public SGToSADomain(SGDomain srcDomain, List<SGAgentAction> useableActions){
		
		this.srcDomain = srcDomain;
		this.useableActions = useableActions;
		
	}
	
	@Override
	public Domain generateDomain() {

		SADomain domainWrapper = new SADomain();

		for(Attribute a : srcDomain.getAttributes()){
			domainWrapper.addAttribute(a);
		}

		for(ObjectClass c : srcDomain.getObjectClasses()){
			domainWrapper.addObjectClass(c);
		}

		for(PropositionalFunction pf : srcDomain.getPropFunctions()){
			domainWrapper.addPropositionalFunction(pf);
		}

		for(SGAgentAction sa : useableActions){
			new SAActionWrapper(sa, domainWrapper);
		}


		return domainWrapper;
	}
	
	
	/**
	 * A single agent action wrapper for a stochastic game action. Calling this action will cause it to call the corresponding single interface to inform it
	 * of the action selection. The constructed action will have the same name and object parametrization specification as the source stochastic game
	 * {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} object.
	 * @author James MacGlashan
	 *
	 */
	public static class SAActionWrapper extends Action{

		public String agentName = "tmpAgentNAme";
		public SGAgentAction srcAction;

		/**
		 * Initializes for a given stochastic games action.
		 * @param srcAction the source stochastic games {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} object.
		 */
		public SAActionWrapper(SGAgentAction srcAction, Domain domainWrapper){
			super(srcAction.actionName, domainWrapper);
			this.srcAction = srcAction;
		}

		@Override
		public boolean applicableInState(State s, GroundedAction groundedAction) {
			return this.srcAction.applicableInState(s, ((GroundedSAAActionWrapper)groundedAction).wrappedSGAction);
		}
		
		@Override
		protected State performActionHelper(State s, GroundedAction groundedAction) {
			throw new RuntimeException("The SGToSADomain Action instances cannot execute the performAction method, because the transition dynamics depend on the other agent decisions which are unknown. Instead, use the performInEnvironment method or use these action indirectly with a LearningAgentToSGAgentInterface instance.");
		}

		@Override
		public boolean isParameterized() {
			return srcAction.isParameterized();
		}

		@Override
		public GroundedAction getAssociatedGroundedAction() {
			GroundedSGAgentAction tmp = this.srcAction.getAssociatedGroundedAction(agentName);
			if(tmp instanceof AbstractObjectParameterizedGroundedAction){
				return new GroundedSObParamedAAActionWrapper(this, tmp);
			}
			return new GroundedSAAActionWrapper(this, null);
		}

		@Override
		public List<GroundedAction> getAllApplicableGroundedActions(State s) {

			List<GroundedSGAgentAction> sgGroundigns = this.srcAction.getAllApplicableGroundedActions(s, agentName);
			List<GroundedAction> gas = new ArrayList<GroundedAction>(sgGroundigns.size());
			for(GroundedSGAgentAction gsa : sgGroundigns){
				if(gsa instanceof AbstractObjectParameterizedGroundedAction){
					gas.add(new GroundedSObParamedAAActionWrapper(this, gsa));
				}
				else {
					gas.add(new GroundedSAAActionWrapper(this, gsa));
				}
			}

			return gas;
		}

	}

	public static class GroundedSAAActionWrapper extends GroundedAction{

		GroundedSGAgentAction wrappedSGAction;

		public GroundedSAAActionWrapper(Action action, GroundedSGAgentAction wrappedSGAction) {
			super(action);
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
			AbstractGroundedAction translatedWrapped = AbstractObjectParameterizedGroundedAction.Helper.translateParameters(this.wrappedSGAction, source, target);
			GroundedSAAActionWrapper translated = new GroundedSAAActionWrapper(this.action, (GroundedSGAgentAction)translatedWrapped);
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
	}

	public static class GroundedSObParamedAAActionWrapper extends GroundedSAAActionWrapper implements AbstractObjectParameterizedGroundedAction{

		public GroundedSObParamedAAActionWrapper(Action action, GroundedSGAgentAction wrappedSGAction) {
			super(action, wrappedSGAction);
		}

		@Override
		public String[] getObjectParameters() {
			return ((AbstractObjectParameterizedGroundedAction)this.wrappedSGAction).getObjectParameters();
		}

		@Override
		public void setObjectParameters(String[] params) {
			((AbstractObjectParameterizedGroundedAction)this.wrappedSGAction).setObjectParameters(params);
		}

		@Override
		public boolean actionDomainIsObjectIdentifierIndependent() {
			return ((AbstractObjectParameterizedGroundedAction)this.wrappedSGAction).actionDomainIsObjectIdentifierIndependent();
		}
	}

}
