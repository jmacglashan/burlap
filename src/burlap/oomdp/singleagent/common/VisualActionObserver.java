package burlap.oomdp.singleagent.common;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.TextArea;
import java.util.List;

import javax.swing.JFrame;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.ActionObserver;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.visualizer.Visualizer;



/**
 * This class enables the live rendering of action calls, updating the visualizer to show the resulting state of an action call. Actions
 * are stalled for a specified interval of time to allow the state to be observed (by default the value is set to 17ms, which is about 60FPS).
 * This class is especially useful for watching learning algorithms or Monte-Carlo-like planning algorithms in action.
 * @author James MacGlashan
 *
 */
public class VisualActionObserver extends JFrame implements ActionObserver {


	private static final long serialVersionUID = 1L;
	
	/**
	 * The domain this visualizer is rendering
	 */
	protected Domain			domain;
	
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
	 * How long to wait in ms for a state to be rendered before returning control to the agent. Default is 17ms (about 60fps)
	 */
	protected long				actionRenderDelay = 17;
	
	
	/**
	 * Initializes with a visualizer size of 800x800
	 * @param domain the domain that is being visualized
	 * @param painter the painter for rendering states
	 */
	public VisualActionObserver(Domain domain, Visualizer painter){
		this(domain, painter, 800, 800);
	}
	
	
	
	/**
	 * Initializes with a visualizer
	 * @param domain the domain that is being visualized
	 * @param painter the painter for rendering states
	 * @param cWidth the width of the state visualization area
	 * @param cHeight the height of the state visualization area
	 */
	public VisualActionObserver(Domain domain, Visualizer painter, int cWidth, int cHeight){
		this.domain = domain;
		this.painter = painter;
		this.cWidth = cWidth;
		this.cHeight = cHeight;
		
		this.propViewer = new TextArea();
		this.propViewer.setEditable(false);
	}
	
	
	/**
	 * Sets how long to wait in ms for a state to be rendered before returning control the agent. The default value is 17, which is about 60FPS.
	 * @param delay how long to wait in ms for a state to be rendered before returning control the agent.
	 */
	public void setFrameDelay(long delay){
		this.actionRenderDelay = delay;
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
		
		pack();
		setVisible(true);
	}
	
	
	@Override
	public void actionEvent(State s, GroundedAction ga, State sp) {
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
	 * Casues the visualizer to replay through the provided {@link EpisodeAnalysis} object. The initial state
	 * of the provided episode is first rendered for the given refresh delay of this object, and then each
	 * action and resulting state in the episode is feed through the {@link #actionEvent(State, GroundedAction, State)}
	 * method of this object.
	 * @param ea the episode to be replayed.
	 */
	public void replayEpisode(EpisodeAnalysis ea){
		this.painter.updateState(ea.getState(0));
		this.updatePropTextArea(ea.getState(0));
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
		
		for(int i = 0; i < ea.maxTimeStep(); i++){
			this.actionEvent(ea.getState(i), ea.getAction(i), ea.getState(i+1));
		}
	}
	
	
	private void updatePropTextArea(State s){
		
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
