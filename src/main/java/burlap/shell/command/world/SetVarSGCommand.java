package burlap.shell.command.world;

import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.world.World;
import burlap.shell.BurlapShell;
import burlap.shell.SGWorldShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * A {@link burlap.shell.command.ShellCommand} for setting state variables values for the current {@link World}
 * {@link State}. Use the -h option for help information.
 * @author James MacGlashan.
 */
public class SetVarSGCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("vh*");

	@Override
	public String commandName() {
		return "setVar";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		OptionSet oset = this.parser.parse(argString.split(" "));
		List<String> args = (List<String>)oset.nonOptionArguments();
		if(oset.has("h")){
			os.println("[-v] [key value]+ \nSets the values for one or more state variables in a " +
					"world state.  Requires 1 or more key value pairs." +
					"\n\n" +
					"-v print the new world state after completion.");
			return 0;
		}

		World w = ((SGWorldShell)shell).getWorld();

		if(w.gameIsRunning()){
			os.println("Cannot manually change state while a game is running.");
			return 0;
		}

		if(args.size() % 2 != 0 && args.size() < 3){
			return -1;
		}

		State s = w.getCurrentWorldState().copy();
		if(!(s instanceof MutableState)){
			os.println("Cannot modify state values, because the state does not implement MutableState");
		}


		for(int i = 0; i < args.size(); i+=2){
			try{
				((MutableState)s).set(args.get(i), args.get(i+1));
			}catch(Exception e){
				os.println("Could not set key " + args.get(i) + " to value " + args.get(i+1) + ". Aborting.");
				return 0;
			}
		}
		w.setCurrentState(s);
		if(oset.has("v")){
			os.println(s.toString());
		}

		return 1;
	}
}
