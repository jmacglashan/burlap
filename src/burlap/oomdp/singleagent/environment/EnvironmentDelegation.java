package burlap.oomdp.singleagent.environment;

/**
 * @author James MacGlashan.
 */
public interface EnvironmentDelegation extends Environment {


	/**
	 * Returns the {@link burlap.oomdp.singleagent.environment.Environment} delegate that handles {@link burlap.oomdp.singleagent.environment.Environment}
	 * functionality
	 * @return the {@link burlap.oomdp.singleagent.environment.Environment} delegate
	 */
	Environment getEnvironmentDelegate();

	/**
	 * Sets the {@link burlap.oomdp.singleagent.environment.Environment} delegate that handles {@link burlap.oomdp.singleagent.environment.Environment} functionality
	 * @param delegate  the {@link burlap.oomdp.singleagent.environment.Environment} delegate
	 */
	void setEnvironmentDelegate(Environment delegate);


	/**
	 * A class that provides tools for working with Environment delegates
	 */
	public static class EnvDelegationTools{
	    
	    private EnvDelegationTools() {
	        // do nothing
	    }

		/**
		 * Returns the root {@link burlap.oomdp.singleagent.environment.Environment} delegate. Useful
		 * if an {@link burlap.oomdp.singleagent.environment.EnvironmentDelegation} is expected to have
		 * a delegate that is an {@link burlap.oomdp.singleagent.environment.EnvironmentDelegation}.
		 * @param env the {@link burlap.oomdp.singleagent.environment.EnvironmentDelegation} to inspect
		 * @return the root {@link burlap.oomdp.singleagent.environment.Environment} delegate
		 */
		public static Environment getRootEnvironmentDelegate(EnvironmentDelegation env){
			Environment envChild = env.getEnvironmentDelegate();
			if(envChild instanceof EnvironmentDelegation){
				envChild = getRootEnvironmentDelegate((EnvironmentDelegation)envChild);
			}
			return envChild;
		}


		/**
		 * Returns the {@link burlap.oomdp.singleagent.environment.Environment} or {@link burlap.oomdp.singleagent.environment.Environment}
		 * delegate that implements the class/interface type, or null if none do.
		 * @param env An {@link burlap.oomdp.singleagent.environment.Environment} to inspect
		 * @param type the class/interface type against which and {@link burlap.oomdp.singleagent.environment.Environment} or
		 *             its delegates are being compared.
		 * @return the {@link burlap.oomdp.singleagent.environment.Environment} delegate implementing the input type or null if none do.
		 */
		public static Environment getDelegateImplementing(Environment env, Class<?> type){

			if(type.isAssignableFrom(env.getClass())){
				return env;
			}
			else if(env instanceof EnvironmentDelegation){
				return getDelegateImplementing(((EnvironmentDelegation)env).getEnvironmentDelegate(), type);
			}

			return null;

		}
	}

}
