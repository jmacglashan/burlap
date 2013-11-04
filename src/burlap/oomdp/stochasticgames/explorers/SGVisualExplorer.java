package burlap.oomdp.stochasticgames.explorers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.explorer.SpecialExplorerAction;
import burlap.oomdp.singleagent.explorer.StateResetSpecialAction;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SingleAction;
import burlap.oomdp.visualizer.Visualizer;


/**
 * This class allows you act as all of the agents in a stochastic game by choosing actions for each of them to take in specific states. States are
 * conveyed to the user through a 2D visualization and the user specifies actions for each agent
 * by pressing keys that are mapped to actions or by typing the actions into the action command field. After each
 * action is specified, the corresponding joint action is taken by pressing a special finalizing key that by default to set to "c".
 * The ` key
 * causes the state to reset to the initial state provided to the explorer. Other special kinds of actions
 * not described in the domain can be added and executed by pressing corresponding keys for them.
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
	
	
	/**
	 * Initializes the data members for the visual explorer.
	 * @param domain the stochastic game domain to be explored
	 * @param painter the 2D visualizer for states
	 * @param baseState the initial state from which to explore
	 * @param jam the joint action model that defines transition probabilities
	 */
	public SGVisualExplorer(SGDomain domain, Visualizer painter, State baseState, JointActionModel jam){
		
		this.init(domain, painter, baseState, jam, 800, 800);
	}
	
	
	/**
	 * Initializes the data members for the visual explorer.
	 * @param domain the stochastic game domain to be explored
	 * @param painter the 2D visualizer for states
	 * @param baseState the initial state from which to explore
	 * @param jam the joint action model that defines transition probabilities
	 * @param w the width of the state visualizer
	 * @param h the height of the state visualizer
	 */
	public SGVisualExplorer(SGDomain domain, Visualizer painter, State baseState, JointActionModel jam, int w, int h){
		this.init(domain, painter, baseState, jam, w, h);
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
		
		getContentPane().add(propViewer, BorderLayout.SOUTH);
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
		
		painter.updateState(baseState);
		
		pack();
		setVisible(true);
		
		
		
		
	}
	
	
	private void handleKeyPressed(KeyEvent e){
		
		String key = String.valueOf(e.getKeyChar());
		

		//otherwise this could be an action, see if there is an action mapping
		String mappedAction = keyActionMap.get(key);
		if(mappedAction != null){
			
			nextAction.addAction(this.parseIntoSingleActions(mappedAction));
			System.out.println(nextAction.toString());
			
		}
		else{
			
			SpecialExplorerAction sea = keySpecialMap.get(key);
			if(sea != null){
				curState = sea.applySpecialAction(curState);
				if(sea instanceof StateResetSpecialAction){
					System.out.println("Number of steps before reset: " + numSteps);
					numSteps = 0;
				}
			}
			else if(key.equals(jointActionComplete)){
				curState = actionModel.performJointAction(curState, nextAction);
				numSteps++;
				nextAction = new JointAction();
			}
			
		}
				
			

		
		//now paint the screen with the new state
		painter.updateState(curState);
		this.updatePropTextArea(curState);
		//System.out.println(curState_.getStateDescription());
		//System.out.println("-------------------------------------------");
		
		
	}
	
	
	//single actions separated by semicolons
	private JointAction parseIntoJointAciton(String str){
		
		String [] sacomps = str.split(";");
		
		JointAction ja = new JointAction(sacomps.length);
		for(int i = 0; i < sacomps.length; i++){
			ja.addAction(this.parseIntoSingleActions(sacomps[i]));
		}
		
		return ja;
	}
	
	//assumed format: "agentName:actionName param1 parm2 ... paramn"
	private GroundedSingleAction parseIntoSingleActions(String str){
		
		String [] agentActionComps = str.split(":");
		String aname = agentActionComps[0];
		
		String [] actionAndParams = agentActionComps[1].split(" ");
		String singleActionName = actionAndParams[0];
		
		String [] params = new String[actionAndParams.length-1];
		for(int i = 1; i < actionAndParams.length; i++){
			params[i-1] = actionAndParams[i];
		}
		
		SingleAction sa = domain.getSingleAction(singleActionName);
		GroundedSingleAction gsa = new GroundedSingleAction(aname, sa, params);
		
		return gsa;
	}
	
	private void updatePropTextArea(State s){
		
		StringBuffer buf = new StringBuffer();
		
		List <PropositionalFunction> props = domain.getPropFunctions();
		for(PropositionalFunction pf : props){
			List<GroundedProp> gps = s.getAllGroundedPropsFor(pf);
			for(GroundedProp gp : gps){
				if(gp.isTrue(s)){
					buf.append(gp.toString()).append("\n");
				}
			}
		}
		

		propViewer.setText(buf.toString());
		
		
	}

}
