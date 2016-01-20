package minecraft.MinecraftDomain.PropositionalFunctions;

import java.util.List;

import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class AgentInLavaPF extends PropositionalFunction{

	public AgentInLavaPF(String name, Domain domain, String [] parameterClasses) {
		super(name, domain, parameterClasses);
	}

	@Override
	public boolean isTrue(State state, String[] params) {
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		
		int agentX = agent.getDiscValForAttribute(NameSpace.ATX);
		int agentY = agent.getDiscValForAttribute(NameSpace.ATY);
		int agentZ = agent.getDiscValForAttribute(NameSpace.ATZ);
		
		//Get objects at agent's head
		List<ObjectInstance> objectsAtAgent = Helpers.objectsAt(agentX, agentY, agentZ, state);
		
		//Add those at his feet
		objectsAtAgent.addAll(Helpers.objectsAt(agentX, agentY, agentZ-1, state));
		
		//Check for lava
		for (ObjectInstance object : objectsAtAgent) {
			if (object.getTrueClassName() == NameSpace.CLASSLAVA) {
				return true;
			}
		}
		
		return false;
	}

}
