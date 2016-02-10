package burlap.oomdp.singleagent.explorer;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.NullRewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.stateserialization.SerializableStateFactory;
import burlap.oomdp.stateserialization.simple.SimpleSerializableStateFactory;
import burlap.oomdp.visualizer.Visualizer;
import burlap.shell.EnvironmentShell;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class allows you act as the agent by choosing actions in an {@link burlap.oomdp.singleagent.environment.Environment}.
 * States are
 * conveyed to the user through a 2D visualization and the user specifies actions
 * by either pressing keys that are mapped to actions or by typing the actions into the action command field. 
 * Action parameters in the action field are specified by space delineated input. For instance: "stack block0 block1" will cause
 * the stack action to called with action parameters block0 and block1. The ` key
 * causes the state to reset to the initial state provided to the explorer or to a state that is sampled from a provided {@link StateGenerator} object. 
 * Other special kinds of actions
 * not described in the domain can be added and executed by pressing corresponding keys for them. The episodes of action taken by a user may also be recorded
 * to a list of recorded episodes and then subsequently polled by a client object. To enable episode recording, use the method
 * {@link #enableEpisodeRecording(String, String)}. To check if the user
 * is still recording episodes, use the method {@link #isRecording()}. To retrieve the recorded episodes, use the method {@link #getRecordedEpisodes()}.
 * @author James MacGlashan
 *
 */
public class VisualExplorer extends JFrame{

	private static final long serialVersionUID = 1L;
	

	protected Environment									env;
	protected Domain										domain;
	protected Map <String, GroundedAction>					keyActionMap;
	protected Map <String, SpecialExplorerAction>			keySpecialMap;
	
	protected Visualizer 									painter;
	protected TextArea										propViewer;
	protected TextField										actionField;
	protected JButton										actionButton;
	protected int											cWidth;
	protected int											cHeight;
	
	protected int											numSteps;

	protected JFrame										consoleFrame;
	protected JTextArea										stateConsole;
	
	//recording data members
	protected EpisodeAnalysis 								currentEpisode = null;
	protected List<EpisodeAnalysis>							recordedEpisodes = null;

	protected double										lastReward;
	protected String										warningMessage = "";

	protected boolean										isRecording = false;

	protected boolean										runLivePolling = false;

	protected EnvironmentShell								shell;
	protected TextAreaStreams								tstreams;

	
	/**
	 * Initializes with a domain and initial state, automatically creating a {@link burlap.oomdp.singleagent.environment.SimulatedEnvironment}
	 * as the environment with which to interact. The created {@link burlap.oomdp.singleagent.environment.SimulatedEnvironment} will
	 * have a {@link burlap.oomdp.singleagent.common.NullRewardFunction} and {@link burlap.oomdp.auxiliary.common.NullTermination} functions set.
	 * @param domain the domain to explore
	 * @param painter the 2D state visualizer
	 * @param baseState the initial state from which to explore
	 */
	public VisualExplorer(Domain domain, Visualizer painter, State baseState){
		Environment env = new SimulatedEnvironment(domain, new NullRewardFunction(), new NullTermination(), baseState);
		this.init(domain, env, painter, 800, 800);
	}


	/**
	 * Initializes with a visualization canvas size set to 800x800.
	 * @param domain the domain to explore
	 * @param env the {@link burlap.oomdp.singleagent.environment.Environment} with which to interact.
	 * @param painter the 2D state visualizer
	 */
	public VisualExplorer(Domain domain, Environment env, Visualizer painter){
		this.init(domain, env, painter, 800, 800);
	}

	
	/**
	 * Initializes.
	 * @param domain the domain to explore
	 * @param env the {@link burlap.oomdp.singleagent.environment.Environment} with which to interact.
	 * @param painter the 2D state visualizer
	 * @param w the width of the visualizer canvas
	 * @param h the height of the visualizer canvas
	 */
	public VisualExplorer(Domain domain, Environment env, Visualizer painter, int w, int h){
		this.init(domain, env, painter, w, h);
	}
	
	protected void init(Domain domain, Environment env, Visualizer painter, int w, int h){
		
		this.domain = domain;
		this.env = env;
		this.painter = painter;
		this.keyActionMap = new HashMap <String, GroundedAction>();
		this.keySpecialMap = new HashMap <String, SpecialExplorerAction>();
		
		StateResetSpecialAction reset = new StateResetSpecialAction(this.env);
		this.addSpecialAction("`", reset);
		
		this.cWidth = w;
		this.cHeight = h;
		
		this.propViewer = new TextArea();
		this.propViewer.setEditable(false);
		
		this.numSteps = 0;
		
	}

	/**
	 * Returns the {@link burlap.oomdp.visualizer.Visualizer} used by this explorer.
	 * @return the {@link burlap.oomdp.visualizer.Visualizer} used by this explorer.
	 */
	public Visualizer getVisualizer(){
		return this.painter;
	}


	/**
	 * Returns a special action that causes the state to reset to the initial state.
	 * @return a special action that causes the state to reset to the initial state.
	 */
	public StateResetSpecialAction getResetSpecialAction(){
		return (StateResetSpecialAction)keySpecialMap.get("`");
	}



	/**
	 * Specifies a string representation of an action to execute when the specified key is pressed.
	 * The string representation should have the first word be the action name, with spaces separating
	 * the parameters of the string representation of each parameter value.
	 * @param key the key that is pressed by the user
	 * @param actionStringRep the {@link burlap.oomdp.singleagent.GroundedAction} to take when the key is pressed
	 */
	public void addKeyAction(String key, String actionStringRep){
		GroundedAction ga = this.getGroundedActionFromStringComps(actionStringRep.split(" "));
		if(ga == null){
			System.out.println("Could not parse GroundedAction string representation of " + actionStringRep + ".\n" +
					"It is not being assigned to VisualExplorer key " + key + ".");
		}
		else {
			this.keyActionMap.put(key, ga);
		}
	}


	/**
	 * Specifies which action to execute for a given key press
	 * @param key the key that is pressed by the user
	 * @param action the {@link burlap.oomdp.singleagent.GroundedAction} to take when the key is pressed
	 */
	public void addKeyAction(String key, GroundedAction action){
		keyActionMap.put(key, action);
	}
	
	
	/**
	 * Specifies which special non-domain action to take for a given key press
	 * @param key the key that is pressed by the user
	 * @param action the special non-domain action to take when the key is pressed
	 */
	public void addSpecialAction(String key, SpecialExplorerAction action){
		keySpecialMap.put(key, action);
	}
	
	
	/**
	 * Enables episodes recording of actions taken. Whenever the recordLastEpisodeKey is pressed, the episode
	 * starting from the initial state, or last state reset (activated with the ` key) up until the current state
	 * is stored in a list of recorded episodes. When the finishedRecordingKey is pressed, the {@link #isRecording()} flag
	 * is set to false to let any client objects know that the list of recorded episodes can be safely polled. The list of
	 * recorded episodes can be polled using the method {@link #getRecordedEpisodes()}.
	 * @param recordLastEpisodeKey the key to press to indicate that the last episode should be recorded/saved.
	 * @param finishedRecordingKey the key to press to indicate that no more episodes will be recorded so that the list of recorded episodes can be safely polled by a client object.
	 */
	public void enableEpisodeRecording(String recordLastEpisodeKey, String finishedRecordingKey){
		this.currentEpisode = new EpisodeAnalysis(this.env.getCurrentObservation());
		this.recordedEpisodes = new ArrayList<EpisodeAnalysis>();
		this.isRecording = true;
		
		this.keySpecialMap.put(recordLastEpisodeKey, new SpecialExplorerAction() {
			
			@Override
			public State applySpecialAction(State curState) {
				synchronized(VisualExplorer.this) {
					VisualExplorer.this.recordedEpisodes.add(VisualExplorer.this.currentEpisode);
					System.out.println("Recorded Episode: " + VisualExplorer.this.recordedEpisodes.size());
				}
				return curState;
			}
		});
		
		this.keySpecialMap.put(finishedRecordingKey, new SpecialExplorerAction() {
			
			@Override
			public State applySpecialAction(State curState) {
				synchronized(VisualExplorer.this) {
					VisualExplorer.this.isRecording = false;
				}
				return curState;
			}
		});
		
	}

	/**
	 * Enables episodes recording of actions taken. Whenever the recordLastEpisodeKey is pressed, the episode
	 * starting from the initial state, or last state reset (activated with the ` key) up until the current state
	 * is stored in a list of recorded episodes. When the finishedRecordingKey is pressed, the {@link #isRecording()} flag
	 * is set to false to let any client objects know that the list of recorded episodes can be safely polled. The list of
	 * recorded episodes is saved to disk in the directory saveDirectory. States will be serialized with {@link burlap.oomdp.stateserialization.simple.SimpleSerializableStateFactory}
	 * The list of
	 * recorded episodes can be polled using the method {@link #getRecordedEpisodes()}.
	 * @param recordLastEpisodeKey the key to press to indicate that the last episode should be recorded/saved.
	 * @param finishedRecordingKey the key to press to indicate that no more episodes will be recorded so that the list of recorded episodes can be safely polled by a client object.
	 * @param saveDirectory the directory in which all episodes will be saved
	 */
	public void enableEpisodeRecording(String recordLastEpisodeKey, String finishedRecordingKey,
									   String saveDirectory){
		this.currentEpisode = new EpisodeAnalysis(this.env.getCurrentObservation());
		this.recordedEpisodes = new ArrayList<EpisodeAnalysis>();
		this.isRecording = true;

		this.keySpecialMap.put(recordLastEpisodeKey, new SpecialExplorerAction() {

			@Override
			public State applySpecialAction(State curState) {
				synchronized(VisualExplorer.this) {
					VisualExplorer.this.recordedEpisodes.add(VisualExplorer.this.currentEpisode);
					System.out.println("Recorded Episode: " + VisualExplorer.this.recordedEpisodes.size());
				}
				return curState;
			}
		});

		this.keySpecialMap.put(finishedRecordingKey, new SaveEpisodeAction(saveDirectory, new SimpleSerializableStateFactory()));

	}


	/**
	 * Enables episodes recording of actions taken. Whenever the recordLastEpisodeKey is pressed, the episode
	 * starting from the initial state, or last state reset (activated with the ` key) up until the current state
	 * is stored in a list of recorded episodes. When the finishedRecordingKey is pressed, the {@link #isRecording()} flag
	 * is set to false to let any client objects know that the list of recorded episodes can be safely polled. The list of
	 * recorded episodes is saved to disk in the directory saveDirectory.
	 * The list of
	 * recorded episodes can be polled using the method {@link #getRecordedEpisodes()}.
	 * @param recordLastEpisodeKey the key to press to indicate that the last episode should be recorded/saved.
	 * @param finishedRecordingKey the key to press to indicate that no more episodes will be recorded so that the list of recorded episodes can be safely polled by a client object.
	 * @param saveDirectory the directory in which all episodes will be saved
	 * @param serializableStateFactory the {@link burlap.oomdp.stateserialization.SerializableStateFactory} to use for serializing states
	 */
	public void enableEpisodeRecording(String recordLastEpisodeKey, String finishedRecordingKey,
									   String saveDirectory, SerializableStateFactory serializableStateFactory){
		this.currentEpisode = new EpisodeAnalysis(this.env.getCurrentObservation());
		this.recordedEpisodes = new ArrayList<EpisodeAnalysis>();
		this.isRecording = true;

		this.keySpecialMap.put(recordLastEpisodeKey, new SpecialExplorerAction() {

			@Override
			public State applySpecialAction(State curState) {
				synchronized(VisualExplorer.this) {
					VisualExplorer.this.recordedEpisodes.add(VisualExplorer.this.currentEpisode);
					System.out.println("Recorded Episode: " + VisualExplorer.this.recordedEpisodes.size());
				}
				return curState;
			}
		});

		this.keySpecialMap.put(finishedRecordingKey, new SaveEpisodeAction(saveDirectory, serializableStateFactory));

	}
	
	/**
	 * Returns whether episodes are still be recorded by a user.
	 * @return true is the user is still recording episode; false otherwise.
	 */
	public boolean isRecording(){
		return this.isRecording;
	}
	
	
	/**
	 * Returns the list of episodes recorded by a user.
	 * @return the list of episodes recorded by a user.
	 */
	public List<EpisodeAnalysis> getRecordedEpisodes(){
		return this.recordedEpisodes;
	}


	/**
	 * Starts a thread that polls this explorer's {@link burlap.oomdp.singleagent.environment.Environment} every
	 * msPollDelay milliseconds for its current state and updates the visualizer to that state.
	 * Polling can be stopped with the {@link #stopLivePolling()}.
	 * @param msPollDelay the number of milliseconds between environment polls and state updates.
	 */
	public void startLiveStatePolling(final long msPollDelay){
		this.runLivePolling = true;
		Thread pollingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(runLivePolling) {
					State s = env.getCurrentObservation();
					if(s != null) {
						updateState(s);
					}
					try {
						Thread.sleep(msPollDelay);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		pollingThread.start();
	}


	/**
	 * Stops this class from live polling this explorer's {@link burlap.oomdp.singleagent.environment.Environment}.
	 */
	public void stopLivePolling(){
		this.runLivePolling = false;
	}

	
	/**
	 * Initializes the visual explorer GUI and presents it to the user.
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
		
		painter.updateState(this.env.getCurrentObservation());
		this.updatePropTextArea(this.env.getCurrentObservation());

		JButton showConsoleButton = new JButton("Show Console");
		showConsoleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				VisualExplorer.this.consoleFrame.setVisible(true);
			}
		});
		bottomContainer.add(showConsoleButton, BorderLayout.SOUTH);


		this.consoleFrame = new JFrame();
		this.consoleFrame.setPreferredSize(new Dimension(600, 500));


		/*
		JLabel consoleCommands = new JLabel("<html><h2>Console command syntax:</h2>" +
				"&nbsp;&nbsp;&nbsp;&nbsp;<b>add</b> objectClass object<br/>" +
				"&nbsp;&nbsp;&nbsp;&nbsp;<b>remove</b> object<br/>" +
				"&nbsp;&nbsp;&nbsp;&nbsp;<b>set</b> object attribute [attribute_2 ... attribute_n] value [value_2 ... value_n]<br/>" +
				"&nbsp;&nbsp;&nbsp;&nbsp;<b>addRelation</b> sourceObject relationalAttribute targetObject<br/>" +
				"&nbsp;&nbsp;&nbsp;&nbsp;<b>removeRelation</b> sourceObject relationalAttribute targetObject<br/>" +
				"&nbsp;&nbsp;&nbsp;&nbsp;<b>clearRelations</b> sourceObject relationalAttribute<br/>" +
				"&nbsp;&nbsp;&nbsp;&nbsp;<b>execute</b> action [param_1 ... param_n]<br/>" +
				"&nbsp;&nbsp;&nbsp;&nbsp;<b>pollState</b><br/>&nbsp;</html>");

		consoleFrame.getContentPane().add(consoleCommands, BorderLayout.NORTH);
		*/

		this.stateConsole = new JTextArea(40, 40);
		this.stateConsole.setLineWrap(true);
		DefaultCaret caret = (DefaultCaret)this.stateConsole.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		this.stateConsole.setEditable(false);

		this.consoleFrame.getContentPane().add(this.stateConsole, BorderLayout.CENTER);

		this.tstreams = new TextAreaStreams(this.stateConsole);
		this.shell = new EnvironmentShell(domain, env, tstreams.getTin(), new PrintStream(tstreams.getTout()));
		//this.shell.start();

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("reading");
				InputStreamReader reader = new InputStreamReader(tstreams.getTin());
				String read = null;
				int ir=-1;
				try {
					ir = reader.read();
					//ir = tstreams.getTin().read();
				} catch(Exception e) {
					e.printStackTrace();
				}
				System.out.println("READ: " + ir);
			}
		});
		t.start();

		JTextField consoleCommand = new JTextField(40);
		consoleCommand.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String command = ((JTextField)e.getSource()).getText();
				tstreams.receiveInput(command + "\n");
			}
		});
		/*
		consoleCommand.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String command = ((JTextField)e.getSource()).getText();


				String [] comps = command.split(" ");
				if(comps.length > 0){

					State ns = VisualExplorer.this.env.getCurrentObservation().copy();

					boolean madeChange = false;
					if(comps[0].equals("set")){
						if(comps.length >= 4) {
							ObjectInstance o = ns.getObject(comps[1]);
							if(o != null){
								int rsize = comps.length - 2;
								if(rsize % 2 == 0){
									int vind = rsize / 2;
									for(int i = 0; i < rsize / 2; i++){
										o.setValue(comps[2+i], comps[2+i+vind]);
									}
								}
								madeChange = true;
							}
						}

					}
					else if(comps[0].equals("addRelation")){
						if(comps.length == 4){
							ObjectInstance o = ns.getObject(comps[1]);
							if(o != null){
								o.addRelationalTarget(comps[2], comps[3]);
								madeChange = true;
							}
						}
					}
					else if(comps[0].equals("removeRelation")){
						if(comps.length == 4){
							ObjectInstance o = ns.getObject(comps[1]);
							if(o != null){
								o.removeRelationalTarget(comps[2], comps[3]);
								madeChange = true;
							}
						}
					}
					else if(comps[0].equals("clearRelations")){
						if(comps.length == 3){
							ObjectInstance o = ns.getObject(comps[1]);
							if(o != null){
								o.clearRelationalTargets(comps[2]);
								madeChange = true;
							}
						}
					}
					else if(comps[0].equals("add")){
						if(comps.length == 3){
							ObjectInstance o = new MutableObjectInstance(VisualExplorer.this.domain.getObjectClass(comps[1]), comps[2]);
							ns.addObject(o);
							madeChange = true;
						}
					}
					else if(comps[0].equals("remove")){
						if(comps.length == 2){
							ns.removeObject(comps[1]);
							madeChange = true;
						}
					}
					else if(comps[0].equals("execute")){
						String [] actionComps = new String[comps.length-1];
						for(int i = 1; i < comps.length; i++){
							actionComps[i-1] = comps[i];
						}
						VisualExplorer.this.executeAction(actionComps);
					}
					else if(comps[0].equals("pollState")){
						updateState(env.getCurrentObservation());
					}

					if(madeChange) {
						if(env instanceof StateSettableEnvironment) {
							((StateSettableEnvironment)env).setCurStateTo(ns);
							VisualExplorer.this.updateState(env.getCurrentObservation());
							VisualExplorer.this.numSteps = 0;
							if(VisualExplorer.this.currentEpisode != null) {
								VisualExplorer.this.currentEpisode = new EpisodeAnalysis(env.getCurrentObservation());
							}
						}
						else{
							warningMessage = "Cannot edit state because the Environment does not implement StateSettableEnvironment";
							VisualExplorer.this.updateState(env.getCurrentObservation());
						}
					}
				}


			}
		});
		*/

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
		this.stateConsole.setText(this.getConsoleText(s));
		this.painter.updateState(s);
		this.updatePropTextArea(s);

	}


	/**
	 * Returns the text that will be printed to the console for the given input state.
	 * @param s the state for which the current console text will be generated.
	 * @return the text that will be printed to the console for the given input state.
	 */
	protected String getConsoleText(State s){
		StringBuilder sb = new StringBuilder(256);
		sb.append(s.getCompleteStateDescriptionWithUnsetAttributesAsNull());
		if(this.env.isInTerminalState()){
			sb.append("State IS terminal\n");
		}
		else{
			sb.append("State is NOT terminal\n");
		}

		sb.append("Reward: " + this.lastReward + "\n");

		if(this.warningMessage.length() > 0) {
			sb.append("WARNING: " + this.warningMessage + "\n");
			this.warningMessage = "";
		}
		sb.append("\n------------------------------\n\n");

		if(s.getAllUnsetAttributes().size() == 0){
			sb.append("Applicable Actions:\n");
			List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(this.domain.getActions(), s);
			for(GroundedAction ga : gas){
				sb.append(ga.toString()).append("\n");
			}
		}
		else{
			sb.append("State has unset values; set them them to see applicable action list.");
		}


		return sb.toString();
	}


	/**
	 * Handles action execute button.
	 */
	protected void handleExecute(){
		
		String actionCommand = this.actionField.getText();
		
		if(actionCommand.length() == 0){
			return ;
		}
		
		String [] comps = actionCommand.split(" ");
		this.executeAction(comps);
	}


	/**
	 * Handles key presses
	 * @param e the key event
	 */
	protected void handleKeyPressed(KeyEvent e){
		
		String key = String.valueOf(e.getKeyChar());

		//otherwise this could be an action, see if there is an action mapping
		GroundedAction mappedAction = keyActionMap.get(key);
		if(mappedAction != null){

			this.executeAction(mappedAction);
			
		}
		else{
			
			SpecialExplorerAction sea = keySpecialMap.get(key);
			if(sea != null){
				sea.applySpecialAction(this.env.getCurrentObservation());
			}
			if(sea instanceof StateResetSpecialAction){
				System.out.println("Number of steps before reset: " + numSteps);
				numSteps = 0;
				this.lastReward = 0.;
				if(this.currentEpisode != null){
					this.currentEpisode = new EpisodeAnalysis(this.env.getCurrentObservation());
				}
			}
			this.updateState(this.env.getCurrentObservation());
		}
		
	}

	/**
	 * Executes the action defined in string array with the first component being the action name and the rest the parameters.
	 * @param comps the string array defining hte action to be executed.
	 */
	protected void executeAction(String [] comps){
		String actionName = comps[0];

		//construct parameter list as all that remains
		String params[];
		if(comps.length > 1){
			params = new String[comps.length-1];
			for(int i = 1; i < comps.length; i++){
				params[i-1] = comps[i];
			}
		}
		else{
			params = new String[0];
		}

		Action action = domain.getAction(actionName);
		if(action == null){
			this.warningMessage = "Unknown action: " + actionName + "; nothing changed";
			System.out.println(warningMessage);
			this.updateState(env.getCurrentObservation());
		}
		else{
			GroundedAction ga = action.getAssociatedGroundedAction();
			ga.initParamsWithStringRep(params);
			executeAction(ga);

		}
	}

	/**
	 * Gets the {@link burlap.oomdp.singleagent.GroundedAction} described by the
	 * String components where the first component is the action name and the rest
	 * are the string representations of the parameters.
	 * @param comps the string components that define the {@link burlap.oomdp.singleagent.GroundedAction}
	 * @return the associated {@link burlap.oomdp.singleagent.GroundedAction} or null if it cannot be constructed.
	 */
	protected GroundedAction getGroundedActionFromStringComps(String [] comps){
		String actionName = comps[0];

		//construct parameter list as all that remains
		String params[];
		if(comps.length > 1){
			params = new String[comps.length-1];
			for(int i = 1; i < comps.length; i++){
				params[i-1] = comps[i];
			}
		}
		else{
			params = new String[0];
		}

		Action action = domain.getAction(actionName);
		if(action == null){
			return null;
		}
		GroundedAction ga = action.getAssociatedGroundedAction();
		ga.initParamsWithStringRep(params);
		return ga;
	}


	/**
	 * Executes the provided {@link burlap.oomdp.singleagent.GroundedAction} in the explorer's environment and records
	 * the result if episodes are being recorded.
	 * @param ga the {@link burlap.oomdp.singleagent.GroundedAction} to execute.
	 */
	protected void executeAction(GroundedAction ga){
		if(ga.applicableInState(env.getCurrentObservation())){

			EnvironmentOutcome eo = ga.executeIn(env);
			if(this.currentEpisode != null){
				this.currentEpisode.recordTransitionTo(ga, eo.op, eo.r);
			}

			this.lastReward = eo.r;


			numSteps++;
			this.updateState(this.env.getCurrentObservation());
		}
		else{
			this.warningMessage = ga.toString() + " is not applicable in the current state; nothing changed";
			System.out.println(warningMessage);
			this.updateState(this.env.getCurrentObservation());
		}
	}


	/**
	 * Updates the propositional function evaluation text display for the given state.
	 * @param s the input state on which propositional functions are to be evaluated.
	 */
	protected void updatePropTextArea(State s){
		
		StringBuffer buf = new StringBuffer();
		
		List <PropositionalFunction> props = domain.getPropFunctions();
		for(PropositionalFunction pf : props){
			//List<GroundedProp> gps = s.getAllGroundedPropsFor(pf);
			List<GroundedProp> gps = pf.getAllGroundedPropsForState(s);
			for(GroundedProp gp : gps){
				boolean needsContinue = false;
				for(String oname : gp.params){
					if(s.getObject(oname).unsetAttributes().size() > 0){
						needsContinue = true;
						break;
					}
				}
				if(needsContinue){
					continue;
				}
				if(gp.isTrue(s)){
					buf.append(gp.toString()).append("\n");
				}
			}
		}
		propViewer.setText(buf.toString());
		
		
	}


	/**
	 * Class for receiving key presses from a {@link burlap.oomdp.singleagent.explorer.VisualExplorer}
	 * that handles it as ending episode recoding and saving all recorded episodes to disk.
	 */
	protected class SaveEpisodeAction implements SpecialExplorerAction{

		/**
		 * The directory in which the episodes will be recorded.
		 */
		protected String directory;

		/**
		 * The {@link burlap.oomdp.stateserialization.SerializableStateFactory} used to serialize states.
		 */
		protected SerializableStateFactory serializableStateFactory;


		/**
		 * Initializes
		 * @param directory the directory path in which episodes will be recorded
		 * @param serializableStateFactory the {@link burlap.oomdp.stateserialization.SerializableStateFactory} to use for serializing states.
		 */
		public SaveEpisodeAction(String directory, SerializableStateFactory serializableStateFactory){
			this.directory = directory;
			this.serializableStateFactory = serializableStateFactory;

			if(!this.directory.endsWith("/")){
				this.directory = this.directory + "/";
			}

		}

		@Override
		public State applySpecialAction(State curState) {

			synchronized(VisualExplorer.this) {
				VisualExplorer.this.isRecording = false;
				List<EpisodeAnalysis> episodes = VisualExplorer.this.getRecordedEpisodes();
				EpisodeAnalysis.writeEpisodesToDisk(episodes, this.directory, "episode", this.serializableStateFactory);
				System.out.println("Recorded " + VisualExplorer.this.recordedEpisodes.size()
						+ " episodes to directory " + this.directory);
			}

			return curState;

		}
	}


}
