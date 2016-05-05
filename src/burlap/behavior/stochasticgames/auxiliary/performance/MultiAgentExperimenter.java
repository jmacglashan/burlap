package burlap.behavior.stochasticgames.auxiliary.performance;

import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.stochasticgames.GameAnalysis;
import burlap.debugtools.DPrint;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.stochasticgames.World;
import burlap.mdp.stochasticgames.WorldGenerator;
import burlap.mdp.stochasticgames.WorldObserver;


/**
 * This class is used to simplify the comparison of agent perfomance in a stochastic game world. This class takes as input a {@link WorldGenerator} and
 * a list of {@link AgentFactoryAndType} objects which can be used to generate agents and play them against each other in a world. Perfomance over
 * multiple trials is plotted using the {@link MultiAgentPerformancePlotter} {@link WorldObserver} object and the results can also be printed
 * out to CSV files using the {@link #writeEpisodeDataToCSV(String)}, {@link #writeStepDataToCSV(String)}, or {@link #writeStepAndEpisodeDataToCSV(String)}
 * methods. If only the CSV data is desired without plotting, the plotting may be disabled using the {@link #toggleVisualPlots(boolean)} method.
 * <p>
 * To set up the metrics and plots that will be displayed, use the {@link #setUpPlottingConfiguration(int, int, int, int, TrialMode, PerformanceMetric...)}
 * method. If this method is not called, but plots are not disabled, then my default the cumulative reward will be displayed.
 * <p>
 * The length of a trial can have two interpretations, either the number of episodes, or the total number of steps taken across multiple episdes.
 * By default, the trial length will be interpreted as the number of episodes in a trial, but this interpreation can be changed with the
 * {@link #toggleTrialLengthInterpretation(boolean)}
 * <p>
 * To start an experiment once everything is configured, use the {@link #startExperiment()} method.
 * 
 * 
 * @author James MacGlashan
 *
 */
public class MultiAgentExperimenter {

	
	/**
	 * The terminal function defining when episodes in a trial end
	 */
	protected TerminalFunction				tf;
	
	/**
	 * A world generated for created a new world for each testing trial
	 */
	protected WorldGenerator				worldGenerator;
	
	/**
	 * The agent factories for the agents to be tested
	 */
	protected AgentFactoryAndType []		agentFactoriesAndTypes;
	
	
	/**
	 * The number of trials that each agent is evaluted
	 */
	protected int							nTrials;
	
	
	/**
	 * The length of each trial
	 */
	protected int							trialLength;
	
	
	/**
	 * Whether the trial length specifies a number of episodes (which is the default) or the total number of steps
	 */
	protected boolean						trialLengthIsInEpisodes = true;
	
	
	/**
	 * The performance plotter object
	 */
	protected MultiAgentPerformancePlotter	plotter;
	
	
	/**
	 * Whether the performance should be visually plotted (by default they will)
	 */
	protected boolean						displayPlots = true;
	
	
	/**
	 * The delay in milliseconds between autmatic refreshes of the plots
	 */
	protected int							plotRefresh = 1000;
	
	
	/**
	 * The signficance value for the confidence interval in the plots. The default is 0.05 which correspodns to a 95% CI
	 */
	protected double						plotCISignificance = 0.05;
	
	
	/**
	 * Whether the experimenter has completed.
	 */
	protected boolean						completedExperiment = false;
	
	
	/**
	 * The debug code used for debug printing. This experimenter will print with the debugger the number of trials completed for each agent.
	 */
	public int								debugCode = 63624014;
	
	
	
	
	/**
	 * Initializes. Trial length is interepted either has the number of episodes per trial or the total number of steps across all episodes.
	 * By default the length will be interepted as the number of episodes, but this interpetation can be changed with the {@link #toggleTrialLengthInterpretation(boolean)}
	 * method. The agents will join generated worlds in the order that they appear in the list.
	 * @param worldGenerator the world generator used to create a clean world for each trial.
	 * @param tf the terminal function used to interpret the end of episodes
	 * @param nTrials the number of trials over which performance will be gathered
	 * @param trialLength the length of trial
	 * @param agentFactoriesAndTypes the agent factories and the type of agent the generated agent will join the world as
	 */
	public MultiAgentExperimenter(WorldGenerator worldGenerator, TerminalFunction tf, int nTrials, int trialLength, AgentFactoryAndType...agentFactoriesAndTypes){
		
		if(agentFactoriesAndTypes.length == 0){
			throw new RuntimeException("Zero agent factories provided. At least one must be given for an experiment");
		}
		this.worldGenerator = worldGenerator;
		this.tf = tf;
		this.nTrials = nTrials;
		this.trialLength = trialLength;
		this.agentFactoriesAndTypes = agentFactoriesAndTypes;
		
		this.displayPlots = true;
	
		
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
		
		this.plotter = new MultiAgentPerformancePlotter(this.tf, chartWidth, chartHeight, columns, maxWindowHeight, trialMode, metrics);
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
			
			TrialMode trialMode = TrialMode.MOSTRECENTANDAVERAGE;
			if(this.nTrials == 1){
				trialMode = TrialMode.MOSTRECENTTTRIALONLY;
			}
			
			this.plotter = new MultiAgentPerformancePlotter(this.tf, 500, 250, 2, 500, trialMode);
				
		}
		
		if(this.displayPlots){
			this.plotter.startGUI();
		}
		
		for(int i = 0; i < this.nTrials; i++){
			
			DPrint.cl(this.debugCode, "Beginning trial " + (i+1) + "/" + this.nTrials);
			
			World w = worldGenerator.generateWorld();

			DPrint.toggleCode(w.getDebugId(), false);
			w.addWorldObserver(this.plotter);
			for(AgentFactoryAndType aft : this.agentFactoriesAndTypes){
				aft.agentFactory.generateAgent().joinWorld(w, aft.at);
			}
			
			this.plotter.startNewTrial();
			if(this.trialLengthIsInEpisodes){
				this.runEpisodewiseTrial(w);
			}
			else{
				this.runStepwiseTrial(w);
			}
			
		}
		
		this.plotter.endAllTrials();
		this.completedExperiment = true;
		
	}
	
	
	
	
	/**
	 * Writes the step-wise and episode-wise data to CSV files.
	 * The episode-wise data will be saved to the file &lt;pathAndBaseNameToUse&gt;Episodes.csv. The step-wise data will.
	 * If the experimenter as not been run, then nothing will be saved and a warrning message will be printed to indicate as such.
	 * be saved to the file &lt;pathAndBaseNameToUse&gt;Steps.csv
	 * @param pathAndBaseNameToUse the base path and file name for the epsidoe-wise and step-wise csv files.
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
	 * Runs a trial where trial length is interpreted as the number of episodes in a trial.
	 * @param w the world object in which the trial will be run
	 */
	protected void runEpisodewiseTrial(World w){
		
		for(int i = 0; i < this.trialLength; i++){
			w.runGame();
		}
		
	}
	
	/**
	 * Runs a trial where the trial lenght is interpreted as the number of total steps taken.
	 * @param w the world object in which the trial will be run
	 */
	protected void runStepwiseTrial(World w){
		
		int stepsReamining = this.trialLength;
		while(stepsReamining > 0){
			GameAnalysis ga = w.runGame(stepsReamining);
			stepsReamining -= ga.numTimeSteps()-1;
		}
		
		
	}
	
}
