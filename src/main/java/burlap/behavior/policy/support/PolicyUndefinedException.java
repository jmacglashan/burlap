package burlap.behavior.policy.support;

/**
 * RuntimeException to be thrown when a Policy is queried for a state in which the policy is undefined.
 * @author James MacGlashan
 *
 */
public class PolicyUndefinedException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public PolicyUndefinedException(){
		super("Policy is undefined for the provided state");
	}

}
