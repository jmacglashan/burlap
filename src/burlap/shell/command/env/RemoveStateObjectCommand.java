package burlap.shell.command.env;

import burlap.oomdp.core.state.State;
import burlap.oomdp.core.oo.state.MutableOOState;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentDelegation;
import burlap.oomdp.singleagent.environment.StateSettableEnvironment;
import burlap.shell.BurlapShell;
import burlap.shell.EnvironmentShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * A {@link burlap.shell.command.ShellCommand} for removing an OO-MDP object from the current {@link burlap.oomdp.singleagent.environment.Environment}
 * {@link State}. Use the -h option for help information.
 * @author James MacGlashan.
 */
public class RemoveStateObjectCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("vh*");

	@Override
	public String commandName() {
		return "rmOb";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		Environment env = ((EnvironmentShell)shell).getEnv();
		OptionSet oset = this.parser.parse(argString.split(" "));
		List<String> args = (List<String>)oset.nonOptionArguments();
		if(oset.has("h")){
			os.println("[-v] objectName\nRemoves an OO-MDP object instance with name objectName" +
					"from the current state of the environment. The environment must implement StateSettableEnvironment " +
					"for this operation to work.\n\n" +
					"-v print the new Environment state after completion.");
			return 0;
		}

		StateSettableEnvironment senv = (StateSettableEnvironment) EnvironmentDelegation.EnvDelegationTools.getDelegateImplementing(env, StateSettableEnvironment.class);
		if(senv == null){
			os.println("Cannot remove object from environment state, because the environment does not implement StateSettableEnvironment");
			return 0;
		}

		if(args.size() != 1){
			return -1;
		}

		State s = env.getCurrentObservation();

		if(!(s instanceof MutableOOState)){
			os.println("Cannot remove object from state, because state is not a MutableOOState");
			return 0;
		}

		((MutableOOState)s).removeObject(args.get(0));
		senv.setCurStateTo(s);

		if(oset.has("v")){
			os.println(env.getCurrentObservation().toString());
		}

		return 1;
	}
}
