package burlap.oomdp.core.objects;

public abstract class OOMDPObjectInstance implements ObjectInstance{

	public OOMDPObjectInstance() {
	}
	
	/**
	 * Returns a string representation of this object including its name and value attribute value assignment.
	 * @return a string representation of this object including its name and value attribute value assignment.
	 */
	public String getObjectDescription(){
		return this.buildObjectDescription(new StringBuilder()).toString();
		
	
	}

}
