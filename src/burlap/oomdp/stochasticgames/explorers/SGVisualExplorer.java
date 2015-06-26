package burlap.oomdp.stochasticgames.explorers;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import burlap.oomdp.core.*;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.singleagent.explorer.SpecialExplorerAction;
import burlap.oomdp.singleagent.explorer.StateResetSpecialAction;
import burlap.oomdp.stochasticgames.*;
import burlap.oomdp.visualizer.Visualizer;


/**
 * This class allows you act as all of the agents in a stochastic game by choosing actions for each of them to take in specific states. States are
 * conveyed to the user through a 2D visualization and the user specifies actions for each agent
 * by pressing keys that are mapped to actions or by typing the actions into the action command field. After each
 * action is specified, the corresponding joint action is taken by pressing a special finalizing key that by default to set to "c".
 * The ` key
 * causes the state to reset to the initial state provided to the explorer. Other special kinds of actions
 * not described in the domain can be added and executed by pressing corresponding keys for them.
 * <br/><br/>
 * This explorer can also track a reward function and terminal function and print them to the console, which can be set
 * with the
 * {@link #setRewardFunction(burlap.oomdp.stochasticgames.JointReward)} and
 * {@link #setTerminalFunction(burlap.oomdp.core.TerminalFunction)} methods.
 * @author James MacGlashan
 *
 */
public class SGVisualExplorer extends JFrame {

	private static final long serialVersionUID = 1L;
	
	
	private SGDomain								domain;
	private JointActionModel						actionModel;
	private Map <String, String>					keyActionMap;
	private Map <String, SpecialExplorerAction>		keySpecialMap;
	State											baseState;
	State											curState;
	
	Visualizer 										painter;
	TextArea										propViewer;
	int												cWidth;
	int												cHeight;
	
	int												numSteps;
	
	String											jointActionComplete = "c";
	JointAction										nextAction;


	protected JFrame								consoleFrame;
	protected TextArea								stateConsole;

	protected TerminalFunction						terminalFunction;
	protected JointReward 							rewardFunction;
	protected String								warningMessage = "";

	protected Map<String, Double>					lastRewards;

	/**
	 * This constructor is deprecated, because {@link burlap.oomdp.stochasticgames.SGDomain} objects are now expected
	 * to have a {@link burlap.oomdp.stochasticgames.JointActionModel} associated with them, making the constructor parameter for it
	 * unnecessary. Instead use the constructor {@link #SGVisualExplorer(burlap.oomdp.stochasticgames.SGDomain, burlap.oomdp.visualizer.Visualizer, burlap.oomdp.core.State)}
	 * @param domain the stochastic game domain to be explored
	 * @param painter the 2D visualizer for states
	 * @param baseState the initial state from which to explore
	 * @param jam the joint action model that defines transition probabilities
	 */
	@Deprecated
	public SGVisualExplorer(SGDomain domain, Visualizer painter, State baseState, JointActionModel jam){
		
		this.init(domain, painter, baseState, jam, 800, 800);
	}

	/**
	 * Initializes the data members for the visual explorer.
	 * @param domain the stochastic game domain to be explored
	 * @param painter the 2D visualizer for states
	 * @param baseState the initial state from which to explore
	 */
	public SGVisualExplorer(SGDomain domain, Visualizer painter, State baseState){

		this.init(domain, painter, baseState, domain.getJointActionModel(), 800, 800);
	}
	
	
	/**
	 * This constructor is deprecated, because {@link burlap.oomdp.stochasticgames.SGDomain} objects are now expected
	 * to have a {@link burlap.oomdp.stochasticgames.JointActionModel} associated with them, making the constructor parameter for it
	 * unnecessary. Instead use the constructor {@link #SGVisualExplorer(burlap.oomdp.stochasticgames.SGDomain, burlap.oomdp.visualizer.Visualizer, burlap.oomdp.core.State, int, int)}
	 * @param domain the stochastic game domain to be explored
	 * @param painter the 2D visualizer for states
	 * @param baseState the initial state from which to explore
	 * @param jam the joint action model that defines transition probabilities
	 * @param w the width of the state visualizer
	 * @param h the height of the state visualizer
	 */
	@Deprecated
	public SGVisualExplorer(SGDomain domain, Visualizer painter, State baseState, JointActionModel jam, int w, int h){
		this.init(domain, painter, baseState, jam, w, h);
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
		this.init(domain, painter, baseState, domain.getJointActionModel(), w, h);
	}
	
	protected void init(SGDomain domain, Visualizer painter, State baseState, JointActionModel jam, int w, int h){
		
		this.domain = domain;
		this.baseState = baseState;
		this.curState = baseState.copy();
		this.painter = painter;
		this.keyActionMap = new HashMap <String, String>();
		this.keySpecialMap = new HashMap <String, SpecialExplorerAction>();
		
		StateResetSpecialAction reset = new StateResetSpecialAction(this.baseState);
		this.addSpecialAction("`", reset);
		
		this.cWidth = w;
		this.cHeight = h;
		
		this.propViewer = new TextArea();
		this.propViewer.setEditable(false);
		
		this.actionModel = jam;
		
		numSteps = 0;
		
		nextAction = new JointAction();
		
	}

	public JointReward getRewardFunction() {
		return rewardFunction;
	}

	public void setRewardFunction(JointReward rewardFunction) {
		this.rewardFunction = rewardFunction;
	}

	public TerminalFunction getTerminalFunction() {
		return terminalFunction;
	}

	public void setTerminalFunction(TerminalFunction terminalFunction) {
		this.terminalFunction = terminalFunction;
	}

	/**
	 * Sets the joint action model to use
	 * @param jac the joint action model to use
	 */
	public void setJAC(String jac){
		this.jointActionComplete = jac;
	}
	
	/**
	 * Returns the reset action being used when the reset key ` is pressed
	 * @return the reset action being used when the reset key ` is pressed
	 */
	public StateResetSpecialAction getResetSpecialAction(){
		return (StateResetSpecialAction)keySpecialMap.get("`");
	}
	
	/**
	 * Specifies the action to set for a given key press. Actions should be formatted to include
	 * the agent name as follows: "agentName::actionName" This means
	 * that different key presses will have to specified for different agents.
	 * @param key the key that will cause the action to be set
	 * @param action the action to set when the specified key is pressed.
	 */
	public void addKeyAction(String key, String action){
		keyActionMap.put(key, action);
	}
	
	
	/**
	 * Adds a special non-domain action to modify the state when a key is pressed
	 * @param key the key that will cause the special non-domain action to be executed
	 * @param action the special non-domain action to exectute
	 */
	public void addSpecialAction(String key, SpecialExplorerAction action){
		keySpecialMap.put(key, action);
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
		





		JButton showConsoleButton = new JButton("Show Console");
		showConsoleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SGVisualExplorer.this.consoleFrame.setVisible(true);
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
				"&nbsp;&nbsp;&nbsp;&nbsp;<b>setAction</b> agentName:actionName [param_1 ... param_n]<br/>" +
				"&nbsp;&nbsp;&nbsp;&nbsp;<b>commit</b><br/></html>");

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

					State ns = SGVisualExplorer.this.curState.copy();

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
							ObjectInstance o = new MutableObjectInstance(SGVisualExplorer.this.domain.getObjectClass(comps[1]), comps[2]);
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
					else if(comps[0].equals("setAction")){
						String [] agentAction = comps[1].split(":");
						SingleAction sa = domain.getSingleAction(agentAction[1]);
						if(sa == null){
							warningMessage = "Unknown action: " + agentAction[1] + "; nothing changed";
							SGVisualExplorer.this.stateConsole.setText(SGVisualExplorer.this.getConsoleText(ns));
						}
						else {

							String[] params = new String[comps.length - 2];
							for(int i = 2; i < comps.length; i++) {
								params[i - 2] = comps[i];
							}
							GroundedSingleAction gsa = new GroundedSingleAction(agentAction[0], sa, params);
							if(sa.isApplicableInState(curState, agentAction[0], params)){
								SGVisualExplorer.this.nextAction.addAction(gsa);
								SGVisualExplorer.this.stateConsole.setText(SGVisualExplorer.this.getConsoleText(ns));
							}
							else{
								warningMessage = gsa.toString() + " is not applicable in the current state; nothing changed";
								SGVisualExplorer.this.stateConsole.setText(SGVisualExplorer.this.getConsoleText(ns));
							}



						}

					}
					else if(comps[0].equals("commit")){
						SGVisualExplorer.this.executeAction();
					}

					/*
					else if(comps[0].equals("execute")){
						String [] actionComps = new String[comps.length-1];
						for(int i = 1; i < comps.length; i++){
							actionComps[i-1] = comps[i];
						}
						SGVisualExplorer.this.executeAction(actionComps);
					}
					*/

					if(madeChange) {
						SGVisualExplorer.this.lastRewards = null;
						SGVisualExplorer.this.updateState(ns);
						SGVisualExplorer.this.numSteps = 0;
					}
				}


			}
		});

		this.consoleFrame.getContentPane().add(consoleCommand, BorderLayout.SOUTH);


		this.updateState(this.baseState);


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
		sb.append("\n------------------------------\n\n");

		if(this.terminalFunction != null){
			if(this.terminalFunction.isTerminal(s)){
				sb.append("State IS terminal\n");
			}
			else{
				sb.append("State is NOT terminal\n");
			}
		}

		if(this.lastRewards != null){
			for(String aname : lastRewards.keySet()){
				sb.append("" + aname + ": " + lastRewards.get(aname) + "\n");
			}
		}

		if(this.warningMessage.length() > 0){
			sb.append(warningMessage + "\n");
			warningMessage = "";
		}
		sb.append(this.nextAction.toString() + "\n");


		//sb.append("\n------------------------------\n\n");

		if(s.getAllUnsetAttributes().size() == 0){

			/*
			sb.append("Applicable Actions:\n");
			List<GroundedAction> gas = burlap.oomdp.singleagent.Action.getAllApplicableGroundedActionsFromActionList(this.domain.getActions(), s);
			for(GroundedAction ga : gas){
				sb.append(ga.toString()).append("\n");
			}
			*/
		}
		else{
			sb.append("State has unset values; set them them to see applicable action list.");
		}


		return sb.toString();
	}
	
	private void handleKeyPressed(KeyEvent e){
		
		String key = String.valueOf(e.getKeyChar());
		

		//otherwise this could be an action, see if there is an action mapping
		String mappedAction = keyActionMap.get(key);
		if(mappedAction != null){

			GroundedSingleAction toAdd = this.parseIntoSingleActions(mappedAction);
			if(toAdd != null) {
				nextAction.addAction(toAdd);
				System.out.println(nextAction.toString());
			}
			this.stateConsole.setText(this.getConsoleText(this.curState));

			
		}
		else{
			
			SpecialExplorerAction sea = keySpecialMap.get(key);
			if(sea != null){
				this.lastRewards = null;
				curState = sea.applySpecialAction(curState);
				if(sea instanceof StateResetSpecialAction){
					System.out.println("Number of steps before reset: " + numSteps);
					numSteps = 0;
				}
				this.updateState(curState);
			}
			else if(key.equals(jointActionComplete)){
				this.executeAction();
			}
			
		}
				
			


		//now paint the screen with the new state

		//System.out.println(curState_.getStateDescription());
		//System.out.println("-------------------------------------------");
		
		
	}
	
	


	protected void executeAction(){
		State nextState = actionModel.performJointAction(curState, nextAction);
		if(this.rewardFunction != null){
			this.lastRewards = this.rewardFunction.reward(curState, nextAction, nextState);
		}
		numSteps++;
		nextAction = new JointAction();
		curState = nextState;
		this.updateState(curState);
	}


	/**
	 * Parses a string into a {@link burlap.oomdp.stochasticgames.GroundedSingleAction}. Expects format:
	 * "agentName:actionName param1 parm2 ... paramn" If there is no SingleAction by that name or
	 * the action and parameters are not applicable in the current state, null is returned.
	 * @param str string rep of a grounding action in the form  "agentName:actionName param1 parm2 ... paramn"
	 * @return a {@link burlap.oomdp.stochasticgames.GroundedSingleAction}
	 */
	protected GroundedSingleAction parseIntoSingleActions(String str){
		
		String [] agentActionComps = str.split(":");
		String aname = agentActionComps[0];
		
		String [] actionAndParams = agentActionComps[1].split(" ");
		String singleActionName = actionAndParams[0];
		
		String [] params = new String[actionAndParams.length-1];
		for(int i = 1; i < actionAndParams.length; i++){
			params[i-1] = actionAndParams[i];
		}
		
		SingleAction sa = domain.getSingleAction(singleActionName);
		if(sa == null){
			warningMessage = "Unknown action: " + singleActionName + "; nothing changed";
			return null;
		}
		GroundedSingleAction gsa = new GroundedSingleAction(aname, sa, params);
		if(!sa.isApplicableInState(curState, aname, params)){
			warningMessage = gsa.toString() + " is not applicable in the current state; nothing changed";
			return null;
		}
		
		return gsa;
	}



	
	protected void updatePropTextArea(State s){
		
		StringBuffer buf = new StringBuffer();
		
		List <PropositionalFunction> props = domain.getPropFunctions();
		for(PropositionalFunction pf : props){
			//List<GroundedProp> gps = s.getAllGroundedPropsFor(pf);
			List<GroundedProp> gps = pf.getAllGroundedPropsForState(s);
			for(GroundedProp gp : gps){
				if(gp.isTrue(s)){
					buf.append(gp.toString()).append("\n");
				}
			}
		}
		

		propViewer.setText(buf.toString());
		
		
	}

}
