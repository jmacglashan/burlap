package burlap.oomdp.core.states;

import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public abstract class OOMDPState implements State {

	
	@Override
	public String toString(){
		return this.getCompleteStateDescription();
	}
	
	/**
	 * Renames the identifier for the object instance currently named originalName with the name newName.
	 * @param originalName the original name of the object instance to be renamed in this state
	 * @param newName the new name of the object instance
	 */
	@Override
	public State renameObject(String originalName, String newName){
		ObjectInstance o = this.getObject(originalName);
		return this.renameObject(o, newName);
	}
}
