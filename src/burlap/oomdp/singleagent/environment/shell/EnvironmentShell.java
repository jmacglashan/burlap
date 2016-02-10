package burlap.oomdp.singleagent.environment.shell;

import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldRewardFunction;
import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.singleagent.environment.shell.command.ShellCommand;
import burlap.oomdp.singleagent.environment.shell.command.reserved.*;
import burlap.oomdp.singleagent.environment.shell.command.std.*;
import burlap.oomdp.visualizer.Visualizer;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * @author James MacGlashan.
 */
public class EnvironmentShell {

	protected Environment env;
	protected InputStream is;
	protected PrintStream os;
	protected Scanner scanner;
	protected Domain domain;
	protected Visualizer visualizer;

	protected Map<String, ShellCommand> commands = new HashMap<String, ShellCommand>();
	protected Map<String, String> aliases = new HashMap<String, String>();
	protected Set<String> reserved;

	protected volatile boolean kill = false;

	protected String welcomeMessage = "Welcome to the BURLAP agent environment shell. Type the command 'help' to bring " +
			"up additional information about using this shell.";

	protected String helpText = "Use the command help to bring up this message again. " +
			"Here is a list of standard reserved commands:\n" +
			"cmds - list all known commands.\n" +
			"aliases - list all known command aliases.\n" +
			"alias - set an alias for a command.\n" +
			"quit - terminate this shell.\n\n" +
			"Other useful, but non-reserved, commands are:\n" +
			"obs - print the current observation of the environment\n" +
			"ex - execute an action\n\n" +
			"Usually, you can get help on an individual command by passing it the -h option.";


	public EnvironmentShell(Domain domain, Environment env, InputStream is, PrintStream os) {
		this.env = env;
		this.is = is;
		this.os = os;
		this.scanner = new Scanner(is);
		this.domain = domain;

		Collection<ShellCommand> res = this.generateReserved();
		this.reserved = new HashSet<String>(res.size());
		for(ShellCommand c : res){
			this.addCommand(c);
			this.reserved.add(c.commandName());
		}
		Collection<ShellCommand> std = this.generateStandard();
		for(ShellCommand c : std){
			this.addCommand(c);
		}

	}

	public void addCommand(ShellCommand command){
		if(reserved.contains(command.commandName())){
			os.println("Cannot add command " + command.commandName() + " because that is a reserved name. " +
					"Consider using addCommand(ShellCommand command, String as); to add it under a different name");
		}
		else{
			commands.put(command.commandName(), command);
		}
	}

	public void addCommandAs(ShellCommand command, String as){
		if(reserved.contains(as)) {
			os.println("Cannot add command " + command.commandName() + " as " + as + " because that is a reserved name. " +
					"Please add it as a different name.");
		}
		else{
			commands.put(as, command);
		}
	}

	public void setAlias(String commandName, String alias){
		this.setAlias(commandName, alias, false);
	}
	public void setAlias(String commandName, String alias, boolean force){

		if(reserved.contains(alias)){
			os.println("Cannot give " + commandName + " the alias " + alias + " because that name is reserved.");
		}
		else if(commands.containsKey(alias) && !force){
			os.println("Cannot give " + commandName + " the alias " + alias + " because that name is already assigned" +
					"to a command. If you wish to override, use the force option");
		}
		else{
			aliases.put(alias, commandName);
			if(commands.containsKey(alias) && !alias.equals(commandName)){
				this.commands.remove(alias);
			}
		}

	}

	public void removeAlias(String alias){
		this.aliases.remove(alias);
	}

	public void removeCommand(String command){
		if(!this.reserved.contains(command)) {
			this.commands.remove(command);
		}
		else{
			os.println("Cannot remove command " + command + " because it is a reserved command.");
		}
	}

	public String getHelpText() {
		return helpText;
	}

	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	public String getWelcomeMessage() {
		return welcomeMessage;
	}

	public void setWelcomeMessage(String welcomeMessage) {
		this.welcomeMessage = welcomeMessage;
	}

	public void kill(){
		this.kill = true;
	}


	public InputStream getIs() {
		return is;
	}

	public void setIs(InputStream is) {
		this.is = is;
		this.scanner = new Scanner(is);
	}

	public PrintStream getOs() {
		return os;
	}

	public void setOs(PrintStream os) {
		this.os = os;
	}

	public ShellCommand resolveCommand(String commandName){
		if(aliases.containsKey(commandName)){
			commandName = this.aliasPointer(commandName);
		}
		return this.commands.get(commandName);
	}


	public String aliasPointer(String alias){
		while(aliases.containsKey(alias)){
			alias = aliases.get(alias);
		}
		return alias;
	}

	public Set<String> getCommands(){
		return new HashSet<String>(this.commands.keySet());
	}

	public Set<Map.Entry<String, String>> getAliases(){
		return new HashSet<Map.Entry<String, String>>(this.aliases.entrySet());
	}

	public Environment getEnv() {
		return env;
	}

	public void setEnv(Environment env) {
		this.env = env;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public Visualizer getVisualizer() {
		return visualizer;
	}

	public void setVisualizer(Visualizer visualizer) {
		this.visualizer = visualizer;
	}

	public void start(){
		this.kill = false;

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				os.println(welcomeMessage);
				while(!kill){
					os.print("> ");
					String input = scanner.nextLine();
					executeCommand(input);
				}
			}
		});

		thread.start();


	}

	public void executeCommand(String input){
		int spaceIndex = input.indexOf(' ');
		String commandName = input;
		if(spaceIndex != -1){
			commandName = input.substring(0, spaceIndex);
		}
		ShellCommand command = resolveCommand(commandName);
		if(command != null){
			String argString = "";
			if(spaceIndex != -1 && input.length() > spaceIndex + 1){
				argString = input.substring(spaceIndex+1).trim();
			}
			try{
				int statusCode = command.call(EnvironmentShell.this, argString, env, scanner, os);
				if(statusCode == -1){
					os.println(command.commandName() + " could not parse input arguments");
				}
			}catch(Exception e){
				os.println("Exception in command execution:\n"+e.getMessage());
			}

		}
		else{
			os.println("Unknown command: " + commandName);
		}
	}


	protected Collection<ShellCommand> generateReserved(){
		return Arrays.asList(new AliasCommand(), new QuitCommand(), new CommandsCommand(), new AliasesCommand(), new HelpCommand());
	}

	protected Collection<ShellCommand> generateStandard(){
		EpisodeRecordingCommands erc = new EpisodeRecordingCommands();
		return Arrays.asList(new ExecuteActionCommand(domain), new ObservationCommand(), new ResetEnvCommand(),
				new AddStateObjectCommand(domain), new RemoveStateObjectCommand(), new SetAttributeCommand(),
				new AddRelationCommand(), new RemoveRelationCommand(), new RewardCommand(), new IsTerminalCommand(),
				erc.getRecCommand(), erc.getBrowser());
	}

	public static void main(String[] args) {
		GridWorldDomain gwd = new GridWorldDomain(11, 11);
		gwd.setMapToFourRooms();
		GridWorldRewardFunction rf = new GridWorldRewardFunction(11, 11, 0);
		rf.setReward(10, 10, 1.);
		TerminalFunction tf = new GridWorldTerminalFunction(10, 10);

		Domain domain = gwd.generateDomain();
		State s = GridWorldDomain.getOneAgentNoLocationState(domain, 0, 0);


		Environment env = new SimulatedEnvironment(domain, rf, tf, s);

		EnvironmentShell shell = new EnvironmentShell(domain, env, System.in, System.out);
		Visualizer v = GridWorldVisualizer.getVisualizer(gwd.getMap());
		shell.setVisualizer(v);
		shell.start();
	}

}
