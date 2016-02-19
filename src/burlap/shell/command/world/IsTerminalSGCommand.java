package burlap.shell.command.world;

import burlap.oomdp.stochasticgames.World;
import burlap.shell.BurlapShell;
import burlap.shell.SGWorldShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * @author James MacGlashan.
 */
public class IsTerminalSGCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("h*");

	@Override
	public String commandName() {
		return "term";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {
		World w = ((SGWorldShell)shell).getWorld();
		OptionSet oset = this.parser.parse(argString.split(" "));
		if(oset.has("h")){
			os.println("Prints whether the environment is in a terminal state or not (true if so, false otherwise)");
			return 0;
		}

		os.println("" + w.worldStateIsTerminal());

		return 0;
	}
}
