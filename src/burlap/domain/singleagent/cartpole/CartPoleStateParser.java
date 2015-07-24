package burlap.domain.singleagent.cartpole;

import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;


/**
 * A custom state parser for the cart pole domain. Format is single space delimited line of: <br/>
 * cartPosition cartVelocity angle angleVelocity
 * @author James MacGlashan
 *
 */
public class CartPoleStateParser implements StateParser {

	/**
	 * The domain to which created state object created in the {@link #stringToState(String)} method will be associated.
	 */
	protected Domain domain;
	
	/**
	 * Initializes. 
	 * @param domain The domain to which created state object created in the {@link #stringToState(String)} method will be associated.
	 */
	public CartPoleStateParser(Domain domain){
		this.domain = domain;
	}
	
	@Override
	public String stateToString(State s) {
		
		ObjectInstance cp = s.getFirstObjectOfClass(CartPoleDomain.CLASSCARTPOLE);
		double x = cp.getRealValForAttribute(CartPoleDomain.ATTX);
		double xv = cp.getRealValForAttribute(CartPoleDomain.ATTV);
		double a = cp.getRealValForAttribute(CartPoleDomain.ATTANGLE);
		double av = cp.getRealValForAttribute(CartPoleDomain.ATTANGLEV);
		
		String res = x + " " + xv + " " + a + " " + av;
		
		return res;
	}

	@Override
	public State stringToState(String str) {
		
		String [] comps = str.split(" ");
		
		double x = Double.parseDouble(comps[0]);
		double xv = Double.parseDouble(comps[1]);
		double a = Double.parseDouble(comps[2]);
		double av = Double.parseDouble(comps[3]);
		
		return CartPoleDomain.getInitialState(this.domain, x, xv, a, av);
	}

}
