package burlap.shell.command.world;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.state.State;
import burlap.oomdp.core.oo.state.MutableOOState;
import burlap.oomdp.core.oo.state.ObjectInstance;
import burlap.oomdp.stochasticgames.World;
import burlap.oomdp.stochasticgames.oo.OOSGDomain;
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
 * {@link State}. Use the -h option for help information.
 * @author James MacGlashan.
 */
public class AddStateObjectSGCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("vh*");
	protected OOSGDomain domain = null;

	public AddStateObjectSGCommand(Domain domain) {
		if(domain instanceof OOSGDomain) {
			this.domain = (OOSGDomain)domain;
		}
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

		if(domain == null){
			os.println("Cannot add object to state, because input domain is not an OODomain");
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


		Class<?> oclass = domain.stateClass(args.get(0));
		if(oclass == null){
			os.println("Cannot add object to state, because the domain does not know about any OO-MDP object class named " + args.get(0));
		}


		ObjectInstance o = null;
		try {
			o = (ObjectInstance)oclass.newInstance();
			o.setName(args.get(1));
		} catch(InstantiationException e) {
			return 0;
		} catch(IllegalAccessException e) {
			return 0;
		}

		State s = w.getCurrentWorldState().copy();
		if(!(s instanceof MutableOOState)){
			os.println("Cannot add object to state, because the state of the environment does not implement MutableOOState");
		}
		((MutableOOState)s).addObject(o);


		w.setCurrentState(s);

		if(oset.has("v")){
			os.println(s.toString());
		}

		return 1;
	}
}
