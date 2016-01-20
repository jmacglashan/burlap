package minecraft.MinecraftDomain.PropositionalFunctions;

import minecraft.NameSpace;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class AgentHasAtLeastXGoldBarPF extends PropositionalFunction{

	private int requisiteAmountOfGoldBars;
	
	/**
	 * 
	 * @param name
	 * @param domain
	 * @param parameterClasses
	 * @param amountOfGoldOreToHave
	 */
	public AgentHasAtLeastXGoldBarPF(String name, Domain domain, String[] parameterClasses, int amountOfGoldBarToHave) {
		super(name, domain, parameterClasses);
		this.requisiteAmountOfGoldBars = amountOfGoldBarToHave;
	}

	@Override
	public boolean isTrue(State state, String[] params) {
		ObjectInstance agent = state.getObjectsOfTrueClass(NameSpace.CLASSAGENT).get(0);
		int amountOfGoldBar = agent.getDiscValForAttribute(NameSpace.ATAMTGOLDBAR);
		return amountOfGoldBar >= this.requisiteAmountOfGoldBars;
	}
}
