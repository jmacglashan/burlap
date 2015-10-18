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
	public State renameObject(String originalName, String newName){
		ObjectInstance o = this.getObject(originalName);
		return this.renameObject(o, newName);
	}
	
	/** 
	 * Sets an object's value.
	 * @throws RuntimeException if the object doesn't exist, or the attribute name doesn't exist for the object.
	 */
	public <T> State setObjectsValue(String objectName, String attName, T value) {
		ObjectInstance obj = this.getObject(objectName);
		if (obj == null) {
			throw new RuntimeException("Object " + objectName + " does not exist in this state");
		}
		obj.setValue(attName, value);
		return this;
	}
}
