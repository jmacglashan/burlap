package burlap.mdp.stochasticgames.common;

import burlap.behavior.stochasticgames.GameEpisode;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.action.JointAction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.world.WorldObserver;
import burlap.visualizer.Visualizer;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;


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
	public void gameStarting(State s) {
		this.updateAndWait(s);
	}


	@Override
	public void observe(State s, JointAction ja, Map<String, Double> reward, State sp) {
		
		this.updateAndWait(sp);

	}

	@Override
	public void gameEnding(State s) {
		//do nothing
	}

	protected void updateAndWait(State s){
		this.painter.updateState(s);
		this.updatePropTextArea(s);
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
	 * Causes the visualizer to be replayed for the given {@link GameEpisode} object. The initial state
	 * of the provided game is first rendered for the given refresh delay of this object, and then each
	 * joint action is played.
	 * @param ga the game analysis object to be replayed.
	 */
	public void replayGame(GameEpisode ga){
		
		this.painter.updateState(ga.state(0));
		this.updatePropTextArea(ga.state(0));
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
			this.observe(ga.state(i), ga.jointAction(i), ga.jointReward(i+1), ga.state(i+1));
		}
	}
	
	
	private void updatePropTextArea(State s){

		if(!(domain instanceof OODomain) || !(s instanceof OOState)){
			return ;
		}

	    StringBuilder buf = new StringBuilder();
		
		List <PropositionalFunction> props = ((OODomain)domain).propFunctions();
		for(PropositionalFunction pf : props){
			//List<GroundedProp> gps = s.getAllGroundedPropsFor(pf);
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
