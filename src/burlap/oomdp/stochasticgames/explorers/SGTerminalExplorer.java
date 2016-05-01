package burlap.oomdp.stochasticgames.explorers;

import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.state.State;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.World;
import burlap.oomdp.stochasticgames.common.NullJointReward;
import burlap.shell.SGWorldShell;

import java.io.PrintStream;



/**
 * A wrapper class for {@link burlap.shell.SGWorldShell}, that is deprecated. Instead you should just use {@link burlap.shell.SGWorldShell}
 * directly.
 * @author James MacGlashan
 *
 */
@Deprecated
public class SGTerminalExplorer {

	protected SGWorldShell shell;

	/**
	 * Initializes the explorer with a domain and action model
	 * @param domain the domain which will be explored
	 */
	public SGTerminalExplorer(SGDomain domain, State s){
		this.shell = new SGWorldShell(domain, System.in, new PrintStream(System.out), new World(domain, new NullJointReward(), new NullTermination(), s));
	}
	
	public SGTerminalExplorer(World w){
		this.shell = new SGWorldShell(w.getDomain(), System.in, new PrintStream(System.out), w);
	}

	public void explore(){
		this.shell.start();
	}



}
