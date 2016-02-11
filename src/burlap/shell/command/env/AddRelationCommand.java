package burlap.shell.command.env;

import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
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
 * A {@link burlap.shell.command.ShellCommand} for adding a relational target to an attribute for the current {@link burlap.oomdp.singleagent.environment.Environment}
 * {@link burlap.oomdp.core.states.State}. Use the -h option for help information.
 * @author James MacGlashan.
 */
public class AddRelationCommand implements ShellCommand{

	protected OptionParser parser = new OptionParser("vh*");

	@Override
	public String commandName() {
		return "addRelation";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {
		Environment env = ((EnvironmentShell)shell).getEnv();
		OptionSet oset = this.parser.parse(argString.split(" "));
		List<String> args = (List<String>)oset.nonOptionArguments();
		if(oset.has("h")){
			os.println("[-v] objectName attribute [value]+ \nAdds a relation (or list of relations) to an object's relational attributes in an " +
					"environment state. First argument is the name of the object, then the attribute name and the list of relational values to add" +
					"The environment must implement StateSettableEnvironment\n\n" +
					"-v print the new Environment state after completion.");
			return 0;
		}

		StateSettableEnvironment senv = (StateSettableEnvironment) EnvironmentDelegation.EnvDelegationTools.getDelegateImplementing(env, StateSettableEnvironment.class);
		if(senv == null){
			os.println("Cannot set object values for environment states, because the environment does not implement StateSettableEnvironment");
			return 0;
		}

		if(args.size() < 3){
			return -1;
		}

		State s = env.getCurrentObservation();
		ObjectInstance o = s.getObject(args.get(0));
		if(o == null){
			os.println("Unknown object " + args.get(0));
			return 0;
		}
		for(int i = 2; i < args.size(); i++){
			try{
				o.addRelationalTarget(args.get(1), args.get(i));
			}catch(Exception e){
				os.println("Could not add relational value " + args.get(2) + " to attribute " + args.get(1) + ". Aborting.");
				return 0;
			}
		}


		senv.setCurStateTo(s);
		if(oset.has("v")){
			os.println(senv.getCurrentObservation().getCompleteStateDescriptionWithUnsetAttributesAsNull());
		}

		return 1;
	}
}
