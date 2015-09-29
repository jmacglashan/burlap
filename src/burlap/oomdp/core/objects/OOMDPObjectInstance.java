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
	
	public <T> ObjectInstance setValue(String attName, T value) {
		String valueClass = value.getClass().getSimpleName();
		switch(valueClass) {
		case "boolean":
		case "Boolean":
			Boolean b = (Boolean)value;
			return this.setValue(attName, (boolean)b);
		case "double":
		case "Double":
			Double d = (Double)value;
			return this.setValue(attName, (double)d);
		case "double[]":
		case "Double[]":
			return this.setValue(attName, (double[])value);
		case "int":
		case "Integer":
			Integer i = (Integer)value;
			return this.setValue(attName, (int)i);
		case "int[]":
		case "Integer[]":
			this.setValue(attName, (int[])value);
		case "String":
			this.setValue(attName, (String)value);
		default:
			throw new RuntimeException("Unsupported value type " + valueClass);
		}
	}

}
