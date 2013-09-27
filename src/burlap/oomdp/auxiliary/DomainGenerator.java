package burlap.oomdp.auxiliary;

import burlap.oomdp.core.Domain;

/**
 * 
 * @author James
 * Any implementation of an OOMDP domain should use this interface in addition to having an internal Domain data member.
 * generateDomain() instatiated the internal Domain object with all the proper data then returns it for use with a visualizer and/or explorer.
 */
public interface DomainGenerator {

	/**
	 * 
	 * @return the newly instantiated Domain object.
	 */
	public Domain generateDomain();
	
	
}
