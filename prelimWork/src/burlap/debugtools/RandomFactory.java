package burlap.debugtools;

import java.util.*;






/**
 * Random factory that allows you to logically group various random generators.
 * This is useful for if you have many classes that use stochastic processes
 * but you want to have a fixed sequence across process execution for debug purposes.
 * To synchronize, just seed either a mapped random generator or the default in the
 * main method and use the get method to retrieve the random object for all other
 * classes. To break the fixed sequence, just construct it without a seed
 * 
 * 
 * @author James MacGlashan
 *
 */
public class RandomFactory {

	private static RandomFactory factory = new RandomFactory();
	
	/**
	 * A default random number generator
	 */
	Random defaultRandom_;
	
	/**
	 * The set of random number generators that have been constructed for different int codes
	 */
	Map <Integer, Random> intMapped;
	
	/**
	 * The set of random number generators that have been constructed for different String codes
	 */
	Map <String, Random> stringMapped;
	
	
	/**
	 * Example usage.
	 * @param args
	 */
	public static void main(String []args){
		
		//example
		
		
		//seed in main method of program to synchronize classes (comment out to break synchronization)
		RandomFactory.seedMapped(0, 943);
	
		
		//in each class that uses a random construct it like this:
		Random rand = RandomFactory.getMapped(0);
		
		
		//and use reference as normal
		System.out.println("" + rand.nextInt());
		
	}
	
	
	/**
	 * Returns the default random number generator.
	 * @return the default random number generator.
	 */
	public static Random getDefault(){
		return factory.ingetDefault();
	}
	
	/**
	 * Sets the seed of the default random number generator
	 * @param seed the seed to use
	 * @return the default random number generator
	 */
	public static Random seedDefault(long seed){
		return factory.inseedDefault(seed);
	}
	
	
	/**
	 * Either return a the default random generator if it has already been created; or created it with the given seed if it has not been created.
	 * @param seed the seed to use
	 * @return the default random generator
	 */
	public static Random getOrSeedDefault(long seed){
		return factory.ingetOrSeedDefault(seed);
	}
	
	/**
	 * Returns the random generator with the associated id or creates it if it does not yet exist
	 * @param id the id of the random generator
	 * @return the random generator
	 */
	public static Random getMapped(int id){
		return factory.ingetMapped(id);
	}
	
	
	/**
	 * Seeds and returns the random generator with the associated id or creates it if it does not yet exist
	 * @param id the id of the random generator
	 * @param seed the seed to use
	 * @return the random generator
	 */
	public static Random seedMapped(int id, long seed){
		return factory.inseedMapped(id, seed);
	}
	
	
	/**
	 * Either returns the random generator for the given id or creates if with the given seed it does not yet exit
	 * @param id the id of the random generator
	 * @param seed the seed to use
	 * @return the random generator
	 */
	public static Random getOrSeedMapped(int id, long seed){
		return factory.ingetOrSeedMapped(id, seed);
	}
	
	
	
	/**
	 * Returns the random generator with the associated String id or creates it if it does not yet exist
	 * @param id the id of the random generator
	 * @return the random generator
	 */
	public static Random getMapped(String id){
		return factory.ingetMapped(id);
	}
	
	
	/**
	 * Seeds and returns the random generator with the associated String id or creates it if it does not yet exist
	 * @param id the id of the random generator
	 * @param seed the seed to use
	 * @return the random generator
	 */
	public static Random seedMapped(String id, long seed){
		return factory.inseedMapped(id, seed);
	}
	
	
	/**
	 * Either returns the random generator for the given String id or creates if with the given seed it does not yet exit
	 * @param id the id of the random generator
	 * @param seed the seed to use
	 * @return the random generator
	 */
	public static Random getOrSeedMapped(String id, long seed){
		return factory.ingetOrSeedMapped(id, seed);
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Initializes the map structures
	 */
	public RandomFactory(){
		defaultRandom_ = null;
		intMapped = new HashMap<Integer, Random>();
		stringMapped = new HashMap<String, Random>();
	}
	
	
	/**
	 * Returns the default random number generator.
	 * @return the default random number generator.
	 */
	public Random ingetDefault(){
		if(defaultRandom_ == null){
			defaultRandom_ = new Random();
		}
		return defaultRandom_;
	}
	
	
	/**
	 * Sets the seed of the default random number generator
	 * @param seed the seed to use
	 * @return the default random number generator
	 */
	public Random inseedDefault(long seed){
		defaultRandom_ = new Random(seed);
		return defaultRandom_;
	}
	
	
	/**
	 * Either return a the default random generator if it has already been created; or created it with the given seed if it has not been created.
	 * @param seed the seed to use
	 * @return the default random generator
	 */
	public Random ingetOrSeedDefault(long seed){
		if(defaultRandom_ == null){
			defaultRandom_ = new Random(seed);
		}
		return defaultRandom_;
	}
	
	
	
	/**
	 * Returns the random generator with the associated id or creates it if it does not yet exist
	 * @param id the id of the random generator
	 * @return the random generator
	 */
	public Random ingetMapped(int id){
		Random r = intMapped.get(id);
		if(r != null){
			return r;
		}
		else{
			r = new Random();
			intMapped.put(id, r);
		}
		return r;
	}
	
	
	/**
	 * Seeds and returns the random generator with the associated id or creates it if it does not yet exist
	 * @param id the id of the random generator
	 * @param seed the seed to use
	 * @return the random generator
	 */
	public Random inseedMapped(int id, long seed){
		Random r = new Random(seed);
		intMapped.put(id, r);
		return r;
	}
	
	
	/**
	 * Either returns the random generator for the given id or creates if with the given seed it does not yet exit
	 * @param id the id of the random generator
	 * @param seed the seed to use
	 * @return the random generator
	 */
	public Random ingetOrSeedMapped(int id, long seed){
		Random r = intMapped.get(id);
		if(r != null){
			return r;
		}
		else{
			r = new Random(seed);
			intMapped.put(id, r);
		}
		return r;
	}
	
	
	
	/**
	 * Returns the random generator with the associated String id or creates it if it does not yet exist
	 * @param id the id of the random generator
	 * @return the random generator
	 */
	public Random ingetMapped(String id){
		Random r = intMapped.get(id);
		if(r != null){
			return r;
		}
		else{
			r = new Random();
			stringMapped.put(id, r);
		}
		return r;
	}
	
	
	/**
	 * Seeds and returns the random generator with the associated String id or creates it if it does not yet exist
	 * @param id the id of the random generator
	 * @param seed the seed to use
	 * @return the random generator
	 */
	public Random inseedMapped(String id, long seed){
		Random r = new Random(seed);
		stringMapped.put(id, r);
		return r;
	}
	
	
	/**
	 * Either returns the random generator for the given String id or creates if with the given seed it does not yet exit
	 * @param id the id of the random generator
	 * @param seed the seed to use
	 * @return the random generator
	 */
	public Random ingetOrSeedMapped(String id, long seed){
		Random r = stringMapped.get(id);
		if(r != null){
			return r;
		}
		else{
			r = new Random(seed);
			stringMapped.put(id, r);
		}
		return r;
	}
	
	
}
