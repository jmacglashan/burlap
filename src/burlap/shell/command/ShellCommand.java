package burlap.shell.command;

import burlap.shell.BurlapShell;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * @author James MacGlashan.
 */
public interface ShellCommand {

	String commandName();
	int call(BurlapShell shell, String argString, Scanner is, PrintStream os);

}
