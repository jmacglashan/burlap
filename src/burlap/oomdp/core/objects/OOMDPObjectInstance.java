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
		String valueClass = value.getClass().getName();
		switch(valueClass) {
		case "boolean":
		case "java.lang.Boolean":
			Boolean b = (Boolean)value;
			return this.setValue(attName, (boolean)b);
		case "double":
		case "java.lang.Double":
			Double d = (Double)value;
			return this.setValue(attName, (double)d);
		case "double[]":
		case "java.lang.Double[]":
			return this.setValue(attName, (double[])value);
		case "int":
		case "java.lang.Integer":
			Integer i = (Integer)value;
			return this.setValue(attName, (int)i);
		case "int[]":
		case "java.lang.Integer[]":
			this.setValue(attName, (int[])value);
		case "java.lang.String":
			this.setValue(attName, (String)value);
		default:
			throw new RuntimeException("Unsupported value type " + valueClass);
		}
	}

}
