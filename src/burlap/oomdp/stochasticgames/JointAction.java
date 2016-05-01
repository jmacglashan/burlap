package burlap.oomdp.stochasticgames;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.oo.AbstractObjectParameterizedGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;

import java.util.*;


/**
 * This class specifies which action each agent took in a world. The class is backed by a Map from
 * agent names to the {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction} taken by that respective agent.
 * The {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction} objects of this class can also
 * be iterated over.
 * @author James MacGlashan
 *
 */
public class JointAction implements AbstractGroundedAction, Iterable<GroundedSGAgentAction>{

	public Map <String, GroundedSGAgentAction>		actions;
	
	public JointAction(){
		actions = new TreeMap<String, GroundedSGAgentAction>();
	}
	
	
	
	/**
	 * Adds all {@link GroundedSGAgentAction} objects in a list to this joint action.
	 * @param actions the actions to add to this joint action.
	 */
	public JointAction(List <GroundedSGAgentAction> actions){
		this.actions = new TreeMap<String, GroundedSGAgentAction>();
		for(GroundedSGAgentAction gsa : actions){
			this.addAction(gsa);
		}
	}
	
	/**
	 * Adds a single {@link GroundedSGAgentAction} object to this joint action. Replaces the action for the same
	 * agent if an action for that agent is already specified.
	 * @param action the action to add
	 */
	public void addAction(GroundedSGAgentAction action){
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
	public List <GroundedSGAgentAction> getActionList(){
		return new ArrayList<GroundedSGAgentAction>(actions.values());
	}
	
	
	/**
	 * Returns the action taken by the agent with the given name
	 * @param agentName the name of the agent whose taken action is to be returned.
	 * @return the action taken by the agent with the given name
	 */
	public GroundedSGAgentAction action(String agentName){
		return actions.get(agentName);
	}
	
	
	/**
	 * Returns a list of the names of all agents who are represented in this joint action.
	 * @return a list of the names of all agents who are represented in this joint action.
	 */
	public List <String> getAgentNames(){
		List <String> anames = new ArrayList<String>(actions.size());
		
		List <GroundedSGAgentAction> gsas = this.getActionList();
		for(GroundedSGAgentAction gsa : gsas){
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
	    StringBuilder buf = new StringBuilder(100);
		List <GroundedSGAgentAction> gsas = this.getActionList();
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
		List <GroundedSGAgentAction> gsas = this.getActionList();
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
	public Iterator<GroundedSGAgentAction> iterator() {
		return new Iterator<GroundedSGAgentAction>() {
			
			List <GroundedSGAgentAction> gsas = getActionList();
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < gsas.size();
			}

			@Override
			public GroundedSGAgentAction next() {
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
		for(GroundedSGAgentAction gsa : this.actions.values()){
			ja.addAction((GroundedSGAgentAction)gsa.copy());
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
		
		Iterator<Map.Entry<String, GroundedSGAgentAction>> taIter = this.actions.entrySet().iterator();
		Iterator<Map.Entry<String, GroundedSGAgentAction>> oaIter = oja.actions.entrySet().iterator();
		
		while(taIter.hasNext()){
			Map.Entry<String, GroundedSGAgentAction> tae = taIter.next();
			Map.Entry<String, GroundedSGAgentAction> oae = oaIter.next();
			
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




	public AbstractGroundedAction translateParameters(State sourceState, State targetState){

		boolean foundIndependent = false;
		for(GroundedSGAgentAction aa : this.actions.values()){
			if(aa instanceof AbstractObjectParameterizedGroundedAction &&
					((AbstractObjectParameterizedGroundedAction)aa).actionDomainIsObjectIdentifierIndependent()){

				foundIndependent = true;
				break;
			}
		}
		if(!foundIndependent){
			return this;
		}


		JointAction nja = new JointAction();

		for(GroundedSGAgentAction gsa : this.actions.values()){
			GroundedSGAgentAction ngsa = (GroundedSGAgentAction) AbstractObjectParameterizedGroundedAction.Helper.translateParameters(gsa, sourceState, targetState);
			nja.addAction(ngsa);
		}

		return nja;

	}
	
	@Override
	public boolean isParameterized(){
		for(GroundedSGAgentAction gsa : this.actions.values()){
			if(gsa.isParameterized()){
				return true;
			}
		}
		
		return false;
	}
	
	
	public static List<JointAction> getAllJointActions(State s, List<SGAgent> agents){
		
		
		//get all possible individual choices
		List<List<GroundedSGAgentAction>> individualActionChoices = new ArrayList<List<GroundedSGAgentAction>>(agents.size());
		for(SGAgent agent : agents){
			List<GroundedSGAgentAction> gsas = SGAgentAction.getAllApplicableGroundedActionsFromActionList(s, agent.worldAgentName, agent.agentType.actions);
			individualActionChoices.add(gsas);
		}
	
		
		//get all joint actions from all combinations of individual actions
		List<JointAction> allJointActions = new LinkedList<JointAction>();
		allJointActionsHelper(individualActionChoices, 0, new LinkedList<GroundedSGAgentAction>(), allJointActions);
		
		
		return allJointActions;
		
	}
	
	
	
	public static List<JointAction> getAllJointActions(State s, Map<String, SGAgentType> agents){
		
		
		//get all possible individual choices
		List<List<GroundedSGAgentAction>> individualActionChoices = new ArrayList<List<GroundedSGAgentAction>>(agents.size());
		for(Map.Entry<String, SGAgentType> e : agents.entrySet()){
			List<GroundedSGAgentAction> gsas = SGAgentAction.getAllApplicableGroundedActionsFromActionList(s, e.getKey(), e.getValue().actions);
			individualActionChoices.add(gsas);
		}
		
		
		//get all joint actions from all combinations of individual actions
		List<JointAction> allJointActions = new LinkedList<JointAction>();
		allJointActionsHelper(individualActionChoices, 0, new LinkedList<GroundedSGAgentAction>(), allJointActions);
		
		
		return allJointActions;
		
	}
	
	
	
	
	protected static void allJointActionsHelper(List<List<GroundedSGAgentAction>> individualActionChoices, int i, LinkedList<GroundedSGAgentAction> currentSelections, List<JointAction> allJointActions){
		
		if(i >= individualActionChoices.size()){ //base case
			JointAction ja = new JointAction();
			for(GroundedSGAgentAction gsa : currentSelections){
				ja.addAction(gsa);
			}
			allJointActions.add(ja);
			return ;
		}
		
		List<GroundedSGAgentAction> agentsChoices = individualActionChoices.get(i);
		for(GroundedSGAgentAction gsa : agentsChoices){
			currentSelections.push(gsa);
			allJointActionsHelper(individualActionChoices, i+1, currentSelections, allJointActions);
			currentSelections.pop();
		}
	}


	@Override
	public void initParamsWithStringRep(String[] params) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getParametersAsString() {
		String [] vals = new String[this.actions.size()];
		int i = 0;
		for(Map.Entry<String,GroundedSGAgentAction> e : this.actions.entrySet()){
			vals[i] = e.toString();
			i++;
		}
		return vals;
	}
}

