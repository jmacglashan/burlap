package burlap.shell.command.env;

import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.*;
import burlap.shell.BurlapShell;
import burlap.shell.EnvironmentShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Two {@link burlap.shell.command.ShellCommand}s, rec and episode, for recording and browsing episodes of behavior that take place in the {@link burlap.mdp.singleagent.environment.Environment}.
 * Use the -h option for help information.
 * @author James MacGlashan.
 */
public class EpisodeRecordingCommands implements EnvironmentObserver {

	protected List<Episode> episodes = new ArrayList<Episode>();
	protected Episode curEpisode;
	protected boolean finished = false;

	protected boolean autoRecord = false;
	protected boolean recording = false;
	protected boolean recordedLast = false;

	protected RecordCommand recCommand = new RecordCommand();
	protected EpisodeBrowserCommand browser = new EpisodeBrowserCommand();


	public RecordCommand getRecCommand() {
		return recCommand;
	}

	public EpisodeBrowserCommand getBrowser() {
		return browser;
	}

	@Override
	public void observeEnvironmentActionInitiation(State o, Action action) {

	}

	@Override
	public void observeEnvironmentInteraction(EnvironmentOutcome eo) {
		if((finished || curEpisode == null) && recording){
			curEpisode = new Episode(eo.o);
			curEpisode.recordTransitionTo(eo.a, eo.op, eo.r);
			finished = false;
		}
		else if(recording){
			curEpisode.recordTransitionTo(eo.a, eo.op, eo.r);
		}

		recordedLast = false;
	}

	@Override
	public void observeEnvironmentReset(Environment resetEnvironment) {
		finished = true;
		if(autoRecord && recording && !recordedLast){
			episodes.add(curEpisode);
			recordedLast = true;
		}
	}


	public class RecordCommand implements ShellCommand{

		protected OptionParser parser = new OptionParser("bfaircewlh*");


		@Override
		public String commandName() {
			return "rec";
		}

		@Override
		public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

			Environment env = ((EnvironmentShell)shell).getEnv();
			OptionSet oset = this.parser.parse(argString.split(" "));
			if(oset.has("h")){
				os.println("[-b [-f] [-a]] [-i] [-r] [-c] [-e] [-w path baseName] [-l [-a] path]\n" +
						"Manages episode recording, loading, and writing results to file. Each recorded episode will be recorded to an internal list of episodes. Use the related 'episode' command to browse recorded episodes\n\n" +
						"-b: begins recording episodes from the environment.\n" +
						"-f: (with -b) causes shell environment to be converted to an EnvironmentServer if it does not already implement EnvironmentServerInterface.\n" +
						"-a: (with -b) causes episodes to be automatically recorded when the environment is reset.\n" +
						"-i: initializes a new episode to track starting the current environment state.\n" +
						"-r: record the last episode since environment reset to the internal list.\n" +
						"-c: clears all internally recorded episodes.\n" +
						"-e: stops tracking and recording of episodes.\n" +
						"-w path baseName: writes all episodes to the directory path, with each episode named baseName_i.episode where i is the ith episode.\n" +
						"-l path: loads all episodes stored in the directory path into the internal episode list.\n" +
						"-a: (with -l) appends loaded episodes to existing episodes.");

				return 0;
			}

			if(oset.has("b")){
				if(oset.has("r") || oset.has("c") || oset.has("e") || oset.has("w") || oset.has("l")){
					return -1;
				}

				if(!(env instanceof EnvironmentServerInterface)){
					if(oset.has("f")){
						env = new EnvironmentServer(env);
						((EnvironmentShell)shell).setEnv(env);
					}
					else{
						os.println("Cannot begin episode recording because the environment does not implement EnvironmentServerInterface. " +
								"Consider using the -f option to set the shell environment to an EnvironmentServer that wraps the current environment.");
						return 0;
					}
				}

				EpisodeRecordingCommands.this.recording = true;
				os.print("Enabling episode recoding");
				if(oset.has("a")){
					EpisodeRecordingCommands.this.autoRecord = true;
					os.println(" with auto episode recording.");
				}
				else{
					EpisodeRecordingCommands.this.autoRecord = false;
					os.println(" without auto episode recording.");
				}
				EnvironmentServerInterface serverenv = (EnvironmentServerInterface)env;
				if(!serverenv.observers().contains(EpisodeRecordingCommands.this)){
					serverenv.addObservers(EpisodeRecordingCommands.this);
				}

				return 0;

			}

			if(oset.has("i")){
				if(recording) {
					curEpisode = new Episode(env.currentObservation());
					finished = false;
					os.println("Initialized new episode.");
				}
				else{
					os.println("Cannot initialize episode because recording has not been initiated.");
				}
				return 0;
			}

			if(oset.has("r")){
				if(EpisodeRecordingCommands.this.curEpisode != null && !EpisodeRecordingCommands.this.recordedLast){
					EpisodeRecordingCommands.this.episodes.add(EpisodeRecordingCommands.this.curEpisode);
					EpisodeRecordingCommands.this.recordedLast = true;
					curEpisode = new Episode(env.currentObservation());
					os.println("Recorded episode since last environment reset or initialization.");
				}
				else{
					os.println("No new episode to record.");
				}
				return 0;
			}

			if(oset.has("c")){
				episodes.clear();
				os.println("Cleared all stored episodes.");
				return 0;
			}

			if(oset.has("e")){
				autoRecord = false;
				recording = false;
				os.println("No longer tracking for recording. Use the -b option to begin again.");
				return 0;
			}

			List<String> args = (List<String>)oset.nonOptionArguments();

			if(oset.has("w")){
				if(args.size() != 2){
					return -1;
				}
				Episode.writeEpisodesToDisk(episodes, args.get(0), args.get(1));
				os.println("Wrote episodes to " + args.get(0));
				return 0;
			}

			if(oset.has("l")){
				if(args.size() != 1){
					return -1;
				}
				List<Episode> nEpisodes = Episode.parseFilesIntoEAList(args.get(0));
				if(oset.has("a")){
					episodes.addAll(nEpisodes);
					os.println("Loaded episodes from " + args.get(0) + " and appended them to current episodes list.");
				}
				else{
					episodes = nEpisodes;
					os.println("Loaded episodes from " + args.get(0) + " and replaced current list with them.");
				}

				return 0;

			}

			return -1;
		}
	}


	public class EpisodeBrowserCommand implements ShellCommand{

		protected int which=-1;

		protected OptionParser parser = new OptionParser("nwc:ts:a:r:l:vh*");


		@Override
		public String commandName() {
			return "episode";
		}

		@Override
		public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

			Environment env = ((EnvironmentShell)shell).getEnv();
			OptionSet oset = this.parser.parse(argString.split(" "));
			if(oset.has("h")){
				os.println("[-n][-c][-s i][-a i][-r i][-l i]\n" +
						"Allows you to browse information about the episodes that have been recorded.\n\n" +
						"-n: print the number of recorded episodes.\n" +
						"-w: print which episode is currently chosen to explore.\n" +
						"-c i: choose which of the n episodes to browse.\n" +
						"-t: print the maximum time step in the current episode.\n" +
						"-s i: print the ith state of the current chosen episode.\n" +
						"-a i: print the ith action of the currently chosen episode.\n" +
						"-r i: print the ith reward of the currently chosen episode.\n" +
						"-l i: loads the ith state into the environment, if the environment implements StateSettableEnvironment.\n" +
						"-v: launch an EpisodeSequenceVisualizer of the recorded episodes, if a Visualizer has been associated with this command.");

				return 0;
			}

			if(oset.has("n")){
				os.println(episodes.size() + " episodes recorded.");
			}

			if(oset.has("w")){
				if(which == -1){
					os.println("No episode chosen to explore. Use -c to choose one.");
				}
				else{
					os.println("Episode " + which + " chosen.");
				}
			}

			if(oset.has("c")){
				String cVal = (String)oset.valueOf("c");
				if(cVal == null){
					return -1;
				}
				int i;
				try{
					i = Integer.parseInt(cVal);
				}catch(Exception e){
					return -1;
				}
				if(i >= episodes.size()){
					os.println("Requested episode " + i + " but only " + episodes.size() + " episodes are recorded.");
				}
				else{
					which = i;
				}
			}

			if(oset.has("t")){
				if(which >= episodes.size()){
					os.println("Cannot retrieve information about episode " + which + " because only " + episodes.size() + " episodes are recorded.");
				}
				else{
					Episode ea = episodes.get(which);
					os.println("The maximum time step in this episode is " + ea.maxTimeStep());
				}
			}

			if(oset.has("s")){
				String sVal = (String)oset.valueOf("s");
				if(sVal == null){
					return -1;
				}
				int i;
				try{
					i = Integer.parseInt(sVal);
				}catch(Exception e){
					return -1;
				}

				if(which >= episodes.size()){
					os.println("Cannot retrieve information about episode " + which + " because only " + episodes.size() + " episodes are recorded.");
				}
				else{
					Episode ea = episodes.get(which);
					if(i > ea.maxTimeStep() || i < 0){
						os.println("Cannot print state " + i + " because the episode only has " + ea.maxTimeStep() + " time steps.");
					}
					else{
						os.println(ea.getState(i).toString());
					}
				}
			}

			if(oset.has("a")){
				String sVal = (String)oset.valueOf("a");
				if(sVal == null){
					return -1;
				}
				int i;
				try{
					i = Integer.parseInt(sVal);
				}catch(Exception e){
					return -1;
				}

				if(which >= episodes.size()){
					os.println("Cannot retrieve information about episode " + which + " because only " + episodes.size() + " episodes are recorded.");
				}
				else{
					Episode ea = episodes.get(which);
					if(i > ea.maxTimeStep() || i < 0){
						os.println("Cannot print action " + i + " because the episode only has " + ea.maxTimeStep() + " time steps (with final time step not having actions taken).");
					}
					else{
						os.println(ea.getAction(i).toString());
					}
				}
			}

			if(oset.has("r")){
				String sVal = (String)oset.valueOf("r");
				if(sVal == null){
					return -1;
				}
				int i;
				try{
					i = Integer.parseInt(sVal);
				}catch(Exception e){
					return -1;
				}

				if(which >= episodes.size()){
					os.println("Cannot retrieve information about episode " + which + " because only " + episodes.size() + " episodes are recorded.");
				}
				else{
					Episode ea = episodes.get(which);
					if(i > ea.maxTimeStep() || i < 0){
						os.println("Cannot print action " + i + " because the episode only has " + ea.maxTimeStep() + " time steps.");
					}
					else{
						os.println(ea.getReward(i));
					}
				}
			}

			if(oset.has("l")){

				String sVal = (String)oset.valueOf("l");
				if(sVal == null){
					return -1;
				}
				int i;
				try{
					i = Integer.parseInt(sVal);
				}catch(Exception e){
					return -1;
				}

				StateSettableEnvironment senv = (StateSettableEnvironment)EnvironmentDelegation.EnvDelegationTools.getDelegateImplementing(env, StateSettableEnvironment.class);
				if(senv == null){
					os.println("Cannot load episode state into environment, because the environment does not implement StateSettableEnvironment.");
				}
				else{
					Episode ea = episodes.get(which);
					if(i > ea.maxTimeStep() || i < 0){
						os.println("Cannot load state " + i + " into the environment because the episode only has " + ea.maxTimeStep() + " time steps.");
					}
					else{
						senv.setCurStateTo(ea.getState(i));
						os.println("Loaded state " + i + " into the environment.");
					}
				}

				return 1;

			}

			if(oset.has("v")){
				if(shell.getVisualizer() == null){
					os.println("Cannot launch EpisodeSequenceVisualizer, because no visualizer has been associated with the shell.");
				}
				else{
					new EpisodeSequenceVisualizer(shell.getVisualizer().copy(), shell.getDomain(), episodes);
				}
			}

			return 0;
		}
	}

}
