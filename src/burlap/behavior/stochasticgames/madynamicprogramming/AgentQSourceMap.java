package burlap.behavior.stochasticgames.madynamicprogramming;

import burlap.behavior.stochasticgames.agents.maql.MultiAgentQLearning;
import burlap.mdp.stochasticgames.SGAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Multiagent value function planning typicall entails storing a separate Q value for each joint action for each agent. This interface
 * provides a means to access the Q-values for any given agent.
 * @author James MacGlashan
 *
 */
public interface AgentQSourceMap {

	
	/**
	 * Returns a QSource which can be used to query the Q-values of a given agent.
	 * @param agentName the name of the agent for which Q-values will be queried.
	 * @return A {@link QSourceForSingleAgent} object that allows the Q-values for a single agent to be queried.
	 */
	public QSourceForSingleAgent agentQSource(String agentName);
	
	
	/**
	 * An implementation of the {@link AgentQSourceMap} in which the sources are specified by a hash map.
	 * @author James MacGlashan
	 *
	 */
	public class HashMapAgentQSourceMap implements AgentQSourceMap{

		protected Map<String, QSourceForSingleAgent> qSourceMapping;
		
		
		/**
		 * Initializes with the Q-source hashmap ot be used.
		 * @param qSourceMapping the source hash map to be used.
		 */
		public HashMapAgentQSourceMap(Map<String, QSourceForSingleAgent> qSourceMapping){
			this.qSourceMapping = qSourceMapping;
		}
		
		/**
		 * Sets the Q-source hash map to be used.
		 * @param qSourceMapping the source hash map to be used.
		 */
		public void setQSourceMap(Map<String, QSourceForSingleAgent> qSourceMapping){
			this.qSourceMapping = qSourceMapping;
		}
		
		@Override
		public QSourceForSingleAgent agentQSource(String agentName) {
			return this.qSourceMapping.get(agentName);
		}
		
		
		
	}
	
	
	
	/**
	 * An implementation of the {@link AgentQSourceMap} in which different agent objects each maintain their own personal Q-source.
	 * This is useful if all agents are implementing the same multi-agent learning algorithm so that each agent doesn't have to
	 * replicate the learned Q-values for all other agents; instead only one copy will be kept for each agent. For example,
	 * this can be useful when all agents implement CoCo-Q learning.
	 * @author James MacGlashan
	 *
	 */
	public class MAQLControlledQSourceMap implements AgentQSourceMap{

		protected Map<String, MultiAgentQLearning> qSourceMapping;
		
		/**
		 * Initializes with the list of agents that each keep their own Q-source. Agent instances
		 * are expected to be of type {@link MultiAgentQLearning}. This constructor accepts a list of
		 * regular agents, however, to make construction easier. An exception will be thrown if all of all of the agents
		 * are not of the right type.
		 * @param agents a list of {@link burlap.mdp.stochasticgames.SGAgent} objects, each which is an instance {@link MultiAgentQLearning}.
		 */
		public MAQLControlledQSourceMap(List<SGAgent> agents){
			this.qSourceMapping = new HashMap<String, MultiAgentQLearning>(agents.size());
			for(SGAgent agent : agents){
				if(!(agent instanceof MultiAgentQLearning)){
					throw new RuntimeException("All agents passed to the MAQLControlledQSourceMap object must be of type MultiAgentQLearning");
				}
				this.qSourceMapping.put(agent.getAgentName(), (MultiAgentQLearning)agent);
			}
		}
		
		
		/**
		 * Initializes with a list of agents that each keep their own Q_source.
		 * @param agents a list of {@link MultiAgentQLearning} agents/
		 */
		public void setAgents(List<MultiAgentQLearning> agents){
			this.qSourceMapping = new HashMap<String, MultiAgentQLearning>(agents.size());
			for(MultiAgentQLearning agent : agents){
				this.qSourceMapping.put(agent.getAgentName(), agent);
			}
		}
		
		@Override
		public QSourceForSingleAgent agentQSource(String agentName) {
			return this.qSourceMapping.get(agentName).getMyQSource();
		}
		
		
		
	}
	
}
