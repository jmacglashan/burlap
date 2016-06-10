package burlap.mdp.stochasticgames.action;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.SGAgentType;

import java.util.*;


/**
 * This class specifies which action each agent took in a world. The class is backed by a Map from
 * agent names to the {@link SGAgentAction} taken by that respective agent.
 * The {@link SGAgentAction} objects of this class can also
 * be iterated over.
 * @author James MacGlashan
 *
 */
public class JointAction implements Action, Iterable<SGAgentAction>{

	public Map <String, SGAgentAction>		actions;



	public JointAction(){
		actions = new TreeMap<String, SGAgentAction>();
	}
	
	
	
	/**
	 * Adds all {@link SGAgentAction} objects in a list to this joint action.
	 * @param actions the actions to add to this joint action.
	 */
	public JointAction(List <SGAgentAction> actions){
		this.actions = new TreeMap<String, SGAgentAction>();
		for(SGAgentAction gsa : actions){
			this.addAction(gsa);
		}
	}
	
	/**
	 * Adds a single {@link SGAgentAction} object to this joint action. Replaces the action for the same
	 * agent if an action for that agent is already specified.
	 * @param action the action to add
	 */
	public void addAction(SGAgentAction action){
		actions.put(action.actingAgent(), action);
	}
	
	/**
	 * Returns the number of actions in this joint action.
	 * @return the number of actions in this joint action.
	 */
	public int size(){
		return actions.size();
	}
	
	/**
	 * Returns a list of the actions in this joint action. Modifying the returned
	 * list does not modify the structure of this joint action.
	 * @return a list of the actions in this joint action
	 */
	public List <SGAgentAction> getActionList(){
		return new ArrayList<SGAgentAction>(actions.values());
	}
	
	
	/**
	 * Returns the action taken by the agent with the given name
	 * @param agentName the name of the agent whose taken action is to be returned.
	 * @return the action taken by the agent with the given name
	 */
	public SGAgentAction action(String agentName){
		return actions.get(agentName);
	}
	
	
	/**
	 * Returns a list of the names of all agents who are represented in this joint action.
	 * @return a list of the names of all agents who are represented in this joint action.
	 */
	public List <String> getAgentNames(){
		List <String> anames = new ArrayList<String>(actions.size());
		
		List <SGAgentAction> gsas = this.getActionList();
		for(SGAgentAction gsa : gsas){
			anames.add(gsa.actingAgent());
		}
		
		return anames;
		
	}


	/**
	 * Returns a string representation of this joint aciton without including the parameters of any parameterized actions.
	 * This method can be useful for generating hash codes since two grounded single actions with different parameter orders may be
	 * the same action if the parameters belong to the same order group.
	 * @return a string representation of this joint aciton without including the parameters of any parameterized actions
	 */
	public String noParametersActionDescription(){
	    StringBuilder buf = new StringBuilder(100);
		List <SGAgentAction> gsas = this.getActionList();
		for(int i = 0; i < gsas.size(); i++){
			if(i > 0){
				buf.append(';');
			}
			buf.append(gsas.get(i).actionName());
		}
		
		return buf.toString();
	}
	
	@Override
	public String toString(){
	    StringBuilder buf = new StringBuilder(100);
		List <SGAgentAction> gsas = this.getActionList();
		for(int i = 0; i < gsas.size(); i++){
			if(i > 0){
				buf.append(';');
			}
			buf.append(gsas.get(i).toString());
		}
		
		return buf.toString();
	}
	
	
	@Override
	public int hashCode(){
		return this.noParametersActionDescription().hashCode();
	}
	

	@Override
	public Iterator<SGAgentAction> iterator() {
		return new Iterator<SGAgentAction>() {
			
			List <SGAgentAction> gsas = getActionList();
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < gsas.size();
			}

			@Override
			public SGAgentAction next() {
				return gsas.get(i++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}
	
	


	public JointAction copy() {
		JointAction ja = new JointAction();
		for(SGAgentAction gsa : this.actions.values()){
			ja.addAction((SGAgentAction)gsa.copy());
		}
		return ja;
	}
	
	
	@Override
	public boolean equals(Object o){
		
		if(!(o instanceof JointAction)){
			return false;
		}
		
		JointAction oja = (JointAction)o;
		if(oja.size() != this.size()){
			return false;
		}
		
		Iterator<Map.Entry<String, SGAgentAction>> taIter = this.actions.entrySet().iterator();
		Iterator<Map.Entry<String, SGAgentAction>> oaIter = oja.actions.entrySet().iterator();
		
		while(taIter.hasNext()){
			Map.Entry<String, SGAgentAction> tae = taIter.next();
			Map.Entry<String, SGAgentAction> oae = oaIter.next();
			
			if(!tae.getValue().equals(oae.getValue())){
				return false;
			}
			
		}
		
		return true;
	}
	
	
	@Override
	public String actionName() {
		return this.toString();
	}


	
	
	public static List<JointAction> getAllJointActions(State s, List<SGAgent> agents){
		
		
		//get all possible individual choices
		List<List<SGAgentAction>> individualActionChoices = new ArrayList<List<SGAgentAction>>(agents.size());
		for(SGAgent agent : agents){
			List<SGAgentAction> gsas = SGActionUtils.allApplicableActionsForTypes(agent.getAgentType().actions, agent.getAgentName(), s);
			individualActionChoices.add(gsas);
		}
	
		
		//get all joint actions from all combinations of individual actions
		List<JointAction> allJointActions = new LinkedList<JointAction>();
		allJointActionsHelper(individualActionChoices, 0, new LinkedList<SGAgentAction>(), allJointActions);
		
		
		return allJointActions;
		
	}
	
	
	
	public static List<JointAction> getAllJointActions(State s, Map<String, SGAgentType> agents){
		
		
		//get all possible individual choices
		List<List<SGAgentAction>> individualActionChoices = new ArrayList<List<SGAgentAction>>(agents.size());
		for(Map.Entry<String, SGAgentType> e : agents.entrySet()){
			List<SGAgentAction> gsas = SGActionUtils.allApplicableActionsForTypes(e.getValue().actions, e.getKey(), s);
			individualActionChoices.add(gsas);
		}
		
		
		//get all joint actions from all combinations of individual actions
		List<JointAction> allJointActions = new LinkedList<JointAction>();
		allJointActionsHelper(individualActionChoices, 0, new LinkedList<SGAgentAction>(), allJointActions);
		
		
		return allJointActions;
		
	}
	
	
	
	
	protected static void allJointActionsHelper(List<List<SGAgentAction>> individualActionChoices, int i, LinkedList<SGAgentAction> currentSelections, List<JointAction> allJointActions){
		
		if(i >= individualActionChoices.size()){ //base case
			JointAction ja = new JointAction();
			for(SGAgentAction gsa : currentSelections){
				ja.addAction(gsa);
			}
			allJointActions.add(ja);
			return ;
		}
		
		List<SGAgentAction> agentsChoices = individualActionChoices.get(i);
		for(SGAgentAction gsa : agentsChoices){
			currentSelections.push(gsa);
			allJointActionsHelper(individualActionChoices, i+1, currentSelections, allJointActions);
			currentSelections.pop();
		}
	}



}

