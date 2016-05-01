package burlap.shell.command.env;

import burlap.oomdp.core.oo.OODomain;
import burlap.oomdp.core.oo.propositional.GroundedProp;
import burlap.oomdp.core.oo.propositional.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.pomdp.SimulatedPOEnvironment;
import burlap.shell.BurlapShell;
import burlap.shell.EnvironmentShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * A {@link ShellCommand} for listing the true (or false) {@link GroundedProp}'s given the current environment observation.
 * Use the -h option for help information and options.
 * @author James MacGlashan.
 */
public class ListPropFunctions implements ShellCommand {

	protected OptionParser parser = new OptionParser("fnsh*");

	@Override
	public String commandName() {
		return "lsp";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {
		Environment env = ((EnvironmentShell)shell).getEnv();

		OptionSet oset = this.parser.parse(argString.split(" "));

		if(oset.has("h")){
			os.println("[s]\nCommand to list all true (or false) grounded propositional function for the current environment observation.\n" +
					"-f: list false grounded propositional functions, rather than true ones. " +
					"-n: list the name of all propositional functions, rather than grounded evaluations\n" +
					"-s: evaluate propositional functions on POMDP environment hidden state, rather than environment observation. Environment must extend SimulatedPOEnvironment");

			return 0;
		}


		if(!(shell.getDomain() instanceof OODomain)){
			os.println("cannot query propositional functions because the domain is not an OODomain");
			return 0;
		}

		if(oset.has("n")){
			for(PropositionalFunction pf : ((OODomain)shell.getDomain()).getPropFunctions()){
				os.println(pf.getName());
			}
			return 0;
		}


		State qs = env.getCurrentObservation();

		if(oset.has("s")){
			if(!(env instanceof SimulatedPOEnvironment)){
				os.println("Cannot query applicable actions with respect to POMDP hidden state, because the environment does not extend SimulatedPOEnvironment.");
				return 0;
			}
			qs = ((SimulatedPOEnvironment)env).getCurrentHiddenState();
		}

		List<GroundedProp> gps = PropositionalFunction.getAllGroundedPropsFromPFList(((OODomain)shell.getDomain()).getPropFunctions(), qs);
		for(GroundedProp gp : gps){
			if(gp.isTrue(qs) == !oset.has("f")){
				os.println(gp.toString());
			}
		}

		return 0;
	}
}
