package burlap.testing;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Before;
import org.junit.Test;

import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.ObjectInstance;
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
	public void testSimpleHashFactoryIdentifierIndependent() {
		SADomain domain = (SADomain)this.gridWorldTest.getDomain();
		State startState = this.gridWorldTest.generateState();
		HashableStateFactory factory = new SimpleHashableStateFactory();
		Set<HashableState> hashedStates = this.getReachableHashedStates(startState, domain, factory);
		assert(hashedStates.size() == 104);
		
		Set<HashableState> renamedStates = new HashSet<HashableState>();
		for (HashableState state : hashedStates) {
			State source = state.getSourceState();
			State renamed = this.renameObjects(source.copy());
			HashableState renamedHashed = factory.hashState(renamed);
			renamedStates.add(renamedHashed);
		}
		hashedStates.addAll(renamedStates);
		assert(hashedStates.size() == 104);
	}
	
	@Test
	public void testSimpleHashFactoryIdentifierDependent() {
		SADomain domain = (SADomain)this.gridWorldTest.getDomain();
		State startState = this.gridWorldTest.generateState();
		HashableStateFactory factory = new SimpleHashableStateFactory(true);
		Set<HashableState> hashedStates = this.getReachableHashedStates(startState, domain, factory);
		assert(hashedStates.size() == 104);
		
		Set<HashableState> renamedStates = new HashSet<HashableState>();
		for (HashableState state : hashedStates) {
			State source = state.getSourceState();
			State renamed = this.renameObjects(source.copy());
			HashableState renamedHashed = factory.hashState(renamed);
			renamedStates.add(renamedHashed);
		}
		hashedStates.addAll(renamedStates);
		assert(hashedStates.size() == 208);
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
	
	@Test
	public void testSimpleHashFactoryLargeState() {
		HashableStateFactory factory = new SimpleHashableStateFactory();
		
		testSimpleHashFactoryLargeState(factory, 10, 100, false);
		testSimpleHashFactoryLargeState(factory, 50, 1000, false);
		testSimpleHashFactoryLargeState(factory, 100, 10000, false);
		testSimpleHashFactoryLargeState(factory, 200,100000, false);
		testSimpleHashFactoryLargeState(factory, 500,100000, false);
		
		testSimpleHashFactoryLargeState(factory, 10, 100, true);
		testSimpleHashFactoryLargeState(factory, 20, 1000, true);
		testSimpleHashFactoryLargeState(factory, 50, 10000, true);
		testSimpleHashFactoryLargeState(factory, 100,100000, true);
	}
	
	public void testSimpleHashFactoryLargeState(HashableStateFactory factory, int width, int numRandomStates, boolean moveLocObjects) {
		GridWorldDomain gw = new GridWorldDomain(width, width);
		SADomain domain = (SADomain)gw.generateDomain();
		State startState = this.generateLargeGW(domain, width);
		Set<HashableState> hashedStates = this.generateRandomStates(domain, startState, factory, width, numRandomStates, moveLocObjects);
		Set<Integer> hashes = new HashSet<Integer>();
		for (HashableState hs : hashedStates) {
			hashes.add(hs.hashCode());
		}
		System.out.println("Hashed states: " + hashedStates.size() + ", hashes: " + hashes.size());
	}
	
	public int getHash1(int x, int y) {
		HashCodeBuilder b1 = new HashCodeBuilder(17,31);
		b1.append(x).append(y);
		return b1.toHashCode();
	}
	
	public int getHash2(int x, int y) {
		HashCodeBuilder b1 = new HashCodeBuilder(17,31);
		b1.append(x).append("x").append(y).append("y");
		return b1.toHashCode();
	}
	
	public int getHash3(int x, int y) {
		HashCodeBuilder b1 = new HashCodeBuilder(17,31);
		b1.append(x).append(0).append(0).append(y).append(0).append(0);
		return b1.toHashCode();
	}
	
	public void testHashingScheme() {
		int n = 10000;
		Set<Integer> hashes1 = new HashSet<Integer>();
		Set<Integer> hashes2 = new HashSet<Integer>();
		Set<Integer> hashes3 = new HashSet<Integer>();
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				hashes1.add(getHash1(i,j));
				hashes2.add(getHash2(i,j));
				hashes3.add(getHash3(i,j));
			}
		}
		System.out.println("1 N: " + n*n + ", " + hashes1.size());
		System.out.println("2 N: " + n*n + ", " + hashes2.size());
		System.out.println("3 N: " + n*n + ", " + hashes3.size());
	}
	
	//@Test
	public void testSimpleHashFactoryLargeStateIdentifierDependent() {
		SADomain domain = (SADomain)this.gridWorldTest.getDomain();
		State startState = this.generateLargeGW(domain, 100);
		HashableStateFactory factory = new SimpleHashableStateFactory();
		Set<HashableState> hashedStates = this.getReachableHashedStates(startState, domain, factory);
		int size = hashedStates.size();
		Set<Integer> hashes = new HashSet<Integer>();
		for (HashableState hs : hashedStates) {
			hashes.add(hs.hashCode());
		}
		System.err.println("Hashed states: " + hashedStates.size() + ", hashes: " + hashes.size());
		if (hashedStates.size() != hashes.size()) {
			System.err.println("Hashed states: " + hashedStates.size() + ", hashes: " + hashes.size());
		}
		
		Set<HashableState> renamedStates = new HashSet<HashableState>();
		for (HashableState state : hashedStates) {
			State source = state.getSourceState();
			State renamed = this.renameObjects(source.copy());
			HashableState renamedHashed = factory.hashState(renamed);
			renamedStates.add(renamedHashed);
		}
		hashedStates.addAll(renamedStates);
		assert(hashedStates.size() == size * 2);
		
	}
	
	public Set<HashableState> generateStates(SADomain domain, State state, HashableStateFactory factory, int width) {
		Set<HashableState> hashedStates = new HashSet<HashableState>();
		for (int i = 0; i < width; ++i) {
			for (int j =0 ; j < width; ++j) {
				State copy = state.copy();
				copy.getObjectsOfClass(GridWorldDomain.CLASSAGENT).get(0).setValue(GridWorldDomain.ATTX, i);
				copy.getObjectsOfClass(GridWorldDomain.CLASSAGENT).get(0).setValue(GridWorldDomain.ATTY, j);
				hashedStates.add(factory.hashState(copy));
			}
		}
		return hashedStates;
	}
	
	public Set<HashableState> generateRandomStates(SADomain domain, State state, HashableStateFactory factory, int width, int numStates, boolean moveLocations) {
		Set<HashableState> hashedStates = new HashSet<HashableState>();
		Random random = new Random();
		int misses = 0;
		int prevSize = 0;
		while (hashedStates.size() < numStates) {
			if (hashedStates.size() == prevSize) {
				misses++;
			}
			if (misses > 100) {
				break;
			}
			prevSize = hashedStates.size();
			if (prevSize > 0 && prevSize % 10000 == 0) {
				System.out.println("\t" + prevSize);
			}
			State copy = state.copy();
			copy.getObjectsOfClass(GridWorldDomain.CLASSAGENT).get(0).setValue(GridWorldDomain.ATTX, random.nextInt(width));
			copy.getObjectsOfClass(GridWorldDomain.CLASSAGENT).get(0).setValue(GridWorldDomain.ATTY, random.nextInt(width));
			
			if (moveLocations) {
				for (ObjectInstance loc : copy.getObjectsOfClass(GridWorldDomain.CLASSLOCATION)){
					loc.setValue(GridWorldDomain.ATTX, random.nextInt(width));
					loc.setValue(GridWorldDomain.ATTY, random.nextInt(width));
				}
			}
			hashedStates.add(factory.hashState(copy));
		}
		return hashedStates;
	}
	
	public State generateLargeGW(SADomain domain, int width) {
		State state = GridWorldDomain.getOneAgentNLocationState(domain, width);
		
		for (int i = 0; i < width; i++) {
			GridWorldDomain.setLocation(state, i, i, width - 1 - i);
		}
		return state;
	}
	
	public State renameObjects(State s) {
		SecureRandom random = new SecureRandom();
		for (ObjectInstance obj : s.getAllObjects()) {
			String newName = new BigInteger(130, random).toString(32);
			s = s.renameObject(obj, newName);
		}
		return s;
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
