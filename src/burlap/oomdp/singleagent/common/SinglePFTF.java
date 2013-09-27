package burlap.oomdp.singleagent.common;

import java.util.List;

import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;


public class SinglePFTF implements TerminalFunction {

	PropositionalFunction			pf;
	boolean							terminateOnTrue;
	
	public SinglePFTF(PropositionalFunction pf){
		this.pf = pf;
		terminateOnTrue = true;
	}
	
	public SinglePFTF(PropositionalFunction pf, boolean terminateOnTrue){
		this.pf = pf;
		this.terminateOnTrue = terminateOnTrue;
	}
	
	public void setTerminateOnTrue(boolean terminateOnTrue){
		this.terminateOnTrue = terminateOnTrue;
	}
	
	@Override
	public boolean isTerminal(State s) {
		List<GroundedProp> gps = s.getAllGroundedPropsFor(pf);
		if(terminateOnTrue){
			for(GroundedProp gp : gps){
				if(gp.isTrue(s)){
					return true;
				}
			}
		}
		else{
			for(GroundedProp gp : gps){
				if(!gp.isTrue(s)){
					return true;
				}
			}
		}
		
		return false;
	}

}
