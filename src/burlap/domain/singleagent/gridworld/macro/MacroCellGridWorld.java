package burlap.domain.singleagent.gridworld.macro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import burlap.debugtools.RandomFactory;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * A domain that extends the grid world domain by adding "Macro Cells" to it, which specify rectangular regions of the space.
 * Macro cells are fully indicated by propositional functions, one for each macro cell, that evaluate to true when the agent is inside the
 * macro cell with which it is associated. This domain is often used in inverse reinforcement learning work and was the motivation
 * for its inclusion in this work as well. Some additional methods and objects for working with IRL cases have been included,
 * such as a method to create a reward function that is a linear function of the macro-cell propositional functions.
 * 
 * 
 * @author Stephen Brawner and Mark Ho; modified by James MacGlashan
 *
 */
public class MacroCellGridWorld extends GridWorldDomain {

	public static final int								DEFAULTHEIGHT = 32;
	public static final int								DEFAULTWIDTH = 32;


	/**
	 * The number of rows of macro cells (cells across the x-axis)
	 */
	protected int										macroCellVerticalCount = 16;
	
	/**
	 * The number of columns of macro cells (cells across the y-axis)
	 */
	protected int										macroCellHorizontalCount = 16;

	
	/**
	 * Initializes with a default world size of 32x32 and macro cell size of 16x16. Transition dynamics are set to 0.7 probability of success in intended direction; 0.3 probability distributed across
	 * other three directions.
	 */
	public MacroCellGridWorld() {
		super(DEFAULTWIDTH, DEFAULTHEIGHT); //default gridworld

		//There are 4 actions (cardinal directions)
		// 30% chance action goes in one of the other 3
		// directions
		this.setProbSucceedTransitionDynamics(.7);

	}

	
	/**
	 * Initializes with the given world width/height and macro-cell width/height
	 * @param width the width of the world
	 * @param height the height of the world
	 * @param macroCellWidth the macro-cell width of the world
	 * @param macroCellHeight the macro-cell height of the world
	 */
	public MacroCellGridWorld(int width, int height, int macroCellWidth, int macroCellHeight) {
		super(width, height);
		this.macroCellHorizontalCount = macroCellWidth;
		this.macroCellVerticalCount = macroCellHeight;
	}


	/**
	 * Returns the number of rows of macro-cells (cells across the y-axis)
	 * @return the number of rows of macro-cells (cells across the y-axis)
	 */
	public int getMacroCellVerticalCount() {
		return this.macroCellVerticalCount;
	}


	/**
	 * Returns the number of columns of macro-cells (cells across the x-axis)
	 * @return the number of rowcolumnss of macro-cells (cells across the x-axis)
	 */
	public int getMacroCellHorizontalCount() {
		return this.macroCellHorizontalCount;
	}
	
	
	/**
	 * Sets the number of rows of macro-cells (cells across the y-axis)
	 * @param vCount the number of rows of macro-cells (cells across the y-axis)
	 */
	public void setMacroCellVerticalCount(int vCount) {
		this.macroCellVerticalCount = vCount;
	}

	
	/**
	 * Sets the number of coumns of macro-cells (cells across the x-axis)
	 * @param vCount the number of coumns of macro-cells (cells across the x-axis)
	 */
	public void setMacroCellHorizontalCount(int vCount) {
		this.macroCellHorizontalCount = vCount;
	}


	@Override
	public Domain generateDomain() {
		Domain domain = super.generateDomain();
		
		int macroCellWidth = this.getWidth() / this.getMacroCellHorizontalCount();
		int macroCellHeight = this.getHeight() / this.getMacroCellVerticalCount();
		for (int i = 0; i < this.getMacroCellHorizontalCount(); ++i) {
			int x = i * macroCellWidth;
			for (int j = 0; j < this.getMacroCellVerticalCount(); ++j) {
				int y = j * macroCellHeight;
				new InMacroCellPF(domain, x, y, macroCellWidth, macroCellHeight);
			}
		}
		
		return domain;
	}

	
	

	/**
	 * Returns a random initial state in a world with no location objects. The agent will not be placed in any cell that contains an obstacle,
	 * but any other cell is equiprobable
	 * @param gridWorldGenerator the grid world generator containing the map
	 * @param d the domain object to which the state will be associated
	 * @return a state with the agent in a random free position.
	 */
	public static State getRandomInitialState(GridWorldDomain gridWorldGenerator, Domain d) {
		Random r = RandomFactory.getMapped(0);
		State s = new State();
		int [][] map = gridWorldGenerator.getMap();
		
		int rx = 0;
		int ry = 0;
		
		do{
			rx = r.nextInt(map.length);
			ry = r.nextInt(map[0].length);
		}while(map[rx][ry] == 1);
		
		ObjectInstance agent = new ObjectInstance(d.getObjectClass(CLASSAGENT), CLASSAGENT+0);
		agent.setValue(ATTX, rx);
		agent.setValue(ATTY, ry);
		s.addObject(agent);
		
		return s;
	}


	/**
	 * Returns an array of the propositional functions that detect if the agent is in a macro cell.
	 * @param domain the domain object containing the propositional functions.
	 * @param gridWorld the {@link MacroCellGridWorld} domain generator.
	 * @return an array of the propositional functions that detect if the agent is in a macro cell.
	 */
	public static PropositionalFunction[] getMacroCellPropositionalFunctions(Domain domain, MacroCellGridWorld gridWorld) {
		
		List<PropositionalFunction> pfs = new ArrayList<PropositionalFunction>(domain.getPropFunctions().size());
		
		for(PropositionalFunction pf : domain.getPropFunctions()){
			if(pf instanceof InMacroCellPF){
				pfs.add(pf);
			}
		}
		
		PropositionalFunction [] pfsArray = new PropositionalFunction[pfs.size()];
		return pfs.toArray(pfsArray);
		
	}

	
	/**
	 * Given an array of macro-cell propositional functions, returns a random reward function that is a linear combination of them.
	 * Reward weights are assigned following a Bernoulli distribution for each
	 * propositional function being active (probability 0.9). An active propositional function is then assigned a random weight. However,
	 * the method will always use a set of reward weights that has at least 2 active propositional functions. 
	 * The reward weights are also normalized in value.
	 * @param functions the macro cell propositional functions.
	 * @return a random reward function that is a linear combination of macro-cell propositional functions
	 */
	public static LinearInPFRewardFunction generateRandomMacroCellRF(PropositionalFunction[] functions){
		Map<String, Double> weights = generateRandomRewardsMap(functions);
		return new LinearInPFRewardFunction(functions, weights);	
	}
	
	
	/**
	 * Given an array of macro-cell propositional functions, returns a random reward weight to be associated with them which
	 * can then be used to create a reward function. Reward weights are assigned following a Bernoulli distribution for each
	 * propositional function being active (probability 0.9). An active propositional function is then assigned a random weight. However,
	 * the method will always return a set of reward weights that has at least 2 active propositional functions. 
	 * The reward weights are also normalized in value.
	 * @param functions the macro cell propositional functions.
	 * @return a map of reward weights for each macro-cell propositional function
	 */
	public static Map<String, Double> generateRandomRewardsMap(PropositionalFunction[] functions) {

		Random rando = new Random();
		//reward function generation algorithm from Ng et al
		double[] weights = new double[functions.length];
		int numFilled = 0;
		while (numFilled < 2) {
			numFilled = 0;
			for (int i = 0; i < functions.length; i++) {
				if (rando.nextDouble() > .9) {
					weights[i] = rando.nextDouble();
					numFilled+=1;
				}
				else {
					weights[i] = 0.0;
				}
			}
		}
		//dont forget to renormalize
		double norm = 0.0;
		for (double w : weights) {
			norm += w*w;
		}
		norm = Math.sqrt(norm);
		for (int i = 0; i < functions.length; i++) {
			weights[i] = weights[i]/norm;
		}

		Map<String, Double> rewards = new HashMap<String, Double>();

		for (int i = 0; i < functions.length; i++) {
			rewards.put(functions[i].getName(), weights[i]);
			System.out.println(functions[i].getName() + " reward: " + weights[i]);
		}
		return rewards;
	}


	
	/**
	 * A propositional function for detecting if the agent is in a specific macro cell.
	 * Although the propositional function operates on the agent, it does not take any parameters
	 * an simply assumes that the agent object position is what is being evaluated. The name
	 * is in the form: [x, y], where x and y specify the left and bottom coordinates of the
	 * macro cell.
	 * @author Stephen Brawner and Mark Ho
	 *
	 */
	public static class InMacroCellPF extends PropositionalFunction{
		private int left, right, top, bottom;

		public InMacroCellPF(Domain domain, int x, int y, int width, int height) {
			super("[" + x + ", " + y + "]", domain, "");
			this.left = x;
			this.right = x + width;
			this.bottom = y;
			this.top = y + width;
		}

		@Override
		public boolean isTrue(State state, String[] params) {
			List<ObjectInstance> agents = state.getObjectsOfTrueClass(MacroCellGridWorld.CLASSAGENT);
			if (agents.size() == 0) {
				return false;
			}
			ObjectInstance agent = agents.get(0);
			int agentX = agent.getIntValForAttribute(MacroCellGridWorld.ATTX);
			int agentY = agent.getIntValForAttribute(MacroCellGridWorld.ATTY);
			return this.isTrue(agentX, agentY);
		}

		public boolean isTrue(int agentX, int agentY) {
			return (left <= agentX && agentX < right &&
					bottom <= agentY && agentY < top);
		}
	}
	
	
	
	
	/**
	 * RewardFunction class that returns rewards based on a linear combination of propositional functions
	 * @author Mark Ho
	 *
	 */
	public static class LinearInPFRewardFunction implements RewardFunction {
		protected Map<String, Double> rewards;
		protected PropositionalFunction[] propositionalFunctions;

		/**
		 * Initializes
		 * @param functions the propositional function over which the RF is a function
		 * @param rewards the map from propositional function names to their linear reward weight
		 */
		public LinearInPFRewardFunction(PropositionalFunction[] functions, Map<String, Double> rewards) {
			this.propositionalFunctions = functions.clone();
			this.rewards = new HashMap<String, Double>(rewards);
		}
		@Override
		public double reward(State s, GroundedAction a, State sprime) {
			double reward = 0;
			for (PropositionalFunction function : this.propositionalFunctions) {
				if (function.isTrue(s, "")) {
					reward += this.rewards.get(function.getName());
				}
			}
			return reward;
		}
		
		public Map<String, Double> getPFRewardWeights(){
			return this.rewards;
		}
	}
	
	
	
}
