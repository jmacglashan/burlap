package burlap.domain.singleagent.graphdefined;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import burlap.debugtools.RandomFactory;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;


public class GraphDefinedDomain implements DomainGenerator {

	public static final String												ATTNODE = "node";
	public static final String												CLASSAGENT = "agent";
	public static final String												BASEACTIONNAME = "action";
	
	
	
	protected int															numNodes;
	protected int															maxActions;
	protected Map<Integer, Map<Integer, Set<NodeTransitionProbibility>>>	transitionDynamics;
	

	
	public GraphDefinedDomain(int numNodes) {
		this.numNodes = numNodes;
		this.maxActions = 0;
		this.transitionDynamics = new HashMap<Integer, Map<Integer,Set<NodeTransitionProbibility>>>();
		
		for(int i = 0; i < this.numNodes; i++){
			this.transitionDynamics.put(i, new HashMap<Integer,Set<NodeTransitionProbibility>>());
		}
		
	}
	
	
	public void setTransition(int srcNode, int action, int tNode, double p){
		
		if(action >= this.maxActions){
			this.maxActions = action+1;
		}
		
		NodeTransitionProbibility ntp = new NodeTransitionProbibility(tNode, p);
		Map<Integer,Set<NodeTransitionProbibility>> actionMap = this.transitionDynamics.get(srcNode);
		Set <NodeTransitionProbibility> nts = actionMap.get(action);
		if(nts == null){
			nts = new HashSet<GraphDefinedDomain.NodeTransitionProbibility>();
			actionMap.put(action, nts);
		}
		nts.add(ntp);

	}
	

	@Override
	public Domain generateDomain() {
		
		Domain domain = new SADomain();
		
		Attribute na = new Attribute(domain, ATTNODE, Attribute.AttributeType.DISC);
		na.setDiscValuesForRange(0, this.numNodes-1, 1);
		
		ObjectClass aclass = new ObjectClass(domain, CLASSAGENT);
		aclass.addAttribute(na);
		
		for(int i = 0; i < this.maxActions; i++){
			new GraphAction(domain, i);
		}
		
		
		return domain;
	}
	
	
	
	public static State getState(Domain d, int sNode){
		State s = new State();
		
		ObjectInstance o = new ObjectInstance(d.getObjectClass(CLASSAGENT), CLASSAGENT);
		o.setValue(ATTNODE, sNode);
		
		s.addObject(o);
		
		return s;
	}
	
	
	
	public class NodeTransitionProbibility{
		
		public int 		transitionTo;
		public double 	probabiltiy;
		
		public NodeTransitionProbibility(int transitionTo, double probability){
			this.transitionTo = transitionTo;
			this.probabiltiy = probability;
		}
		
		@Override
		public boolean equals(Object other){
			if(this == other){
				return true;
			}
			
			if(!(other instanceof NodeTransitionProbibility)){
				return false;
			}
			
			NodeTransitionProbibility that = (NodeTransitionProbibility)other;
			return transitionTo == that.transitionTo;
		}
		
		@Override
		public int hashCode(){
			return transitionTo;
		}
		
		
	}
	
	
	
	
	class GraphAction extends Action{

		protected Random rand;
		protected int aId;
		
		public GraphAction(Domain domain, int aId){
			super(BASEACTIONNAME+aId, domain, "");
			this.aId = aId;
			rand = RandomFactory.getMapped(0);
		}
		
		
		@Override
		public boolean applicableInState(State st, String [] params){
			
			ObjectInstance o = st.getObjectsOfTrueClass(CLASSAGENT).get(0);
			int n = o.getDiscValForAttribute(ATTNODE);
			
			Map<Integer, Set<NodeTransitionProbibility>> actionMap = transitionDynamics.get(n);
			Set<NodeTransitionProbibility> transitions = actionMap.get(aId);
			if(transitions == null){
				return false;
			}
			
			return true;
		}
		
		
		@Override
		protected State performActionHelper(State st, String[] params) {
			
			ObjectInstance o = st.getObjectsOfTrueClass(CLASSAGENT).get(0);
			int n = o.getDiscValForAttribute(ATTNODE);
			
			Map<Integer, Set<NodeTransitionProbibility>> actionMap = transitionDynamics.get(n);
			Set<NodeTransitionProbibility> transitions = actionMap.get(aId);
			
			double roll = rand.nextDouble();
			double sumP = 0.;
			int selection = 0;
			for(NodeTransitionProbibility ntp : transitions){
				sumP += ntp.probabiltiy;
				if(roll < sumP){
					selection = ntp.transitionTo;
					break;
				}
			}
			
			o.setValue(ATTNODE, selection);
			
			return st;
		}
		
		
		@Override
		public List<TransitionProbability> getTransitions(State st, String [] params){
			
			List <TransitionProbability> result = new ArrayList<TransitionProbability>();
			
			ObjectInstance o = st.getObjectsOfTrueClass(CLASSAGENT).get(0);
			int n = o.getDiscValForAttribute(ATTNODE);
			
			Map<Integer, Set<NodeTransitionProbibility>> actionMap = transitionDynamics.get(n);
			Set<NodeTransitionProbibility> transitions = actionMap.get(aId);
			
			for(NodeTransitionProbibility ntp : transitions){
				
				State ns = st.copy();
				ObjectInstance no = ns.getObjectsOfTrueClass(CLASSAGENT).get(0);
				no.setValue(ATTNODE, ntp.transitionTo);
				
				TransitionProbability tp = new TransitionProbability(ns, ntp.probabiltiy);
				result.add(tp);
				
			}
			
			
			return result;
			
		}
		
		
		
		
	}
	
	

}
