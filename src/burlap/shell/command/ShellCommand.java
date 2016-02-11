package burlap.shell.command;

import burlap.shell.BurlapShell;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * An interface for implementing shell commands. Requires implementing a default name for the command and a
 * call command for what should be executed when the command is called. When executed, commands return
 * a return code. -1 indicates a parsing error. 0 indicates standard completion.
 * 1 indicates that an important external variable, such as the state of an {@link burlap.oomdp.singleagent.environment.Environment} was changed.
 * @author James MacGlashan.
 */
public interface ShellCommand {

	/**
	 * Returns the default name of this command.
	 * @return the default name of this command.
	 */
	String commandName();

	/**
	 * Executes this command.
	 * @param shell the calling {@link burlap.shell.BurlapShell}
	 * @param argString the argument string that was associated with this command.
	 * @param is a {@link java.util.Scanner} that can be used to read input from a user.
	 * @param os a {@link java.io.OutputStream} to write output
	 * @return a return code of the command. -1 indicates a parsing error. 0 indicates standard completion. 1 indicates that an important external variable, such as the state of an {@link burlap.oomdp.singleagent.environment.Environment} was changed.
	 */
	int call(BurlapShell shell, String argString, Scanner is, PrintStream os);

}
