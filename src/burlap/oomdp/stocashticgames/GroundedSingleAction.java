package burlap.oomdp.stocashticgames;

public class GroundedSingleAction {

	public String actingAgent;
	public SingleAction action;
	public String [] params;
	
	public GroundedSingleAction(String actingAgent, SingleAction a, String [] p){
		this.init(actingAgent, a, p);
	}
	
	
	//may also take parameters as a single string that is comma delineated
	public GroundedSingleAction(String actingAgent, SingleAction a, String p){
		
		String [] ps = null;
		if(p.equals("")){
			ps = new String[0];
		}
		else{
			ps = p.split(",");
		}
		this.init(actingAgent, a, ps);
	}
	
	
	private void init(String actingAgent, SingleAction a, String [] p){
		this.actingAgent = actingAgent;
		this.action = a;
		this.params = p;
	}
	
	
	public boolean isPamaeterized(){
		return action.isPamaeterized();
	}
	
	public String justActionString(){
		StringBuffer buf = new StringBuffer();
		buf.append(action.actionName);
		for(int i = 0; i < params.length; i++){
			buf.append(" ").append(params[i]);
		}
		
		return buf.toString();
		
	}
	
	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append(actingAgent).append(":");
		buf.append(action.actionName);
		for(int i = 0; i < params.length; i++){
			buf.append(" ").append(params[i]);
		}
		
		return buf.toString();
	}
	
	
	@Override
	public boolean equals(Object other){
		
		if(this == other){
			return true;
		}
		
		if(!(other instanceof GroundedSingleAction)){
			return false;
		}
		
		GroundedSingleAction go = (GroundedSingleAction)other;
		if(!this.action.actionName.equals(go.action.actionName)){
			return false;
		}
		
		String [] rclasses = this.action.parameterRenames;
		
		for(int i = 0; i < this.params.length; i++){
			String p = this.params[i];
			String replaceClass = rclasses[i];
			boolean foundMatch = false;
			for(int j = 0; j < go.params.length; j++){
				if(p.equals(go.params[j]) && replaceClass.equals(rclasses[j])){
					foundMatch = true;
					break;
				}
			}
			if(!foundMatch){
				return false;
			}
			
		}
		
		return true;
	}

}
