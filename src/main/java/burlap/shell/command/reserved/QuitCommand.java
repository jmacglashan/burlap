package burlap.shell.command.reserved;

import burlap.shell.BurlapShell;
import burlap.shell.command.ShellCommand;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * A reserved {@link burlap.shell.command.ShellCommand} for terminating a shell.
 * @author James MacGlashan.
 */
public class QuitCommand implements ShellCommand{

	@Override
	public String commandName() {
		return "quit";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {
		shell.kill();
		return 0;
	}
}
