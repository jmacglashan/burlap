package minecraft.MinecraftDomain.PropositionalFunctions;

import java.util.List;

import minecraft.NameSpace;
import minecraft.MinecraftDomain.Helpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class AgentLookingInDirectionOfBlock extends PropositionalFunction {
	private String objectLookingToward;
	
	/**
	 * 
	 * @param name
	 * @param domain
	 * @param parameterClasses
	 * @param direction
	 * @param objectLookingToward
	 */
	public AgentLookingInDirectionOfBlock(String name, Domain domain, String[] parameterClasses, String objectLookingToward) {
		super(name, domain, parameterClasses);
		this.objectLookingToward = objectLookingToward;
	}
	
	@Override
	public boolean isTrue(State state, String[] params) {
		return Helpers.agentLookingInDirectionOfBlock(state, this.objectLookingToward);
		
	}
	
}
