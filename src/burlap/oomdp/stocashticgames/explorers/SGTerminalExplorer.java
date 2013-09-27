package burlap.oomdp.stocashticgames.explorers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.State;
import burlap.oomdp.stocashticgames.GroundedSingleAction;
import burlap.oomdp.stocashticgames.JointAction;
import burlap.oomdp.stocashticgames.JointActionModel;
import burlap.oomdp.stocashticgames.JointReward;
import burlap.oomdp.stocashticgames.SGDomain;
import burlap.oomdp.stocashticgames.SingleAction;


public class SGTerminalExplorer {

	protected SGDomain					domain;
	protected Map <String, String>		actionShortHand;
	protected JointActionModel			jam;
	protected JointAction				curJointAction;
	protected JointReward				rf;
	
	public SGTerminalExplorer(SGDomain domain, JointActionModel jam){
		this.domain = domain;
		this.jam = jam;
		this.setActionShortHand(new HashMap <String, String>());
		this.rf = null;
	}
	
	public SGTerminalExplorer(SGDomain domain, JointActionModel jam, Map <String, String> ash){
		this.domain = domain;
		this.jam = jam;
		this.setActionShortHand(ash);
		this.rf = null;
	}
	
	public void setTrackingRF(JointReward rf){
		this.rf = rf;
	}
	
	
	public void setActionShortHand(Map <String, String> ash){
		this.actionShortHand = ash;
		List <SingleAction> actionList = domain.getSingleActions();
		for(SingleAction a : actionList){
			this.addActionShortHand(a.actionName, a.actionName);
		}
	}
	
	public void addActionShortHand(String shortHand, String action){
		actionShortHand.put(shortHand, action);
	}
	
	public void exploreFromState(State st){
		
		curJointAction = new JointAction();
		
		State src = st.copy();
		String actionPromptDelimiter = "-----------------------------------";
		
		this.printState(st);
		System.out.println(actionPromptDelimiter);
		
		while(true){
			
			
			BufferedReader in;
			String line;
			try{
			
				in = new BufferedReader(new InputStreamReader(System.in));
				line = in.readLine();
				
				if(line.equals("##reset##")){
					st = src;
					curJointAction = new JointAction();
					this.printState(st);
					System.out.println(actionPromptDelimiter);
				}
				else if(line.equals("##")){
					State ns = this.jam.performJointAction(st, curJointAction);
					
					if(this.rf != null){
						Map<String, Double> reward = rf.reward(st, curJointAction, ns);
						for(String aname : reward.keySet()){
							System.out.println("" + aname + ": " + reward.get(aname));
						}
						System.out.println("++++++++++++++++++++++++++++++++");
					}
					
					st = ns;
					
					curJointAction = new JointAction();
					this.printState(st);
					System.out.println(actionPromptDelimiter);
				}
				else{
					
					//split the string up into components
					String [] agacComps = line.split("::"); 
					String agentName = agacComps[0];
					
					String [] comps = agacComps[1].split(" ");
					String actionName = actionShortHand.get(comps[0]);
					
					if(actionName == null){
						actionName = comps[0];
					}
					
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
					
					SingleAction sa = this.domain.getSingleAction(actionName);
					if(sa == null){
						System.out.println("Unknown action: " + actionName);
					}
					else{
						GroundedSingleAction gsa = new GroundedSingleAction(agentName, sa, params);
						if(sa.isApplicableInState(st, agentName, params)){
							System.out.println("Setting action: " + agentName + "::" + actionName);
							curJointAction.addAction(gsa);
						}
						else{
							System.out.println("Cannot apply this action in this state: " + agentName + "::" + actionName);
						}
					}
					

					
				}
				
				
			}
				
			catch(Exception e){
				System.out.println(e);
			}
			
		}
		
		
	}
	
	public void printState(State st){
		
		System.out.println(st.getStateDescription());
		
	}

}
