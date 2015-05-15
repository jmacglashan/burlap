package burlap.oomdp.singleagent.explorer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.oomdp.core.*;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.NullRewardFunction;
import burlap.oomdp.visualizer.Visualizer;

/**
 * This class allows you act as the agent by choosing actions to take in specific states. States are
 * conveyed to the user through a 2D visualization and the user specifies actions
 * by either pressing keys that are mapped to actions or by typing the actions into the action command field. 
 * Action parameters in the action field are specified by space delineated input. For instance: "stack block0 block1" will cause
 * the stack action to called with action parameters block0 and block1. The ` key
 * causes the state to reset to the initial state provided to the explorer or to a state that is sampled from a provided {@link StateGenerator} object. 
 * Other special kinds of actions
 * not described in the domain can be added and executed by pressing corresponding keys for them. The episodes of action taken by a user may also be recorded
 * to a list of recorded episodes and then subsequently polled by a client object. To enable episode recording, use the method
 * {@link #enableEpisodeRecording(String, String)} or {@link #enableEpisodeRecording(String, String, RewardFunction)}. To check if the user
 * is still recording episodes, use the method {@link #isRecording()}. To retrieve the recorded episodes, use the method {@link #getRecordedEpisodes()}.
 * <br/><br/>
 * This class can also be provided a reward function and terminal function through the
 * {@link #setTrackingRewardFunction(burlap.oomdp.singleagent.RewardFunction)} and
 * {@link #setTerminalFunction(burlap.oomdp.core.TerminalFunction)} methods. Once set, the console for the visualizer
 * will report the last reward received and whether the current state is a terminal state.
 * @author James MacGlashan
 *
 */
public class VisualExplorer extends JFrame{

	private static final long serialVersionUID = 1L;
	
	
	protected Domain										domain;
	protected Map <String, String>							keyActionMap;
	protected Map <String, SpecialExplorerAction>			keySpecialMap;
	protected State											baseState;
	protected State											curState;
	
	protected Visualizer 									painter;
	protected TextArea										propViewer;
	protected TextField										actionField;
	protected JButton										actionButton;
	protected int											cWidth;
	protected int											cHeight;
	
	protected int											numSteps;

	protected JFrame										consoleFrame;
	protected TextArea										stateConsole;
	
	//recording data members
	protected EpisodeAnalysis 								currentEpisode = null;
	protected List<EpisodeAnalysis>							recordedEpisodes = null;
	protected RewardFunction								trackingRewardFunction = new NullRewardFunction();
	protected TerminalFunction								terminalFunction;

	protected GroundedAction								lastAction;
	protected double										lastReward;
	
	protected boolean										isRecording = false;

	
	/**
	 * Initializes the visual explorer with the domain to explorer, the visualizer to use, and the base state from which to explore.
	 * @param domain the domain to explore
	 * @param painter the 2D state visualizer
	 * @param baseState the initial state from which to explore
	 */
	public VisualExplorer(Domain domain, Visualizer painter, State baseState){
		
		this.init(domain, painter, new ConstantStateGenerator(baseState), 800, 800);
	}
	
	
	/**
	 * Initializes the visual explorer with the domain to explorer, the visualizer to use, the base state from which to explore,
	 * and the dimensions of the visualizer.
	 * @param domain the domain to explore
	 * @param painter the 2D state visualizer
	 * @param baseState the initial state from which to explore
	 * @param w the width of the visualizer canvas
	 * @param h the height of the visualizer canvas
	 */
	public VisualExplorer(Domain domain, Visualizer painter, State baseState, int w, int h){
		this.init(domain, painter, new ConstantStateGenerator(baseState), w, h);
	}
	
	/**
	 * Initializes the visual explorer with the domain to explorer, the visualizer to use, and an initial state generator from which to explore,
	 * and the dimensions of the visualizer.
	 * @param domain the domain to explore
	 * @param painter the 2D state visualizer
	 * @param initialStateGenerator a generator for initial states that is polled everytime the special reset action is called
	 * @param w the width of the visualizer canvas
	 * @param h the height of the visualizer canvas
	 */
	public VisualExplorer(Domain domain, Visualizer painter, StateGenerator initialStateGenerator, int w, int h){
		this.init(domain, painter, initialStateGenerator, w, h);
	}
	
	protected void init(Domain domain, Visualizer painter, StateGenerator stateGeneratorForReset, int w, int h){
		
		this.domain = domain;
		this.baseState = stateGeneratorForReset.generateState();
		this.curState = baseState.copy();
		this.painter = painter;
		this.keyActionMap = new HashMap <String, String>();
		this.keySpecialMap = new HashMap <String, SpecialExplorerAction>();
		
		StateResetSpecialAction reset = new StateResetSpecialAction(stateGeneratorForReset);
		this.addSpecialAction("`", reset);
		
		this.cWidth = w;
		this.cHeight = h;
		
		this.propViewer = new TextArea();
		this.propViewer.setEditable(false);
		
		this.numSteps = 0;
		
	}

	public RewardFunction getTrackingRewardFunction() {
		return trackingRewardFunction;
	}

	public void setTrackingRewardFunction(RewardFunction trackingRewardFunction) {
		this.trackingRewardFunction = trackingRewardFunction;
	}

	public TerminalFunction getTerminalFunction() {
		return terminalFunction;
	}

	public void setTerminalFunction(TerminalFunction terminalFunction) {
		this.terminalFunction = terminalFunction;
	}

	/**
	 * Returns a special action that causes the state to reset to the initial state.
	 * @return a special action that causes the state to reset to the initial state.
	 */
	public StateResetSpecialAction getResetSpecialAction(){
		return (StateResetSpecialAction)keySpecialMap.get("`");
	}
	
	
	/**
	 * Specifies which action to execute for a given key press
	 * @param key the key that is pressed by the user
	 * @param action the action to take when the key is pressed
	 */
	public void addKeyAction(String key, String action){
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
	 * recorded episodes can be polled using the method {@link #getRecordedEpisodes()}. Rewards stored in the recorded episode will
	 * all be zero.
	 * @param recordLastEpisodeKey the key to press to indidcate that the last episode should be recorded/saved.
	 * @param finishedRecordingKey the key to press to indicate that no more episodes will be recorded so that the list of recorded episodes can be safely polled by a client object.
	 */
	public void enableEpisodeRecording(String recordLastEpisodeKey, String finishedRecordingKey){
		this.currentEpisode = new EpisodeAnalysis(this.baseState);
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
	 * recorded episodes can be polled using the method {@link #getRecordedEpisodes()}.
	 * @param recordLastEpisodeKey the key to press to indidcate that the last episode should be recorded/saved.
	 * @param finishedRecordingKey the key to press to indicate that no more episodes will be recorded so that the list of recorded episodes can be safely polled by a client object.
	 * @param rewardFunction the reward function to use to record the reward received for each action taken.
	 */
	public void enableEpisodeRecording(String recordLastEpisodeKey, String finishedRecordingKey, RewardFunction rewardFunction){
		this.currentEpisode = new EpisodeAnalysis(this.baseState);
		this.recordedEpisodes = new ArrayList<EpisodeAnalysis>();
		this.isRecording = true;
		this.trackingRewardFunction = rewardFunction;
		
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
	 * recorded episodes is saved to disk in the directory saveDirectory with states parsed using sp.
	 * The list of
	 * recorded episodes can be polled using the method {@link #getRecordedEpisodes()}.
	 * @param recordLastEpisodeKey the key to press to indidcate that the last episode should be recorded/saved.
	 * @param finishedRecordingKey the key to press to indicate that no more episodes will be recorded so that the list of recorded episodes can be safely polled by a client object.
	 * @param rewardFunction the reward function to use to record the reward received for each action taken.
	 * @param saveDirectory the directory in which all episodes will be saved
	 * @param sp the {@link burlap.oomdp.auxiliary.StateParser} to use for parsing states to strings.
	 */
	public void enableEpisodeRecording(String recordLastEpisodeKey, String finishedRecordingKey, RewardFunction rewardFunction,
									   String saveDirectory, StateParser sp){
		this.currentEpisode = new EpisodeAnalysis(this.baseState);
		this.recordedEpisodes = new ArrayList<EpisodeAnalysis>();
		this.isRecording = true;
		this.trackingRewardFunction = rewardFunction;

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

		this.keySpecialMap.put(finishedRecordingKey, new SaveEpisodeAction(saveDirectory, sp));

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
		
		painter.updateState(baseState);
		this.updatePropTextArea(baseState);

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

		JLabel consoleCommands = new JLabel("<html><h2>Console command syntax:</h2>" +
				"&nbsp;&nbsp;&nbsp;&nbsp;<b>add</b> objectClass object<br/>" +
				"&nbsp;&nbsp;&nbsp;&nbsp;<b>remove</b> object<br/>" +
				"&nbsp;&nbsp;&nbsp;&nbsp;<b>set</b> object attribute [attribute_2 ... attribute_n] value [value_2 ... value_n]<br/>" +
				"&nbsp;&nbsp;&nbsp;&nbsp;<b>addRelation</b> sourceObject relationalAttribute targetObject<br/>" +
				"&nbsp;&nbsp;&nbsp;&nbsp;<b>removeRelation</b> sourceObject relationalAttribute targetObject<br/>" +
				"&nbsp;&nbsp;&nbsp;&nbsp;<b>clearRelations</b> sourceObject relationalAttribute<br/>" +
				"&nbsp;&nbsp;&nbsp;&nbsp;<b>execute</b> action [param_1 ... param_n]<br/>&nbsp;</html>");

		consoleFrame.getContentPane().add(consoleCommands, BorderLayout.NORTH);

		this.stateConsole = new TextArea(this.getConsoleText(this.baseState), 40, 40, TextArea.SCROLLBARS_BOTH);
		this.consoleFrame.getContentPane().add(this.stateConsole, BorderLayout.CENTER);

		JTextField consoleCommand = new JTextField(40);
		consoleCommand.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String command = ((JTextField)e.getSource()).getText();


				String [] comps = command.split(" ");
				if(comps.length > 0){

					State ns = VisualExplorer.this.curState.copy();

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
							ObjectInstance o = new ObjectInstance(VisualExplorer.this.domain.getObjectClass(comps[1]), comps[2]);
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

					if(madeChange) {
						VisualExplorer.this.lastAction = null;
						VisualExplorer.this.updateState(ns);
						VisualExplorer.this.numSteps = 0;
						if (VisualExplorer.this.currentEpisode != null) {
							VisualExplorer.this.currentEpisode = new EpisodeAnalysis(curState);
						}
					}
				}


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
	public void updateState(State s){
		this.curState = s;
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
		if(this.terminalFunction != null){
			if(this.terminalFunction.isTerminal(s)){
				sb.append("State IS terminal\n");
			}
			else{
				sb.append("State is NOT terminal\n");
			}
		}
		if(this.trackingRewardFunction != null && this.lastAction != null){
			sb.append("Reward: " + this.lastReward + "\n");
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
		String mappedAction = keyActionMap.get(key);
		if(mappedAction != null){
			
			//then we have a action for this key
			//split the string up into components
			String [] comps = mappedAction.split(" ");
			this.executeAction(comps);
			
		}
		else{
			
			SpecialExplorerAction sea = keySpecialMap.get(key);
			if(sea != null){
				this.lastAction = null;
				this.updateState(sea.applySpecialAction(curState));
			}
			if(sea instanceof StateResetSpecialAction){
				System.out.println("Number of steps before reset: " + numSteps);
				numSteps = 0;
				if(this.currentEpisode != null){
					this.currentEpisode = new EpisodeAnalysis(curState);
				}
			}
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
			System.out.println("Unknown action: " + actionName);
		}
		else{
			GroundedAction ga = new GroundedAction(action, params);
			State nextState = ga.executeIn(curState);
			if(this.currentEpisode != null){
				this.currentEpisode.recordTransitionTo(ga, nextState, this.trackingRewardFunction.reward(curState, ga, nextState));
			}

			if(this.trackingRewardFunction != null){
				this.lastAction = ga;
				this.lastReward = this.trackingRewardFunction.reward(curState, ga, nextState);
			}

			numSteps++;
			this.updateState(nextState);
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
		 * The State parser used to save episodes
		 */
		protected StateParser sp;


		/**
		 * Initializes
		 * @param directory the directory path in which episodes will be recorded
		 * @param sp the state parser to use.
		 */
		public SaveEpisodeAction(String directory, StateParser sp){
			this.directory = directory;
			this.sp = sp;

			if(!this.directory.endsWith("/")){
				this.directory = this.directory + "/";
			}

		}

		@Override
		public State applySpecialAction(State curState) {

			synchronized(VisualExplorer.this) {
				VisualExplorer.this.isRecording = false;
				List<EpisodeAnalysis> episodes = VisualExplorer.this.getRecordedEpisodes();
				EpisodeAnalysis.writeEpisodesToDisk(episodes, this.directory, "episode", 0, this.sp);
				System.out.println("Recorded " + VisualExplorer.this.recordedEpisodes.size()
						+ " episodes to directory " + this.directory);
			}

			return curState;

		}
	}

	
	
}
