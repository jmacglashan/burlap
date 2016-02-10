package burlap.shell.command.reserved;

import burlap.shell.BurlapShell;
import burlap.shell.command.ShellCommand;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * @author James MacGlashan.
 */
public class HelpCommand implements ShellCommand {

	@Override
	public String commandName() {
		return "help";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {
		os.println(shell.getHelpText());
		return 0;
	}
}
