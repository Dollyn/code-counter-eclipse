package com.google.code.jcodecounter.core;


/**
 * @author Dollyn
 *
 */
public class StringUtil {

	/**
	 * Returns true if the given string is an empty string.
	 * @param str the string
	 * @return <code>true</code> if the given string is empty or null
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}
}
