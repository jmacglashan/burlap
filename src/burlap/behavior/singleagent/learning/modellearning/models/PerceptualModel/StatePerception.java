package burlap.behavior.singleagent.learning.modellearning.models.PerceptualModel;

import java.util.List;

public abstract class StatePerception {
	public abstract String getArffString(boolean labeled);
	
	public abstract List<String> getattributeNames();
	public abstract double[] getData();
}
