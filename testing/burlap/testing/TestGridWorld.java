package burlap.testing;

import burlap.domain.singleagent.gridworld.state.GridAgent;
import burlap.domain.singleagent.gridworld.state.GridLocation;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.core.Domain;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.Action;
import burlap.mdp.singleagent.GroundedAction;
import burlap.mdp.singleagent.oo.OOSADomain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TestGridWorld {
	OOSADomain domain;
	GridWorldDomain gw;
	
	
	@Before
	public void setup() {
		this.gw = new GridWorldDomain(11,11);
		gw.setMapToFourRooms();
		gw.setProbSucceedTransitionDynamics(1.0);
		this.domain = gw.generateDomain(); //generate the grid world domain
		
	}
	public State generateState() {
		GridWorldState s = new GridWorldState(new GridAgent(0, 0), new GridLocation(10, 10, "location0"));
		return s;
	}
	
	public Domain getDomain() {
		return this.domain;
	}
	@Test
	public void testGridWorld() {
		State s = this.generateState();
		this.testGridWorld(s);
	}
	
	public void testGridWorld(State s) {
		Action northAction = domain.getAction(GridWorldDomain.ACTION_NORTH);
		Action eastAction = domain.getAction(GridWorldDomain.ACTION_EAST);
		Action southAction = domain.getAction(GridWorldDomain.ACTION_SOUTH);
		Action westAction = domain.getAction(GridWorldDomain.ACTION_WEST);
		
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
		PropositionalFunction atLocation = domain.getPropFunction(GridWorldDomain.PF_AT_LOCATION);
		List<GroundedProp> gpAt = atLocation.getAllGroundedPropsForState(s);
		Assert.assertEquals(1, gpAt.size());
		Assert.assertEquals(expectedValues[0], gpAt.get(0).isTrue((OOState)s));
		
		PropositionalFunction pfWallNorth = domain.getPropFunction(GridWorldDomain.PF_WALL_NORTH);
		List<GroundedProp> gpWallNorth = pfWallNorth.getAllGroundedPropsForState(s);
		Assert.assertEquals(1, gpWallNorth.size());
		Assert.assertEquals(expectedValues[1], gpWallNorth.get(0).isTrue((OOState)s));
		
		
		PropositionalFunction pfWallSouth = domain.getPropFunction(GridWorldDomain.PF_WALL_SOUTH);
		List<GroundedProp> gpWallSouth = pfWallSouth.getAllGroundedPropsForState(s);
		Assert.assertEquals(1, gpWallSouth.size());
		Assert.assertEquals(expectedValues[2], gpWallSouth.get(0).isTrue((OOState)s));
		
		
		PropositionalFunction pfWallEast = domain.getPropFunction(GridWorldDomain.PF_WALL_EAST);
		List<GroundedProp> gpWallEast = pfWallEast.getAllGroundedPropsForState(s);
		Assert.assertEquals(1, gpWallEast.size());
		Assert.assertEquals(expectedValues[3], gpWallEast.get(0).isTrue((OOState)s));
		
		
		PropositionalFunction pfWallWest = domain.getPropFunction(GridWorldDomain.PF_WALL_WEST);
		List<GroundedProp> gpWallWest = pfWallWest.getAllGroundedPropsForState(s);
		Assert.assertEquals(1, gpWallWest.size());
		Assert.assertEquals(expectedValues[4], gpWallWest.get(0).isTrue((OOState)s));
		

	}
	
}
