package burlap.oomdp.core;


import burlap.oomdp.core.states.State;

import java.util.Arrays;

/**
 * Propositional functions are defined to be evaluated on object parameters and this class provides a
 * definition for a grounded propositional function; that is, it specifies specific object parameters
 * on which the propositional function should be evaluated. The equals method correctly accounts for
 * parameter order groups defined by this objects propositional function.
 * @author James MacGlashan
 *
 */
public class GroundedProp implements Cloneable{

	public PropositionalFunction pf;
	public String [] params;
	
	/**
	 * Initializes a grounded propositional function
	 * @param p the propositional function that does the evaluation
	 * @param par the object name references on which the propositional function would be evaluated
	 */
	public GroundedProp(PropositionalFunction p, String [] par){
		pf = p;
		params = par;
	}
	
	
	public Object clone()
	{
		try {return super.clone();}
		catch(Exception e) {return null;}
	}
	
	/**
	 * Evaluates whether this grounded propositional function is true in the provided state.
	 * @param s the state on which to evaluate the grounded propositional function
	 * @return true if the propositional function bounded to this groundedProp's parameters is true in the specified state.
	 */
	public boolean isTrue(State s){
		return pf.isTrue(s, params);
	}
	
	/**
	 * Returns a string representation of this grounded prop. If this groundedProp is specified by two parameters (ob1, ob2)
	 * then the returned format is: "PFName(ob1, ob2)"
	 */
	public String toString(){
	    StringBuilder buf = new StringBuilder();
		
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
	
	
	
	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(params);
        result = prime * result + ((pf == null) ? 0 : pf.hashCode());
        return result;
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
					if(orderGroup.equals(that.pf.parameterOrderGroup[j]) && params[i].equals(that.params[j])){
					    foundMatch = true;
                        break;
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
