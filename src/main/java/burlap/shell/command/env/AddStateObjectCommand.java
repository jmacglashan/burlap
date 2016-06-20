package burlap.shell.command.env;

import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.extensions.EnvironmentDelegation;
import burlap.mdp.singleagent.environment.extensions.StateSettableEnvironment;
import burlap.shell.BurlapShell;
import burlap.shell.EnvironmentShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * A {@link burlap.shell.command.ShellCommand} for adding an OO-MDP object to the current {@link burlap.mdp.singleagent.environment.Environment}
 * {@link State}. Use the -h option for help information.
 * @author James MacGlashan.
 */
public class AddStateObjectCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("vh*");
	protected OODomain domain = null;

	public AddStateObjectCommand(Domain domain) {
		if(domain instanceof OODomain) {
			this.domain = (OODomain) domain;
		}
	}

	@Override
	public String commandName() {
		return "addOb";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {
		Environment env = ((EnvironmentShell)shell).getEnv();
		OptionSet oset = this.parser.parse(argString.split(" "));
		List<String> args = (List<String>)oset.nonOptionArguments();
		if(oset.has("h")){
			os.println("[-v] objectClass objectName\nAdds an OO-MDP object instance of class objectClass and with name " +
					"objectName to the current state of the environment. The environment must implement StateSettableEnvironment " +
					"for this operation to work. The Java ObjectInstance implementation used will be MutableObjectInstance.\n\n" +
					"-v print the new Environment state after completion.");
			return 0;
		}

		StateSettableEnvironment senv = (StateSettableEnvironment) EnvironmentDelegation.Helper.getDelegateImplementing(env, StateSettableEnvironment.class);
		if(senv == null){
			os.println("Cannot add object to environment state, because the environment does not implement StateSettableEnvironment");
			return 0;
		}

		if(args.size() != 2){
			return -1;
		}

		if(domain == null){
			os.println("Cannot add object to state, because input domain is not an OODomain");
			return 0;
		}

		Class<?> oclass = domain.stateClass(args.get(0));
		if(oclass == null){
			os.println("Cannot add object to state, because the domain does not know about any OO-MDP object class named " + args.get(0));
		}


		ObjectInstance o = null;
		try {
			o = (ObjectInstance)oclass.newInstance();
			o = o.copyWithName(args.get(1));
		} catch(InstantiationException e) {
			return 0;
		} catch(IllegalAccessException e) {
			return 0;
		}

		State s = env.currentObservation();

		if(!(s instanceof MutableOOState)){
			os.println("Cannot add object to state, because the state of the environment does not implement MutableOOState");
		}

		((MutableOOState)s).addObject(o);
		senv.setCurStateTo(s);

		if(oset.has("v")){
			os.println(senv.currentObservation().toString());
		}

		return 1;
	}
}
