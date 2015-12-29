package burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures.AttributeRelations.AttributeRelation;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

/**
 * A relational state perception for a single state where every object instance attribute - object instance attribute pair is examined
 * @author Dhershkowitz
 *
 */
public class RelationalStatePerception extends StatePerception {
	private List<String> attributeNames;
	private List<Double> dataList;
	Boolean tag;

	public RelationalStatePerception(State state, Boolean posInstance, List<AttributeRelation> attRelations) {
		this.tag = posInstance;
		this.attributeNames = new ArrayList<String>();
		List<Double> dataList = new ArrayList<Double>();
		for (ObjectInstance o : state.getAllObjects()) {
			for (Attribute att : o.getObjectClass().attributeList) {
				for (ObjectInstance oOther : state.getAllObjects()) {
					for (Attribute attOther : oOther.getObjectClass().attributeList) {
						double oValue = o.getNumericValForAttribute(att.name);
						double otherOValue = oOther.getNumericValForAttribute(attOther.name);
						for (AttributeRelation attRel : attRelations) {
							attributeNames.add(attRel.toString() + o.getName()+att.name+oOther.getName()+attOther.name+" NUMERIC\n");
							dataList.add(attRel.getRelationValue(oValue, otherOValue));
						}


					}
				}
			}
		}
		this.dataList = dataList;

	}


	@Override
	public String getArffValueString(boolean labeled) {
		StringBuilder sb = new StringBuilder();
		double [] data = this.getData();
		String prefix = "";
		for (int i = 0; i < data.length; i++) {
			sb.append(prefix + data[i]);
			prefix = ",";
		} 
		sb.append(",");
		if (tag != null) {
			sb.append(tag);
		}
		else {
			sb.append("?");
		}

		sb.append("\n");
		return sb.toString();
	}

	@Override
	public List<String> getattributeNames() {
		return this.attributeNames;
	}

	@Override
	public double[] getData() {
		double [] toReturn = new double[this.dataList.size()];
		for (int i = 0; i < this.dataList.size(); i ++) {
			toReturn[i] = this.dataList.get(i);
		}
		return toReturn;
	}

}
