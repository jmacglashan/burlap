package burlap.behavior.functionapproximation.dense;

import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;

import java.util.LinkedList;
import java.util.List;

/**
 * Binary features that are determined from a list of {@link PropositionalFunction}s. The element for a corresponding
 * {@link PropositionalFunction} is set to 1, when any possible binding for the {@link PropositionalFunction} is true
 * for the input state.
 */
public class PFFeatures implements DenseStateFeatures {

	protected PropositionalFunction [] pfsToUse;
	
	
	/**
	 * Initializes using all propositional functions that belong to the domain
	 * @param domain the domain containing all the propositional functions to use
	 */
	public PFFeatures(OODomain domain){
		
		this.pfsToUse = new PropositionalFunction[domain.propFunctions().size()];
		int i = 0;
		for(PropositionalFunction pf : domain.propFunctions()){
			this.pfsToUse[i] = pf;
			i++;
		}
		
	}
	
	/**
	 * Initializes using the list of given propositional functions.
	 * @param pfs the propositional functions to use.
	 */
	public PFFeatures(List<PropositionalFunction> pfs){
		this.pfsToUse = new PropositionalFunction[pfs.size()];
		this.pfsToUse = pfs.toArray(this.pfsToUse);
	}
	
	
	/**
	 * Initializes using the array of given propositional functions.
	 * @param pfs the propositional functions to use.
	 */
	public PFFeatures(PropositionalFunction [] pfs){
		this.pfsToUse = pfs.clone();
	}
	
	
	@Override
	public double[] features(State s) {
		
		List<Double> featureValueList = new LinkedList<Double>();
		for(PropositionalFunction pf : this.pfsToUse){
			//List<GroundedProp> gps = s.getAllGroundedPropsFor(pf);
			List<GroundedProp> gps = pf.getAllGroundedPropsForState(s);
			for(GroundedProp gp : gps){
				if(gp.isTrue((OOState)s)){
					featureValueList.add(1.);
				}
				else{
					featureValueList.add(0.);
				}
			}
		}
		
		double [] fv = new double[featureValueList.size()];
		int i = 0;
		for(double f : featureValueList){
			fv[i] = f;
			i++;
		}
		
		return fv;
	}

	@Override
	public DenseStateFeatures copy() {
		return new PFFeatures(this.pfsToUse.clone());
	}
}
