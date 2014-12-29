package burlap.behavior.singleagent.pomdp.utils;

import java.util.ArrayList;
import java.util.List;

public class Util {
	
	/**
	 * Generates all possible probability distributions with a given dimension whose
	 * elements are all multiples of a given granularity.
	 * 
	 * @param dimension		the dimension of the generated distributions
	 * @param granularity	a number g such that each element of the probability distribution is a multiple of g
	 * @return 				all possible distributions with the given parameters
	 */
	public static List<List<Double>> makeDistro(int dimension, int granularity) {
		List<List<Double>> result = new ArrayList<List<Double>>();
		int num = multichoose(dimension, granularity);
		for(int bIndex = 0; bIndex < num; ++bIndex) {
			List<Double> temp;
			while(true) {
				temp = new ArrayList<Double>();
				for(int i = 0; i < dimension; ++i) {
					temp.add(0.0);
				}
				for(int sCount = 0; sCount < granularity; ++sCount) {
					int index = (int) (new java.util.Random().nextDouble() * dimension);
					temp.set(index, temp.get(index) + 1/(double)granularity);
				}
				if(!result.contains(temp)) {
					break;
				} else {
					continue;
				}
			}
			listNorm(temp);
			result.add(temp);
		}
		return result;
	}

	public static void listNorm(List<Double> list) {
		double sum = 0.0;
		for(int i = 0; i < list.size(); ++i) {
			sum += list.get(i);
		}
		for(int i = 0; i < list.size(); ++i) {
			list.set(i, list.get(i)/sum);
		}
	}
	
	
	
	public static List<Double> listCDF(List<Double> list) {
		double sum = 0.0;
		List<Double> returnList = new ArrayList<Double>(); 
		for(int i = 0; i < list.size(); ++i) {
			sum += list.get(i);
			returnList.add(sum);
		}
		return returnList;
	}

	
	
	public static int factorial(int n) {
		if(n == 0) {
			return 1;
		}
		return n * factorial(n - 1);
	}
	
	public static int multichoose(int n, int k) {
		return factorial(n + k - 1)/(factorial(k) * factorial(n - 1));
	}
}
