package burlap.shell.command.reserved;

import burlap.shell.BurlapShell;
import burlap.shell.command.ShellCommand;

import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * @author James MacGlashan.
 */
public class AliasesCommand implements ShellCommand {

	@Override
	public String commandName() {
		return "aliases";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {
		Set<Map.Entry<String, String>> aliases = shell.getAliases();
		for(Map.Entry<String, String> e : aliases){
			os.println(e.getKey() + " -> " + e.getValue());
		}
		return 0;
	}
}
