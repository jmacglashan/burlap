package burlap.oomdp.singleagent.explorer;

import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.state.State;
import burlap.oomdp.singleagent.common.NullRewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.shell.EnvironmentShell;

import java.io.PrintStream;


/**
 * This class is deprecated. You should use {@link burlap.shell.EnvironmentShell} instead. Currently,
 * this class merely wraps {@link burlap.shell.EnvironmentShell}.
 * @author James MacGlashan
 *
 */
@Deprecated
public class TerminalExplorer {
	
	protected EnvironmentShell shell;
	
	/**
	 * Initializes the explorer  shell with the specified domain using a {@link burlap.oomdp.singleagent.environment.SimulatedEnvironment} with
	 * a {@link burlap.oomdp.singleagent.common.NullRewardFunction} and {@link burlap.oomdp.auxiliary.common.NullTermination}
	 * @param domain the domain to explore
	 * @param baseState the initial {@link State} of the {@link burlap.oomdp.singleagent.environment.SimulatedEnvironment}
	 */
	public TerminalExplorer(Domain domain, State baseState){
		SimulatedEnvironment env = new SimulatedEnvironment(domain, new NullRewardFunction(), new NullTermination(), baseState);
		shell = new EnvironmentShell(domain, env, System.in, new PrintStream(System.out));
	}


	/**
	 * Initializes.
	 * @param domain the {@link burlap.oomdp.core.Domain} to explore
	 * @param env the {@link burlap.oomdp.singleagent.environment.Environment} with which to interact.
	 */
	public TerminalExplorer(Domain domain, Environment env){
		shell = new EnvironmentShell(domain, env, System.in, new PrintStream(System.out));
	}
	
	
	/**
	 * Starts the shell.
	 */
	public void explore(){


		this.shell.start();
		
		
	}

	public EnvironmentShell getShell(){
		return this.shell;
	}

	public void setShell(EnvironmentShell shell) {
		this.shell = shell;
	}
}
