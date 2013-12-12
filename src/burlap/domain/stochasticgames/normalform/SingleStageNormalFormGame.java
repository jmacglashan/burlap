package burlap.domain.stochasticgames.normalform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.AgentType;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.JointReward;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SingleAction;
import burlap.oomdp.stochasticgames.common.StaticRepeatedGameActionModel;
import burlap.oomdp.stochasticgames.explorers.SGTerminalExplorer;



/**
 * This stochastic game domain generator provides methods to create N-player single stage games. There is only one object class, a "player" object class, which has
 * one attribute, its player number. A state consists simply of an object instance for each player. Different players maybe have different numbers of available
 * actions and the actions available to each player may have different names. The SingleAction's are created such that a player can only execute a single
 * action if that action is available to that player. Therefore, when agents joint a world for one of these games, their 
 * {@link burlap.oomdp.stochasticgames.AgentType} can be specified to have
 * all of the possible actions, because they will only be able to execute the relevant ones. The method {@link getAgentTypeForAllPlayers(Domain)} will return
 * such an {@link burlap.oomdp.stochasticgames.AgentType} class that can be used for all agents.
 * <p/>
 * In addition to this generator being able to return the domain object, it may also be used to return the corresponding joint reward function. The method
 * {@link getRepatedGameActionModel()} will return a joint action mode that always returns to the same state, which can be used for repeated game playing.
 * <p/>
 * This class also provides static methods for returning generators for a number of classic bimatrix games: prisoner's dilemma, chicken, hawk dove,
 * battle of the sexes 1, battle of the sexes 2, matching pennies, and stag hunt.
 * @author James MacGlashan
 *
 */
public class SingleStageNormalFormGame implements DomainGenerator {

	/**
	 * Attribute name for player number
	 */
	public static final String				ATTPN = "playerNum";
	
	/**
	 * Class name for a player class
	 */
	public static final String				CLASSPLAYER = "player";
	
	/**
	 * Number of players
	 */
	protected int							nPlayers;
	
	/**
	 * The ordered action set for each player
	 */
	protected List<List<String>>			actionSets;
	
	/**
	 * The pay out function for each player. This each element of the array takes {@link StrategyProfile} objects
	 * as input to return the pay out for the player at the given index in the array.
	 */
	protected AgentPayoutFunction[]			payouts;
	
	/**
	 * Returns the the int index of an action for a given name for each player
	 */
	protected ActionNameMap[]				actionNameToIndex;
	
	/**
	 * The unique action names for the domain to be generated.
	 */
	protected Set<String>					uniqueActionNames;
	
	
	
	/**
	 * A constructor for bimatrix games.
	 * @param actionSets a 2x2 array of strings giving the names for each action. actionSets[0][1] returns the name of the second action for the first player.
	 * @param twoPlayerPayoutMatrix A 2x2x2 payout matrix. twoPlayerPayoutMatrix[0][0][1] gives the payout player 1 receives when player takes its first action and player two takes its second action.
	 */
	public SingleStageNormalFormGame(String [][] actionSets, double [][][] twoPlayerPayoutMatrix){
		
		this.nPlayers = actionSets.length;
		this.actionSets = new ArrayList<List<String>>();
		for(int i = 0; i < actionSets.length; i++){
			List <String> actions = new ArrayList<String>();
			this.actionSets.add(actions);
			for(int j = 0; j < actionSets[0].length; j++){
				actions.add(actionSets[i][j]);
			}
		}
		
		this.payouts = new AgentPayoutFunction[actionSets.length];
		for(int i = 0; i < payouts.length; i++){
			this.payouts[i] = new AgentPayoutFunction();
		}
		
		
		this.uniqueActionNames = new HashSet<String>();
		this.actionNameToIndex = new ActionNameMap[this.actionSets.size()];
		for(int i = 0; i < this.actionSets.size(); i++){
			this.actionNameToIndex[i] = new ActionNameMap();
			for(int j = 0; j < this.actionSets.get(i).size(); j++){
				this.actionNameToIndex[i].put(this.actionSets.get(i).get(j), j);
				this.uniqueActionNames.add(this.actionSets.get(i).get(j));
			}
		}
		
		for(int i = 0; i < this.nPlayers; i++){
			for(int j = 0; j < 2; j++){
				for(int k = 0; k < 2; k++){
					StrategyProfile sp = new StrategyProfile(j,k);
					this.payouts[i].set(sp, twoPlayerPayoutMatrix[i][j][k]);
				}
			}
		}
		
	}
	
	
	/**
	 * A constructor for games with a symmetric number of actions for each player.
	 * @param actionSets an nxm matrix specifying the m actions names for each of the n players. actionSets[i][j] specifies the jth action name for player i
	 */
	public SingleStageNormalFormGame(String [][] actionSets){
		this.nPlayers = actionSets.length;
		this.actionSets = new ArrayList<List<String>>();
		for(int i = 0; i < actionSets.length; i++){
			List <String> actions = new ArrayList<String>();
			this.actionSets.add(actions);
			for(int j = 0; j < actionSets[0].length; j++){
				actions.add(actionSets[i][j]);
			}
		}
		
		this.payouts = new AgentPayoutFunction[actionSets.length];
		for(int i = 0; i < payouts.length; i++){
			this.payouts[i] = new AgentPayoutFunction();
		}
		
		
		this.uniqueActionNames = new HashSet<String>();
		this.actionNameToIndex = new ActionNameMap[this.actionSets.size()];
		for(int i = 0; i < this.actionSets.size(); i++){
			this.actionNameToIndex[i] = new ActionNameMap();
			for(int j = 0; j < this.actionSets.get(i).size(); j++){
				this.actionNameToIndex[i].put(this.actionSets.get(i).get(j), j);
				this.uniqueActionNames.add(this.actionSets.get(i).get(j));
			}
		}
		
	}
	
	
	/**
	 * A constructor for games with an asymmetric number of actions for each player.
	 * @param actionSets the oredered list of actions for each player. actionSets.get(i).get(j) returns the name of the jth action for the ith player.
	 */
	public SingleStageNormalFormGame(List<List<String>> actionSets){
		
		this.nPlayers = actionSets.size();
		this.actionSets = new ArrayList<List<String>>(actionSets.size());
		for(List<String> actions : actionSets){
			List <String> nactions = new ArrayList<String>(actions);
			this.actionSets.add(nactions);
		}
		
		
		this.payouts = new AgentPayoutFunction[this.actionSets.size()];
		for(int i = 0; i < payouts.length; i++){
			this.payouts[i] = new AgentPayoutFunction();
		}
		
		
		this.uniqueActionNames = new HashSet<String>();
		this.actionNameToIndex = new ActionNameMap[this.actionSets.size()];
		for(int i = 0; i < this.actionSets.size(); i++){
			this.actionNameToIndex[i] = new ActionNameMap();
			for(int j = 0; j < this.actionSets.get(i).size(); j++){
				this.actionNameToIndex[i].put(this.actionSets.get(i).get(j), j);
				this.uniqueActionNames.add(this.actionSets.get(i).get(j));
			}
		}
		
	}
	
	
	/**
	 * Sets the pay out that player number {@link playerNumber} receives for a given strategy profile
	 * @param playerNumber the index of the player whose payout should be specified (index starts at 0)
	 * @param payout the payout the {@link playerNumber}th receives
	 * @param actions the strategy profile; array specifying the names of the actions taken by each player, ordered by their player number
	 */
	public void setPayout(int playerNumber, double payout, String...actions){
		int [] iprofile = new int[actions.length];
		for(int i = 0; i < actions.length; i++){
			iprofile[i] = this.actionNameToIndex[i].get(actions[i]);
		}
		this.setPayout(playerNumber, payout, iprofile);
	}
	
	/**
	 * Sets the pay out that player number {@link playerNumber} receives for a given strategy profile
	 * @param playerNumber the index of the player whose payout should be specified (index starts at 0)
	 * @param payout the payout the {@link playerNumber}th receives
	 * @param actions the strategy profile; array specifying the int index of the actions taken by each player, ordered by their player number
	 */
	public void setPayout(int playerNumber, double payout, int...actions){
		StrategyProfile profile = new StrategyProfile(actions);
		this.payouts[playerNumber].set(profile, payout);
	}
	
	
	/**
	 * Returns the name of the {@link an} action of player {@link pn}
	 * @param pn the player number
	 * @param an the action index
	 * @return the name of the {@link an} action of player {@link pn}
	 */
	public String actionName(int pn, int an){
		return this.actionSets.get(pn).get(an);
	}
	
	
	/**
	 * Returns the action index of the action named {@link actionName} of player {@link pn}
	 * @param pn the player number
	 * @param actionName the name of the action
	 * @return the action index of the action named {@link actionName} of player {@link pn}
	 */
	public int actionIndex(int pn, String actionName){
		return this.actionNameToIndex[pn].get(actionName);
	}
	
	
	/**
	 * Returns the payout that player {@link pn} receives for the given strategy profile.
	 * @param pn the player number
	 * @param actions the strategy profile specified as an array of action names, ordered by the player number of the player that took the action.
	 * @return the payout that player {@link pn} receives for the given strategy profile.
	 */
	public double getPayout(int pn, String...actions){
		int [] iprofile = new int[actions.length];
		for(int i = 0; i < actions.length; i++){
			iprofile[i] = this.actionNameToIndex[i].get(actions[i]);
		}
		return this.getPayout(pn, iprofile);
	}
	
	
	/**
	 * Returns the payout that player {@link pn} receives for the given strategy profile.
	 * @param pn the player number
	 * @param actions the strategy profile specified as an array of action indices, ordered by the player number of the player that took the action.
	 * @return the payout that player {@link pn} receives for the given strategy profile.
	 */
	public double getPayout(int pn, int...actions){
		StrategyProfile sp = new StrategyProfile(actions);
		return this.payouts[pn].getPayout(sp);
	}
	
	
	/**
	 * Returns the number of players in the domain to be generated
	 * @return
	 */
	public int getNumPlayers(){
		return this.nPlayers;
	}
	
	
	@Override
	public Domain generateDomain() {
		
		SGDomain domain = new SGDomain();
		
		Attribute att = new Attribute(domain, ATTPN, Attribute.AttributeType.DISC);
		att.setDiscValuesForRange(0, nPlayers-1, 1);
		
		ObjectClass player = new ObjectClass(domain, CLASSPLAYER);
		player.addAttribute(att);
		
		for(String aname : this.uniqueActionNames){
			NFGSingleAction sa = new NFGSingleAction(domain, aname);
		}
		
		return domain;
	}
	
	
	/**
	 * Returns a {@link burlap.oomdp.stochasticgames.JointReward} function for this game.
	 * @return a {@link burlap.oomdp.stochasticgames.JointReward} function for this game.
	 */
	public JointReward getJointRewardFunction(){
		return new SingleStageNormalFormJointReward();
	}
	
	
	/**
	 * Returns a hashable strategy profile object for a strategy profile specified by action names
	 * @param actions the strategy profile specified as an array of action names, ordered by the player number of the player that took the action.
	 * @return a hashable strategy profile.
	 */
	protected StrategyProfile getStrategyProfile(String...actions){
		int [] iprofile = new int[actions.length];
		for(int i = 0; i < actions.length; i++){
			iprofile[i] = this.actionNameToIndex[i].get(actions[i]);
		}
		return new StrategyProfile(iprofile);
	}
	
	
	/**
	 * Returns a state object for a domain object generated by a {@link SingleStageNormalFormGame} object. This method will examine
	 * the domain for the number of players by looking at the discrete attribute range of the playerNum attribute. It will then create
	 * a state with that many player object class instances, each with their playerNum attribute set accordingly.
	 * @param domain a domain object generated by a {@link SingleStageNormalFormGame} object
	 * @return a state object for a domain object generated by a {@link SingleStageNormalFormGame} object, with the appropriate number of player object instances.
	 */
	public static State getState(SGDomain domain){
		State s = new State();
		
		ObjectClass pclass = domain.getObjectClass(CLASSPLAYER);
		Attribute pnAtt = pclass.getAttribute(ATTPN);
		int n = pnAtt.discValues.size(); //determines the number of players
		
		for(int i = 0; i < n; i++){
			ObjectInstance player = new ObjectInstance(pclass, CLASSPLAYER+i);
			player.setValue(ATTPN, i);
			s.addObject(player);
		}
		
		return s;
	}
	
	
	/**
	 * Returns an {@link burlap.oomdp.stochasticgames.AgentType} object that can be used by agents being associated with any player number.
	 * This AgentType permits agents to use any action in the domain, but the action preconditions will prevent the agent from taking actions
	 * that its player number cannot take.
	 * @param domain the domain in which the the agents will be playing.
	 * @return an {@link burlap.oomdp.stochasticgames.AgentType} object that can be used by agents being associated with any player number.
	 */
	public static AgentType getAgentTypeForAllPlayers(SGDomain domain){
		AgentType at = new AgentType("player", domain.getObjectClass(CLASSPLAYER), domain.getSingleActions());
		return at;
	}
	
	
	/**
	 * Returns a repeated game joint action model. This action model always returns to the same state.
	 * @return
	 */
	public static JointActionModel getRepatedGameActionModel(){
		return new StaticRepeatedGameActionModel();
	}
	
	
	/**
	 * Returns an instance of Prisoner's Dilemma, which is defined by:<br/>
	 * (3,3); (0,5) <br/>
	 * (5,0); (1,1)
	 * @return and instance of Prisoner's Dilemma.
	 */
	public static SingleStageNormalFormGame getPrisonersDilemma(){
		SingleStageNormalFormGame gen = new SingleStageNormalFormGame(new String[][]{{"cooperate", "defect"},{"cooperate", "defect"}}, 
				new double[][][]{{{3, 0},
								  {5, 1}},
				
								  {{3, 5},
								  {0, 1}}
				});
		return gen;
	}
	
	
	/**
	 * Returns an instance of Chicken, which is defined by:<br/>
	 * (0,0); (-1,1) <br/>
	 * (1,-1); (-10,-10)
	 * @return and instance of Chicken.
	 */
	public static SingleStageNormalFormGame getChicken(){
		SingleStageNormalFormGame gen = new SingleStageNormalFormGame(new String[][]{{"turn", "stright"},{"turn", "stright"}}, 
				new double[][][]{{{0, -1},
								  {1, -10}},
				
								  {{0, 1},
								  {-1, -10}}
				});
		return gen;
	}
	
	
	/**
	 * Returns an instance of Hawk Dove, which is defined by:<br/>
	 * (-1,-1); (2,0) <br/>
	 * (0,2); (1,1)
	 * @return and instance of Hawk Dove.
	 */
	public static SingleStageNormalFormGame getHawkDove(){
		SingleStageNormalFormGame gen = new SingleStageNormalFormGame(new String[][]{{"hawk", "dove"},{"hawk", "dove"}}, 
				new double[][][]{{{-1, 2},
								  {0, 1}},
				
								  {{-1, 0},
								  {2, 1}}
				});
		return gen;
	}
	
	
	
	/**
	 * Returns an instance of Battle of the Sexes 1, which is defined by:<br/>
	 * (3,2); (0,0) <br/>
	 * (0,0); (2,3)
	 * @return and instance of Battle of the Sexes 1.
	 */
	public static SingleStageNormalFormGame getBattleOfTheSexes1(){
		SingleStageNormalFormGame gen = new SingleStageNormalFormGame(new String[][]{{"opera", "football"},{"opera", "football"}}, 
				new double[][][]{{{3, 0},
								  {0, 2}},
				
								  {{2, 0},
								  {0, 3}}
				});
		return gen;
	}
	
	
	/**
	 * Returns an instance of Battle of the Sexes 2, which is defined by:<br/>
	 * (3,2); (1,1) <br/>
	 * (0,0); (2,3)
	 * @return and instance of Battle of the Sexes 2.
	 */
	public static SingleStageNormalFormGame getBattleOfTheSexes2(){
		SingleStageNormalFormGame gen = new SingleStageNormalFormGame(new String[][]{{"opera", "football"},{"opera", "football"}}, 
				new double[][][]{{{3, 1},
								  {0, 2}},
				
								  {{2, 1},
								  {0, 3}}
				});
		return gen;
	}
	
	
	
	/**
	 * Returns an instance of Matching Pennies, which is defined by:<br/>
	 * (1,-1); (-1,1) <br/>
	 * (-1,1); (1,-1)
	 * @return and instance of Matching Pennies.
	 */
	public static SingleStageNormalFormGame getMatchingPennies(){
		SingleStageNormalFormGame gen = new SingleStageNormalFormGame(new String[][]{{"heads", "tails"},{"heads", "tails"}}, 
				new double[][][]{{{1, -1},
								  {-1, 1}},
				
								  {{-1, 1},
								  {1, -1}}
				});
		return gen;
	}
	
	
	/**
	 * Returns an instance of Stag Hunt, which is defined by:<br/>
	 * (2,2); (0,1) <br/>
	 * (1,0); (1,1)
	 * @return and instance of Stag Hunt.
	 */
	public static SingleStageNormalFormGame getStagHunt(){
		SingleStageNormalFormGame gen = new SingleStageNormalFormGame(new String[][]{{"stag", "hare"},{"stag", "hare"}}, 
				new double[][][]{{{2, 0},
								  {1, 1}},
				
								  {{2, 1},
								  {0, 1}}
				});
		return gen;
	}
	
	
	
	/**
	 * A Joint Reward Function class that uses the parent domain generators payout matrix to determine payouts for any given strategy profile.
	 * @author James MacGlashan
	 *
	 */
	protected class SingleStageNormalFormJointReward implements JointReward{

		@Override
		public Map<String, Double> reward(State s, JointAction ja, State sp) {
			
			Map<String, Double> rewards = new HashMap<String, Double>();
			
			String [] profile = new String[SingleStageNormalFormGame.this.nPlayers];
			for(GroundedSingleAction sa : ja){
				String name = sa.actingAgent;
				ObjectInstance player = s.getObject(name);
				int pn = player.getDiscValForAttribute(ATTPN);
				profile[pn] = sa.action.actionName;
			}
			
			StrategyProfile stprofile = SingleStageNormalFormGame.this.getStrategyProfile(profile);
			for(GroundedSingleAction sa : ja){
				String name = sa.actingAgent;
				ObjectInstance player = s.getObject(name);
				int pn = player.getDiscValForAttribute(ATTPN);
				rewards.put(name, SingleStageNormalFormGame.this.payouts[pn].getPayout(stprofile));
			}
			
			return rewards;
		}
		
		
		
		
	}
	
	
	
	/**
	 * A SingleAction class that uses the parent domain generator to determine which agent can take which actions and enforces that in the preconditions.
	 * @author James MacGlashan
	 *
	 */
	protected class NFGSingleAction extends SingleAction{

		public NFGSingleAction(SGDomain d, String name) {
			super(d, name);
		}

		@Override
		public boolean isApplicableInState(State s, String actingAgent, String[] params) {
			
			ObjectInstance a = s.getObject(actingAgent);
			int pn = a.getDiscValForAttribute(ATTPN);
			
			if(SingleStageNormalFormGame.this.actionNameToIndex[pn].containsKey(this.actionName)){
				return true;
			}
			
			return false;
		}
		
		
		
		
	}
	
	
	/**
	 * A class for defining a payout function for a single agent for each possible strategy profile.
	 * @author James MacGlashan
	 *
	 */
	public static class AgentPayoutFunction{
		
		Map<StrategyProfile, Double> payout;
		
		public AgentPayoutFunction(){
			this.payout = new HashMap<SingleStageNormalFormGame.StrategyProfile, Double>();
		}
		
		
		/**
		 * sets the payout for a given strategy profile
		 * @param profile the strategy profile
		 * @param p the payout returned for this strategy profile
		 */
		public void set(StrategyProfile profile, double p){
			this.payout.put(profile, p);
		}
		
		
		/**
		 * Returns the payout for a given strategy profile
		 * @param profile the strategy profile
		 * @return the payout for the given strategy profile.
		 */
		public double getPayout(StrategyProfile profile){
			Double P = this.payout.get(profile);
			if(P == null){
				throw new RuntimeException("Payout Function is not defined for this strategy profile: " + profile.toString());
			}
			
			return P;
		}
		
	}
	
	
	/**
	 * A strategy profile represented as an array of action indices that is hashable.
	 * @author James MacGlashan
	 *
	 */
	public static class StrategyProfile{
		
		int [] profile;
		int hashCode;
		
		public StrategyProfile(int...actions){
			this.profile = actions.clone();
			
			this.hashCode = 0;
			for(int a : this.profile){
				this.hashCode *= 23;
				this.hashCode += a;
			}
			
		}
		
		@Override
		public boolean equals(Object o){
			if(!(o instanceof StrategyProfile)){
				return false;
			}
			
			StrategyProfile so = (StrategyProfile)o;
			
			if(this.profile.length != so.profile.length){
				return false;
			}
			
			for(int i = 0; i < profile.length; i++){
				if(this.profile[i] != so.profile[i]){
					return false;
				}
			}
			
			return true;
		}
		
		@Override
		public int hashCode(){
			return this.hashCode;
		}
		
		@Override
		public String toString(){
			if(this.profile.length == 0){
				return "";
			}
			StringBuffer buf = new StringBuffer(3*this.profile.length);
			buf.append(this.profile[0]);
			
			for(int i = 1; i < this.profile.length; i++){
				buf.append(" ").append(this.profile[i]);
			}
			
			return buf.toString();
		}
		
	}
	
	
	/**
	 * A wrapper for a HashMap from strings to ints used to map action names to their action index.
	 * @author James MacGlashan
	 *
	 */
	protected static class ActionNameMap{
		Map<String, Integer> namesToInd;
		
		public ActionNameMap(){
			this.namesToInd = new HashMap<String, Integer>();
		}
		
		public void put(String name, int ind){
			this.namesToInd.put(name, ind);
		}
		
		public Integer get(String name){
			Integer i = this.namesToInd.get(name);
			if(i == null){
				throw new RuntimeException("No action named " + name + " for this player");
			}
			return i;
		}
		
		public boolean containsKey(String name){
			return this.namesToInd.containsKey(name);
		}
		
		public int size(){
			return this.namesToInd.size();
		}
	}
	
	
	
	
	/**
	 * A main method showing example code that would be used to create an instance of Prisoner's dilemma and begin playing it with a 
	 * {@link burlap.oomdp.stochasticgames.explorers.SGTerminalExplorer}.
	 * @param args
	 */
	public static void main(String [] args){
		
		SingleStageNormalFormGame game = SingleStageNormalFormGame.getPrisonersDilemma();
		SGDomain domain = (SGDomain)game.generateDomain();
		JointReward r = game.getJointRewardFunction();
		JointActionModel jam = SingleStageNormalFormGame.getRepatedGameActionModel();
		
		SGTerminalExplorer exp = new SGTerminalExplorer(domain, jam);
		
		//add short hand as first letter of each action name
		for(SingleAction sa : domain.getSingleActions()){
			exp.addActionShortHand(sa.actionName.substring(0, 1), sa.actionName);
		}
		
		
		exp.setTrackingRF(r);
		
		exp.exploreFromState(SingleStageNormalFormGame.getState(domain));
		
	}
	
	

}
