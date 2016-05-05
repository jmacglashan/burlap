package burlap.shell.command.world;

import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.World;
import burlap.mdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.shell.BurlapShell;
import burlap.shell.SGWorldShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * A {@link burlap.shell.command.ShellCommand} for printing the last joint action taken in a {@link burlap.mdp.stochasticgames.World}.
 * Use the -h option for help information.
 * @author James MacGlashan.
 */
public class LastJointActionCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("h*");

	@Override
	public String commandName() {
		return "lja";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {
		OptionSet oset = this.parser.parse(argString.split(" "));

		if(oset.has("h")){
			os.println("Prints last joint action executed in the world.");
			return 0;
		}

		World w = ((SGWorldShell)shell).getWorld();

		JointAction ja = w.getLastJointAction();
		if(ja == null){
			os.println("No joint actions have been executed.");
			return 0;
		}
		for(GroundedSGAgentAction a : ja){
			os.println(a.toString());
		}

		return 0;
	}
}
