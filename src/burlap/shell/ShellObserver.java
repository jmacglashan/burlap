package burlap.shell;

import burlap.shell.command.ShellCommand;

/**
 * An interface that allows an object to receive messages about {@link burlap.shell.BurlapShell} {@link burlap.shell.command.ShellCommand} execution
 * completion.
 * @author James MacGlashan.
 */
public interface ShellObserver {

	/**
	 * Received whenever a {@link burlap.shell.command.ShellCommand} completes execution.
	 * @param shell the calling {@link burlap.shell.BurlapShell}
	 * @param event the command event, stored as a {@link burlap.shell.ShellObserver.ShellCommandEvent}
	 */
	void observeCommand(BurlapShell shell, ShellCommandEvent event);


	/**
	 * Stores information about a command event in various public data members.
	 */
	public static class ShellCommandEvent{

		/**
		 * The shell command string that was executed.
		 */
		public String commandString;

		/**
		 * The resolved {@link burlap.shell.command.ShellCommand} responsible for carrying out the command string.
		 */
		public ShellCommand command;

		/**
		 * The return code of the command.
		 */
		public int returnCode;


		/**
		 * Initializes.
		 * @param commandString The shell command string that was executed.
		 * @param command The resolved {@link burlap.shell.command.ShellCommand} responsible for carrying out the command string.
		 * @param returnCode The return code of the command.
		 */
		public ShellCommandEvent(String commandString, ShellCommand command, int returnCode) {
			this.commandString = commandString;
			this.command = command;
			this.returnCode = returnCode;
		}
	}

}
