package burlap.shell.command.env;

import burlap.mdp.singleagent.environment.Environment;
import burlap.shell.BurlapShell;
import burlap.shell.EnvironmentShell;
import burlap.shell.command.ShellCommand;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * A {@link burlap.shell.command.ShellCommand} for resetting the {@link burlap.mdp.singleagent.environment.Environment}.
 * Use the -h option for help information.
 * @author James MacGlashan.
 */
public class ResetEnvCommand implements ShellCommand {

	@Override
	public String commandName() {
		return "reset";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {
		Environment env = ((EnvironmentShell)shell).getEnv();
		env.resetEnvironment();
		return 1;
	}
}
