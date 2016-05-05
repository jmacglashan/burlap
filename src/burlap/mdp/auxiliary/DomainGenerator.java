package burlap.mdp.auxiliary;

import burlap.mdp.core.Domain;

/**
 * This class provides a simple interface for constructing domains, but it is not required to create domains. All domains that
 * exist in BURLAP adhere to this interface for constructing domains.
 * @author James MacGlashan
 */
public interface DomainGenerator {

	/**
	 * Returns a newly instanced Domain object
	 * @return the newly instantiated Domain object.
	 */
	public Domain generateDomain();
	
	
}
