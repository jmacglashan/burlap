package burlap.behavior.singleagent.interfaces.rlglue.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.ExponentialDecayLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.interfaces.rlglue.RLGlueAgentShell;
import burlap.behavior.singleagent.interfaces.rlglue.RLGlueLearningAgentFactory;
import burlap.behavior.singleagent.interfaces.rlglue.RLGlueWrappedDomainGenerator;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.behavior.singleagent.planning.commonpolicies.BoltzmannQPolicy;
import burlap.behavior.singleagent.planning.commonpolicies.EpsilonGreedy;
import burlap.behavior.singleagent.vfa.ValueFunctionApproximation;
import burlap.behavior.singleagent.vfa.cmac.CMACFeatureDatabase;
import burlap.datastructures.CommandLineOptions;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;



/**
 * This class is used to setup a BURLAP gradient SARSA-lambda algorithm with CMAC value function approximation for RLGlue. 
 * It can be launched from a main method and have various
 * SARSA-lambda parameters specified by command line options. Use the "--help" option to see full list.
 * @author James MacGlashan
 *
 */
public class RLGlueCMACSarsaLambdaFactory implements RLGlueLearningAgentFactory {

	
	/**
	 * The number of tilings to use
	 */
	protected int 													nTiles = 5;
	
	/**
	 * The default tile width to use for unspecified attributes
	 */
	protected double 												defaultTileWidth = 0.3;
	
	/**
	 * The tile widths for each attribute
	 */
	protected Map<Integer, Double> 									tileWidths = new HashMap<Integer, Double>();
	
	
	/**
	 * The learning rate function used.
	 */
	protected LearningRate											learningRate = new ConstantLR(0.02);
	
	/**
	 * The learning policy to use. Typically these will be policies that link back to this object so that they change as the Q-value estimate change.
	 */
	protected Policy												learningPolicy = new EpsilonGreedy(0.1);
	
	/**
	 * The initial Q-value function weight
	 */
	protected double												initialFunctionWeight = 0.0;
	
	
	/**
	 * The lambda value with default value of 0.5
	 */
	protected double		lambda = 0.5;
	
	
	
	
	
	
	
	

	public int getnTiles() {
		return nTiles;
	}

	public void setnTiles(int nTiles) {
		this.nTiles = nTiles;
	}

	public double getDefaultTileWidth() {
		return defaultTileWidth;
	}

	public void setDefaultTileWidth(double defaultTileWidth) {
		this.defaultTileWidth = defaultTileWidth;
	}
	
	
	public LearningRate getLearningRate() {
		return learningRate;
	}

	public void setLearningRate(LearningRate learningRate) {
		this.learningRate = learningRate;
	}

	public Policy getLearningPolicy() {
		return learningPolicy;
	}

	public void setLearningPolicy(Policy learningPolicy) {
		this.learningPolicy = learningPolicy;
	}

	public double getInitialFunctionWeight() {
		return initialFunctionWeight;
	}

	public void setInitialFunctionWeight(double initialFunctionWeight) {
		this.initialFunctionWeight = initialFunctionWeight;
	}

	public double getLambda() {
		return lambda;
	}

	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	/**
	 * Sets the tile width for a given attribute
	 * @param attributeIndex the continuous attribute index
	 * @param width the width for that attribute
	 */
	public void addTileWidth(int attributeIndex, double width){
		this.tileWidths.put(attributeIndex, width);
	}
	
	@Override
	public LearningAgent generateAgentForRLDomain(Domain domain, double discount, RewardFunction rf, TerminalFunction tf) {
		
		CMACFeatureDatabase fd = new CMACFeatureDatabase(this.nTiles, CMACFeatureDatabase.TilingArrangement.RANDOMJITTER);
		int i = 0;
		for(Attribute a : domain.getAttributes()){
			Double width = this.tileWidths.get(i);
			width = width != null ? width : this.defaultTileWidth;
			fd.addSpecificationForAllTilings(RLGlueWrappedDomainGenerator.REALCLASS, a, width);
			i++;
		}
		
		ValueFunctionApproximation vfa = fd.generateVFA(this.initialFunctionWeight);
		
		GradientDescentSarsaLam gd = new GradientDescentSarsaLam(domain, rf, tf, discount, vfa, 0.1, this.lambda);
		gd.setLearningRate(this.learningRate);
		gd.setLearningPolicy(this.getLearningPolicy());
		
		return gd;
	}
	
	
	
	public static void main(String [] args){
		
		CommandLineOptions options = new CommandLineOptions(args);
		
		if(options.containsOption("help")){
			System.out.println("--help: print this message\n" +
							   "--lambda=v: sets the lambda value\n" +
							   "--qinit=v: sets the initial q-value to v everywhere\n" + 
							   "--constant_lr=v: sets a constant learnign rate to v\n" + 
							   "--exp_lr_base=v: sets the learning rate to an exponential decay with expoential base v\n" + 
							   "--exp_lr_init=v: sets the learning rate to an exponential decay with initial learning rate v\n" + 
							   "--exp_lr_min=v: sets the learning rate to an exponential decay with minimum learning rate v\n" +
							   "--egreedy=v: sets the learning policy to epsilon greedy with epsilon = v\n" +
							   "--boltzmann=v: sets the learning policy to boltzmann with temperature = v\n" + 
							   "--defaultTileWidth=v: sets the tile width to v for attributes that do not have a specific width set\n" +
							   "--nTilings=v: sets the number of overlapping tilings to use\n" +
							   "--tileWidth_i=v: sets the tile width for continue attribute i to v\n" +
							   "\nBy default lambda = 0.5, learning rate is constant 0.1, q initialization is zero, and epsilon greedy policy with epsilon = 0.1");
			System.exit(0);
		}
		
		System.out.println("Use --help to see varaible settings.");
		
		RLGlueCMACSarsaLambdaFactory fact = new RLGlueCMACSarsaLambdaFactory();
		
		if(options.containsOption("lambda")){
			double lambda = Double.parseDouble(options.optionValue("lamba"));
			fact.setLambda(lambda);
		}
		
		if(options.containsOption("qinit")){
			double qval = Double.parseDouble(options.optionValue("qinit"));
			fact.setInitialFunctionWeight(qval);
		}
		
		if(options.containsOption("egreedy")){
			double eps = Double.parseDouble(options.optionValue("egreedy"));
			fact.setLearningPolicy(new EpsilonGreedy(eps));
		}
		
		if(options.containsOption("boltzmann")){
			double temp = Double.parseDouble(options.optionValue("boltzmann"));
			fact.setLearningPolicy(new BoltzmannQPolicy(temp));
		}
		
		if(options.containsOption("constant_lr")){
			double lr = Double.parseDouble(options.optionValue("constant_lr"));
			fact.setLearningRate(new ConstantLR(lr));
		}
		else{
			
			boolean useExpLR = false;
			double initialLR = 0.1;
			double decayBase = 0.99;
			double minLR = Double.MIN_VALUE;
			
			if(options.containsOption("exp_lr_base")){
				useExpLR = true;
				decayBase = Double.parseDouble(options.optionValue("exp_lr_base"));
			}
			
			if(options.containsOption("exp_lr_init")){
				useExpLR = true;
				initialLR = Double.parseDouble(options.optionValue("exp_lr_init"));
			}
			
			if(options.containsOption("exp_lr_min")){
				useExpLR = true;
				minLR = Double.parseDouble(options.optionValue("exp_lr_min"));
			}
			
			if(useExpLR){
				fact.setLearningRate(new ExponentialDecayLR(initialLR, decayBase, minLR));
			}
			
		}
		
		if(options.containsOption("defaultTileWidth")){
			double tw = Double.parseDouble(options.optionValue("defaultTileWidth"));
			fact.setDefaultTileWidth(tw);
		}
		
		if(options.containsOption("nTilings")){
			int ntiles = Integer.parseInt(options.optionValue("nTilings"));
			fact.setnTiles(ntiles);
		}
		
		
		List<String> widthOptions = options.getOptionsStartingWithName("tileWidth_");
		for(String option : widthOptions){
			String [] comps = option.split("_");
			int index = Integer.parseInt(comps[1]);
			double tw = Double.parseDouble(options.optionValue(option));
			fact.addTileWidth(index, tw);
		}
		
		RLGlueAgentShell ashell = new RLGlueAgentShell(fact);
		System.out.println("Loading agent into RLGlue...");
		ashell.loadAgent();
		
	}
	
	
}
