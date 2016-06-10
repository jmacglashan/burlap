package burlap.shell.command.world;

import burlap.mdp.stochasticgames.world.World;
import burlap.shell.BurlapShell;
import burlap.shell.SGWorldShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;

/**
 * A {@link burlap.shell.command.ShellCommand} for printing the last joint rewards delivered by a {@link World}.
 * Use the -h option for help information.
 * @author James MacGlashan.
 */
public class RewardsCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("h*");

	@Override
	public String commandName() {
		return "rewards";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		OptionSet oset = this.parser.parse(argString.split(" "));

		if(oset.has("h")){
			os.println("Prints last rewards for each agent that acted in the world.");
			return 0;
		}

		World w = ((SGWorldShell)shell).getWorld();

		for(Map.Entry<String, Double> rs : w.getLastRewards().entrySet()){
			os.println(rs.getKey() + ": " + rs.getValue());
		}

		return 0;
	}
}
