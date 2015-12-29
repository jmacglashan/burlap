package burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures.AttributeRelations;

public class ArithmeticDifferenceRelation extends AttributeRelation {

	@Override
	public double getRelationValue(double oVal, double otherOVal) {
		return oVal-otherOVal;
	}

	@Override
	public String toString() {
		return "ArithmeticDifferenceRelation";
	}
}
