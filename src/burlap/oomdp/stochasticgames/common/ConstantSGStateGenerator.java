package burlap.oomdp.stochasticgames.common;

import java.util.List;

import burlap.datastructures.HashedAggregator;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.SGAgent;
import burlap.oomdp.stochasticgames.SGStateGenerator;


/**
 * A stochastic games state generator that always returns the same base state, which is specified via the constructor. The
 * provided source state does *not* need to worry about the object name of OO-MDP objects corresponding to agent states.
 * This generator will automatically reassign the relevant OO-MDP object names to the names of each agent by querying the agent type
 * and agent name in the list of agents provides to the {@link #generateState(List)} method. This reassignment is done
 * each time the {@link #generateState(List)} method is called on a copy of the originally provided state.
 * @author James MacGlashan
 *
 */
public class ConstantSGStateGenerator extends SGStateGenerator {

	/**
	 * The source state that will be copied and returned by the {@link #generateState(List)} method.
	 */
	protected State srcState;
	
	
	/**
	 * Initializes.
	 * @param srcState The source state that will be copied and returned by the {@link #generateState(List)} method.
	 */
	public ConstantSGStateGenerator(State srcState){
		this.srcState = srcState;
	}
	
	@Override
	public State generateState(List<SGAgent> agents) {
		
		State s = this.srcState.copy();
		HashedAggregator<String> counts = new HashedAggregator<String>();
		
		for(SGAgent a : agents){
			String agentClassName = a.getAgentType().oclass.name;
			int index = (int) counts.v(agentClassName);
			List<ObjectInstance> possibleAgentObjects = s.getObjectsOfClass(agentClassName);
			if(possibleAgentObjects.size() <= index){
				throw new RuntimeException("Error: Constant state used by ConstanteStateSGGenerator does not have enough oo-mdp objects for agents defined by class: " + agentClassName);
			}
			ObjectInstance agentObject = possibleAgentObjects.get(index);
			s.renameObject(agentObject, a.getAgentName());
			
			counts.add(agentClassName, 1.);
			
		}
		
		return s;
		
	}

}
