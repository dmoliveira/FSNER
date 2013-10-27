package com.lbd.test.string;

import junit.framework.Assert;
import lbd.FSNER.Configuration.Constants;
import lbd.Utils.StringUtils;

import org.junit.Test;


public class StringTest {

	@Test
	public void NonAlphaNumericTest() {

		String vNonAlphaNumericCharacters = Constants.NonAlphaNumericCharacters;

		boolean isAllNonAlphaNumericCharacters = true;

		for(int i = 0; i < vNonAlphaNumericCharacters.length(); i++) {
			if(!StringUtils.isNonAlphaNumericCharacter(vNonAlphaNumericCharacters.charAt(i))) {
				isAllNonAlphaNumericCharacters = false;
				break;
			}
		}

		Assert.assertTrue(isAllNonAlphaNumericCharacters);
	}

}
