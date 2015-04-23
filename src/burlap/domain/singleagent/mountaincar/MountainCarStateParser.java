package burlap.domain.singleagent.mountaincar;

import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.states.State;

public class MountainCarStateParser implements StateParser {

	Domain domain;
	
	/**
	 * Constructs a state parser for the given mountain car domain.
	 * @param domain the domain of the mountain car
	 */
	public MountainCarStateParser(Domain domain){
		this.domain = domain;
	}
	
	
	@Override
	public String stateToString(State s) {
		
		ObjectInstance agent = s.getFirstObjectOfClass(MountainCar.CLASSAGENT);
		double x = agent.getRealValForAttribute(MountainCar.ATTX);
		double v = agent.getRealValForAttribute(MountainCar.ATTV);
		
		return x + " " + v;
	}

	@Override
	public State stringToState(String str) {
		
		String [] comps = str.split(" ");
		double x = Double.parseDouble(comps[0]);
		double v = Double.parseDouble(comps[1]);
		
		State s = new State();
		ObjectInstance agent = new ObjectInstance(this.domain.getObjectClass(MountainCar.CLASSAGENT), MountainCar.CLASSAGENT);
		agent.setValue(MountainCar.ATTX, x);
		agent.setValue(MountainCar.ATTV, v);
		s.addObject(agent);
		
		return s;
	}

}
