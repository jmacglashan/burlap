package burlap.behavior.singleagent.planning;

import java.util.List;

import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;


public class SinglePFSCT implements StateConditionTest {

	PropositionalFunction pf;
	
	public SinglePFSCT(PropositionalFunction pf) {
		this.pf = pf;
	}

	@Override
	public boolean satisfies(State s) {
		
		List<GroundedProp> gps = s.getAllGroundedPropsFor(pf);
		
		for(GroundedProp gp : gps){
			if(gp.isTrue(s)){
				return true;
			}
		}
		
		return false;
		
	}

}
