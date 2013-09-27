package burlap.debugtools;

import java.util.*;




//Random factory that allows you to logically group various random generators
//This is useful for if you have many classes that use stochashtic processes
//but you want to have a fixed sequence across process execution for debug purposes.
//to synchronize, just seed either a mapped random generator or the default in the
//main method and use the get method to retrieve the random object for all other
//classes. To break the fixed sequence, just construct it without a seed


public class RandomFactory {

	private static RandomFactory factory = new RandomFactory();
	
	Random defaultRandom_;
	Map <Integer, Random> intMapped;
	Map <String, Random> stringMapped;
	
	
	public static void main(String []args){
		
		//example
		
		
		//seed in main method of program to synchronize classes (comment out to break synchronization)
		RandomFactory.seedMapped(0, 943);
	
		
		//in each class that uses a random construct it like this:
		Random rand = RandomFactory.getMapped(0);
		
		
		//and use reference as normal
		System.out.println("" + rand.nextInt());
		
	}
	
	
	public static Random getDefault(){
		return factory.ingetDefault();
	}
	
	public static Random seedDefault(long seed){
		return factory.inseedDefault(seed);
	}
	
	public static Random getOrSeedDefault(long seed){
		return factory.ingetOrSeedDefault(seed);
	}
	
	
	public static Random getMapped(int id){
		return factory.ingetMapped(id);
	}
	
	public static Random seedMapped(int id, long seed){
		return factory.inseedMapped(id, seed);
	}
	
	public static Random getOrSeedMapped(int id, long seed){
		return factory.ingetOrSeedMapped(id, seed);
	}
	
	
	public static Random getMapped(String id){
		return factory.ingetMapped(id);
	}
	
	public static Random seedMapped(String id, long seed){
		return factory.inseedMapped(id, seed);
	}
	
	public static Random getOrSeedMapped(String id, long seed){
		return factory.ingetOrSeedMapped(id, seed);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public RandomFactory(){
		defaultRandom_ = null;
		intMapped = new HashMap<Integer, Random>();
		stringMapped = new HashMap<String, Random>();
	}
	
	
	public Random ingetDefault(){
		if(defaultRandom_ == null){
			defaultRandom_ = new Random();
		}
		return defaultRandom_;
	}
	
	public Random inseedDefault(long seed){
		defaultRandom_ = new Random(seed);
		return defaultRandom_;
	}
	
	public Random ingetOrSeedDefault(long seed){
		if(defaultRandom_ == null){
			defaultRandom_ = new Random(seed);
		}
		return defaultRandom_;
	}
	
	
	
	
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
	
	public Random inseedMapped(int id, long seed){
		Random r = new Random(seed);
		intMapped.put(id, r);
		return r;
	}
	
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
	
	public Random inseedMapped(String id, long seed){
		Random r = new Random(seed);
		stringMapped.put(id, r);
		return r;
	}
	
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
