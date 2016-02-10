package burlap.oomdp.singleagent.environment.shell.command.reserved;

import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.shell.EnvironmentShell;
import burlap.oomdp.singleagent.environment.shell.command.ShellCommand;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * @author James MacGlashan.
 */
public class CommandsCommand implements ShellCommand {

	@Override
	public String commandName() {
		return "cmds";
	}

	@Override
	public int call(EnvironmentShell shell, String argString, Environment env, Scanner is, PrintStream os) {
		for(String command : shell.getCommands()){
			os.println(command);
		}
		return 0;
	}
}
