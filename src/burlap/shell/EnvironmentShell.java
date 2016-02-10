package burlap.shell;

import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldRewardFunction;
import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.visualizer.Visualizer;
import burlap.shell.command.ShellCommand;
import burlap.shell.command.env.*;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;

/**
 * @author James MacGlashan.
 */
public class EnvironmentShell extends BurlapShell{

	protected Environment env;

	public EnvironmentShell(Domain domain, Environment env, InputStream is, PrintStream os) {
		super(domain, is, os);
		this.env = env;
		this.is = is;
		this.os = os;
		this.scanner = new Scanner(is);
		this.domain = domain;

		Collection<ShellCommand> res = this.generateReserved();
		this.reserved = new HashSet<String>(res.size());
		for(ShellCommand c : res){
			this.addCommand(c);
			this.reserved.add(c.commandName());
		}
		Collection<ShellCommand> std = this.generateStandard();
		for(ShellCommand c : std){
			this.addCommand(c);
		}

		this.welcomeMessage = "Welcome to the BURLAP agent environment shell. Type the command 'help' to bring " +
				"up additional information about using this shell.";

		this.helpText = "Use the command help to bring up this message again. " +
				"Here is a list of standard reserved commands:\n" +
				"cmds - list all known commands.\n" +
				"aliases - list all known command aliases.\n" +
				"alias - set an alias for a command.\n" +
				"quit - terminate this shell.\n\n" +
				"Other useful, but non-reserved, commands are:\n" +
				"obs - print the current observation of the environment\n" +
				"ex - execute an action\n\n" +
				"Usually, you can get help on an individual command by passing it the -h option.";

	}


	public Environment getEnv() {
		return env;
	}

	public void setEnv(Environment env) {
		this.env = env;
	}

	@Override
	protected Collection<ShellCommand> generateStandard() {
		EpisodeRecordingCommands erc = new EpisodeRecordingCommands();
		return Arrays.asList(new ExecuteActionCommand(domain), new ObservationCommand(), new ResetEnvCommand(),
				new AddStateObjectCommand(domain), new RemoveStateObjectCommand(), new SetAttributeCommand(),
				new AddRelationCommand(), new RemoveRelationCommand(), new RewardCommand(), new IsTerminalCommand(),
				erc.getRecCommand(), erc.getBrowser());
	}


	public static void main(String[] args) {
		GridWorldDomain gwd = new GridWorldDomain(11, 11);
		gwd.setMapToFourRooms();
		GridWorldRewardFunction rf = new GridWorldRewardFunction(11, 11, 0);
		rf.setReward(10, 10, 1.);
		TerminalFunction tf = new GridWorldTerminalFunction(10, 10);

		Domain domain = gwd.generateDomain();
		State s = GridWorldDomain.getOneAgentNoLocationState(domain, 0, 0);


		Environment env = new SimulatedEnvironment(domain, rf, tf, s);

		EnvironmentShell shell = new EnvironmentShell(domain, env, System.in, System.out);
		Visualizer v = GridWorldVisualizer.getVisualizer(gwd.getMap());
		shell.setVisualizer(v);
		shell.start();
	}
}
