package burlap.behavior.singleagent.learning.modellearning.models.PerceptualModelDataStructures;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.StateHelpers;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class PFStatePerception extends StatePerception {

	List<PropositionalFunction> propFuns;
	String dataString;
	int [] data;
	
	public PFStatePerception(State state, List<PropositionalFunction> propFuns, Boolean tag) {
		this.propFuns = propFuns;
		this.data = StateHelpers.stateToBitStringOfPreds(state, propFuns);
		StringBuilder sb = new StringBuilder();

		
		String prefix = "";
		for (PropositionalFunction pf : propFuns) {
				sb.append(prefix + pf.somePFGroundingIsTrue(state));
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
		
		this.dataString = sb.toString();

	}
	
	@Override
	public String getArffValueString(boolean labeled) {
		return this.dataString;
	}

	@Override
	public List<String> getattributeNames() {
		List<String> toReturn = new ArrayList<String>();
		for (PropositionalFunction pf : this.propFuns) {
			toReturn.add(pf.getName() + " {true,false}\n");
		}
		return toReturn;
	}

	@Override
	public double[] getData() {
		double [] toReturn = new double [this.data.length];
		int i =0;
		for (int currInt: this.data) {
			toReturn[i] = currInt;
			i++;
		}
		return toReturn;
	}

}
