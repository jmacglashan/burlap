package burlap.mdp.auxiliary.stateconditiontest;

import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 * A state condition class that returns true when ever any grounded version of a specified
 * propositional function is true in a state. Useful for specifying goal conditions.
 * @author James MacGlashan
 *
 */
public class SinglePFSCT implements StateConditionTest {

	PropositionalFunction pf;
	
	/**
	 * Initializes with the propositional function that is checked for state satisfaction
	 * @param pf the propositional function to use for satisfaction tests
	 */
	public SinglePFSCT(PropositionalFunction pf) {
		this.pf = pf;
	}

	@Override
	public boolean satisfies(State s) {
		
		//List<GroundedProp> gps = s.getAllGroundedPropsFor(pf);
		List<GroundedProp> gps = this.pf.getAllGroundedPropsForState(s);
		
		for(GroundedProp gp : gps){
			if(gp.isTrue((OOState)s)){
				return true;
			}
		}
		
		return false;
		
	}

}
