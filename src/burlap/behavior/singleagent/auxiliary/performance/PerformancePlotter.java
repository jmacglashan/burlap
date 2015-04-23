package burlap.behavior.singleagent.auxiliary.performance;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

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

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.ActionObserver;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;



/**
 * This class is an action observer used to collect and plot performance data of a learning agent either by itself or against another learning agent. There are 
 * six possible performance metrics that can be used to plot performance, as specified in the {@link PerformanceMetric} enumerator.  A plot showing the most recent
 * "trial" of an agent can be displayed, the average of the metric over all trials with confidence intervals, or both may be displayed; which plots are shown is specfied by the
 * {@link TrialMode} enumerator.
 * Any subset of these metrics
 * may be displayed in any order specified by the user and plots are displayed in a matrix format with a maximum number of columns that are filled out first.
 * If the number of plots would cause a window height larger than a maximimum specified, then the plots are placed in a scroll view.
 * <p/>
 * The way this class should be used is the constructor is first called. Then the {@link #startGUI()} method. A trial indicates a single evaluation of
 * a learning algorithm for some number of steps or episodes. Multiple trials are used to produce the average trial plots. Each trial should reinitialize
 * the learning algorithm so that it learns from scratch. At the start of each trial the {@link #startNewTrial()} method should be called. At the end
 * of each episode in a trial, the {@link #endEpisode()} method should be called. At the end of a trial the {@link #endTrial()} method should be called. When
 * all trials for the current agent are complete and a new agent is to tested to be compared, the {@link #startNewAgent(String)} method should be called,
 * providing the name of the new agent to be tested. Since the constructor takes the name of the first agent, this method does not have to be called for
 * the first agent. When all testing for all agents is complete, a call to the {@link #endTrialsForCurrentAgent()} method should be made.
 * <p/>
 * To ensure proper use of this class, it is highly reccomended that the {@link LearningAlgorithmExperimenter} class is used, since it handles all of these
 * method calls behind the scenes.
 * <p/>
 * When testing is done, you may optionally request all data to be printed to CSV files. One CSV file will produce the step-wise performance
 * metric (cumulaitve reward by step) for all agents and trials. Another will produce all the episode-wise performance metric data. This data
 * can be produced regardless of which metrics you requested to be plotted.
 * <p/>
 * Note that the plots that are created have a number of interactive options. Try right-clicking on them to see the list of things you can modfiy in the GUI.
 * 
 * 
 * @author James MacGlashan
 *
 */
public class PerformancePlotter extends JFrame implements ActionObserver {

	private static final long serialVersionUID = 1L;
	
	
	private static final Map<Integer, Double> cachedCriticalValues = new HashMap<Integer, Double>();
	
	
	/**
	 * The reward funciton used to measure performance.
	 */
	protected RewardFunction rf;
	
	/**
	 * Contains all the current trial performance data
	 */
	protected Trial	curTrial;
	
	/**
	 * contains the plot series data that will be displayed for the current agent
	 */
	protected AgentDatasets	curAgentDatasets;
	
	
	/**
	 * contains all trial data for each agent
	 */
	protected Map<String, List<Trial>> agentTrials;
	
	
	/**
	 * The name of the current agent being tested
	 */
	protected String curAgentName;
	
	
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
	protected boolean collectData = false;
	
	
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
	 * Synchronization object to ensure proper threaded plot updating
	 */
	protected MutableBoolean trialUpdateComplete = new MutableBoolean(true);
	
	
	
	/**
	 * Initializes a performance plotter.
	 * @param firstAgentName the name of the first agent whose performance will be measured.
	 * @param rf the reward function used to meausure performance
	 * @param chartWidth the width of each chart/plot
	 * @param chartHeight the height of each chart//plot
	 * @param columns the number of columns of the plots displayed. Plots are filled in columns first, then move down the next row.
	 * @param maxWindowHeight the maximum window height allowed before a scroll view is used.
	 * @param trialMode which plots to use; most recent trial, average over all trials, or both. If both, the most recent plot will be inserted into the window first, then the average.
	 * @param metrics the metrics that should be plotted. The metrics will appear in the window in the order that they are specified (columns first)
	 */
	public PerformancePlotter(String firstAgentName, RewardFunction rf, int chartWidth, int chartHeight, int columns, int maxWindowHeight, 
								TrialMode trialMode, PerformanceMetric...metrics){
		
		this.rf = rf;
		this.curAgentName = firstAgentName;
		
		this.agentTrials = new HashMap<String, List<Trial>>();
		this.agentTrials.put(this.curAgentName, new ArrayList<PerformancePlotter.Trial>());
		
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
		
		this.curTrial = new Trial();
		this.curAgentDatasets = new AgentDatasets(curAgentName);
        
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
	
	
	/**
	 * Launches the GUI and automatic refresh thread.
	 */
	public void startGUI(){
		this.pack();
		this.setVisible(true);
		this.launchThread();
	}
	
	
	@Override
	synchronized public void actionEvent(State s, GroundedAction ga, State sp) {
		
		if(!this.collectData){
			return;
		}
		
		double r = this.rf.reward(s, ga, sp);
		this.curTrial.stepIncrement(r);
		this.curTimeStep++;
	}
	
	
	
	/**
	 * Informs the plotter that all data for the last episode has been collected.
	 */
	synchronized public void endEpisode(){
		this.curTrial.setupForNewEpisode();
		this.curEpisode++;
	}
	
	
	/**
	 * Informs the plotter that a new trial of the current agent is beginning.
	 */
	synchronized public void startNewTrial(){

		if(this.curTimeStep > 0){
			this.needsClearing = true;
		}
		
		this.curTrial = new Trial();
		this.lastTimeStepUpdate = 0;
		this.lastEpisode = 0;
		this.curTimeStep = 0;
		this.curEpisode = 0;
		
		
	}
	
	/**
	 * Informs the plotter that all data for the current trial as been collected.
	 */
	public void endTrial(){
		

		this.trialUpdateComplete.b = false;
		this.updateTimeSeries();
		this.agentTrials.get(this.curAgentName).add(curTrial);
		
			
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
	 * Informs the plotter that data collecton for a new agent should begin.
	 * If the current agent is already set to the agent name provided, then a warning message is printed and nothing changes.
	 * @param agentName the name of the agent
	 */
	synchronized public void startNewAgent(final String agentName){
		
		if(this.curAgentName.equals(agentName)){
			System.out.println("Already recording data for: " + agentName + "; noting to change from startNewAgent method call.");
			return;
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				
				synchronized (PerformancePlotter.this) {
				
					PerformancePlotter.this.endTrialsForCurrentAgent();
					
					
					PerformancePlotter.this.curAgentName = agentName;
					PerformancePlotter.this.agentTrials.put(PerformancePlotter.this.curAgentName, new ArrayList<PerformancePlotter.Trial>());
					PerformancePlotter.this.curAgentDatasets = new AgentDatasets(curAgentName);
						
						
				}
					
					
				
			}
		});
		
		
		
	}
	
	
	
	
	
	
	/**
	 * Informs the plotter that all data for all agents has been collected.
	 * Will also cause the average plots for the last agent's data to be plotted.
	 */
	synchronized public void endAllAgents(){
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				
				synchronized (PerformancePlotter.this) {
				
					PerformancePlotter.this.endTrialsForCurrentAgent();
				}	
				
			}
		});
		
	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * Writes the step-wise and episode-wise data to CSV files.
	 * The episode-wise data will be saved to the file <pathAndBaseNameToUse>Episodes.csv. The step-wise data will
	 * be saved to the file <pathAndBaseNameToUse>Steps.csv
	 * @param pathAndBaseNameToUse the base path and file name for the epsidoe-wise and step-wise csv files.
	 */
	public void writeStepAndEpisodeDataToCSV(String pathAndBaseNameToUse){
		
		if(pathAndBaseNameToUse.endsWith(".csv")){
			pathAndBaseNameToUse = pathAndBaseNameToUse.substring(0, pathAndBaseNameToUse.length()-4);
		}
		
		try {
			BufferedWriter outStep = new BufferedWriter(new FileWriter(pathAndBaseNameToUse + "Steps.csv"));
			BufferedWriter outEpisode = new BufferedWriter(new FileWriter(pathAndBaseNameToUse + "Episodes.csv"));
			
			//create header
			outStep.write("agent,trial,step,cumulativeReward\n");
			outEpisode.write("agent,trial,episode,cumulativeReward,averageReward,cumulativeSteps,numSteps\n");
			
			for(Map.Entry<String, List<Trial>> e : this.agentTrials.entrySet()){
				String aname = e.getKey();
				List<Trial> trials = e.getValue();
				for(int i = 0; i < trials.size(); i++){
					Trial trial = trials.get(i);
					for(int j = 0; j < trial.totalSteps; j++){
						outStep.write(aname+","+i+","+j+","+trial.cumulativeStepReward.get(j)+"\n");
					}
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
			
			outStep.close();
			outEpisode.close();
			
		} catch (Exception e) {
			System.err.println("Could not write csv files to: " + pathAndBaseNameToUse + "Steps.csv and " + pathAndBaseNameToUse + "Episode.csv");
			e.printStackTrace();
		}
		
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
			
			for(Map.Entry<String, List<Trial>> e : this.agentTrials.entrySet()){
				String aname = e.getKey();
				List<Trial> trials = e.getValue();
				for(int i = 0; i < trials.size(); i++){
					Trial trial = trials.get(i);
					for(int j = 0; j < trial.totalSteps; j++){
						outStep.write(aname+","+i+","+j+","+trial.cumulativeStepReward.get(j)+"\n");
					}
				}
			}
			
			outStep.close();
			
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
			
			for(Map.Entry<String, List<Trial>> e : this.agentTrials.entrySet()){
				String aname = e.getKey();
				List<Trial> trials = e.getValue();
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
			
		} catch (Exception e) {
			System.err.println("Could not write csv file to: " + filePath);
			e.printStackTrace();
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
	 * Launches the automatic plot refresh thread.
	 */
	protected void launchThread(){
		 Thread refreshThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					while(true){
						PerformancePlotter.this.updateTimeSeries();
						try {
							Thread.sleep(PerformancePlotter.this.delay);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
				}
			});
	        
	       refreshThread.start();
		 	
	}
	
	
	/**
	 * Updates all the most recent trial time series with the latest data
	 */
	synchronized protected void updateTimeSeries(){
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				
				if(PerformancePlotter.this.trialMode.mostRecentTrialEnabled()){
					synchronized (PerformancePlotter.this) {
						
						synchronized (PerformancePlotter.this.trialUpdateComplete) {
							
							if(PerformancePlotter.this.needsClearing){
								PerformancePlotter.this.curAgentDatasets.clearNonAverages();
								PerformancePlotter.this.needsClearing = false;
							}
							
							if(PerformancePlotter.this.curTimeStep > PerformancePlotter.this.lastTimeStepUpdate){
								PerformancePlotter.this.updateCSRSeries();
								PerformancePlotter.this.lastTimeStepUpdate = curTimeStep;
							}
							if(PerformancePlotter.this.curEpisode > PerformancePlotter.this.lastEpisode){
								PerformancePlotter.this.updateCERSeries();
								PerformancePlotter.this.updateAERSeris();
								PerformancePlotter.this.updateMERSeris();
								PerformancePlotter.this.updateCSESeries();
								PerformancePlotter.this.updateSESeries();
								
								PerformancePlotter.this.lastEpisode = PerformancePlotter.this.curEpisode;
							}
							
							
							PerformancePlotter.this.trialUpdateComplete.b = true;
							PerformancePlotter.this.trialUpdateComplete.notifyAll();
							
						}
						
						
					}
				}
				
				
				
			}
		});
		
		
		
	}
	
	
	/**
	 * Informs the plotter that all trials for the current agent have been collected and causes the average plots to be set and displayed.
	 */
	protected void endTrialsForCurrentAgent(){
		
		final String aName = this.curAgentName;
		
		if(!this.trialMode.averagesEnabled()){
			return ;
		}
					
					
		List<Trial> trials = PerformancePlotter.this.agentTrials.get(aName);
		int [] n = PerformancePlotter.this.minStepAndEpisodes(trials);
		
		
		if(this.metricsSet.contains(PerformanceMetric.CUMULATIVEREWARDPERSTEP)){
			for(int i = 0; i < n[0]; i++){
				DescriptiveStatistics avgi = new DescriptiveStatistics();
				for(Trial t : trials){
					avgi.addValue(t.cumulativeStepReward.get(i));
				}
				double [] ci = getCI(avgi, this.significance);
				curAgentDatasets.csrAvgSeries.add(i, ci[0], ci[1], ci[2]);
			}
		}
		
		
		if(this.metricsSet.contains(PerformanceMetric.CUMULTAIVEREWARDPEREPISODE)){
			for(int i = 0; i < n[1]; i++){
				DescriptiveStatistics avgi = new DescriptiveStatistics();
				for(Trial t : trials){
					avgi.addValue(t.cumulativeEpisodeReward.get(i));
				}
				double [] ci = getCI(avgi, this.significance);
				curAgentDatasets.cerAvgSeries.add(i, ci[0], ci[1], ci[2]);
			}
		}
		
		
		if(this.metricsSet.contains(PerformanceMetric.AVERAGEEPISODEREWARD)){
			for(int i = 0; i < n[1]; i++){
				DescriptiveStatistics avgi = new DescriptiveStatistics();
				for(Trial t : trials){
					avgi.addValue(t.averageEpisodeReward.get(i));
				}
				double [] ci = getCI(avgi, this.significance);
				curAgentDatasets.aerAvgSeries.add(i, ci[0], ci[1], ci[2]);
			}
		}
		
		if(this.metricsSet.contains(PerformanceMetric.MEDIANEPISODEREWARD)){
			for(int i = 0; i < n[1]; i++){
				DescriptiveStatistics avgi = new DescriptiveStatistics();
				for(Trial t : trials){
					avgi.addValue(t.medianEpisodeReward.get(i));
				}
				double [] ci = getCI(avgi, this.significance);
				curAgentDatasets.merAvgSeries.add(i, ci[0], ci[1], ci[2]);
			}
		}
		
		if(this.metricsSet.contains(PerformanceMetric.CUMULATIVESTEPSPEREPISODE)){
			for(int i = 0; i < n[1]; i++){
				DescriptiveStatistics avgi = new DescriptiveStatistics();
				for(Trial t : trials){
					avgi.addValue(t.cumulativeStepEpisode.get(i));
				}
				double [] ci = getCI(avgi, this.significance);
				curAgentDatasets.cseAvgSeries.add(i, ci[0], ci[1], ci[2]);
			}
		}
		
		
		if(this.metricsSet.contains(PerformanceMetric.STEPSPEREPISODE)){
			for(int i = 0; i < n[1]; i++){
				DescriptiveStatistics avgi = new DescriptiveStatistics();
				for(Trial t : trials){
					avgi.addValue(t.stepEpisode.get(i));
				}
				double [] ci = getCI(avgi, this.significance);
				curAgentDatasets.seAvgSeries.add(i, ci[0], ci[1], ci[2]);
			}
		}
					
	
		curAgentDatasets.fireAllAverages();
		
		
	}
	
	
	/**
	 * Updates the cumulative reward by step series. Does nothing if that metric is not being plotted.
	 */
	protected void updateCSRSeries(){
		
		if(!this.metricsSet.contains(PerformanceMetric.CUMULATIVEREWARDPERSTEP)){
			return ;
		}
		
		int n = this.curTrial.cumulativeStepReward.size();
		for(int i = this.lastTimeStepUpdate; i < n; i++){
			this.curAgentDatasets.cumulativeStepRewardSeries.add((double)i, this.curTrial.cumulativeStepReward.get(i), false);
		}
		if(n > this.lastTimeStepUpdate){
			this.curAgentDatasets.cumulativeStepRewardSeries.fireSeriesChanged();
		}
	}
	
	
	/**
	 * Updates the cumulative reward by episode series.  Does nothing if that metric is not being plotted.
	 */
	protected void updateCERSeries(){
		
		if(!this.metricsSet.contains(PerformanceMetric.CUMULTAIVEREWARDPEREPISODE)){
			return ;
		}
		
		int n = this.curTrial.cumulativeEpisodeReward.size();
		for(int i = this.lastEpisode; i < n; i++){
			this.curAgentDatasets.cumulativeEpisodeRewardSeries.add((double)i, this.curTrial.cumulativeEpisodeReward.get(i), false);
		}
		if(n > this.lastEpisode){
			this.curAgentDatasets.cumulativeEpisodeRewardSeries.fireSeriesChanged();
		}
		
	}
	
	
	/**
	 * Updates the average reward by episode series.  Does nothing if that metric is not being plotted.
	 */
	protected void updateAERSeris(){
		
		if(!this.metricsSet.contains(PerformanceMetric.AVERAGEEPISODEREWARD)){
			return ;
		}
		
		int n = this.curTrial.averageEpisodeReward.size();
		for(int i = this.lastEpisode; i < n; i++){
			this.curAgentDatasets.averageEpisodeRewardSeries.add((double)i, this.curTrial.averageEpisodeReward.get(i), false);
		}
		if(n > this.lastEpisode){
			this.curAgentDatasets.averageEpisodeRewardSeries.fireSeriesChanged();
		}
	}
	
	
	/**
	 * Updates the median reward by episode series.  Does nothing if that metric is not being plotted.
	 */
	protected void updateMERSeris(){
		
		if(!this.metricsSet.contains(PerformanceMetric.MEDIANEPISODEREWARD)){
			return ;
		}
		
		int n = this.curTrial.medianEpisodeReward.size();
		for(int i = this.lastEpisode; i < n; i++){
			this.curAgentDatasets.medianEpisodeRewardSeries.add((double)i, this.curTrial.medianEpisodeReward.get(i), false);
		}
		if(n > this.lastEpisode){
			this.curAgentDatasets.medianEpisodeRewardSeries.fireSeriesChanged();
		}
	}
	
	
	/**
	 * Updates the cumulative steps by episode series.  Does nothing if that metric is not being plotted.
	 */
	protected void updateCSESeries(){
		
		if(!this.metricsSet.contains(PerformanceMetric.CUMULATIVESTEPSPEREPISODE)){
			return ;
		}
		
		int n = this.curTrial.cumulativeStepEpisode.size();
		for(int i = this.lastEpisode; i < n; i++){
			this.curAgentDatasets.cumulativeStepEpisodeSeries.add((double)i, this.curTrial.cumulativeStepEpisode.get(i), false);
		}
		if(n > this.lastEpisode){
			this.curAgentDatasets.cumulativeStepEpisodeSeries.fireSeriesChanged();
		}
	}
	
	
	/**
	 * Updates the steps by episode series.  Does nothing if that metric is not being plotted.
	 */
	protected void updateSESeries(){
		
		if(!this.metricsSet.contains(PerformanceMetric.STEPSPEREPISODE)){
			return ;
		}
		
		int n = this.curTrial.stepEpisode.size();
		for(int i = this.lastEpisode; i < n; i++){
			this.curAgentDatasets.stepEpisodeSeries.add((double)i, this.curTrial.stepEpisode.get(i), false);
		}
		if(n > this.lastEpisode){
			this.curAgentDatasets.stepEpisodeSeries.fireSeriesChanged();
		}
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
		
		
		
		/**
		 * Updates all datastructures with the reward received from the last step
		 * @param r the last reward received
		 */
		public void stepIncrement(double r){
			
			accumulate(this.cumulativeStepReward, r);
			this.curEpisodeReward += r;
			this.curEpisodeSteps++;
			this.curEpisodeRewards.add(r);

			
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
