package lbd.Utils;

import java.util.HashSet;
import java.util.Set;

import lbd.FSNER.Configuration.Constants;

public class StringUtils {

	public static boolean isNullOrEmpty(String pValue) {
		return pValue == null || pValue.isEmpty();
	}

	public static final Set<Character> NON_ALPHANUMERIC_SET = new HashSet<Character>();
	static {
		for(int i = 0; i < Constants.NonAlphaNumericCharacters.length(); i++) {
			NON_ALPHANUMERIC_SET.add(Constants.NonAlphaNumericCharacters.charAt(i));
		}
	}

	public static boolean isNonAlphaNumericCharacter(Character pCharacter) {
		return NON_ALPHANUMERIC_SET.contains(pCharacter);
	}
}
