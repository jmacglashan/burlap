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


/**
 * This is a general class structure for implementing Actor-critic learning. The kind of actor critic learning performed
 * can be modified by swapping out different {@link Actor} and {@link Critic} objects. The general structure of the 
 * learning algorithm is for the {@link Actor} class to be queried for an action given the current state of the world.
 * That action is taken and a resulting state is observed. The {@link Critic} is then asked to critique this behavior
 * which is returned in a {@link CritqueResult} object and then passed along to the {@link Actor} so that the actor may
 * update is behavior accordingly.
 * 
 * @author James MacGlashan
 *
 */
public class ActorCritic extends OOMDPPlanner implements LearningAgent {

	
	/**
	 * The actor component to use.
	 */
	protected Actor													actor;
	
	/**
	 * The critic component to use
	 */
	protected Critic												critic;
	
	/**
	 * The maximum number of steps of an episode before the agent will manually terminate it.This is defaulted
	 * to Integer.MAX_VALUE.
	 */
	protected int													maxEpisodeSize = Integer.MAX_VALUE;
	
	/**
	 * The number of simulated learning episodes to use when the {@link planFromState(State)} method is called.
	 */
	protected int													numEpisodesForPlanning;
	
	
	/**
	 * The saved and most recent learning episodes this agent has performed. 
	 */
	protected LinkedList<EpisodeAnalysis>							episodeHistory;
	
	/**
	 * The number of most recent learning episodes to store.
	 */
	protected int													numEpisodesToStore;
	
	
	
	/**
	 * Initializes the learning algorithm.
	 * @param domain the domain in which to learn
	 * @param rf the reward function to use
	 * @param tf the terminal state function to use
	 * @param gamma the discount factor
	 * @param actor the actor component to use to select actions
	 * @param critic the critic component to use to critique 
	 */
	public ActorCritic(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, Actor actor, Critic critic) {
		this.actor = actor;
		this.critic = critic;
		numEpisodesForPlanning = 1;
		this.episodeHistory = new LinkedList<EpisodeAnalysis>();
		numEpisodesToStore = 1;
		this.plannerInit(domain, rf, tf, gamma, null);
	}
	
	
	/**
	 * Initializes the learning algorithm.
	 * @param domain the domain in which to learn
	 * @param rf the reward function to use
	 * @param tf the terminal state function to use
	 * @param gamma the discount factor
	 * @param actor the actor component to use to select actions
	 * @param critic the critic component to use to critique 
	 * @param maxEpisodeSize the maximum number of steps the agent will take in a learning episode before the agent gives up.
	 */
	public ActorCritic(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, Actor actor, Critic critic, int maxEpisodeSize) {
		this.actor = actor;
		this.critic = critic;
		this.maxEpisodeSize = maxEpisodeSize;
		numEpisodesForPlanning = 1;
		this.episodeHistory = new LinkedList<EpisodeAnalysis>();
		numEpisodesToStore = 1;
		this.plannerInit(domain, rf, tf, gamma, null);
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
	
	
	/**
	 * Returns the policy/actor of this learning algorithm. Note that all {@link Actor} objects are also
	 * Policy objects.
	 * @return the policy/actor of this learning algorithm.
	 */
	public Policy getPolicy(){
		return this.actor;
	}

}
