package burlap.oomdp.core.states;

import burlap.oomdp.core.objects.ObjectInstance;

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
