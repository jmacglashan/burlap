package burlap.oomdp.singleagent;

import burlap.oomdp.core.AbstractObjectParameterizedGroundedAction;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.common.SimpleGroundedAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * If your {@link burlap.oomdp.singleagent.Action} implementation is paramerterized to OO-MDP {@link burlap.oomdp.core.objects.ObjectInstance}
 * references, you can subclass this {@link burlap.oomdp.singleagent.Action} subclass to easily provide that functionality. The {@link burlap.oomdp.singleagent.GroundedAction}
 * instance associated with this action is {@link burlap.oomdp.singleagent.ObjectParameterizedAction.ObjectParameterizedGroundedAction},
 * which implements the {@link burlap.oomdp.core.AbstractObjectParameterizedGroundedAction}, since its parameters refer to
 * OO-MDP {@link burlap.oomdp.core.objects.ObjectInstance} references.
 * <p>
 * The string array in the {@link #ObjectParameterizedAction(String, burlap.oomdp.core.Domain, String[])} constructor
 * specifies the valid type of {@link burlap.oomdp.core.ObjectClass}
 * to which the parameters must belong. For example, in {@link burlap.domain.singleagent.blocksworld.BlocksWorld},
 * we might define a "stack" {@link burlap.oomdp.singleagent.ObjectParameterizedAction} that takes two parameters
 * that each must be instances of the BLOCK class. In such a case, the String array passed to the constructor of the stack
 * {@link burlap.oomdp.singleagent.ObjectParameterizedAction} would be new String[]{"BLOCK", "BLOCK"}.
 * <p>
 * It may also be the case that the order of parameters for an {@link burlap.oomdp.singleagent.ObjectParameterizedAction} is unimportant.
 * For example, a cooking domain might have a "combine"
 * action that combines two INGREDIENT objects. In such a case, the effect of combine(ing1, ing2) would be the same as combine(ing2, ing1).
 * Our action definition can include this parameter symmetry information by assigning parameters to the same <i>parameter order group</i>.
 * The order of parameters in the same parameter order group and be swapped without affecting results, but the order of parameters in different groups
 * cannot be swapped without affecting performance. By default
 * the parameter order group of parameters are all assumed to be different, which means the order of the parameters is important. However,
 * by using the {@link #ObjectParameterizedAction(String, burlap.oomdp.core.Domain, String[], String[])} method, each parameter can also be set
 * to a parameter order group. For example, the parameterClasses of the combine action would be new String[]{INGREDIENT, INGREDIENT}, and
 * the parameterOrderGroups would be new String[]{g1, g1}, thereby placing them in the same group to indicate that their order
 * is unimportant. Specifying parameter order groups is useful because it allows the list of {@link burlap.oomdp.singleagent.ObjectParameterizedAction.ObjectParameterizedGroundedAction}
 * instances returned by the {@link #getAllApplicableGroundedActions(burlap.oomdp.core.states.State)} method to exclude
 * multiple parameterizations that have the same effect.
 * @author James MacGlashan.
 */
public abstract class ObjectParameterizedAction extends Action {

	/**
	 * The object classes each parameter of this action can accept; empty list for a parameter-less action (which is the default)
	 */
	protected String []					parameterClasses;

	/**
	 * Specifies the parameter order group each parameter. Parameters in the same order group are order invariant; that is, if you swapped the parameter assignments for for parameters in the same group, the action would have
	 * the same effect. However, if you swapped the parameter assignments of two parameters in different order groups, the action would have a different effect.
	 */
	protected String []					parameterOrderGroup;


	/**
	 * Initializes the action with the name of the action, the domain to which it belongs, and the parameters it takes.
	 * The action will also be automatically be added to the domain. The parameter order group is set to be a unique order
	 * group for each parameter.
	 * @param name the name of the action
	 * @param domain the domain to which the action belongs
	 * @param parameterClasses a String array of the names of the object classes to which bound parameters must belong
	 */
	public ObjectParameterizedAction(String name, Domain domain, String [] parameterClasses){
		super(name, domain);
		this.parameterClasses = parameterClasses;
		this.parameterOrderGroup = new String[parameterClasses.length];
		//without parameter order group specified, all parameters are assumed to be in a different group
		for(int i = 0; i < parameterOrderGroup.length; i++){
			parameterOrderGroup[i] = name + ".P" + i;
		}

	}

	/**
	 * Initializes the action with the name of the action, the domain to which it belongs, the parameters it takes, and the parameter order groups.
	 * The action will also be automatically be added to the domain.
	 * @param name the name of the action
	 * @param domain the domain to which the action belongs
	 * @param parameterClasses a String array of the names of the object classes to which bound parameters must belong
	 * @param parameterOrderGroups the order group assignments for each of the parameters.
	 */
	public ObjectParameterizedAction(String name, Domain domain, String [] parameterClasses, String [] parameterOrderGroups){
		super(name, domain);
		this.parameterClasses = parameterClasses;
		this.parameterOrderGroup = parameterOrderGroups;
	}

	/**
	 * Returns a String array of the names of of the object classes to which bound parameters must belong
	 * @return a String array of the names of of the object classes to which bound parameters must belong. The array is empty if this action does not require parameters.
	 */
	public final String[] getParameterClasses(){
		return parameterClasses;
	}


	/**
	 * Returns the a String array specifying the parameter order group of each parameter.
	 * @return the a String array specifying the parameter order group of each parameter. The array is empty if this action does not require parameters.
	 */
	public final String[] getParameterOrderGroups(){
		return parameterOrderGroup;
	}

	public abstract boolean parametersAreObjectIdentifierIndependent();

	@Override
	public boolean isParameterized() {
		return true;
	}

	@Override
	public GroundedAction getAssociatedGroundedAction() {
		return new ObjectParameterizedGroundedAction(this);
	}

	@Override
	public List<GroundedAction> getAllApplicableGroundedActions(State s) {

		List <GroundedAction> res = new ArrayList<GroundedAction>();


		if(this.parameterClasses.length == 0){
			//parameterless action for some reason...
			GroundedAction ga = new SimpleGroundedAction(this);
			if(this.applicableInState(s, ga)){
				res.add(new SimpleGroundedAction(this));
			}
			return res; //no parameters to ground
		}



		//otherwise need to do parameter binding
		List <List <String>> bindings = s.getPossibleBindingsGivenParamOrderGroups(this.getParameterClasses(), this.getParameterOrderGroups());

		for(List <String> params : bindings){
			String [] aprams = params.toArray(new String[params.size()]);
			ObjectParameterizedGroundedAction ga = new ObjectParameterizedGroundedAction(this, aprams);
			if(this.applicableInState(s, ga)){
				res.add(ga);
			}
		}

		return res;

	}






	public static class ObjectParameterizedGroundedAction extends GroundedAction implements AbstractObjectParameterizedGroundedAction {

		public String [] params;

		public ObjectParameterizedGroundedAction(Action action){
			super(action);
		}

		public ObjectParameterizedGroundedAction(Action action, String [] params) {
			super(action);
			this.params = params;
		}

		@Override
		public String[] getObjectParameters() {
			return params;
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
		public void setObjectParameters(String[] params) {
			this.params = params;
		}

		@Override
		public boolean actionDomainIsObjectIdentifierIndependent() {
			return ((ObjectParameterizedAction)this.action).parametersAreObjectIdentifierIndependent();
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append(action.getName());
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
        public boolean equals(Object other) {
            if(this == other){
                return true;
            }

            if(!(other instanceof ObjectParameterizedGroundedAction)){
                return false;
            }

            ObjectParameterizedGroundedAction go = (ObjectParameterizedGroundedAction)other;

            if(!this.action.getName().equals(go.action.getName())){
                return false;
            }

            String [] pog = ((ObjectParameterizedAction)this.action).getParameterOrderGroups();

            for(int i = 0; i < this.params.length; i++){
                String p = this.params[i];
                String orderGroup = pog[i];
                boolean foundMatch = false;
                for(int j = 0; j < go.params.length; j++){
                    if(p.equals(go.params[j]) && orderGroup.equals(pog[j])){
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

		@Override
		public GroundedAction copy() {
			return new ObjectParameterizedGroundedAction(this.action, params.clone());
		}
	}


}
