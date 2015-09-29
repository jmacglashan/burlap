package burlap.testing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import burlap.oomdp.core.states.ImmutableState;
import burlap.oomdp.core.states.State;

public class TestImmutableState {
	TestGridWorld gridWorldTest;
	@Before
	public void setup() {
		this.gridWorldTest = new TestGridWorld();
		this.gridWorldTest.setup();
	}
	
	@Test
	public void testGridWorld() {
		State s = this.gridWorldTest.generateState();
		this.gridWorldTest.testGridWorld(new ImmutableState(s));	
	}
	
	
	@After
	public void teardown() {
		this.gridWorldTest.teardown();
		this.gridWorldTest = null;
	}
}
