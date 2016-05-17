package burlap.behavior.stochasticgames.agents.interfacing.singleagent;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.ActionType;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.action.SGAgentAction;
import burlap.mdp.stochasticgames.action.SGAgentActionType;

import java.util.ArrayList;
import java.util.List;


/**
 * This domain generator is used to produce single agent domain version of a stochastic games domain for an agent of a given type
 * (specified by an {@link SGAgentType} object or for a given list of stochastic games agent actions ({@link SGAgentActionType}).
 * The generating single-agent domain has an {@link ActionType} for every {@link SGAgentActionType} associated with the agent.
 * The {@link ActionType} holds an agent name and generates the corresponding {@link SGAgentAction} objects for that agent name.
 * The constructed domain has no associated model since the model in the single agent context would require knowledge
 * of how other agents will act.
 * @author James MacGlashan
 *
 */
public class SGToSADomain implements DomainGenerator {


	String agentName;
	SGDomain srcDomain;
	List<SGAgentActionType> useableActions;
	
	
	/**
	 * Initializes.
	 * @param srcDomain the source stochastic games domain
	 * @param asAgentType the {@link SGAgentType} object specifying the actions that should be created in the single agent domain.
	 */
	public SGToSADomain(String agentName, SGDomain srcDomain, SGAgentType asAgentType){
		this(agentName, srcDomain, asAgentType.actions);
	}
	
	
	/**
	 * Initializes.
	 * @param srcDomain the source stochastic games domain
	 * @param useableActions the stochastic game actions for which single agent actions should be created created in the single agent domain.
	 */
	public SGToSADomain(String agentName, SGDomain srcDomain, List<SGAgentActionType> useableActions){

		this.agentName = agentName;
		this.srcDomain = srcDomain;
		this.useableActions = useableActions;
		
	}
	
	@Override
	public SADomain generateDomain() {

		SADomain domainWrapper = new SADomain();


		for(SGAgentActionType sa : useableActions){
			new SAActionTypeWrapper(agentName, sa);
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
	public static class SAActionTypeWrapper implements ActionType {

		public String agentName;
		public SGAgentActionType srcAction;


		/**
		 * Initializes for a given stochastic games action.
		 * @param srcAction the source stochastic games {@link SGAgentActionType} object.
		 */
		public SAActionTypeWrapper(String agentName, SGAgentActionType srcAction){
			this.agentName = agentName;
			this.srcAction = srcAction;
		}

		@Override
		public String typeName() {
			return srcAction.typeName();
		}

		@Override
		public Action associatedAction(String strRep) {
			return srcAction.associatedAction(agentName, strRep);
		}

		@Override
		public List<Action> allApplicableActions(State s) {
			return new ArrayList<Action>(srcAction.allApplicableActions(agentName, s));
		}
	}


}
