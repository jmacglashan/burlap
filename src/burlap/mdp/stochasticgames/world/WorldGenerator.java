package burlap.mdp.stochasticgames.world;


/**
 * An interface for generating {@link World} instances.
 * @author James MacGlashan
 *
 */
public interface WorldGenerator {
	/**
	 * Generates a new {@link World} instance.
	 * @return a new {@link World} instance.
	 */
	World generateWorld();
}
