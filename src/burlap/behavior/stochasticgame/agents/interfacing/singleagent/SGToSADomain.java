package burlap.behavior.stochasticgame.agents.interfacing.singleagent;

import java.util.List;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.stochasticgames.AgentType;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SingleAction;


/**
 * This domain generator is used to produce single agent domain version of a stochastic games domain for an agent of a given type
 * (specified by an {@link burlap.oomdp.stochasticgames.AgentType} object or for a given list of stochastic games single actions ({@link burlap.oomdp.stochasticgames.SingleAction}).
 * Each of the stochastic game single actions is converted into a single agent {@link burlap.oomdp.singleagent.Action} object with the same 
 * action name and parameterizatiosn. Execution of the resulting {@link burlap.oomdp.singleagent.Action} object tells a {@link SingleAgentInterface} object
 * which action was selected so that it may be passed to a corresponding stochastic games world. 
 * <p/>
 * Note: subsequent calls to the {@link #generateDomain()} method
 * will not produce new single agent domain objects and will always return the same reference.
 * @author James MacGlashan
 *
 */
public class SGToSADomain implements DomainGenerator {

	/**
	 * The singel agent domain object that will be returned
	 */
	protected SADomain					domainWrapper;
	
	/**
	 * The agent interface which single agent action objects will call
	 */
	protected SingleAgentInterface 		saInterface;
	
	
	/**
	 * Initializes.
	 * @param srcDomain the source stochastic games domain
	 * @param asAgentType the {@link burlap.oomdp.stochasticgames.AgentType} object specifying the actions that should be created in the single agent domain.
	 * @param saInterface the interface to single agent learning algorithms that the single agent actions created in this domain generator will call.
	 */
	public SGToSADomain(SGDomain srcDomain, AgentType asAgentType, SingleAgentInterface saInterface){
		this(srcDomain, asAgentType.actions, saInterface);	
	}
	
	
	/**
	 * Initializes.
	 * @param srcDomain the source stochastic games domain
	 * @param useableActions the stochastic game actions for whichsingle agent actions should be created created in the single agent domain.
	 * @param saInterface the interface to single agent learning algorithms that the single agent actions created in this domain generator will call.
	 */
	public SGToSADomain(SGDomain srcDomain, List<SingleAction> useableActions, SingleAgentInterface saInterface){
		
		this.domainWrapper = new SADomain();
		this.saInterface = saInterface;
		
		for(Attribute a : srcDomain.getAttributes()){
			this.domainWrapper.addAttribute(a);
		}
		
		for(ObjectClass c : srcDomain.getObjectClasses()){
			this.domainWrapper.addObjectClass(c);
		}
		
		for(PropositionalFunction pf : srcDomain.getPropFunctions()){
			this.domainWrapper.addPropositionalFunction(pf);
		}
		
		for(SingleAction sa : useableActions){
			new SAActionWrapper(sa);
		}
		
	}
	
	@Override
	public Domain generateDomain() {
		return this.domainWrapper;
	}
	
	
	/**
	 * A single agent action wrapper for a stochastic game action. Calling this action will cause it to call the corresponding single interface to inform it
	 * of the action selection. The consructed action will have the same name and object parameterization specification as the source stochastic game
	 * {@link burlap.oomdp.stochasticgames.SingleAction} object.
	 * @author James MacGlashan
	 *
	 */
	protected class SAActionWrapper extends Action{

		/**
		 * Initializes for a given stochastic games action.
		 * @param srcAction the source stochastic games {@link burlap.oomdp.stochasticgames.SingleAction} object.
		 */
		public SAActionWrapper(SingleAction srcAction){
			super(srcAction.actionName, SGToSADomain.this.domainWrapper, srcAction.parameterTypes, srcAction.parameterOrderGroups);
		}
		
		@Override
		protected State performActionHelper(State s, String[] params) {
			return SGToSADomain.this.saInterface.receiveSAAction(new GroundedAction(this, params));
		}
		
	}

}
