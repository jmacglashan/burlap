package burlap.oomdp.core.values;

import burlap.oomdp.core.Attribute;


/**
 * This class provides a value for a string. 
 * @author Greg Yauney (gyauney)
 *
 */
public class StringValue extends OOMDPValue implements Value {
	private static final String UNSET = "";
	/**
	 * The string value
	 */
	protected final String			stringVal;
	
	
	/**
	 * Initializes for a given attribute. The default value will be set to 0.
	 * @param attribute
	 */
	public StringValue(Attribute attribute) {
		super(attribute);
		this.stringVal = UNSET;
	}
	
	
	/**
	 * Initializes from an existing value.
	 * @param v the value to copy
	 */
	public StringValue(StringValue v) {
		super(v);
		this.stringVal = ((StringValue)v).stringVal;
	}
	
	public StringValue(Attribute attribute, String stringVal) {
		super(attribute);
		this.stringVal = stringVal;
	}

	@Override
	public Value copy() {
		return new StringValue(this);
	}

	@Override
	public boolean valueHasBeenSet() {
		return true;
	}

	@Override
	public Value setValue(int v) {
		return new StringValue(this.attribute, Integer.toString(v));
	}
	
	@Override
	public Value setValue(double v) {
		return new StringValue(this.attribute, Double.toString(v));
	}
	
	@Override
	public Value setValue(String v) {
		return new StringValue(this.attribute, v);
	}
	
	@Override
	public StringBuilder buildStringVal(StringBuilder builder) {
		return builder.append(this.stringVal);
	}
	
	
	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((stringVal == null) ? 0 : stringVal.hashCode());
        return result;
    }


	@Override
    public boolean equals(Object obj){
        if (this == obj) {
            return true;
        }
        if(!(obj instanceof StringValue)){
            return false;
        }
        
        StringValue o = (StringValue)obj;
        
        if(!o.attribute.equals(attribute)){
            return false;
        }
        
        return this.stringVal.equals(o.stringVal);
        
    }
}
