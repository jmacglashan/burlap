package burlap.domain.stochasticgames.normalform;

import burlap.behavior.stochasticgames.solvers.GeneralBimatrixSolverTools;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.*;
import burlap.mdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.mdp.stochasticgames.agentactions.SimpleSGAgentAction;
import burlap.mdp.stochasticgames.common.StaticRepeatedGameActionModel;
import burlap.mdp.stochasticgames.explorers.SGTerminalExplorer;

import java.util.*;


/**
 * This stochastic game domain generator provides methods to create N-player single stage games. There is only one object class, a "player" object class, which has
 * one attribute, its player number. A state consists simply of an object instance for each player. Different players maybe have different numbers of available
 * actions and the actions available to each player may have different names. The SingleAction's are created such that a player can only execute a single
 * action if that action is available to that player. Therefore, when agents joint a world for one of these games, their 
 * {@link burlap.mdp.stochasticgames.SGAgentType} can be specified to have
 * all of the possible actions, because they will only be able to execute the relevant ones. The method {@link #getAgentTypeForAllPlayers(SGDomain)} will return
 * such an {@link burlap.mdp.stochasticgames.SGAgentType} class that can be used for all agents.
 * <p>
 * In addition to this generator being able to return the domain object, it may also be used to return the corresponding joint reward function. The method
 * {@link #getRepatedGameActionModel()} will return a joint action mode that always returns to the same state, which can be used for repeated game playing.
 * <p>
 * This class also provides static methods for returning generators for a number of classic bimatrix games: prisoner's dilemma, chicken, hawk dove,
 * battle of the sexes 1, battle of the sexes 2, matching pennies, and stag hunt.
 * <p>
 * This class also has a method for streamlining the world creation process so that repeated games (or single shot games) can be easily played
 * in the constructed game. For this use either the {@link #createRepeatedGameWorld(burlap.mdp.stochasticgames.SGAgent...)} or {@link #createRepeatedGameWorld(SGDomain, burlap.mdp.stochasticgames.SGAgent...)}
 * method. The former method will create an new domain instance using the {@link #generateDomain()} method; the latter will
 * use an already generated version of the domain that you provide to it.
 * <p>
 * Finally, this class's payout function definition (and other properties) can be modified without affecting previously
 * generated domains or payout functions, allowing it to be reused multiple times.
 * @author James MacGlashan
 *
 */
public class SingleStageNormalFormGame implements DomainGenerator {

	/**
	 * When this generator is constructed with a generic bimatrix or zero sum definition ({@link #SingleStageNormalFormGame(String[][], double[][][])} or
	 * {@link #SingleStageNormalFormGame(String[][])}, respectively), action names for each row/column
	 * will take the form of: DEFAULTBIMATRIXACTIONBASENAMEi where i is the row/column index. More specifically,
	 * it will be "actioni", since DEFAULTBIMATRIXACTIONBASENAME = "action"
	 */
	public static final String				DEFAULTBIMATRIXACTIONBASENAME = "action";
	
	/**
	 * variable key for player number
	 */
	public static final String VAR_PN = "playerNum";
	
	/**
	 * Class name for a player class
	 */
	public static final String CLASS_PLAYER = "player";
	
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
	 * A constructor for bimatrix games with specified action names.
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
			for(int j = 0; j < actionSets[0].length; j++){
				for(int k = 0; k < actionSets[1].length; k++){
					StrategyProfile sp = new StrategyProfile(j,k);
					this.payouts[i].set(sp, twoPlayerPayoutMatrix[i][j][k]);
				}
			}
		}
		
	}
	
	
	/**
	 * A construtor for a bimatrix game where the row player payoffs and colum player payoffs are provided in two different 2D double matrices. The action
	 * names for each row/column will be named "action0" ... "actionN" where N is the maximum number of rows/columns. 
	 * @param rowPayoff the payoff matrix for the row player
	 * @param colPayoff the payoff matrix for the column player
	 */
	public SingleStageNormalFormGame(double [][] rowPayoff, double [][] colPayoff){
		
		if(rowPayoff.length != colPayoff.length || rowPayoff[0].length != colPayoff[0].length){
			throw new RuntimeException("Payoff matrices for the row and column player are not the same dimensionality.");
		}
		
		this.nPlayers = 2;
		int nRows = rowPayoff.length;
		int nCols = rowPayoff[0].length;
		this.actionSets = new ArrayList<List<String>>();
		List <String> actionsP1 = new ArrayList<String>();
		for(int i = 0; i < nRows; i++){
			actionsP1.add(DEFAULTBIMATRIXACTIONBASENAME + i);
		}
		this.actionSets.add(actionsP1);
		
		List <String> actionsP2 = new ArrayList<String>();
		for(int i = 0; i < nCols; i++){
			actionsP2.add(DEFAULTBIMATRIXACTIONBASENAME + i);
		}
		this.actionSets.add(actionsP2);
		
		this.payouts = new AgentPayoutFunction[2];
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
		
		
		for(int i = 0; i < nRows; i++){
			for(int j = 0; j < nCols; j++){
				StrategyProfile sp = new StrategyProfile(i,j);
				this.payouts[0].set(sp, rowPayoff[i][j]);
				this.payouts[1].set(sp, colPayoff[i][j]);
			}
		}
		
		
	}
	
	/**
	 * A constructor for a bimatrix zero sum game. The action
	 * names for each row/column will be named "action0" ... "actionN" where N is the maximum number of rows/columns. 
	 * @param zeroSumRowPayoff the payoffs for the row player
	 */
	public SingleStageNormalFormGame(double [][] zeroSumRowPayoff){
		this(zeroSumRowPayoff, GeneralBimatrixSolverTools.getNegatedMatrix(zeroSumRowPayoff));
	}
	
	
	/**
	 * A constructor for games with a symmetric number of actions for each player. The payoffs will not be set.
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
	 * Sets the pay out that player number <code>playerNumber</code> receives for a given strategy profile
	 * @param playerNumber the index of the player whose payout should be specified (index starts at 0)
	 * @param payout the payout the <code>playerNumber</code>th receives
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
	 * Sets the pay out that player number <code>playerNumber</code> receives for a given strategy profile
	 * @param playerNumber the index of the player whose payout should be specified (index starts at 0)
	 * @param payout the payout the <code>playerNumber</code>th receives
	 * @param actions the strategy profile; array specifying the int index of the actions taken by each player, ordered by their player number
	 */
	public void setPayout(int playerNumber, double payout, int...actions){
		StrategyProfile profile = new StrategyProfile(actions);
		this.payouts[playerNumber].set(profile, payout);
	}
	
	
	/**
	 * Returns the name of the <code>an</code> action of player <code>pn</code>
	 * @param pn the player number
	 * @param an the action index
	 * @return the name of the <code>an</code> action of player <code>pn</code>
	 */
	public String actionName(int pn, int an){
		return this.actionSets.get(pn).get(an);
	}
	
	
	/**
	 * Returns the action index of the action named <code>actionName</code> of player <code>pn</code>
	 * @param pn the player number
	 * @param actionName the name of the action
	 * @return the action index of the action named <code>actionName</code> of player <code>pn</code>
	 */
	public int actionIndex(int pn, String actionName){
		return this.actionNameToIndex[pn].get(actionName);
	}
	
	
	/**
	 * Returns the payout that player <code>pn</code> receives for the given strategy profile.
	 * @param pn the player number
	 * @param actions the strategy profile specified as an array of action names, ordered by the player number of the player that took the action.
	 * @return the payout that player <code>pn</code> receives for the given strategy profile.
	 */
	public double getPayout(int pn, String...actions){
		int [] iprofile = new int[actions.length];
		for(int i = 0; i < actions.length; i++){
			iprofile[i] = this.actionNameToIndex[i].get(actions[i]);
		}
		return this.getPayout(pn, iprofile);
	}
	
	
	/**
	 * Returns the payout that player <code>pn</code> receives for the given strategy profile.
	 * @param pn the player number
	 * @param actions the strategy profile specified as an array of action indices, ordered by the player number of the player that took the action.
	 * @return the payout that player <code>pn</code> receives for the given strategy profile.
	 */
	public double getPayout(int pn, int...actions){
		StrategyProfile sp = new StrategyProfile(actions);
		return this.payouts[pn].getPayout(sp);
	}
	
	
	/**
	 * Returns the number of players in the domain to be generated
	 * @return the number of players in the domain to be generated
	 */
	public int getNumPlayers(){
		return this.nPlayers;
	}
	
	
	@Override
	public Domain generateDomain() {
		
		SGDomain domain = new SGDomain();

		ActionNameMap [] cnames = ActionNameMap.deepCopyActionNameMapArray(this.actionNameToIndex);
		for(String aname : this.uniqueActionNames){
			new NFGAgentAction(domain, aname, cnames);
		}

		domain.setJointActionModel(new StaticRepeatedGameActionModel());
		
		return domain;
	}
	
	/**
	 * Creates a world instance for this game in which the provided agents join in the order they are passed.
	 * @param agents the agents to join the created world.
	 * @return a world instance with the provided agents having already joined.
	 */
	public World createRepeatedGameWorld(SGAgent...agents){
		
		SGDomain domain = (SGDomain)this.generateDomain();
		return this.createRepeatedGameWorld(domain, agents);
		
	}
	
	/**
	 * Creates a world instance for this game in which the provided agents join in the order they are passed. This object
	 * uses the provided domain instance generated from this object instead of generating a new one.
	 * @param agents the agents to join the created world.
	 * @return a world instance with the provided agents having already joined.
	 */
	public World createRepeatedGameWorld(SGDomain domain, SGAgent...agents){
		
		//grab the joint reward function from our bimatrix game in the more general BURLAP joint reward function interface
		JointReward jr = this.getJointRewardFunction(); 
		
		//game repeats forever unless manually stopped after T times.
		TerminalFunction tf = new NullTermination();
		
		//set up the initial state generator for the world, which for a bimatrix game is trivial
		SGStateGenerator sg = new NFGameState();
		
		//agent type defines the action set of players and OO-MDP class associated with their state information
		//in this case that's just their player number. We can use the same action type for all players, regardless of wether
		//each agent can play a different number of actions, because the actions have preconditions that prevent a player from taking actions
		//that don't belong to them.
		SGAgentType at = SingleStageNormalFormGame.getAgentTypeForAllPlayers(domain);
		
		
		//create a world to synchronize the actions of agents in this domain and record results
		World w = new World(domain, jr, tf, sg);
		
		for(SGAgent a : agents){
			a.joinWorld(w, at);
		}
		
		return w;
		
	}
	
	
	/**
	 * Returns a {@link burlap.mdp.stochasticgames.JointReward} function for this game.
	 * @return a {@link burlap.mdp.stochasticgames.JointReward} function for this game.
	 */
	public JointReward getJointRewardFunction(){
		return new SingleStageNormalFormJointReward(this.nPlayers, ActionNameMap.deepCopyActionNameMapArray(this.actionNameToIndex), AgentPayoutFunction.getDeepCopyOfPayoutArray(this.payouts));
	}
	
	
	/**
	 * Returns a hashable strategy profile object for a strategy profile specified by action names
	 * @param actions the strategy profile specified as an array of action names, ordered by the player number of the player that took the action.
	 * @return a hashable strategy profile.
	 */
	protected static StrategyProfile getStrategyProfile(ActionNameMap[] actionNameToIndex, String...actions){
		int [] iprofile = new int[actions.length];
		for(int i = 0; i < actions.length; i++){
			iprofile[i] = actionNameToIndex[i].get(actions[i]);
		}
		return new StrategyProfile(iprofile);
	}

	
	
	/**
	 * Returns an {@link burlap.mdp.stochasticgames.SGAgentType} object that can be used by agents being associated with any player number.
	 * This AgentType permits agents to use any action in the domain, but the action preconditions will prevent the agent from taking actions
	 * that its player number cannot take.
	 * @param domain the domain in which the the agents will be playing.
	 * @return an {@link burlap.mdp.stochasticgames.SGAgentType} object that can be used by agents being associated with any player number.
	 */
	public static SGAgentType getAgentTypeForAllPlayers(SGDomain domain){
		SGAgentType at = new SGAgentType("player", domain.getAgentActions());
		return at;
	}
	
	
	/**
	 * Returns a repeated game joint action model. This action model always returns to the same state.
	 * @return a repeated game joint action model.
	 */
	public static JointActionModel getRepatedGameActionModel(){
		return new StaticRepeatedGameActionModel();
	}
	
	
	/**
	 * Returns an instance of Prisoner's Dilemma, which is defined by:<p>
	 * (3,3); (0,5) <p>
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
	 * Returns an instance of Chicken, which is defined by:<p>
	 * (0,0); (-1,1) <p>
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
	 * Returns an instance of Hawk Dove, which is defined by:<p>
	 * (-1,-1); (2,0) <p>
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
	 * Returns an instance of Battle of the Sexes 1, which is defined by:<p>
	 * (3,2); (0,0) <p>
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
	 * Returns an instance of Battle of the Sexes 2, which is defined by:<p>
	 * (3,2); (1,1) <p>
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
	 * Returns an instance of Matching Pennies, which is defined by:<p>
	 * (1,-1); (-1,1) <p>
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
	 * Returns an instance of Stag Hunt, which is defined by:<p>
	 * (2,2); (0,1) <p>
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
	protected static class SingleStageNormalFormJointReward implements JointReward{

		int nPlayers;
		ActionNameMap [] actionNameToIndex;
		AgentPayoutFunction [] payouts;

		public SingleStageNormalFormJointReward(int nPlayers, ActionNameMap[] actionNameToIndex, AgentPayoutFunction[] payouts) {
			this.nPlayers = nPlayers;
			this.actionNameToIndex = actionNameToIndex;
			this.payouts = payouts;
		}

		@Override
		public Map<String, Double> reward(State s, JointAction ja, State sp) {

			NFGameState ns = (NFGameState)s;

			Map<String, Double> rewards = new HashMap<String, Double>();
			
			String [] profile = new String[this.nPlayers];
			for(GroundedSGAgentAction sa : ja){
				String name = sa.actingAgent;
				int pn = ns.playerIndex(name);
				profile[pn] = sa.action.actionName;
			}
			
			StrategyProfile stprofile = SingleStageNormalFormGame.getStrategyProfile(this.actionNameToIndex, profile);
			for(GroundedSGAgentAction sa : ja){
				String name = sa.actingAgent;
				int pn = ns.playerIndex(name);
				rewards.put(name, this.payouts[pn].getPayout(stprofile));
			}
			
			return rewards;
		}
		
		
		
		
	}
	
	
	
	/**
	 * A SingleAction class that uses the parent domain generator to determine which agent can take which actions and enforces that in the preconditions.
	 * @author James MacGlashan
	 *
	 */
	protected static class NFGAgentAction extends SimpleSGAgentAction {

		ActionNameMap [] actionNameToIndex;

		public NFGAgentAction(SGDomain d, String name, ActionNameMap[] actionNameToIndex) {
			super(d, name);
			this.actionNameToIndex = actionNameToIndex;
		}

		@Override
		public boolean applicableInState(State s, GroundedSGAgentAction gsa) {

			NFGameState ns = (NFGameState)s;

			int pn = ns.playerIndex(gsa.actingAgent);
			
			if(this.actionNameToIndex[pn].containsKey(this.actionName)){
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

		public AgentPayoutFunction copy(){
			AgentPayoutFunction c = new AgentPayoutFunction();
			for(Map.Entry<StrategyProfile,Double> e : this.payout.entrySet()){
				c.set(e.getKey().copy(), e.getValue());
			}
			return c;
		}

		public static AgentPayoutFunction[] getDeepCopyOfPayoutArray(AgentPayoutFunction[] input){
			AgentPayoutFunction[] c = new AgentPayoutFunction[input.length];
			for(int i = 0; i < input.length; i++){
				c[i] = input[i].copy();
			}
			return c;
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
			StringBuilder buf = new StringBuilder(3*this.profile.length);
			buf.append(this.profile[0]);
			
			for(int i = 1; i < this.profile.length; i++){
				buf.append(" ").append(this.profile[i]);
			}
			
			return buf.toString();
		}

		public StrategyProfile copy(){
			return new StrategyProfile(this.profile);
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

		public ActionNameMap copy(){
			ActionNameMap c = new ActionNameMap();
			for(Map.Entry<String, Integer> e : this.namesToInd.entrySet()){
				c.put(e.getKey(), e.getValue());
			}

			return c;
		}

		public static ActionNameMap[] deepCopyActionNameMapArray(ActionNameMap [] input){
			ActionNameMap [] c = new ActionNameMap[input.length];
			for(int i = 0; i < input.length; i++){
				c[i] = input[i].copy();
			}
			return c;
		}
	}
	
	
	
	
	/**
	 * A main method showing example code that would be used to create an instance of Prisoner's dilemma and begin playing it with a 
	 * {@link burlap.mdp.stochasticgames.explorers.SGTerminalExplorer}.
	 * @param args
	 */
	public static void main(String [] args){
		
		SingleStageNormalFormGame game = SingleStageNormalFormGame.getPrisonersDilemma();
		SGDomain domain = (SGDomain)game.generateDomain();
		JointReward r = game.getJointRewardFunction();

		World w = new World(domain, r, new NullTermination(), (State)new NFGameState(2));

		SGTerminalExplorer exp = new SGTerminalExplorer(w);
		exp.explore();
		
	}
	
	

}
