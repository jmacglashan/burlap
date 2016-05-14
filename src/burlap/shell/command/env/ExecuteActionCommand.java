package burlap.shell.command.env;

import burlap.mdp.core.Action;
import burlap.mdp.core.Domain;
import burlap.mdp.singleagent.ActionType;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.shell.BurlapShell;
import burlap.shell.EnvironmentShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * A {@link burlap.shell.command.ShellCommand} for executing an action in the {@link burlap.mdp.singleagent.environment.Environment}
 * Use the -h option for help information.
 * @author James MacGlashan.
 */
public class ExecuteActionCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("vah*");
	protected Map<String, String> actionNameMap = new HashMap<String, String>();
	protected Domain domain;

	public ExecuteActionCommand(Domain domain) {
		this.domain = domain;
	}

	@Override
	public String commandName() {
		return "ex";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		Environment env = ((EnvironmentShell)shell).getEnv();

		OptionSet oset = this.parser.parse(argString.split(" "));
		List<String> args = (List<String>)oset.nonOptionArguments();

		if(oset.has("h")){
			os.println("[v|a] args*\nCommand to execute an action or set an action name alias.\n" +
					"If -a is not specified, then executes the action with name args[0] with parameters args[1]*\n" +
					"-v: the resulting reward, termination, and observation from execution is printed.\n" +
					"-a: assigns an action name alias where args[0] is the original action name, and args[1] is the alias.");

			return 0;
		}

		if(oset.has("a")){
			if(args.size() != 2){
				return  -1;
			}
			this.actionNameMap.put(args.get(1), args.get(0));
			return 0;
		}

		if(args.isEmpty()){
			return -1;
		}

		ActionType actionType = this.domain.getAction(args.get(0));
		if(actionType == null){
			String actionName = this.actionNameMap.get(args.get(0));
			if(actionName != null){
				actionType = this.domain.getAction(actionName);
			}
		}
		if(actionType != null){
			Action a = actionType.associatedAction(actionArgs(args));
			EnvironmentOutcome o = env.executeAction(a);
			if(oset.has("v")){
				os.println("reward: " + o.r);
				if(o.terminated){
					os.println("IS terminal");
				}
				else{
					os.println("is NOT terminal");
				}
				os.println(o.op.toString());
			}
			return 1;
		}


		return -1;
	}

	protected String actionArgs(List<String> commandArgs){
		StringBuilder buf = new StringBuilder();
		for(int i = 1; i < commandArgs.size(); i++){
			if(i > 1){
				buf.append(" ");
			}
			buf.append(commandArgs.get(i));
		}
		return buf.toString();
	}


}
