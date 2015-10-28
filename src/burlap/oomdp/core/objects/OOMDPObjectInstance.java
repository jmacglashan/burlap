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
	
	/**
	 * Sets an object's value based on it's java.lang type.
	 */
	public <T> ObjectInstance setValue(String attName, T value) {
		String valueClass = value.getClass().getName();
		if(valueClass.equals("boolean") || valueClass.equals("java.lang.Double")){
			Boolean b = (Boolean)value;
			return this.setValue(attName, (boolean)b);
		}
		else if(valueClass.equals("double") || valueClass.equals("java.lang.Double")){
			Double d = (Double)value;
			return this.setValue(attName, (double)d);
		}
		else if(valueClass.equals("double[]") || valueClass.equals("java.lang.Double[]")){
			return this.setValue(attName, (double[])value);
		}
		else if(valueClass.equals("int") || valueClass.equals("java.lang.Integer")){
			Integer i = (Integer)value;
			return this.setValue(attName, (int)i);
		}
		else if(valueClass.equals("int[]") || valueClass.equals("java.lang.Integer[]")){
			return this.setValue(attName, (int[])value);
		}
		else if(valueClass.equals("java.lang.String")){
			return this.setValue(attName, (String)value);
		}
		throw new RuntimeException("Unsupported value type " + valueClass);

	}

}
