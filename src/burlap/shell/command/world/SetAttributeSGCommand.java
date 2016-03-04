package burlap.shell.command.world;

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
 * A {@link burlap.shell.command.ShellCommand} for setting attribute values for the current {@link burlap.oomdp.stochasticgames.World}
 * {@link burlap.oomdp.core.states.State}. Use the -h option for help information.
 * @author James MacGlashan.
 */
public class SetAttributeSGCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("vh*");

	@Override
	public String commandName() {
		return "setAtt";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		OptionSet oset = this.parser.parse(argString.split(" "));
		List<String> args = (List<String>)oset.nonOptionArguments();
		if(oset.has("h")){
			os.println("[-v] objectName [attribute value]+ \nSets the values for one or more attributes in a " +
					"world state. First argument is the name of the object, then a list of attribute value pairs." +
					"\n\n" +
					"-v print the new world state after completion.");
			return 0;
		}

		World w = ((SGWorldShell)shell).getWorld();

		if(w.gameIsRunning()){
			os.println("Cannot manually change state while a game is running.");
			return 0;
		}

		if(args.size() % 2 != 1 && args.size() < 3){
			return -1;
		}

		State s = w.getCurrentWorldState().copy();
		ObjectInstance o = s.getObject(args.get(0));
		if(o == null){
			os.println("Unknown object " + args.get(0));
			return 0;
		}
		for(int i = 1; i < args.size(); i+=2){
			try{
				o.setValue(args.get(i), args.get(i+1));
			}catch(Exception e){
				os.println("Could not set attribute " + args.get(i) + " to value " + args.get(i+1) + ". Aborting.");
				return 0;
			}
		}
		w.setCurrentState(s);
		if(oset.has("v")){
			os.println(s.getCompleteStateDescriptionWithUnsetAttributesAsNull());
		}

		return 1;
	}
}
