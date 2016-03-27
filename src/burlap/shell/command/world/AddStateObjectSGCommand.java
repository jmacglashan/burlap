package burlap.shell.command.world;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
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
 * A {@link burlap.shell.command.ShellCommand} for adding an OO-MDP object to the current {@link burlap.oomdp.stochasticgames.World}
 * {@link burlap.oomdp.core.states.State}. Use the -h option for help information.
 * @author James MacGlashan.
 */
public class AddStateObjectSGCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("vh*");
	protected Domain domain;

	public AddStateObjectSGCommand(Domain domain) {
		this.domain = domain;
	}

	@Override
	public String commandName() {
		return "addOb";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		OptionSet oset = this.parser.parse(argString.split(" "));
		List<String> args = (List<String>)oset.nonOptionArguments();
		if(oset.has("h")){
			os.println("[-v] objectClass objectName\nAdds an OO-MDP object instance of class objectClass and with name " +
					"objectName to the current state of the world." +
					"The Java ObjectInstance implementation used will be MutableObjectInstance.\n\n" +
					"-v print the new world state after completion.");
			return 0;
		}

		World w = ((SGWorldShell)shell).getWorld();

		if(w.gameIsRunning()){
			os.println("Cannot manually change state while a game is running.");
			return 0;
		}

		if(args.size() != 2){
			return -1;
		}

		ObjectInstance o = new MutableObjectInstance(domain.getObjectClass(args.get(0)), args.get(1));
		State s = w.getCurrentWorldState().copy();
		s.addObject(o);
		w.setCurrentState(s);

		if(oset.has("v")){
			os.println(s.getCompleteStateDescriptionWithUnsetAttributesAsNull());
		}

		return 1;
	}
}
