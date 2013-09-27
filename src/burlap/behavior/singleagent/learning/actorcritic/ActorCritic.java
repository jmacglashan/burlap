package burlap.behavior.singleagent.learning.actorcritic;

import java.util.LinkedList;
import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


public class ActorCritic extends OOMDPPlanner implements LearningAgent {

	protected Actor													actor;
	protected Critic												critic;
	
	protected int													maxEpisodeSize = Integer.MAX_VALUE;
	
	protected int													numEpisodesForPlanning;
	
	protected LinkedList<EpisodeAnalysis>							episodeHistory;
	protected int													numEpisodesToStore;
	
	public ActorCritic(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, Actor actor, Critic critic) {
		this.actor = actor;
		this.critic = critic;
		numEpisodesForPlanning = 1;
		this.episodeHistory = new LinkedList<EpisodeAnalysis>();
		numEpisodesToStore = 1;
		this.PlannerInit(domain, rf, tf, gamma, null);
	}
	
	
	
	public ActorCritic(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, Actor actor, Critic critic, int maxEpisodeSize) {
		this.actor = actor;
		this.critic = critic;
		this.maxEpisodeSize = maxEpisodeSize;
		numEpisodesForPlanning = 1;
		this.episodeHistory = new LinkedList<EpisodeAnalysis>();
		numEpisodesToStore = 1;
		this.PlannerInit(domain, rf, tf, gamma, null);
	}
	
	
	
	@Override
	public void addNonDomainReferencedAction(Action a){
		super.addNonDomainReferencedAction(a);
		this.actor.addNonDomainReferencedAction(a);
		this.critic.addNonDomainReferencedAction(a);
		
	}

	@Override
	public EpisodeAnalysis runLearningEpisodeFrom(State initialState) {
		
		EpisodeAnalysis ea = new EpisodeAnalysis(initialState);
		
		State curState = initialState;
		
		this.critic.initializeEpisode(curState);
		
		int timeSteps = 0;
		while(!tf.isTerminal(curState) && timeSteps < this.maxEpisodeSize){
			
			GroundedAction ga = this.actor.getAction(curState);
			State nextState = ga.executeIn(curState);
			double r = this.rf.reward(curState, ga, nextState);
			
			ea.recordTransitionTo(nextState, ga, r);
			
			CritiqueResult critqiue = this.critic.critiqueAndUpdate(curState, ga, nextState);
			this.actor.updateFromCritqique(critqiue);
			
			curState = nextState;
			timeSteps++;
			
		}
		
		this.critic.endEpisode();
		
		if(episodeHistory.size() >= numEpisodesToStore){
			episodeHistory.poll();
		}
		episodeHistory.offer(ea);
		
		return ea;
	}

	@Override
	public EpisodeAnalysis getLastLearningEpisode() {
		return episodeHistory.getLast();
	}

	@Override
	public void setNumEpisodesToStore(int numEps) {
		this.numEpisodesToStore = numEps;
	}

	@Override
	public List<EpisodeAnalysis> getAllStoredLearningEpisodes() {
		return this.episodeHistory;
	}

	@Override
	public void planFromState(State initialState) {
		for(int i = 0; i < numEpisodesForPlanning; i++){
			this.runLearningEpisodeFrom(initialState);
		}
	}
	
	public Policy getPolicy(){
		return this.actor;
	}

}
