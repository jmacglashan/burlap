package burlap.domain.singleagent.cartpole;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.legacy.StateParser;
import burlap.oomdp.stateserialization.SerializableState;
import burlap.oomdp.stateserialization.SerializableStateFactory;

/**
 * A {@link burlap.oomdp.stateserialization.SerializableStateFactory} for simple string representations of {@link burlap.domain.singleagent.cartpole.InvertedPendulum} states.
 * @author James MacGlashan.
 */
public class SerializableInvertedPendulumStateFactory implements SerializableStateFactory{

	@Override
	public SerializableState serialize(State s) {
		return new SerializableInvertedPendulumState(s);
	}

	@Override
	public Class<?> getGeneratedClass() {
		return SerializableInvertedPendulumState.class;
	}


	public static class SerializableInvertedPendulumState extends SerializableState {


		public String stringRep;

		public SerializableInvertedPendulumState() {
		}

		public SerializableInvertedPendulumState(State s) {
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

	public static class InvertedPendulumStateParser implements StateParser {

		Domain domain;

		public InvertedPendulumStateParser(Domain domain) {
			this.domain = domain;
		}

		@Override
		public String stateToString(State s) {
			return SerializableInvertedPendulumStateFactory.stateToString(s);
		}

		@Override
		public State stringToState(String str) {
			return SerializableInvertedPendulumStateFactory.stringToState(domain, str);
		}
	}



	public static String stateToString(State s){
		ObjectInstance o = s.getFirstObjectOfClass(InvertedPendulum.CLASSPENDULUM);
		double a = o.getRealValForAttribute(InvertedPendulum.ATTANGLE);
		double av = o.getRealValForAttribute(InvertedPendulum.ATTANGLEV);

		return a + " " + av;
	}

	public static State stringToState(Domain domain, String str){

		String [] comps = str.split(" ");
		double a = Double.parseDouble(comps[0]);
		double av = Double.parseDouble(comps[1]);
		return InvertedPendulum.getInitialState(domain, a, av);
	}

}
