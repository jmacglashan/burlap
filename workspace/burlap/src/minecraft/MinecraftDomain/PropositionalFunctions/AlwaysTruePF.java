package minecraft.MinecraftDomain.PropositionalFunctions;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class AlwaysTruePF extends PropositionalFunction {
	
	/**
	 * A propositional function that always returns true;
	 * @param name
	 * @param domain
	 * @param parameterClasses
	 */
	public AlwaysTruePF(String name, Domain domain,
			String[] parameterClasses) {
		super(name, domain, parameterClasses);
	}

	@Override
	public boolean isTrue(State state, String[] params) {
		return true;
	}

}
