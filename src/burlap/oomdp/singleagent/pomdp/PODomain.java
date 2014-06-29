package burlap.oomdp.singleagent.pomdp;

import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.oomdp.singleagent.SADomain;

public class PODomain extends SADomain {

	protected ObservationFunction 	obsevationFunction;
	protected StateEnumerator		stateEnumerator;
	
	public void setObservationFunction(ObservationFunction observationFunction){
		this.obsevationFunction = observationFunction;
	}
	
	public ObservationFunction getObservationFunction(){
		return this.obsevationFunction;
	}

	public StateEnumerator getStateEnumerator() {
		return stateEnumerator;
	}

	public void setStateEnumerator(StateEnumerator stateEnumerator) {
		this.stateEnumerator = stateEnumerator;
	}
	
	
	
}
