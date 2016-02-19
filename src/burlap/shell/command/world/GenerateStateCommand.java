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
public class GenerateStateCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("vh*");

	@Override
	public String commandName() {
		return "gs";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		OptionSet oset = this.parser.parse(argString.split(" "));

		if(oset.has("h")) {
			os.println("[-v]\nCauses the world to generate a new initial state.\n\n" +
					"-v: print the new state after generating it.");
			return 0;
		}

		World w = ((SGWorldShell)shell).getWorld();
		w.generateNewCurrentState();

		if(oset.has("v")){
			os.println(w.getCurrentWorldState().getCompleteStateDescriptionWithUnsetAttributesAsNull());
		}

		return 1;
	}
}
