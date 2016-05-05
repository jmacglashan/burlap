package burlap.shell;

import burlap.mdp.core.Domain;
import burlap.mdp.visualizer.Visualizer;
import burlap.shell.command.ShellCommand;
import burlap.shell.command.reserved.*;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * A class for a runtime shell. Takes as input a {@link java.io.InputStream} and {@link java.io.OutputStream}
 * for the shell. Shell commands are implemented by objects implementing the {@link burlap.shell.command.ShellCommand}
 * interface, and allows a shells set of commands to be customized and trivially extended by adding new commands
 * with the {@link #addCommand(burlap.shell.command.ShellCommand)} method.
 * <p>
 * The shell keeps a small set of special reserved commands that new commands cannot replace. These include the help,
 * cmds (lists all known shell commands), quit (stops the shell), alias (allows a custom name to be assigned to a command),
 * and aliases (lists all known aliases).
 * <p>
 * The shell is started with the {@link #start()} method, which runs the shell in a separate thread. Beyond the
 * {@link java.io.InputStream} that is scanned for user input, shell commands may also be executed using the
 * {@link #executeCommand(String)} method. Other objects can be alerted to command execution completion by implementing
 * the {@link burlap.shell.ShellObserver} and adding an observer with the {@link #addObservers(ShellObserver...)}
 * method.
 * @see burlap.shell.EnvironmentShell EnvironmentShell
 * @author James MacGlashan.
 */
public class BurlapShell {

	protected InputStream is;
	protected PrintStream os;
	protected Scanner scanner;
	protected Domain domain;
	protected Visualizer visualizer;

	protected Map<String, ShellCommand> commands = new HashMap<String, ShellCommand>();
	protected Map<String, String> aliases = new HashMap<String, String>();
	protected Set<String> reserved;

	protected List<ShellObserver> observers = new ArrayList<ShellObserver>();

	protected volatile boolean kill = false;

	protected String welcomeMessage = "Welcome to the BURLAP shell. Type the command 'help' to bring " +
			"up additional information about using this shell.";

	protected String helpText = "Use the command help to bring up this message again. " +
			"Here is a list of standard reserved commands:\n" +
			"cmds - list all known commands.\n" +
			"aliases - list all known command aliases.\n" +
			"alias - set an alias for a command.\n" +
			"quit - terminate this shell.";


	public BurlapShell(Domain domain, InputStream is, PrintStream os) {
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

	public void addObservers(ShellObserver...observers){
		for(ShellObserver observer : observers){
			this.observers.add(observer);
		}
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
				int statusCode = command.call(BurlapShell.this, argString, scanner, os);
				if(statusCode == -1){
					os.println(command.commandName() + " could not parse input arguments");
				}
				for(ShellObserver observer : observers){
					observer.observeCommand(this, new ShellObserver.ShellCommandEvent(input, command, statusCode));
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
		return new ArrayList<ShellCommand>();
	}


}
