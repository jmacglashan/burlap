package burlap.domain.singleagent.mountaincar;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.OldObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.legacy.StateParser;
import burlap.oomdp.stateserialization.SerializableState;
import burlap.oomdp.stateserialization.SerializableStateFactory;


/**
 * A {@link burlap.oomdp.stateserialization.SerializableStateFactory} for a simple string representation of {@link burlap.domain.singleagent.mountaincar.MountainCar}
 * states.
 * @author James MacGlashan.
 */
public class SerializableMountainCarStateFactory implements SerializableStateFactory {

	@Override
	public SerializableState serialize(State s) {
		return new SerializableMountainCarState(s);
	}

	@Override
	public Class<?> getGeneratedClass() {
		return SerializableMountainCarState.class;
	}


	public static class SerializableMountainCarState extends SerializableState {


		public String stringRep;

		public SerializableMountainCarState() {
		}

		public SerializableMountainCarState(State s) {
			super(s);
		}

		@Override
		public void serialize(State s) {
			this.stringRep = stateToString(s);
		}

		@Override
		public State deserialize(Domain domain) {
			return stringToState(domain, this.stringRep);
		}

	}


	public static class MountainCarStateParser implements StateParser{
		Domain domain;

		public MountainCarStateParser(Domain domain){
			this.domain = domain;
		}

		@Override
		public String stateToString(State s) {
			return SerializableMountainCarStateFactory.stateToString(s);
		}

		@Override
		public State stringToState(String str) {
			return SerializableMountainCarStateFactory.stringToState(domain, str);
		}
	}


	public static String stateToString(State s){
		OldObjectInstance agent = s.getFirstObjectOfClass(MountainCar.CLASSAGENT);
		double x = agent.getRealValForAttribute(MountainCar.ATTX);
		double v = agent.getRealValForAttribute(MountainCar.ATTV);

		return x + " " + v;
	}

	public static State stringToState(Domain domain, String str){
		String [] comps = str.split(" ");
		double x = Double.parseDouble(comps[0]);
		double v = Double.parseDouble(comps[1]);

		State s = new CMutableState();
		OldObjectInstance agent = new MutableObjectInstance(domain.getObjectClass(MountainCar.CLASSAGENT), MountainCar.CLASSAGENT);
		agent.setValue(MountainCar.ATTX, x);
		agent.setValue(MountainCar.ATTV, v);
		s.addObject(agent);

		return s;
	}
}
