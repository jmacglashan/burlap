package burlap.shell.command.env;

import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentDelegation;
import burlap.mdp.singleagent.environment.StateSettableEnvironment;
import burlap.shell.BurlapShell;
import burlap.shell.EnvironmentShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * A {@link burlap.shell.command.ShellCommand} for setting state variables values for the current {@link burlap.mdp.singleagent.environment.Environment}
 * {@link State}. Use the -h option for help information.
 * @author James MacGlashan.
 */
public class SetVarCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("vh*");

	@Override
	public String commandName() {
		return "setVar";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		Environment env = ((EnvironmentShell)shell).getEnv();
		OptionSet oset = this.parser.parse(argString.split(" "));
		List<String> args = (List<String>)oset.nonOptionArguments();
		if(oset.has("h")){
			os.println("[-v] [key value]+ \nSets the values for one or more state variables in an " +
					"environment state. Requires one or more key value pairs." +
					"The environment must implement StateSettableEnvironment and the states must be MutableState instances\n\n" +
					"-v print the new Environment state after completion.");
			return 0;
		}

		StateSettableEnvironment senv = (StateSettableEnvironment) EnvironmentDelegation.EnvDelegationTools.getDelegateImplementing(env, StateSettableEnvironment.class);
		if(senv == null){
			os.println("Cannot set object values for environment states, because the environment does not implement StateSettableEnvironment");
			return 0;
		}

		if(args.size() % 2 != 0 && args.size() < 3){
			return -1;
		}

		State s = env.currentObservation();
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
		senv.setCurStateTo(s);
		if(oset.has("v")){
			os.println(senv.currentObservation().toString());
		}

		return 1;
	}
}
