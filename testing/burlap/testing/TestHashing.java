package burlap.testing;

import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.state.GridAgent;
import burlap.domain.singleagent.gridworld.state.GridLocation;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.ActionType;
import burlap.mdp.singleagent.ActionUtils;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.SimpleHashableStateFactory;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

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
			State renamed = this.renameObjects((GridWorldState)source.copy());
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
		HashableStateFactory factory = new SimpleHashableStateFactory(false);
		Set<HashableState> hashedStates = this.getReachableHashedStates(startState, domain, factory);
		assert(hashedStates.size() == 104);
		
		Set<HashableState> renamedStates = new HashSet<HashableState>();
		for (HashableState state : hashedStates) {
			State source = state.getSourceState();
			State renamed = this.renameObjects((GridWorldState)source.copy());
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
	
	@Test
	public void testSimpleHashFactoryLargeStateIdentifierDependent() {
		SADomain domain = (SADomain)this.gridWorldTest.getDomain();
		State startState = this.generateLargeGW(domain, 100);
		HashableStateFactory factory = new SimpleHashableStateFactory(false);
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
			State renamed = this.renameObjects((GridWorldState)source.copy());
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
				GridWorldState copy = (GridWorldState)state.copy();
				copy.touchAgent().x = i;
				copy.agent.y = j;
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
			GridWorldState copy = (GridWorldState)state.copy();
			copy.touchAgent().x = random.nextInt(width);
			copy.agent.y = random.nextInt(width);

			
			if (moveLocations) {
				List<GridLocation> locations = copy.deepTouchLocations();
				for(GridLocation loc : locations){
					loc.x = random.nextInt(width);
					loc.y = random.nextInt(width);
				}
			}
			hashedStates.add(factory.hashState(copy));
		}
		return hashedStates;
	}
	
	public State generateLargeGW(SADomain domain, int width) {

		GridWorldState state = new GridWorldState(new GridAgent());

		for (int i = 0; i < width; i++) {
			state.locations.add(new GridLocation(i, width - 1 - i, "loc"+i));
		}
		return state;
	}
	
	public State renameObjects(GridWorldState s) {
		SecureRandom random = new SecureRandom();
		List<GridLocation> locations = s.deepTouchLocations();
		for (GridLocation obj : locations) {
			String newName = new BigInteger(130, random).toString(32);
			obj.setName(newName);
		}
		return s;
	}
	
	public Set <HashableState> getReachableHashedStates(State from, SADomain inDomain, HashableStateFactory usingHashFactory){
		
		Set<HashableState> hashedStates = new HashSet<HashableState>();
		HashableState shi = usingHashFactory.hashState(from);
		List <ActionType> actionTypes = inDomain.getActionTypes();
		
		LinkedList <HashableState> openList = new LinkedList<HashableState>();
		openList.offer(shi);
		hashedStates.add(shi);
		while(!openList.isEmpty()){
			HashableState sh = openList.poll();

			List<Action> gas = ActionUtils.allApplicableActionsForTypes(actionTypes, sh.s);
			for(Action ga : gas){
				List <TransitionProb> tps = ((FullModel)inDomain.getModel()).transitions(sh.s, ga);
				for(TransitionProb tp : tps){
					HashableState nsh = usingHashFactory.hashState(tp.eo.op);
					
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
