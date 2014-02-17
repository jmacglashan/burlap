package burlap.behavior.singleagent.auxiliary.performance;


/**
 * Enumerator for specifying the what kinds of plots for each {@link PerformanceMetric} will be plotted by {@link PerformancePlotter}.
 * The MOSTRECENTTTRIALONLY mode will result in only the most recent trial's performance being displayed. TRIALAVERAGESONLY will
 * result in only plots for the trial averages to be shown. MOSTRECENTANDAVERAGE will result in both the most recent trial and the trial
 * average plots to be shown.
 * @author James MacGlashan
 *
 */
public enum TrialMode {
	MOSTRECENTTTRIALONLY,
	TRIALAVERAGESONLY,
	MOSTRECENTANDAVERAGE;
	
	/**
	 * Returns true if the most recent trial plots will be plotted by this mode.
	 * @return true if the most recent trial plots will be plotted by this mode; false otherwise.
	 */
	public boolean mostRecentTrialEnabled(){
		return this == MOSTRECENTTTRIALONLY || this == MOSTRECENTANDAVERAGE;
	}
	
	
	/**
	 * Returns true if the trial average plots will be plotted by this mode.
	 * @return true if the trial average plots will be plotted by this mode; false otherwise.
	 */
	public boolean averagesEnabled(){
		return this == TRIALAVERAGESONLY || this == MOSTRECENTANDAVERAGE;
	}
}
