package burlap.datastructures;

import java.util.Random;

import javax.management.RuntimeErrorException;

import burlap.debugtools.RandomFactory;


/**
 * This class provides methods for computing, sampling, and working with Boltzmann distributions/Softmax.
 * The input is a set of "preference" values. The output is a probability distribution over those preferences
 * with elements that had higher preferences having higher probability. Computation is performed using a log sum exponential
 * re-expression trick that avoids overflow errors.
 * 
 * @author James MacGlashan
 *
 */
public class BoltzmannDistribution {

	/**
	 * The preference values to turn into probabilities
	 */
	protected double []		preferences;
	
	/**
	 * The temperature value. Lower values produce more greedy deterministic probability outputs
	 * while higher values produce more uniform probability outputs
	 */
	protected double		temperature = 1.0;
	
	
	/**
	 * The preference values normalized by the temperature
	 */
	protected double []		tempNormalized;
	
	/**
	 * The output probabilities
	 */
	protected double []		probs;
	
	
	/**
	 * Indicates whether the probabilities need to be recomputed
	 */
	protected boolean		needsUpdate = true;
	
	
	/**
	 * The random object to use for sampling.
	 */
	protected Random		rand = RandomFactory.getMapped(0);
	
	
	/**
	 * Initializes the distribution with the preference values that are to be turned into a soft max probability distribution.
	 * @param preferences the preference values to turn into a probability distribution
	 */
	public BoltzmannDistribution(double [] preferences) {
		this.preferences = preferences.clone();
	}
	
	
	/**
	 * Initializes the distribution with the preference values that are to be turned into a soft max probability distribution and
	 * a temperature value to control how deterministic the probability output is. Lower temperature values result in a more
	 * deterministic distribution; higher values produce a more uniform distribution.
	 * @param preferences the preference values to turn into a probability distribution
	 * @param temperature a value on 0 < temperature < +infinity
	 */
	public BoltzmannDistribution(double [] preferences, double temperature) {
		this.preferences = preferences.clone();
		this.temperature = temperature;
	}
	
	
	/**
	 * Returns the input preferences
	 * @return the input preferences
	 */
	public double [] getPreferences(){
		return this.preferences;
	}
	
	
	/**
	 * Returns the temperature parameter
	 * @return the temperature parameter
	 */
	public double getTemperature(){
		return this.temperature;
	}
	
	/**
	 * Returns the number of elements on which there are preferences
	 * @return the number of elements on which there are preferences
	 */
	public int preferenceLength(){
		return this.preferences.length;
	}
	
	
	/**
	 * Returns the output probability distribution. This method computes the probabilities
	 * lazily and only when needed.
	 * @return the output probability distribution
	 */
	public double [] getProbabilities(){
		if(this.needsUpdate){
			this.computeProbs();
		}
		
		return this.probs;
	}
	
	
	/**
	 * Sets the temperature value to use.
	 * @param t a value on 0 < temperature < +infinity
	 */
	public void setTemperature(double t){
		this.temperature = t;
		this.needsUpdate = true;
	}
	
	/**
	 * Sets the preference for the ith elemnt
	 * @param i which element to set
	 * @param p the preference for that element
	 */
	public void setPreference(int i, double p){
		this.preferences[i] = p;
		this.needsUpdate = true;
	}
	
	
	/**
	 * Sets the input preferences
	 * @param preferences the input preferences
	 */
	public void setPreferences(double [] preferences){
		this.preferences = preferences.clone();
		this.needsUpdate = true;
	}
	
	
	/**
	 * Samples the output probability distribution.
	 * @return the index of the sampled element
	 */
	public int sample(){
		
		if(this.needsUpdate){
			this.computeProbs();
		}
		
		double r = this.rand.nextDouble();
		double sum = 0.;
		for(int i = 0; i < this.probs.length; i++){
			sum += this.probs[i];
			if(r < sum){
				return i;
			}
		}
		
		throw new RuntimeErrorException(new Error("Error in sample; Boltzmann distribution did not sum to 1"));
		
	}
	
	
	/**
	 * Computes the probability distribution. This method uses the log sum exp trick to avoid overflow issues.
	 */
	protected void computeProbs(){
		
		this.computeTempNormalized();
		double max = this.maxTNormed();
		
		double sum = 0.;
		for(int i = 0; i < this.tempNormalized.length; i++){
			sum += Math.exp(this.tempNormalized[i] - max);
		}
		
		double lsum = Math.log(sum);
		
		this.probs = new double[this.tempNormalized.length];
		for(int i = 0; i < this.probs.length; i++){
			this.probs[i] = Math.exp(this.tempNormalized[i] - max - lsum);
		}
		
		needsUpdate = false;
		
	}
	
	/**
	 * Computes the temperature normalized preference values
	 */
	protected void computeTempNormalized(){
		this.tempNormalized = new double[this.preferences.length];
		for(int i = 0; i < this.preferences.length; i++){
			this.tempNormalized[i] = this.preferences[i] / this.temperature;
		}
	}
	
	/**
	 * Returns the maximum temperature normalized preference
	 * @return the maximum temperature normalized preference
	 */
	protected double maxTNormed(){
		double max = Double.NEGATIVE_INFINITY;
		for(int i = 0; i < this.tempNormalized.length; i++){
			max = Math.max(max, this.tempNormalized[i]);
		}
		return max;
	}
	
	
	

}
