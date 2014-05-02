package burlap.behavior.singleagent;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.TextArea;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.visualizer.Visualizer;





/**
 * This class is used to visualize a set of episodes that have been saves to files in a common directory. In an episode list, the action name is
 * selected action in the currently rendered state.
 * @author James MacGlashan
 *
 */
public class EpisodeSequenceVisualizer extends JFrame{

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
	
	protected EpisodeAnalysis						curEA;
	protected DefaultListModel						iterationListModel;
	
	protected Domain								domain;
	
	protected boolean								alreadyInitedGUI = false;
	
	
	
	/**
	 * Initializes the EpisodeSequenceVisualizer. By default the state visualizer will be set to the size 800x800 pixels.
	 * @param v the visualizer used to render states
	 * @param d the domain in which the episodes took place
	 * @param sp a state parser that can be used to parse the states stored in the episode files
	 * @param experimentDirectory the path to the directory containing the episode files.
	 */
	public EpisodeSequenceVisualizer(Visualizer v, Domain d, StateParser sp, String experimentDirectory){
		
		this.init(v, d, sp, experimentDirectory, 800, 800);
		
	}
	
	/**
	 * Initializes the EpisodeSequenceVisualizer.
	 * @param v the visualizer used to render states
	 * @param d the domain in which the episodes took place
	 * @param sp a state parser that can be used to parse the states stored in the episode files
	 * @param experimentDirectory the path to the directory containing the episode files.
	 * @param w the width of the state visualizer canvas
	 * @param h the height of the state visualizer canvas
	 */
	public void init(Visualizer v, Domain d, StateParser sp, String experimentDirectory, int w, int h){
		
		painter = v;
		domain = d;
		
		//get rid of trailing / and pull out the file paths
		if(experimentDirectory.charAt(experimentDirectory.length()-1) == '/'){
			experimentDirectory = experimentDirectory.substring(0, experimentDirectory.length());
		}
		this.parseEpisodeFiles(experimentDirectory);
		
		cWidth = w;
		cHeight = h;
		
		this.sp = sp;
		
		
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
		
		/*
		//add render movie button
		JButton renderButton = new JButton("Render Episode Movie");
		renderButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//ExperimentVisualizer.this.handleEpisodeRender();
				
			}
		});
		
		controlContainer.add(renderButton, BorderLayout.SOUTH);*/
		
		getContentPane().add(controlContainer, BorderLayout.EAST);
		
		
		
		//display the window
		pack();
		setVisible(true);
		
	}
	
	
	private void parseEpisodeFiles(String directory){
		
		File dir = new File(directory);
		final String ext = ".episode";
		
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if(name.endsWith(ext)){
					return true;
				}
				return false;
			}
		};
		String[] children = dir.list(filter);
		
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
		for(GroundedAction ga : curEA.actionSequence){
			iterationListModel.addElement(ga.toString());
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
       			curEA = EpisodeAnalysis.parseFileIntoEA(episodeFiles.get(ind), domain, sp);
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
				
				State curState = curEA.getState(index);
				
				
				
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
