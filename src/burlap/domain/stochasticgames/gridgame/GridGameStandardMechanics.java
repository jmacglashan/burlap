package burlap.domain.stochasticgames.gridgame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.JointActionModel;


public class GridGameStandardMechanics extends JointActionModel {

	Random 						rand;
	Domain						domain;
	double						pMoveThroughSWall;
	
	
	public GridGameStandardMechanics(Domain d){
		rand = RandomFactory.getMapped(0);
		domain = d;
		pMoveThroughSWall = 0.5;
	}
	
	@Override
	public List<TransitionProbability> transitionProbsFor(State s, JointAction ja) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void actionHelper(State s, JointAction ja) {
		
		List <GroundedSingleAction> gsas = ja.getActionList();
		
		//need to force no movement when trying to enter space of a noop agent
		List <Location2> previousLocations = new ArrayList<GridGameStandardMechanics.Location2>();
		List <Location2> noopLocations = new ArrayList<GridGameStandardMechanics.Location2>();
		
		for(GroundedSingleAction gsa : gsas){
			Location2 loc = this.getLocation(s, gsa.actingAgent);
			previousLocations.add(loc);
			if(gsa.action.actionName.equals(GridGame.ACTIONNOOP)){
				noopLocations.add(loc);
			}
		}
		
		List <Location2> basicMoveResults = new ArrayList<GridGameStandardMechanics.Location2>();
		for(int i = 0; i < ja.size(); i++){
			Location2 loc = previousLocations.get(i);
			GroundedSingleAction gsa = gsas.get(i);
			basicMoveResults.add(this.sampleBasicMovement(s, loc, this.attemptedDelta(gsa.action.actionName), noopLocations));
		}
		
		//resolve swaps
		basicMoveResults = this.resolvePositionSwaps(previousLocations, basicMoveResults);
		
		List <Location2> finalPositions = this.resolveCollisions(previousLocations, basicMoveResults);
		for(int i = 0; i < finalPositions.size(); i++){
			GroundedSingleAction gsa = gsas.get(i);
			Location2 loc = finalPositions.get(i);
			
			ObjectInstance agent = s.getObject(gsa.actingAgent);
			agent.setValue(GridGame.ATTX, loc.x);
			agent.setValue(GridGame.ATTY, loc.y);
			
		}

	}
	
	
	protected Location2 getLocation(State s, String agentName){
		
		ObjectInstance a = s.getObject(agentName);
		Location2 loc = new Location2(a.getDiscValForAttribute(GridGame.ATTX), a.getDiscValForAttribute(GridGame.ATTY));
		
		return loc;
	}
	
	
	protected Location2 attemptedDelta(String actionName){
		
		if(actionName.equals(GridGame.ACTIONNORTH)){
			return new Location2(0, 1);
		}
		else if(actionName.equals(GridGame.ACTIONSOUTH)){
			return new Location2(0, -1);
		}
		else if(actionName.equals(GridGame.ACTIONEAST)){
			return new Location2(1, 0);
		}
		else if(actionName.equals(GridGame.ACTIONWEST)){
			return new Location2(-1, 0);
		}
		else if(actionName.equals(GridGame.ACTIONNOOP)){
			return new Location2(0, 0);
		}
		
		return null; //unknown action...
	}
	
	
	protected List<Location2> resolvePositionSwaps(List <Location2> originalPositions, List<Location2> desiredPositions){
		
		List<Location2> resolvedPositions = new ArrayList<GridGameStandardMechanics.Location2>(desiredPositions);
		
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
					break;
				}
				
			}
		}
		
		//make sure there are no other problems resulting from this
		Map <Integer, List <Integer>> collissionSets = this.getColissionSets(resolvedPositions);
		while(collissionSets.size() > 0){
			for(Integer aid : collissionSets.keySet()){
				resolvedPositions.set(aid, originalPositions.get(aid));
			}
			collissionSets = this.getColissionSets(resolvedPositions);
		}
		
		return resolvedPositions;
		
	}
	
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
	
	protected Location2 sampleBasicMovement(State s, Location2 p0, Location2 delta, List <Location2> agentNoOpLocs){
		
		Location2 p1 = p0.add(delta);
		
		boolean reset = false;
		
		for(Location2 anl : agentNoOpLocs){
			if(p1.equals(anl)){
				reset = true;
				break;
			}
		}
		
		if(delta.x != 0 && !reset){
			reset = this.sampleWallCollision(p0, delta, s.getObjectsOfTrueClass(GridGame.CLASSDIMVWALL), true);
		}
		
		if(delta.y != 0 && !reset){
			reset = this.sampleWallCollision(p0, delta, s.getObjectsOfTrueClass(GridGame.CLASSDIMHWALL), false);
		}
		
		
		if(reset){
			p1 = p1.subtract(delta);
		}
		
		return p1;
	}
	
	
	protected boolean sampleWallCollision(Location2 p0, Location2 delta, List <ObjectInstance> walls, boolean vertical){
		
		for(int i = 0; i < walls.size(); i++){
			ObjectInstance w = walls.get(i);
			if(this.crossesWall(p0, delta, w, vertical)){
				int wt = w.getDiscValForAttribute(GridGame.ATTWT);
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
		
		int wp = w.getDiscValForAttribute(GridGame.ATTP);
		int we1 = w.getDiscValForAttribute(GridGame.ATTE1);
		int we2 = w.getDiscValForAttribute(GridGame.ATTE2);

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
	
	class Location2{
		
		public int x;
		public int y;
		
		public Location2(int x, int y){
			this.x = x;
			this.y = y;
		}
		
		public Location2(Location2 l){
			this.x = l.x;
			this.y = l.y;
		}
		
		public Location2 add(Location2 o){
			return new Location2(x+o.x, y+o.y);
		}
		
		public Location2 subtract(Location2 o){
			return new Location2(x-o.x, y-o.y);
		}
		
		
		@Override
		public boolean equals(Object o){
			if(!(o instanceof Location2)){
				return false;
			}
			
			Location2 ol = (Location2)o;
			
			return x == ol.x && y == ol.y;
			
		}
		
	}

}
