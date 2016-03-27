package burlap.behavior.stochasticgames.auxiliary.performance;

import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.WorldObserver;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;
import java.util.List;



/**
 * This class is a world observer used for recording and plotting the performance of the agents in the world.
 * There are 
 * six possible performance metrics that can be used to plot performance, as specified in the {@link PerformanceMetric} enumerator.  A plot showing the most recent
 * "trial" of the agents can be displayed, the average of the metric over all trials with confidence intervals, or both may be displayed; which plots are shown is specfied by the
 * {@link TrialMode} enumerator.
 * Any subset of these metrics
 * may be displayed in any order specified by the user and plots are displayed in a matrix format with a maximum number of columns that are filled out first.
 * If the number of plots would cause a window height larger than a maximimum specified, then the plots are placed in a scroll view.
 * <p>
 * The way this class should be used is first the constructor should be called, then the {@link #startGUI()} method. At the start of each trial, the {@link #startNewTrial()} method should be called
 * (although it is unncessary to call this method if it is the *first* trial since that will automatically be created.) A world object should then be created
 * with this object attached to it. The world should then be run for as many episodes as desired until the next trial when things repeat. After all trials
 * are complete, the {@link #startNewTrial()} method should be called.
 * <p>
 * To ensure proper use of this class, it is highly recommended that the {@link MultiAgentExperimenter} class is used, because it will perform all the necessary steps for you.
 * <p>
 * When testing is done, you may optionally request all data to be printed to CSV files. One CSV file will produce the step-wise performance
 * metric (cumulaitve reward by step) for all agents and trials. Another will produce all the episode-wise performance metric data. This data
 * can be produced regardless of which metrics you requested to be plotted.
 * <p>
 * Note that the plots that are created have a number of interactive options. Try right-clicking on them to see the list of things you can modfiy in the GUI.
 * 
 * @author James MacGlashan
 *
 */
public class MultiAgentPerformancePlotter extends JFrame implements WorldObserver {

	private static final long serialVersionUID = 1L;


	private static final Map<Integer, Double> cachedCriticalValues = new HashMap<Integer, Double>();
	
	
	/**
	 * Terminal funciton for determining when episodes have ended.
	 */
	protected TerminalFunction	tf;
	
	/**
	 * Datastructure for maintaining data for each agent playing in the game.
	 */
	protected Map<String, DatasetsAndTrials> agentWiseData = new HashMap<String, MultiAgentPerformancePlotter.DatasetsAndTrials>();
	
	/**
	 * All agent plot series for the the most recetent trial's cumulative reward per step
	 */
	protected XYSeriesCollection colCSR;
	
	/**
	 * All agent plot series for the the most recetent trial's cumulative reward per episode
	 */
	protected XYSeriesCollection colCER;
	
	/**
	 * All agent plot series for the the most recetent trial's average reward per episode
	 */
	protected XYSeriesCollection colAER;
	
	/**
	 * All agent plot series for the the most recetent trial's median reward per episode
	 */
	protected XYSeriesCollection colMER;
	
	/**
	 * All agent plot series for the the most recetent trial's cumulative step per episode
	 */
	protected XYSeriesCollection colCSE;
	
	/**
	 * All agent plot series for the most recetent trial's steps per episode
	 */
	protected XYSeriesCollection colSE;
	
	
	/**
	 * All agent plot series for the average of all trial's cumulative reward per step
	 */
	protected YIntervalSeriesCollection colCSRAvg;
	
	/**
	 * All agent plot series for the average of all trial's cumulative reward per episode
	 */
	protected YIntervalSeriesCollection colCERAvg;
	
	/**
	 * All agent plot series for the average of all trial's average reward per episode
	 */
	protected YIntervalSeriesCollection colAERAvg;
	
	/**
	 * All agent plot series for the average of all trial's median reward per episode
	 */
	protected YIntervalSeriesCollection colMERAvg;
	
	/**
	 * All agent plot series for the average of all trial's cumulative steps per episode
	 */
	protected YIntervalSeriesCollection colCSEAvg;
	
	/**
	 * All agent plot series for the average of all trial's steps per episode
	 */
	protected YIntervalSeriesCollection colSEAvg;
	
	
	/**
	 * A set specifying the performance metrics that will be plotted
	 */
	protected Set<PerformanceMetric> metricsSet = new HashSet<PerformanceMetric>();
	
	/**
	 * specifies whether the most recent trial, average of all trials, or both plots will be displayed 
	 */
	protected TrialMode trialMode;
	
	
	/**
	 * Whether the data from action observations received should be recoreded or not.
	 */
	protected boolean collectData = true;
	
	
	/**
	 * The last time step at which the plots' series data was updated
	 */
	protected int lastTimeStepUpdate = 0;
	
	/**
	 * The last episode at which the plot's series data was updated
	 */
	protected int lastEpisode = 0;
	
	
	/**
	 * the current time step that was recorded
	 */
	protected int curTimeStep = 0;
	
	/**
	 * the current episode that was recorded
	 */
	protected int curEpisode = 0;
	
	/**
	 * the delay in milliseconds between which the charts are updated automatically
	 */
	protected int delay = 1000;
	
	/**
	 * the significance level used for confidence intervals. The default is 0.05 (corresponding to a 95% CI).
	 */
	protected double significance = 0.05;
	
	
	/**
	 * Whether the current plots need their series data cleared for a new trial
	 */
	protected boolean needsClearing = false;
	
	
	/**
	 * Indicates whether this observer has observed any outcomes yet.
	 */
	protected boolean freshStart = true;
	
	/**
	 * Synchronization object to ensure proper threaded plot updating
	 */
	protected MutableBoolean trialUpdateComplete = new MutableBoolean(true);
	
	
	/**
	 * The debug code used for debug printing.
	 */
	public int debugCode = 74629;
	
	
	
	
	
	
	
	/**
	 * Initializes
	 * @param tf the terminal function that will be used for detecting the end of episdoes
	 * @param chartWidth the width of a cart
	 * @param chartHeight the height of a chart
	 * @param columns the number of columns of charts
	 * @param maxWindowHeight the maximum window height until a scroll bar will be added
	 * @param trialMode the kinds of trail data that will be displayed
	 * @param metrics which metrics will be plotted.
	 */
	public MultiAgentPerformancePlotter(TerminalFunction tf, int chartWidth, int chartHeight, int columns, int maxWindowHeight, 
								TrialMode trialMode, PerformanceMetric...metrics){
		
		this.tf = tf;
		
		colCSR = new XYSeriesCollection();
		colCER = new XYSeriesCollection();
		colAER = new XYSeriesCollection();
		colMER = new XYSeriesCollection();
		colCSE = new XYSeriesCollection();
		colSE = new XYSeriesCollection();
		
		colCSRAvg = new YIntervalSeriesCollection();
		colCERAvg = new YIntervalSeriesCollection();
		colAERAvg = new YIntervalSeriesCollection();
		colMERAvg = new YIntervalSeriesCollection();
		colCSEAvg = new YIntervalSeriesCollection();
		colSEAvg = new YIntervalSeriesCollection();
		
		
		if(metrics.length == 0){
			metricsSet.add(PerformanceMetric.CUMULATIVEREWARDPERSTEP);
			
			metrics = new PerformanceMetric[]{PerformanceMetric.CUMULATIVEREWARDPERSTEP};
		}
		
		this.trialMode = trialMode;
		
		Container plotContainer = new Container();
		plotContainer.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(0, 0, 10, 10);
        
        for(PerformanceMetric m : metrics){
        	
        	this.metricsSet.add(m);
        	
        	if(m == PerformanceMetric.CUMULATIVEREWARDPERSTEP){
        		this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Cumulative Reward", "Time Step", "Cumulative Reward", colCSR, colCSRAvg);
        	}
        	else if(m == PerformanceMetric.CUMULTAIVEREWARDPEREPISODE){
        		this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Cumulative Reward", "Episode", "Cumulative Reward", colCER, colCERAvg);
        	}
        	else if(m == PerformanceMetric.AVERAGEEPISODEREWARD){
        		this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Average Reward", "Episode", "Average Reward", colAER, colAERAvg);
        	}
        	else if(m == PerformanceMetric.MEDIANEPISODEREWARD){
        		this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Median Reward", "Episode", "Median Reward", colMER, colMERAvg);
        	}
        	else if(m == PerformanceMetric.CUMULATIVESTEPSPEREPISODE){
        		this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Cumulative Steps", "Episode", "Cumulative Steps", colCSE, colCSEAvg);
        	}
        	else if(m == PerformanceMetric.STEPSPEREPISODE){
        		this.insertChart(plotContainer, c, columns, chartWidth, chartHeight, "Number of Steps", "Episode", "Number of Steps", colSE, colSEAvg);
        	}
        	
        	
        }
        
        int totalChartHeight = ((metrics.length / columns)+1)*(chartHeight+10);
        if(totalChartHeight > maxWindowHeight){
			JScrollPane scrollPane = new JScrollPane(plotContainer);
			scrollPane.setPreferredSize(new Dimension(chartWidth*columns+50, maxWindowHeight));
			this.add(scrollPane);
        }
        else{
        	this.add(plotContainer);
        }
		
	}
	



	/**
	 * Adds the most recent trial (if enabled) chart and trial average (if enabled) chart into the provided container.
	 * The GridBagConstraints will aumatically be incremented to the next position after this method returns.
	 * @param plotContainer the contain in which to insert the plot(s).
	 * @param c the current grid bag contraint locaiton in which the plots should be inserted.
	 * @param columns the number of columns to fill in the plot container
	 * @param chartWidth the width of any single plot
	 * @param chartHeight the height of any single plot
	 * @param title the title to label thep plot; if average trial plots are enabled the word "Average" will be prepended to the title for the average plot.
	 * @param xlab the xlab axis of the plot
	 * @param ylab the y lab axis of the plot
	 * @param mostRecentCollection the XYSeriesCollection dataset with which the most recent trial plot is associated 
	 * @param averageCollection the YIntervalSeriesCollection dataset with which the trial average plot is associated
	 */
	protected void insertChart(Container plotContainer, GridBagConstraints c, int columns, int chartWidth, int chartHeight,
			String title, String xlab, String ylab, XYSeriesCollection mostRecentCollection, YIntervalSeriesCollection averageCollection){
		
		if(this.trialMode.mostRecentTrialEnabled()){
			final JFreeChart chartCSR = ChartFactory.createXYLineChart(title, xlab, ylab, mostRecentCollection);
			ChartPanel chartPanelCSR = new ChartPanel(chartCSR);
			chartPanelCSR.setPreferredSize(new java.awt.Dimension(chartWidth, chartHeight));
			plotContainer.add(chartPanelCSR, c);
			this.updateGBConstraint(c, columns);
		}
		
		if(this.trialMode.averagesEnabled()){
			final JFreeChart chartCSRAvg = ChartFactory.createXYLineChart("Average " + title, xlab, ylab, averageCollection);
			((XYPlot)chartCSRAvg.getPlot()).setRenderer(this.createDeviationRenderer());
			ChartPanel chartPanelCSRAvg = new ChartPanel(chartCSRAvg);
			chartPanelCSRAvg.setPreferredSize(new java.awt.Dimension(chartWidth, chartHeight));
			plotContainer.add(chartPanelCSRAvg, c);
			this.updateGBConstraint(c, columns);
		}
	}
	
	/**
	 * Creates a DeviationRenderer to use for the trial average plots
	 * @return a DeviationRenderer
	 */
	protected DeviationRenderer createDeviationRenderer(){
		DeviationRenderer renderer = new DeviationRenderer(true, false);
		
		for(int i = 0; i < DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE.length; i++){
			Color c = (Color)DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE[i];
			Color nc = new Color(c.getRed(), c.getGreen(), c.getBlue(), 100);
			renderer.setSeriesFillPaint(i, nc);
		}
		
		return renderer;
	}
	
	
	/**
	 * Increments the x-y position of a constraint to the next position.
	 * If there are still free columns in the current row, then the next position in the next column; otherwise a new row is started. 
	 * @param c the constraint to increment
	 * @param maxCol the maximum columns allowable in a container
	 */
	protected void updateGBConstraint(GridBagConstraints c, int maxCol){
		c.gridx++;
		if(c.gridx >= maxCol){
			c.gridx = 0;
			c.gridy++;
		}
	}
	
	/**
	 * Launches the GUI and automatic refresh thread.
	 */
	public void startGUI(){
		this.pack();
		this.setVisible(true);
		this.launchThread();
	}
	
	/**
	 * Launches the automatic plot refresh thread.
	 */
	protected void launchThread(){
		 Thread refreshThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					while(true){
						MultiAgentPerformancePlotter.this.updateTimeSeries();
						try {
							Thread.sleep(MultiAgentPerformancePlotter.this.delay);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
				}
			});
	        
	       refreshThread.start();
		 	
	}
	
	/**
	 * sets the delay in milliseconds between automatic refreshes of the plots
	 * @param delayInMS the refresh delay in milliseconds
	 */
	public void setRefreshDelay(int delayInMS){
		this.delay = delayInMS;
	}
	
	
	/**
	 * Sets the significance used for confidence intervals.
	 * The default is 0.05, which corresponds to a 95% confidence interval.
	 * @param signifcance the significance used for confidence intervals.
	 */
	public void setSignificanceForCI(double signifcance){
		this.significance = signifcance;
	}
	
	
	/**
	 * Toggle whether performance data collected from the action observation is recorded or not
	 * @param collectData true if data collected should be plotted; false if not.
	 */
	public void toggleDataCollection(boolean collectData){
		this.collectData = collectData;
	}


	@Override
	public void gameStarting(State s) {
		//do nothing
	}


	@Override
	synchronized public void observe(State s, JointAction ja, Map<String, Double> reward,
			State sp) {
		
		
		if(!this.collectData){
			return;
		}
		
		//do we need to instantiate the agents?
		if(this.agentWiseData.size() == 0){
			//then we need to instnaitate matters
			for(String agentName : reward.keySet()){
				this.agentWiseData.put(agentName, new DatasetsAndTrials(agentName));
			}
		}

		this.freshStart = false;
		
		
		boolean isTermainal = this.tf.isTerminal(sp);
		
		//update information for each agent
		for(Map.Entry<String, Double> e : reward.entrySet()){
			DatasetsAndTrials dt = this.agentWiseData.get(e.getKey());
			if(dt == null){
				throw new RuntimeException("Error: a new agent has been overseved (" + e.getKey() + ") who was not present in the start of the games. Unable to track performance with repsect to other agents.");
			}
			dt.getLatestTrial().stepIncrement(e.getValue());
			if(isTermainal){
				dt.getLatestTrial().setupForNewEpisode();
			}
		}
		this.curTimeStep++;
		if(isTermainal){
			this.curEpisode++;
		}
		
		
	}

	@Override
	public void gameEnding(State s) {
		//do nothing
	}
	
	/**
	 * Initializes the datastructures for a new trial. Nothing will happen if no data has been recorded in the last trial. This method
	 * will automatically end the current trial, finializing all data before the new trial is started.
	 */
	public void startNewTrial(){
		
		if(this.freshStart){
			return;
		}
		
		this.endTrial();
		
		if(this.curTimeStep > 0){
			this.needsClearing = true;
		}
		
		for(DatasetsAndTrials dt : this.agentWiseData.values()){
			dt.startNewTrial();
		}
		this.lastTimeStepUpdate = 0;
		this.lastEpisode = 0;
		this.curTimeStep = 0;
		this.curEpisode = 0;
		
		this.freshStart = true;
	}
	
	
	/**
	 * Writes the step-wise and episode-wise data to CSV files.
	 * The episode-wise data will be saved to the file &lt;pathAndBaseNameToUse&gt;Episodes.csv. The step-wise data will
	 * be saved to the file &lt;pathAndBaseNameToUse&gt;Steps.csv
	 * @param pathAndBaseNameToUse the base path and file name for the episode-wise and step-wise csv files.
	 */
	public void writeStepAndEpisodeDataToCSV(String pathAndBaseNameToUse){
		
		if(pathAndBaseNameToUse.endsWith(".csv")){
			pathAndBaseNameToUse = pathAndBaseNameToUse.substring(0, pathAndBaseNameToUse.length()-4);
		}
		
		this.writeStepDataToCSV(pathAndBaseNameToUse+"Steps.csv");
		this.writeEpisodeDataToCSV(pathAndBaseNameToUse+"Episodes.csv");
	}
	
	
	/**
	 * Writes the step-wise data to a csv file.
	 * If the file path does not include the .csv extension, it will automatically be added.
	 * @param filePath the path to the csv file to write to.
	 */
	public void writeStepDataToCSV(String filePath){
		
		if(!filePath.endsWith(".csv")){
			filePath = filePath + ".csv";
		}
		
		try {
			BufferedWriter outStep = new BufferedWriter(new FileWriter(filePath));
			
			//create header
			outStep.write("agent,trial,step,cumulativeReward\n");
			
			for(DatasetsAndTrials dt : this.agentWiseData.values()){
				String aname = dt.agentName;
				List<Trial> trials = dt.trials;
				for(int i = 0; i < trials.size(); i++){
					Trial trial = trials.get(i);
					for(int j = 0; j < trial.totalSteps; j++){
						outStep.write(aname+","+i+","+j+","+trial.cumulativeStepReward.get(j)+"\n");
					}
				}
			}
			
			outStep.close();
			DPrint.cl(this.debugCode, "Finished writing step csv file to: " + filePath);
			
		} catch (Exception e) {
			System.err.println("Could not write csv file to: " + filePath);
			e.printStackTrace();
		}
		
		
		
	}
	
	
	/**
	 * Writes the episode-wise data to a csv file.
	 * If the file path does not include the .csv extension, it will automatically be added.
	 * @param filePath the path to the csv file to write to.
	 */
	public void writeEpisodeDataToCSV(String filePath){
		
		if(!filePath.endsWith(".csv")){
			filePath = filePath + ".csv";
		}
		
		try {
			BufferedWriter outEpisode = new BufferedWriter(new FileWriter(filePath));
			
			//create header
			outEpisode.write("agent,trial,episode,cumulativeReward,averageReward,cumulativeSteps,numSteps\n");
			
			for(DatasetsAndTrials dt : this.agentWiseData.values()){
				String aname = dt.agentName;
				List<Trial> trials = dt.trials;
				for(int i = 0; i < trials.size(); i++){
					Trial trial = trials.get(i);
					for(int j = 0; j < trial.totalEpisodes; j++){
						outEpisode.write(aname+","+i+","+j);
						outEpisode.write(","+trial.cumulativeEpisodeReward.get(j));
						outEpisode.write(","+trial.averageEpisodeReward.get(j));
						outEpisode.write(","+trial.cumulativeStepEpisode.get(j));
						outEpisode.write(","+trial.stepEpisode.get(j));
						outEpisode.write("\n");
					}
				}
			}
			
			
			outEpisode.close();
			DPrint.cl(this.debugCode, "Finished writing episode csv file to: " + filePath);
			
		} catch (Exception e) {
			System.err.println("Could not write csv file to: " + filePath);
			e.printStackTrace();
		}
		
	}
	
	
	
	
	/**
	 * Ends the current trial data and updates the plots accordingly.
	 */
	protected void endTrial(){
		this.trialUpdateComplete.b = false;
		for(DatasetsAndTrials dt : this.agentWiseData.values()){
			if(!dt.getLatestTrial().hasFinishedLastEpisode()){
				dt.getLatestTrial().setupForNewEpisode();
			}
		}
		this.updateTimeSeries();
		
			
		//wait until it's updated before allowing anything else to happen
		synchronized (this.trialUpdateComplete) {
			while(this.trialUpdateComplete.b == false){
				try {
					this.trialUpdateComplete.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.trialUpdateComplete.notifyAll();
			
		}
	}
	
	
	/**
	 * Specifies that all trials are complete and that the average trial results and error bars should be plotted.
	 */
	public void endAllTrials(){
		
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				
				synchronized (MultiAgentPerformancePlotter.this) {
				
					MultiAgentPerformancePlotter.this.endAllTrialsHelper();
				}	
				
			}
		});
	}
	
	
	/**
	 * The end all trial methods helper called at the end of a swing update.
	 */
	protected void endAllTrialsHelper(){
		
		for(DatasetsAndTrials dt : this.agentWiseData.values()){
			if(!dt.getLatestTrial().hasFinishedLastEpisode()){
				dt.getLatestTrial().setupForNewEpisode();
			}
		}
		this.updateMostRecentSeriesHelper();
		
		
		if(!this.trialMode.averagesEnabled()){
			return ;
		}
		
		for(String agentName : this.agentWiseData.keySet()){
			this.endAllTrialsForAgent(agentName);
		}
		
		
	}
	
	/**
	 * Ends all the trials, plotting the average trial data for the agent with the given name
	 * @param agentName the name of the agent whose trial data will be completed and plotted
	 */
	protected void endAllTrialsForAgent(String agentName){
		
		
		DatasetsAndTrials dt = this.agentWiseData.get(agentName);
		List<Trial> trials = dt.trials;
		int [] n = MultiAgentPerformancePlotter.this.minStepAndEpisodes(trials);
		
		
		if(this.metricsSet.contains(PerformanceMetric.CUMULATIVEREWARDPERSTEP)){
			for(int i = 0; i < n[0]; i++){
				DescriptiveStatistics avgi = new DescriptiveStatistics();
				for(Trial t : trials){
					avgi.addValue(t.cumulativeStepReward.get(i));
				}
				double [] ci = getCI(avgi, this.significance);
				dt.datasets.csrAvgSeries.add(i, ci[0], ci[1], ci[2]);
			}
		}
		
		
		if(this.metricsSet.contains(PerformanceMetric.CUMULTAIVEREWARDPEREPISODE)){
			for(int i = 0; i < n[1]; i++){
				DescriptiveStatistics avgi = new DescriptiveStatistics();
				for(Trial t : trials){
					avgi.addValue(t.cumulativeEpisodeReward.get(i));
				}
				double [] ci = getCI(avgi, this.significance);
				dt.datasets.cerAvgSeries.add(i, ci[0], ci[1], ci[2]);
			}
		}


		if(this.metricsSet.contains(PerformanceMetric.AVERAGEEPISODEREWARD)){
			for(int i = 0; i < n[1]; i++){
				DescriptiveStatistics avgi = new DescriptiveStatistics();
				for(Trial t : trials){
					avgi.addValue(t.averageEpisodeReward.get(i));
				}
				double [] ci = getCI(avgi, this.significance);
				dt.datasets.aerAvgSeries.add(i, ci[0], ci[1], ci[2]);
			}
		}

		if(this.metricsSet.contains(PerformanceMetric.MEDIANEPISODEREWARD)){
			for(int i = 0; i < n[1]; i++){
				DescriptiveStatistics avgi = new DescriptiveStatistics();
				for(Trial t : trials){
					avgi.addValue(t.medianEpisodeReward.get(i));
				}
				double [] ci = getCI(avgi, this.significance);
				dt.datasets.merAvgSeries.add(i, ci[0], ci[1], ci[2]);
			}
		}

		if(this.metricsSet.contains(PerformanceMetric.CUMULATIVESTEPSPEREPISODE)){
			for(int i = 0; i < n[1]; i++){
				DescriptiveStatistics avgi = new DescriptiveStatistics();
				for(Trial t : trials){
					avgi.addValue(t.cumulativeStepEpisode.get(i));
				}
				double [] ci = getCI(avgi, this.significance);
				dt.datasets.cseAvgSeries.add(i, ci[0], ci[1], ci[2]);
			}
		}


		if(this.metricsSet.contains(PerformanceMetric.STEPSPEREPISODE)){
			for(int i = 0; i < n[1]; i++){
				DescriptiveStatistics avgi = new DescriptiveStatistics();
				for(Trial t : trials){
					avgi.addValue(t.stepEpisode.get(i));
				}
				double [] ci = getCI(avgi, this.significance);
				dt.datasets.seAvgSeries.add(i, ci[0], ci[1], ci[2]);
			}
		}
		
		dt.datasets.fireAllAverages();
		
	}
	
	
	
	/**
	 * Updates all the most recent trial time series with the latest data
	 */
	synchronized protected void updateTimeSeries(){
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				
				if(MultiAgentPerformancePlotter.this.trialMode.mostRecentTrialEnabled()){
					synchronized (MultiAgentPerformancePlotter.this) {
						
						synchronized (MultiAgentPerformancePlotter.this.trialUpdateComplete) {
							
							MultiAgentPerformancePlotter.this.updateMostRecentSeriesHelper();
							
							
							MultiAgentPerformancePlotter.this.trialUpdateComplete.b = true;
							MultiAgentPerformancePlotter.this.trialUpdateComplete.notifyAll();
							
						}
						
						
					}
				}
				
				
				
			}
		});

	}
	
	
	/**
	 * Updates the series data for the most recent trial plots.
	 */
	protected void updateMostRecentSeriesHelper(){
		if(MultiAgentPerformancePlotter.this.needsClearing){
			for(DatasetsAndTrials dt : MultiAgentPerformancePlotter.this.agentWiseData.values()){
				dt.datasets.clearNonAverages();
			}
			MultiAgentPerformancePlotter.this.needsClearing = false;
		}
		
		if(MultiAgentPerformancePlotter.this.curTimeStep > MultiAgentPerformancePlotter.this.lastTimeStepUpdate){
			for(DatasetsAndTrials dt : MultiAgentPerformancePlotter.this.agentWiseData.values()){
				MultiAgentPerformancePlotter.this.updateCSRSeries(dt);
			}
			MultiAgentPerformancePlotter.this.lastTimeStepUpdate = curTimeStep;
		}
		if(MultiAgentPerformancePlotter.this.curEpisode > MultiAgentPerformancePlotter.this.lastEpisode){
			for(DatasetsAndTrials dt : MultiAgentPerformancePlotter.this.agentWiseData.values()){
				MultiAgentPerformancePlotter.this.updateCERSeries(dt);
				MultiAgentPerformancePlotter.this.updateAERSeris(dt);
				MultiAgentPerformancePlotter.this.updateMERSeris(dt);
				MultiAgentPerformancePlotter.this.updateCSESeries(dt);
				MultiAgentPerformancePlotter.this.updateSESeries(dt);
			}
			
			MultiAgentPerformancePlotter.this.lastEpisode = MultiAgentPerformancePlotter.this.curEpisode;
		}
	}
	
	
	
	
	
	
	
	
	/**
	 * Returns the minimum steps and episodes across all trials
	 * @param trials the trials to perform the min over
	 * @return a double array of length 2; the first entry is the minimum steps, the second entry tthe minimum episodes
	 */
	protected int [] minStepAndEpisodes(List<Trial> trials){
		int minStep = Integer.MAX_VALUE;
		int minEpisode = Integer.MAX_VALUE;
		
		for(Trial t : trials){
			minStep = Math.min(minStep, t.totalSteps);
			minEpisode = Math.min(minEpisode, t.totalEpisodes);
		}
		
		return new int[]{minStep,minEpisode};
	}
	
	
	/**
	 * Computes the sum of the last entry in list and the value v and adds it to the end of list. Use for maintainly cumulative data.
	 * @param list the list to add and append to.
	 * @param v the value to add to the last value of list and append
	 */
	protected static void accumulate(List<Double> list, double v){
		if(list.size() > 0){
			v += list.get(list.size()-1);
		}
		list.add(v);
	}
	
	/**
	 * Returns the confidence interval for the specified significance level
	 * @param stats the summary including the array of data for which the confidence interval is to be returned
	 * @param significanceLevel the significance level required
	 * @return a double array of length three in the form: {mean, lowerBound, upperBound}
	 */
	public static double [] getCI(DescriptiveStatistics stats, double significanceLevel){
		
		int n = (int)stats.getN();
		Double critD = cachedCriticalValues.get(n-1);
		if(critD == null){
			TDistribution tdist = new TDistribution(stats.getN()-1);
			double crit = tdist.inverseCumulativeProbability(1. - (significanceLevel/2.));
			critD = crit;
			cachedCriticalValues.put(n-1, critD);
		}
		double crit = critD;
		double width = crit * stats.getStandardDeviation() / Math.sqrt(stats.getN());
		double m = stats.getMean();
		return new double[]{m, m-width, m+width};
	}
	
	
	
	/**
	 * Updates the cumulative reward by step series. Does nothing if that metric is not being plotted.
	 */
	protected void updateCSRSeries(DatasetsAndTrials agentData){
		
		if(!this.metricsSet.contains(PerformanceMetric.CUMULATIVEREWARDPERSTEP)){
			return ;
		}
		
		int n = agentData.getLatestTrial().cumulativeStepReward.size();
		for(int i = this.lastTimeStepUpdate; i < n; i++){
			agentData.datasets.cumulativeStepRewardSeries.add((double)i, agentData.getLatestTrial().cumulativeStepReward.get(i), false);
		}
		if(n > this.lastTimeStepUpdate){
			agentData.datasets.cumulativeStepRewardSeries.fireSeriesChanged();
		}
	}


	/**
	 * Updates the cumulative reward by episode series.  Does nothing if that metric is not being plotted.
	 */
	protected void updateCERSeries(DatasetsAndTrials agentData){
		
		if(!this.metricsSet.contains(PerformanceMetric.CUMULTAIVEREWARDPEREPISODE)){
			return ;
		}
		
		int n = agentData.getLatestTrial().cumulativeEpisodeReward.size();
		for(int i = this.lastEpisode; i < n; i++){
			agentData.datasets.cumulativeEpisodeRewardSeries.add((double)i, agentData.getLatestTrial().cumulativeEpisodeReward.get(i), false);
		}
		if(n > this.lastEpisode){
			agentData.datasets.cumulativeEpisodeRewardSeries.fireSeriesChanged();
		}
		
	}


	/**
	 * Updates the average reward by episode series.  Does nothing if that metric is not being plotted.
	 */
	protected void updateAERSeris(DatasetsAndTrials agentData){
		
		if(!this.metricsSet.contains(PerformanceMetric.AVERAGEEPISODEREWARD)){
			return ;
		}
		
		int n = agentData.getLatestTrial().averageEpisodeReward.size();
		for(int i = this.lastEpisode; i < n; i++){
			agentData.datasets.averageEpisodeRewardSeries.add((double)i, agentData.getLatestTrial().averageEpisodeReward.get(i), false);
		}
		if(n > this.lastEpisode){
			agentData.datasets.averageEpisodeRewardSeries.fireSeriesChanged();
		}
	}


	/**
	 * Updates the median reward by episode series.  Does nothing if that metric is not being plotted.
	 */
	protected void updateMERSeris(DatasetsAndTrials agentData){
		
		if(!this.metricsSet.contains(PerformanceMetric.MEDIANEPISODEREWARD)){
			return ;
		}
		
		int n = agentData.getLatestTrial().medianEpisodeReward.size();
		for(int i = this.lastEpisode; i < n; i++){
			agentData.datasets.medianEpisodeRewardSeries.add((double)i, agentData.getLatestTrial().medianEpisodeReward.get(i), false);
		}
		if(n > this.lastEpisode){
			agentData.datasets.medianEpisodeRewardSeries.fireSeriesChanged();
		}
	}


	/**
	 * Updates the cumulative steps by episode series.  Does nothing if that metric is not being plotted.
	 */
	protected void updateCSESeries(DatasetsAndTrials agentData){
		
		if(!this.metricsSet.contains(PerformanceMetric.CUMULATIVESTEPSPEREPISODE)){
			return ;
		}
		
		int n = agentData.getLatestTrial().cumulativeStepEpisode.size();
		for(int i = this.lastEpisode; i < n; i++){
			agentData.datasets.cumulativeStepEpisodeSeries.add((double)i, agentData.getLatestTrial().cumulativeStepEpisode.get(i), false);
		}
		if(n > this.lastEpisode){
			agentData.datasets.cumulativeStepEpisodeSeries.fireSeriesChanged();
		}
	}


	/**
	 * Updates the steps by episode series.  Does nothing if that metric is not being plotted.
	 */
	protected void updateSESeries(DatasetsAndTrials agentData){
		
		if(!this.metricsSet.contains(PerformanceMetric.STEPSPEREPISODE)){
			return ;
		}
		
		int n = agentData.getLatestTrial().stepEpisode.size();
		for(int i = this.lastEpisode; i < n; i++){
			agentData.datasets.stepEpisodeSeries.add((double)i, agentData.getLatestTrial().stepEpisode.get(i), false);
		}
		if(n > this.lastEpisode){
			agentData.datasets.stepEpisodeSeries.fireSeriesChanged();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * A class for storing the tiral data and series datasets for a given agent.
	 * @author James MacGlashan
	 *
	 */
	protected class DatasetsAndTrials{
		/**
		 * The name of the agent
		 */
		public String agentName;
		
		/**
		 * All the trials with this agent
		 */
		public List<Trial> trials;
		
		/**
		 * The series datasets for this agent
		 */
		public AgentDatasets datasets;
		
		/**
		 * Initializes for an agent with the given name.
		 * @param agentName the name of the agent
		 */
		public DatasetsAndTrials(String agentName){
			this.agentName = agentName;
			this.trials = new ArrayList<MultiAgentPerformancePlotter.Trial>();
			this.trials.add(new Trial());
			this.datasets = new AgentDatasets(agentName);
		}
		
		/**
		 * Returns the most recent {@link Trial} object
		 * @return the most recent {@link Trial} object
		 */
		public Trial getLatestTrial(){
			return this.trials.get(trials.size()-1);
		}
		
		/**
		 * Creates a new trial object and adds it to the end of the list of trials.
		 */
		public void startNewTrial(){
			this.trials.add(new Trial());
		}
		
	}
	
	
	
	
	
	/**
	 * A datastructure for maintaining all the metric stats for a single trial.
	 * @author James MacGlashan
	 *
	 */
	protected class Trial{
		
		/**
		 * Stores the cumulative reward by step
		 */
		public List<Double> cumulativeStepReward = new ArrayList<Double>();
		
		/**
		 * Stores the cumulative reward by episode
		 */
		public List<Double> cumulativeEpisodeReward = new ArrayList<Double>();
		
		/**
		 * Stores the average reward by episode
		 */
		public List<Double> averageEpisodeReward = new ArrayList<Double>();
		
		/**
		 * Stores the median reward by episode
		 */
		public List<Double> medianEpisodeReward = new ArrayList<Double>();
		
		
		/**
		 * Stores the cumulative steps by episode
		 */
		public List<Double> cumulativeStepEpisode = new ArrayList<Double>();
		
		/**
		 * Stores the steps by episode
		 */
		public List<Double> stepEpisode = new ArrayList<Double>();
		
		
		/**
		 * The cumulative reward of the episode so far
		 */
		public double curEpisodeReward = 0.;
		
		/**
		 * The number of steps in the episode so far
		 */
		public int curEpisodeSteps = 0;
		
		/**
		 * the total number of steps in the trial
		 */
		public int totalSteps = 0;
		
		/**
		 * The total number of episodes in the trial
		 */
		public int totalEpisodes = 0;
		
		
		/**
		 * A list of the reward sequence in the current episode
		 */
		protected List<Double> curEpisodeRewards = new ArrayList<Double>();
		
		
		protected boolean hasFinishedLastEpisode = true;
		
		
		
		/**
		 * Updates all datastructures with the reward received from the last step
		 * @param r the last reward received
		 */
		public void stepIncrement(double r){
			
			accumulate(this.cumulativeStepReward, r);
			this.curEpisodeReward += r;
			this.curEpisodeSteps++;
			this.curEpisodeRewards.add(r);
			this.hasFinishedLastEpisode = false;

			
		}
		
		
		/**
		 * Completes the last episode and sets up the datastructures for the next episode
		 */
		public void setupForNewEpisode(){
			accumulate(this.cumulativeEpisodeReward, this.curEpisodeReward);
			accumulate(this.cumulativeStepEpisode, this.curEpisodeSteps);
			
			double avgER = this.curEpisodeReward / (double)this.curEpisodeSteps;
			this.averageEpisodeReward.add(avgER);
			this.stepEpisode.add((double)this.curEpisodeSteps);
			
			Collections.sort(this.curEpisodeRewards);
			double med = 0.;
			if(this.curEpisodeSteps > 0){
				int n2 = this.curEpisodeSteps / 2;
				if(this.curEpisodeSteps % 2 == 0){
					double m = this.curEpisodeRewards.get(n2);
					double m2 = this.curEpisodeRewards.get(n2-1);
					med = (m + m2) / 2.;
				}
				else{
					med = this.curEpisodeRewards.get(n2);
				}
			}
			
			this.medianEpisodeReward.add(med);
			
			
			this.totalSteps += this.curEpisodeSteps;
			this.totalEpisodes++;
			
			this.curEpisodeReward = 0.;
			this.curEpisodeSteps = 0;
			
			this.curEpisodeRewards.clear();
			
			this.hasFinishedLastEpisode = true;
			
		}
		
		/**
		 * Indicates whether the data finalization for the current episode has been performed.
		 * @return true if all the episode data for the current epidoe has been finalized, false otherwise
		 */
		public boolean hasFinishedLastEpisode(){
			return this.hasFinishedLastEpisode;
		}
		
	}
	
	
	/**
	 * A datastructure for maintain the plot series data in the current agent
	 * @author James MacGlashan
	 *
	 */
	protected class AgentDatasets{
		
		
		/**
		 * Most recent trial's cumulative reward per step series data
		 */
		public XYSeries 		cumulativeStepRewardSeries;
		
		/**
		 * Most recent trial's cumulative reward per step episode data
		 */
		public XYSeries 		cumulativeEpisodeRewardSeries;
		
		/**
		 * Most recent trial's average reward per step episode data
		 */
		public XYSeries 		averageEpisodeRewardSeries;
		
		/**
		 * Most recent trial's median reward per step episode data
		 */
		public XYSeries			medianEpisodeRewardSeries;
		
		
		/**
		 * Most recent trial's cumulative steps per step episode data
		 */
		public XYSeries 		cumulativeStepEpisodeSeries;
		
		/**
		 * Most recent trial's steps per step episode data
		 */
		public XYSeries 		stepEpisodeSeries;
		
		
		/**
		 * All trial's average cumulative reward per step series data
		 */
		public YIntervalSeries	csrAvgSeries;
		
		/**
		 * All trial's average cumulative reward per episode series data
		 */
		public YIntervalSeries	cerAvgSeries;
		
		/**
		 * All trial's average average reward per episode series data
		 */
		public YIntervalSeries	aerAvgSeries;
		
		/**
		 * All trial's average median reward per episode series data
		 */
		public YIntervalSeries	merAvgSeries;
		
		/**
		 * All trial's average cumulative steps per episode series data
		 */
		public YIntervalSeries	cseAvgSeries;
		
		/**
		 * All trial's average steps per episode series data
		 */
		public YIntervalSeries	seAvgSeries;
		
		
		/**
		 * Initializes the datastructures for an agent with the given name
		 */
		public AgentDatasets(String agentName){
			this.cumulativeStepRewardSeries = new XYSeries(agentName);
			colCSR.addSeries(this.cumulativeStepRewardSeries);
			
			this.cumulativeEpisodeRewardSeries = new XYSeries(agentName);
			colCER.addSeries(this.cumulativeEpisodeRewardSeries);
			
			this.averageEpisodeRewardSeries = new XYSeries(agentName);
			colAER.addSeries(this.averageEpisodeRewardSeries);
			
			this.cumulativeStepEpisodeSeries = new XYSeries(agentName);
			colCSE.addSeries(this.cumulativeStepEpisodeSeries);
			
			this.stepEpisodeSeries = new XYSeries(agentName);
			colSE.addSeries(this.stepEpisodeSeries);
			
			this.medianEpisodeRewardSeries = new XYSeries(agentName);
			colMER.addSeries(this.medianEpisodeRewardSeries);
			
			
			this.csrAvgSeries = new YIntervalSeries(agentName);
			this.csrAvgSeries.setNotify(false);
			colCSRAvg.addSeries(this.csrAvgSeries);
			
			this.cerAvgSeries = new YIntervalSeries(agentName);
			this.cerAvgSeries.setNotify(false);
			colCERAvg.addSeries(this.cerAvgSeries);
			
			this.aerAvgSeries = new YIntervalSeries(agentName);
			this.aerAvgSeries.setNotify(false);
			colAERAvg.addSeries(this.aerAvgSeries);
			
			this.merAvgSeries = new YIntervalSeries(agentName);
			this.merAvgSeries.setNotify(false);
			colMERAvg.addSeries(this.merAvgSeries);
			
			this.cseAvgSeries = new YIntervalSeries(agentName);
			this.cseAvgSeries.setNotify(false);
			colCSEAvg.addSeries(this.cseAvgSeries);
			
			this.seAvgSeries = new YIntervalSeries(agentName);
			this.seAvgSeries.setNotify(false);
			colSEAvg.addSeries(this.seAvgSeries);
			
		}
		
		
		/**
		 * clears all the series data for the most recent trial.
		 */
		public void clearNonAverages(){
			this.cumulativeStepRewardSeries.clear();
			this.cumulativeEpisodeRewardSeries.clear();
			this.averageEpisodeRewardSeries.clear();
			this.medianEpisodeRewardSeries.clear();
			this.cumulativeStepEpisodeSeries.clear();
			this.stepEpisodeSeries.clear();
			this.medianEpisodeRewardSeries.clear();
		}
		
		
		/**
		 * Causes all average trial data series to tell their plots that they've updated and need to be refreshed
		 */
		public void fireAllAverages(){
			this.csrAvgSeries.setNotify(true);
			this.csrAvgSeries.fireSeriesChanged();
			this.csrAvgSeries.setNotify(false);
			
			this.cerAvgSeries.setNotify(true);
			this.cerAvgSeries.fireSeriesChanged();
			this.cerAvgSeries.setNotify(false);
			
			this.aerAvgSeries.setNotify(true);
			this.aerAvgSeries.fireSeriesChanged();
			this.aerAvgSeries.setNotify(false);
			
			this.merAvgSeries.setNotify(true);
			this.merAvgSeries.fireSeriesChanged();
			this.merAvgSeries.setNotify(false);
			
			this.cseAvgSeries.setNotify(true);
			this.cseAvgSeries.fireSeriesChanged();
			this.cseAvgSeries.setNotify(false);
			
			this.seAvgSeries.setNotify(true);
			this.seAvgSeries.fireSeriesChanged();
			this.seAvgSeries.setNotify(false);
		}

		
	}
	
	
	
	/**
	 * A class for a mutable boolean
	 * @author James MacGlashan
	 *
	 */
	protected class MutableBoolean{
		
		/**
		 * The boolean value
		 */
		public boolean b;
		
		/**
		 * Initializes with the given Boolean value
		 * @param b
		 */
		public MutableBoolean(boolean b){
			this.b = b;
		}
	}

}
