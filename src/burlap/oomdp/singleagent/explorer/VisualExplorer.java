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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.visualizer.Visualizer;

/**
 * This class allows you act as the agent by choosing actions to take in specific states. States are
 * conveyed to the user through a 2D visualization and the user specifies actions
 * by either pressing keys that are mapped to actions or by typing the actions into the action command field. 
 * Action parameters in the action field are specified by space delineated input. For instance: "stack block0 block1" will cause
 * the stack action to called with action parameters block0 and block1. The ` key
 * causes the state to reset to the initial state provided to the explorer. Other special kinds of actions
 * not described in the domain can be added and executed by pressing corresponding keys for them.
 * @author James MacGlashan
 *
 */
public class VisualExplorer extends JFrame{

	private static final long serialVersionUID = 1L;
	
	
	private Domain									domain;
	private Map <String, String>					keyActionMap;
	private Map <String, SpecialExplorerAction>		keySpecialMap;
	State											baseState;
	State											curState;
	
	Visualizer 										painter;
	TextArea										propViewer;
	TextField										actionField;
	JButton											actionButton;
	int												cWidth;
	int												cHeight;
	
	int												numSteps;
	
	
	
	/**
	 * Initializes the visual explorer with the domain to explorer, the visualizer to use, and the base state from which to explore.
	 * @param domain the domain to explore
	 * @param painter the 2D state visualizer
	 * @param baseState the initial state from which to explore
	 */
	public VisualExplorer(Domain domain, Visualizer painter, State baseState){
		
		this.init(domain, painter, baseState, 800, 800);
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
		this.init(domain, painter, baseState, w, h);
	}
	
	protected void init(Domain domain, Visualizer painter, State baseState, int w, int h){
		
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
		
		this.numSteps = 0;
		
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
		
		pack();
		setVisible(true);
	}
	
	
	protected void handleExecute(){
		
		String actionCommand = this.actionField.getText();
		
		if(actionCommand.length() == 0){
			return ;
		}
		
		String [] comps = actionCommand.split(" ");
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
			curState = action.performAction(curState, params);
			numSteps++;
			
			painter.updateState(curState);
			this.updatePropTextArea(curState);
		}
	}
	
	protected void handleKeyPressed(KeyEvent e){
		
		String key = String.valueOf(e.getKeyChar());

		//otherwise this could be an action, see if there is an action mapping
		String mappedAction = keyActionMap.get(key);
		if(mappedAction != null){
			
			//then we have a action for this key
			//split the string up into components
			String [] comps = mappedAction.split(" ");
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
				curState = action.performAction(curState, params);
				numSteps++;
			}
			
		}
		else{
			
			SpecialExplorerAction sea = keySpecialMap.get(key);
			if(sea != null){
				curState = sea.applySpecialAction(curState);
			}
			if(sea instanceof StateResetSpecialAction){
				System.out.println("Number of steps before reset: " + numSteps);
				numSteps = 0;
			}
		}
				
		
		//now paint the screen with the new state
		painter.updateState(curState);
		this.updatePropTextArea(curState);
		//System.out.println(curState_.getStateDescription());
		//System.out.println("-------------------------------------------");
		
		
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
