package burlap.shell;

import burlap.mdp.core.Domain;
import burlap.mdp.stochasticgames.World;
import burlap.shell.command.ShellCommand;
import burlap.shell.command.world.*;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author James MacGlashan.
 */
public class SGWorldShell extends BurlapShell {

	protected World world;

	public SGWorldShell(Domain domain, InputStream is, PrintStream os, World world) {
		super(domain, is, os);
		this.world = world;

		this.welcomeMessage = "Welcome to the BURLAP stochastic games world shell. Type the command 'help' to bring " +
				"up additional information about using this shell.";

		this.helpText = "Use the command help to bring up this message again. " +
				"Here is a list of standard reserved commands:\n" +
				"cmds - list all known commands.\n" +
				"aliases - list all known command aliases.\n" +
				"alias - set an alias for a command.\n" +
				"quit - terminate this shell.\n\n" +
				"Other useful, but non-reserved, commands are:\n" +
				"obs - print the current observation of the world\n" +
				"ja - specify and execute a joint action\n\n" +
				"Usually, you can get help on an individual command by passing it the -h option.";

	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	@Override
	protected Collection<ShellCommand> generateStandard() {
		ManualAgentsCommands macs = new ManualAgentsCommands();
		return Arrays.<ShellCommand>asList(new WorldObservationCommand(), macs.getRegCommand(), macs.getLsActions(),
				macs.getLsAgents(), macs.getSetAction(), new GameCommand(), new JointActionCommand(),
				new RewardsCommand(), new LastJointActionCommand(), new IsTerminalSGCommand(),
				new GenerateStateCommand(), new AddStateObjectSGCommand(domain),
				new RemoveStateObjectSGCommand(), new SetAttributeSGCommand());
	}



}
