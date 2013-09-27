package burlap.datastructures;

import java.util.Random;

import javax.management.RuntimeErrorException;

import burlap.debugtools.RandomFactory;

public class BoltzmannDistribution {

	protected double []		preferences;
	protected double		temperature = 1.0;
	
	protected double []		tempNormalized;
	protected double []		probs;
	
	protected boolean		needsUpdate = true;
	
	protected Random		rand = RandomFactory.getMapped(0);
	
	public BoltzmannDistribution(double [] preferences) {
		this.preferences = preferences.clone();
	}
	
	public BoltzmannDistribution(double [] preferences, double temperature) {
		this.preferences = preferences.clone();
		this.temperature = temperature;
	}
	
	
	public double [] getPreferences(){
		return this.preferences;
	}
	
	public double getTemperature(){
		return this.temperature;
	}
	
	public int preferenceLength(){
		return this.preferences.length;
	}
	
	public double [] getProbabilities(){
		if(this.needsUpdate){
			this.computeProbs();
		}
		
		return this.probs;
	}
	
	public void setTemperature(double t){
		this.temperature = t;
		this.needsUpdate = true;
	}
	
	public void setPreference(int i, double p){
		this.preferences[i] = p;
		this.needsUpdate = true;
	}
	
	public void setPreferences(double [] preferences){
		this.preferences = preferences.clone();
		this.needsUpdate = true;
	}
	
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
	
	
	protected void computeTempNormalized(){
		this.tempNormalized = new double[this.preferences.length];
		for(int i = 0; i < this.preferences.length; i++){
			this.tempNormalized[i] = this.preferences[i] / this.temperature;
		}
	}
	
	protected double maxTNormed(){
		double max = Double.NEGATIVE_INFINITY;
		for(int i = 0; i < this.tempNormalized.length; i++){
			max = Math.max(max, this.tempNormalized[i]);
		}
		return max;
	}
	
	
	

}
