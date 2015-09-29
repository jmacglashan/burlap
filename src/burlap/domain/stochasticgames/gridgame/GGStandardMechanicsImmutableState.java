package burlap.domain.stochasticgames.gridgame;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.ImmutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;

public class GGStandardMechanicsImmutableState extends
		GridGameStandardMechanics {

	public GGStandardMechanicsImmutableState(Domain d) {
		super(d);
	}

	public GGStandardMechanicsImmutableState(Domain d,
			double semiWallPassThroughProb) {
		super(d, semiWallPassThroughProb);
	}
	
	@Override
	public List<TransitionProbability> transitionProbsFor(State s, JointAction ja) {
		
		List <TransitionProbability> tps = new ArrayList<TransitionProbability>();
		
		List <GroundedSGAgentAction> gsas = ja.getActionList();
		
		//need to force no movement when trying to enter space of a noop agent
		List <Location2> previousLocations = new ArrayList<GridGameStandardMechanics.Location2>();
		List <Location2> noopLocations = new ArrayList<GridGameStandardMechanics.Location2>();
		
		for(GroundedSGAgentAction gsa : gsas){
			Location2 loc = this.getLocation(s, gsa.actingAgent);
			previousLocations.add(loc);
			if(gsa.action.actionName.equals(GridGame.ACTIONNOOP)){
				noopLocations.add(loc);
			}
		}
		
		List <List<Location2Prob>> possibleOutcomes = new ArrayList<List<Location2Prob>>();
		for(int i = 0; i < ja.size(); i++){
			Location2 loc = previousLocations.get(i);
			GroundedSGAgentAction gsa = gsas.get(i);
			possibleOutcomes.add(this.getPossibleLocationsFromWallCollisions(s, loc, this.attemptedDelta(gsa.action.actionName), noopLocations));
		}
		
		List <LocationSetProb> outcomeSets = this.getAllLocationSets(possibleOutcomes);
		
		for(LocationSetProb sp : outcomeSets){
			
			//resolve collisions from attempted swaps, which is deterministic and does not need to be recursed
			List <Location2> basicMoveResults = this.resolvePositionSwaps(previousLocations, sp.locs);
			
			//finally, we need to find all stochastic outcomes from cell competition
			List <LocationSetProb> cOutcomeSets = this.getPossibleCollisionOutcomes(previousLocations, basicMoveResults);
			
			//turn them into states with probabilities
			if (cOutcomeSets.size() > 1) {
				System.out.print("");
			}
			for(LocationSetProb csp : cOutcomeSets){
				if (csp.toString().equals("1, 2 - 3, 1")) {
					System.out.print("");
				}
				ImmutableState ns = (ImmutableState)s;
				for(int i = 0; i < csp.locs.size(); i++){
					GroundedSGAgentAction gsa = gsas.get(i);
					Location2 loc = csp.locs.get(i);
					
					ObjectInstance agent = ns.getObject(gsa.actingAgent);
					ObjectInstance modAgent = agent.setValue(GridGame.ATTX, loc.x);
					modAgent = modAgent.setValue(GridGame.ATTY, loc.y);
					ns = ns.replaceObject(agent, modAgent);
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
		
		List <GroundedSGAgentAction> gsas = ja.getActionList();
		
		//need to force no movement when trying to enter space of a noop agent
		List <Location2> previousLocations = new ArrayList<GridGameStandardMechanics.Location2>();
		List <Location2> noopLocations = new ArrayList<GridGameStandardMechanics.Location2>();
		
		for(GroundedSGAgentAction gsa : gsas){
			Location2 loc = this.getLocation(s, gsa.actingAgent);
			previousLocations.add(loc);
			if(gsa.action.actionName.equals(GridGame.ACTIONNOOP)){
				noopLocations.add(loc);
			}
		}
		
		List <Location2> basicMoveResults = new ArrayList<GridGameStandardMechanics.Location2>();
		for(int i = 0; i < ja.size(); i++){
			Location2 loc = previousLocations.get(i);
			GroundedSGAgentAction gsa = gsas.get(i);
			basicMoveResults.add(this.sampleBasicMovement(s, loc, this.attemptedDelta(gsa.action.actionName), noopLocations));
		}
		
		//resolve swaps
		basicMoveResults = this.resolvePositionSwaps(previousLocations, basicMoveResults);
		
		List <Location2> finalPositions = this.resolveCollisions(previousLocations, basicMoveResults);
		ImmutableState modState = (ImmutableState)s;
		for(int i = 0; i < finalPositions.size(); i++){
			GroundedSGAgentAction gsa = gsas.get(i);
			Location2 loc = finalPositions.get(i);
			
			ObjectInstance agent = s.getObject(gsa.actingAgent);
			ObjectInstance modAgent = agent.setValue(GridGame.ATTX, loc.x);
			modAgent = modAgent.setValue(GridGame.ATTY, loc.y);
			modState = modState.replaceObject(agent, modAgent);
		}
		
		return modState;

	}

}
