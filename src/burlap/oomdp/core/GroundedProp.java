package burlap.oomdp.core;


public class GroundedProp implements Cloneable{

	public PropositionalFunction pf;
	public String [] params;
	
	public GroundedProp(PropositionalFunction p, String [] par){
		pf = p;
		params = par;
	}
	
	public Object clone()
	{
		try {return super.clone();}
		catch(Exception e) {return null;}
	}
	
	public boolean isTrue(State st){
		return pf.isTrue(st, params);
	}
	
	public String toString(){
		StringBuffer buf = new StringBuffer();
		
		buf.append(pf.name).append("(");
		for(int i = 0; i < params.length; i++){
			if(i > 0){
				buf.append(", ");
			}
			buf.append(params[i]);
		}
		buf.append(")");
		
		return buf.toString();
	}
	
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		
		if(!(obj instanceof GroundedProp)){
			return false;
		}
		
		GroundedProp that = (GroundedProp)obj;
		
		if(pf != that.pf){
			return false;
		}
		
		if(params.length != that.params.length){
			return false;
		}
		
		for(int i = 0; i < params.length; i++){
			if(!params[i].equals(that.params[i])){
				//check if there is another parameter with this reference that has the same rename class (which means parameters are order independent)
				String orderGroup = pf.parameterOrderGroup[i];
				boolean foundMatch = false;
				for(int j = 0; j < that.params.length; j++){
					if(j == i){
						continue; //already checked this
					}
					if(orderGroup.equals(that.pf.parameterOrderGroup[j])){
						if(params[i].equals(that.params[j])){
							foundMatch = true;
							break;
						}
					}
					
				}
				if(!foundMatch){
					return false;
				}
			}
		}
		
		return true;
		
	}
	
}
