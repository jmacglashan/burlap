package burlap.domain.stochasticgames.gridgame;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TransitionProbability;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.oo.state.generic.GenericOOState;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.JointActionModel;
import burlap.mdp.stochasticgames.agentactions.SGAgentAction;

import java.util.*;


/**
 * This class defines the standard transition dynamics for a grid game. By default, the dynamics set a 0.5 probability of agents being able to
 * move through semi-walls. The semi-wall pass through probability, however, may be changed by a parameter to a constructor.
 * If two agents adjacent agents try to move into each others locations, no one will move. If an agent tries
 * to move into a cell that the other agent is leaving, then they both will move. If two agents try to move into the same location,
 * then only one of them will randomly make it, while the other will be unmoved in their position.
 * @author James MacGlashan
 *
 */
public class GridGameStandardMechanics extends JointActionModel {

	Random 						rand;
	Domain						domain;
	double						pMoveThroughSWall;
	
	
	/**
	 * Initializes the mechanics for the given domain and sets the semi-wall pass through probability to 0.5;
	 * @param d the domain object
	 */
	public GridGameStandardMechanics(Domain d){
		rand = RandomFactory.getMapped(0);
		domain = d;
		pMoveThroughSWall = 0.5;
	}
	
	
	/**
	 * Initializes the mechanics for the given domain and sets the semi-wall pass through probability to semiWallPassThroughProb.
	 * @param d d the domain object
	 * @param semiWallPassThroughProb the probability that an agent will pass through a semi-wall.
	 */
	public GridGameStandardMechanics(Domain d, double semiWallPassThroughProb){
		rand = RandomFactory.getMapped(0);
		domain = d;
		pMoveThroughSWall = semiWallPassThroughProb;
	}
	
	@Override
	public List<TransitionProbability> transitionProbsFor(State s, JointAction ja) {
		
		List <TransitionProbability> tps = new ArrayList<TransitionProbability>();
		
		List <SGAgentAction> gsas = ja.getActionList();
		
		//need to force no movement when trying to enter space of a noop agent
		List <Location2> previousLocations = new ArrayList<GridGameStandardMechanics.Location2>();
		List <Location2> noopLocations = new ArrayList<GridGameStandardMechanics.Location2>();
		
		for(SGAgentAction gsa : gsas){
			Location2 loc = this.getLocation((OOState)s, gsa.actingAgent);
			previousLocations.add(loc);
			if(gsa.action.actionName.equals(GridGame.ACTION_NOOP)){
				noopLocations.add(loc);
			}
		}
		
		List <List<Location2Prob>> possibleOutcomes = new ArrayList<List<Location2Prob>>();
		for(int i = 0; i < ja.size(); i++){
			Location2 loc = previousLocations.get(i);
			SGAgentAction gsa = gsas.get(i);
			possibleOutcomes.add(this.getPossibleLocationsFromWallCollisions((OOState)s, loc, this.attemptedDelta(gsa.action.actionName), noopLocations));
		}
		
		List <LocationSetProb> outcomeSets = this.getAllLocationSets(possibleOutcomes);
		
		for(LocationSetProb sp : outcomeSets){
			
			//resolve collisions from attempted swaps, which is deterministic and does not need to be recursed
			List <Location2> basicMoveResults = this.resolvePositionSwaps(previousLocations, sp.locs);
			
			//finally, we need to find all stochastic outcomes from cell competition
			List <LocationSetProb> cOutcomeSets = this.getPossibleCollisionOutcomes(previousLocations, basicMoveResults);
			
			//turn them into states with probabilities
			for(LocationSetProb csp : cOutcomeSets){

				GenericOOState ns = (GenericOOState)s.copy();
				for(int i = 0; i < csp.locs.size(); i++){
					SGAgentAction gsa = gsas.get(i);
					Location2 loc = csp.locs.get(i);
					
					ObjectInstance agent = ns.touch(gsa.actingAgent);
					((MutableState)agent).set(GridGame.VAR_X, loc.x);
					((MutableState)agent).set(GridGame.VAR_Y, loc.y);
				}
				
				double totalProb = sp.p * csp.p;
				TransitionProbability tp = new TransitionProbability(ns, totalProb);
				tps.add(tp);
				
			}
			
			
		}
		
		
		return this.combineDuplicateTransitionProbabilities(tps);
	}

	@Override
	protected State actionHelper(State s, JointAction ja) {
		
		
		List <SGAgentAction> gsas = ja.getActionList();
		
		//need to force no movement when trying to enter space of a noop agent
		List <Location2> previousLocations = new ArrayList<GridGameStandardMechanics.Location2>();
		List <Location2> noopLocations = new ArrayList<GridGameStandardMechanics.Location2>();
		
		for(SGAgentAction gsa : gsas){
			Location2 loc = this.getLocation((OOState)s, gsa.actingAgent);
			previousLocations.add(loc);
			if(gsa.action.actionName.equals(GridGame.ACTION_NOOP)){
				noopLocations.add(loc);
			}
		}
		
		List <Location2> basicMoveResults = new ArrayList<GridGameStandardMechanics.Location2>();
		for(int i = 0; i < ja.size(); i++){
			Location2 loc = previousLocations.get(i);
			SGAgentAction gsa = gsas.get(i);
			basicMoveResults.add(this.sampleBasicMovement((OOState)s, loc, this.attemptedDelta(gsa.action.actionName), noopLocations));
		}
		
		//resolve swaps
		basicMoveResults = this.resolvePositionSwaps(previousLocations, basicMoveResults);
		
		List <Location2> finalPositions = this.resolveCollisions(previousLocations, basicMoveResults);
		for(int i = 0; i < finalPositions.size(); i++){
			SGAgentAction gsa = gsas.get(i);
			Location2 loc = finalPositions.get(i);
			
			ObjectInstance agent = ((GenericOOState)s).touch(gsa.actingAgent);
			((MutableState)agent).set(GridGame.VAR_X, loc.x);
			((MutableState)agent).set(GridGame.VAR_Y, loc.y);
			
		}
		
		return s;

	}
	
	
	/**
	 * Returns the x-y position of an agent stored in a Location2 object.
	 * @param s the state in which the agent exists
	 * @param agentName the name of the agent.
	 * @return a {@link GridGameStandardMechanics.Location2} object containing the agents position in the world.
	 */
	protected Location2 getLocation(OOState s, String agentName){
		
		ObjectInstance a = s.object(agentName);
		Location2 loc = new Location2((Integer)a.get(GridGame.VAR_X), (Integer)a.get(GridGame.VAR_Y));
		
		return loc;
	}
	
	
	/**
	 * Returns the attempted change in position by the agent for the given action. For instance, if the action is north,
	 * it would result in an attempted position change of (0, +1).
	 * @param actionName the action taken.
	 * @return the attempted change in position for the given action.
	 */
	protected Location2 attemptedDelta(String actionName){
		
		if(actionName.equals(GridGame.ACTION_NORTH)){
			return new Location2(0, 1);
		}
		else if(actionName.equals(GridGame.ACTION_SOUTH)){
			return new Location2(0, -1);
		}
		else if(actionName.equals(GridGame.ACTION_EAST)){
			return new Location2(1, 0);
		}
		else if(actionName.equals(GridGame.ACTION_WEST)){
			return new Location2(-1, 0);
		}
		else if(actionName.equals(GridGame.ACTION_NOOP)){
			return new Location2(0, 0);
		}
		
		throw new RuntimeException("Error: Unknown action named '" + actionName + "' that GridGameStandardMechanics cannot handle");
	}
	
	
	
	/**
	 * Returns the position of each agent after accounting for collisions that are a result of agents
	 * trying to move into each others previous locations.
	 * @param originalPositions the original position of the agents before their actions were taken.
	 * @param desiredPositions the new position the agents are trying to go into
	 * @return the positions of the agents accounting for collisions.
	 */
	protected List<Location2> resolvePositionSwaps(List <Location2> originalPositions, List<Location2> desiredPositions){
		
		List<Location2> resolvedPositions = new ArrayList<GridGameStandardMechanics.Location2>(desiredPositions);
		List <Location2> newNoopPositions = new ArrayList<GridGameStandardMechanics.Location2>();
		
		for(int i = 0; i < originalPositions.size(); i++){
			Location2 a1op = originalPositions.get(i);
			Location2 a1dp = resolvedPositions.get(i);
			for(int j = i+1; j < resolvedPositions.size(); j++){
				Location2 a2op = originalPositions.get(j);
				Location2 a2dp = resolvedPositions.get(j);
				if(a1op.equals(a2dp) && a1dp.equals(a2op)){
					//swap collision!
					resolvedPositions.set(i, new Location2(a1op));
					resolvedPositions.set(j, new Location2(a2op));
					newNoopPositions.add(a1op);
					newNoopPositions.add(a2op);
					
					break;
				}
				
			}
		}
		
		
		if(!newNoopPositions.isEmpty()){
			return this.backupNoOps(originalPositions, resolvedPositions, newNoopPositions);
		}
		
		return resolvedPositions;
		
	}
	
	
	
	/**
	 * Backups position changes by agents which can no longer move to their desired location. That is, if agent a wanted to move
	 * to cell z, but failed for some reason (e.g., direct collision with another agent), then z would be added to the noops list
	 * and this method would back up the effect of a's ability to change position to any agents that wanted to move into
	 * a's position.
	 * @param originalPositions the original position of agents in the previous state
	 * @param desiredPositions the desired position where the agents want to go
	 * @param noops the locations in which agents have been forced to stay from other events
	 * @return the new resolved locations of all agents
	 */
	List <Location2> backupNoOps(List <Location2> originalPositions, List<Location2> desiredPositions, List <Location2> noops){
		
		List <Location2> resolved = new ArrayList<GridGameStandardMechanics.Location2>(desiredPositions);
		
		boolean needsUpdating = true;
		while(needsUpdating){
			needsUpdating = false;
			for(int i = 0; i < resolved.size(); i++){
				Location2 dl = resolved.get(i);
				Location2 ol = originalPositions.get(i);
				if(!dl.equals(ol) && noops.contains(dl)){
					resolved.set(i, ol);
					noops.add(ol);
					needsUpdating = true;
				}
			}
		}
		
		return resolved;
	}
	
	
	/**
	 * Resolves collisions that occur when two or more agents try to enter the same cell, in which case only one
	 * agent will make it into the position and the rest will stay in place
	 * @param originalPositions the positions of the agents in the original state before their actions were taken
	 * @param desiredPositions the desired locations of the agents
	 * @return a list of the resulting positions having accounted for collisions.
	 */
	protected List<Location2> resolveCollisions(List<Location2> originalPositions, List <Location2> desiredPositions){
		
		//get movement collisions
		Map <Integer, List <Integer>> collissionSets = this.getColissionSets(desiredPositions);
		
		if(collissionSets.size() == 0){
			return desiredPositions; //no resolutions needed
		}
		
		
		//resolve attempted movement collisions
		List <Location2> finalPoses = new ArrayList<GridGameStandardMechanics.Location2>();
		Map <Integer, Integer> winners = this.getWinningAgentMovements(collissionSets);
		for(int i = 0; i < originalPositions.size(); i++){
			if(winners.containsKey(i)){
				if(winners.get(i) != i){
					//this player lost and stays in place
					finalPoses.add(originalPositions.get(i));
				}
				else{
					//this player wins and gets to go to desired location
					finalPoses.add(desiredPositions.get(i));
				}
			}
			else{
				//no competitors so the agent goes where it wants
				finalPoses.add(desiredPositions.get(i));
			}
		}
		
		//it's possible that a losing collision means the agent's spot is no longer available, causing another collision
		//in this case all agents affected by loser are pushed back
		collissionSets = this.getColissionSets(finalPoses);
		while(collissionSets.size() > 0){
			
			Set <Integer> pushedBackAgents = collissionSets.keySet();
			for(Integer aid : pushedBackAgents){
				finalPoses.set(aid, originalPositions.get(aid));
			}
			
			collissionSets = this.getColissionSets(finalPoses);
			
		}
		
		
		return finalPoses;
	}
	
	
	protected List <LocationSetProb> getPossibleCollisionOutcomes(List<Location2> originalPositions, List <Location2> desiredPositions){
		
		//get movement collisions
		Map <Integer, List <Integer>> collissionSets = this.getColissionSets(desiredPositions);
		
		if(collissionSets.size() == 0){
			//then this is trivially just the set of desired positions
			List <LocationSetProb> outcomes = new ArrayList<GridGameStandardMechanics.LocationSetProb>(1);
			outcomes.add(new LocationSetProb(new ArrayList<GridGameStandardMechanics.Location2>(desiredPositions), 1.));
			return outcomes;
			
		}
		
		List <LocationSetProb> allOutcomes = new ArrayList<GridGameStandardMechanics.LocationSetProb>();
		
		List <Map <Integer, Integer>> winnerAssignments = this.getAllPossibleCollisionWinnerAssignment(collissionSets);
		double p = 1. / (double)winnerAssignments.size();
		for(Map <Integer, Integer> winners : winnerAssignments){
			List <Location2> finalPoses = new ArrayList<GridGameStandardMechanics.Location2>();
			
			for(int i = 0; i < originalPositions.size(); i++){
				if(winners.containsKey(i)){
					if(winners.get(i) != i){
						//this player lost and stays in place
						finalPoses.add(originalPositions.get(i));
					}
					else{
						//this player wins and gets to go to desired location
						finalPoses.add(desiredPositions.get(i));
					}
				}
				else{
					//no competitors so the agent goes where it wants
					finalPoses.add(desiredPositions.get(i));
				}
			}
			
			//it's possible that a losing collision means the agent's spot is no longer available, causing another collision
			//in this case all agents affected by loser are pushed back
			collissionSets = this.getColissionSets(finalPoses);
			while(collissionSets.size() > 0){
				
				Set <Integer> pushedBackAgents = collissionSets.keySet();
				for(Integer aid : pushedBackAgents){
					finalPoses.set(aid, originalPositions.get(aid));
				}
				
				collissionSets = this.getColissionSets(finalPoses);
				
			}
			
			LocationSetProb lsp = new LocationSetProb(finalPoses, p);
			allOutcomes.add(lsp);
			
		}
		
		
		return allOutcomes;
		
	}
	
	/**
	 * Takes as input the set of collisions and randomly selects a winner
	 * @param collissionSets the set of collisions; maps from one agent index to a list of the agents with whom he is competing for a cell
	 * @return A map from each agent involved in a collision to the winning agent of that collision.
	 */
	protected Map <Integer, Integer> getWinningAgentMovements(Map <Integer, List <Integer>> collissionSets){
		
		Map <Integer, Integer> winners = new HashMap<Integer, Integer>();
		
		Set <Integer> keySet = collissionSets.keySet();
		for(Integer agentId : keySet){
			if(winners.containsKey(agentId)){
				continue; //already resolved winner
			}
			List <Integer> competitors = collissionSets.get(agentId);
			int winner = competitors.get(rand.nextInt(competitors.size()));
			for(Integer a2 : competitors){
				winners.put(a2, winner);
			}
		}
		
		
		return winners;
		
	}
	
	
	/**
	 * Returns with whom each agent is in a collision competition for a cell.
	 * @param candidatePositions a list of the positions to which each agent would like to go; the Location2 in index 1 indicates the location the 1th index agent
	 * wants to go to.
	 * @return A map from the index of each agent to a list of the index of agents with whom there is competition for a cell
	 */
	protected Map <Integer, List <Integer>> getColissionSets(List <Location2> candidatePositions){
		
		Map <Integer, List <Integer>> collisionSets = new HashMap<Integer, List<Integer>>();
		
		for(int i = 0; i < candidatePositions.size(); i++){
			
			Location2 candLoc = candidatePositions.get(i);
			List <Integer> collisions = new ArrayList<Integer>();
			collisions.add(i);
			for(int j = i+1; j < candidatePositions.size(); j++){
				if(collisionSets.containsKey(j)){
					continue; //already found collisions with this agent
				}
				Location2 cLoc = candidatePositions.get(j);
				if(candLoc.equals(cLoc)){
					collisions.add(j);
				}
			}
			
			if(collisions.size() > 1){ //greater than one because an agent always "collides" with itself
				//set the collision set for each agent involved
				for(Integer aid : collisions){
					collisionSets.put(aid, collisions);
				}
			}
		}
		
		
		return collisionSets;
		
	}
	
	
	/**
	 * Returns a movement result of the agent. If the agent tries to pass through a semi-wall, then it is randomly
	 * selected whether the agent succeeds or not. If the agent tries to move through a solid wall or to a location
	 * where there is another agent who is not moving, then no change occurs.
	 * @param s the state containing the agent
	 * @param p0 the initial position of the agent
	 * @param delta the desired change of position.
	 * @param agentNoOpLocs the locations occupied by agents who are not moving.
	 * @return The resulting location of this agents movement.
	 */
	protected Location2 sampleBasicMovement(OOState s, Location2 p0, Location2 delta, List <Location2> agentNoOpLocs){
		
		Location2 p1 = p0.add(delta);
		
		boolean reset = false;
		
		for(Location2 anl : agentNoOpLocs){
			if(p1.equals(anl)){
				reset = true;
				break;
			}
		}
		
		if(delta.x != 0 && !reset){
			reset = this.sampleWallCollision(p0, delta, s.objectsOfClass(GridGame.CLASS_DIM_V_WALL), true);
		}
		
		if(delta.y != 0 && !reset){
			reset = this.sampleWallCollision(p0, delta, s.objectsOfClass(GridGame.CLASS_DIM_H_WALL), false);
		}
		
		
		if(reset){
			p1 = p1.subtract(delta);
		}
		
		return p1;
	}
	
	
	
	/**
	 * Returns the list of possible outcome locations for a given start point and desired position change.
	 * @param s the state in which the changes would occur
	 * @param p0 the initial location
	 * @param delta the desired change in position
	 * @param agentNoOpLocs the locations occupied by agents who are not moving
	 * @return the list of possible outcome locations in which the agent could wind up
	 */
	protected List <Location2Prob> getPossibleLocationsFromWallCollisions(OOState s, Location2 p0, Location2 delta, List <Location2> agentNoOpLocs){
		
		List <Location2Prob> locs = new ArrayList<GridGameStandardMechanics.Location2Prob>(2);
		
		Location2 p1 = p0.add(delta);
		for(Location2 anl : agentNoOpLocs){
			if(p1.equals(anl)){
				//definitely cannot move to desired position
				p1 = p1.subtract(delta);
				locs.add(new Location2Prob(p1, 1.));
				return locs;
			}
		}
		
		if(delta.x != 0){
			int wc = this.wallCollision(p0, delta, s.objectsOfClass(GridGame.CLASS_DIM_V_WALL), true);
			if(wc == 0){
				locs.add(new Location2Prob(p1, 1.)); //agent freely moves
			}
			else if(wc == 1){
				p1 = p1.subtract(delta);
				locs.add(new Location2Prob(p1, 1.)); //agent certainly cannot move
			}
			else{
				//agent moves with some probability
				locs.add(new Location2Prob(p1, this.pMoveThroughSWall));
				locs.add(new Location2Prob(p1.subtract(delta), 1.-this.pMoveThroughSWall));
			}
		}
		else if(delta.y != 0){
			int wc = this.wallCollision(p0, delta, s.objectsOfClass(GridGame.CLASS_DIM_H_WALL), false);
			if(wc == 0){
				locs.add(new Location2Prob(p1, 1.)); //agent freely moves
			}
			else if(wc == 1){
				p1 = p1.subtract(delta);
				locs.add(new Location2Prob(p1, 1.)); //agent certainly cannot move
			}
			else{
				//agent moves with some probability
				locs.add(new Location2Prob(p1, this.pMoveThroughSWall));
				locs.add(new Location2Prob(p1.subtract(delta), 1.-this.pMoveThroughSWall));
			}
		}
		else{
			//agent is not trying to change location
			locs.add(new Location2Prob(p1, 1.));
		}
		
		
		
		return locs;
		
	}
	
	
	
	/**
	 * Return true if the agent is able to move in the desired location; false if the agent moves into a solid wall
	 * or if the agent randomly fails to move through a semi-wall that is in the way.
	 * @param p0 the initial location of the agent
	 * @param delta the desired change in direction of the agent
	 * @param walls the list of walls in the world
	 * @param vertical whether the list of provided walls are vertical or horizontal walls
	 * @return true if the agent is able to move in the desired location; false otherwise
	 */
	protected boolean sampleWallCollision(Location2 p0, Location2 delta, List <ObjectInstance> walls, boolean vertical){
		
		for(int i = 0; i < walls.size(); i++){
			ObjectInstance w = walls.get(i);
			if(this.crossesWall(p0, delta, w, vertical)){
				int wt = (Integer)w.get(GridGame.VAR_WT);
				if(wt == 0){ //solid wall
					return true;
				}
				else if(wt == 1){ //stochastic wall
					double roll = rand.nextDouble();
					if(roll > pMoveThroughSWall){
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	
	
	/**
	 * Indicates whether there are any wall collisions.
	 * @param p0 the original location
	 * @param delta the desired change in direction
	 * @param walls possible wall objects
	 * @param vertical true if the wall objects are vertical; false if they are horizontal
	 * @return 0 if there is no collision with a wall, 1 if there is a collision with a solid wall, 2 if there is a potential collision with a semi-wall
	 */
	protected int wallCollision(Location2 p0, Location2 delta, List <ObjectInstance> walls, boolean vertical){
		
		for(int i = 0; i < walls.size(); i++){
			ObjectInstance w = walls.get(i);
			if(this.crossesWall(p0, delta, w, vertical)){
				int wt = (Integer)w.get(GridGame.VAR_WT);
				if(wt == 0){ //solid wall
					return 1;
				}
				else if(wt == 1){ //stochastic wall
					return 2;
				}
			}
		}
		
		return 0;
		
	}
	
	
	
	/**
	 * Returns true if the agent would cross a given wall instance given a movement attempt.
	 * @param p0 the initial state of the agent
	 * @param delta the desired change in direction
	 * @param w wall instance to check
	 * @param vertical true if the wall is a vertical wall; false if the wall is a horizontal wall
	 * @return whether the agent's movement would cross a wall
	 */
	protected boolean crossesWall(Location2 p0, Location2 delta, ObjectInstance w, boolean vertical){
		
		int a0, a1, d;
		if(vertical){
			a0 = p0.x;
			a1 = p0.y;
			d = delta.x;
		}
		else{
			a0 = p0.y;
			a1 = p0.x;
			d = delta.y;
		}
		
		int wp = (Integer)w.get(GridGame.VAR_POS);
		int we1 = (Integer)w.get(GridGame.VAR_E1);
		int we2 = (Integer)w.get(GridGame.VAR_E2);

		if(d < 0){
			//check crosses "before" agent if decreasing movement
			if(wp == a0){
				//wall to immediate left
				if(a1 >= we1 && a1 <= we2){
					//then we have a wall cross
					return true;
				}
			}
		}
		else if(d > 0){
			//check crosses after agent in increasing movement
			if(wp == a0+1){
				//wall to immediate right
				if(a1 >= we1 && a1 <= we2){
					//then we have a wall cross
					return true;
				}
			}
		}
		
		
		return false;
		
	}
	
	
	
	/**
	 * Takes a list of possible location outcomes for each agent and generates all joint location outcomes
	 * @param locOutcomes a list of possible location outcomes
	 * @return all joint location outcomes
	 */
	protected List<LocationSetProb> getAllLocationSets(List<List<Location2Prob>> locOutcomes){
		
		List <LocationSetProb> sets = new ArrayList<GridGameStandardMechanics.LocationSetProb>();
		Location2 [] locArray = new Location2[locOutcomes.size()];
		generateAllLocationSetsHelper(locOutcomes, 0, locArray, 1., sets);
		
		return sets;
	}
	
	
	/**
	 * This method will recursively generate all possible joint location outcomes for a list of possible outcomes for each agent 
	 * @param locOutcomes the list of possible location outcomes for each agent
	 * @param i the index from which to generate possible outcomes in a depth-first manner
	 * @param locArray an array holding the current assignments for a given depth
	 * @param stackedProb the probability of this location set occurring
	 * @param discovered the discovered combinations thus far
	 */
	protected void generateAllLocationSetsHelper(List<List<Location2Prob>> locOutcomes, int i, Location2 [] locArray, double stackedProb, List<LocationSetProb> discovered){
		
		if(i == locOutcomes.size()){
			//bundle them up
			List <Location2> locs = new ArrayList<GridGameStandardMechanics.Location2>(locArray.length);
			Collections.addAll(locs, locArray);
			LocationSetProb lsp = new LocationSetProb(locs, stackedProb);
			discovered.add(lsp);
			return ;
		}
		
		//otherwise we're in recursive step
		List<Location2Prob> individualOutcomes = locOutcomes.get(i);
		for(Location2Prob lp : individualOutcomes){
			locArray[i] = lp.l;
			this.generateAllLocationSetsHelper(locOutcomes, i+1, locArray, stackedProb*lp.p, discovered);
		}
		
	}
	
	
	
	
	protected List<Map <Integer, Integer>> getAllPossibleCollisionWinnerAssignment(Map <Integer, List <Integer>> collissionSets){
		
		
		//first get all the disjoint collision lists
		Set <Integer> collisionSetGeneratedFor = new HashSet<Integer>();
		List <List<Integer>> collisionLists = new ArrayList<List<Integer>>();
		for(Map.Entry<Integer, List<Integer>> e : collissionSets.entrySet()){
			if(collisionSetGeneratedFor.contains(e.getKey())){
				continue;
			}
			collisionLists.add(e.getValue());
		}
		
		
		//then get all possible assignments for them
		List <List <Integer>> winnerAssignments = new ArrayList<List<Integer>>();
		int [] assignment = new int[collisionLists.size()];
		this.generateAllPossibleCollisionWinnerAssignments(collisionLists, 0, assignment, winnerAssignments);
		
		
		//finally, package it up a list of map interfaces
		List<Map <Integer, Integer>> result = new ArrayList<Map<Integer,Integer>>(winnerAssignments.size());
		for(List <Integer> wa : winnerAssignments){
			Map<Integer, Integer> cmap = new HashMap<Integer, Integer>();
			for(int i = 0; i < wa.size(); i++){
				List <Integer> possibleWinners = collisionLists.get(i);
				int w = wa.get(i);
				for(int pw : possibleWinners){
					cmap.put(pw, w);
				}
			}
			result.add(cmap);
			
		}
		
		
		return result;
	}
	
	
	protected void generateAllPossibleCollisionWinnerAssignments(List <List<Integer>> collisionLists, int i, int [] assignment, List <List<Integer>> discovered){
		
		if(i == collisionLists.size()){
			//package it up
			List <Integer> assignmentList = new ArrayList<Integer>(assignment.length);
			for(int a : assignment){
				assignmentList.add(a);
			}
			discovered.add(assignmentList);
			return ;
		}
		
		//otherwise we're in recursive step
		List<Integer> possibleWinners = collisionLists.get(i);
		for(int w : possibleWinners){
			assignment[i] = w;
			this.generateAllPossibleCollisionWinnerAssignments(collisionLists, i+1, assignment, discovered);
		}
		
	}
	
	
	
	
	/**
	 * Iterates through a list of transition probability objects and combines any that refer to the same state
	 * @param srcTPs and initial list of transition probability objects
	 * @return an output list of transition probability objects in which any duplicate states have been mereged
	 */
	protected List <TransitionProbability> combineDuplicateTransitionProbabilities(List <TransitionProbability> srcTPs){
		
		double totalProb = 0.;
		List <TransitionProbability> result = new ArrayList<TransitionProbability>(srcTPs.size());
		Set <Integer> marked = new HashSet<Integer>();
		for(int i = 0; i < srcTPs.size(); i++){
			if(marked.contains(i)){
				continue;
			}
			TransitionProbability tp = srcTPs.get(i);
			double sumP = tp.p;
			for(int j = i+1; j < srcTPs.size(); j++){
				if(marked.contains(j)){
					continue;
				}
				TransitionProbability cmpTP = srcTPs.get(j);
				if(this.agentsEqual((OOState)tp.s, (OOState)cmpTP.s)){
					sumP += cmpTP.p;
					marked.add(j);
				}
			}
			result.add(new TransitionProbability(tp.s, sumP));
			totalProb += sumP;
		}
		
		if(Math.abs(1. - totalProb) > 0.000000000001){
			throw new RuntimeException("Error, transition probabilities do not sum to 1");
		}
		
		return result;
		
	}
	
	
	/**
	 * Returns true if the agent objects between these two states are equal
	 * @param s1 the first state
	 * @param s2 the second state
	 * @return true if the agent objects between these two states are equal
	 */
	protected boolean agentsEqual(OOState s1, OOState s2){
		
		List<ObjectInstance> agents1 = s1.objectsOfClass(GridGame.CLASS_AGENT);
		for(ObjectInstance a1 : agents1){
			ObjectInstance a2 = s2.object(a1.name());
			
			int x1 = (Integer)a1.get(GridGame.VAR_X);
			int x2 = (Integer)a2.get(GridGame.VAR_X);
			
			int y1 = (Integer)a1.get(GridGame.VAR_Y);
			int y2 = (Integer)a2.get(GridGame.VAR_Y);
			
			if(x1 != x2 || y1 != y2){
				return false;
			}
			
		}
		
		return true;
	}
	
	
	/**
	 * A class for storing 2 dimensional position information. Add and subtract operations are defined for it.
	 * @author James MacGlashan
	 *
	 */
	class Location2{
		
		/**
		 * The x position
		 */
		public int x;
		
		/**
		 * The y position
		 */
		public int y;
		
		
		/**
		 * Constructs with the given position
		 * @param x the x position
		 * @param y the y position
		 */
		public Location2(int x, int y){
			this.x = x;
			this.y = y;
		}
		
		/**
		 * Constructs a new instance from a previous {@link Location2} instance
		 * @param l the {@link Location2} instance to copy.
		 */
		public Location2(Location2 l){
			this.x = l.x;
			this.y = l.y;
		}
		
		
		/**
		 * Returns a new {@link Location2} object that is the sum of this object and the provided object. This objects values
		 * are not affected by this operation.
		 * @param o the other object whose values should be added.
		 * @return a new {@link Location2} object that is the sum of this object and the provided object.
		 */
		public Location2 add(Location2 o){
			return new Location2(x+o.x, y+o.y);
		}
		
		
		/**
		 * Returns a new {@link Location2} object that is the subtraction of a provided object from this object (this - o). This objects values
		 * are not affected by this operation.
		 * @param o the other object whose values should be subtract.
		 * @return a new {@link Location2} object that is the subtraction of a provided object from this object (this - o).
		 */
		public Location2 subtract(Location2 o){
			return new Location2(x-o.x, y-o.y);
		}
		
		
		
		@Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + x;
            result = prime * result + y;
            return result;
        }

		@Override
        public boolean equals(Object o){
            if(!(o instanceof Location2)){
                return false;
            }
            
            Location2 ol = (Location2)o;
            
            return x == ol.x && y == ol.y;
            
        }

        private GridGameStandardMechanics getOuterType() {
            return GridGameStandardMechanics.this;
        }
		
	}
	
	
	/**
	 * A class for holding a location and a probability associated with that location.
	 * @author James MacGlashan
	 *
	 */
	class Location2Prob{
		
		/**
		 * The location
		 */
		public Location2 l;
		
		/**
		 * The probability
		 */
		public double p;
		
		
		/**
		 * Initializes with the given location and probability
		 * @param l the location
		 * @param p the probability
		 */
		public Location2Prob(Location2 l, double p){
			this.l = l;
			this.p = p;
		}
		
	}
	
	
	/**
	 * A class for holding the joint probability for a particular set of location outcomes for each agent.
	 * @author James MacGlashan
	 *
	 */
	class LocationSetProb{
		
		/**
		 * The location outcomes for each agent
		 */
		public List <Location2> locs;
		
		/**
		 * The probability for this outcome sequence
		 */
		public double p;
		
		
		/**
		 * Initializes with a list of outcome locations for each agent and the probability of that joint outcome
		 * @param locs the location outcomes
		 * @param p the joint probability
		 */
		public LocationSetProb(List <Location2> locs, double p){
			this.locs = locs;
			this.p = p;
		}
		
	}

}
