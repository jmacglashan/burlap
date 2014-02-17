package burlap.behavior.singleagent.auxiliary.performance;

/**
 * Enumerator for the types of statistics that can be plotted by {@link PerformancePlotter}.
 * @author James MacGlashan
 *
 */
public enum PerformanceMetric {
	CUMULATIVEREWARDPERSTEP,
	CUMULTAIVEREWARDPEREPISODE,
	AVERAGEEPISODEREWARD,
	MEDIANEPISODEREWARD,
	CUMULATIVESTEPSPEREPISODE,
	STEPSPEREPISODE;
}
