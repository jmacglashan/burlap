package burlap.testing;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.states.ImmutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;

public class TestHashing {
	TestGridWorld gridWorldTest;
	
	@Before
	public void setup() {
		this.gridWorldTest = new TestGridWorld();
		this.gridWorldTest.setup();
	}
	
	@Test
	public void testSimpleHashFactory() {
		SADomain domain = (SADomain)this.gridWorldTest.getDomain();
		State startState = this.gridWorldTest.generateState();
		HashableStateFactory factory = new SimpleHashableStateFactory();
		Set<HashableState> hashedStates = this.getReachableHashedStates(startState, domain, factory);
		assert(hashedStates.size() == 104);
	}
	
	@Test
	public void testSimpleHashFactoryIdentifierDependent() {
		SADomain domain = (SADomain)this.gridWorldTest.getDomain();
		State startState = this.gridWorldTest.generateState();
		HashableStateFactory factory = new SimpleHashableStateFactory(true);
		Set<HashableState> hashedStates = this.getReachableHashedStates(startState, domain, factory);
		assert(hashedStates.size() == 104);
	}
	
	@Test
	public void testSimpleHashFactoryCached() {
		SADomain domain = (SADomain)this.gridWorldTest.getDomain();
		State startState = this.gridWorldTest.generateState();
		HashableStateFactory factory = new SimpleHashableStateFactory(false, true);
		Set<HashableState> hashedStates = this.getReachableHashedStates(startState, domain, factory);
		assert(hashedStates.size() == 104);
	}
	
	@Test
	public void testSimpleHashFactoryIdentifierDependentCached() {
		SADomain domain = (SADomain)this.gridWorldTest.getDomain();
		State startState = this.gridWorldTest.generateState();
		HashableStateFactory factory = new SimpleHashableStateFactory(true, true);
		Set<HashableState> hashedStates = this.getReachableHashedStates(startState, domain, factory);
		assert(hashedStates.size() == 104);
	}
	
	@Test
	public void testImmutableSimpleHashFactoryIdentifierDependentCached() {
		SADomain domain = (SADomain)this.gridWorldTest.getDomain();
		State startState = this.gridWorldTest.generateState();
		HashableStateFactory factory = new SimpleHashableStateFactory(true, true);
		Set<HashableState> hashedStates = this.getReachableHashedStates(new ImmutableState(startState), domain, factory);
		assert(hashedStates.size() == 104);
	}
	
	
	public Set <HashableState> getReachableHashedStates(State from, SADomain inDomain, HashableStateFactory usingHashFactory){
		
		Set<HashableState> hashedStates = new HashSet<HashableState>();
		HashableState shi = usingHashFactory.hashState(from);
		List <Action> actions = inDomain.getActions();
		
		LinkedList <HashableState> openList = new LinkedList<HashableState>();
		openList.offer(shi);
		hashedStates.add(shi);
		while(openList.size() > 0){
			HashableState sh = openList.poll();
			
			//List <GroundedAction> gas = sh.s.getAllGroundedActionsFor(actions);
			List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(actions, sh.s);
			for(GroundedAction ga : gas){
				List <TransitionProbability> tps = ga.getTransitions(sh.s);
				for(TransitionProbability tp : tps){
					HashableState nsh = usingHashFactory.hashState(tp.s);
					
					for (HashableState hashedState : hashedStates) {
						boolean sameObject = (hashedState == nsh);
						boolean valueEquals = (hashedState.equals(nsh));
						boolean hashEquals = (hashedState.hashCode() == nsh.hashCode());
						if (sameObject || valueEquals) {
							assert(hashEquals); // Same state, hashes need to be equal
						}
						if (!hashEquals) {
							assert(!sameObject && !valueEquals);
						}
					}
					
					if(!hashedStates.contains(nsh)){
						openList.offer(nsh);
						hashedStates.add(nsh);
					}
				}
				
			}
			
		}
		
		return hashedStates;
	}
}
