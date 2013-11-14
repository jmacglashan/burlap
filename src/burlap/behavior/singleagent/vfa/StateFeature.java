package burlap.behavior.singleagent.vfa;


/**
 * A class for associating a state feature identifier with a value of that state feature
 * @author James MacGlashan
 *
 */
public class StateFeature {

	/**
	 * The state feature identifier
	 */
	public int				id;
	
	/**
	 * The value of the state feature
	 */
	public double			value;
	
	
	/**
	 * Initializes.
	 * @param id the state feature identifier
	 * @param value the value of the state feature
	 */
	public StateFeature(int id, double value) {
		this.id = id;
		this.value = value;
	}
	
	

}
