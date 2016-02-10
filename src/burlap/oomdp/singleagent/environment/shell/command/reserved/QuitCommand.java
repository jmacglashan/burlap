package burlap.oomdp.singleagent.environment.shell.command.reserved;

import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.shell.EnvironmentShell;
import burlap.oomdp.singleagent.environment.shell.command.ShellCommand;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * @author James MacGlashan.
 */
public class QuitCommand implements ShellCommand{

	@Override
	public String commandName() {
		return "quit";
	}

	@Override
	public int call(EnvironmentShell shell, String argString, Environment env, Scanner is, PrintStream os) {
		shell.kill();
		return 0;
	}
}
