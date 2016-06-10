package burlap.shell.command.env;

import burlap.mdp.singleagent.environment.Environment;
import burlap.shell.BurlapShell;
import burlap.shell.EnvironmentShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * A {@link burlap.shell.command.ShellCommand} for checking if the {@link burlap.mdp.singleagent.environment.Environment}
 * is in a terminal state. Use the -h option for help information.
 * @author James MacGlashan.
 */
public class IsTerminalCommand implements ShellCommand{

	protected OptionParser parser = new OptionParser("h*");

	@Override
	public String commandName() {
		return "term";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		Environment env = ((EnvironmentShell)shell).getEnv();
		OptionSet oset = this.parser.parse(argString.split(" "));
		if(oset.has("h")){
			os.println("Prints whether the environment is in a terminal state or not (true if so, false otherwise)");
			return 0;
		}

		os.println("" + env.isInTerminalState());

		return 0;
	}
}
