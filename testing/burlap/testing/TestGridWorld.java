package burlap.testing;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public class TestGridWorld {
	Domain domain;
	GridWorldDomain gw;
	
	@Before
	public void setup() {
		this.gw = new GridWorldDomain(11,11);
		gw.setMapToFourRooms();
		gw.setProbSucceedTransitionDynamics(1.0);
		this.domain = gw.generateDomain(); //generate the grid world domain

		
	}
	
	@Test
	public void testGridWorld() {
		//setup initial state
		State s = GridWorldDomain.getOneAgentOneLocationState(domain);
		GridWorldDomain.setAgent(s, 0, 0);
		GridWorldDomain.setLocation(s, 0, 10, 10);
		
		
		Action northAction = domain.getAction(GridWorldDomain.ACTIONNORTH);
		Action eastAction = domain.getAction(GridWorldDomain.ACTIONEAST);
		Action southAction = domain.getAction(GridWorldDomain.ACTIONSOUTH);
		Action westAction = domain.getAction(GridWorldDomain.ACTIONWEST);
		
		List<GroundedAction> northActions = northAction.getAllApplicableGroundedActions(s);
		Assert.assertEquals(1, northActions.size());
		
		List<GroundedAction> eastActions = eastAction.getAllApplicableGroundedActions(s);
		Assert.assertEquals(1, eastActions.size());
		
		List<GroundedAction> southActions = southAction.getAllApplicableGroundedActions(s);
		Assert.assertEquals(1, southActions.size());
		
		List<GroundedAction> westActions = westAction.getAllApplicableGroundedActions(s);
		Assert.assertEquals(1, westActions.size());
		
		GroundedAction north = northActions.get(0);
		GroundedAction south = southActions.get(0);
		GroundedAction east = eastActions.get(0);
		GroundedAction west = westActions.get(0);
		
		// AtLocation, WallNorth, WallSouth, WallEast, WallWest
		this.assertPFs(s, new boolean[] {false, false, true, false, true});
		s = north.executeIn(s);
		this.assertPFs(s, new boolean[] {false, false, false, false, true});
		s = east.executeIn(s);
		this.assertPFs(s, new boolean[] {false, false, false, false, false});
		s = north.executeIn(s);
		s = north.executeIn(s);
		s = north.executeIn(s);
		s = north.executeIn(s);
		this.assertPFs(s, new boolean[] {false, false, false, true, true});
		s = north.executeIn(s);
		s = east.executeIn(s);
		s = east.executeIn(s);
		s = east.executeIn(s);
		this.assertPFs(s, new boolean[] {false, false, true, true, false});
		s = north.executeIn(s);
		s = north.executeIn(s);
		s = east.executeIn(s);
		this.assertPFs(s, new boolean[] {false, true, true, false, false});
		s = east.executeIn(s);
		s = north.executeIn(s);
		s = north.executeIn(s);
		this.assertPFs(s, new boolean[] {false, true, false, false, true});
		s = east.executeIn(s);
		s = south.executeIn(s);
		s = north.executeIn(s);
		s = west.executeIn(s);
		this.assertPFs(s, new boolean[] {false, true, false, false, true});
		s = east.executeIn(s);
		s = east.executeIn(s);
		s = east.executeIn(s);
		s = east.executeIn(s);
		this.assertPFs(s, new boolean[] {true, true, false, true, false});
		
	}
	
	@After
	public void teardown() {
		this.domain = null;
		this.gw = null;
	}

	public void assertPFs(State s, boolean[] expectedValues) {
		PropositionalFunction atLocation = domain.getPropFunction(GridWorldDomain.PFATLOCATION);
		List<GroundedProp> gpAt = atLocation.getAllGroundedPropsForState(s);
		Assert.assertEquals(1, gpAt.size());
		Assert.assertEquals(expectedValues[0], gpAt.get(0).isTrue(s));
		
		PropositionalFunction pfWallNorth = domain.getPropFunction(GridWorldDomain.PFWALLNORTH);
		List<GroundedProp> gpWallNorth = pfWallNorth.getAllGroundedPropsForState(s);
		Assert.assertEquals(1, gpWallNorth.size());
		Assert.assertEquals(expectedValues[1], gpWallNorth.get(0).isTrue(s));
		
		
		PropositionalFunction pfWallSouth = domain.getPropFunction(GridWorldDomain.PFWALLSOUTH);
		List<GroundedProp> gpWallSouth = pfWallSouth.getAllGroundedPropsForState(s);
		Assert.assertEquals(1, gpWallSouth.size());
		Assert.assertEquals(expectedValues[2], gpWallSouth.get(0).isTrue(s));
		
		
		PropositionalFunction pfWallEast = domain.getPropFunction(GridWorldDomain.PFWALLEAST);
		List<GroundedProp> gpWallEast = pfWallEast.getAllGroundedPropsForState(s);
		Assert.assertEquals(1, gpWallEast.size());
		Assert.assertEquals(expectedValues[3], gpWallEast.get(0).isTrue(s));
		
		
		PropositionalFunction pfWallWest = domain.getPropFunction(GridWorldDomain.PFWALLWEST);
		List<GroundedProp> gpWallWest = pfWallWest.getAllGroundedPropsForState(s);
		Assert.assertEquals(1, gpWallWest.size());
		Assert.assertEquals(expectedValues[4], gpWallWest.get(0).isTrue(s));
		

	}
	
}
