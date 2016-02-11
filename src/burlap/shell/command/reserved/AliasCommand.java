package burlap.shell.command.reserved;

import burlap.shell.BurlapShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * A reserved {@link burlap.shell.command.ShellCommand} for creating a command alias for a given command. Use the -h option for help information.
 * @author James MacGlashan.
 */
public class AliasCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("fh*");

	@Override
	public String commandName() {
		return "alias";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {
		OptionSet oset = parser.parse(argString.split(" "));
		List<String> arguments = (List<String>)oset.nonOptionArguments();

		if(oset.has("h")){
			os.println("[-f] commandName alias\n-f: force assignment of alias even if existing command has the name of the alias.");
		}

		if(arguments.size() != 2){
			return -2;
		}

		if(oset.has("f")){
			shell.setAlias(arguments.get(0), arguments.get(1));
		}
		else{
			shell.setAlias(arguments.get(0), arguments.get(1), true);
		}

		return 0;
	}

}
