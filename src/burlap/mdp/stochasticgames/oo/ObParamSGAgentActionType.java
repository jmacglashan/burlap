package burlap.mdp.stochasticgames.oo;

import burlap.mdp.core.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.agentactions.SGAgentAction;
import burlap.mdp.stochasticgames.agentactions.SGAgentActionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 *
 * @author James MacGlashan.
 */
public class ObParamSGAgentActionType implements SGAgentActionType {

	public String name;
	public String [] parameterTypes;
	public String [] parameterOrderGroups;



	/**
	 * Initializes this action type with the given name, and with
	 * the given parameter class types.
	 * @param name the name of this action
	 * @param parameterTypes the object class names for the possible parameters of this action.
	 */
	public ObParamSGAgentActionType(String name, String[] parameterTypes){
		this.name = name;
		this.parameterTypes = parameterTypes;
		parameterOrderGroups = new String[parameterTypes.length];
		for(int i = 0; i < parameterOrderGroups.length; i++){
			parameterOrderGroups[i] = name + ".P" + i;
		}

	}


	/**
	 * Initializes this single action type with the given name, with
	 * the given parameter class types, and with the given parameter order groups.
	 * @param name the name of this action
	 * @param types the object class names for the possible parameters of this action.
	 * @param parameterOrderGroups the parameter order groups to use
	 */
	public ObParamSGAgentActionType(String name, String[] types, String[] parameterOrderGroups){
		this.name = name;
		this.parameterTypes = types;
		this.parameterOrderGroups = parameterOrderGroups;
	}

	@Override
	public String typeName() {
		return name;
	}

	@Override
	public SGAgentAction associatedAction(String actingAgent, String strRep) {
		return this.generateAction(actingAgent, strRep.split(" "));
	}

	@Override
	public List<SGAgentAction> allApplicableActions(String actingAgent, State s) {
		List <SGAgentAction> res = new ArrayList<SGAgentAction>();

		if(!(s instanceof OOState)){
			throw new RuntimeException("Cannot generate possible object-parameterized grounded actions because the input state " + s.getClass().getName() + " does not implement OOState");
		}

		List <List <String>> bindings = OOStateUtilities.getPossibleBindingsGivenParamOrderGroups((OOState)s, this.parameterTypes, this.parameterOrderGroups);

		for(List <String> params : bindings){
			String [] aparams = params.toArray(new String[params.size()]);
			SGAgentAction a = this.generateAction(actingAgent, aparams);
			res.add(a);

		}


		return res;
	}

	protected SGAgentAction generateAction(String actingAgent, String [] params){
		return new ObParamSGAgentAction(this.name, actingAgent, params);
	}


	public static class ObParamSGAgentAction implements SGAgentAction, ObjectParameterizedAction {


		public String actingAgent;
		public String name;
		public String [] params;


		public ObParamSGAgentAction() {
		}

		public ObParamSGAgentAction(String name, String actingAgent, String [] params) {
			this.name = name;
			this.params = params;
		}

		@Override
		public String[] getObjectParameters() {
			return params;
		}

		@Override
		public void setObjectParameters(String[] params) {
			this.params = params;
		}

		@Override
		public String actionName() {
			return name;
		}

		@Override
		public String actingAgent() {
			return actingAgent;
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append(actingAgent).append(":").append(name);
			for(int i = 0; i < params.length; i++){
				buf.append(" ").append(params[i]);
			}

			return buf.toString();
		}


		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(o == null || getClass() != o.getClass()) return false;

			ObParamSGAgentAction that = (ObParamSGAgentAction) o;

			if(actingAgent != null ? !actingAgent.equals(that.actingAgent) : that.actingAgent != null) return false;
			if(name != null ? !name.equals(that.name) : that.name != null) return false;
			// Probably incorrect - comparing Object[] arrays with Arrays.equals
			return Arrays.equals(params, that.params);

		}

		@Override
		public int hashCode() {
			int result = actingAgent != null ? actingAgent.hashCode() : 0;
			result = 31 * result + (name != null ? name.hashCode() : 0);
			result = 31 * result + Arrays.hashCode(params);
			return result;
		}

		@Override
		public Action copy() {
			return new ObParamSGAgentAction(this.name, this.actingAgent, params.clone());
		}

	}



}





