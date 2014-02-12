package burlap.behavior.singleagent.interfaces.rlglue.common;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.ExponentialDecayLR;
import burlap.behavior.singleagent.ValueFunctionInitialization;
import burlap.behavior.singleagent.interfaces.rlglue.RLGlueAgentShell;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.behavior.singleagent.planning.PlannerDerivedPolicy;
import burlap.behavior.singleagent.planning.commonpolicies.BoltzmannQPolicy;
import burlap.behavior.singleagent.planning.commonpolicies.EpsilonGreedy;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.datastructures.CommandLineOptions;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * This class is used to setup a BURLAP SARSA-lambda algorithm with RLGlue. It can be launched from a main method and have various
 * SARSA-lambda parameters specified by command line options. Use the "--help" option to see full list.
 * @author James MacGlashan
 *
 */
public class RLGlueSARSALambdaFactory extends RLGLueQlearningFactory {

	/**
	 * The lambda value with default value of 0.5
	 */
	protected double		lambda = 0.5;
	
	
	@Override
	public LearningAgent generateAgentForRLDomain(Domain domain, double discount, RewardFunction rf, TerminalFunction tf) {
		
		SarsaLam sarsa = new SarsaLam(domain, rf, tf, discount, new DiscreteStateHashFactory(), 0., 0.1, this.lambda);
		sarsa.setQInitFunction(qInitFunction);
		sarsa.setLearningRateFunction(learningRate);
		
		((PlannerDerivedPolicy)learningPolicy).setPlanner(sarsa);
		sarsa.setLearningPolicy(learningPolicy);
		
		return sarsa;
	}


	public double getLambda() {
		return lambda;
	}


	public void setLambda(double lambda) {
		this.lambda = lambda;
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
							   "\nBy default lambda = 0.5, learning rate is constant 0.1, q initialization is zero, and epsilon greedy policy with epsilon = 0.1");
			System.exit(0);
		}
		
		System.out.println("Use --help to see varaible settings.");
		
		RLGlueSARSALambdaFactory fact = new RLGlueSARSALambdaFactory();
		
		if(options.containsOption("lambda")){
			double lambda = Double.parseDouble(options.optionValue("lamba"));
			fact.setLambda(lambda);
		}
		
		if(options.containsOption("qinit")){
			double qval = Double.parseDouble(options.optionValue("qinit"));
			fact.setqInitFunction(new ValueFunctionInitialization.ConstantValueFunctionInitialization(qval));
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
		
		RLGlueAgentShell ashell = new RLGlueAgentShell(fact);
		System.out.println("Loading agent into RLGlue...");
		ashell.loadAgent();
		
	}
	
}
