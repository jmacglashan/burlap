package burlap.oomdp.stocashticgames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JointAction implements Iterable<GroundedSingleAction>{

	public Map <String, GroundedSingleAction>		actions;
	
	public JointAction(){
		actions = new HashMap<String, GroundedSingleAction>();
	}
	
	public JointAction(int capacity){
		actions = new HashMap<String, GroundedSingleAction>(capacity);
	}
	
	public JointAction(List <GroundedSingleAction> actions){
		for(GroundedSingleAction gsa : actions){
			this.addAction(gsa);
		}
	}
	
	public void addAction(GroundedSingleAction action){
		actions.put(action.actingAgent, action);
	}
	
	public int size(){
		return actions.size();
	}
	
	public List <GroundedSingleAction> getActionList(){
		return new ArrayList<GroundedSingleAction>(actions.values());
	}
	
	public GroundedSingleAction action(String agentName){
		return actions.get(agentName);
	}
	
	public List <String> getAgentNames(){
		List <String> anames = new ArrayList<String>(actions.size());
		
		List <GroundedSingleAction> gsas = this.getActionList();
		for(GroundedSingleAction gsa : gsas){
			anames.add(gsa.actingAgent);
		}
		
		return anames;
		
	}
	
	@Override
	public String toString(){
		StringBuffer buf = new StringBuffer(100);
		List <GroundedSingleAction> gsas = this.getActionList();
		for(int i = 0; i < gsas.size(); i++){
			if(i > 0){
				buf.append(';');
			}
			buf.append(gsas.get(i).toString());
		}
		
		return buf.toString();
	}
	

	@Override
	public Iterator<GroundedSingleAction> iterator() {
		return new Iterator<GroundedSingleAction>() {
			
			List <GroundedSingleAction> gsas = getActionList();
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < gsas.size();
			}

			@Override
			public GroundedSingleAction next() {
				return gsas.get(i++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}
	
	
}

