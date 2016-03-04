package burlap.shell.command.reserved;

import burlap.shell.BurlapShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * A reserved {@link burlap.shell.command.ShellCommand} for listing the set of aliases the shell knows. Use the -h option for help information.
 * @author James MacGlashan.
 */
public class AliasesCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("h*");

	@Override
	public String commandName() {
		return "aliases";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		OptionSet oset = parser.parse(argString.split(" "));
		if(oset.has("h")){
			os.println("This command will list all known command aliases in the form a -> c, where a is the command" +
					"alias you can use to call the command, c");
			return 0;
		}

		Set<Map.Entry<String, String>> aliases = shell.getAliases();
		for(Map.Entry<String, String> e : aliases){
			os.println(e.getKey() + " -> " + e.getValue());
		}
		return 0;
	}
}
