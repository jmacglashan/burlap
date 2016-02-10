package burlap.oomdp.singleagent.environment.shell.command;

import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.shell.EnvironmentShell;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * @author James MacGlashan.
 */
public interface ShellCommand {

	String commandName();
	int call(EnvironmentShell shell, String argString, Environment env, Scanner is, PrintStream os);

}
