package burlap.behavior.singleagent.learning.modellearning.models.PerceptualModel;

import java.util.List;

import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.Effect;
import burlap.oomdp.core.Attribute;

public class GroupOfPerceptions {
	private List<StatePerception> perceptions;
	
	public GroupOfPerceptions() {
		
	}
	
	public void addPerception(StatePerception p) {
		this.perceptions.add(p);
	}
	
	
	private String getArffString(boolean labeled) {
		StringBuffer sb = new StringBuffer();
		//Set up relation
		sb.append("@RELATION " + "junkFileName" + "\n");
		
		//No data -- break
		if (perceptions.isEmpty()) {
			System.out.println("\tNo data to write");
			return sb.toString();
		}
		
		StatePerception samplePerception = this.perceptions.get(0);
		
		double[] samplePercData = samplePerception.getData();
		
		int numPerceptualDPs = samplePercData.length;
		
		List<String> attributeList = samplePerception.getattributeNames();
		//Set up arff attributes -- attributes in OOMDP state
		for (String attributeName : attributeList) {
			String attributeLine = "@ATTRIBUTE " + attributeName + " NUMERIC\n";  
			sb.append(attributeLine);
		}
		//Set up attributes -- label of data
		sb.append("@ATTRIBUTE class {true,false}\n");

		//Set up data
		sb.append("@DATA\n");
		for (StatePerception currPercData : this.perceptions) {
			String currArffString = currPercData.getArffString(labeled);
			sb.append(currArffString);
		}
		
		
		return sb.toString();
	}
	
}
