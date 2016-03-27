package burlap.shell.command.world;

import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.World;
import burlap.shell.BurlapShell;
import burlap.shell.SGWorldShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * A {@link burlap.shell.command.ShellCommand} for removing an OO-MDP object from the current {@link burlap.oomdp.stochasticgames.World}
 * {@link burlap.oomdp.core.states.State}. Use the -h option for help information.
 * @author James MacGlashan.
 */
public class RemoveStateObjectSGCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("vh*");

	@Override
	public String commandName() {
		return "rmOb";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		OptionSet oset = this.parser.parse(argString.split(" "));
		List<String> args = (List<String>)oset.nonOptionArguments();
		if(oset.has("h")){
			os.println("[-v] objectName\nRemoves an OO-MDP object instance with name objectName" +
					"\n\n" +
					"-v print the new world state after completion.");
			return 0;
		}


		World w = ((SGWorldShell)shell).getWorld();

		if(w.gameIsRunning()){
			os.println("Cannot manually change state while a game is running.");
			return 0;
		}

		if(args.size() != 1){
			return -1;
		}

		State s = w.getCurrentWorldState().copy();
		s.removeObject(args.get(0));
		w.setCurrentState(s);

		if(oset.has("v")){
			os.println(s.getCompleteStateDescriptionWithUnsetAttributesAsNull());
		}

		return 1;
	}
}
