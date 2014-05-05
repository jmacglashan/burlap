package burlap.oomdp.stochasticgames.common;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.TextArea;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import burlap.behavior.stochasticgame.GameAnalysis;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.WorldObserver;
import burlap.oomdp.visualizer.Visualizer;


/**
 * A {@link WorldObserver} that visualizes each transition with a fixed refresh delay between when the transition is rendered and when control
 * is returned to the client.
 * @author James MacGlashan
 *
 */
public class VisualWorldObserver extends JFrame implements WorldObserver {


	private static final long serialVersionUID = 1L;
	
	
	/**
	 * The domain this visualizer is rendering
	 */
	protected SGDomain			domain;
	
	/**
	 * The visualizer that will render states
	 */
	protected Visualizer		painter;
	
	/**
	 * Text area to display the propositional functions that are true
	 */
	TextArea					propViewer;
	
	/**
	 * The width of the painter
	 */
	protected int				cWidth;
	
	/**
	 * The height of the painter
	 */
	protected int				cHeight;
	
	
	/**
	 * How long to wait in ms for a state to be rendered before returning control to the world. Default is 17ms (about 60fps)
	 */
	protected long				actionRenderDelay = 17;
	
	
	
	/**
	 * Iniitalizes for the given domain and visualizer.
	 * @param domain the stochastic games domain to be visualized
	 * @param v the visualizer to use
	 */
	public VisualWorldObserver(SGDomain domain, Visualizer v){
		this(domain, v, 800, 800);
	}
	
	
	/**
	 * Initializes
	 * @param domain the stochastic games domain to be visualized
	 * @param v the visualize to use
	 * @param cWidth the canvas width of the visuzlier
	 * @param cHeight the canvas height of the visualizer
	 */
	public VisualWorldObserver(SGDomain domain, Visualizer v, int cWidth, int cHeight){
		this.domain = domain;
		this.painter = v;
		this.cWidth = cWidth;
		this.cHeight = cHeight;
		
		this.propViewer = new TextArea();
		this.propViewer.setEditable(false);
	}
	
	/**
	 * Sets how long to wait in ms for a state to be rendered before returning control the world. The default value is 17, which is about 60FPS.
	 * @param delay how long to wait in ms for a state to be rendered before returning control the world.
	 */
	public void setFrameDelay(long delay){
		this.actionRenderDelay = delay;
	}
	
	
	/**
	 * Initializes the visual world observer GUI and presents it to the user.
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
		
		pack();
		setVisible(true);
	}
	

	@Override
	public void observe(State s, JointAction ja, Map<String, Double> reward, State sp) {
		
		this.painter.updateState(sp);
		this.updatePropTextArea(sp);
		Thread waitThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(actionRenderDelay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		
		waitThread.start();
		
		try {
			waitThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	
	/**
	 * Causes the visualizer to be replayed for the given {@link GameAnalysis} object. The initial state
	 * of the provided game is first rendered for the given refresh delay of this object, and then each
	 * joint action is played.
	 * @param ga the game analysis object to be replayed.
	 */
	public void replayGame(GameAnalysis ga){
		
		this.painter.updateState(ga.getState(0));
		this.updatePropTextArea(ga.getState(0));
		Thread waitThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(actionRenderDelay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		
		waitThread.start();
		
		try {
			waitThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for(int i = 0; i < ga.maxTimeStep(); i++){
			this.observe(ga.getState(i), ga.getJointAction(i), ga.getJointReward(i+1), ga.getState(i+1));
		}
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
