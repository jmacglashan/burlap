package minecraft.MinecraftDomain.Actions;

import java.util.List;

import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class UseBlockAction extends AgentAction {

	/**
	 * 
	 * @param name
	 * @param domain
	 * @param rows
	 * @param cols
	 * @param height
	 */
	public UseBlockAction(String name, Domain domain, int rows, int cols,int height) {
		super(name, domain, rows, cols, height, false);
	}

	@Override
	void doAction(State state) {
		List<ObjectInstance> objectsInfrontAgent = Helpers.getBlocksInfrontOfAgent(1, state);
		for (ObjectInstance object: objectsInfrontAgent) {
					UseBlockAction.objectUsed(object, state, this.domain);
		}
	}
	
	//Used to change state when an object is used
	private static void objectUsed(ObjectInstance object, State state, Domain domain) {
		String objectName = object.getTrueClassName();
		//FURNACE
		if (objectName.equals(NameSpace.CLASSFURNACE)) {
			ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
			int oldGoldBars = agent.getDiscValForAttribute(NameSpace.ATAMTGOLDBAR);
			int oldGoldOre = agent.getDiscValForAttribute(NameSpace.ATAMTGOLDORE);
			if (oldGoldOre > 0) {
				agent.setValue(NameSpace.ATAMTGOLDBAR, oldGoldBars + 1);
				agent.setValue(NameSpace.ATAMTGOLDORE, oldGoldOre - 1);
			}
		}
		//OTHER STUFF
	}
}
