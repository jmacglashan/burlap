package minecraft.MinecraftDomain.PropositionalFunctions;

import minecraft.NameSpace;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class AgentHasAtLeastXGoldOrePF extends PropositionalFunction {

	private int requisiteAmountOfGoldOre;
	
	/**
	 * 
	 * @param name
	 * @param domain
	 * @param parameterClasses
	 * @param amountOfGoldOreToHave
	 */
	public AgentHasAtLeastXGoldOrePF(String name, Domain domain, String[] parameterClasses, int amountOfGoldOreToHave) {
		super(name, domain, parameterClasses);
		this.requisiteAmountOfGoldOre = amountOfGoldOreToHave;
	}

	@Override
	public boolean isTrue(State state, String[] params) {
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		int amountOfGold = agent.getDiscValForAttribute(NameSpace.ATAMTGOLDORE);
		return amountOfGold >= this.requisiteAmountOfGoldOre;
	}
}
