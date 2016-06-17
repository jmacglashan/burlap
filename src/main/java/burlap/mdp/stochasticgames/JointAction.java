package burlap.mdp.stochasticgames;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.ActionUtils;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * This class specifies which action each agent took in a world, where agents are identified by their player number;
 * that is, the order of the actions determines who selected what.
 * @author James MacGlashan
 *
 */
public class JointAction implements Action, Iterable<Action>{

	protected ArrayList<Action> actions;

	public JointAction(){
		actions = new ArrayList<Action>();
	}
	
	
	
	/**
	 * Adds all {@link Action} objects in a list to this joint action.
	 * @param actions the actions to add to this joint action.
	 */
	public JointAction(List <Action> actions){
		this.actions = new ArrayList<Action>(actions);
	}
	
	/**
	 * Adds a single {@link Action} object to this joint action. Replaces the action for the same
	 * agent if an action for that agent is already specified.
	 * @param action the action to add
	 */
	public void addAction(Action action){
		actions.add(action);
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
	public List <Action> getActions(){
		return new ArrayList<Action>(actions);
	}

	public void setActions(List<Action> actions) {
		this.actions = new ArrayList<Action>(actions);
	}

	/**
	 * Sets the action for the specified agent. If the internal joint action does not have
	 * space for an agent with that number, the joint action size will be expanded with null entries until there is space.
	 * @param agentNum the agent whose action is being set
	 * @param a the action of the agent
	 */
	public void setAction(int agentNum, Action a){
		while(this.actions.size() <= agentNum){
			this.actions.add(null);
		}
		this.actions.set(agentNum, a);
	}

	/**
	 * Returns the action taken by the agent
	 * @param agentNum the agent whose action is being queried
	 * @return the action taken by the agent
	 */
	public Action action(int agentNum){
		return actions.get(agentNum);
	}


	
	@Override
	public String toString(){
	    StringBuilder buf = new StringBuilder(100);
		for(int i = 0; i < actions.size(); i++){
			if(i > 0){
				buf.append(';');
			}
			buf.append(actions.get(i).toString());
		}

		return buf.toString();
	}
	
	
	@Override
	public int hashCode(){
		HashCodeBuilder builder = new HashCodeBuilder(17, 31);
		for(Action a : actions){
			builder.append(a.hashCode());
		}
		int code = builder.toHashCode();
		return code;
	}
	

	@Override
	public Iterator<Action> iterator() {
		return this.actions.iterator();
	}
	
	


	public JointAction copy() {
		JointAction ja = new JointAction(new ArrayList<Action>(this.actions));
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

		for(int i = 0; i < this.actions.size(); i++){
			if(!actions.get(i).equals(oja.actions.get(i))){
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
		

		List<SGAgentType> types = new ArrayList<SGAgentType>(agents.size());
		for(SGAgent a : agents){
			types.add(a.agentType());
		}


		return getAllJointActionsFromTypes(s, types);
		
	}

	public static List<JointAction> getAllJointActionsFromTypes(State s, List<SGAgentType> types){

		//get all possible individual choices
		List<List<Action>> individualActionChoices = new ArrayList<List<Action>>(types.size());
		for(SGAgentType type : types){
			List<Action> actions = ActionUtils.allApplicableActionsForTypes(type.actions, s);
			individualActionChoices.add(actions);
		}

		List<JointAction> allJointActions = new LinkedList<JointAction>();
		allJointActionsHelper(individualActionChoices, 0, new LinkedList<Action>(), allJointActions);
		return allJointActions;

	}


	protected static void allJointActionsHelper(List<List<Action>> individualActionChoices, int i, LinkedList<Action> currentSelections, List<JointAction> allJointActions){

		if(i >= individualActionChoices.size()){ //base case
			JointAction ja = new JointAction();
			for(Action gsa : currentSelections){
				ja.addAction(gsa);
			}
			allJointActions.add(ja);
			return ;
		}

		List<Action> agentsChoices = individualActionChoices.get(i);
		for(Action gsa : agentsChoices){
			currentSelections.push(gsa);
			allJointActionsHelper(individualActionChoices, i+1, currentSelections, allJointActions);
			currentSelections.pop();
		}
	}
	




}

