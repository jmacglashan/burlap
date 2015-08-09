package burlap.domain.singleagent.cartpole;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.legacy.StateParser;
import burlap.oomdp.stateserialization.SerializableState;
import burlap.oomdp.stateserialization.SerializableStateFactory;


/**
 * A {@link burlap.oomdp.stateserialization.SerializableStateFactory} for simple string representations of {@link burlap.domain.singleagent.cartpole.CartPoleDomain} states.
 * @author James MacGlashan.
 */
public class SerializableCartPoleStateFactory implements SerializableStateFactory {

	@Override
	public SerializableState serialize(State s) {
		return new SerializableCartPoleState(s);
	}

	@Override
	public Class<?> getGeneratedClass() {
		return SerializableCartPoleState.class;
	}


	public static class SerializableCartPoleState extends SerializableState {


		public String stringRep;

		public SerializableCartPoleState() {
		}

		public SerializableCartPoleState(State s) {
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

	public static class CartPoleStateParser implements StateParser{

		Domain domain;

		public CartPoleStateParser(Domain domain) {
			this.domain = domain;
		}

		@Override
		public String stateToString(State s) {
			return SerializableCartPoleStateFactory.stateToString(s);
		}

		@Override
		public State stringToState(String str) {
			return SerializableCartPoleStateFactory.stringToState(domain, str);
		}
	}



	public static String stateToString(State s){
		ObjectInstance cp = s.getFirstObjectOfClass(CartPoleDomain.CLASSCARTPOLE);
		double x = cp.getRealValForAttribute(CartPoleDomain.ATTX);
		double xv = cp.getRealValForAttribute(CartPoleDomain.ATTV);
		double a = cp.getRealValForAttribute(CartPoleDomain.ATTANGLE);
		double av = cp.getRealValForAttribute(CartPoleDomain.ATTANGLEV);

		String res = x + " " + xv + " " + a + " " + av;

		return res;
	}

	public static State stringToState(Domain domain, String str){

		String [] comps = str.split(" ");

		double x = Double.parseDouble(comps[0]);
		double xv = Double.parseDouble(comps[1]);
		double a = Double.parseDouble(comps[2]);
		double av = Double.parseDouble(comps[3]);

		return CartPoleDomain.getInitialState(domain, x, xv, a, av);
	}
}
