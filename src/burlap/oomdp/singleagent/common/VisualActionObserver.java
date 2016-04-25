package burlap.oomdp.singleagent.common;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.ActionObserver;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentObserver;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import burlap.oomdp.visualizer.Visualizer;

import javax.swing.*;
import java.awt.*;
import java.util.List;



/**
 * This class enables the live rendering of action calls or environment interactions, by implementing the
 * {@link burlap.oomdp.singleagent.ActionObserver} and {@link burlap.oomdp.singleagent.environment.EnvironmentObserver} interfaces.
 * It updates the visualizer to show the resulting state of an action call. After rendering, the client thread is blocked
 * for a specified interval of time to allow the state to be observed (by default the value is set to 17ms, which is about 60FPS).
 * This class will also render the new state of an {@link burlap.oomdp.singleagent.environment.Environment} after
 * a {@link burlap.oomdp.singleagent.environment.Environment#resetEnvironment()} message is observed and block for the
 * same interval of time.
 * This class is especially useful for watching learning algorithms or Monte Carlo-like planning algorithms in action.
 * <p>
 * Optionally, this class may also render state-action events in an {@link burlap.oomdp.singleagent.environment.Environment}
 * (so that the action is also rendered) so long as
 * the input {@link burlap.oomdp.visualizer.Visualizer} has a set {@link burlap.oomdp.visualizer.StateActionRenderLayer}.
 * To enable this support, pass the {@link #setRepaintOnActionInitiation(boolean)} true. If you would then
 * like to disable rendering the post-state from the {@link #observeEnvironmentInteraction(burlap.oomdp.singleagent.environment.EnvironmentOutcome)}
 * method, pass the {@link #setRepaintStateOnEnvironmentInteraction(boolean)} false.
 * @author James MacGlashan
 *
 */
public class VisualActionObserver extends JFrame implements ActionObserver, EnvironmentObserver {


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
	 * If true (which is default), then the post-state in an {@link #observeEnvironmentInteraction(burlap.oomdp.singleagent.environment.EnvironmentOutcome)}
	 * is rendered.
	 */
	protected boolean			repaintStateOnEnvironmentInteraction = true;


	/**
	 * If true, then a a state-action pair is rendered on calls to {@link #observeEnvironmentActionInitiation(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
	 * so long as the input {@link burlap.oomdp.visualizer.Visualizer} has a set {@link burlap.oomdp.visualizer.StateActionRenderLayer}. Default value is false.
	 */
	protected boolean			repaintOnActionInitiation = false;
	
	
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


	public Visualizer getPainter() {
		return painter;
	}

	public void setPainter(Visualizer painter) {
		this.painter = painter;
	}

	/**
	 * Sets whether the state should be updated on environment interactions events (the {@link #observeEnvironmentInteraction(burlap.oomdp.singleagent.environment.EnvironmentOutcome)}
	 * or only with state-actions in the {@link #observeEnvironmentActionInitiation(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}.
	 * @param repaintStateOnEnvironmentInteraction if true, then update states with environment interactions; if false then only with environment action initiation.
	 */
	public void setRepaintStateOnEnvironmentInteraction(boolean repaintStateOnEnvironmentInteraction) {
		this.repaintStateOnEnvironmentInteraction = repaintStateOnEnvironmentInteraction;
	}


	/**
	 * Sets whether the state-action should be updated when an action is initiated in an {@link burlap.oomdp.singleagent.environment.Environment} via the
	 * {@link #observeEnvironmentActionInitiation(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)} method.
	 * @param repaintOnActionInitiation if true, then state-action's are painted on action initiation; if false, they are not.
	 */
	public void setRepaintOnActionInitiation(boolean repaintOnActionInitiation) {
		this.repaintOnActionInitiation = repaintOnActionInitiation;
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

	@Override
	public void observeEnvironmentActionInitiation(State o, GroundedAction action) {
		if(this.repaintOnActionInitiation) {
			this.painter.updateStateAction(o, action);
			this.updatePropTextArea(o);
		}
	}

	@Override
	public void observeEnvironmentInteraction(EnvironmentOutcome eo) {
		if(this.repaintStateOnEnvironmentInteraction) {
			this.painter.updateState(eo.op);
			this.updatePropTextArea(eo.op);
		}
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

	@Override
	public void observeEnvironmentReset(Environment resetEnvironment) {


		this.painter.updateState(resetEnvironment.getCurrentObservation());
		this.updatePropTextArea(resetEnvironment.getCurrentObservation());

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
	 * Causes the visualizer to replay through the provided {@link EpisodeAnalysis} object. The initial state
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
		
	    StringBuilder buf = new StringBuilder();
		
		List <PropositionalFunction> props = domain.getPropFunctions();
		for(PropositionalFunction pf : props){
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
