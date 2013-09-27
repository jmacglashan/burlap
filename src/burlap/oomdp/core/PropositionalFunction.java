package burlap.oomdp.core;


public abstract class PropositionalFunction {

	protected String					name;					//name of the propositional function
	protected Domain					domain;					//domain that hosts this function
	protected String []					parameterClasses;		//list of class names for each parameter of the function
	protected String []					parameterOrderGroup;	//setting two or more parameters to the same order group indicates that the function evaluate the same regardless of which specific object is set to each parameter
	protected String					pfClass;				//optional; allows propositional functions to be grouped by class names
	
	
	//parameterClasses is expected to be comma delimited with no unnecessary spaces
	public PropositionalFunction(String name, Domain domain, String parameterClasses){
		
		String [] pClassArray;
		if(parameterClasses.equals("")){
			pClassArray = new String[0];
		}
		else{
			pClassArray = parameterClasses.split(",");
		}
		
		String [] pog = new String[pClassArray.length];
		for(int i = 0; i < pog.length; i++){
			pog[i] = name + ".P" + i;
		}
		
		this.init(name, domain, pClassArray, pog, name);
		
	}
	
	public PropositionalFunction(String name, Domain domain, String parameterClasses, String pfClassName){
		
		String [] pClassArray;
		if(parameterClasses.equals("")){
			pClassArray = new String[0];
		}
		else{
			pClassArray = parameterClasses.split(",");
		}
		
		String [] pog = new String[pClassArray.length];
		for(int i = 0; i < pog.length; i++){
			pog[i] = name + ".P" + i;
		}
		
		this.init(name, domain, pClassArray, pog, pfClassName);
		
	}
	
	public PropositionalFunction(String name, Domain domain, String [] parameterClasses){
		
		String [] rcn = new String[parameterClasses.length];
		for(int i = 0; i < rcn.length; i++){
			rcn[i] = name + ".P" + i;
		}
		
		this.init(name, domain, parameterClasses, rcn, name);
		
	}
	
	public PropositionalFunction(String name, Domain domain, String [] parameterClasses, String pfClassName){
		
		String [] rcn = new String[parameterClasses.length];
		for(int i = 0; i < rcn.length; i++){
			rcn[i] = name + ".P" + i;
		}
		
		this.init(name, domain, parameterClasses, rcn, pfClassName);
		
	}
	
	public PropositionalFunction(String name, Domain domain, String [] parameterClasses, String [] replacedClassNames){
		this.init(name, domain, parameterClasses, replacedClassNames, name);
	}
	
	public PropositionalFunction(String name, Domain domain, String [] parameterClasses, String [] parameterOrderGroup, String pfClassName){
		this.init(name, domain, parameterClasses, parameterOrderGroup, pfClassName);
	}
	
	public final void init(String name, Domain domain, String [] parameterClasses, String [] parameterOrderGroup, String pfClass){
		this.name = name;
		this.domain = domain;
		this.domain.addPropositionalFunction(this);
		this.parameterClasses = parameterClasses;
		this.parameterOrderGroup = parameterOrderGroup;
		this.pfClass = pfClass;
	}
	
	public final String getName(){
		return name;
	}
	
	
	public final String[] getParameterClasses(){
		return parameterClasses;
	}
	
	public final String[] getParameterOrderGroups(){
		return parameterOrderGroup;
	}
	
	public final void setClassName(String cn){
		pfClass = cn;
	}
	
	public final String getClassName(){
		return pfClass;
	}
	
	/**
	 * Returns whether the propositional function is true for the given state with the given parameters
	 * params is expected to be comma delimited with no unnecessary spaces
	 * @param st the state that is being checked
	 * @param params the parameters being passed in to the propositional function
	 * @return whether the propositional function is true
	 */
	public final boolean isTrue(State st, String params){
		return isTrue(st, params.split(","));
	}
	
	/**
	 * Returns whether the propositional function is true for the given state with the given parameters
	 * This version is preferred to the comma delimited version.
	 * @param st the state that is being checked
	 * @param params the parameters being passed in to the propositional function
	 * @return whether the propositional function is true
	 */
	public abstract boolean isTrue(State st, String [] params);
	
	
	public boolean equals(Object obj){
		PropositionalFunction op = (PropositionalFunction)obj;
		if(op.name.equals(name))
			return true;
		return false;
	}
	
	public String toString() {
		return this.name;
	}

	public int hashCode(){
		return name.hashCode();
	}
	
	
}
