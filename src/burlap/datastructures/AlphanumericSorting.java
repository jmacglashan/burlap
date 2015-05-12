package burlap.datastructures;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author Kushal Paudyal www.icodejava.com Last Modified On 16th July 2009
 *
 *         This class is used to sort alphanumeric strings.
 *
 *         My solution is inspired from a similar C# implementation available at
 *         http://dotnetperls.com/alphanumeric-sorting written by Sam Allen
 */
public class AlphanumericSorting implements Comparator {

	/**
	 * The compare method that compares the alphanumeric strings
	 */
	public int compare(Object firstObjToCompare, Object secondObjToCompare) {
		String firstString = removePadding(firstObjToCompare.toString());
		String secondString = removePadding(secondObjToCompare.toString());

		if (secondString == null || firstString == null) {
			return 0;
		}

		int lengthFirstStr = firstString.length();
		int lengthSecondStr = secondString.length();

		int index1 = 0;
		int index2 = 0;

		while (index1 < lengthFirstStr && index2 < lengthSecondStr) {
			char ch1 = firstString.charAt(index1);
			char ch2 = secondString.charAt(index2);

			char[] space1 = new char[lengthFirstStr];
			char[] space2 = new char[lengthSecondStr];

			int loc1 = 0;
			int loc2 = 0;

			do {
				space1[loc1++] = ch1;
				index1++;

				if (index1 < lengthFirstStr) {
					ch1 = firstString.charAt(index1);
				} else {
					break;
				}
			} while (Character.isDigit(ch1) == Character.isDigit(space1[0]));

			do {
				space2[loc2++] = ch2;
				index2++;

				if (index2 < lengthSecondStr) {
					ch2 = secondString.charAt(index2);
				} else {
					break;
				}
			} while (Character.isDigit(ch2) == Character.isDigit(space2[0]));

			String str1 = new String(space1);
			String str2 = new String(space2);

			int result;

			if (Character.isDigit(space1[0]) && Character.isDigit(space2[0])) {
				Integer firstNumberToCompare = Integer.parseInt(str1.trim());
				Integer secondNumberToCompare = Integer.parseInt(str2.trim());
				result = firstNumberToCompare.compareTo(secondNumberToCompare);
			} else {
				result = str1.compareTo(str2);
			}

			if (result != 0) {
				return result;
			}
		}
		return lengthFirstStr - lengthSecondStr;
	}

	/**
	 * The purpose of this method is to remove any zero padding for numbers.
	 *
	 * Otherwise returns the input string.
	 *
	 *
	 * @param string
	 * @return
	 */
	private String removePadding(String string) {
		String result="";
		try{
			result+= Integer.parseInt(string.trim());
		} catch (Exception e) {
			result= string;
		}
		return result;
	}

	/**
	 * Testing the alphanumeric sorting
	 */
	public static void main(String[] args) {
		String[] alphaNumericStringArray = new String[] { "NUM10071",
				"NUM9999", "9997", "9998", "9996", "9996F", "0001", "01", "1", "001" };

        /*
         * Arrays.sort method can take an unsorted array and a comparator to
         * give a final sorted array.
         *
         * The sorting is done according to the comparator that we have
         * provided.
         */
		Arrays.sort(alphaNumericStringArray, new AlphanumericSorting());

		for (int i = 0; i < alphaNumericStringArray.length; i++) {
			System.out.println(alphaNumericStringArray[i]);
		}

	}

}
