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
 * A {@link burlap.shell.command.ShellCommand} printing the current observation in the {@link burlap.mdp.singleagent.environment.Environment}
 * Use the -h option for help information.
 * @author James MacGlashan.
 */
public class ObservationCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("h*");

	@Override
	public String commandName() {
		return "obs";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		OptionSet oset = this.parser.parse(argString.split(" "));
		if(oset.has("h")){
			os.println("Prints the current observation from the environment.");
			return 0;
		}

		Environment env = ((EnvironmentShell)shell).getEnv();
		os.println(env.getCurrentObservation().toString());
		return 0;
	}
}
