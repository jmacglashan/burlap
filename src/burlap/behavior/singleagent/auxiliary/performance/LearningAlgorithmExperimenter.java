package burlap.behavior.singleagent.auxiliary.performance;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.debugtools.DPrint;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentServer;


/**
 * This class is used to simplify the comparison of different learning algorithms. It takes as input a test {@link burlap.mdp.singleagent.environment.Environment}
 * in which to perform the experiments,
 * a number of trials, the length of the trials, and an array of learning agent factories used to generated agent instances and compare their performance.
 * The {@link burlap.mdp.singleagent.environment.Environment} may optionally implement the {@link burlap.behavior.singleagent.auxiliary.performance.ExperimentalEnvironment}
 * interface which will let this class to tell the {@link burlap.mdp.singleagent.environment.Environment} whenever experiments with a new agent class (defined by
 * an {@link burlap.behavior.singleagent.learning.LearningAgentFactory} is begun).
 * The length of the trials by default is assumed to be in episodes, but it may also be changed to indicate length in total number of steps using the 
 * {@link #toggleTrialLengthInterpretation(boolean)} method.
 * <p>
 * Performance results are displayed in plots using the {@link PerformancePlotter} class, but visualization may also be disabled with the {@link #toggleVisualPlots(boolean)}
 * method. Results may be saved to csv files after the experiment is complete.
 * <p>
 * The purpose of the experimenter is to test an agent for a specified number of trials. At the beginning of each trial, a new agent is generated using the designated
 * LearningAgentFactory and is used for the specified trial length. After all trials are complete for an agent, the next agent is tested. Note that immediately before
 * an agent is generated from an agent factory, the performance plotter is temporarily frozen from collecting data until the new agent is returned. This allows
 * agent factories to perform offline learning before returning a new agent in the same domain without affecting the experimenter results.
 * <p>
 * By default the cumulative reward per step will be plotted and if more than one trial is specified, the both the most recent trail and the trial average plot will be shown.
 * If only one trial is specified, then only the most recent trial plot will be shown. To control the kinds of plots displayed use the 
 * {@link #setUpPlottingConfiguration(int, int, int, int, TrialMode, PerformanceMetric...)} method. 
 * 
 * @author James MacGlashan
 *
 */
public class LearningAlgorithmExperimenter {


	/**
	 * The test {@link burlap.mdp.singleagent.environment.Environment} in which experiments will be performed.
	 */
	protected Environment 		testEnvironment;


	/**
	 * The {@link burlap.mdp.singleagent.environment.EnvironmentServer} that wraps the test {@link burlap.mdp.singleagent.environment.Environment}
	 * and tells a {@link burlap.behavior.singleagent.auxiliary.performance.PerformancePlotter} about the individual interactions.
	 */
	protected EnvironmentServer environmentSever;


	/**
	 * The array of agent factories for the agents to be compared.
	 */
	protected LearningAgentFactory []	agentFactories;
	
	
	/**
	 * The number of trials that each agent is evaluated
	 */
	protected int						nTrials;
	
	
	/**
	 * The length of each trial
	 */
	protected int						trialLength;
	
	
	/**
	 * Whether the trial length specifies a number of episodes (which is the default) or the total number of steps
	 */
	protected boolean					trialLengthIsInEpisodes = true;
	
	
	/**
	 * The PerformancePlotter used to collect and plot results
	 */
	protected PerformancePlotter		plotter = null;
	
	
	/**
	 * Whether the performance should be visually plotted (by default they will)
	 */
	protected boolean					displayPlots = true;
	
	
	/**
	 * The delay in milliseconds between autmatic refreshes of the plots
	 */
	protected int						plotRefresh = 1000;
	
	
	/**
	 * The signficance value for the confidence interval in the plots. The default is 0.05 which correspodns to a 95% CI
	 */
	protected double					plotCISignificance = 0.05;
	
	
	/**
	 * Whether the experimenter has completed.
	 */
	protected boolean					completedExperiment = false;
	
	
	/**
	 * The debug code used for debug printing. This experimenter will print with the debugger the number of trials completed for each agent.
	 */
	public int							debugCode = 63634013;


	
	
	
	/**
	 * Initializes.
	 * The trialLength will be interpreted as the number of episodes, but it can be reinterpreted as a total number of steps per trial using the
	 * {@link #toggleTrialLengthInterpretation(boolean)}.
	 * @param testEnvironment the test {@link burlap.mdp.singleagent.environment.Environment} in which experiments will be performed.
	 * @param nTrials the number of trials
	 * @param trialLength the length of the trials (by default in episodes, but can be intereted as maximum step length)
	 * @param agentFactories factories to generate the agents to be tested.
	 */
	public LearningAlgorithmExperimenter(Environment testEnvironment, int nTrials, int trialLength, LearningAgentFactory...agentFactories){
		
		if(agentFactories.length == 0){
			throw new RuntimeException("Zero agent factories provided. At least one must be given for an experiment");
		}
		
		this.testEnvironment = testEnvironment;
		this.nTrials = nTrials;
		this.trialLength = trialLength;
		this.agentFactories = agentFactories;
	}
	
	
	
	/**
	 * Setsup the plotting confiruation.
	 * @param chartWidth the width of each chart/plot
	 * @param chartHeight the height of each chart//plot
	 * @param columns the number of columns of the plots displayed. Plots are filled in columns first, then move down the next row.
	 * @param maxWindowHeight the maximum window height allowed before a scroll view is used.
	 * @param trialMode which plots to use; most recent trial, average over all trials, or both. If both, the most recent plot will be inserted into the window first, then the average.
	 * @param metrics the metrics that should be plotted. The metrics will appear in the window in the order that they are specified (columns first)
	 */
	public void setUpPlottingConfiguration(int chartWidth, int chartHeight, int columns, int maxWindowHeight, TrialMode trialMode, PerformanceMetric...metrics){
		
		if(trialMode.averagesEnabled() && this.nTrials == 1){
			trialMode = TrialMode.MOST_RECENT_TRIAL_ONLY;
		}
		
		this.displayPlots = true;
		this.plotter = new PerformancePlotter(this.agentFactories[0].getAgentName(), chartWidth, chartHeight, columns, maxWindowHeight, trialMode, metrics);
		this.plotter.setRefreshDelay(this.plotRefresh);
		this.plotter.setSignificanceForCI(this.plotCISignificance);
	}
	
	
	/**
	 * Sets the delay in milliseconds between automatic plot refreshes
	 * @param delayInMS the delay in milliseconds
	 */
	public void setPlotRefreshDelay(int delayInMS){
		this.plotRefresh = delayInMS;
		if(this.plotter != null){
			this.plotter.setRefreshDelay(delayInMS);
		}
	}
	
	
	/**
	 * Sets the significance used for confidence intervals.
	 * The default is 0.05 which corresponds to a 95% CI.
	 * @param significance the significance for confidence intervals to use
	 */
	public void setPlotCISignificance(double significance){
		this.plotCISignificance = significance;
		if(this.plotter != null){
			this.plotter.setSignificanceForCI(significance);
		}
	}
	
	
	/**
	 * Toggles whether plots should be displayed or not.
	 * @param shouldPlotResults if true, then plots will be displayed; if false plots will not be displayed.
	 */
	public void toggleVisualPlots(boolean shouldPlotResults){
		this.displayPlots = shouldPlotResults;
	}
	
	
	/**
	 * Changes whether the trial length provided in the constructor is interpreted as the number of episodes or total number of steps.
	 * @param lengthRepresentsEpisodes if true, interpret length as number of episodes; if false interprete as total number of steps.
	 */
	public void toggleTrialLengthInterpretation(boolean lengthRepresentsEpisodes){
		this.trialLengthIsInEpisodes = lengthRepresentsEpisodes;
	}
	
	
	/**
	 * Starts the experiment and runs all trails for all agents.
	 */
	public void startExperiment(){

		if(this.completedExperiment){
			System.out.println("Experiment was already run and has completed. If you want to run a new experiment create a new Experiment object.");
			return;
		}
		
		if(this.plotter == null){
			
			TrialMode trialMode = TrialMode.MOST_RECENT_AND_AVERAGE;
			if(this.nTrials == 1){
				trialMode = TrialMode.MOST_RECENT_TRIAL_ONLY;
			}
			
			this.plotter = new PerformancePlotter(this.agentFactories[0].getAgentName(), 500, 250, 2, 500, trialMode);
				
		}
		
		
		//this.domain.addActionObserverForAllAction(plotter);
		this.environmentSever = new EnvironmentServer(this.testEnvironment, plotter);
		
		if(this.displayPlots){
			this.plotter.startGUI();
		}
		
		for(int i = 0; i < this.agentFactories.length; i++){
			
			if(i > 0){
				this.plotter.startNewAgent(this.agentFactories[i].getAgentName());
			}

			if(this.testEnvironment instanceof ExperimentalEnvironment){
				((ExperimentalEnvironment)this.testEnvironment).startNewExperiment();
			}
			for(int j = 0; j < this.nTrials; j++){
				
				DPrint.cl(this.debugCode, "Beginning " + this.agentFactories[i].getAgentName() + " trial " + (j+1) + "/" + this.nTrials);
				
				if(this.trialLengthIsInEpisodes){
					this.runEpisodeBoundTrial(this.agentFactories[i]);
				}
				else{
					this.runStepBoundTrial(this.agentFactories[i]);
				}
			}
			
		}
		
		this.plotter.endAllAgents();
		
		this.completedExperiment = true;
		
	}
	
	
	/**
	 * Writes the step-wise and episode-wise data to CSV files.
	 * The episode-wise data will be saved to the file &lt;pathAndBaseNameToUse&gt;Episodes.csv. The step-wise data will.
	 * If the experimenter as not been run, then nothing will be saved and a warning message will be printed to indicate as such.
	 * be saved to the file &lt;pathAndBaseNameToUse&gt;Steps.csv
	 * @param pathAndBaseNameToUse the base path and file name for the episode-wise and step-wise csv files.
	 */
	public void writeStepAndEpisodeDataToCSV(String pathAndBaseNameToUse){
		if(!this.completedExperiment){
			System.out.println("Cannot write data until the experiment has been started with the startExperiment() method.");
			return;
		}
		this.plotter.writeStepAndEpisodeDataToCSV(pathAndBaseNameToUse);
	}
	
	
	/**
	 * Writes an episode-wise data to a csv file.
	 * If the file path does not include the .csv extension, it will automatically be added.
	 * If the experimenter as not been run, then nothing will be saved and a warrning message will be printed to indicate as such.
	 * @param filePath the path to the csv file to write to.
	 */
	public void writeStepDataToCSV(String filePath){
		if(!this.completedExperiment){
			System.out.println("Cannot write data until the experiment has been started with the startExperiment() method.");
			return;
		}
		this.plotter.writeStepDataToCSV(filePath);
	}
	
	
	/**
	 * Writes an step-wise data to a csv file.
	 * If the file path does not include the .csv extension, it will automatically be added.
	 * If the experimenter as not been run, then nothing will be saved and a warrning message will be printed to indicate as such.
	 * @param filePath the path to the csv file to write to.
	 */
	public void writeEpisodeDataToCSV(String filePath){
		if(!this.completedExperiment){
			System.out.println("Cannot write data until the experiment has been started with the startExperiment() method.");
			return;
		}
		this.plotter.writeEpisodeDataToCSV(filePath);
	}
	
	
	
	/**
	 * Runs a trial for an agent generated by the given factory when interpreting trial length as a number of episodes.
	 * @param agentFactory the agent factory used to generate the agent to test.
	 */
	protected void runEpisodeBoundTrial(LearningAgentFactory agentFactory){
		
		//temporarily disable plotter data collection to avoid possible contamination for any actions taken by the agent generation
		//(e.g., if there is pre-test training)
		this.plotter.toggleDataCollection(false);

		LearningAgent agent = agentFactory.generateAgent();
		
		this.plotter.toggleDataCollection(true); //turn it back on to begin
		
		this.plotter.startNewTrial();
		
		for(int i = 0; i < this.trialLength; i++){
			agent.runLearningEpisode(this.environmentSever);
			this.plotter.endEpisode();
			this.environmentSever.resetEnvironment();
		}
		
		this.plotter.endTrial();
		
	}
	
	
	/**
	 * Runs a trial for an agent generated by the given factor when interpreting trial length as a number of total steps.
	 * @param agentFactory the agent factory used to generate the agent to test.
	 */
	protected void runStepBoundTrial(LearningAgentFactory agentFactory){
		
		//temporarily disable plotter data collection to avoid possible contamination for any actions taken by the agent generation
		//(e.g., if there is pre-test training)
		this.plotter.toggleDataCollection(false);
		
		LearningAgent agent = agentFactory.generateAgent();
		
		this.plotter.toggleDataCollection(true); //turn it back on to begin
		
		this.plotter.startNewTrial();
		
		int stepsRemaining = this.trialLength;
		while(stepsRemaining > 0){
			Episode ea = agent.runLearningEpisode(this.environmentSever, stepsRemaining);
			stepsRemaining -= ea.numTimeSteps()-1; //-1  because we want to subtract the number of actions, not the number of states seen
			this.plotter.endEpisode();
			this.environmentSever.resetEnvironment();
		}
		
		this.plotter.endTrial();
		
	}
	
	
	
}
