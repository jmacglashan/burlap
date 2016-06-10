package burlap.mdp.core.state.annotations;

/**
 * A marker for {@link burlap.mdp.core.state.State} implementations that indicates that their copy operation is deep.
 * Copied states that use a deep copy can have their variable values safely modified without affecting the values
 * of the state from which they were copied.
 * @author James MacGlashan.
 */
public @interface DeepCopyState {
}
