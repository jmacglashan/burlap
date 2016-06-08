package burlap.shell.visual;

import burlap.mdp.core.Action;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.action.ActionType;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.environment.extensions.StateSettableEnvironment;
import burlap.visualizer.Visualizer;
import burlap.shell.BurlapShell;
import burlap.shell.EnvironmentShell;
import burlap.shell.ShellObserver;
import burlap.shell.command.ShellCommand;
import burlap.shell.command.env.ObservationCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * This class allows you act as the agent by choosing actions in an {@link burlap.mdp.singleagent.environment.Environment}.
 * States are
 * conveyed to the user through a 2D visualization and the user specifies actions
 * by either pressing keys that are mapped to actions or by typing the actions into the action command field. 
 * Action parameters in the action field are specified by space delineated input. For instance: "stack block0 block1" will cause
 * the stack action to called with action parameters block0 and block1. The ` key
 * causes the environment to reset.
 * <p>
 * Additionally, the VisualExplorer also creates an associated instance of an {@link burlap.shell.EnvironmentShell}
 * that you can access using the "Show Shell" button. You can use the shell to modify the state (assuming the input
 * {@link burlap.mdp.singleagent.environment.Environment} implements {@link StateSettableEnvironment},
 * record trajectories that you make in the environment, and any number of other tasks. The shell's "programs," specified with
 * {@link burlap.shell.command.ShellCommand} instances, may also be expanded so that you can create your own runtime tools.
 * See the {@link burlap.shell.BurlapShell} and {@link burlap.shell.EnvironmentShell} Java doc for more information on how
 * to use them. If you need access to the shell instance, you can get it with the {@link #getShell()} method. You can
 * also set shell commands to be executed when a key is pressed in the visualization. Set them with the
 * {@link #addKeyShellCommand(String, String)} method.
 * <p>
 * A special shell command, "livePoll" is automatically added to the shell, that allows the user to set the visualizer
 * to auto poll the environment at a fixed interval for the state and display it. This is useful when the environment
 * can evolve independent of agent action or explicit shell commands. See it's help in the shell (-h option) for more
 * information on it.
 * @author James MacGlashan
 *
 */
public class VisualExplorer extends JFrame implements ShellObserver{

	private static final long serialVersionUID = 1L;
	

	protected Environment									env;
	protected SADomain										domain;
	protected Map <String, Action>							keyActionMap;
	protected Map <String, String>							keyShellMap = new HashMap<String, String>();

	
	protected Visualizer 									painter;
	protected TextArea										propViewer;
	protected TextField										actionField;
	protected JButton										actionButton;
	protected int											cWidth;
	protected int											cHeight;


	protected JFrame										consoleFrame;
	protected JTextArea										stateConsole;



	protected boolean										runLivePolling = false;
	protected long											pollInterval;

	protected EnvironmentShell								shell;
	protected TextAreaStreams								tstreams;

	
	/**
	 * Initializes with a domain and initial state, automatically creating a {@link burlap.mdp.singleagent.environment.SimulatedEnvironment}
	 * as the environment with which to interact. The created {@link burlap.mdp.singleagent.environment.SimulatedEnvironment} will
	 * have a {@link burlap.mdp.singleagent.common.NullRewardFunction} and {@link burlap.mdp.auxiliary.common.NullTermination} functions set.
	 * @param domain the domain to explore
	 * @param painter the 2D state visualizer
	 * @param baseState the initial state from which to explore
	 */
	public VisualExplorer(SADomain domain, Visualizer painter, State baseState){
		Environment env = new SimulatedEnvironment(domain, baseState);
		this.init(domain, env, painter, 800, 800);
	}


	/**
	 * Initializes with a visualization canvas size set to 800x800.
	 * @param domain the domain to explore
	 * @param env the {@link burlap.mdp.singleagent.environment.Environment} with which to interact.
	 * @param painter the 2D state visualizer
	 */
	public VisualExplorer(SADomain domain, Environment env, Visualizer painter){
		this.init(domain, env, painter, 800, 800);
	}

	
	/**
	 * Initializes.
	 * @param domain the domain to explore
	 * @param env the {@link burlap.mdp.singleagent.environment.Environment} with which to interact.
	 * @param painter the 2D state visualizer
	 * @param w the width of the visualizer canvas
	 * @param h the height of the visualizer canvas
	 */
	public VisualExplorer(SADomain domain, Environment env, Visualizer painter, int w, int h){
		this.init(domain, env, painter, w, h);
	}
	
	protected void init(SADomain domain, Environment env, Visualizer painter, int w, int h){
		
		this.domain = domain;
		this.env = env;
		this.painter = painter;
		this.keyActionMap = new HashMap <String, Action>();
		
		this.keyShellMap.put("`", "reset");
		
		this.cWidth = w;
		this.cHeight = h;
		
		this.propViewer = new TextArea();
		this.propViewer.setEditable(false);

		
	}

	/**
	 * Returns the {@link burlap.visualizer.Visualizer} used by this explorer.
	 * @return the {@link burlap.visualizer.Visualizer} used by this explorer.
	 */
	public Visualizer getVisualizer(){
		return this.painter;
	}


	/**
	 * Returns the {@link burlap.shell.EnvironmentShell} instance associated with this object.
	 * @return the {@link burlap.shell.EnvironmentShell} instance associated with this object.
	 */
	public EnvironmentShell getShell() {
		return shell;
	}



	/**
	 * Specifies which action to execute for a given key press
	 * @param key the key that is pressed by the user
	 * @param action the {@link burlap.mdp.core.Action} to take when the key is pressed
	 */
	public void addKeyAction(String key, Action action){
		keyActionMap.put(key, action);
	}


	/**
	 * Adds a key action mapping.
	 * @param key the key that is pressed by the user
	 * @param actionTypeName the name of the {@link ActionType}
	 * @param paramStringRep the string representation of the action parameters
	 */
	public void addKeyAction(String key, String actionTypeName, String paramStringRep){
		keyActionMap.put(key, this.domain.getAction(actionTypeName).associatedAction(paramStringRep));
	}

	/**
	 * Cause a shell command to be executed when key is pressed with the visualizer highlighted.
	 * @param key the key to activate the shell command.
	 * @param shellCommand the shell command to execute.
	 */
	public void addKeyShellCommand(String key, String shellCommand){
		this.keyShellMap.put(key, shellCommand);
	}




	/**
	 * Starts a thread that polls this explorer's {@link burlap.mdp.singleagent.environment.Environment} every
	 * msPollDelay milliseconds for its current state and updates the visualizer to that state.
	 * Polling can be stopped with the {@link #stopLivePolling()}. If live polling is already running,
	 * this method call will only change the poll rate.
	 * @param msPollDelay the number of milliseconds between environment polls and state updates.
	 */
	public void startLiveStatePolling(final long msPollDelay){
		this.pollInterval = msPollDelay;
		if(this.runLivePolling){
			return;
		}
		this.runLivePolling = true;
		Thread pollingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(runLivePolling) {
					State s = env.currentObservation();
					if(s != null) {
						updateState(s);
					}
					try {
						Thread.sleep(pollInterval);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		pollingThread.start();
	}


	/**
	 * Stops this class from live polling this explorer's {@link burlap.mdp.singleagent.environment.Environment}.
	 */
	public void stopLivePolling(){
		this.runLivePolling = false;
	}


	@Override
	public void observeCommand(BurlapShell shell, ShellCommandEvent event) {
		if(event.returnCode == 1){
			this.updateState(this.env.currentObservation());
		}
		if(event.command instanceof ObservationCommand){
			this.updateState(this.env.currentObservation());
		}
	}

	/**
	 * Initializes the visual explorer GUI and presents it to the user.
	 */
	public void initGUI(){
		
		painter.setPreferredSize(new Dimension(cWidth, cHeight));
		propViewer.setPreferredSize(new Dimension(cWidth, 100));
		
		Container bottomContainer = new Container();
		bottomContainer.setLayout(new BorderLayout());
		bottomContainer.add(propViewer, BorderLayout.NORTH);
		
		getContentPane().add(bottomContainer, BorderLayout.SOUTH);
		getContentPane().add(painter, BorderLayout.CENTER);
	
		
		addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {	
			}
			public void keyTyped(KeyEvent e) {
				handleKeyPressed(e);
			}

		});
		
		//also add key listener to the painter in case the focus is changed
		painter.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {	
			}
			public void keyTyped(KeyEvent e) {
				handleKeyPressed(e);
			}

		});
		
		propViewer.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {	
			}
			public void keyTyped(KeyEvent e) {
				handleKeyPressed(e);
			}

		});
		
		actionField = new TextField(20);
		bottomContainer.add(actionField, BorderLayout.CENTER);
		
		actionButton = new JButton("Execute");
		actionButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				handleExecute();
				
			}
		});
		bottomContainer.add(actionButton, BorderLayout.EAST);
		
		painter.updateState(this.env.currentObservation());
		this.updatePropTextArea(this.env.currentObservation());

		JButton showConsoleButton = new JButton("Show Shell");
		showConsoleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				VisualExplorer.this.consoleFrame.setVisible(true);
			}
		});
		bottomContainer.add(showConsoleButton, BorderLayout.SOUTH);


		this.consoleFrame = new JFrame();
		this.consoleFrame.setPreferredSize(new Dimension(600, 500));



		this.stateConsole = new JTextArea(40, 40);
		this.stateConsole.setLineWrap(true);
		DefaultCaret caret = (DefaultCaret)this.stateConsole.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		this.stateConsole.setEditable(false);
		this.stateConsole.setMargin(new Insets(10, 5, 10, 5));


		JScrollPane shellScroll = new JScrollPane(this.stateConsole, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		this.consoleFrame.getContentPane().add(shellScroll, BorderLayout.CENTER);

		this.tstreams = new TextAreaStreams(this.stateConsole);
		this.shell = new EnvironmentShell(domain, env, tstreams.getTin(), new PrintStream(tstreams.getTout()));
		this.shell.addObservers(this);
		this.shell.setVisualizer(this.painter);
		this.shell.addCommand(new LivePollCommand());
		this.shell.start();


		final JTextField consoleCommand = new JTextField(40);
		consoleCommand.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String command = ((JTextField)e.getSource()).getText();
				consoleCommand.setText("");
				tstreams.receiveInput(command + "\n");
			}
		});

		this.consoleFrame.getContentPane().add(consoleCommand, BorderLayout.SOUTH);



		pack();
		setVisible(true);

		this.consoleFrame.pack();
		this.consoleFrame.setVisible(false);
	}


	/**
	 * Updates the currently visualized state to the input state.
	 * @param s the state to visualize.
	 */
	synchronized public void updateState(State s){
		this.painter.updateState(s);
		this.updatePropTextArea(s);

	}




	/**
	 * Handles action execute button.
	 */
	protected void handleExecute(){
		
		String actionCommand = this.actionField.getText();
		
		if(actionCommand.length() == 0){
			return ;
		}

		this.shell.executeCommand("ex " + actionCommand);

	}


	/**
	 * Handles key presses
	 * @param e the key event
	 */
	protected void handleKeyPressed(KeyEvent e){
		
		String key = String.valueOf(e.getKeyChar());

		//otherwise this could be an action, see if there is an action mapping
		Action mappedAction = keyActionMap.get(key);
		if(mappedAction != null){

			this.executeAction(mappedAction);
			
		}
		else{

			String shellCommand = this.keyShellMap.get(key);
			if(shellCommand != null && this.shell != null){
			    this.shell.executeCommand(shellCommand);
			}

		}
		
	}




	/**
	 * Executes the provided {@link burlap.mdp.core.Action} in the explorer's environment and records
	 * the result if episodes are being recorded.
	 * @param ga the {@link burlap.mdp.core.Action} to execute.
	 */
	protected void executeAction(Action ga){
		EnvironmentOutcome eo = env.executeAction(ga);
		this.updateState(eo.op);
	}


	/**
	 * Updates the propositional function evaluation text display for the given state.
	 * @param s the input state on which propositional functions are to be evaluated.
	 */
	protected void updatePropTextArea(State s){

		if(!(domain instanceof OODomain) || !(s instanceof OOState)){
			return ;
		}

	    StringBuilder buf = new StringBuilder();
		
		List <PropositionalFunction> props = ((OODomain)domain).propFunctions();
		for(PropositionalFunction pf : props){
			List<GroundedProp> gps = pf.getAllGroundedPropsForState(s);
			for(GroundedProp gp : gps){
				if(gp.isTrue((OOState)s)){
					buf.append(gp.toString()).append("\n");
				}
			}
		}
		propViewer.setText(buf.toString());
		
		
	}


	/**
	 * Shell command that allow live polling of the {@link VisualExplorer} to be polled.
	 */
	public class LivePollCommand implements ShellCommand{

		protected OptionParser parser = new OptionParser("t:fch*");

		@Override
		public String commandName() {
			return "livePoll";
		}

		@Override
		public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

			Environment env = ((EnvironmentShell)shell).getEnv();
			OptionSet oset = this.parser.parse(argString.split(" "));

			if(oset.has("h")){
				os.println("[-t interval] [-f] [-c]\n\n" +
						"Used to set the associated visual explorer to poll the environment for the current state and update the display on a fixed interval.\n" +
						"-t interval: turns on live polling and causes the environment to be polled every interval milliseconds.\n" +
						"-f: turns off live polling.\n" +
						"-c: returns the status of live polling (enabled/disabled and rate");

			}

			if(oset.has("t")){
				String val = (String)oset.valueOf("t");
				long interval = Long.valueOf(val);
				startLiveStatePolling(interval);
			}
			else if(oset.has("f")){
				stopLivePolling();
			}

			if(oset.has("c")){
				if(runLivePolling){
					os.println("Live polling is enabled and polls every " + pollInterval + " milliseconds.");
				}
				else{
					os.println("Live polling is disabled.");
				}
			}

			return 0;
		}
	}



}
