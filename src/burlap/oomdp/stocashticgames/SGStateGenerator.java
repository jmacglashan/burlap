package burlap.oomdp.stocashticgames;

import java.util.List;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;


public abstract class SGStateGenerator {

	public abstract State generateState(List <Agent> agents);
	
	protected ObjectInstance getAgentObjectInstance(Agent a){
		return new ObjectInstance(a.agentType.oclass, a.worldAgentName);
	}

}
