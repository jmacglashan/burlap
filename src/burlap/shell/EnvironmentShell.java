package burlap.shell;

import burlap.oomdp.core.Domain;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.shell.command.ShellCommand;
import burlap.shell.command.env.*;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;

/**
 * This is a subclass of {@link burlap.shell.BurlapShell} for a shell with shell commands that manipulate or read
 * an {@link burlap.oomdp.singleagent.environment.Environment}. The {@link burlap.oomdp.singleagent.environment.Environment}
 * can be accessed with the {@link #getEnv()} method.
 * <p>
 * Use the cmds shell command to see all commands
 * and use the -h option on any given command to see its help message. Generally, the Java implementations of the
 * default commands provided for this
 * shell are are in burlap.shell.command.env package.
 * @author James MacGlashan.
 */
public class EnvironmentShell extends BurlapShell{

	protected Environment env;

	public EnvironmentShell(Domain domain, Environment env, InputStream is, PrintStream os) {
		super(domain, is, os);
		this.env = env;

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
				new RewardCommand(), new IsTerminalCommand(),
				erc.getRecCommand(), erc.getBrowser(), new ListActionsCommand(), new ListPropFunctions());
	}


}
