package burlap.behavior.singleagent.auxiliary.performance;

/**
 * Enumerator for the types of statistics that can be plotted by {@link PerformancePlotter}.
 * @author James MacGlashan
 *
 */
public enum PerformanceMetric {
	CUMULATIVE_REWARD_PER_STEP,
	CUMULATIVE_REWARD_PER_EPISODE,
	AVERAGE_EPISODE_REWARD,
	MEDIAN_EPISODE_REWARD,
	CUMULATIVE_STEPS_PER_EPISODE,
	STEPS_PER_EPISODE
}
