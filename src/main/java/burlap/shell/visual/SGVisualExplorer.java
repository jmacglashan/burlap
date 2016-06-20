package burlap.shell.visual;

import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.common.NullJointRewardFunction;
import burlap.mdp.stochasticgames.world.World;
import burlap.mdp.stochasticgames.world.WorldObserver;
import burlap.shell.BurlapShell;
import burlap.shell.SGWorldShell;
import burlap.shell.ShellObserver;
import burlap.shell.command.world.JointActionCommand;
import burlap.visualizer.Visualizer;

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


/**
 * This class allows you act as all of the agents in a stochastic game (controlled by a {@link World} object)
 * by choosing actions for each of them to take in specific states. A game with registered agents in the world can also
 * be played out (with some or all of the agents being manually controlled). States are
 * conveyed to the user through a 2D visualization and the user specifies actions for each agent
 * by pressing keys that are mapped to actions or by typing the actions into the action command field. After each
 * action is specified, the corresponding joint action is taken by pressing a special finalizing key that by default is set to "c".
 * The ` key
 * causes the state to reset to the initial state provided to the explorer or the {@link World}'s
 * {@link burlap.mdp.auxiliary.StateGenerator}. This explorer also associates itself with a {@link burlap.shell.SGWorldShell} so that additional commands can be given.
 * Keys can also be mapped to execute specific shell commands. You can access the shell with the
 * <p>
 * @author James MacGlashan
 *
 */
public class SGVisualExplorer extends JFrame implements ShellObserver, WorldObserver{

	private static final long serialVersionUID = 1L;
	
	
	protected SGDomain								domain;
	protected World									w;
	protected Map <String, Action>		keyActionMap;
	protected Map<String, Integer>						keyAgentMap;
	protected Map <String, String>						keyShellMap;


	protected Visualizer 										painter;
	protected TextArea											propViewer;
	protected int												cWidth;
	protected int												cHeight;




	protected JFrame								consoleFrame;
	protected JTextArea								stateConsole;


	protected SGWorldShell 		shell;
	protected TextAreaStreams tstreams;



	/**
	 * Initializes the data members for the visual explorer.
	 * @param domain the stochastic game domain to be explored
	 * @param painter the 2D visualizer for states
	 * @param baseState the initial state from which to explore
	 */
	public SGVisualExplorer(SGDomain domain, Visualizer painter, State baseState){

		this(domain, painter, baseState, 800, 800);
	}

	/**
	 * Initializes the data members for the visual explorer.
	 * @param domain the stochastic game domain to be explored
	 * @param painter the 2D visualizer for states
	 * @param baseState the initial state from which to explore
	 * @param w the width of the state visualizer
	 * @param h the height of the state visualizer
	 */
	public SGVisualExplorer(SGDomain domain, Visualizer painter, State baseState, int w, int h){
		this.init(domain, new World(domain, new NullJointRewardFunction(), new NullTermination(), baseState), painter, w, h);
	}

	/**
	 * Initializes the data members for the visual explorer.
	 * @param domain the stochastic game domain to be explored
	 * @param world the world with which to interact
	 * @param painter the 2D visualizer for states
	 * @param w the width of the state visualizer
	 * @param h the height of the state visualizer
	 */
	public SGVisualExplorer(SGDomain domain, World world, Visualizer painter, int w, int h){
		this.init(domain, world, painter, w, h);
	}


	/**
	 * Initializes.
	 * @param domain the stochastic game domain
	 * @param world the {@link World} with which to interact
	 * @param painter the state {@link burlap.visualizer.Visualizer}
	 * @param w the width of the state visualizer
	 * @param h the height of the state visualizer
	 */
	protected void init(SGDomain domain, World world, Visualizer painter, int w, int h){
		
		this.domain = domain;
		this.w = world;
		this.painter = painter;
		this.keyActionMap = new HashMap <String, Action>();
		this.keyAgentMap = new HashMap<String, Integer>();
		this.keyShellMap = new HashMap <String, String>();

		this.keyShellMap.put("`", "gs");
		
		this.cWidth = w;
		this.cHeight = h;
		
		this.propViewer = new TextArea();
		this.propViewer.setEditable(false);

		this.keyShellMap.put("c", "ja -x");

		this.w.addWorldObserver(this);

		
	}




	
	/**
	 * Specifies the action to set for a given key press. Actions should be formatted to include
	 * the agent name as follows: "agentName::actionName" This means
	 * that different key presses will have to specified for different agents.
	 * @param key the key that will cause the action to be set
	 * @param actingAgent the id of the acting agent
	 * @param action the action to set when the specified key is pressed.
	 */
	public void addKeyAction(String key, int actingAgent, Action action){
		keyActionMap.put(key, action);
		keyAgentMap.put(key, actingAgent);
	}

	/**
	 * Adds a key action mapping.
	 * @param key the key that is pressed by the user
	 * @param actingAgent the acting agent for this command
	 * @param actionTypeName the name of the {@link ActionType}
	 * @param paramStringRep the string representation of the action parameters
	 */
	public void addKeyAction(String key, int actingAgent, String actionTypeName, String paramStringRep){
		keyActionMap.put(key, this.domain.getActionType(actionTypeName).associatedAction(paramStringRep));
		keyAgentMap.put(key, actingAgent);
	}


	/**
	 * Returns the {@link burlap.shell.SGWorldShell} associated with this visual explorer.
	 * @return the {@link burlap.shell.SGWorldShell} associated with this visual explorer.
	 */
	public SGWorldShell getShell() {
		return shell;
	}

	/**
	 * Returns the {@link World} associated with this explorer.
	 * @return the {@link World} associated with this explorer.
	 */
	public World getW() {
		return w;
	}

	/**
	 * Sets the {@link World} associated with this visual explorer and shell.
	 * @param w the {@link World} associated with this visual explorer and shell.
	 */
	public void setW(World w) {
		this.w = w;
		this.shell.setWorld(w);
	}

	/**
	 * Causes a shell command to be executed when a key is pressed with the visualizer in focus.
	 * @param key the key
	 * @param shellCommand the shell command to execute.
	 */
	public void addKeyShellCommand(String key, String shellCommand){
		this.keyShellMap.put(key, shellCommand);
	}

	/**
	 * Initializes the GUI and presents it to the user.
	 */
	public void initGUI(){
		
		painter.setPreferredSize(new Dimension(cWidth, cHeight));
		propViewer.setPreferredSize(new Dimension(cWidth, 100));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
		





		JButton showConsoleButton = new JButton("Show Shell");
		showConsoleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SGVisualExplorer.this.consoleFrame.setVisible(true);
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
		this.shell = new SGWorldShell(this.domain, tstreams.getTin(), new PrintStream(tstreams.getTout()), this.w);
		this.shell.addObservers(this);
		this.shell.setVisualizer(this.painter);
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

		this.updateState(this.w.getCurrentWorldState());
		
	}


	@Override
	public void observeCommand(BurlapShell shell, ShellCommandEvent event) {
		if(event.returnCode == 1){
			this.updateState(this.w.getCurrentWorldState());
		}
	}

	@Override
	public void gameStarting(State s) {
		this.updateState(s);
	}

	@Override
	public void observe(State s, JointAction ja, double[] reward, State sp) {
		this.updateState(sp);
	}

	@Override
	public void gameEnding(State s) {
		//nothing
	}

	/**
	 * Updates the currently visualized state to the input state.
	 * @param s the state to visualize.
	 */
	public void updateState(State s){
		this.painter.updateState(s);
		this.updatePropTextArea(s);

	}


	
	private void handleKeyPressed(KeyEvent e){
		
		String key = String.valueOf(e.getKeyChar());
		

		//otherwise this could be an action, see if there is an action mapping
		Action toAdd = keyActionMap.get(key);
		Integer agent = keyAgentMap.get(key);
		if(toAdd != null) {
			((JointActionCommand)this.shell.resolveCommand("ja")).setAction(agent, toAdd);
		}

		else{

			String command = this.keyShellMap.get(key);
			if(command != null){
				this.shell.executeCommand(command);
			}

			
		}
		
		
	}


	protected void updatePropTextArea(State s){

		if(!(domain instanceof OODomain) || !(s instanceof OOState)){
			return;
		}

	    StringBuilder buf = new StringBuilder();
		
		List <PropositionalFunction> props = ((OODomain)domain).propFunctions();
		for(PropositionalFunction pf : props){
			List<GroundedProp> gps = pf.allGroundings((OOState)s);
			for(GroundedProp gp : gps){
				if(gp.isTrue((OOState)s)){
					buf.append(gp.toString()).append("\n");
				}
			}
		}
		

		propViewer.setText(buf.toString());
		
		
	}



}
