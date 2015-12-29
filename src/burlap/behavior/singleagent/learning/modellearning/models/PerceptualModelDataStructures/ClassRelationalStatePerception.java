package burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures.AttributeRelations.AttributeRelation;
import burlap.behavior.singleagent.learning.modellearning.rmax.TaxiDomain;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

/**
 * A relational state perception for a single state where every object instance attribute - object instance attribute pair is examined
 * @author Dhershkowitz
 *
 */
public class ClassRelationalStatePerception extends StatePerception {
	private List<String> attributeNames;
	private List<Double> dataList;
	Boolean tag;

	public ClassRelationalStatePerception(State state, Boolean posInstance, List<AttributeRelation> attRelations) {
		this.tag = posInstance;
		this.attributeNames = new ArrayList<String>();
		List<Double> dataList = new ArrayList<Double>();
		for (String classString : state.getObjectClassesPresent()) {
			for (Attribute att : state.getObjectsOfTrueClass(classString).get(0).getObjectClass().attributeList) {
				for (AttributeRelation attRel : attRelations) {
					for (String oClassString : state.getObjectClassesPresent()) {
						if (!classString.equals(oClassString)) {
							for (Attribute attOther : state.getObjectsOfTrueClass(oClassString).get(0).getObjectClass().attributeList) {
								double attRelationVal = Integer.MAX_VALUE;

								//Loop over pairs of each object instance
								for (ObjectInstance o : state.getObjectsOfTrueClass(classString)) {
									for (ObjectInstance oOther : state.getObjectsOfTrueClass(oClassString)) {





										if (!oOther.equals(o) || !attOther.equals(att)) {
											double oValue = o.getNumericValForAttribute(att.name);
											double otherOValue = oOther.getNumericValForAttribute(attOther.name);
											attRelationVal = Math.min(attRelationVal, attRel.getRelationValue(otherOValue, oValue));
										}
									}


								}
								attributeNames.add(attRel.toString() + classString+att.name+oClassString+attOther.name+" NUMERIC\n");

								dataList.add(attRelationVal);

							}
						}
					}
				}
			}
			this.dataList = dataList;
		}
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
