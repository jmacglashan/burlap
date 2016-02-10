package burlap.oomdp.singleagent.environment.shell.command.reserved;

import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.shell.EnvironmentShell;
import burlap.oomdp.singleagent.environment.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * @author James MacGlashan.
 */
public class AliasCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("fh*");

	@Override
	public String commandName() {
		return "alias";
	}

	@Override
	public int call(EnvironmentShell shell, String argString, Environment env, Scanner is, PrintStream os) {
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
