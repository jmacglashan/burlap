package burlap.oomdp.singleagent.environment.shell.command.reserved;

import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.shell.EnvironmentShell;
import burlap.oomdp.singleagent.environment.shell.command.ShellCommand;

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
	public int call(EnvironmentShell shell, String argString, Environment env, Scanner is, PrintStream os) {
		Set<Map.Entry<String, String>> aliases = shell.getAliases();
		for(Map.Entry<String, String> e : aliases){
			os.println(e.getKey() + " -> " + e.getValue());
		}
		return 0;
	}
}
