package burlap.shell.command.reserved;

import burlap.shell.BurlapShell;
import burlap.shell.command.ShellCommand;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * @author James MacGlashan.
 */
public class CommandsCommand implements ShellCommand {

	@Override
	public String commandName() {
		return "cmds";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {
		for(String command : shell.getCommands()){
			os.println(command);
		}
		return 0;
	}
}
