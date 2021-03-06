/**
 * 
 */
package com.kant.general;

import java.util.Arrays;

/**
 * http://www.geeksforgeeks.org/find-next-greater-number-set-digits/
 * 
 * Following are few observations about the next greater number. 1) If all
 * digits sorted in descending order, then output is always “Not Possible”. For
 * example, 4321. 2) If all digits are sorted in ascending order, then we need
 * to swap last two digits. For example, 1234. 3) For other cases, we need to
 * process the number from rightmost side (why? because we need to find the
 * smallest of all greater numbers)
 * 
 * You can now try developing an algorithm yourself.
 * 
 * Following is the algorithm for finding the next greater number.
 * 
 * I) Traverse the given number from rightmost digit, keep traversing till you
 * find a digit which is smaller than the previously traversed digit. For
 * example, if the input number is “534976”, we stop at 4 because 4 is smaller
 * than next digit 9. If we do not find such a digit, then output is “Not
 * Possible”.
 * 
 * II) Now search the right side of above found digit ‘d’ for the smallest digit
 * greater than ‘d’. For “534976″, the right side of 4 contains “976”. The
 * smallest digit greater than 4 is 6.
 * 
 * III) Swap the above found two digits, we get 536974 in above example.
 * 
 * IV) Now sort all digits from position next to ‘d’ to the end of number. The
 * number that we get after sorting is the output. For above example, we sort
 * digits in bold 536974. We get “536479” which is the next greater number for
 * input 534976.
 * 
 * @author shaskant
 *
 */
public class NextGreaterNumberWithSameDigits {

	char[] theNumber;

	public NextGreaterNumberWithSameDigits(String number) {
		theNumber = number.toCharArray();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NextGreaterNumberWithSameDigits prob = new NextGreaterNumberWithSameDigits(
				"534976");
		System.out.println(prob.getNextGreaterNumber());
	}

	public String getNextGreaterNumber() {
		int d = -1;
		// I) Start from the right most digit and find the first digit that is
		// smaller than the digit next to it.
		for (int i = theNumber.length - 1; i > 0; i--) {
			if (theNumber[i - 1] < theNumber[i]) {
				d = i - 1;
				break;
			}
		}
		// If no such digit is found, then all digits are in descending order
		// means there cannot be a greater number with same set of digits
		if (d == -1) {
			System.out.println("next greater number not possible");
			return null;
		}

		// II) Find the smallest digit on right side of (d)'th digit that is
		// greater than number[d]
		int x = d + 1;
		for (int j = d + 1; j < theNumber.length; j++) {
			if (theNumber[j] > theNumber[d] && theNumber[j] < theNumber[x]) {
				x = j;
			}
		}

		// III) Swap the above found smallest digit with number[d]
		swap(d, x);
		// IV) Sort the digits after (d) in ascending order
		Arrays.sort(theNumber, d + 1, theNumber.length);
		return new String(theNumber);
	}

	/**
	 * 
	 * @param i
	 * @param j
	 */
	private void swap(int i, int j) {
		char temp = theNumber[i];
		theNumber[i] = theNumber[j];
		theNumber[j] = temp;
	}

}
