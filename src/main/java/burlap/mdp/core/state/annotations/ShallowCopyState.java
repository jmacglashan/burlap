package burlap.mdp.core.state.annotations;

/**
 * A marker for {@link burlap.mdp.core.state.State} implementations that indicates that their copy operation is shallow.
 * When modifying copied states that use a shallow copy, the variable values should always be copied before modifying
 * them, so that the values in the state from which they were copied are not modified. That is, use copy-on-write.
 * Typically, the {@link burlap.mdp.core.state.MutableState#set(Object, Object)} for shallow copied states will
 * always implement a copy-on-write that is safe.
 * @author James MacGlashan.
 */
public @interface ShallowCopyState {
}
