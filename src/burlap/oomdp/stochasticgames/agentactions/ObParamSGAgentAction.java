package burlap.oomdp.stochasticgames.agentactions;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.AbstractObjectParameterizedGroundedAction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.SGDomain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  If your {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} implementation is paramerterized to OO-MDP {@link burlap.oomdp.core.objects.ObjectInstance}
 * references, you can subclass this {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} subclass to easily provide that functionality. The {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction}
 * instance associated with this action is {@link burlap.oomdp.stochasticgames.agentactions.ObParamSGAgentAction.GroundedObParamSGAgentAction},
 * which implements the {@link burlap.oomdp.core.AbstractObjectParameterizedGroundedAction}, since its parameters refer to
 * OO-MDP {@link burlap.oomdp.core.objects.ObjectInstance} references.
 * <p>
 * The string array in the {@link #ObParamSGAgentAction(burlap.oomdp.stochasticgames.SGDomain, String, String[])} constructor
 * specifies the valid type of {@link burlap.oomdp.core.ObjectClass}
 * to which the parameters must belong.
 * <p>
 * It may also be the case that the order of parameters for an {@link burlap.oomdp.singleagent.ObjectParameterizedAction} is unimportant.
 * In this case, you can specify parameter order groups to indicate for which parameters the order is unimportant. See the
 * {@link burlap.oomdp.singleagent.ObjectParameterizedAction} class documentation for more information of parameter order groups.
 *
 * @author James MacGlashan.
 */
public abstract class ObParamSGAgentAction extends SGAgentAction {

	public String [] parameterTypes;
	public String [] parameterOrderGroups;



	/**
	 * Initializes this single action to be for the given domain, with the given name, and with
	 * the given parameter class types. This action
	 * is automatically added to the given domain
	 * @param d the domain to which this action belongs
	 * @param name the name of this action
	 * @param parameterTypes the object class names for the possible parameters of this action.
	 */
	public ObParamSGAgentAction(SGDomain d, String name, String[] parameterTypes){
		super(d, name);
		this.parameterTypes = parameterTypes;
		parameterOrderGroups = new String[parameterTypes.length];
		for(int i = 0; i < parameterOrderGroups.length; i++){
			parameterOrderGroups[i] = name + ".P" + i;
		}

	}


	/**
	 * Initializes this single action to be for the given domain, with the given name,  with
	 * the given parameter class types, and with the given parameter order groups. This action
	 * is automatically added to the given domain
	 * @param d the domain to which this action belongs
	 * @param name the name of this action
	 * @param types the object class names for the possible parameters of this action.
	 * @param parameterOrderGroups the parameter order groups to use
	 */
	public ObParamSGAgentAction(SGDomain d, String name, String[] types, String[] parameterOrderGroups){
		super(d, name);
		this.parameterTypes = types;
		this.parameterOrderGroups = parameterOrderGroups;
	}

	@Override
	public boolean isParameterized() {
		return this.parameterTypes.length > 0;
	}

	public abstract boolean parametersAreObjectIdentifierIndependent();


	@Override
	public GroundedSGAgentAction getAssociatedGroundedAction(String actingAgent) {
		return new GroundedObParamSGAgentAction(actingAgent, this);
	}

	@Override
	/**
	 * Returns all possible grounded versions of this single action for a given state and acting agent.
	 * @param s the state in which the agent would execute this action
	 * @param actingAgent the agent who would execute the action
	 * @return all possible grounded versions of this single action for a given state and acting agent.
	 */
	public List<GroundedSGAgentAction> getAllApplicableGroundedActions(State s, String actingAgent){

		List <GroundedSGAgentAction> res = new ArrayList<GroundedSGAgentAction>();

		if(this.parameterTypes.length == 0){
			GroundedSGAgentAction gsa = new SimpleGroundedSGAgentAction(actingAgent, this);
			if(this.applicableInState(s, gsa)){
				res.add(gsa);
			}
			return res; //no parameters so just the single ga without params
		}

		List <List <String>> bindings = s.getPossibleBindingsGivenParamOrderGroups(this.parameterTypes, this.parameterOrderGroups);

		for(List <String> params : bindings){
			String [] aparams = params.toArray(new String[params.size()]);
			GroundedObParamSGAgentAction gsa = new GroundedObParamSGAgentAction(actingAgent, this, aparams);
			if(this.applicableInState(s, gsa)){
				res.add(gsa);
			}
		}


		return res;

	}



	public static class GroundedObParamSGAgentAction extends GroundedSGAgentAction implements AbstractObjectParameterizedGroundedAction{

		public String [] params;

		public GroundedObParamSGAgentAction(String actingAgent, SGAgentAction a) {
			super(actingAgent, a);
		}

		public GroundedObParamSGAgentAction(String actingAgent, SGAgentAction a, String[] p) {
			super(actingAgent, a);
			this.params = p;
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
		public boolean actionDomainIsObjectIdentifierIndependent() {
			return ((ObParamSGAgentAction)this.action).parametersAreObjectIdentifierIndependent();
		}


		@Override
		public void initParamsWithStringRep(String[] params) {
			this.params = params;
		}

		@Override
		public String[] getParametersAsString() {
			return this.params;
		}

		@Override
		public GroundedSGAgentAction copy() {
			return new GroundedObParamSGAgentAction(this.actingAgent, this.action, this.params.clone());
		}


		/**
		 * Returns a string specifying the action name and parameters used in this GroundedSingleAction.
		 * @return a string specifying the action name and parameters used in this GroundedSingleAction.
		 */
		@Override
		public String justActionString(){
		    StringBuilder buf = new StringBuilder();
			buf.append(action.actionName);
			for(int i = 0; i < params.length; i++){
				buf.append(" ").append(params[i]);
			}

			return buf.toString();

		}

		@Override
		public String toString(){
		    StringBuilder buf = new StringBuilder();
			buf.append(actingAgent).append(":");
			buf.append(action.actionName);
			for(int i = 0; i < params.length; i++){
				buf.append(" ").append(params[i]);
			}

			return buf.toString();
		}


		@Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + Arrays.hashCode(params);
            return result;
        }

		@Override
        public boolean equals(Object other){

            if(this == other){
                return true;
            }

            if(!(other instanceof GroundedObParamSGAgentAction)){
                return false;
            }

            GroundedObParamSGAgentAction go = (GroundedObParamSGAgentAction)other;

            if(!this.actingAgent.equals(go.actingAgent)){
                return false;
            }

            if(!this.action.actionName.equals(go.action.actionName)){
                return false;
            }

            String [] rclasses = ((ObParamSGAgentAction)this.action).parameterOrderGroups;

            for(int i = 0; i < this.params.length; i++){
                String p = this.params[i];
                String replaceClass = rclasses[i];
                boolean foundMatch = false;
                for(int j = 0; j < go.params.length; j++){
                    if(p.equals(go.params[j]) && replaceClass.equals(rclasses[j])){
                        foundMatch = true;
                        break;
                    }
                }
                if(!foundMatch){
                    return false;
                }

            }

            return true;
        }
	}

}
