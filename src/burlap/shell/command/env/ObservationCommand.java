package burlap.shell.command.env;

import burlap.oomdp.singleagent.environment.Environment;
import burlap.shell.BurlapShell;
import burlap.shell.EnvironmentShell;
import burlap.shell.command.ShellCommand;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * A {@link burlap.shell.command.ShellCommand} printing the current observation in the {@link burlap.oomdp.singleagent.environment.Environment}
 * Use the -h option for help information.
 * @author James MacGlashan.
 */
public class ObservationCommand implements ShellCommand {

	@Override
	public String commandName() {
		return "obs";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		Environment env = ((EnvironmentShell)shell).getEnv();
		os.println(env.getCurrentObservation().getCompleteStateDescriptionWithUnsetAttributesAsNull());
		return 0;
	}
}
