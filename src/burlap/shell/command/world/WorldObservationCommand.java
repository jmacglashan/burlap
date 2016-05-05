package burlap.shell.command.world;

import burlap.mdp.stochasticgames.World;
import burlap.shell.BurlapShell;
import burlap.shell.SGWorldShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * A {@link burlap.shell.command.ShellCommand} for printing the current observation from the shell's {@link burlap.mdp.stochasticgames.World}.
 * Use the -h option for help information.
 * @author James MacGlashan.
 */
public class WorldObservationCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("h*");

	@Override
	public String commandName() {
		return "obs";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		OptionSet oset = this.parser.parse(argString.split(" "));
		if(oset.has("h")){
			os.println("Prints the current observation from the world.");
			return 0;
		}

		World world = ((SGWorldShell)shell).getWorld();
		os.println(world.getCurrentWorldState().toString());

		return 0;
	}
}
