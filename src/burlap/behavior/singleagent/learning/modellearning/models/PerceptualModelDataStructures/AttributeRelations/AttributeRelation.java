package burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures.AttributeRelations;

import java.util.ArrayList;
import java.util.List;

public abstract class AttributeRelation {
	public abstract double getRelationValue(double oVal, double otherOVal);
	
	public static List<AttributeRelation> getAllRelations() {
		ArrayList<AttributeRelation> toReturn = new ArrayList<AttributeRelation>();
		toReturn.add(new ArithmeticDifferenceRelation());
		return toReturn;
	}
	

}
