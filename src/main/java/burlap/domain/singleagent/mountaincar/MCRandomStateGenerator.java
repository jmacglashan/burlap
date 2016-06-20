package burlap.domain.singleagent.mountaincar;

import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;

import java.util.Random;


/**
 * Generates {@link MountainCar} states with the x-position between some specified range and the velocity between some specified range. By default, the ranges are the
 * full ranges supported by the domain, but they can be individually changed.
 * @author James MacGlashan
 *
 */
public class MCRandomStateGenerator implements StateGenerator {

	private double xmin;
	private double xmax;
	private double vmin;
	private double vmax;
	
	private Random rand;
	
	
	/**
	 * Initializes for the {@link MountainCar} {@link Domain} object for which states will be generated. By default, the random x and velocity ranges will be
	 * the full range used by the domain.
	 * @param params the mountain car physics parameters specifying the boundaries
	 */
	public MCRandomStateGenerator(MountainCar.MCPhysicsParams params){

		this.xmin = params.xmin;
		this.xmax = params.xmax;
		this.vmin = params.vmin;
		this.vmax = params.vmax;

		
		this.rand = RandomFactory.getMapped(0);
	}
	
	/**
	 * Initializes for the given boundaries in which random states will be created
	 * @param xmin the minimum x position value
	 * @param xmax the maximum x position value
	 * @param vmin the minimum velocity value
	 * @param vmax the maximum velocity value
	 */
	public MCRandomStateGenerator(double xmin, double xmax, double vmin, double vmax){
		this.xmin = xmin;
		this.xmax = xmax;
		this.vmin = vmin;
		this.vmax = vmax;
	}
	
	
	
	/**
	 * Returns the minimum x-value that a generated state can have.
	 * @return the minimum x-value that a generated state can have.
	 */
	public double getXmin() {
		return xmin;
	}

	/**
	 * Sets the minimum x-value that a generated state can have.
	 * @param xmin the minimum x-value that a generated state can have.
	 */
	public void setXmin(double xmin) {
		this.xmin = xmin;
	}

	
	/**
	 * Returns the maximum x-value that a generated state can have.
	 * @return the maximum x-value that a generated state can have.
	 */
	public double getXmax() {
		return xmax;
	}

	
	/**
	 * Sets the maximum x-value that a generated state can have.
	 * @param xmax the maximum x-value that a generated state can have.
	 */
	public void setXmax(double xmax) {
		this.xmax = xmax;
	}

	
	/**
	 * Returns the minimum velocity that a generated state can have.
	 * @return the minimum velocity that a generated state can have.
	 */
	public double getVmin() {
		return vmin;
	}

	
	/**
	 * Sets the minimum velocity that a generated state can have.
	 * @param vmin the minimum velocity that a generated state can have.
	 */
	public void setVmin(double vmin) {
		this.vmin = vmin;
	}

	
	/**
	 * Returns the maximum velocity that a generated state can have.
	 * @return the maximium velocity tht a generated state can have.
	 */
	public double getVmax() {
		return vmax;
	}

	/**
	 * Sets the maximum velocity that a generated state can have.
	 * @param vmax the maximum velocity that a generated state can have.
	 */
	public void setVmax(double vmax) {
		this.vmax = vmax;
	}
	
	/**
	 * Sets the random x-value range that a generated state can have.
	 * @param xmin the miniimum x-value
	 * @param xmax the maximum x-value
	 */
	public void setXRange(double xmin, double xmax){
		this.xmin = xmin;
		this.xmax = xmax;
	}
	
	/**
	 * Sets the random velocity range that a generated state can have.
	 * @param vmin the minimum velocity
	 * @param vmax the maximum velocity
	 */
	public void setVRange(double vmin, double vmax){
		this.vmin = vmin;
		this.vmax = vmax;
	}
	
	/**
	 * Returns the random object used for generating states
	 * @return the random object used for generating states
	 */
	public Random getRandomObject(){
		return this.rand;
	}
	
	/**
	 * Sets the random object used for generating states
	 * @param rand the random object used for generating states
	 */
	public void setRandomObject(Random rand){
		this.rand = rand;
	}

	@Override
	public State generateState() {

		
		double rx = this.rand.nextDouble() * (this.xmax - this.xmin) + this.xmin;
		double rv = this.rand.nextDouble() * (this.vmax - this.vmin) + this.vmin;
		
		MCState s = new MCState(rx, rv);
		
		return s;
	}

}
