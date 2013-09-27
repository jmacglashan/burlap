package burlap.oomdp.stocashticgames.tournament;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.stocashticgames.Agent;
import burlap.oomdp.stocashticgames.AgentFactory;
import burlap.oomdp.stocashticgames.World;
import burlap.oomdp.stocashticgames.WorldGenerator;


public class Tournament {

	protected List<AgentFactory>			agents;
	protected List <Double>					tournamentCumulatedReward;
	
	protected int							maxStages;
	protected int							numGames;
	
	protected MatchSelector					selector;
	protected WorldGenerator				worldGenerator;
	
	
	
	protected int							debugId = 25633;
	
	
	
	public Tournament(int maxStages, MatchSelector selector, WorldGenerator worldGenerator){
		agents = new ArrayList<AgentFactory>();
		tournamentCumulatedReward = new ArrayList<Double>();
		this.maxStages = maxStages;
		this.selector = selector;
		this.worldGenerator = worldGenerator;
		
		this.numGames = 0;
		
	}
	
	public Tournament(int maxStages, int numGames, MatchSelector selector, WorldGenerator worldGenerator){
		agents = new ArrayList<AgentFactory>();
		tournamentCumulatedReward = new ArrayList<Double>();
		this.maxStages = maxStages;
		this.selector = selector;
		this.worldGenerator = worldGenerator;
		
		this.numGames = numGames;
		
	}
	
	public Tournament(List <AgentFactory> agents, int maxStages, MatchSelector selector, WorldGenerator worldGenerator){
		this.agents = agents;
		tournamentCumulatedReward = new ArrayList<Double>(this.agents.size());
		for(int i = 0; i < agents.size(); i++){
			tournamentCumulatedReward.add(0.);
		}
		
		this.maxStages = maxStages;
		this.selector = selector;
		this.worldGenerator = worldGenerator;
		
		this.numGames = 0;
	}
	
	public Tournament(List <AgentFactory> agents, int maxStages, int numGames, MatchSelector selector, WorldGenerator worldGenerator){
		this.agents = agents;
		tournamentCumulatedReward = new ArrayList<Double>(this.agents.size());
		for(int i = 0; i < agents.size(); i++){
			tournamentCumulatedReward.add(0.);
		}
		
		this.maxStages = maxStages;
		this.selector = selector;
		this.worldGenerator = worldGenerator;
		
		this.numGames = numGames;;
	}
	
	
	public void addAgent(AgentFactory agent){
		this.agents.add(agent);
		this.tournamentCumulatedReward.add(0.);
	}
	
	public int getNumAgents(){
		return agents.size();
	}
	
	
	public double getCumulativeRewardFor(int i){
		return tournamentCumulatedReward.get(i);
	}
	
	public void resetTournamentReward(){
		for(int i = 0; i < tournamentCumulatedReward.size(); i++){
			tournamentCumulatedReward.set(i, 0.);
		}
	}
	
	public void printOutResults(){
		for(int i = 0; i < agents.size(); i++){
			System.out.println(i + ": " + tournamentCumulatedReward.get(i));
		}
	}
	
	
	public void runTournament(){
		
		selector.resetMatchSelections();
		
		List<MatchEntry> match = null;
		while((match = selector.getNextMatch()) != null){
			
			World w = worldGenerator.generateWorld();
			
			//shuffle entrants
			Collections.shuffle(match, RandomFactory.getMapped(0));
			
			Map<String, Integer> agentNameToId = new HashMap<String, Integer>();
			
			//have the matched agents join the world
			for(MatchEntry me : match){
				Agent a = agents.get(me.agentId).generateAgent();
				a.joinWorld(w, me.agentType);
				agentNameToId.put(a.getAgentName(), me.agentId);
				DPrint.c(debugId, me.agentId + " ");
			}
			DPrint.cl(debugId, "");
			
			//run the game
			for(int i = 0; i < this.numGames; i++){
				w.runGame(maxStages);
			}
			
			//record results
			for(String aname : agentNameToId.keySet()){
				int aId = agentNameToId.get(aname);
				double gameCumR = w.getCumulativeRewardForAgent(aname);
				double tournCumR = tournamentCumulatedReward.get(aId);
				tournamentCumulatedReward.set(aId, gameCumR+tournCumR);
			}
			
		}
		
	}

}
