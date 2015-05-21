package burlap.behavior.stochasticgame;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.TextArea;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import burlap.datastructures.AlphanumericSorting;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.visualizer.Visualizer;



/**
 * This class is used to visualize a set of games that have been
 * saved to files in a common directory or which are
 * provided to the object as a list of {@link burlap.behavior.stochasticgame.GameAnalysis} objects.
 * In the GUI's list of a game's actions, the action name is
 * selected joint action in the currently rendered state.
 * @author James MacGlashan
 *
 */
public class GameSequenceVisualizer extends JFrame {

	private static final long serialVersionUID = 1L;
	
	
	//Frontend GUI
	protected Visualizer							painter;
	protected TextArea								propViewer;
	
	protected JList									episodeList;
	protected JScrollPane							episodeScroller;
	
	protected JList									iterationList;
	protected JScrollPane							iterationScroller;
	
	protected Container								controlContainer;
	
	protected int									cWidth;
	protected int									cHeight;
	
	
	
	//Backend
	protected List <String>							episodeFiles;
	protected DefaultListModel						episodesListModel;
	protected StateParser							sp;

	protected List <GameAnalysis>					directGames;
	
	protected GameAnalysis							curGA;
	protected DefaultListModel						iterationListModel;
	
	protected SGDomain								domain;
	
	
	protected boolean								alreadyInitedGUI = false;
	
	
	
	/**
	 * Initializes the GameSequenceVisualizer. By default the state visualizer will be set to the size 800x800 pixels.
	 * @param v the visualizer used to render states
	 * @param d the domain in which the games took place
	 * @param sp a state parser that can be used to parse the states stored in the game files
	 * @param experimentDirectory the path to the directory containing the game files.
	 */
	public GameSequenceVisualizer(Visualizer v, SGDomain d, StateParser sp, String experimentDirectory){
		this.init(v, d, sp, experimentDirectory, 800, 800);
	}


	/**
	 * Initializes the GameSequenceVisualizer with programmatially supplied list of {@link burlap.behavior.stochasticgame.GameAnalysis} objects to view.
	 * By default the state visualizer will be 800x800 pixels.
	 * @param v the visualizer used to render states
	 * @param d the domain in which the games took place
	 * @param games the games to view
	 */
	public GameSequenceVisualizer(Visualizer v, SGDomain d, List <GameAnalysis> games){
		this.initWithDirectGames(v, d, games, 800, 800);
	}
	
	
	/**
	 * Initializes the GameSequenceVisualizer.
	 * @param v the visualizer used to render states
	 * @param d the domain in which the games took place
	 * @param sp a state parser that can be used to parse the states stored in the game files
	 * @param experimentDirectory the path to the directory containing the game files.
	 * @param width the width of the state visualizer canvas
	 * @param height the height of the state visualizer canvas
	 */
	public GameSequenceVisualizer(Visualizer v, SGDomain d, StateParser sp, String experimentDirectory, int width, int height){
		this.init(v, d, sp, experimentDirectory, width, height);
	}


	/**
	 * Initializes the GameSequenceVisualizer with programmatially supplied list of {@link burlap.behavior.stochasticgame.GameAnalysis} objects to view.
	 * @param v the visualizer used to render states
	 * @param d the domain in which the games took place
	 * @param games the games to view
	 * @param w the width of the state visualizer canvas
	 * @param h the height of the state visualizer canvas
	 */
	public GameSequenceVisualizer(Visualizer v, SGDomain d, List<GameAnalysis> games, int w, int h){
		this.initWithDirectGames(v, d, games, w, h);
	}
	
	
	/**
	 * Initializes the GameSequenceVisualizer.
	 * @param v the visualizer used to render states
	 * @param d the domain in which the games took place
	 * @param sp a state parser that can be used to parse the states stored in the game files
	 * @param experimentDirectory the path to the directory containing the game files.
	 * @param w the width of the state visualizer canvas
	 * @param h the height of the state visualizer canvas
	 */
	public void init(Visualizer v, SGDomain d, StateParser sp, String experimentDirectory, int w, int h){
		
		painter = v;
		domain = d;
		
		//get rid of trailing / and pull out the file paths
		if(experimentDirectory.charAt(experimentDirectory.length()-1) == '/'){
			experimentDirectory = experimentDirectory.substring(0, experimentDirectory.length());
		}
		this.parseGameFiles(experimentDirectory);
		
		cWidth = w;
		cHeight = h;
		
		this.sp = sp;
		
		
		this.initGUI();
		
		
	}



	/**
	 * Initializes the GameSequenceVisualizer.
	 * @param v the visualizer used to render states
	 * @param d the domain in which the games took place
	 * @param games the games to view
	 * @param w the width of the state visualizer canvas
	 * @param h the height of the state visualizer canvas
	 */
	public void initWithDirectGames(Visualizer v, SGDomain d, List<GameAnalysis> games, int w, int h){

		painter = v;
		domain = d;

		this.directGames = games;
		this.episodesListModel = new DefaultListModel();
		int c = 0;
		for(GameAnalysis ga : this.directGames){
			episodesListModel.addElement("game_" + c);
			c++;
		}

		cWidth = w;
		cHeight = h;


		this.initGUI();


	}
	
	
	/**
	 * Initializes the GUI and presents it to the user.
	 */
	public void initGUI(){
		
		if(this.alreadyInitedGUI){
			return;
		}
		
		this.alreadyInitedGUI = true;

		//set viewer components
		propViewer = new TextArea();
		propViewer.setEditable(false);
		painter.setPreferredSize(new Dimension(cWidth, cHeight));
		propViewer.setPreferredSize(new Dimension(cWidth, 100));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		getContentPane().add(painter, BorderLayout.CENTER);
		getContentPane().add(propViewer, BorderLayout.SOUTH);
		
		
		
		//set episode component
		episodeList = new JList(episodesListModel);
		
		
		episodeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		episodeList.setLayoutOrientation(JList.VERTICAL);
		episodeList.setVisibleRowCount(-1);
		episodeList.addListSelectionListener(new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent e) {
				handleEpisodeSelection(e);
			}
		});
		
		episodeScroller = new JScrollPane(episodeList);
		episodeScroller.setPreferredSize(new Dimension(100, 600));
		
		
		
		//set iteration component
		iterationListModel = new DefaultListModel();
		iterationList = new JList(iterationListModel);
		
		iterationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		iterationList.setLayoutOrientation(JList.VERTICAL);
		iterationList.setVisibleRowCount(-1);
		iterationList.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e){
				handleIterationSelection(e);
			}
		});
		
		iterationScroller = new JScrollPane(iterationList);
		iterationScroller.setPreferredSize(new Dimension(150, 600));
		
		
		
		//add episode-iteration lists to window
		controlContainer = new Container();
		controlContainer.setLayout(new BorderLayout());
		
		
		controlContainer.add(episodeScroller, BorderLayout.WEST);
		controlContainer.add(iterationScroller, BorderLayout.EAST);
		
		
		
		getContentPane().add(controlContainer, BorderLayout.EAST);
		
		
		
		//display the window
		pack();
		setVisible(true);
		
	}
	
	
	private void parseGameFiles(String directory){
		
		File dir = new File(directory);
		final String ext = ".game";
		
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if(name.endsWith(ext)){
					return true;
				}
				return false;
			}
		};
		String[] children = dir.list(filter);
		Arrays.sort(children, new AlphanumericSorting());
		
		episodeFiles = new ArrayList<String>(children.length);
		episodesListModel = new DefaultListModel();
		
		for(int i = 0; i < children.length; i++){
			episodeFiles.add(directory + "/" + children[i]);
			episodesListModel.addElement(children[i].substring(0, children[i].indexOf(ext)));
			//System.out.println(files.get(i));
		}
		
		
		
	}
	
	
	private void setIterationListData(){
		
		//clear the old contents
		iterationListModel.clear();
		
		
		//add each action (which is taken in the state being renderd)
		for(JointAction ja : curGA.getJointActions()){
			iterationListModel.addElement(ja.toString());
		}
		
		//add the final state
		iterationListModel.addElement("final state");
		
	}
	
	private void handleEpisodeSelection(ListSelectionEvent e){
		
		if (e.getValueIsAdjusting() == false) {

			int ind = episodeList.getSelectedIndex();
			//System.out.println("epsidoe id: " + ind);
       		if (ind != -1) {
       			
				//System.out.println("Loading Episode File...");
				if(this.directGames == null) {
					curGA = GameAnalysis.parseFileIntoGA(episodeFiles.get(ind), domain, sp);
				}
				else{
					curGA = this.directGames.get(ind);
				}
				//curEA = EpisodeAnalysis.readEpisodeFromFile(episodeFiles.get(ind));
				//System.out.println("Finished Loading Episode File.");
				
				painter.updateState(new State()); //clear screen
				this.setIterationListData();
				
			}
			else{
				//System.out.println("canceled selection");
			}
			
		}
		
	
	}
	
	private void handleIterationSelection(ListSelectionEvent e){
		
		if (e.getValueIsAdjusting() == false) {

       		if (iterationList.getSelectedIndex() != -1) {
				//System.out.println("Changing visualization...");
				int index = iterationList.getSelectedIndex();
				
				State curState = curGA.getState(index);
				
				
				
				//draw it and update prop list
				//System.out.println(curState.getCompleteStateDescription()); //uncomment to print to terminal
				painter.updateState(curState);
				this.updatePropTextArea(curState);
				
				//System.out.println("Finished updating visualization.");
			}
			else{
				//System.out.println("canceled selection");
			}
			
		}
	
	}
	
	
	private void updatePropTextArea(State s){
		
		StringBuffer buf = new StringBuffer();
		
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
