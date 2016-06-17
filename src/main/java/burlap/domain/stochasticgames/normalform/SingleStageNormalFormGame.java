package burlap.domain.stochasticgames.normalform;

import burlap.behavior.stochasticgames.solvers.GeneralBimatrixSolverTools;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.Action;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.ActionType;
import burlap.mdp.singleagent.action.UniversalActionType;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.SGStateGenerator;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.common.StaticRepeatedGameModel;
import burlap.mdp.stochasticgames.model.JointModel;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import burlap.mdp.stochasticgames.world.World;
import burlap.shell.SGWorldShell;

import java.util.*;


/**
 * This stochastic game domain generator provides methods to create N-player single stage games. The method
 * {@link #generateAgentType(int)} will generate the agent type (set of actions) for the given player number.
 * <p>
 * In addition to this generator being able to return the domain object, it may also be used to return the corresponding joint reward function
 * with the method {@link #getJointRewardFunction()}.
 * <p>
 * This class also provides static methods for returning generators for a number of classic bimatrix games: prisoner's dilemma, chicken, hawk dove,
 * battle of the sexes 1, battle of the sexes 2, matching pennies, and stag hunt.
 * <p>
 * This class also has a method for streamlining the world creation process so that repeated games (or single shot games) can be easily played
 * in the constructed game. For this use either the {@link #createRepeatedGameWorld(SGAgent...)} or {@link #createRepeatedGameWorld(SGDomain, SGAgent...)}
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
	 * will take the form of: BIMATRIX_ACTION_BASE_NAMEi where i is the row/column index. More specifically,
	 * it will be "actioni", since BIMATRIX_ACTION_BASE_NAME = "action"
	 */
	public static final String BIMATRIX_ACTION_BASE_NAME = "action";

	
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
	 * A constructor for a bimatrix game where the row player payoffs and column player payoffs are provided in two different 2D double matrices. The action
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
			actionsP1.add(BIMATRIX_ACTION_BASE_NAME + i);
		}
		this.actionSets.add(actionsP1);
		
		List <String> actionsP2 = new ArrayList<String>();
		for(int i = 0; i < nCols; i++){
			actionsP2.add(BIMATRIX_ACTION_BASE_NAME + i);
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

		for(int i = 0; i < this.actionNameToIndex.length; i++){
			for(Map.Entry<String, Integer> as : this.actionNameToIndex[i].namesToInd.entrySet()){
				domain.addActionType(new UniversalActionType(i + "_" + as.getKey(), new MatrixAction(as.getKey(), as.getValue())));
			}
		}

		domain.setJointActionModel(new StaticRepeatedGameModel());
		
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
	 * @param domain the SGDomain instance
	 * @param agents the agents to join the created world.
	 * @return a world instance with the provided agents having already joined.
	 */
	public World createRepeatedGameWorld(SGDomain domain, SGAgent...agents){
		
		//grab the joint reward function from our bimatrix game in the more general BURLAP joint reward function interface
		JointRewardFunction jr = this.getJointRewardFunction();
		
		//game repeats forever unless manually stopped after T times.
		TerminalFunction tf = new NullTermination();
		
		//set up the initial state generator for the world, which for a bimatrix game is trivial
		SGStateGenerator sg = new NFGameState();

		//create a world to synchronize the actions of agents in this domain and record results
		World w = new World(domain, jr, tf, sg);
		
		for(SGAgent a : agents){
			w.join(a);
		}
		
		return w;
		
	}
	
	
	/**
	 * Returns a {@link JointRewardFunction} function for this game.
	 * @return a {@link JointRewardFunction} function for this game.
	 */
	public JointRewardFunction getJointRewardFunction(){
		return new SingleStageNormalFormJointRewardFunction(this.nPlayers, ActionNameMap.deepCopyActionNameMapArray(this.actionNameToIndex), AgentPayoutFunction.getDeepCopyOfPayoutArray(this.payouts));
	}
	
	
	/**
	 * Returns a hashable strategy profile object for a strategy profile specified by action names
	 * @param actionNameToIndex the map from action names to action indices
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


	public SGAgentType generateAgentType(int player){
		List<ActionType> actions = new ArrayList<ActionType>();
		for(Map.Entry<String, Integer> e : actionNameToIndex[player].namesToInd.entrySet()){
			actions.add(new UniversalActionType(player + e.getKey(), new MatrixAction(e.getKey(), e.getValue())));
		}
		SGAgentType type = new SGAgentType("player" + player, actions);
		return type;
	}
	
	/**
	 * Returns a repeated game joint action model. This action model always returns to the same state.
	 * @return a repeated game joint action model.
	 */
	public static JointModel getRepatedGameActionModel(){
		return new StaticRepeatedGameModel();
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
	protected static class SingleStageNormalFormJointRewardFunction implements JointRewardFunction {

		int nPlayers;
		ActionNameMap [] actionNameToIndex;
		AgentPayoutFunction [] payouts;

		public SingleStageNormalFormJointRewardFunction(int nPlayers, ActionNameMap[] actionNameToIndex, AgentPayoutFunction[] payouts) {
			this.nPlayers = nPlayers;
			this.actionNameToIndex = actionNameToIndex;
			this.payouts = payouts;
		}

		@Override
		public double[] reward(State s, JointAction ja, State sp) {

			double [] rewards = new double[this.nPlayers];
			String [] profile = new String[this.nPlayers];
			for(int i = 0; i < this.nPlayers; i++){
				profile[i] = ja.action(i).actionName();
			}
			StrategyProfile stprofile = SingleStageNormalFormGame.getStrategyProfile(this.actionNameToIndex, profile);
			for(int i = 0; i < nPlayers; i++){
				rewards[i] = this.payouts[i].getPayout(stprofile);
			}

			return rewards;
		}
		
		
		
		
	}
	
	
	

	public static class MatrixAction implements Action{

		public int actionId;
		public String name;

		public MatrixAction(String name, int actionId) {
			this.name = name;
			this.actionId = actionId;
		}

		@Override
		public String actionName() {
			return name;
		}

		@Override
		public Action copy() {
			return new MatrixAction(name, actionId);
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public int hashCode() {
			return actionId;
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(o == null || getClass() != o.getClass()) return false;

			MatrixAction that = (MatrixAction) o;

			if(actionId != that.actionId) return false;
			return name != null ? name.equals(that.name) : that.name == null;

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
		public Map<String, Integer> namesToInd;
		
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
	 * {@link SGWorldShell}.
	 * @param args command line args
	 */
	public static void main(String [] args){
		
		SingleStageNormalFormGame game = SingleStageNormalFormGame.getPrisonersDilemma();
		SGDomain domain = (SGDomain)game.generateDomain();
		JointRewardFunction r = game.getJointRewardFunction();

		World w = new World(domain, r, new NullTermination(), (State)new NFGameState(2));

		SGWorldShell shell = new SGWorldShell(domain, w);
		shell.start();

		
	}
	
	

}
