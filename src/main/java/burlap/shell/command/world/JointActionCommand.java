package burlap.shell.command.world;

import burlap.mdp.core.Action;
import burlap.mdp.singleagent.action.ActionType;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.world.World;
import burlap.shell.BurlapShell;
import burlap.shell.SGWorldShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * A {@link burlap.shell.command.ShellCommand} for manually setting and executing a {@link JointAction}
 * for the shell's {@link World}. Use the -h option for help information.
 * @author James MacGlashan.
 */
public class JointActionCommand implements ShellCommand{

	protected JointAction ja = new JointAction();
	protected OptionParser parser = new OptionParser("xcpvh*");

	@Override
	public String commandName() {
		return "ja";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		OptionSet oset = this.parser.parse(argString.split(" "));
		List<String> args = (List<String>)oset.nonOptionArguments();

		if(oset.has("h")){
			os.println("[-x [-v]][-c][-p] [agentId actionTypeName [actionParams*]]\n" +
					"Used to manually execute a joint action in the world. Joint actions cannot be manually executed if" +
					"the world is currently running a game with attached agents.\n\n" +
					"-x: execute the joint action (executes after setting specified agent action)\n" +
					"-c: clear the joint action (clears after execution if -x is specified)\n" +
					"-p: print the joint action (before execution and clearing)\n" +
					"-v: prints the new state rewards and terminal state condition after executing.");
			return 0;
		}

		if(!args.isEmpty()){
			int agentId = Integer.parseInt(args.get(0));

			String aname = args.get(1);

			ActionType action = ((SGDomain)shell.getDomain()).getActionType(aname);
			if(action == null){
				os.println("Cannot set action to " + aname + " because that action name is not known.");
				return 0;
			}

			Action ga = action.associatedAction(this.actionArgs(args));

			ja.setAction(agentId, ga);
		}

		if(oset.has("p")){
			os.println(ja.toString());
		}

		World w = ((SGWorldShell)shell).getWorld();

		boolean changed = false;
		if(oset.has("x")){
			if(w.gameIsRunning()){
				os.println("Cannot manually execute joint action, because a game with attached agents is running in the world.");
			}
			else{
				changed = true;
				w.executeJointAction(ja);
				this.ja = ja.copy();

				if(oset.has("v")){

					os.println(w.getCurrentWorldState().toString());
					os.println(Arrays.toString(w.getLastRewards()));
					if(w.worldStateIsTerminal()){
						os.println("IS a terminal state.");
					}
					else{
						os.println("is NOT a terminal state.");
					}

				}

			}
		}

		if(oset.has("c")){
			this.ja = new JointAction();
		}

		if(changed){
			return 1;
		}

		return 0;
	}


	/**
	 * Sets the action for a single agent in the joint action this shell command controls
	 * @param actingAgent the agent for whom the action is to be set
	 * @param a the action
	 */
	public void setAction(int actingAgent, Action a){
		this.ja.setAction(actingAgent, a);
	}




	protected String actionArgs(List<String> commandArgs){
		StringBuilder buf = new StringBuilder();
		for(int i = 2; i < commandArgs.size(); i++){
			if(i > 2){
				buf.append(" ");
			}
			buf.append(commandArgs.get(i));
		}
		return buf.toString();
	}
}
