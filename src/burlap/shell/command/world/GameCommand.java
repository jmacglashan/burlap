package burlap.shell.command.world;

import burlap.debugtools.DPrint;
import burlap.mdp.stochasticgames.world.World;
import burlap.shell.BurlapShell;
import burlap.shell.SGWorldShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * A {@link burlap.shell.command.ShellCommand} for starting a game with the agents registered in a {@link World}.
 * Use the -h option for help information.
 * @author James MacGlashan.
 */
public class GameCommand implements ShellCommand {

	protected OptionParser parser = new OptionParser("s::cih*");

	@Override
	public String commandName() {
		return "game";
	}

	@Override
	public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

		OptionSet oset = this.parser.parse(argString.split(" "));

		if(oset.has("h")){
			os.println("[-s [n] [-i]] [-c]\n" +
					"Starts a game in the world with the registered agents.\n\n" +
					"-s [n]: start the game, with optional int parameter n specifying the maximum number of stages in the game.\n" +
					"-i (with -s): starts the game from the current world state, rather than generating a new one.\n" +
					"-c checks whether a game is already running.");
			return 0;
		}


		final World w = ((SGWorldShell)shell).getWorld();
		DPrint.toggleCode(w.getDebugId(), false);

		if(oset.has("c")){
			if(w.gameIsRunning()){
				os.println("game IS currently running.");
			}
			else{
				os.println("game is NOT currently running.");
			}
		}

		if(oset.has("s")){
			if(w.gameIsRunning()){
				os.println("Game is already running, cannot start a new one.");
			}
			else{


				String sval = (String)oset.valueOf("s");
				int ms = -1;

				if(sval != null) {
					ms = Integer.parseInt(sval);
				}
				final int maxStages = ms;


				if(oset.has("i")){
					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							w.runGame(maxStages);
						}
					});
					thread.start();
				}
				else{
					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							w.runGame(maxStages, w.getCurrentWorldState());
						}
					});
					thread.start();
				}



			}
		}

		return 0;
	}
}
