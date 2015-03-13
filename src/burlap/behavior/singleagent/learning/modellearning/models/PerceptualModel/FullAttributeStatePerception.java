package burlap.behavior.singleagent.learning.modellearning.models.PerceptualModel;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Value;

public class FullAttributeStatePerception extends StatePerception {
	private double[] data;
	private boolean tag;
	private List<String> attributeNames;
	
	public FullAttributeStatePerception(State state, Boolean posInstance) {
		this.tag = posInstance;
		this.attributeNames = new ArrayList<String>();
		//Get allValues
		List<ObjectInstance> allObjects = state.getAllObjects();
		List<Value> allValues = new ArrayList<Value>();
		for (ObjectInstance o : allObjects) {
			for (Attribute att : o.getObjectClass().attributeList) {
				this.attributeNames.add(o.getName()+att.name);
				allValues.add(o.getValueForAttribute(att.name));
			}
		}
		
		//Set data to all values
		double[] data = new double[allValues.size()];
		
		int index = 0;
		for (Value val: allValues) {
			data[index] = val.getNumericRepresentation();
			index++;
		}
		
		this.data = data;
	}
	
	@Override
	public String getArffString(boolean labeled) {
		StringBuffer sb = new StringBuffer();
		String prefix = "";
		for (int i = 0; i < data.length; i++) {
			sb.append(prefix + Double.toString(this.data[i]));
			prefix = ",";
		} 
		sb.append(",");
		if (labeled) {
			sb.append(this.tag);
		}
		else {
			sb.append("?");
		}

		sb.append("\n");
		String toReturn = sb.toString();
		
		return toReturn;
	}

	@Override
	public double[] getData() {
		return this.data;
	}

	@Override
	public List<String> getattributeNames() {
		return this.attributeNames;
	}
}
