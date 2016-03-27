package burlap.shell.command.env;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
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
 * A {@link ShellCommand} for listing the set of applicable actions given the current environment observation.
 * Use the -h option for help information and options.
 * @author James MacGlashan.
 */
public class ListActionsCommand implements ShellCommand{

	protected OptionParser parser = new OptionParser("nsh*");

	@Override
	public String commandName() {
		return "lsa";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		Environment env = ((EnvironmentShell)shell).getEnv();

		OptionSet oset = this.parser.parse(argString.split(" "));

		if(oset.has("h")){
			os.println("[s]\nCommand to list applicable and executable actions for the current environment observation.\n" +
					"-n: list the name of all known actions (no parameters specified), regardless of whether they are applicable in the current observation\n" +
					"-s: query applicable actions w.r.t. a POMDP hidden state, rather than environment observation. Environment must extend SimulatedPOEnvironment");

			return 0;
		}


		if(oset.has("n")){
			for(Action a : shell.getDomain().getActions()){
				os.println(a.getName());
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

		List<GroundedAction> actions = Action.getAllApplicableGroundedActionsFromActionList(shell.getDomain().getActions(), qs);
		for(GroundedAction ga : actions){
			os.println(ga.toString());
		}

		return 0;
	}
}
