package burlap.oomdp.stochasticgames;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;


/**
 * This class specifies which action each agent took in a world. The class is backed by a Map from
 * agent names to the {@link GroundedSingleAction} taken by that respective agent. 
 * The {@link GroundedSingleAction} objects of this class can also
 * be iterated over.
 * @author James MacGlashan
 *
 */
public class JointAction extends AbstractGroundedAction implements Iterable<GroundedSingleAction>{

	public Map <String, GroundedSingleAction>		actions;
	
	public JointAction(){
		actions = new TreeMap<String, GroundedSingleAction>();
	}
	
	
	
	/**
	 * Adds all {@link GroundedSingleAction} objects in a list to this joint action.
	 * @param actions the actions to add to this joint action.
	 */
	public JointAction(List <GroundedSingleAction> actions){
		for(GroundedSingleAction gsa : actions){
			this.addAction(gsa);
		}
	}
	
	/**
	 * Adds a single {@link GroundedSingleAction} object to this joint aciton
	 * @param action the action to add
	 */
	public void addAction(GroundedSingleAction action){
		actions.put(action.actingAgent, action);
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
	public List <GroundedSingleAction> getActionList(){
		return new ArrayList<GroundedSingleAction>(actions.values());
	}
	
	
	/**
	 * Returns the action taken by the agent with the given name
	 * @param agentName the name of the agent whose taken action is to be returned.
	 * @return the action taken by the agent with the given name
	 */
	public GroundedSingleAction action(String agentName){
		return actions.get(agentName);
	}
	
	
	/**
	 * Returns a list of the names of all agents who are represented in this joint action.
	 * @return a list of the names of all agents who are represented in this joint action.
	 */
	public List <String> getAgentNames(){
		List <String> anames = new ArrayList<String>(actions.size());
		
		List <GroundedSingleAction> gsas = this.getActionList();
		for(GroundedSingleAction gsa : gsas){
			anames.add(gsa.actingAgent);
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
		StringBuffer buf = new StringBuffer(100);
		List <GroundedSingleAction> gsas = this.getActionList();
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
		StringBuffer buf = new StringBuffer(100);
		List <GroundedSingleAction> gsas = this.getActionList();
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
	public Iterator<GroundedSingleAction> iterator() {
		return new Iterator<GroundedSingleAction>() {
			
			List <GroundedSingleAction> gsas = getActionList();
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < gsas.size();
			}

			@Override
			public GroundedSingleAction next() {
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
		for(GroundedSingleAction gsa : this.actions.values()){
			ja.addAction((GroundedSingleAction)gsa.copy());
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
		
		Iterator<Map.Entry<String, GroundedSingleAction>> taIter = this.actions.entrySet().iterator();
		Iterator<Map.Entry<String, GroundedSingleAction>> oaIter = oja.actions.entrySet().iterator();
		
		while(taIter.hasNext()){
			Map.Entry<String, GroundedSingleAction> tae = taIter.next();
			Map.Entry<String, GroundedSingleAction> oae = oaIter.next();
			
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



	@Override
	public boolean isExecutable() {
		return false;
	}



	@Override
	public State executeIn(State s) {
		throw new RuntimeException("Joint action cannnot be directly executed; apply it with a joint action model instead.");
	}



	@Override
	public boolean actionDomainIsObjectIdentifierDependent() {
		for(GroundedSingleAction gsa : this.actions.values()){
			return gsa.action.domain.isObjectIdentifierDependent();
		}
		return false;
	}


	@Override
	public AbstractGroundedAction translateParameters(State sourceState, State targetState){
		
		if(this.actionDomainIsObjectIdentifierDependent()){
			return this;
		}
		
		JointAction nja = new JointAction();
		
		for(GroundedSingleAction gsa : this.actions.values()){
			GroundedSingleAction ngsa = (GroundedSingleAction) gsa.translateParameters(sourceState, targetState);
			nja.addAction(ngsa);
		}
		
		return nja;
		
	}
	
	@Override
	public boolean isParameterized(){
		for(GroundedSingleAction gsa : this.actions.values()){
			if(gsa.isParameterized()){
				return true;
			}
		}
		
		return false;
	}
	
	
	public static List<JointAction> getAllJointActions(State s, List<Agent> agents){
		
		
		//get all possible individual choices
		List<List<GroundedSingleAction>> individualActionChoices = new ArrayList<List<GroundedSingleAction>>(agents.size());
		for(Agent agent : agents){
			List<GroundedSingleAction> gsas = SingleAction.getAllPossibleGroundedSingleActions(s, agent.worldAgentName, agent.agentType.actions);
			individualActionChoices.add(gsas);
		}
	
		
		//get all joint actions from all combinations of individual actions
		List<JointAction> allJointActions = new LinkedList<JointAction>();
		allJointActionsHelper(individualActionChoices, 0, new LinkedList<GroundedSingleAction>(), allJointActions);
		
		
		return allJointActions;
		
	}
	
	
	
	public static List<JointAction> getAllJointActions(State s, Map<String, AgentType> agents){
		
		
		//get all possible individual choices
		List<List<GroundedSingleAction>> individualActionChoices = new ArrayList<List<GroundedSingleAction>>(agents.size());
		for(Map.Entry<String, AgentType> e : agents.entrySet()){
			List<GroundedSingleAction> gsas = SingleAction.getAllPossibleGroundedSingleActions(s, e.getKey(), e.getValue().actions);
			individualActionChoices.add(gsas);
		}
		
		
		//get all joint actions from all combinations of individual actions
		List<JointAction> allJointActions = new LinkedList<JointAction>();
		allJointActionsHelper(individualActionChoices, 0, new LinkedList<GroundedSingleAction>(), allJointActions);
		
		
		return allJointActions;
		
	}
	
	
	
	
	protected static void allJointActionsHelper(List<List<GroundedSingleAction>> individualActionChoices, int i, LinkedList<GroundedSingleAction> currentSelections, List<JointAction> allJointActions){
		
		if(i >= individualActionChoices.size()){ //base case
			JointAction ja = new JointAction();
			for(GroundedSingleAction gsa : currentSelections){
				ja.addAction(gsa);
			}
			allJointActions.add(ja);
			return ;
		}
		
		List<GroundedSingleAction> agentsChoices = individualActionChoices.get(i);
		for(GroundedSingleAction gsa : agentsChoices){
			currentSelections.push(gsa);
			allJointActionsHelper(individualActionChoices, i+1, currentSelections, allJointActions);
			currentSelections.pop();
		}
	}



	@Override
	public boolean parametersAreObjects() {
		return false;
	}



	

	
	
	
}

