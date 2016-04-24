package burlap.datastructures;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Datasturcutre for maintaining numeric aggregated values for different keys. If the value has never been queried or added to before,
 * then it assumes an initial value (zero by default). The values are backed by a HashMap.
 * @author James MacGlashan
 *
 * @param <K> the key, which must be hashable.
 */
public class HashedAggregator <K>{

	/**
	 * The backing hash map
	 */
	protected HashMap<K, Double>		storage;
	
	/**
	 * The initial value for each key
	 */
	protected double					initialValue = 0.;
	
	
	/**
	 * Initializes with initial value for each key being 0.0.
	 */
	public HashedAggregator(){
		this.storage = new HashMap<K, Double>();
	}
	
	/**
	 * Initializes with the given initial value for each key.
	 * @param initialValue the initial value associated with each key.
	 */
	public HashedAggregator(double initialValue){
		this.storage = new HashMap<K, Double>();
		this.initialValue = initialValue;
	}
	
	
	/**
	 * Adds a specified value to a key. If the key has not been index previously, its new value is this object's initialValue datamember + v.
	 * @param ind the key index
	 * @param v the value to add to the value associated with ind.
	 */
	public void add(K ind, double v){
		Double cur = storage.get(ind);
		double c = cur != null ? cur : initialValue;
		this.storage.put(ind, c+v);
	}
	
	
	/**
	 * The current value associated with key ind. This object's initialValue datamember is returned if nothing has been added to this value previously.
	 * @param ind the key index
	 * @return the value associated with the key index.
	 */
	public double v(K ind){
		Double cur = storage.get(ind);
		return cur != null ? cur : initialValue;
	}
	
	/**
	 * Returns the number of keys stored.
	 * @return the number of keys stored.
	 */
	public int size(){
		return storage.size();
	}
	
	/**
	 * The set of keys stored.
	 * @return The set of keys stored.
	 */
	public Set<K> keySet(){
		return this.storage.keySet();
	}
	
	
	/**
	 * The set of values stored.
	 * @return The set of values stored.
	 */
	public Collection<Double> valueSet(){
		return this.storage.values();
	}
	
	/**
	 * The entry set for stored keys and values.
	 * @return The entry set for stored keys and values.
	 */
	public Set<Map.Entry<K, Double>> entrySet(){
		return this.storage.entrySet();
	}
	
	
	/**
	 * Returns the HashMap that backs this object.
	 * @return the HashMap that backs this object.
	 */
	public Map<K, Double> getHashMap(){
		return this.storage;
	}

	/**
	 * Returns true if this object has a value associated with the specified key, false otherwise.
	 * @param key the key to check
	 * @return true if this object has a value associated with the specified key, false otherwise.
	 */
	public boolean containsKey(K key){
		return this.storage.containsKey(key);
	}
	
}
