package burlap.oomdp.singleagent.environment.shell.command.std;

import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.shell.EnvironmentShell;
import burlap.oomdp.singleagent.environment.shell.command.ShellCommand;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * @author James MacGlashan.
 */
public class ObservationCommand implements ShellCommand {

	@Override
	public String commandName() {
		return "obs";
	}

	@Override
	public int call(EnvironmentShell shell, String argString, Environment env, Scanner is, PrintStream os) {
		os.println(env.getCurrentObservation().getCompleteStateDescriptionWithUnsetAttributesAsNull());
		return 0;
	}
}
