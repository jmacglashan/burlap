package burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.Effect;
import burlap.oomdp.core.Attribute;

public class GroupOfPerceptions {
	private List<StatePerception> perceptions;
	
	public GroupOfPerceptions() {
		this.perceptions = new ArrayList<StatePerception>();
	}
	
	public void addPerception(StatePerception p) {
		if (!this.perceptions.contains(p)) {
			this.perceptions.add(p);
		}
	}
	
	public boolean containsPerception(StatePerception p) {
		return this.perceptions.contains(p);
	}
	
	public List<String> getAttributeNames() {
		StatePerception samplePerception = this.perceptions.get(0);
		
				
		return samplePerception.getattributeNames();
	}
	
	
	public String getArffString(boolean labeled) {
		StringBuffer sb = new StringBuffer();
		//Set up relation
		sb.append("@RELATION " + "junkFileName" + "\n");
		
		//No data -- break
		if (perceptions.isEmpty()) {
			System.out.println("\tNo data to write");
			return sb.toString();
		}
		
		StatePerception samplePerception = this.perceptions.get(0);
						
		List<String> attributeList = samplePerception.getattributeNames();
		//Set up arff attributes -- attributes in OOMDP state
		for (String attributeName : attributeList) {
			String attributeLine = "@ATTRIBUTE " + attributeName;  
			sb.append(attributeLine);
		}
		//Set up attributes -- label of data
		sb.append("@ATTRIBUTE class {true,false}\n");

		//Set up data
		sb.append("@DATA\n");
		for (StatePerception currPercData : this.perceptions) {
			String currArffString = currPercData.getArffValueString(labeled);
			sb.append(currArffString);
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (StatePerception sp: this.perceptions) {
			sb.append(sp.getArffValueString(true));
		}
		return sb.toString();
	}
	
}
