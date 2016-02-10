package burlap.oomdp.singleagent.environment.shell;

import burlap.oomdp.singleagent.environment.shell.command.ShellCommand;

/**
 * @author James MacGlashan.
 */
public interface ShellObserver {

	void observeCommand(EnvironmentShell shell, ShellCommandEvent event);


	public static class ShellCommandEvent{
		public String commandString;
		public ShellCommand command;
		public int returnCode;
	}

}
