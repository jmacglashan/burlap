package burlap.shell.command.reserved;

import burlap.shell.BurlapShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * A reserved {@link burlap.shell.command.ShellCommand} for listing all shell commands. Use the -h option for help information.
 * @author James MacGlashan.
 */
public class CommandsCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("h*");

	@Override
	public String commandName() {
		return "cmds";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		OptionSet oset = parser.parse(argString.split(" "));
		if(oset.has("h")){
			os.println("Lists all commands this shell can execute.");
			return 0;
		}

		for(String command : shell.getCommands()){
			os.println(command);
		}
		return 0;
	}
}
