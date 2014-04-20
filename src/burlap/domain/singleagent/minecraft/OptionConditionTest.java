package burlap.domain.singleagent.minecraft;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class OptionConditionTest implements StateConditionTest {

	private PropositionalFunction pf;
	private boolean negate;

	public OptionConditionTest(PropositionalFunction terminatingCondition, boolean negate) {
		this.pf = terminatingCondition;
		this.negate = negate;
	}
	
	@Override
	public boolean satisfies(State s) {
		return this.pf.isTrue(s) ^ this.negate || s.getFirstObjectOfClass("agent").getDiscValForAttribute("bNum") < 1;
	}
	
}