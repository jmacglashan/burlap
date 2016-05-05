package burlap.mdp.core.oo.state;

import burlap.mdp.core.state.StateUtilities;

import java.util.*;

/**
 * @author James MacGlashan.
 */
public class OOStateUtilities {


	public static <T extends ObjectInstance> T objectWithName(List<T> objects, String name){
		for(T ob : objects){
			if(ob.name().equals(name)){
				return ob;
			}
		}
		return null;
	}

	public static <T extends ObjectInstance> int objectIndexWithName(List<T> objects, String name){
		for(int i = 0; i < objects.size(); i++){
			T ob = objects.get(i);
			if(ob.name().equals(name)){
				return i;
			}
		}
		return -1;
	}

	public static List<Object> flatStateKeys(OOState s){
		List<Object> flatKeys = new ArrayList<Object>();
		for(ObjectInstance o : s.objects()){
			List<Object> keys = o.variableKeys();
			for(Object key : keys){
				OOVariableKey fkey = new OOVariableKey(o.name(), key);
				flatKeys.add(fkey);
			}
		}
		return flatKeys;
	}

	public static Object get(OOState s, Object variableKey){
		OOVariableKey key = generateKey(variableKey);
		ObjectInstance o = s.object(key.obName);
		return o.get(key.obVarKey);
	}

	public static OOVariableKey generateKey(Object key){
		if(key instanceof OOVariableKey){
			return (OOVariableKey)key;
		}
		else if(key instanceof String){
			return new OOVariableKey((String)key);
		}
		throw new RuntimeException("An OOState variable Key must either be a OOVariableKey or String that can be parsed into an OOVariableKey, but key was of type " + key.getClass().getName());
	}

	public static Map<String, List<ObjectInstance>> objectsByClass(OOState s){

		Set<String> classes = objectClassesPresent(s);
		Map<String, List<ObjectInstance>> obsByClass = new HashMap<String, List<ObjectInstance>>(classes.size());
		for(String className : classes){
			List<ObjectInstance> obs = new ArrayList<ObjectInstance>(s.objectsOfClass(className));
			obsByClass.put(className, obs);
		}

		return obsByClass;

	}

	public static Set<String> objectClassesPresent(OOState s){
		List<ObjectInstance> obs = s.objects();
		Set<String> classes = new HashSet<String>(obs.size());
		for(ObjectInstance ob : obs){
			classes.add(ob.className());
		}
		return classes;
	}

	public static String ooStateToString(OOState s){
		StringBuilder buf = new StringBuilder();
		buf.append("{\n");
		for(ObjectInstance o : s.objects()){
			buf.append(objectInstanceToString(o)).append("\n");
		}
		buf.append("}");
		return buf.toString();
	}

	public static String objectInstanceToString(ObjectInstance o){
		StringBuilder buf = new StringBuilder();
		buf.append(o.name()).append(" (").append(o.className()).append("): ")
				.append(StateUtilities.stateToString(o));
		return buf.toString();
	}

	public static List <List<String>> getPossibleBindingsGivenParamOrderGroups(OOState s, String [] paramClasses, String [] paramOrderGroups){

		List <List <String>> res = new ArrayList<List<String>>();
		List <List <String>> currentBindingSets = new ArrayList <List<String>>();
		List <String> uniqueRenames = identifyUniqueClassesInParameters(paramOrderGroups);
		List <String> uniqueParamClases = identifyUniqueClassesInParameters(paramClasses);

		Map<String, List <ObjectInstance>> instanceMap = objectsByClass(s);

		//first make sure we have objects for each class parameter; if not return empty list
		for(String oclass : uniqueParamClases){
			int n = getNumOccurencesOfClassInParameters(oclass, paramClasses);
			List <ObjectInstance> objectsOfClass = instanceMap.get(oclass);
			if(objectsOfClass == null){
				return res;
			}
			if(objectsOfClass.size() < n){
				return res;
			}
		}

		getPossibleRenameBindingsHelper(res, currentBindingSets, 0, s.objects(), uniqueRenames, paramClasses, paramOrderGroups);


		return res;

	}

	public static List <String> identifyUniqueClassesInParameters(String [] paramClasses){
		List <String> unique = new ArrayList <String>();
		for(int i = 0; i < paramClasses.length; i++){
			if(!unique.contains(paramClasses[i])){
				unique.add(paramClasses[i]);
			}
		}
		return unique;
	}



	private static void getPossibleRenameBindingsHelper(List <List <String>> res, List <List <String>> currentBindingSets, int bindIndex,
												 List <ObjectInstance> remainingObjects, List <String> uniqueOrderGroups, String [] paramClasses, String [] paramOrderGroups){

		if(bindIndex == uniqueOrderGroups.size()){
			//base case, put it all together and add it to the result
			res.add(getBindingFromCombinationSet(currentBindingSets, uniqueOrderGroups, paramOrderGroups));
			return ;
		}

		//otherwise we're in the recursive case

		String r = uniqueOrderGroups.get(bindIndex);
		String c = parameterClassAssociatedWithOrderGroup(r, paramOrderGroups, paramClasses);
		List <ObjectInstance> cands = objectsMatchingClass(remainingObjects, c);
		int k = numOccurencesOfOrderGroup(r, paramOrderGroups);
		List <List <String>> combs = getAllCombinationsOfObjects(cands, k);
		for(List <String> cb : combs){

			List <List<String>> nextBinding = new ArrayList<List<String>>(currentBindingSets.size());
			for(List <String> prevBind : currentBindingSets){
				nextBinding.add(prevBind);
			}
			nextBinding.add(cb);
			List <ObjectInstance> nextObsReamining = objectListDifference(remainingObjects, cb);

			//recursive step
			getPossibleRenameBindingsHelper(res, nextBinding, bindIndex+1, nextObsReamining, uniqueOrderGroups, paramClasses, paramOrderGroups);

		}



	}


	/**
	 * for a specific parameter order group, return a possible binding
	 * @param comboSets is a list of the bindings for each order group. For instance, if the order groups for each parameter were P, Q, P, Q, R; then there would be three lists
	 * @param orderGroupAssociatedWithSet which order group each list of bindings in comboSets is for
	 * @param orderGroups the parameter order groups for each parameter
	 * @return a binding as a list of object instance names
	 */
	private static List <String> getBindingFromCombinationSet(List <List <String>> comboSets, List <String> orderGroupAssociatedWithSet, String [] orderGroups){

		List <String> res = new ArrayList <String>(orderGroups.length);
		//add the necessary space first
		for(int i = 0; i < orderGroups.length; i++){
			res.add("");
		}

		//apply the parameter bindings for each rename combination
		for(int i = 0; i < comboSets.size(); i++){
			List <String> renameCombo = comboSets.get(i);
			String r = orderGroupAssociatedWithSet.get(i);

			//find the parameter indices that match this rename and set a binding accordingly
			int ind = 0;
			for(int j = 0; j < orderGroups.length; j++){
				if(orderGroups[j].equals(r)){
					res.set(j, renameCombo.get(ind));
					ind++;
				}
			}
		}

		return res;
	}

	private static int getNumOccurencesOfClassInParameters(String className, String [] paramClasses){
		int num = 0;
		for(int i = 0; i < paramClasses.length; i++){
			if(paramClasses[i].equals(className)){
				num++;
			}
		}
		return num;
	}

	private static String parameterClassAssociatedWithOrderGroup(String orderGroup, String [] orderGroups, String [] paramClasses){
		for(int i = 0; i < orderGroups.length; i++){
			if(orderGroups[i].equals(orderGroup)){
				return paramClasses[i];
			}
		}
		return "";
	}

	private static int numOccurencesOfOrderGroup(String rename, String [] orderGroups){
		int num = 0;
		for(int i = 0; i < orderGroups.length; i++){
			if(orderGroups[i].equals(rename)){
				num++;
			}
		}

		return num;

	}


	private static List<ObjectInstance> objectsMatchingClass(List<ObjectInstance> objects, String className){
		List<ObjectInstance> filtered = new ArrayList<ObjectInstance>(objects.size());
		for(ObjectInstance ob : objects){
			if(ob.className().equals(className)){
				filtered.add(ob);
			}
		}
		return filtered;
	}


	private static List <List <String>> getAllCombinationsOfObjects(List <ObjectInstance> objects, int k){

		List <List<String>> allCombs = new ArrayList <List<String>>();

		int n = objects.size();
		int [] comb = initialComb(k, n);
		allCombs.add(getListOfBindingsFromCombination(objects, comb));
		while(nextComb(comb, k, n) == 1){
			allCombs.add(getListOfBindingsFromCombination(objects, comb));
		}

		return allCombs;

	}


	private static List <String> getListOfBindingsFromCombination(List <ObjectInstance> objects, int [] comb){
		List <String> res = new ArrayList <String>(comb.length);
		for(int i = 0; i < comb.length; i++){
			res.add(objects.get(comb[i]).name());
		}
		return res;
	}


	private static List <ObjectInstance> objectListDifference(List <ObjectInstance> objects, List <String> toRemove){
		List <ObjectInstance> remaining = new ArrayList<ObjectInstance>(objects.size());
		for(ObjectInstance oi : objects){
			String oname = oi.name();
			if(!toRemove.contains(oname)){
				remaining.add(oi);
			}
		}
		return remaining;
	}



	private static int [] initialComb(int k, int n){
		int [] res = new int[k];
		for(int i = 0; i < k; i++){
			res[i] = i;
		}

		return res;
	}

	/**
	 * Iterates through combinations.
	 * Modified code from: http://compprog.wordpress.com/tag/generating-combinations/
	 * @param comb the last combination of elements selected
	 * @param k number of elements in any combination (n choose k)
	 * @param n number of possible elements (n choose k)
	 * @return 0 when there are no more combinations; 1 when a new combination is generated
	 */
	private static int nextComb(int [] comb, int k, int n){

		int i = k-1;
		comb[i]++;

		while(i > 0 && comb[i] >= n-k+1+i){
			i--;
			comb[i]++;
		}

		if(comb[0] > n-k){
			return 0;
		}

		/* comb now looks like (..., x, n, n, n, ..., n).
		Turn it into (..., x, x + 1, x + 2, ...) */
		for(i = i+1; i < k; i++){
			comb[i] = comb[i-1] + 1;
		}

		return 1;
	}


}
